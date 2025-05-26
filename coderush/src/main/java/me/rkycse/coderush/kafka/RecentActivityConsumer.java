package me.rkycse.coderush.kafka;

import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import me.rkycse.coderush.entity.RecentActivityEntity;
import me.rkycse.coderush.repository.MTMTournamentRepository;
import me.rkycse.coderush.repository.RecentActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class RecentActivityConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RecentActivityConsumer.class);

    private final RecentActivityRepository recentActivityRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MTMTournamentRepository mtmTournamentRepository;

    public RecentActivityConsumer(RecentActivityRepository recentActivityRepository,
                                  RedisTemplate<String, Object> redisTemplate, MTMTournamentRepository mtmTournamentRepository) {
        this.recentActivityRepository = recentActivityRepository;
        this.redisTemplate = redisTemplate;
        this.mtmTournamentRepository = mtmTournamentRepository;
    }

    /**
     * Listens to the "recent-activity-update" topic for tournament IDs.
     * For each tournament ID received, it fetches tournament details (start time and type)
     * from a microservice, then retrieves all RankDTOs stored in Redis and updates the
     * recent activity for each user by incrementing the count for classic or freestyle.
     */
    @KafkaListener(topics = "recent-activity-update", groupId = "myGroup")
    @Transactional
    public void consumeRecentActivityUpdate(Long tournamentId) {
        logger.info("Received recent activity update for tournament: {}", tournamentId);
        System.out.println("RECENT ACTIVITY UPDATES!!!!!!!!!!");
        // Fetch tournament details (e.g., start time and type) from your microservice.
        //TournamentBaseEntity tournament = tournamentService.getTournamentById(tournamentId);
        MTMTournamentEntity tournament = mtmTournamentRepository.findByTournamentId(tournamentId);
        if (tournament == null) {
            logger.error("Tournament not found for tournamentId: {}", tournamentId);
            return;
        }

        // Use the tournament start time as the key.
        LocalDate tournamentStartDate = Instant.ofEpochMilli(tournament.getStartTime())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        String dateKey = tournamentStartDate.toString();

        // Determine the tournament type: classic or freestyle.
        String tournamentTypeKey = tournament.getTournamentType() == MTMTournamentEntity.TournamentType.CLASSIC
                ? "classic" : "freestyle";

        // Construct the Redis key pattern to fetch all RankDTOs for the given tournament.
        String rankKeyPattern = "rankDTO/" + tournamentId + "/*";
        Set<String> rankKeys = redisTemplate.keys(rankKeyPattern);

        if (rankKeys == null || rankKeys.isEmpty()) {
            logger.info("No RankDTO keys found for tournament: {}", tournamentId);
            return;
        }

        // Process each RankDTO key from Redis.
        for (String rankKey : rankKeys) {
            RankDTO rankDTO = (RankDTO) redisTemplate.opsForValue().get(rankKey);
            if (rankDTO == null) {
                continue;
            }

            String username = rankDTO.getUserName();
            // Retrieve or create the user's recent activity record.
            Optional<RecentActivityEntity> recentActivityOpt = recentActivityRepository.findByUsername(username);
            RecentActivityEntity recentActivity = recentActivityOpt.orElseGet(() -> {
                RecentActivityEntity newActivity = new RecentActivityEntity();
                newActivity.setUsername(username);
                newActivity.setJson(new HashMap<>());
                return newActivity;
            });

            // Retrieve the current activity map.
            Map<String, Map<String, Integer>> activityJson = recentActivity.getJson();
            // Get or create the activity for the tournament's start date.
            Map<String, Integer> activityForDate = activityJson.getOrDefault(dateKey, new HashMap<>());

            // Increment the count for the tournament type.
            int currentCount = activityForDate.getOrDefault(tournamentTypeKey, 0);
            activityForDate.put(tournamentTypeKey, currentCount + 1);

            // Update the JSON and persist the record.
            activityJson.put(dateKey, activityForDate);
            recentActivity.setJson(activityJson);

            recentActivityRepository.save(recentActivity);
            logger.info("Updated recent activity for user: {} on date: {} type: {}", username, dateKey, tournamentTypeKey);
        }
    }
}
