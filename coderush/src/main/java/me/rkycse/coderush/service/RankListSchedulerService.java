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

                for(String rankKey : rankKeys) {
                    RankWithUserTestcaseDTO rankWithUserTestcaseDTO=new RankWithUserTestcaseDTO();
                    RankDTO rankDTO=(RankDTO) redisTemplate.opsForValue().get(rankKey);
                    Set<String>userTestcaseKeys=redisTemplate
                            .keys("userTestcaseDTO/"+tournamentId+"/"+rankDTO.getUserName()+"/*");
                    if(userTestcaseKeys == null || userTestcaseKeys.isEmpty()) {
                        continue;
                    }
                    rankWithUserTestcaseDTO.setId(rankDTO.getId());
                    rankWithUserTestcaseDTO.setUserName(rankDTO.getUserName());
                    rankWithUserTestcaseDTO.setScore(rankDTO.getScore());
                    rankWithUserTestcaseDTO.setTournamentId(tournamentId);

                    for(String userTestcaseKey : userTestcaseKeys) {
                        UserTestcaseDTO userTestcaseDTO=(UserTestcaseDTO) redisTemplate
                                .opsForValue().get(userTestcaseKey);
                        if(userTestcaseDTO == null) {
                            continue;
                        }
                        rankWithUserTestcaseDTO
                                .getUserTestcases()
                                .add(userTestcaseDTO);
                    }
                    rankListDTO.getRankList().add(rankWithUserTestcaseDTO);


                }

                if (rankListDTO != null) {
                    // Send ranklist to the topic
                    System.out.println("sending rank list to tournament: "
                            + tournamentId);
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