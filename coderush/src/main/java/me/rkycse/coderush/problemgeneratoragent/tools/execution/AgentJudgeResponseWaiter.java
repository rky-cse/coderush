package me.rkycse.coderush.problemgeneratoragent.tools.execution;

import me.rkycse.coderush.dto.ClassicSubmissionDTO;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Allows the agent to synchronously wait for a judge response.
 * The agent registers a future before sending to Kafka.
 * The existing Kafka consumer completes the future when the response arrives.
 */
@Component
public class AgentJudgeResponseWaiter {

    private final ConcurrentHashMap<String, CompletableFuture<ClassicSubmissionDTO>> pending
        = new ConcurrentHashMap<>();

    /**
     * Registers a future and blocks until the judge responds or timeout expires.
     *
     * @param correlationId  Unique ID embedded in the submission.
     * @param timeoutMs      Max wait time in milliseconds.
     * @return               The judge response, or null if timed out.
     */
    public ClassicSubmissionDTO waitFor(String correlationId, long timeoutMs) {
        CompletableFuture<ClassicSubmissionDTO> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            return null;
        } finally {
            pending.remove(correlationId);
        }
    }

    /**
     * Called by the Kafka consumer when a response arrives.
     * If the correlationId matches a pending future, completes it.
     * No-op if correlationId is null (tournament submissions).
     */
    public void complete(String correlationId, ClassicSubmissionDTO response) {
        if (correlationId == null) return;
        CompletableFuture<ClassicSubmissionDTO> future = pending.get(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }
}
