package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.TestcaseEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.TestcaseRepository;
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
        if (testcase == null || testcase.getInput() == null || testcase.getOutput() == null) {
            throw new IllegalArgumentException("Testcase input/output cannot be null");
        }

        QuestionEntity question = questionRepository.findById(testcase.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + testcase.getQuestionId()));

        TestcaseEntity testcaseEntity = new TestcaseEntity();
        testcaseEntity.setQuestionId(testcase.getQuestionId());
        testcaseEntity.setInput(testcase.getInput());
        testcaseEntity.setOutput(testcase.getOutput());
        testcaseEntity.setRating(testcase.getRating());
        testcaseRepository.save(testcaseEntity);

        return true;
    }

    public List<TestcaseDTO> getTestcasesByQuestionId(Long questionId) {
        List<TestcaseEntity> testcaseEntities = testcaseRepository.findByQuestionId(questionId);

        List<TestcaseDTO> testcaseDTOS = new ArrayList<>();
        for (TestcaseEntity testcaseEntity : testcaseEntities) {
            testcaseDTOS.add(Mapper.toDTO(testcaseEntity));
        }
        return testcaseDTOS;
    }
}
