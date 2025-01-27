package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.entity.TestcaseEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.TestcaseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TestcaseService {

    private final TestcaseRepository testcaseRepository;
    private final QuestionRepository questionRepository;

    public TestcaseService(TestcaseRepository testcaseRepository, QuestionRepository questionRepository) {
        this.testcaseRepository = testcaseRepository;
        this.questionRepository = questionRepository;
    }

    public Boolean createTestcase(TestcaseDTO testcase) {

        if(testcase==null) {
            throw new NullPointerException("testcase is null");
        }
        if(testcase.getInput()==null) {
            throw new NullPointerException("input is null");
        }
        if(testcase.getOutput()==null) {
            throw new NullPointerException("output is null");
        }
        TestcaseEntity testcaseEntity = new TestcaseEntity();
        testcaseEntity.setInput(testcase.getInput());
        testcaseEntity.setOutput(testcase.getOutput());
        testcaseEntity.setRating(testcase.getRating());
        testcaseEntity.setQuestionId(testcase.getQuestionId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        testcaseRepository.save(testcaseEntity);
        return true;


    }

    public List<TestcaseDTO> getTestcasesByQuestionId(Long questionId) {
        List<TestcaseEntity> testcaseEntity=testcaseRepository.findByQuestionId(questionId);
        List<TestcaseDTO> testcaseDTOS=new ArrayList<>();
        for(TestcaseEntity testcaseEntity1: testcaseEntity) {
            testcaseDTOS.add(Mapper.toDTO(testcaseEntity1));
        }
        return testcaseDTOS;
    }
}
