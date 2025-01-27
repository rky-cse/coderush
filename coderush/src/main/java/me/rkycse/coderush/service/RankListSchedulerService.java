package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.QuestionEntity;
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

    private final RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate;
    private final RedisTemplate<String, RankDTO> rankListRedisTemplate;
    private final RedisTemplate<String,UserTestcaseDTO>userTestcaseRedisTemplate;
    private final RedisTemplate<String, List<QuestionEntity>>tournamentQuestionRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TournamentService tournamentService;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);

    public RankListSchedulerService(RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate, RedisTemplate<String, RankDTO> rankListRedisTemplate, RedisTemplate<String, UserTestcaseDTO> userTestcaseRedisTemplate, RedisTemplate<String, List<QuestionEntity>> tournamentQuestionRedisTemplate, SimpMessagingTemplate messagingTemplate, TournamentService tournamentService) {
        this.tournamentRedisTemplate = tournamentRedisTemplate;
        this.rankListRedisTemplate = rankListRedisTemplate;
        this.userTestcaseRedisTemplate = userTestcaseRedisTemplate;
        this.tournamentQuestionRedisTemplate = tournamentQuestionRedisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.tournamentService = tournamentService;
    }


    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::checkAndSendRankLists, 0, 5, TimeUnit.SECONDS);
    }

    private void checkAndSendRankLists() {
        try {
            // Fetch all keys starting with "$"
            Set<String> keys = tournamentRedisTemplate.keys("$*");
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
                Set<String> rankKeys = rankListRedisTemplate.keys("rankDTO/"+tournamentId
                +"/*");
                if(rankKeys == null || rankKeys.isEmpty()) {
                    System.out.println("No ranks found for tournament "+tournamentId);
                    return;
                }

                RankListDTO rankListDTO=new RankListDTO();
                rankListDTO.setTournamentId(tournamentId);

                for(String rankKey : rankKeys) {
                    RankWithUserTestcaseDTO rankWithUserTestcaseDTO=new RankWithUserTestcaseDTO();
                    RankDTO rankDTO=rankListRedisTemplate.opsForValue().get(rankKey);
                    Set<String>userTestcaseKeys=userTestcaseRedisTemplate
                            .keys("userTestcaseDTO/"+tournamentId+"/"+rankDTO.getUserName()+"/*");
                    if(userTestcaseKeys == null || userTestcaseKeys.isEmpty()) {
                        continue;
                    }
                    rankWithUserTestcaseDTO.setId(rankDTO.getId());
                    rankWithUserTestcaseDTO.setUserName(rankDTO.getUserName());
                    rankWithUserTestcaseDTO.setScore(rankDTO.getScore());
                    rankWithUserTestcaseDTO.setTournamentId(tournamentId);

                    for(String userTestcaseKey : userTestcaseKeys) {
                        UserTestcaseDTO userTestcaseDTO=userTestcaseRedisTemplate
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