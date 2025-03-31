package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
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

    public RankListSchedulerService(RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::checkAndSendRankLists, 0, 5, TimeUnit.SECONDS);
        logger.info("Started rank list scheduling.");
    }

    private void checkAndSendRankLists() {
        try {
            // Fetch all keys starting with "$"
            Set<String> keys = redisTemplate.keys("$*");
            if (keys == null || keys.isEmpty()) {
                logger.info("No tournament keys found");
                return;
            }

            for (String key : keys) {
                Long tournamentId = extractTournamentId(key);
                TournamentCacheDTO tournamentCacheDTO = (TournamentCacheDTO)
                        redisTemplate.opsForValue().get(key);

                if (tournamentId == null) {
                    logger.warn("Skipping tournament with id {}", tournamentId);
                    continue;
                }
                Set<String> rankKeys = redisTemplate.keys("rankDTO/" + tournamentId + "/*");
                if (rankKeys == null || rankKeys.isEmpty()) {
                    logger.info("No ranks found for tournament {}", tournamentId);
                    return;
                }

                RankListDTO rankListDTO = new RankListDTO();
                rankListDTO.setTournamentId(tournamentId);
                rankListDTO.setEndTime(tournamentCacheDTO.getStartTime() +
                        (1000L * tournamentCacheDTO.getDurationInSeconds()));

                for (String rankKey : rankKeys) {
                    RankWithFreeStyleSubmissionDTO rankWithFreeStyleSubmissionDTO = new RankWithFreeStyleSubmissionDTO();
                    RankDTO rankDTO = (RankDTO) redisTemplate.opsForValue().get(rankKey);
                    Set<String> submissionStatusKeys = redisTemplate
                            .keys("SubmissionStatusDTO/" + tournamentId + "/" + rankDTO.getUserName() + "/*");
                    if (submissionStatusKeys == null || submissionStatusKeys.isEmpty()) {
                        continue;
                    }
                    rankWithFreeStyleSubmissionDTO.setId(rankDTO.getId());
                    rankWithFreeStyleSubmissionDTO.setUserName(rankDTO.getUserName());
                    rankWithFreeStyleSubmissionDTO.setScore(rankDTO.getScore());
                    rankWithFreeStyleSubmissionDTO.setTournamentId(tournamentId);
                    rankWithFreeStyleSubmissionDTO.setPenalty(rankDTO.getPenalty());

                    for (String submissionStatusKey : submissionStatusKeys) {
                        SubmissionStatusDTO submissionStatusDTO = (SubmissionStatusDTO) redisTemplate
                                .opsForValue().get(submissionStatusKey);
                        if (submissionStatusDTO == null) {
                            continue;
                        }
                        rankWithFreeStyleSubmissionDTO
                                .getFreeStyleSubmissionDTOS()
                                .add(submissionStatusDTO);
                    }
                    rankListDTO.getRankList().add(rankWithFreeStyleSubmissionDTO);
                }

                if (rankListDTO != null) {
                    // Send rank list to the topic
                    logger.info("Sending rank list to tournament: {}", tournamentId);
                    rankListDTO.sortByScore();
                    Long endTime = rankListDTO.getEndTime();
                    List<RankWithFreeStyleSubmissionDTO> rankWithFreeStyleSubmissionDTO = rankListDTO.getRankList();
                    for(int i=0;i<rankWithFreeStyleSubmissionDTO.size();i++) {
                        String userName = rankWithFreeStyleSubmissionDTO.get(i).getUserName();


                        UserRankDTO userRankDTO=new UserRankDTO();
                        userRankDTO.setUserName(userName);
                        userRankDTO.setCurrentRank((long) (i + 1));
                        userRankDTO.setEndTime(endTime);
                        userRankDTO.setRankWithFreeStyleSubmissionDTO(rankWithFreeStyleSubmissionDTO.get(i));

                        messagingTemplate.convertAndSend("/topic/userRank/"+tournamentId+"/"+userName, userRankDTO);
                        //4999
                        if(userRankDTO.getEndTime()- TimeUtil.getCurrentEpochMillis()<=5000
                                && userRankDTO.getEndTime()- TimeUtil.getCurrentEpochMillis()>0){

                        }

                    }

//                    messagingTemplate.convertAndSend(
//                            "/topic/rankList" + tournamentId,
//                            rankListDTO
//                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while checking and sending rank lists", e);
        }
    }

    private Long extractTournamentId(String key) {
        try {
            return Long.parseLong(key.substring(1)); // Remove the "$" prefix
        } catch (NumberFormatException e) {
            logger.error("Invalid tournament key format: {}", key, e);
            return null;
        }
    }
}
