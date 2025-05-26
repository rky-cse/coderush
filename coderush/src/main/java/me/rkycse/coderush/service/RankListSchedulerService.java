package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.RankListDTO;
import me.rkycse.coderush.dto.RankWithFreeStyleSubmissionDTO;
import me.rkycse.coderush.dto.SubmissionStatusDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.dto.UserRankDTO;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RankListSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(RankListSchedulerService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);
    private final UserRepository userRepository;
    private final Producer producer;

    public RankListSchedulerService(RedisTemplate<String, Object> redisTemplate,
                                    SimpMessagingTemplate messagingTemplate,
                                    UserRepository userRepository,
                                    Producer producer) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.producer = producer;
    }

    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::checkAndSendRankLists, 0, 5, TimeUnit.SECONDS);
        logger.info("Started rank list scheduling.");
    }

    private void checkAndSendRankLists() {
        // 1. Process pending rating updates first.
        Set<String> ratingUpdateKeys = redisTemplate.keys("rating-update/*");
        if (ratingUpdateKeys != null) {
            for (String key : ratingUpdateKeys) {

                TournamentCacheDTO tournamentCacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get(key);
                if (tournamentCacheDTO == null) {
                    continue;
                }
                Long currentTime = TimeUtil.getCurrentEpochMillis();
                Long startTime = tournamentCacheDTO.getStartTime();
                Long tournamentDuration = tournamentCacheDTO.getDurationInSeconds() * 1000L;
                long endTime = startTime + tournamentDuration;
                System.out.println("Updating rank list: " + key+"currentTime: "+currentTime+" endTime: "+endTime);
                Long count=(Long)redisTemplate.opsForValue().get("judgeCount/"+tournamentCacheDTO.getTournamentId());
                if(count==null) {
                    count =0L;
                }

                if (currentTime > endTime+7777L && count ==0L ) {
                    logger.info("Time to update ratings for tournament: {}", tournamentCacheDTO.getTournamentId());
                    redisTemplate.delete(key);
                    producer.sendRatingUpdate(tournamentCacheDTO.getTournamentId());
                    producer.sendRecentActivityUpdate(tournamentCacheDTO.getTournamentId());
                    redisTemplate.delete("judgeCount/"+tournamentCacheDTO.getTournamentId());
                }
            }
        }

        // 2. Process tournaments and send rank lists.
        Set<String> tournamentKeys = redisTemplate.keys("$*");
        if (tournamentKeys == null || tournamentKeys.isEmpty()) {
            logger.info("No tournament keys found");
            return;
        }

        for (String key : tournamentKeys) {
            Long tournamentId = extractTournamentId(key);
            TournamentCacheDTO tournamentCacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get(key);

            if (tournamentId == null || tournamentCacheDTO == null) {
                logger.warn("Skipping tournament for key: {}", key);
                continue;
            }

            Set<String> rankKeys = redisTemplate.keys("rankDTO/" + tournamentId + "/*");
            if (rankKeys == null || rankKeys.isEmpty()) {
                logger.info("No ranks found for tournament {}", tournamentId);
                continue;
            }

            RankListDTO rankListDTO = new RankListDTO();
            rankListDTO.setTournamentId(tournamentId);
            long endTime = tournamentCacheDTO.getStartTime() + (1000L * tournamentCacheDTO.getDurationInSeconds());
            rankListDTO.setEndTime(endTime);

            // Build the rank list.
            for (String rankKey : rankKeys) {
                RankDTO rankDTO = (RankDTO) redisTemplate.opsForValue().get(rankKey);
                if (rankDTO == null) {
                    continue;
                }
                RankWithFreeStyleSubmissionDTO rankWithFreeStyleSubmissionDTO = new RankWithFreeStyleSubmissionDTO();
                rankWithFreeStyleSubmissionDTO.setId(rankDTO.getId());
                rankWithFreeStyleSubmissionDTO.setUserName(rankDTO.getUserName());
                rankWithFreeStyleSubmissionDTO.setScore(rankDTO.getScore());
                rankWithFreeStyleSubmissionDTO.setTournamentId(tournamentId);
                rankWithFreeStyleSubmissionDTO.setPenalty(rankDTO.getPenalty());

                Set<String> submissionStatusKeys = redisTemplate.keys("SubmissionStatusDTO/" + tournamentId + "/" + rankDTO.getUserName() + "/*");
                if (submissionStatusKeys != null) {
                    for (String submissionStatusKey : submissionStatusKeys) {
                        SubmissionStatusDTO submissionStatusDTO = (SubmissionStatusDTO) redisTemplate.opsForValue().get(submissionStatusKey);
                        if (submissionStatusDTO != null) {
                            rankWithFreeStyleSubmissionDTO.getFreeStyleSubmissionDTOS().add(submissionStatusDTO);
                        }
                    }
                }
                rankListDTO.getRankList().add(rankWithFreeStyleSubmissionDTO);
            }

            if (rankListDTO.getRankList().isEmpty()) {
                logger.info("Rank list is empty for tournament {}", tournamentId);
                continue;
            }

            // Sort the rank list before sending.
            rankListDTO.sortByScore();
            List<RankWithFreeStyleSubmissionDTO> rankWithFreeStyleSubmissionDTOs = rankListDTO.getRankList();

            // Send rank list updates to each user.
            for (int i = 0; i < rankWithFreeStyleSubmissionDTOs.size(); i++) {
                String userName = rankWithFreeStyleSubmissionDTOs.get(i).getUserName();

                UserRankDTO userRankDTO = new UserRankDTO();
                userRankDTO.setUserName(userName);
                userRankDTO.setCurrentRank((long) (i + 1));
                userRankDTO.setEndTime(endTime);
                userRankDTO.setRankWithFreeStyleSubmissionDTO(rankWithFreeStyleSubmissionDTOs.get(i));

                messagingTemplate.convertAndSend("/topic/userRank/" + tournamentId + "/" + userName, userRankDTO);
            }

            // Check if the tournament is about to end (within the next 10 seconds)
            long currentTime = TimeUtil.getCurrentEpochMillis();
            if ((endTime - currentTime) <= 10000 && (endTime - currentTime) > 0) {
                String ratingUpdateKey = "rating-update/" + tournamentId;
                // Set the key if not already present and call the producer only once.
                Boolean isSet = redisTemplate.opsForValue().setIfAbsent(ratingUpdateKey, tournamentCacheDTO);
                if (Boolean.TRUE.equals(isSet)) {
                    logger.info("Scheduling rating update for tournament: {}", tournamentId);
                    //producer.sendRatingUpdate(tournamentId);//is error ne mera 2 ghanta khaya hai
                }
            }
        }
    }

    private Long extractTournamentId(String key) {
        try {
            // Assuming the tournament key is prefixed with "$", remove it.
            return Long.parseLong(key.substring(1));
        } catch (NumberFormatException e) {
            logger.error("Invalid tournament key format: {}", key, e);
            return null;
        }
    }
}
