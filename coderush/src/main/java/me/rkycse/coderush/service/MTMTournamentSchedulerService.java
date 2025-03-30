package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MTMTournamentSchedulerService.class);

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
        logger.info("Starting tournament scheduler...");
        executorService.scheduleAtFixedRate(this::fetchAndStoreTournaments, 0, 20, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::scheduleTournaments, 0, 4, TimeUnit.SECONDS);
    }

    private void fetchAndStoreTournaments() {
        try {
            Long now = TimeUtil.getCurrentEpochMillis();
            Long upperLimit = now + 28000L;
            List<Object[]> tournaments = tournamentBaseRepository.findTournamentsBetween(now, upperLimit);

            if (tournaments.isEmpty()) {
                logger.info("No upcoming tournaments found in the given time range.");
                return;
            }

            for (Object[] row : tournaments) {
                Long tournamentId = (Long) row[0];
                Long startTime = (Long) row[1];
                Long durationInSeconds = (Long) row[2];
                TournamentBaseEntity.TournamentType tournamentType = (TournamentBaseEntity.TournamentType) row[3]; // Fixed Casting
                Long penaltyFactor = (Long) row[4];

                logger.info("tournament type:{}", tournamentType);

                String key = "tournament:" + tournamentId;
                if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                    TournamentCacheDTO cacheDTO = new TournamentCacheDTO();
                    cacheDTO.setTournamentId(tournamentId);
                    cacheDTO.setStartTime(startTime);
                    cacheDTO.setDurationInSeconds(durationInSeconds);
                    cacheDTO.setTournamentType(tournamentType);
                    cacheDTO.setScheduled(Boolean.FALSE);
                    cacheDTO.setPenaltyFactor(penaltyFactor);
                    redisTemplate.opsForValue().set(key, cacheDTO, 28, TimeUnit.SECONDS);

                    logger.info("Stored tournament {} in Redis with key {}", tournamentId, key);
                }
            }
        } catch (Exception e) {
            logger.error("Error while fetching and storing tournaments", e);
        }
    }


    private void scheduleTournaments() {
        try {
            Set<String> keys = redisTemplate.keys("tournament:*");
            if (keys == null || keys.isEmpty()) {
                logger.info("No tournaments found in Redis for scheduling.");
                return;
            }

            for (String key : keys) {
                TournamentCacheDTO cacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get(key);
                if (cacheDTO == null || cacheDTO.isScheduled()) continue;

                long delay = cacheDTO.getStartTime();
                if (delay <= 0 && !cacheDTO.isScheduled()) {
                    redisTemplate.delete(key);
                    logger.info("Deleted expired tournament from Redis: {}", key);
                } else {
                    taskScheduler.schedule(
                            () -> startTournament(cacheDTO.getTournamentId(), cacheDTO),
                            Date.from(Instant.ofEpochMilli(cacheDTO.getStartTime()))
                    );

                    cacheDTO.setScheduled(true);
                    redisTemplate.opsForValue().set(key, cacheDTO, 28, TimeUnit.SECONDS);
                    logger.info("Scheduled tournament {} to start at {}", cacheDTO.getTournamentId(), cacheDTO.getStartTime());
                }
            }
        } catch (Exception e) {
            logger.error("Error while scheduling tournaments", e);
        }
    }

    private void startTournament(Long tournamentId, TournamentCacheDTO cacheDTO) {
        try {
            logger.info("Starting tournament with ID: {} at {}", tournamentId, LocalDateTime.now());
            redisTemplate.opsForValue().set("$" + tournamentId, cacheDTO, cacheDTO.getDurationInSeconds(), TimeUnit.SECONDS);
            producer.startTournamentInit(cacheDTO);
        } catch (Exception e) {
            logger.error("Error while starting tournament {}", tournamentId, e);
        }
    }
}
