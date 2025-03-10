package me.rkycse.coderush.kafka;

import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.UserTestcaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final String rankUpdateTopic = "rank-update";
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    public void sendRankUpdate(RankEntity rank) {
        kafkaTemplate.send(rankUpdateTopic, rank);
        System.out.println("Produced message: " + rank);
    }
    private static final String userTestcaseUpdateTopic = "user-testcase-update";

    public void sendUserTestcaseUpdate(UserTestcaseEntity testcase ) {
        kafkaTemplate.send(userTestcaseUpdateTopic, testcase);
    }

    private static final  String startTournamentInitTopic="start-tournament-init";
    public void startTournamentInit(TournamentCacheDTO cacheDTO) {
        kafkaTemplate.send(startTournamentInitTopic, cacheDTO);

    }

}
