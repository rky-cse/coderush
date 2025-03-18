package me.rkycse.judge.kafka;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final  String classicSubmissionResponseTopic="classical-submission-response";
    public void sendClassicSubmissionResponse(String classicSubmissionResponse) {
        kafkaTemplate.send(classicSubmissionResponseTopic, classicSubmissionResponse);
    }


}
