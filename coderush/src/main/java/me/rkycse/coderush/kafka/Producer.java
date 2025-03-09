package me.rkycse.coderush.kafka;

import me.rkycse.coderush.entity.RankEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final String rankUpdateTopic = "rank-update";

    @Autowired
    private KafkaTemplate<String, RankEntity> kafkaTemplate;
    public void sendRankUpdate(RankEntity rank) {
        kafkaTemplate.send(rankUpdateTopic, rank);
        System.out.println("Produced message: " + rank);
    }
}
