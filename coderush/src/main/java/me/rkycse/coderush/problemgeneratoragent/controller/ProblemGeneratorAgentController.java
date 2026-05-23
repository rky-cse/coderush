package me.rkycse.coderush.problemgeneratoragent.controller;

import me.rkycse.coderush.problemgeneratoragent.dto.GenerationRequest;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJob;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJobStatus;
import me.rkycse.coderush.problemgeneratoragent.service.ProblemGeneratorAgentService;
import me.rkycse.coderush.problemgeneratoragent.sse.SseEmitterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/problem")
@CrossOrigin("*")
public class ProblemGeneratorAgentController {

    private final ProblemGeneratorAgentService service;
    private final SseEmitterRegistry sseRegistry;

    public ProblemGeneratorAgentController(ProblemGeneratorAgentService service, SseEmitterRegistry sseRegistry) {
        this.service = service;
        this.sseRegistry = sseRegistry;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(@RequestBody GenerationRequest request) {
        String jobId = service.startGeneration(request);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<AgentJob> getJob(@PathVariable String jobId) {
        return ResponseEntity.ok(service.getJob(jobId));
    }

    @GetMapping(value = "/job/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String jobId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        sseRegistry.register(jobId, emitter);

        // Send all existing events immediately (catch-up)
        AgentJob job = service.getJob(jobId);
        job.getProgress().forEach(event -> {
            try { emitter.send(SseEmitter.event().name("progress").data(event)); }
            catch (IOException ignored) {}
        });

        // Close immediately if already in terminal state
        AgentJobStatus s = job.getStatus();
        if (s == AgentJobStatus.DONE || s == AgentJobStatus.FAILED || s == AgentJobStatus.ABORTED) {
            emitter.complete();
        }

        return emitter;
    }

    @PostMapping("/job/{jobId}/approve")
    public ResponseEntity<Void> approve(@PathVariable String jobId,
                                         @RequestBody(required = false) Map<String, Object> edits) {
        service.approve(jobId, edits);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/job/{jobId}/regenerate")
    public ResponseEntity<Void> regenerate(@PathVariable String jobId,
                                            @RequestBody Map<String, Integer> body) {
        service.regenerate(jobId, body.getOrDefault("stage", 1));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/job/{jobId}/abort")
    public ResponseEntity<Void> abort(@PathVariable String jobId) {
        service.abort(jobId);
        return ResponseEntity.ok().build();
    }
}
