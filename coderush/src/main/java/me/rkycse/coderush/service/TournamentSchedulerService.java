package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.dto.TournamentPlayerDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.TestcaseEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TournamentSchedulerService {



    private final TournamentRepository tournamentRepository;
    private final QuestionRepository questionRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate;
    private final RedisTemplate<String, List<RankDTO>>rankListRedisTemplate;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final RankRepository rankRepository;
    private final RedisTemplate<String,List<QuestionEntity>> questionListRedisTemplate;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TestcaseRepository testcaseRepository;
    private final RedisTemplate<String,TestcaseDTO>testcaseDTORedisTemplate;


    public TournamentSchedulerService(
            TournamentRepository tournamentRepository,
            ThreadPoolTaskScheduler taskScheduler,
            RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate,
            RedisTemplate<String, List<RankDTO>> rankListRedisTemplate,
            RankRepository rankRepository, QuestionRepository questionRepository,
            RedisTemplate<String, List<QuestionEntity>> questionListRedisTemplate, TournamentPlayerRepository tournamentPlayerRepository, TestcaseRepository testcaseRepository,
            RedisTemplate<String, TestcaseDTO> testcaseDTORedisTemplate) {
        this.tournamentRepository = tournamentRepository;

        this.taskScheduler = taskScheduler;
        this.tournamentRedisTemplate = tournamentRedisTemplate;
        this.rankListRedisTemplate = rankListRedisTemplate;
        this.rankRepository = rankRepository;
        this.questionRepository = questionRepository;
        this.questionListRedisTemplate = questionListRedisTemplate;

        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.testcaseRepository = testcaseRepository;
        this.testcaseDTORedisTemplate = testcaseDTORedisTemplate;
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
                Long durationInSeconds = (Long) row[2];

                String key = "tournament:" + tournamentId;
                // Only store if not present
                if (!Boolean.TRUE.equals(tournamentRedisTemplate.hasKey(key))) {
                    TournamentCacheDTO cacheDTO = new TournamentCacheDTO(tournamentId, startTime,durationInSeconds, false);
                    tournamentRedisTemplate.opsForValue().set(key, cacheDTO, 7, TimeUnit.SECONDS);
                }
            });
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }

    private void scheduleTournaments() {
        try {
            Set<String> keys = tournamentRedisTemplate.keys("tournament:*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            for (String key : keys) {
                TournamentCacheDTO cacheDTO = tournamentRedisTemplate.opsForValue().get(key);
                if (cacheDTO == null || cacheDTO.isScheduled()) {
                    continue;
                }

                long delay = LocalDateTime.now().until(cacheDTO.getStartTime(), java.time.temporal.ChronoUnit.MILLIS);
                if (delay <= 0) {
                    tournamentRedisTemplate.delete(key);
                } else {
                    taskScheduler.schedule(
                            () -> startTournament(cacheDTO.getTournamentId(),cacheDTO),
                            Date.from(cacheDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant())
                    );
                    cacheDTO.setScheduled(true);
                    tournamentRedisTemplate.opsForValue().set(key, cacheDTO, 7, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }

    private void startTournament(Long tournamentId,TournamentCacheDTO cacheDTO) {
        System.out.println("Starting tournament with ID: " + tournamentId + " at " + LocalDateTime.now());
        // Additional logic
        // Remove from Redis if desired
        tournamentRedisTemplate.opsForValue().set("$"+tournamentId, cacheDTO, cacheDTO.getDuration(), TimeUnit.SECONDS);
        List<RankEntity> rankListEntity= rankRepository.findByTournamentId(tournamentId);
        if(rankListEntity != null) {

            List<RankDTO>rankListDTO=new ArrayList<>();
            for(RankEntity rankEntity:rankListEntity) {
                RankDTO rankDTO= Mapper.toDTO(rankEntity);
                if(rankDTO != null) {
                    rankListDTO.add(rankDTO);
                }
            }
            rankListRedisTemplate.opsForValue().set("@"+tournamentId, rankListDTO, cacheDTO.getDuration(), TimeUnit.SECONDS);
        }
        List<QuestionEntity> questions=questionRepository.findAll();



        for(int i=0;i<5;i++) {
            int randomIndex = (int) (Math.random()*questions.size());
            questions.add(questions.get(randomIndex));
        }

        List<TournamentPlayerEntity> tournamentPlayers=
                tournamentPlayerRepository.findByTournamentId(tournamentId);
        List<TestcaseDTO>tournamentTestcases=new ArrayList<>();

        for(QuestionEntity questionEntity:questions) {
            List<TestcaseEntity> testcases=testcaseRepository
                    .findByQuestionId(questionEntity.getQuestionId());

            for(TournamentPlayerEntity tournamentPlayerEntity:tournamentPlayers) {
                int randomIndex = (int) (Math.random()*testcases.size());
                TestcaseDTO testcaseDTO=Mapper.toDTO(testcases.get(randomIndex));
                testcaseDTORedisTemplate.opsForValue()
                        .set("!"+questionEntity.getQuestionId()+"/"+tournamentPlayerEntity
                                .getPlayerUserName(),testcaseDTO
                                ,cacheDTO.getDuration(),TimeUnit.SECONDS);


            }
        }

        questionListRedisTemplate.opsForValue().set("%"+tournamentId, questions, cacheDTO.getDuration(), TimeUnit.SECONDS);

        tournamentRedisTemplate.delete("tournament:" + tournamentId);
    }
}