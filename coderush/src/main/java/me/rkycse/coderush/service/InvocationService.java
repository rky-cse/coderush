// src/main/java/me/rkycse/coderush/service/InvocationService.java
package me.rkycse.coderush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.rkycse.coderush.dto.InvocationPayload;
import me.rkycse.coderush.entity.CheckerValidatorSolutionEntity;
import me.rkycse.coderush.entity.ClassicTestcaseEntity;
import me.rkycse.coderush.exception.ResourceNotFoundException;
import me.rkycse.coderush.repository.CheckerValidatorSolutionRepository;
import me.rkycse.coderush.repository.ClassicTestcaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvocationService {
    private static final Logger log = LoggerFactory.getLogger(InvocationService.class);
    private static final String TOPIC = "invocation";

    private final CheckerValidatorSolutionRepository solutionRepo;
    private final ClassicTestcaseRepository testcaseRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InvocationService(CheckerValidatorSolutionRepository solutionRepo,
                             ClassicTestcaseRepository testcaseRepo,
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper) {
        this.solutionRepo = solutionRepo;
        this.testcaseRepo = testcaseRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public void handleInvocation(Long questionId) {
        if (questionId == null || questionId <= 0) {
            log.error("Invalid questionId provided: {}", questionId);
            throw new IllegalArgumentException("questionId must be a positive non-null value");
        }

        log.info("Starting invocation build for questionId={}", questionId);

        CheckerValidatorSolutionEntity solution = solutionRepo.findByQuestionId(questionId)
                .orElseThrow(() -> {
                    String msg = "No checker/validator/solution found for questionId=" + questionId;
                    log.error(msg);
                    return new ResourceNotFoundException(msg);
                });

        List<ClassicTestcaseEntity> testcases = testcaseRepo.findByQuestionId(questionId);
        if (testcases.isEmpty()) {
            log.warn("No testcases found for questionId={}", questionId);
        }

        InvocationPayload payload = new InvocationPayload(
                questionId,
                solution.getCheckerFilePath(),
                solution.getValidatorFilePath(),
                solution.getSolutionFilePath(),
                testcases.stream()
                        .map(tc -> new InvocationPayload.TestcaseInfo(
                                tc.getInputFilePath(),
                                tc.getOutputFilePath(),
                                tc.getId()
                        ))
                        .collect(Collectors.toList())
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize InvocationPayload for questionId={}", questionId, e);
            throw new RuntimeException("Serialization error", e);
        }

        try {
            kafkaTemplate.send(TOPIC, payloadJson);
            log.info("Invocation message sent to Kafka for questionId={}", questionId);
        } catch (Exception e) {
            log.error("Kafka send failed for questionId={}", questionId, e);
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
