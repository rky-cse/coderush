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
        try {
            logger.info("✅ Rank list scheduler tick...");

            // 1. Process pending rating updates
            Set<String> ratingUpdateKeys = redisTemplate.keys("rating-update/*");
            if (ratingUpdateKeys != null) {
                for (String key : ratingUpdateKeys) {
                    try {
                        TournamentCacheDTO tournamentCacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get(key);
                        if (tournamentCacheDTO == null) continue;

                        Long currentTime = TimeUtil.getCurrentEpochMillis();
                        Long startTime = tournamentCacheDTO.getStartTime();
                        Long tournamentDuration = tournamentCacheDTO.getDurationInSeconds() * 1000L;
                        long endTime = startTime + tournamentDuration;

                        Object value = redisTemplate.opsForValue().get("judgeCount/" + tournamentCacheDTO.getTournamentId());
                        Long count = 0L;

                        if (value instanceof Integer) {
                            count = ((Integer) value).longValue();
                        } else if (value instanceof Long) {
                            count = (Long) value;
                        }

                        logger.info("🔄 Evaluating key={} | currentTime={} | endTime={} | count={}", key, currentTime, endTime, count);

                        if (currentTime > endTime + 7777L && count == 0L) {
                            logger.info("🚀 Sending rating update for tournament: {}", tournamentCacheDTO.getTournamentId());
                            redisTemplate.delete(key);
                            producer.sendRatingUpdate(tournamentCacheDTO.getTournamentId());
                            producer.sendRecentActivityUpdate(tournamentCacheDTO.getTournamentId());
                            redisTemplate.delete("judgeCount/" + tournamentCacheDTO.getTournamentId());
                        }
                    } catch (Exception ex) {
                        logger.error("❌ Error processing rating-update key: {}", key, ex);
                    }
                }
            }

            // 2. Process rank list broadcasting
            Set<String> tournamentKeys = redisTemplate.keys("$*");
            if (tournamentKeys == null || tournamentKeys.isEmpty()) {
                logger.info("ℹ️ No tournament keys found in Redis.");
                return;
            }

            for (String key : tournamentKeys) {
                try {
                    Long tournamentId = extractTournamentId(key);
                    TournamentCacheDTO tournamentCacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get(key);

                    if (tournamentId == null || tournamentCacheDTO == null) {
                        logger.warn("⚠️ Skipping tournament for invalid key: {}", key);
                        continue;
                    }

                    Set<String> rankKeys = redisTemplate.keys("rankDTO/" + tournamentId + "/*");
                    if (rankKeys == null || rankKeys.isEmpty()) {
                        logger.info("ℹ️ No ranks found for tournament {}", tournamentId);
                        continue;
                    }

                    RankListDTO rankListDTO = new RankListDTO();
                    rankListDTO.setTournamentId(tournamentId);
                    long endTime = tournamentCacheDTO.getStartTime() + (1000L * tournamentCacheDTO.getDurationInSeconds());
                    rankListDTO.setEndTime(endTime);

                    for (String rankKey : rankKeys) {
                        try {
                            RankDTO rankDTO = (RankDTO) redisTemplate.opsForValue().get(rankKey);
                            if (rankDTO == null) continue;

                            RankWithFreeStyleSubmissionDTO fullDTO = new RankWithFreeStyleSubmissionDTO();
                            fullDTO.setId(rankDTO.getId());
                            fullDTO.setUserName(rankDTO.getUserName());
                            fullDTO.setScore(rankDTO.getScore());
                            fullDTO.setTournamentId(tournamentId);
                            fullDTO.setPenalty(rankDTO.getPenalty());

                            Set<String> submissionKeys = redisTemplate.keys("SubmissionStatusDTO/" + tournamentId + "/" + rankDTO.getUserName() + "/*");
                            if (submissionKeys != null) {
                                for (String submissionKey : submissionKeys) {
                                    SubmissionStatusDTO sub = (SubmissionStatusDTO) redisTemplate.opsForValue().get(submissionKey);
                                    if (sub != null) {
                                        fullDTO.getFreeStyleSubmissionDTOS().add(sub);
                                    }
                                }
                            }

                            rankListDTO.getRankList().add(fullDTO);
                        } catch (Exception ex) {
                            logger.error("❌ Error processing rankDTO key: {}", rankKey, ex);
                        }
                    }

                    if (rankListDTO.getRankList().isEmpty()) {
                        logger.info("ℹ️ Rank list is empty for tournament {}", tournamentId);
                        continue;
                    }

                    rankListDTO.sortByScore();
                    List<RankWithFreeStyleSubmissionDTO> finalRankList = rankListDTO.getRankList();

                    for (int i = 0; i < finalRankList.size(); i++) {
                        String userName = finalRankList.get(i).getUserName();
                        UserRankDTO userRankDTO = new UserRankDTO();
                        userRankDTO.setUserName(userName);
                        userRankDTO.setCurrentRank((long) (i + 1));
                        userRankDTO.setEndTime(endTime);
                        userRankDTO.setRankWithFreeStyleSubmissionDTO(finalRankList.get(i));

                        messagingTemplate.convertAndSend("/topic/userRank/" + tournamentId + "/" + userName, userRankDTO);
                    }

                    long currentTime = TimeUtil.getCurrentEpochMillis();
                    if ((endTime - currentTime) <= 10000 && (endTime - currentTime) > 0) {
                        String ratingUpdateKey = "rating-update/" + tournamentId;
                        Boolean isSet = redisTemplate.opsForValue().setIfAbsent(ratingUpdateKey, tournamentCacheDTO);
                        if (Boolean.TRUE.equals(isSet)) {
                            logger.info("🕒 Scheduled rating update for tournament: {}", tournamentId);
                        }
                    }
                } catch (Exception ex) {
                    logger.error("❌ Error processing tournament key: {}", key, ex);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Fatal error in rank list scheduler loop", e);
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
