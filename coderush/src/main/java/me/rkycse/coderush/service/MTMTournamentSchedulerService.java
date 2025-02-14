package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
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

    public MTMTournamentSchedulerService(TournamentBaseRepository tournamentBaseRepository, RedisTemplate<String, Object> redisTemplate, QuestionRepository questionRepository, ThreadPoolTaskScheduler taskScheduler, TournamentPlayerRepository tournamentPlayerRepository, TestcaseRepository testcaseRepository) {
        this.tournamentBaseRepository = tournamentBaseRepository;
        this.redisTemplate = redisTemplate;
        this.questionRepository = questionRepository;
        this.taskScheduler = taskScheduler;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.testcaseRepository = testcaseRepository;
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

            List<TournamentPlayerEntity> tournamentPlayerEntities = tournamentPlayerRepository
                    .findByTournamentId(tournamentId);

            if (tournamentPlayerEntities == null || tournamentPlayerEntities.isEmpty()) {
                System.out.println("No tournament found for ID: " + tournamentId);
            } else {
                System.out.println("Found tournament with ID: " + tournamentId);
            }


            for (TournamentPlayerEntity tournamentPlayerEntity : tournamentPlayerEntities) {
                RankDTO rankDTO = new RankDTO();
                rankDTO.setTournamentId(tournamentId);
                rankDTO.setScore(0);
                rankDTO.setUserName(tournamentPlayerEntity.getPlayerUserName());

                System.out.println("inserting rank in tournament:");
                redisTemplate.opsForValue().set(
                        "rankDTO/" + tournamentId + "/" + rankDTO.getUserName(),
                        rankDTO,
                        cacheDTO.getDurationInSeconds(),
                        TimeUnit.SECONDS
                );

            }

            List<QuestionEntity> allQuestions = questionRepository.findAll();

            List<QuestionEntity> selectedQuestions = new ArrayList<>();

            Set<QuestionEntity> st = new HashSet<>();
            for (int i = 0; i < 5; i++) {
                int randomIndex = (int) (Math.random() * allQuestions.size());
                if (st.contains(allQuestions.get(randomIndex))) {
                    int j = randomIndex;
                    int ct = allQuestions.size();
                    while (ct-- > 0) {
                        if (!st.contains(allQuestions.get(j % (allQuestions.size())))) {
                            selectedQuestions.add(allQuestions.get(j % (allQuestions.size())));
                            st.add(allQuestions.get(j % (allQuestions.size())));
                            break;
                        }
                        j++;
                    }

                } else {
                    st.add(allQuestions.get(randomIndex));
                    selectedQuestions.add(allQuestions.get(randomIndex));
                }

            }
            int index = 0;

            for (QuestionEntity question : selectedQuestions) {
                System.out.println("QuestionId: " + question.getQuestionId());
                List<TestcaseEntity> testcases = testcaseRepository.
                        findByQuestionId(question.getQuestionId());
                for (TournamentPlayerEntity player : tournamentPlayerEntities) {
                    if (testcases.isEmpty()) continue;
                    int randomIndex = (int) (Math.random() * testcases.size());
                    TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));

                    if (testcaseDTO != null) {
                        UserTestcaseDTO userTestcaseDTO = new UserTestcaseDTO();
                        userTestcaseDTO.setTestcaseId(testcaseDTO.getTestcaseId());
                        userTestcaseDTO.setUserName(player.getPlayerUserName());
                        userTestcaseDTO.setIsSolved(false);
                        userTestcaseDTO.setNumberOfAttempts(0);

                        redisTemplate.opsForValue().set(
                                "testcaseDTO/" + tournamentId + "/" + player.getPlayerUserName()
                                        + "/" + index,
                                testcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        redisTemplate.opsForValue().set(
                                "userTestcaseDTO/" + tournamentId + "/" +
                                        player.getPlayerUserName() + "/" + index,
                                userTestcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        redisTemplate.opsForValue().set(
                                "questionDTO/" + tournamentId + "/" + index,
                                Mapper.toDTO(question),
                                cacheDTO.getDurationInSeconds(), TimeUnit.SECONDS
                        );
                    }
                }
                index++;
            }

            //questionListRedisTemplate.opsForValue().set("questionListEntity/" + tournamentId, selectedQuestions, cacheDTO.getDuration(), TimeUnit.SECONDS);
            redisTemplate.delete("tournament:" + tournamentId);
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }
}




