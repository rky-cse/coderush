package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.TournamentBaseEntity;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.util.StringComparator;
import me.rkycse.coderush.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
public class TournamentWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(TournamentWebSocketService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Producer producer;

    public TournamentWebSocketService(RedisTemplate<String, Object> redisTemplate, Producer producer) {
        this.redisTemplate = redisTemplate;
        this.producer = producer;
    }

    public QuestionDTO getQuestion(Long tournamentId, int index) throws InterruptedException {
        logger.info("Fetching question for tournamentId: {} and index: {}", tournamentId, index);
        QuestionDTO question = (QuestionDTO) redisTemplate
                .opsForValue().get("questionDTO/" + tournamentId + "/" + index);
        if (question == null) {
            logger.error("Question not found for tournamentId: {} and index: {}", tournamentId, index);
            throw new NoSuchElementException("Question not found");
        }
        logger.info("Successfully retrieved question for tournamentId: {} and index: {}", tournamentId, index);
        return question;
    }

    public TestcaseDTO getTestcase(Long tournamentId, String userName, int index) {
        logger.info("Fetching testcase for tournamentId: {}, userName: {}, index: {}", tournamentId, userName, index);
        TestcaseDTO testcase = (TestcaseDTO) redisTemplate
                .opsForValue().get("testcaseDTO/" + tournamentId + "/" + userName + "/" + index);
        if (testcase == null) {
            logger.error("Testcase not found for tournamentId: {}, userName: {}, index: {}", tournamentId, userName, index);
            throw new NoSuchElementException("Testcase not found");
        }
        testcase.setOutput(null);
        logger.info("Successfully retrieved testcase for tournamentId: {}, userName: {}, index: {}", tournamentId, userName, index);
        return testcase;
    }

    public QuestionWithTestcaseDTO getQuestionWithTestcase(Long tournamentId, int index, String userName) throws InterruptedException {
        logger.info("Fetching question with testcase for tournamentId: {}, index: {}, userName: {}", tournamentId, index, userName);
        // Fetch question and testcase
        QuestionDTO question = getQuestion(tournamentId, index);
        TournamentCacheDTO cacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get("$" + tournamentId);
        if (cacheDTO == null) {
            logger.error("TournamentCacheDTO not found for tournamentId: {}", tournamentId);
            throw new NoSuchElementException("Tournament cache not found");
        }

        // Create and send the response DTO
        QuestionWithTestcaseDTO questionWithTestcaseDTO = new QuestionWithTestcaseDTO();
        if (TournamentBaseEntity.TournamentType.FREE_STYLE.equals(cacheDTO.getTournamentType())) {
            TestcaseDTO testcaseDTO = getTestcase(tournamentId, userName, index);
            questionWithTestcaseDTO.setTestcase(testcaseDTO);
            logger.info("Free style tournament: testcase set for tournamentId: {}, userName: {}, index: {}", tournamentId, userName, index);
        } else {
            TestcaseDTO testcaseDTO1 = new TestcaseDTO();
            testcaseDTO1.setInput("");
            testcaseDTO1.setOutput("");
            questionWithTestcaseDTO.setTestcase(testcaseDTO1); // temp
            logger.info("Non free style tournament: setting empty testcase for tournamentId: {}, userName: {}, index: {}", tournamentId, userName, index);
        }
        questionWithTestcaseDTO.setQuestion(question);
        logger.info("Successfully built QuestionWithTestcaseDTO for tournamentId: {}, index: {}, userName: {}", tournamentId, index, userName);
        return questionWithTestcaseDTO;
    }

    public Boolean isCorrect(String userName, int index, UserResponseDTO answer) {
        Long tournamentId = answer.getTournamentId();

        logger.info("Validating answer for userName: {}, tournamentId: {}, index: {}", userName, tournamentId, index);

        TournamentCacheDTO cacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get("$" + tournamentId);

        if (cacheDTO == null) {
            logger.error("Tournament not found for tournamentId: {}", tournamentId);
            throw new NoSuchElementException("Tournament not found");
        }

        Long startTime = cacheDTO.getStartTime();
        Long endTime = startTime + cacheDTO.getDurationInSeconds() * 1000L;
        Long submissionTime = TimeUtil.getCurrentEpochMillis();


        if (submissionTime > endTime) {
            logger.error("Tournament already ended for tournamentId: {} (submissionTime: {}, endTime: {})", tournamentId, submissionTime, endTime);
            throw new NoSuchElementException("tournament already ended");
        }

        // Fetch the testcase from Redis
        TestcaseDTO testcase = (TestcaseDTO) redisTemplate
                .opsForValue().get("testcaseDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
        if (testcase == null) {
            logger.error("Testcase not found for user: {} and index: {}", userName, index);
            throw new NoSuchElementException("Testcase not found for user: " + userName + " and index: " + index);
        }

        String testcaseOutput = testcase.getOutput();
        if (testcaseOutput == null) {
            logger.error("Testcase output not found for testcase: {}", testcase);
            throw new NoSuchElementException("Testcase output not found for testcase: " + testcase);
        }
        logger.info("Fetched testcase output for user: {} and tournamentId: {}", userName, tournamentId);

        // Fetch rank details
        logger.info("Fetching rank for user: {} and tournamentId: {}", userName, answer.getTournamentId());

        RankDTO rank = (RankDTO) redisTemplate
                .opsForValue().get("rankDTO/" + answer.getTournamentId() + "/" + userName);
        if (rank == null) {
            logger.error("Rank not found for user: {}", userName);
            throw new NoSuchElementException("Rank not found for user: " + userName);
        }

        // Compare user output with the expected output
        if (StringComparator.compareIgnoringWhitespace(testcaseOutput, answer.getUserOutput())) {
            logger.info("User output matches expected output for user: {}, tournamentId: {}, index: {}", userName, tournamentId, index);
            // Fetch user testcase details
            SubmissionStatusDTO submissionStatusDTO = (SubmissionStatusDTO) redisTemplate
                    .opsForValue().get("SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
            if (submissionStatusDTO == null) {
                logger.error("User testcase not found for user: {} and index: {}", userName, index);
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            // If already solved, do not increase the number of attempts and return true immediately.
            if (submissionStatusDTO.getSolved()) {
                logger.info("Testcase already solved for user: {} and index: {}", userName, index);
                return true;
            }

            // Mark as solved and update attempts
            submissionStatusDTO.setSolved(true);
            submissionStatusDTO.setNumberOfAttempts(submissionStatusDTO.getNumberOfAttempts() + 1);
//            rank.setPenalty(rank.getPenalty() + (TimeUtil.getCurrentEpochMillis()) / (60000L));

            Long timeFromStart = (submissionTime-startTime)/1000L;
            rank.setPenalty(rank.getPenalty() + timeFromStart);

            // Update SubmissionStatusDTO in Redis without changing TTL
            String key = "SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index;
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS); // Restore TTL
                producer.sendSubmissionStatusUpdate(Mapper.toEntity(submissionStatusDTO));
                logger.info("Updated SubmissionStatusDTO with TTL for key: {}", key);
            } else {
                redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                logger.info("Updated SubmissionStatusDTO without TTL for key: {}", key);
            }

            // Update rank score
            rank.setScore(rank.getScore() + testcase.getRating());

            // Update rank in Redis without changing TTL
            String rankKey = "rankDTO/" + answer.getTournamentId() + "/" + userName;
            Long rankTTL = redisTemplate.getExpire(rankKey, TimeUnit.SECONDS);
            if (rankTTL != null && rankTTL > 0) {
                redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                redisTemplate.expire(rankKey, rankTTL, TimeUnit.SECONDS); // Restore TTL using rankKey
                producer.sendRankUpdate(Mapper.toEntity(rank));
                logger.info("Updated RankDTO with TTL for key: {}", rankKey);
            } else {
                redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                logger.info("Updated RankDTO without TTL for key: {}", rankKey);
            }

            return true;

        } else {
            logger.info("User output does not match expected output for user: {}, tournamentId: {}, index: {}", userName, tournamentId, index);
            // Handle incorrect answers
            SubmissionStatusDTO submissionStatusDTO = (SubmissionStatusDTO) redisTemplate
                    .opsForValue().get("SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
            if (submissionStatusDTO == null) {
                logger.error("User testcase not found for user: {} and index: {}", userName, index);
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            // Increment the number of attempts if not already solved
            if (!submissionStatusDTO.getSolved()) {
                submissionStatusDTO.setNumberOfAttempts(submissionStatusDTO.getNumberOfAttempts() + 1);
                rank.setPenalty(rank.getPenalty() + cacheDTO.getPenaltyFactor());
                String key = "SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index;
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                    producer.sendSubmissionStatusUpdate(Mapper.toEntity(submissionStatusDTO));
                    logger.info("Incremented attempt count for user: {} on key: {} with TTL", userName, key);
                } else {
                    redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                    logger.info("Incremented attempt count for user: {} on key: {} without TTL", userName, key);
                }
                String rankKey = "rankDTO/" + answer.getTournamentId() + "/" + userName;
                Long rankTTL = redisTemplate.getExpire(rankKey, TimeUnit.SECONDS);
                if (rankTTL != null && rankTTL > 0) {
                    redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                    redisTemplate.expire(rankKey, rankTTL, TimeUnit.SECONDS); // Restore TTL using rankKey
                    producer.sendRankUpdate(Mapper.toEntity(rank));
                    logger.info("Updated RankDTO with TTL for key: {}", rankKey);
                } else {
                    redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                    logger.info("Updated RankDTO without TTL for key: {}", rankKey);
                }
            }
        }
        return false;
    }

    public void classicRankAndSubUpdate(ClassicSubmissionDTO classicDTO) {
        Long tournamentId = classicDTO.getTournamentId();
        String userName = classicDTO.getUsername();
        int index = classicDTO.getIndex();

        logger.info("Processing classic rank and submission update for user: {}, tournamentId: {}, index: {}", userName, tournamentId, index);
        TournamentCacheDTO cacheDTO = (TournamentCacheDTO) redisTemplate.opsForValue().get("$" + tournamentId);
        if (cacheDTO == null) {
            logger.error("Tournament not found for tournamentId: {}", tournamentId);
            throw new NoSuchElementException("Tournament not found");
        }
        Long startTime = cacheDTO.getStartTime();
        Long endTime = startTime + cacheDTO.getDurationInSeconds() * 1000L;
        Long submissionTime = classicDTO.getSubmissionTime();

        logger.info("start: {}, end: {}, submissionTime {}", startTime, endTime,submissionTime);

        if (submissionTime > endTime) {
            logger.error("Tournament already ended for tournamentId: {} (submissionTime: {}, endTime: {})", tournamentId, submissionTime, endTime);
            throw new NoSuchElementException("tournament already ended");
        }

        String verdict = classicDTO.getVerdict();
        if (verdict == null) {
            logger.error("Verdict not found for classic submission for user: {}", userName);
            throw new NoSuchElementException("verdict not found");
        }
        // Fetch rank details
        logger.info("Fetching rank for classic submission for user: {} and tournamentId: {}", userName, tournamentId);
        RankDTO rank = (RankDTO) redisTemplate
                .opsForValue().get("rankDTO/" + classicDTO.getTournamentId() + "/" + classicDTO.getUsername());
        if (rank == null) {
            logger.error("Rank not found for user: {}", userName);
            throw new NoSuchElementException("Rank not found for user: " + classicDTO.getUsername());
        }

        if (verdict.equals("AC")) {
            logger.info("Verdict is AC for user: {} and tournamentId: {}", userName, tournamentId);
            // Fetch user testcase details
            SubmissionStatusDTO submissionStatusDTO = (SubmissionStatusDTO) redisTemplate
                    .opsForValue().get("SubmissionStatusDTO/" + classicDTO.getTournamentId() + "/" + classicDTO.getUsername() + "/" + classicDTO.getIndex());
            if (submissionStatusDTO == null) {
                logger.error("User testcase not found for user: {} and index: {}", userName, index);
                throw new NoSuchElementException("User testcase not found for user: " + classicDTO.getUsername() + " and index: " + classicDTO.getIndex());
            }
            if (submissionStatusDTO.getSolved()) {
                logger.info("Problem already solved for classic submission for user: {} and index: {}", userName, index);
                return; // do nothing if already solved
            }

            // Only update the attempt count if the test case has not been solved before.
            if (!submissionStatusDTO.getSolved()) {
                submissionStatusDTO.setSolved(true);
                submissionStatusDTO.setNumberOfAttempts(submissionStatusDTO.getNumberOfAttempts() + 1);
                Long timeFromStart = (submissionTime-startTime)/1000L;
                rank.setPenalty(rank.getPenalty() + timeFromStart);
                logger.info("timeFromStart: {}, penalty: {}", timeFromStart, rank.getPenalty());

                // Update SubmissionStatusDTO in Redis without changing TTL
                String key = "SubmissionStatusDTO/" + tournamentId + "/" + userName + "/" + index;
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                    producer.sendSubmissionStatusUpdate(Mapper.toEntity(submissionStatusDTO));
                    logger.info("Updated SubmissionStatusDTO for classic submission with TTL for key: {}", key);
                } else {
                    redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                    logger.info("Updated SubmissionStatusDTO for classic submission without TTL for key: {}", key);
                }
                rank.setScore(rank.getScore() + 1);
                String rankKey = "rankDTO/" + tournamentId + "/" + userName;
                Long rankTTL = redisTemplate.getExpire(rankKey, TimeUnit.SECONDS);
                if (rankTTL != null && rankTTL > 0) {
                    redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                    redisTemplate.expire(rankKey, rankTTL, TimeUnit.SECONDS);
                    producer.sendRankUpdate(Mapper.toEntity(rank));
                    logger.info("Updated RankDTO for classic submission with TTL for key: {}", rankKey);
                } else {
                    redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                    logger.info("Updated RankDTO for classic submission without TTL for key: {}", rankKey);
                }
            }

            // Update rank score for AC verdict regardless of previous solved state.

            // Update rank in Redis without changing TTL

        } else {
            logger.info("Verdict is not AC for classic submission for user: {} and tournamentId: {}", userName, tournamentId);
            // Handle incorrect answers
            SubmissionStatusDTO submissionStatusDTO = (SubmissionStatusDTO) redisTemplate
                    .opsForValue().get("SubmissionStatusDTO/" + tournamentId + "/" + userName + "/" + index);
            if (submissionStatusDTO == null) {
                logger.error("User testcase not found for user: {} and index: {}", userName, index);
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            // Increment the number of attempts only if not already solved.
            if (!submissionStatusDTO.getSolved()) {
                submissionStatusDTO.setNumberOfAttempts(submissionStatusDTO.getNumberOfAttempts() + 1);
                rank.setPenalty(rank.getPenalty() + cacheDTO.getPenaltyFactor());
                logger.info("penaltyFactor: {}, penalty: {}",cacheDTO.getPenaltyFactor(), rank.getPenalty());
                String key = "SubmissionStatusDTO/" + tournamentId + "/" + userName + "/" + index;
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                    producer.sendSubmissionStatusUpdate(Mapper.toEntity(submissionStatusDTO));
                    logger.info("Incremented attempt count for classic submission for key: {} with TTL", key);
                } else {
                    redisTemplate.opsForValue().setIfPresent(key, submissionStatusDTO);
                    logger.info("Incremented attempt count for classic submission for key: {} without TTL", key);
                }
                String rankKey = "rankDTO/" + tournamentId + "/" + userName;
                Long rankTTL = redisTemplate.getExpire(rankKey, TimeUnit.SECONDS);
                if (rankTTL != null && rankTTL > 0) {
                    redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                    redisTemplate.expire(rankKey, rankTTL, TimeUnit.SECONDS);
                    producer.sendRankUpdate(Mapper.toEntity(rank));
                    logger.info("Updated RankDTO for classic submission with TTL for key: {}", rankKey);
                } else {
                    redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                    logger.info("Updated RankDTO for classic submission without TTL for key: {}", rankKey);
                }
            }
        }
    }
}
