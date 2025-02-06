package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class TournamentSchedulerService {

    private final TournamentRepository tournamentRepository;
    private final RedisTemplate<String,QuestionDTO> questionsDTORedisTemplate;

    private final QuestionRepository questionRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate;
    private final RedisTemplate<String, RankDTO> rankRedisTemplate;
    private final RankRepository rankRepository;
    private final RedisTemplate<String, List<QuestionEntity>> questionListRedisTemplate;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TestcaseRepository testcaseRepository;
    private final RedisTemplate<String, TestcaseDTO> testcaseDTORedisTemplate;
    private final RedisTemplate<String, UserTestcaseDTO> userTestcaseDTORedisTemplate;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public TournamentSchedulerService(
            TournamentRepository tournamentRepository, RedisTemplate<String, QuestionDTO> questionsDTORedisTemplate,
            QuestionRepository questionRepository,
            ThreadPoolTaskScheduler taskScheduler,
            RedisTemplate<String, TournamentCacheDTO> tournamentRedisTemplate,
            RedisTemplate<String, RankDTO> rankRedisTemplate,
            RankRepository rankRepository,
            RedisTemplate<String, List<QuestionEntity>> questionListRedisTemplate,
            TournamentPlayerRepository tournamentPlayerRepository,
            TestcaseRepository testcaseRepository,
            RedisTemplate<String, TestcaseDTO> testcaseDTORedisTemplate,
            RedisTemplate<String, UserTestcaseDTO> userTestcaseDTORedisTemplate
    ) {
        this.tournamentRepository = tournamentRepository;
        this.questionsDTORedisTemplate = questionsDTORedisTemplate;
        this.questionRepository = questionRepository;
        this.taskScheduler = taskScheduler;
        this.tournamentRedisTemplate = tournamentRedisTemplate;
        this.rankRedisTemplate = rankRedisTemplate;
        this.rankRepository = rankRepository;
        this.questionListRedisTemplate = questionListRedisTemplate;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.testcaseRepository = testcaseRepository;
        this.testcaseDTORedisTemplate = testcaseDTORedisTemplate;
        this.userTestcaseDTORedisTemplate = userTestcaseDTORedisTemplate;
    }

    public void startScheduling() {
        executorService.scheduleAtFixedRate(this::fetchAndStoreTournaments, 0, 5, TimeUnit.SECONDS);
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
                if (!Boolean.TRUE.equals(tournamentRedisTemplate.hasKey(key))) {
                    TournamentCacheDTO cacheDTO = new TournamentCacheDTO(tournamentId, startTime, durationInSeconds, false);
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
            if (keys == null || keys.isEmpty()) return;

            for (String key : keys) {
                TournamentCacheDTO cacheDTO = tournamentRedisTemplate.opsForValue().get(key);
                if (cacheDTO == null || cacheDTO.isScheduled()) continue;

                long delay = LocalDateTime.now().until(cacheDTO.getStartTime(), java.time.temporal.ChronoUnit.MILLIS);
                if (delay <= 0 && !cacheDTO.isScheduled() ) {
                    tournamentRedisTemplate.delete(key);
                } else {
                    taskScheduler.schedule(
                            () -> startTournament(cacheDTO.getTournamentId(), cacheDTO),
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

    private void startTournament(Long tournamentId, TournamentCacheDTO cacheDTO) {
        try {
            System.out.println("Starting tournament with ID: " + tournamentId + " at " + LocalDateTime.now());
            tournamentRedisTemplate.opsForValue().set("$" + tournamentId, cacheDTO, cacheDTO.getDurationInSeconds(), TimeUnit.SECONDS);

            List<TournamentPlayerEntity> tournamentPlayerEntities=tournamentPlayerRepository
                    .findByTournamentId(tournamentId);

            if(tournamentPlayerEntities == null || tournamentPlayerEntities.isEmpty()){
                System.out.println("No tournament found for ID: " + tournamentId);
            }
            else{
                System.out.println("Found tournament with ID: " + tournamentId);
            }


            for(TournamentPlayerEntity tournamentPlayerEntity : tournamentPlayerEntities) {
                RankDTO rankDTO = new RankDTO();
                rankDTO.setTournamentId(tournamentId);
                rankDTO.setScore(0);
                rankDTO.setUserName(tournamentPlayerEntity.getPlayerUserName());

                System.out.println("inserting rank in tournament:");
                rankRedisTemplate.opsForValue().set(
                        "rankDTO/" + tournamentId + "/" + rankDTO.getUserName(),
                        rankDTO,
                        cacheDTO.getDurationInSeconds(),
                        TimeUnit.SECONDS
                );

            }

            List<QuestionEntity> allQuestions = questionRepository.findAll();

            List<QuestionEntity> selectedQuestions = new ArrayList<>();

            Set<QuestionEntity> st=new HashSet<>();
            for (int i = 0;i<5; i++) {
                int randomIndex = (int) (Math.random() * allQuestions.size());
                if(st.contains(allQuestions.get(randomIndex))){
                    int j=randomIndex;
                    int ct=allQuestions.size();
                    while(ct-->0){
                        if(!st.contains(allQuestions.get(j%(allQuestions.size())))){
                            selectedQuestions.add(allQuestions.get(j%(allQuestions.size())));
                            st.add(allQuestions.get(j%(allQuestions.size())));
                            break;
                        }
                        j++;
                    }

                }
                else{
                    st.add(allQuestions.get(randomIndex));
                    selectedQuestions.add(allQuestions.get(randomIndex));
                }

            }
            int index=0;

            for (QuestionEntity question : selectedQuestions) {
                System.out.println("QuestionId: " +question.getQuestionId() );
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

                        testcaseDTORedisTemplate.opsForValue().set(
                                "testcaseDTO/"+tournamentId+"/"+ player.getPlayerUserName()
                                        +"/"+index,
                                testcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        userTestcaseDTORedisTemplate.opsForValue().set(
                                "userTestcaseDTO/" +tournamentId+"/"+
                                        player.getPlayerUserName() + "/" + index,
                                userTestcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        questionsDTORedisTemplate.opsForValue().set(
                                "questionDTO/"+tournamentId+"/"+index,
                                Mapper.toDTO(question),
                                cacheDTO.getDurationInSeconds(),TimeUnit.SECONDS
                        );
                    }
                }
                index++;
            }

            //questionListRedisTemplate.opsForValue().set("questionListEntity/" + tournamentId, selectedQuestions, cacheDTO.getDuration(), TimeUnit.SECONDS);
            tournamentRedisTemplate.delete("tournament:" + tournamentId);
        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
    }
}



