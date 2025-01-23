package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.dto.UserResponseDTO;
import me.rkycse.coderush.dto.UserTestcaseDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.util.StringComparator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
@Service
public class TournamentWebSocketService {

   private final RedisTemplate<String, List<QuestionEntity>>redisQuestionListTemplate;

   private final RedisTemplate<String, TestcaseDTO>testcaseRedisTemplate;

    public TournamentWebSocketService( RedisTemplate<String, List<QuestionEntity>> redisQuestionListTemplate, RedisTemplate<String, TestcaseDTO> testcaseRedisTemplate) {
        this.redisQuestionListTemplate = redisQuestionListTemplate;

        this.testcaseRedisTemplate = testcaseRedisTemplate;
    }


    public QuestionDTO getQuestion(Long tournamentId,int index) {

        List<QuestionEntity> questions = redisQuestionListTemplate.opsForValue().get("%"+tournamentId);
        if (questions == null) {
            throw new NoSuchElementException("Question not found");
        }
        if(questions.size() <= index) {
            throw new NoSuchElementException("Question not found");
        }
        return Mapper.toDTO(questions.get(index));
    }

    public TestcaseDTO getTestcase(Long questionId, String userName) {

        TestcaseDTO testcase = testcaseRedisTemplate
                .opsForValue().get("!"+questionId+"/"+userName);
        if (testcase == null) {
            throw new NoSuchElementException("Testcase not found");
        }
        testcase.setOutput(null);
        return testcase;

    }

    public Boolean isCorrect (Long questionId, String userName, UserResponseDTO answer) {
        TestcaseDTO testcase = testcaseRedisTemplate
                .opsForValue().get("!"+questionId+"/"+userName);
        if (testcase == null) {
            throw new NoSuchElementException("Testcase not found");
        }
        String testcaseOutput = testcase.getOutput();
        if(testcaseOutput == null) {
            throw new NoSuchElementException("Testcase output not found");
        }
        if(StringComparator
                .compareIgnoringWhitespace(testcaseOutput, answer.getUserOutput()))
        {
            return true;




        }
        return false;


    }
}
