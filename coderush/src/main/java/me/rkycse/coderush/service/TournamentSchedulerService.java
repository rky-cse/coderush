package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.repository.TournamentRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TournamentSchedulerService {

    private final TournamentRepository tournamentRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final RedisTemplate<String, TournamentCacheDTO> redisTemplate;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public TournamentSchedulerService(
            TournamentRepository tournamentRepository,
            ThreadPoolTaskScheduler taskScheduler,
            RedisTemplate<String, TournamentCacheDTO> redisTemplate
    ) {
        this.tournamentRepository = tournamentRepository;
        this.taskScheduler = taskScheduler;
        this.redisTemplate = redisTemplate;
    }

    public void startScheduling() {
        // Fetch tournaments every 5 seconds
        executorService.scheduleAtFixedRate(this::fetchAndStoreTournaments, 0, 5, TimeUnit.SECONDS);

        // Schedule tournaments every second (or pick another interval)
        executorService.scheduleAtFixedRate(this::scheduleTournaments, 0, 1, TimeUnit.SECONDS);
    }

    private void fetchAndStoreTournaments() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime upperLimit = now.plusSeconds(7);

            var tournaments = tournamentRepository.findTournamentsBetween(now, upperLimit);

            tournaments.forEach(row -> {
                Long tournamentId = (Long) row[0];
                LocalDateTime startTime = (LocalDateTime) row[1];

                String key = "tournament:" + tournamentId;
                // Only store if not present
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    TournamentCacheDTO cacheDTO = new TournamentCacheDTO(tournamentId, startTime, false);
                    redisTemplate.opsForValue().set(key, cacheDTO, 7, TimeUnit.SECONDS);
                }
            });
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }

    private void scheduleTournaments() {
        try {
            Set<String> keys = redisTemplate.keys("tournament:*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            for (String key : keys) {
                TournamentCacheDTO cacheDTO = redisTemplate.opsForValue().get(key);
                if (cacheDTO == null || cacheDTO.isScheduled()) {
                    continue;
                }

                long delay = LocalDateTime.now().until(cacheDTO.getStartTime(), java.time.temporal.ChronoUnit.MILLIS);
                if (delay <= 0) {
                    redisTemplate.delete(key);
                } else {
                    taskScheduler.schedule(
                            () -> startTournament(cacheDTO.getTournamentId()),
                            Date.from(cacheDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant())
                    );
                    cacheDTO.setScheduled(true);
                    redisTemplate.opsForValue().set(key, cacheDTO, 7, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }

    private void startTournament(Long tournamentId) {
        System.out.println("Starting tournament with ID: " + tournamentId + " at " + LocalDateTime.now());
        // Additional logic
        // Remove from Redis if desired
        redisTemplate.delete("tournament:" + tournamentId);
    }
}