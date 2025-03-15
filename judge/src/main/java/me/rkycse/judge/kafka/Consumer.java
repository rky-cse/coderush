package me.rkycse.judge.kafka;

import me.rkycse.judge.service.CodeExecutionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {
    private final CodeExecutionService codeExecutionService;

    public Consumer(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @KafkaListener(topics = "classical-submission", groupId = "myGroup")
    public void consume(String message) {
        System.out.println("message: "+message);
        String response=codeExecutionService.executeCode(message);
        System.out.println("response: "+response);
    }
}
