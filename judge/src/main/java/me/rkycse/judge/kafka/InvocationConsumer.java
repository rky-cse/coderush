package me.rkycse.judge.kafka;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.rkycse.judge.dto.InvocationPayload;
import me.rkycse.judge.service.InvocationProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvocationConsumer {
    private static final Logger log = LoggerFactory.getLogger(InvocationConsumer.class);

    private final ObjectMapper objectMapper;

    private final InvocationProcessingService invocationProcessingService;

    public InvocationConsumer(ObjectMapper objectMapper, InvocationProcessingService invocationProcessingService) {
        this.objectMapper = objectMapper;
        this.invocationProcessingService = invocationProcessingService;
    }

    @KafkaListener(topics = "invocation", groupId = "myGroup")
    public void consume(String message) {
        log.info("Received invocation message: {}", message);
        try {
            InvocationPayload payload = objectMapper.readValue(message, InvocationPayload.class);
            log.info("Parsed InvocationPayload: questionId={}, checker={}, validator={}, solution={}, testcasesCount={} ",
                    payload.getQuestionId(),
                    payload.getCheckerFilePath(),
                    payload.getValidatorFilePath(),
                    payload.getSolutionFilePath(),
                    payload.getTestcases() != null ? payload.getTestcases().size() : 0);
            invocationProcessingService.processInvocation(message);

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize InvocationPayload", e);
        }
    }
}
