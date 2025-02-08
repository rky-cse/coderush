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
import java.util.Optional;
import java.util.stream.Collectors;

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
        testcaseEntity.setInput(testcase.getInput());
        testcaseEntity.setOutput(testcase.getOutput());
        testcaseEntity.setRating(testcase.getRating());
        testcaseEntity.setQuestion(question);

        question.getTestcases().add(testcaseEntity);
        testcaseRepository.save(testcaseEntity);

        return true;
    }

    public List<TestcaseDTO> getTestcasesByQuestionId(Long questionId) {
        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        List<TestcaseDTO> testcaseDTOS = new ArrayList<>();
        for (TestcaseEntity testcaseEntity : question.getTestcases()) {
            testcaseDTOS.add(Mapper.toDTO(testcaseEntity));
        }
        return testcaseDTOS;
    }
}
