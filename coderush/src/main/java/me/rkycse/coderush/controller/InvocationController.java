package me.rkycse.coderush.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.rkycse.coderush.dto.InvocationResultDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.exception.ResourceNotFoundException;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.TestcaseRepository;
import me.rkycse.coderush.service.InvocationService;
import me.rkycse.coderush.service.TestcaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.*;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

@RestController
@RequestMapping("/api/questions")
public class InvocationController {
    private static final Logger log = LoggerFactory.getLogger(InvocationController.class);
    private static final String INVOCATION_TOPIC = "invocation";
    private static final String RESULT_TOPIC     = "invocation-result";
    private static final long   TIMEOUT_MS       = Duration.ofMinutes(6).toMillis();

    private final InvocationService invocationService;
    private final ConsumerFactory<String,String> consumerFactory;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final TestcaseService testcaseService;
    private final TestcaseRepository testcaseRepository;
    private final QuestionRepository questionRepository;

    public InvocationController(InvocationService invocationService,
                                ConsumerFactory<String,String> consumerFactory,
                                SimpMessagingTemplate messagingTemplate,
                                ObjectMapper objectMapper, TestcaseService testcaseService, TestcaseRepository testcaseRepository, QuestionRepository questionRepository) {
        this.invocationService  = invocationService;
        this.consumerFactory    = consumerFactory;
        this.messagingTemplate  = messagingTemplate;
        this.objectMapper       = objectMapper;
        this.testcaseService = testcaseService;
        this.testcaseRepository = testcaseRepository;
        this.questionRepository = questionRepository;
    }

    @PostMapping("/invocation/{questionId}")
    public ResponseEntity<String> invokeQuestion(@PathVariable Long questionId) {
        log.info("Received HTTP invocation request for questionId={}", questionId);

        // 1) send invocation to Kafka
        try {

            long freeStyleTestcasesCount=testcaseRepository.countByQuestionId(questionId);
            QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
            if(freeStyleTestcasesCount==0L) {

                if(questionEntity!=null) {
                    questionEntity.setFreeStyle(Boolean.FALSE);

                }

            }
            else{
                assert questionEntity != null;
                questionEntity.setFreeStyle(Boolean.TRUE);
            }
            assert questionEntity != null;
            questionEntity = questionRepository.save(questionEntity);

            invocationService.handleInvocation(questionId);
        } catch (ResourceNotFoundException rnfe) {
            log.warn("Resource not found for questionId={}", questionId);
            return ResponseEntity.status(404).body(rnfe.getMessage());
        } catch (Exception ex) {
            log.error("Failed to send invocation for questionId={}", questionId, ex);
            return ResponseEntity.status(500).body("Invocation request failed");
        }

        // 2) start background polling for result
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            Consumer<String,String> consumer = consumerFactory.createConsumer();
            consumer.subscribe(Collections.singletonList(RESULT_TOPIC));
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;
            try {
                while (System.currentTimeMillis() < deadline) {
                    ConsumerRecords<String,String> recs = consumer.poll(Duration.ofSeconds(1));
                    for (ConsumerRecord<String,String> r : recs) {
                        if (r.value().contains("\"questionId\":" + questionId)) {
                            log.info("Publishing result to WebSocket topic for questionId={}", questionId);
                            // parse JSON into DTO
                            InvocationResultDTO result =
                                    objectMapper.readValue(r.value(), InvocationResultDTO.class);
                            // send to STOMP destination: /topic/invocation-result/{questionId}
                            messagingTemplate.convertAndSend(
                                    "/topic/invocation-result/" + questionId,
                                    result
                            );
                            return;
                        }
                    }
                }
                log.warn("Timeout waiting for result for questionId={}", questionId);
                messagingTemplate.convertAndSend(
                        "/topic/invocation-result/" + questionId,
                        Collections.singletonMap("error", "No result after timeout")
                );
            } catch (Exception e) {
                log.error("Error polling/publishing result for questionId=" + questionId, e);
                messagingTemplate.convertAndSend(
                        "/topic/invocation-result/" + questionId,
                        Collections.singletonMap("error", "Internal error")
                );
            } finally {
                consumer.close();
                executor.shutdown();
            }
        });

        return ResponseEntity.accepted()
                .body("Invocation queued; subscribe to /topic/invocation-result/" + questionId);
    }
}