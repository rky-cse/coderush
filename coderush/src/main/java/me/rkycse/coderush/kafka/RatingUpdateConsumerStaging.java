package me.rkycse.coderush.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.UpdatedRating;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import me.rkycse.coderush.repository.RankRepository;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.repository.UserTournamentRatingRepository;
import me.rkycse.coderush.service.RatingUpdateStagingService;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingUpdateConsumerStaging {

    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final RatingUpdateStagingService ratingUpdateStagingService;
    private final UserTournamentRatingRepository userTournamentRatingRepository;
    private static final double K = 20.0;
    private static final int BATCH_SIZE=1000;

    public RatingUpdateConsumerStaging(RankRepository rankRepository,
                                       RatingUpdateStagingService ratingUpdateStagingService,
                                       UserRepository userRepository, UserTournamentRatingRepository userTournamentRatingRepository) {
        this.rankRepository = rankRepository;
        this.ratingUpdateStagingService = ratingUpdateStagingService;
        this.userRepository = userRepository;
        this.userTournamentRatingRepository = userTournamentRatingRepository;
    }

    @KafkaListener(topics = "rating-update", groupId = "myGroup")
    @Transactional
    public void consumeRatingUpdate(Long tournamentId) {
        System.out.println("Kafka rating update triggered for tournament " + tournamentId);

        // 1. Fetch and sort the rank list for the tournament.
        List<RankEntity> ranks = rankRepository.findByTournamentId(tournamentId);
        ranks.sort((r1, r2) -> {
            int scoreCompare = Long.compare(r2.getScore(), r1.getScore());
            if (scoreCompare != 0) return scoreCompare;
            return Long.compare(r1.getPenalty(), r2.getPenalty());
        });

        int totalPlayers = ranks.size();
        double[] expectedRanks = new double[totalPlayers];

        // 2. Calculate expected rank for each player.
        for (int i = 0; i < totalPlayers; i++) {
            RankEntity player = ranks.get(i);
            double expectedRank = 1.0; // Base for self-comparison.
            for (int j = 0; j < totalPlayers; j++) {
                if (i == j) continue;
                RankEntity opponent = ranks.get(j);
                double probability = 1.0 / (1.0 + Math.pow(10, (player.getRating() - opponent.getRating()) / 400.0));
                expectedRank += probability;
            }
            expectedRanks[i] = expectedRank;
        }

        List<UpdatedRating> updatedRatings = new ArrayList<>();
        List<UserTournamentRatingEntity>ratingsToInsert = new ArrayList<>();

        // 3. Compute the new rating for each player.
        for (int i = 0; i < totalPlayers; i++) {
            RankEntity player = ranks.get(i);
            UserTournamentRatingEntity ratingOfPlayer = new UserTournamentRatingEntity();
            ratingOfPlayer.setOldRating(player.getRating());
            ratingOfPlayer.setTournamentId(tournamentId);
            ratingOfPlayer.setUsername(player.getUserName());
            ratingOfPlayer.setRatingUpdateTimestamp(TimeUtil.getCurrentEpochMillis());
            int actualRank = i + 1; // 1-indexed rank.
            double ratingChange = K * (expectedRanks[i] - actualRank);
            long newRating = Math.round(player.getRating() + ratingChange);
            player.setRating(newRating);
            ratingOfPlayer.setNewRating(newRating);
            ratingsToInsert.add(ratingOfPlayer);
            // Get the corresponding UserEntity and add to the update list.
            //Optional<UserEntity> userOpt = userRepository.findByUserName(player.getUserName());
//            Optional<Long> userRatingOpt = userRepository.getRatingByUserName(player.getUserName());
//            userRatingOpt.ifPresent(rating -> {
//
//            });

            updatedRatings.add(new UpdatedRating(player.getUserName(), newRating));
        }

        // 4. Perform the bulk update using the staging table approach.
        ratingUpdateStagingService.bulkUpdateUserRatings(updatedRatings);
        // Split into sub-batches and perform bulk inserts
        for (int i = 0; i < ratingsToInsert.size(); i += BATCH_SIZE) {
            int end = Math.min(ratingsToInsert.size(), i + BATCH_SIZE);
            List<UserTournamentRatingEntity> subBatch = ratingsToInsert.subList(i, end);
            userTournamentRatingRepository.saveAll(subBatch);
            userTournamentRatingRepository.flush(); // force immediate database write
        }

        // 5. Optionally update the rank records via JPA.
        //rankRepository.saveAll(ranks);
     }
}

