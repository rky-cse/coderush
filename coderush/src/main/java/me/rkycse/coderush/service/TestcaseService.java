package me.rkycse.coderush.service;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.ClassicTestcaseDTO;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.entity.ClassicTestcaseEntity;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.TestcaseEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.ClassicTestcaseRepository;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.TestcaseRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;


@Service
@Transactional
public class TestcaseService {

    private final TestcaseRepository testcaseRepository;
    private final QuestionRepository questionRepository;
    private final ClassicTestcaseRepository classicTestcaseRepository;

    public TestcaseService(TestcaseRepository testcaseRepository, QuestionRepository questionRepository, ClassicTestcaseRepository classicTestcaseRepository) {
        this.testcaseRepository = testcaseRepository;
        this.questionRepository = questionRepository;
        this.classicTestcaseRepository = classicTestcaseRepository;
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

    public Boolean createClassicTestcase(ClassicTestcaseDTO classicTestcase) {
        try {
            // Step 0: Check if the questionId exists
            if (!questionRepository.existsById(classicTestcase.getQuestionId())) {
                System.out.println("Question with id " + classicTestcase.getQuestionId() + " does not exist.");
                return false;
            }

            // Step 1: Map DTO to entity
            ClassicTestcaseEntity entity = new ClassicTestcaseEntity();
            entity.setQuestionId(classicTestcase.getQuestionId());
            // Initially, file paths can be empty
            entity.setInputFilePath("");
            entity.setOutputFilePath("");

            // Step 2: Save entity to repo to get a generated id
            ClassicTestcaseEntity savedEntity = classicTestcaseRepository.save(entity);
            Long testcaseId = savedEntity.getId();
            Long questionId = classicTestcase.getQuestionId();

            // Step 3: Create directories and files under "E:/testcases"
            String baseDir = "E:/testcases";
            // Create directory for the questionId
            File questionDir = new File(baseDir, questionId.toString());
            if (!questionDir.exists() && !questionDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + questionDir.getAbsolutePath());
            }
            // Create directory for the testcaseId inside the question directory
            File testcaseDir = new File(questionDir, testcaseId.toString());
            if (!testcaseDir.exists() && !testcaseDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + testcaseDir.getAbsolutePath());
            }

            // Create the input.txt and output.txt files
            File inputFile = new File(testcaseDir, "input.txt");
            File outputFile = new File(testcaseDir, "output.txt");

            // Write the input data to input.txt
            try (BufferedWriter inputWriter = new BufferedWriter(new FileWriter(inputFile))) {
                inputWriter.write(classicTestcase.getInput());
            }
            // Write the output data to output.txt
            try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile))) {
                outputWriter.write(classicTestcase.getOutput());
            }

            // Step 4: Update the entity with file paths and update the repo
            savedEntity.setInputFilePath(inputFile.getAbsolutePath());
            savedEntity.setOutputFilePath(outputFile.getAbsolutePath());
            classicTestcaseRepository.save(savedEntity);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ClassicTestcaseDTO getClassicTestcaseById(Long id) {
        Optional<ClassicTestcaseEntity> optionalEntity = classicTestcaseRepository.findById(id);
        if (!optionalEntity.isPresent()) {
            throw new ResourceNotFoundException("Classic Testcase with id " + id + " not found");
        }
        ClassicTestcaseEntity entity = optionalEntity.get();

        String input = "";
        String output = "";
        try {
            // Read file content from the stored file paths
            Path inputPath = Paths.get(entity.getInputFilePath());
            Path outputPath = Paths.get(entity.getOutputFilePath());
            input = Files.readString(inputPath);
            output = Files.readString(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading testcase files", e);
        }

        // Map the entity to DTO
        ClassicTestcaseDTO dto = new ClassicTestcaseDTO();
        dto.setId(entity.getId());
        dto.setQuestionId(entity.getQuestionId());
        dto.setInput(input);
        dto.setOutput(output);

        return dto;
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
