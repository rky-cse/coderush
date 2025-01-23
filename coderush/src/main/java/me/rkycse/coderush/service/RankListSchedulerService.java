package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
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
    private final RedisTemplate<String, List<RankDTO>> rankListRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TournamentService tournamentService;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);

    public RankListSchedulerService(RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate,
                             RedisTemplate<String, List<RankDTO>> rankListRedisTemplate,
                             SimpMessagingTemplate messagingTemplate,
                             TournamentService tournamentService) {
        this.tournamentRedisTemplate = tournamentRedisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.tournamentService = tournamentService;
        this.rankListRedisTemplate = rankListRedisTemplate;
        startScheduling();
    }

    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::checkAndSendRankLists, 0, 5, TimeUnit.SECONDS);
    }

    private void checkAndSendRankLists() {
        try {
            // Fetch all keys starting with "$"
            Set<String> keys = tournamentRedisTemplate.keys("$*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            for (String key : keys) {
                Long tournamentId = extractTournamentId(key);
                if (tournamentId == null) {
                    continue;
                }

                // Check if ranklist exists with key "@" + tournamentId
                String rankListKey = "@" + tournamentId;
                Boolean rankListExists = rankListRedisTemplate.hasKey(rankListKey);
                if (Boolean.TRUE.equals(rankListExists)) {
                    List<RankDTO> rankList = (List<RankDTO>) rankListRedisTemplate.opsForValue().get(rankListKey);
                    if (rankList != null) {
                        // Send ranklist to the topic
                        System.out.println("sending rank list to tournament: " + tournamentId);
                        messagingTemplate.convertAndSend(
                                "/topic/rankList" + tournamentId,
                                rankList
                        );
                    }
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