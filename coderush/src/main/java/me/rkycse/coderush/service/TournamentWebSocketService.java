package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.repository.TournamentRepository;
import me.rkycse.coderush.util.StringComparator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
@Service
public class TournamentWebSocketService {

    private final RedisTemplate<String, Object> redisTemplate;

    public TournamentWebSocketService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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

        // Compare user output with the expected output
        if (StringComparator.compareIgnoringWhitespace(testcaseOutput, answer.getUserOutput())) {
            // Fetch user testcase details
            UserTestcaseDTO userTestcaseDTO = (UserTestcaseDTO)redisTemplate
                    .opsForValue().get("userTestcaseDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
            if (userTestcaseDTO == null) {
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            if (userTestcaseDTO.getIsSolved()) {
                return true; // If already solved, return true
            }

            // Mark as solved and update attempts
            userTestcaseDTO.setIsSolved(true);
            userTestcaseDTO.setNumberOfAttempts(userTestcaseDTO.getNumberOfAttempts() + 1);

            // Update userTestcaseDTO in Redis without changing TTL
            redisTemplate.opsForValue()
                    .setIfPresent("userTestcaseDTO/" + answer.getTournamentId() + "/" + userName + "/" + index, userTestcaseDTO);

            // Fetch rank details
            System.out.println("Fetching rank for user: " + userName + " and tid: " + answer.getTournamentId());
            RankDTO rank = (RankDTO)redisTemplate
                    .opsForValue().get("rankDTO/" + answer.getTournamentId() + "/" + userName);
            if (rank == null) {
                throw new NoSuchElementException("Rank not found for user: " + userName);
            }

            // Update rank score
            rank.setScore(rank.getScore() + testcase.getRating());

            // Update rank in Redis without changing TTL
            redisTemplate.opsForValue()
                    .setIfPresent("rankDTO/" + answer.getTournamentId() + "/" + userName, rank);

            return true;

        } else {
            // Handle incorrect answers
            UserTestcaseDTO userTestcaseDTO =(UserTestcaseDTO) redisTemplate
                    .opsForValue().get("userTestcaseDTO/" + answer.getTournamentId() + "/" + userName + "/" + index);
            if (userTestcaseDTO == null) {
                throw new NoSuchElementException("User testcase not found for user: " + userName + " and index: " + index);
            }

            // Increment the number of attempts if not already solved
            if (!userTestcaseDTO.getIsSolved()) {
                userTestcaseDTO.setNumberOfAttempts(userTestcaseDTO.getNumberOfAttempts() + 1);

                // Update userTestcaseDTO in Redis without changing TTL
                redisTemplate.opsForValue()
                        .setIfPresent("userTestcaseDTO/" + answer.getTournamentId() + "/" + userName + "/" + index, userTestcaseDTO);
            }
        }

        return false;
    }


}
