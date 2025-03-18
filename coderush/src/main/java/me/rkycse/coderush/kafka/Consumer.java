package me.rkycse.coderush.kafka;

import me.rkycse.coderush.controller.TournamentWebSocketController;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.service.TournamentWebSocketService;
import me.rkycse.coderush.util.JsonConverter;
import me.rkycse.coderush.util.TimeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class Consumer {


    private final RedisTemplate<String,Object>redisTemplate;
    private final QuestionRepository questionRepository;
    private final TestcaseRepository testcaseRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TournamentQuestionRepository tournamentQuestionRepository;
    private final TournamentWebSocketService tournamentWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final SubmissionStatusRepository submissionStatusRepository;

    public Consumer(RedisTemplate<String, Object> redisTemplate, QuestionRepository questionRepository, TestcaseRepository testcaseRepository, TournamentPlayerRepository tournamentPlayerRepository, TournamentQuestionRepository tournamentQuestionRepository, TournamentWebSocketService tournamentWebSocketService, SimpMessagingTemplate messagingTemplate, RankRepository rankRepository, UserRepository userRepository, SubmissionStatusRepository submissionStatusRepository) {
        this.redisTemplate = redisTemplate;
        this.questionRepository = questionRepository;
        this.testcaseRepository = testcaseRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.tournamentQuestionRepository = tournamentQuestionRepository;
        this.tournamentWebSocketService = tournamentWebSocketService;
        this.messagingTemplate = messagingTemplate;
        this.rankRepository = rankRepository;
        this.userRepository = userRepository;
        this.submissionStatusRepository = submissionStatusRepository;
    }

    @KafkaListener(topics = "rank-update", groupId = "myGroup")
    public void consume(RankEntity rank) {
        System.out.println("consuming.............................");
        if(rank != null) {
            RankEntity oldRank=rankRepository.findByUserNameAndTournamentId(rank.getUserName(), rank.getTournamentId());
            if(oldRank==null) {
                rankRepository.save(rank);
            }
            else{
                oldRank.setScore(rank.getScore());
                rankRepository.save(oldRank);

            }
        }
    }

    @KafkaListener(topics="user-testcase-update",groupId = "myGroup")
    public void consume(SubmissionStatus submissionStatus) {
        System.out.println("consuming.............."+ submissionStatus);
        if(submissionStatus!=null) {
            SubmissionStatus oldsubmissionStatus=submissionStatusRepository.
                    findByUserNameAndTournamentIdAndQuestionId(submissionStatus.getUserName(),
                            submissionStatus.getTournamentId(),submissionStatus.getQuestionId())
                    .orElse(null);
            if(oldsubmissionStatus==null) {
                submissionStatusRepository.save(submissionStatus);
            }
            else{
                oldsubmissionStatus.setNumberOfAttempts(submissionStatus.getNumberOfAttempts());
                oldsubmissionStatus.setSolved(submissionStatus.getSolved());
                submissionStatusRepository.save(oldsubmissionStatus);
            }
        }


    }
    @KafkaListener(topics = "start-tournament-init", groupId = "myGroup")
    public void consume(TournamentCacheDTO cacheDTO) {
        System.out.println("consuming............................."+ cacheDTO);

        if (TournamentBaseEntity.TournamentType.FREE_STYLE.equals(cacheDTO.getTournamentType())) {
            System.out.println("FREE STYLE");
        }
        else{
            System.out.println("CLASSIC");
        }
        Long tournamentId = cacheDTO.getTournamentId();

        if (tournamentId == null) {
            System.out.println("Tournament ID is null. Exiting consume method.");
            return;
        }

        // Retrieve tournament players and check if available
        List<TournamentPlayerEntity> tournamentPlayerEntities = tournamentPlayerRepository.findByTournamentId(tournamentId);
        if (tournamentPlayerEntities == null || tournamentPlayerEntities.isEmpty()) {
            System.out.println("No tournament players found for tournament ID: " + tournamentId);
            return;
        } else {
            System.out.println("Found tournament players for tournament ID: " + tournamentId);
        }

        // Insert a rank entry for each tournament player in Redis
        for (TournamentPlayerEntity playerEntity : tournamentPlayerEntities) {
            RankDTO rankDTO = new RankDTO();
            rankDTO.setTournamentId(tournamentId);
            rankDTO.setScore(0);
            rankDTO.setUserName(playerEntity.getPlayerUserName());
            rankDTO.setPenalty(0L);

            System.out.println("Inserting rank in tournament for user: " + rankDTO.getUserName());
            try {
                redisTemplate.opsForValue().set(
                        "rankDTO/" + tournamentId + "/" + rankDTO.getUserName(),
                        rankDTO,
                        cacheDTO.getDurationInSeconds(),
                        TimeUnit.SECONDS
                );
            } catch (Exception e) {
                System.err.println("Failed to insert rank for user: " + rankDTO.getUserName() + ". Error: " + e.getMessage());
            }
        }

        // Retrieve questions and check if available
        List<QuestionEntity> allQuestions = questionRepository.findAll();
        if (allQuestions == null || allQuestions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }

        // Shuffle and select up to 5 questions
        List<QuestionEntity> selectedQuestions = new ArrayList<>();
        Collections.shuffle(allQuestions);
        int numQuestions = Math.min(5, allQuestions.size());
        for (int i = 0; i < numQuestions; i++) {
            selectedQuestions.add(allQuestions.get(i));
        }

        int index = 0;
        // Process each selected question
        for (QuestionEntity question : selectedQuestions) {
            System.out.println("QuestionId: " + question.getQuestionId());
            TournamentQuestionEntity tournamentQuestionEntity = new TournamentQuestionEntity();
            tournamentQuestionEntity.setQuestionId(question.getQuestionId());
            tournamentQuestionEntity.setTournamentId(tournamentId);
            System.out.println("Processing tournament question: " + tournamentQuestionEntity);

            // Check if tournament question already exists and save if not
            try {
                TournamentQuestionEntity existingEntity = tournamentQuestionRepository
                        .findByTournamentIdAndQuestionId(tournamentId, question.getQuestionId());
                if (existingEntity == null) {
                    TournamentQuestionEntity savedTournamentQuestion = tournamentQuestionRepository.save(tournamentQuestionEntity);
                    System.out.println("Saved tournament question: " + savedTournamentQuestion);
                } else {
                    System.out.println("Tournament question already exists: " + existingEntity);
                }
            } catch (Exception e) {
                System.err.println("Error saving tournament question: " + tournamentQuestionEntity + ". Error: " + e.getMessage());
            }

            // Retrieve test cases for the question
            List<TestcaseEntity> testcases = testcaseRepository.findByQuestionId(question.getQuestionId());
            // For each tournament player, assign a random testcase (if available)
            for (TournamentPlayerEntity player : tournamentPlayerEntities) {
                if (testcases == null || testcases.isEmpty()) {
                    continue;
                }
                SubmissionStatusDTO SubmissionStatusDTO = new SubmissionStatusDTO();
                SubmissionStatusDTO.setTournamentId(tournamentId);
                SubmissionStatusDTO.setQuestionId(question.getQuestionId());
                SubmissionStatusDTO.setUserName(player.getPlayerUserName());
                SubmissionStatusDTO.setSolved(false);
                SubmissionStatusDTO.setSubmissionTime(TimeUtil.getCurrentEpochMillis());
                SubmissionStatusDTO.setNumberOfAttempts(0);

                try {
                    submissionStatusRepository.save(Mapper.toEntity(SubmissionStatusDTO));
                } catch (Exception e) {
                    System.err.println("Failed to save SubmissionStatus for user: " + player.getPlayerUserName()
                            + ". Error: " + e.getMessage());
                }
                try {
                    redisTemplate.opsForValue().set(
                            "SubmissionStatusDTO/" + tournamentId + "/" + player.getPlayerUserName() + "/" + index,
                            SubmissionStatusDTO,
                            cacheDTO.getDurationInSeconds(),
                            TimeUnit.SECONDS
                    );
                } catch (Exception e) {
                    System.err.println("Failed to set SubmissionStatusDTO for user: " + player.getPlayerUserName() + ". Error: " + e.getMessage());
                }
                try {
                    redisTemplate.opsForValue().set(
                            "questionDTO/" + tournamentId + "/" + index,
                            Mapper.toDTO(question),
                            cacheDTO.getDurationInSeconds(),
                            TimeUnit.SECONDS
                    );
                } catch (Exception e) {
                    System.err.println("Failed to set questionDTO for tournament question: " + question.getQuestionId()
                            + ". Error: " + e.getMessage());
                }



                if (TournamentBaseEntity.TournamentType.FREE_STYLE.equals(cacheDTO.getTournamentType())) {

                    int randomIndex = (int) (Math.random() * testcases.size());
                    TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));


                    try {
                        redisTemplate.opsForValue().set(
                                "testcaseDTO/" + tournamentId + "/" + player.getPlayerUserName() + "/" + index,
                                testcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to set testcaseDTO for user: " + player.getPlayerUserName() + ". Error: " + e.getMessage());
                    }


                }
            }
            index++;
        }

        // Optionally, delete the tournament key from Redis after processing
        try {
            redisTemplate.delete("tournament:" + tournamentId);
        } catch (Exception e) {
            System.err.println("Failed to delete tournament key from Redis for tournament ID: " + tournamentId
                    + ". Error: " + e.getMessage());
        }
    }

    @KafkaListener(topics="classical-submission-response",groupId = "myGroup")
    public void consumeClassicSubmissionResponse(String classicSubmissionResponse) {
        System.out.println("ClassicSubmissionResponse: " + classicSubmissionResponse);
        // convert to object

        ClassicSubmissionResponseDTO classicSubmissionResponseDTO=
                JsonConverter.fromJson(classicSubmissionResponse,
                        ClassicSubmissionResponseDTO.class);

        tournamentWebSocketService.classicRankAndSubUpdate(classicSubmissionResponseDTO);
        String userName=classicSubmissionResponseDTO.getUsername();
        int index=classicSubmissionResponseDTO.getIndex();
        messagingTemplate.convertAndSend("/topic/tournament/classicSubmit/" + userName+"/" + index, classicSubmissionResponseDTO);
    }




}