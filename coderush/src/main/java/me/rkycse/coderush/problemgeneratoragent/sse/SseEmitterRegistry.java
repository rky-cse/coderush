package me.rkycse.coderush.problemgeneratoragent.sse;

import me.rkycse.coderush.problemgeneratoragent.dto.AgentProgressEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SseEmitterRegistry.class);
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void register(String jobId, SseEmitter emitter) {
        emitters.computeIfAbsent(jobId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(jobId, emitter));
        emitter.onTimeout(() -> { remove(jobId, emitter); emitter.complete(); });
        emitter.onError(e -> remove(jobId, emitter));
    }

    public void broadcast(String jobId, AgentProgressEvent event) {
        List<SseEmitter> list = emitters.getOrDefault(jobId, List.of());
        list.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("progress").data(event));
                if ("DONE".equals(event.status()) && "COMPLETE".equals(event.step())) {
                    emitter.complete();
                }
            } catch (IOException e) {
                remove(jobId, emitter);
            }
        });
    }

    private void remove(String jobId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(jobId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(jobId);
        }
    }
}
