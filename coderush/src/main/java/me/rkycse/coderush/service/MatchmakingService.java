package me.rkycse.coderush.service;
import me.rkycse.coderush.repository.UserRepository;
import jakarta.transaction.Transactional;
import me.rkycse.coderush.dto.MatchResult;
import me.rkycse.coderush.entity.TournamentEntity;
import me.rkycse.coderush.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class MatchmakingService {


    private final UserRepository userRepository;
    private final RedisTemplate<String, String> matchmakingRedisTemplate;
    private final TournamentService tournamentService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String WAITING_POOL_KEY = "waiting:1v1";
    private static final String WAITING_POOL_JOIN_TIME_KEY = "waiting:1v1:joinTime";
    private static final long RATING_THRESHOLD = 100;
    private static final long TIMEOUT_SECONDS = 60;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Autowired
    public MatchmakingService(UserRepository userRepository, RedisTemplate<String, String> matchmakingRedisTemplate,
                              TournamentService tournamentService,
                              SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.matchmakingRedisTemplate = matchmakingRedisTemplate;
        this.tournamentService = tournamentService;
        this.messagingTemplate = messagingTemplate;
        executorService.scheduleAtFixedRate(this::matchWaitingPlayers, 0, 2, TimeUnit.MINUTES);
    }

    public MatchResult joinQueue(String playerId, long rating) {
        matchmakingRedisTemplate.opsForZSet().add(WAITING_POOL_KEY, playerId, rating);
        long joinTime = System.currentTimeMillis() / 1000;
        matchmakingRedisTemplate.opsForHash().put(WAITING_POOL_JOIN_TIME_KEY, playerId, String.valueOf(joinTime));

        Set<String> candidates = matchmakingRedisTemplate.opsForZSet()
                .rangeByScore(WAITING_POOL_KEY, rating - RATING_THRESHOLD, rating + RATING_THRESHOLD);

        MatchResult result = new MatchResult();
        result.setPlayer1(playerId);
        if (candidates != null && candidates.size() > 1) {
            candidates.remove(playerId);
            String opponentId = candidates.iterator().next();
            matchmakingRedisTemplate.opsForZSet().remove(WAITING_POOL_KEY, playerId, opponentId);
            matchmakingRedisTemplate.opsForHash().delete(WAITING_POOL_JOIN_TIME_KEY, playerId, opponentId);
            Long tournamentId = createTournament(playerId, opponentId);
            result.setPlayer2(opponentId);
            result.setTournamentId(tournamentId);
            notifyPlayers(playerId, opponentId, result);
            return result;
        }
        return result;
    }

    public MatchResult matchWaitingPlayers() {
        MatchResult result = new MatchResult();
        try {
            Set<String> waitingPlayers = matchmakingRedisTemplate.opsForZSet().range(WAITING_POOL_KEY, 0, -1);
            if (waitingPlayers == null || waitingPlayers.isEmpty()) return result;

            long currentTime = System.currentTimeMillis() / 1000;
            for (String playerId : waitingPlayers) {
                Object joinTimeObj = matchmakingRedisTemplate.opsForHash().get(WAITING_POOL_JOIN_TIME_KEY, playerId);
                if (joinTimeObj == null) continue;
                long joinTime = Long.parseLong(joinTimeObj.toString());
                long waitingTime = currentTime - joinTime;

                if (waitingTime >= TIMEOUT_SECONDS) {
                    Double rating = matchmakingRedisTemplate.opsForZSet().score(WAITING_POOL_KEY, playerId);
                    if (rating == null) continue;
                    long relaxedThreshold = RATING_THRESHOLD * 2;
                    Set<String> candidates = matchmakingRedisTemplate.opsForZSet()
                            .rangeByScore(WAITING_POOL_KEY, rating - relaxedThreshold, rating + relaxedThreshold);
                    if (candidates != null && candidates.size() > 1) {
                        candidates.remove(playerId);
                        String opponentId = candidates.iterator().next();
                        matchmakingRedisTemplate.opsForZSet().remove(WAITING_POOL_KEY, playerId, opponentId);
                        matchmakingRedisTemplate.opsForHash().delete(WAITING_POOL_JOIN_TIME_KEY, playerId, opponentId);
                        Long tournamentId = createTournament(playerId, opponentId);

                        result.setPlayer1(playerId);
                        result.setPlayer2(opponentId);
                        result.setTournamentId(tournamentId);
                        notifyPlayers(playerId, opponentId, result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Long createTournament(String playerId, String opponentId) {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setCreatorUserName(playerId);
        tournament.setDescription("1v1 Match between " + playerId + " and " + opponentId);
        tournament.setName("Tournament between " + playerId + " and " + opponentId);
        tournament.setStartTime(LocalDateTime.now().plusSeconds(10));
        tournament.setRated(false);
        tournament.setMinRatingReq(0);
        tournament.setMaxRatingReq(Long.MAX_VALUE);
        tournament.setDurationInSeconds(3600);
        tournament.setTournamentMode("1v1");
        tournament.setVisibility("PRIVATE");
        tournament.setPassword(null);
        TournamentEntity createdTournament = tournamentService.createTournament(tournament);
        tournamentService.joinTournament(createdTournament.getTournamentId());
        return createdTournament.getTournamentId();
    }

    private void notifyPlayers(String player1, String player2, MatchResult result) {
        // Convert the string user IDs to Longs
        Long userId1 = Long.parseLong(player1);
        Long userId2 = Long.parseLong(player2);

        // Retrieve the user entities using the injected userRepository

        Optional<UserEntity> optionalUser1 = userRepository.findById(userId1);
        Optional<UserEntity> optionalUser2 = userRepository.findById(userId2);

        if (optionalUser1.isPresent() && optionalUser2.isPresent()) {
            String username1 = optionalUser1.get().getUserName();
            String username2 = optionalUser2.get().getUserName();

            System.out.println("Notifying players: " + username1 + " and " + username2);
            // Send notifications to the correct user-specific destinations
            messagingTemplate.convertAndSendToUser(username1,"/queue/match",result);
            result.setOpponentTrue();
            messagingTemplate.convertAndSendToUser(username2,"/queue/match", result);
        } else {
            System.out.println("User(s) not found for notification: " + player1 + ", " + player2);
        }
    }

    private String toString(MatchResult result) {
        return "MatchResult{" +
                "player1='" + result.getPlayer1() + '\'' +
                ", player2='" + result.getPlayer2() + '\'' +
                ", tournamentId=" + result.getTournamentID() +
                '}';
    }


}
