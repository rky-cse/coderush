package me.rkycse.coderush.kafka;

import me.rkycse.coderush.controller.TournamentWebSocketController;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.service.ClassicSubmissionService;
import me.rkycse.coderush.service.TournamentWebSocketService;
import me.rkycse.coderush.util.JsonConverter;
import me.rkycse.coderush.util.TimeUtil;
import org.apache.catalina.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final QuestionRepository questionRepository;
    private final TestcaseRepository testcaseRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TournamentQuestionRepository tournamentQuestionRepository;
    private final TournamentWebSocketService tournamentWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClassicSubmissionService classicSubmissionService;

    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final SubmissionStatusRepository submissionStatusRepository;

    private final Producer producer;

    public Consumer(RedisTemplate<String, Object> redisTemplate, QuestionRepository questionRepository, TestcaseRepository testcaseRepository, TournamentPlayerRepository tournamentPlayerRepository, TournamentQuestionRepository tournamentQuestionRepository, TournamentWebSocketService tournamentWebSocketService, SimpMessagingTemplate messagingTemplate, ClassicSubmissionService classicSubmissionService, RankRepository rankRepository, UserRepository userRepository, SubmissionStatusRepository submissionStatusRepository, Producer producer) {
        this.redisTemplate = redisTemplate;
        this.questionRepository = questionRepository;
        this.testcaseRepository = testcaseRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.tournamentQuestionRepository = tournamentQuestionRepository;
        this.tournamentWebSocketService = tournamentWebSocketService;
        this.messagingTemplate = messagingTemplate;
        this.classicSubmissionService = classicSubmissionService;
        this.rankRepository = rankRepository;
        this.userRepository = userRepository;
        this.submissionStatusRepository = submissionStatusRepository;
        this.producer = producer;
    }

    @KafkaListener(topics = "rank-update", groupId = "myGroup")
    public void consume(RankEntity rank) {

        logger.warn("consuming.............................",rank);
        System.out.println(rank);
        if(rank == null){
            System.out.println("rank null kaise hai iski maa ka bhosda\n\n\n\n");
        }
        if (rank != null) {
            RankEntity oldRank = rankRepository.findByUserNameAndTournamentId(rank.getUserName(), rank.getTournamentId());
            if (oldRank == null) {
                rankRepository.save(rank);
                logger.info("Saved new rank for user: {}", rank.getUserName());
            } else {
                oldRank.setScore(rank.getScore());
                oldRank.setPenalty(rank.getPenalty());
                oldRank.setRating(rank.getRating());
                rankRepository.save(oldRank);
                logger.info("Updated rank for user: {}", rank.getUserName());
            }
        }
    }

    @KafkaListener(topics = "user-testcase-update", groupId = "myGroup")
    public void consume(SubmissionStatus submissionStatus) {

        logger.info("consuming..............{}", submissionStatus);
        if (submissionStatus != null) {
            SubmissionStatus oldsubmissionStatus = submissionStatusRepository
                    .findByUserNameAndTournamentIdAndQuestionId(submissionStatus.getUserName(),
                            submissionStatus.getTournamentId(), submissionStatus.getQuestionId())
                    .orElse(null);
            if (oldsubmissionStatus == null) {
                submissionStatusRepository.save(submissionStatus);
                logger.info("Saved new SubmissionStatus for user: {}", submissionStatus.getUserName());
            } else {
                oldsubmissionStatus.setNumberOfAttempts(submissionStatus.getNumberOfAttempts());
                oldsubmissionStatus.setSolved(submissionStatus.getSolved());
                submissionStatusRepository.save(oldsubmissionStatus);
                logger.info("Updated SubmissionStatus for user: {}", submissionStatus.getUserName());
            }
        }
    }

    @KafkaListener(topics = "start-tournament-init", groupId = "myGroup")
    public void consume(TournamentCacheDTO cacheDTO) {
        
        logger.info("consuming.............................{}", cacheDTO);

        if (TournamentBaseEntity.TournamentType.FREE_STYLE.equals(cacheDTO.getTournamentType())) {
            System.out.println("FREE STYLE");
            logger.info("Tournament type: FREE STYLE");
        } else {
            System.out.println("CLASSIC");
            logger.info("Tournament type: CLASSIC");
        }
        Long tournamentId = cacheDTO.getTournamentId();

        if (tournamentId == null) {
            logger.error("Tournament ID is null. Exiting consume method.");
            return;
        }

        // Retrieve tournament players and check if available
        List<TournamentPlayerEntity> tournamentPlayerEntities = tournamentPlayerRepository.findByTournamentId(tournamentId);

        if (tournamentPlayerEntities == null || tournamentPlayerEntities.isEmpty()) {
            logger.error("No tournament players found for tournament ID: {}", tournamentId);
            return;
        } else {
            logger.info("Found tournament players for tournament ID: {}", tournamentId);
            System.out.println("playerEntity\n\n\n\n\n");
        }

        // Insert a rank entry for each tournament player in Redis
        for (TournamentPlayerEntity playerEntity : tournamentPlayerEntities) {
            RankDTO rankDTO = new RankDTO();
            rankDTO.setTournamentId(tournamentId);
            rankDTO.setScore(0);
            rankDTO.setUserName(playerEntity.getPlayerUserName());
            rankDTO.setPenalty(0L);
            rankDTO.setRating(playerEntity.getRating());

            logger.info("playerEntity",playerEntity.getRating(),"rankDto",rankDTO.getRating());
            System.out.println("playerEntity\n\n\n\n\n"+playerEntity.getRating()+"rankDto"+rankDTO.getRating());
            RankEntity startingRank=Mapper.toEntity(rankDTO);
            producer.sendRankUpdate(startingRank);

            logger.info("Inserting rank in tournament for user: {}", rankDTO.getUserName());
            try {
                redisTemplate.opsForValue().set(
                        "rankDTO/" + tournamentId + "/" + rankDTO.getUserName(),
                        rankDTO,
                        cacheDTO.getDurationInSeconds(),
                        TimeUnit.SECONDS
                );
                logger.info("Inserted rankDTO for user: {} in Redis", rankDTO.getUserName());
            } catch (Exception e) {

                logger.error("Failed to insert rank for user: {}. Error: {}", rankDTO.getUserName(), e.getMessage());
            }
        }

        // Retrieve questions and check if available
        List<QuestionEntity> allQuestions = questionRepository.findAll();
        if (allQuestions == null || allQuestions.isEmpty()) {

            logger.error("No questions available.");
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
            logger.info("Processing QuestionId: {}", question.getQuestionId());
            TournamentQuestionEntity tournamentQuestionEntity = new TournamentQuestionEntity();
            tournamentQuestionEntity.setQuestionId(question.getQuestionId());
            tournamentQuestionEntity.setTournamentId(tournamentId);
            logger.info("Processing tournament question: {}", tournamentQuestionEntity);

            // Check if tournament question already exists and save if not
            try {
                TournamentQuestionEntity existingEntity = tournamentQuestionRepository
                        .findByTournamentIdAndQuestionId(tournamentId, question.getQuestionId());
                if (existingEntity == null) {
                    TournamentQuestionEntity savedTournamentQuestion = tournamentQuestionRepository.save(tournamentQuestionEntity);
                    logger.info("Saved tournament question: {}", savedTournamentQuestion);
                } else {
                    logger.info("Tournament question already exists: {}", existingEntity);
                }
            } catch (Exception e) {
                logger.error("Error saving tournament question: {}. Error: {}", tournamentQuestionEntity, e.getMessage());
            }

            // For each tournament player, assign a random testcase (if available)
            for (TournamentPlayerEntity player : tournamentPlayerEntities) {

                SubmissionStatusDTO SubmissionStatusDTO = new SubmissionStatusDTO();
                SubmissionStatusDTO.setTournamentId(tournamentId);
                SubmissionStatusDTO.setQuestionId(question.getQuestionId());
                SubmissionStatusDTO.setUserName(player.getPlayerUserName());
                SubmissionStatusDTO.setSolved(false);
                SubmissionStatusDTO.setSubmissionTime(TimeUtil.getCurrentEpochMillis());
                SubmissionStatusDTO.setNumberOfAttempts(0);

                try {
                    submissionStatusRepository.save(Mapper.toEntity(SubmissionStatusDTO));
                    logger.info("Saved SubmissionStatus for user: {}", player.getPlayerUserName());
                } catch (Exception e) {

                    logger.error("Failed to save SubmissionStatus for user: {}. Error: {}", player.getPlayerUserName(), e.getMessage());
                }
                try {
                    redisTemplate.opsForValue().set(
                            "SubmissionStatusDTO/" + tournamentId + "/" + player.getPlayerUserName() + "/" + index,
                            SubmissionStatusDTO,
                            cacheDTO.getDurationInSeconds(),
                            TimeUnit.SECONDS
                    );
                    logger.info("Set SubmissionStatusDTO in Redis for user: {} at index: {}", player.getPlayerUserName(), index);
                } catch (Exception e) {

                    logger.error("Failed to set SubmissionStatusDTO for user: {}. Error: {}", player.getPlayerUserName(), e.getMessage());
                }
                try {
                    redisTemplate.opsForValue().set(
                            "questionDTO/" + tournamentId + "/" + index,
                            Mapper.toDTO(question),
                            cacheDTO.getDurationInSeconds(),
                            TimeUnit.SECONDS
                    );
                    logger.info("Set questionDTO in Redis for tournament question: {} at index: {}", question.getQuestionId(), index);
                } catch (Exception e) {

                    logger.error("Failed to set questionDTO for tournament question: {}. Error: {}", question.getQuestionId(), e.getMessage());
                }

                if (TournamentBaseEntity.TournamentType.FREE_STYLE.equals(cacheDTO.getTournamentType())) {
                    List<TestcaseEntity> testcases = testcaseRepository.findByQuestionId(question.getQuestionId());
                    if (testcases == null || testcases.isEmpty()) {

                        logger.error("No testcases available for question: {}", question.getQuestionId());
                        continue;
                    }

                    int randomIndex = (int) (Math.random() * testcases.size());
                    TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));

                    try {
                        redisTemplate.opsForValue().set(
                                "testcaseDTO/" + tournamentId + "/" + player.getPlayerUserName() + "/" + index,
                                testcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        logger.info("Set testcaseDTO in Redis for user: {} at index: {}", player.getPlayerUserName(), index);
                    } catch (Exception e) {
                        logger.error("Failed to set testcaseDTO for user: {}. Error: {}", player.getPlayerUserName(), e.getMessage());
                    }
                }
            }
            index++;
        }

        // Optionally, delete the tournament key from Redis after processing
        try {
            redisTemplate.delete("tournament:" + tournamentId);
            logger.info("Deleted tournament key from Redis for tournament ID: {}", tournamentId);
        } catch (Exception e) {
            logger.error("Failed to delete tournament key from Redis for tournament ID: {}. Error: {}", tournamentId, e.getMessage());
        }
    }

    @KafkaListener(topics = "classical-submission-response", groupId = "myGroup")
    public void consumeClassicSubmissionResponse(String classicSubmissionResponse) {
        logger.info("ClassicSubmissionResponse: {}", classicSubmissionResponse);
        // convert to object

        ClassicSubmissionDTO classicSubmissionDTO =
                JsonConverter.fromJson(classicSubmissionResponse,
                        ClassicSubmissionDTO.class);

        tournamentWebSocketService.classicRankAndSubUpdate(classicSubmissionDTO);
        String userName = classicSubmissionDTO.getUsername();
        int index = classicSubmissionDTO.getIndex();
        messagingTemplate.convertAndSend("/topic/tournament/classicSubmit/" + userName + "/" + index, classicSubmissionDTO);
        classicSubmissionService.saveToDB(classicSubmissionDTO);
        logger.info("Sent classic submission response via WebSocket for user: {} at index: {}", userName, index);

    }
    @KafkaListener(topics = "rating-update", groupId = "myGroup")
    public void consumeRatingUpdate(Long tournamentId) {
        // 1. Fetch rank list for the tournament
        List<RankEntity> ranks = rankRepository.findByTournamentId(tournamentId);

        // 2. Sort the ranks: higher score first, and lower penalty if scores are equal
        ranks.sort((r1, r2) -> {
            int scoreCompare = Long.compare(r2.getScore(), r1.getScore());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return Long.compare(r1.getPenalty(), r2.getPenalty());
        });

        int totalPlayers = ranks.size();
        double[] expectedRanks = new double[totalPlayers];

        // 3. Calculate expected rank for each player
        for (int i = 0; i < totalPlayers; i++) {
            RankEntity player = ranks.get(i);
            double expectedRank = 0.5; // self-match contribution
            for (int j = 0; j < totalPlayers; j++) {
                if (i == j) {
                    continue;
                }
                RankEntity opponent = ranks.get(j);
                double ratingDiff = opponent.getRating() - player.getRating();
                double probability = 1.0 / (1.0 + Math.pow(10, ratingDiff / 400.0));
                expectedRank += probability;
            }
            expectedRanks[i] = expectedRank;
        }

        // 4. Compute rating change and update each player's rating
        // The actual rank is determined by the sorted order (1-indexed)
        final double K = 20.0;
        for (int i = 0; i < totalPlayers; i++) {
            RankEntity player = ranks.get(i);
            int actualRank = i + 1; // 1-indexed rank
            double ratingChange = K * (expectedRanks[i] - actualRank);
            long newRating = Math.round(player.getRating() + ratingChange);
            player.setRating(newRating);
            Optional<UserEntity> user = userRepository.findByUserName(player.getUserName());
            if(user.isPresent()){
                user.get().setRating(newRating);
                userRepository.save(user.get());
            }
        }

        // 5. Persist the updated ranks (or update the user ratings as needed)
        rankRepository.saveAll(ranks);
    }

}
