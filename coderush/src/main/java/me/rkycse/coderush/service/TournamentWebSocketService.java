package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.util.StringComparator;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
public class TournamentWebSocketService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Producer producer;

    public TournamentWebSocketService(RedisTemplate<String, Object> redisTemplate, Producer producer) {
        this.redisTemplate = redisTemplate;
        this.producer = producer;
    }


    public QuestionDTO getQuestion(Long tournamentId,int index) {

        QuestionDTO question = (QuestionDTO)redisTemplate
                .opsForValue().get("questionDTO/"+tournamentId+"/"+index);
        if (question == null) {
            throw new NoSuchElementException("Question not found");
        }

        return question;
    }

    public TestcaseDTO getTestcase(Long tournamentId,String userName,int index) {

        TestcaseDTO testcase = (TestcaseDTO) redisTemplate
                .opsForValue().get("testcaseDTO/"+tournamentId+"/"+userName+ "/"+ index);
        if (testcase == null) {
            throw new NoSuchElementException("Testcase not found");
        }
        testcase.setOutput(null);
        return testcase;

    }



    public Boolean isCorrect(String userName, int index, UserResponseDTO answer) {
        Long tournamentId=answer.getTournamentId();
        TournamentCacheDTO cacheDTO=(TournamentCacheDTO) redisTemplate.opsForValue().get("$"+tournamentId);
        if(cacheDTO==null) {
            throw new NoSuchElementException("Tournament not found");
        }

        Long startTime=cacheDTO.getStartTime();
        Long endTime=startTime+cacheDTO.getDurationInSeconds()*1000L;
        Long submissionTime= TimeUtil.getCurrentEpochMillis();

        if(submissionTime>endTime) {
            throw new NoSuchElementException("tournament already ended");
        }

        // Fetch the testcase from Redis
        TestcaseDTO testcase = (TestcaseDTO) redisTemplate
                .opsForValue().get("testcaseDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
        if (testcase == null) {
            throw new NoSuchElementException("Testcase not found for user: " + userName + " and index: " + index);
        }

        String testcaseOutput = testcase.getOutput();
        if (testcaseOutput == null) {
            throw new NoSuchElementException("Testcase output not found for testcase: " + testcase);
        }
        // Fetch rank details
        System.out.println("Fetching rank for user: " + userName + " and tid: " + answer.getTournamentId());
        RankDTO rank = (RankDTO)redisTemplate
                .opsForValue().get("rankDTO/" + answer.getTournamentId() + "/" + userName);
        if (rank == null) {
            throw new NoSuchElementException("Rank not found for user: " + userName);
        }

        // Compare user output with the expected output
        if (StringComparator.compareIgnoringWhitespace(testcaseOutput, answer.getUserOutput())) {
            // Fetch user testcase details
            SubmissionStatusDTO SubmissionStatusDTO = (SubmissionStatusDTO)redisTemplate
                    .opsForValue().get("SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
            if (SubmissionStatusDTO == null) {
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            if (SubmissionStatusDTO.getSolved()) {
                return true; // If already solved, return true
            }

            // Mark as solved and update attempts
            SubmissionStatusDTO.setSolved(true);
            SubmissionStatusDTO.setNumberOfAttempts(SubmissionStatusDTO.getNumberOfAttempts() + 1);
            rank.setPenalty(rank.getPenalty()+(TimeUtil.getCurrentEpochMillis())/(60000L));

            // Update SubmissionStatusDTO in Redis without changing TTL
            String key = "SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index;

// Get the current TTL before updating
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            if (ttl != null && ttl > 0) {
                // Key exists and has an expiry
                redisTemplate.opsForValue().setIfPresent(key, SubmissionStatusDTO);
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS); // Restore TTL
                producer.sendSubmissionStatusUpdate(Mapper.toEntity(SubmissionStatusDTO));
            } else {
                redisTemplate.opsForValue().setIfPresent(key, SubmissionStatusDTO);
            }



            // Update rank score
            rank.setScore(rank.getScore() + testcase.getRating());

            // Update rank in Redis without changing TTL
            String rankKey = "rankDTO/" + answer.getTournamentId() + "/" + userName;
// Get the current TTL before updating
            Long rankTTL = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            if (rankTTL != null && rankTTL > 0) { // Key exists and has an expiry
                redisTemplate.opsForValue().setIfPresent(rankKey, rank);
                redisTemplate.expire(key, rankTTL, TimeUnit.SECONDS); // Restore TTL
                producer.sendRankUpdate(Mapper.toEntity(rank));
            } else {
                redisTemplate.opsForValue().setIfPresent(key,rank);
            }

            return true;

        } else {
            // Handle incorrect answers
            SubmissionStatusDTO SubmissionStatusDTO =(SubmissionStatusDTO) redisTemplate
                    .opsForValue().get("SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
            if (SubmissionStatusDTO == null) {
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            // Increment the number of attempts if not already solved
            if (!SubmissionStatusDTO.getSolved()) {
                SubmissionStatusDTO.setNumberOfAttempts(SubmissionStatusDTO.getNumberOfAttempts() + 1);
                rank.setPenalty(rank.getPenalty() + cacheDTO.getPenaltyFactor());
                // Update SubmissionStatusDTO in Redis without changing TTL
                String key = "SubmissionStatusDTO/" + answer.getTournamentId() + "/" + userName + "/" + index;

// Get the current TTL before updating
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

                if (ttl != null && ttl > 0) { // Key exists and has an expiry
                    redisTemplate.opsForValue().setIfPresent(key, SubmissionStatusDTO);
                    redisTemplate.expire(key, ttl, TimeUnit.SECONDS); // Restore TTL
                    producer.sendSubmissionStatusUpdate(Mapper.toEntity(SubmissionStatusDTO));
                } else {
                    redisTemplate.opsForValue().setIfPresent(key, SubmissionStatusDTO);
                }
            }
        }

        return false;
    }


}
