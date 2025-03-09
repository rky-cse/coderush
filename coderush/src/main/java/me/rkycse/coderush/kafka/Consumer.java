package me.rkycse.coderush.kafka;

import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.repository.RankRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    private final RankRepository rankRepository;

    public Consumer(RankRepository rankRepository) {
        this.rankRepository = rankRepository;
    }


    @KafkaListener(topics = "rank-update", groupId = "myGroup")
    public void consume(RankEntity rank) {
        System.out.println("consuming.............................");
        if(rank != null) {
            RankEntity oldRank=rankRepository.findByUserNameAndTournamentId(rank.getUserName(), rank.getTournamentId());
            if(oldRank==null) {
                rankRepository.save(rank);
            }
            else{
                oldRank.setScore(rank.getScore());
                rankRepository.save(oldRank);

            }
        }
    }

}
