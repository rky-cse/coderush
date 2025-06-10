package me.rkycse.coderush.kafka;

import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.SubmissionStatus;
import me.rkycse.coderush.entity.RankEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {

    private static final String rankUpdateTopic = "rank-update";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public Producer(KafkaTemplate<String, Object> kafkaTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    public void sendRankUpdate(RankEntity rank) {
        kafkaTemplate.send(rankUpdateTopic, rank);
        System.out.println("Produced message\n\n\n\n\n\n\n\n\n\n: " + rank);
        System.out.println("kya mila ?????????????????????????????" + rank);
    }
    private static final String SubmissionStatusUpdateTopic = "user-testcase-update";

    public void sendSubmissionStatusUpdate(SubmissionStatus testcase ) {
        kafkaTemplate.send(SubmissionStatusUpdateTopic, testcase);
    }

    private static final  String startTournamentInitTopic="start-tournament-init";
    public void startTournamentInit(TournamentCacheDTO cacheDTO) {
        kafkaTemplate.send(startTournamentInitTopic, cacheDTO);

    }
    private static final  String classicSubmissionUpdateTopic="classical-submission";
    public void sendClassicSubmission(String classicSubmissionDTO,Long tournamentId) {

        kafkaTemplate.send(classicSubmissionUpdateTopic, classicSubmissionDTO);
        Object value = redisTemplate.opsForValue().get("judgeCount/" + tournamentId);
        Long count = 0L;

        if (value instanceof Integer) {
            count = ((Integer) value).longValue();
        } else if (value instanceof Long) {
            count = (Long) value;
        }

        redisTemplate.opsForValue().set("judgeCount/"+tournamentId,count+1L);
    }

    private static final String ratingUpdateTopic="rating-update";
    public void sendRatingUpdate(Long tournamentId){
        kafkaTemplate.send(ratingUpdateTopic,tournamentId);
    }

    private static final String recentActivityUpdateTopic="recent-activity-update";
    public void sendRecentActivityUpdate(Long tournamentId) {
        kafkaTemplate.send(recentActivityUpdateTopic, tournamentId);
    }
}
