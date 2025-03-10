package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class MTMTournamentSchedulerService {

    private final TournamentBaseRepository tournamentBaseRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final QuestionRepository questionRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TestcaseRepository testcaseRepository;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final Producer producer;

    public MTMTournamentSchedulerService(TournamentBaseRepository tournamentBaseRepository, RedisTemplate<String, Object> redisTemplate, QuestionRepository questionRepository, ThreadPoolTaskScheduler taskScheduler, TournamentPlayerRepository tournamentPlayerRepository, TestcaseRepository testcaseRepository, Producer producer) {
        this.tournamentBaseRepository = tournamentBaseRepository;
        this.redisTemplate = redisTemplate;
        this.questionRepository = questionRepository;
        this.taskScheduler = taskScheduler;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.testcaseRepository = testcaseRepository;
        this.producer = producer;
    }


    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::fetchAndStoreTournaments, 0, 5, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::scheduleTournaments, 0, 1, TimeUnit.SECONDS);
    }

    private void fetchAndStoreTournaments() {
        try {
            Long now = (Long) TimeUtil.getCurrentEpochMillis();
            Long upperLimit = now + 7000L;
            var tournaments = tournamentBaseRepository.findTournamentsBetween(now, upperLimit);
            if (tournaments.isEmpty()) {
                System.out.println("No tournaments found in next 7 second");
            }
            else{
                System.out.println("Found " + tournaments.size() + " tournaments");
            }

            tournaments.forEach(row -> {
                Long tournamentId = (Long) row[0];
                Long startTime = (Long) row[1];
                Long durationInSeconds = (Long) row[2];

                String key = "tournament:" + tournamentId;
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    TournamentCacheDTO cacheDTO = new TournamentCacheDTO(tournamentId, startTime, durationInSeconds, false);
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
            if (keys == null || keys.isEmpty()) return;

            for (String key : keys) {
                TournamentCacheDTO cacheDTO = (TournamentCacheDTO)redisTemplate.opsForValue().get(key);
                if (cacheDTO == null || cacheDTO.isScheduled()) continue;

                long delay = cacheDTO.getStartTime();
                if (delay <= 0 && !cacheDTO.isScheduled()) {
                    redisTemplate.delete(key);
                } else {
                    taskScheduler.schedule(
                            () -> startTournament(cacheDTO.getTournamentId(), cacheDTO),
                            Date.from(Instant.ofEpochMilli(cacheDTO.getStartTime()))
                    );

                    cacheDTO.setScheduled(true);
                    redisTemplate.opsForValue().set(key, cacheDTO, 7, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }

    private void startTournament(Long tournamentId, TournamentCacheDTO cacheDTO) {
        try {
            System.out.println("Starting tournament with ID: " + tournamentId + " at " + LocalDateTime.now());
            redisTemplate.opsForValue().set("$" + tournamentId, cacheDTO, cacheDTO.getDurationInSeconds(), TimeUnit.SECONDS);
            producer.startTournamentInit(cacheDTO);

        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }
}




