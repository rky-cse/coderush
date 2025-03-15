package me.rkycse.coderush.kafka;

import me.rkycse.coderush.dto.FreeStyleSubmissionStatusDTO;
import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.util.TimeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
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

    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final UserTestcaseRepository userTestcaseRepository;

    public Consumer(RedisTemplate<String, Object> redisTemplate, QuestionRepository questionRepository, TestcaseRepository testcaseRepository, TournamentPlayerRepository tournamentPlayerRepository, TournamentQuestionRepository tournamentQuestionRepository,  RankRepository rankRepository, UserRepository userRepository, UserTestcaseRepository userTestcaseRepository) {
        this.redisTemplate = redisTemplate;
        this.questionRepository = questionRepository;
        this.testcaseRepository = testcaseRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.tournamentQuestionRepository = tournamentQuestionRepository;

        this.rankRepository = rankRepository;
        this.userRepository = userRepository;
        this.userTestcaseRepository = userTestcaseRepository;
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
    public void consume(FreeStyleSubmissionStatus userTestcase) {
        System.out.println("consuming.............."+ userTestcase);
        if(userTestcase!=null) {
            FreeStyleSubmissionStatus oldUserTestcase=userTestcaseRepository.
                    findByUserNameAndTournamentIdAndTestcaseId(userTestcase.getUserName(),
                            userTestcase.getTournamentId(),userTestcase.getTestcaseId())
                    .orElse(null);
            if(oldUserTestcase==null) {
                userTestcaseRepository.save(userTestcase);
            }
            else{
                oldUserTestcase.setNumberOfAttempts(userTestcase.getNumberOfAttempts());
                oldUserTestcase.setSolved(userTestcase.getSolved());
                userTestcaseRepository.save(oldUserTestcase);
            }
        }


    }
    @KafkaListener(topics = "start-tournament-init", groupId = "myGroup")
    public void consume(TournamentCacheDTO cacheDTO) {
        System.out.println("consuming.............................");
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
                int randomIndex = (int) (Math.random() * testcases.size());
                TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));

                if (testcaseDTO != null) {
                    FreeStyleSubmissionStatusDTO freeStyleSubmissionStatusDTO = new FreeStyleSubmissionStatusDTO();
                    freeStyleSubmissionStatusDTO.setTournamentId(tournamentId);
                    freeStyleSubmissionStatusDTO.setTestcaseId(testcaseDTO.getTestcaseId());
                    freeStyleSubmissionStatusDTO.setUserName(player.getPlayerUserName());
                    freeStyleSubmissionStatusDTO.setSolved(false);
                    freeStyleSubmissionStatusDTO.setSubmissionTime(TimeUtil.getCurrentEpochMillis());
                    freeStyleSubmissionStatusDTO.setNumberOfAttempts(0);

                    try {
                        userTestcaseRepository.save(Mapper.toEntity(freeStyleSubmissionStatusDTO));
                    } catch (Exception e) {
                        System.err.println("Failed to save FreeStyleSubmissionStatus for user: " + player.getPlayerUserName()
                                + ". Error: " + e.getMessage());
                    }

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

                    try {
                        redisTemplate.opsForValue().set(
                                "freeStyleSubmissionStatusDTO/" + tournamentId + "/" + player.getPlayerUserName() + "/" + index,
                                freeStyleSubmissionStatusDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to set freeStyleSubmissionStatusDTO for user: " + player.getPlayerUserName() + ". Error: " + e.getMessage());
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


}
