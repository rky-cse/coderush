package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RankListSchedulerService {


    private final RedisTemplate<String, Object>redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);

    public RankListSchedulerService(RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::checkAndSendRankLists, 0, 5, TimeUnit.SECONDS);
    }

    private void checkAndSendRankLists() {
        try {
            // Fetch all keys starting with "$"
            Set<String> keys = redisTemplate.keys("$*");
            if (keys == null || keys.isEmpty()) {
                System.out.println("No tournament keys found");
                return;
            }

            for (String key : keys) {
                Long tournamentId = extractTournamentId(key);
                TournamentCacheDTO tournamentCacheDTO =(TournamentCacheDTO)
                        redisTemplate.opsForValue().get(key);

                if (tournamentId == null) {
                    System.out.println("Skipping tournament with id " + tournamentId);
                    continue;
                }
                Set<String> rankKeys = redisTemplate.keys("rankDTO/"+tournamentId
                +"/*");
                if(rankKeys == null || rankKeys.isEmpty()) {
                    System.out.println("No ranks found for tournament "+tournamentId);
                    return;
                }

                RankListDTO rankListDTO=new RankListDTO();
                rankListDTO.setTournamentId(tournamentId);
                rankListDTO.setEndTime(tournamentCacheDTO.getStartTime()+
                        (1000L*tournamentCacheDTO.getDurationInSeconds()));

                for(String rankKey : rankKeys) {
                    RankWithFreeStyleSubmissionDTO rankWithFreeStyleSubmissionDTO =new RankWithFreeStyleSubmissionDTO();
                    RankDTO rankDTO=(RankDTO) redisTemplate.opsForValue().get(rankKey);
                    Set<String>SubmissionStatusKeys=redisTemplate
                            .keys("SubmissionStatusDTO/"+tournamentId+"/"+rankDTO.getUserName()+"/*");
                    if(SubmissionStatusKeys == null || SubmissionStatusKeys.isEmpty()) {
                        continue;
                    }
                    rankWithFreeStyleSubmissionDTO.setId(rankDTO.getId());
                    rankWithFreeStyleSubmissionDTO.setUserName(rankDTO.getUserName());
                    rankWithFreeStyleSubmissionDTO.setScore(rankDTO.getScore());
                    rankWithFreeStyleSubmissionDTO.setTournamentId(tournamentId);

                    for(String SubmissionStatusKey : SubmissionStatusKeys) {
                        SubmissionStatusDTO SubmissionStatusDTO =(SubmissionStatusDTO) redisTemplate
                                .opsForValue().get(SubmissionStatusKey);
                        if(SubmissionStatusDTO == null) {
                            continue;
                        }
                        rankWithFreeStyleSubmissionDTO
                                .getFreeStyleSubmissionDTOS()
                                .add(SubmissionStatusDTO);
                    }
                    rankListDTO.getRankList().add(rankWithFreeStyleSubmissionDTO);


                }

                if (rankListDTO != null) {
                    // Send ranklist to the topic
                    System.out.println("sending rank list to tournament: "
                            + tournamentId);
                    rankListDTO.sortByScore();
                    messagingTemplate.convertAndSend(
                            "/topic/rankList" + tournamentId,
                            rankListDTO
                    );
                }


            }
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging in production
        }
    }

    private Long extractTournamentId(String key) {
        try {
            return Long.parseLong(key.substring(1)); // Remove the "$" prefix
        } catch (NumberFormatException e) {
            // Invalid key format
            return null;
        }
    }
}