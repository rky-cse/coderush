package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.dto.MatchRequestDTO;
import me.rkycse.coderush.dto.MatchResponseDTO;
import me.rkycse.coderush.dto.PendingMatch;
import me.rkycse.coderush.dto.PendingMatch.PendingStatus;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.DuelTournamentEntity;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.repository.DuelTournamentRepository;
import me.rkycse.coderush.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MatchmakingService {

    private static final String MATCHMAKING_QUEUE_KEY = "matchmaking:queue";
    private static final String PENDING_MATCH_KEY_PREFIX = "pending-match:";

    private static final long MATCH_TIMEOUT_MS = 90_000; // 90 seconds
    private static final int INITIAL_RATING_RANGE = 50;
    private static final int RATING_EXPANSION_STEP = 25;

    // Single general-purpose RedisTemplate for the older configuration
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MatchmakingService.class);
    private final DuelTournamentService duelTournamentService;
    private final UserRepository userRepository;
    private final DuelTournamentRepository duelRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Producer producer;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    // Constructor updated to use a single RedisTemplate
    public MatchmakingService(RedisTemplate<String, Object> redisTemplate, DuelTournamentService duelTournamentService,
                              UserRepository userRepository,
                              DuelTournamentRepository duelRepository,
                              SimpMessagingTemplate messagingTemplate, Producer producer) {
        this.redisTemplate = redisTemplate;
        this.duelTournamentService = duelTournamentService;
        this.userRepository = userRepository;
        this.duelRepository = duelRepository;
        this.messagingTemplate = messagingTemplate;
        this.producer = producer;
    }

    @Transactional
    public MatchResponseDTO processMatchRequest(MatchRequestDTO request) {
        // Check if user is already in a queue and remove them
        removeUserFromQueue(request.getUserId());

        // Proceed to add to new queue
        long timestamp = System.currentTimeMillis();
        double score = request.getRating() + (timestamp % 1000000) / 1000000.0;

        request.setRequestTime(timestamp);
        String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

        // Add to main matchmaking queue
        redisTemplate.opsForZSet().add(queueKey, request, score);

        redisTemplate.opsForValue().set("user_queue:" + request.getUserId(), queueKey);
        redisTemplate.opsForValue().set("user_request:" + request.getUserId(), request);

        // Return a basic response indicating the user is queued
        return new MatchResponseDTO(
                "QUEUED",
                null, // matchId is null
                request.getUserId(),
                null,
                timestamp,
                null // pendingMatchId is also null at this stage
        );
    }

    public void removeUserFromQueue(Long userId) {
        // 1. Get the queue the user is in
        String queueKey = (String) redisTemplate.opsForValue().get("user_queue:" + userId);
        if (queueKey == null)
            return;

        // 2. Get the exact MatchRequestDTO to remove
        MatchRequestDTO request = (MatchRequestDTO) redisTemplate.opsForValue().get("user_request:" + userId);
        if (request == null)
            return;

        System.out.println("[RemoveUserFromQueue Service] Removing User Entry :" + userId + " request.id: "
                + request.getUserId() + "request.rating: " + request.getRating() + "request.timeControl: "
                + request.getTimeControl() + "request.requestTime: " + request.getRequestTime());

        // 3. Remove from ZSET
        redisTemplate.opsForZSet().remove(queueKey, request);

        // 4. Cleanup tracking keys
        redisTemplate.delete("user_queue:" + userId);
        redisTemplate.delete("user_request:" + userId);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processMatchmakingQueue() {
        for (String timeControl : new String[] { "5", "10", "15", "25", "45", "60", "75", "90", "100", "120" }) {
            String queueKey = MATCHMAKING_QUEUE_KEY + ":" + timeControl;

            // Create a copy to avoid concurrent modification issues
            // Cast the result to the expected Set type
            @SuppressWarnings("unchecked")
            Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>(
                    redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, -1));

//            System.out.println(
//                    "[DEBUG] Processing queue of timeControl " + timeControl + " with " + tuples.size() + " entries");

            if (tuples == null || tuples.isEmpty()) {
//                System.out.println("[DEBUG] Queue is empty");
                continue;
            }

            tuples.forEach(tuple -> {
                // We need to cast the value to MatchRequestDTO
                Object value = tuple.getValue();
                if (!(value instanceof MatchRequestDTO)) {
                    System.out.println("[ERROR] Value is not a MatchRequestDTO: " + value);
                    return;
                }

                MatchRequestDTO request = (MatchRequestDTO) value;
                long currentTime = System.currentTimeMillis();
                long timeInQueue = currentTime - request.getRequestTime();

//                System.out.printf("[DEBUG] Processing user %d (in queue for %dms)%n",
//                        request.getUserId(), timeInQueue);

                if (timeInQueue >= MATCH_TIMEOUT_MS || findSuitableOpponent(request, currentTime) != null) {
                    System.out.println("[MATCH] Found candidate for processing: " + request.getUserId());
                    processMatchCandidate(request, currentTime);
                }
            });
        }
    }

    private MatchRequestDTO findSuitableOpponent(MatchRequestDTO request, long currentTime) {
        long timeInQueue = currentTime - request.getRequestTime();
        int ratingRange = calculateCurrentRatingRange(timeInQueue);
        String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

        // Need to use raw type conversion as the older RedisTemplate returns Object
        Set<Object> candidates = redisTemplate.opsForZSet().rangeByScore(
                queueKey,
                request.getRating() - ratingRange,
                request.getRating() + ratingRange);

        if (candidates == null || candidates.isEmpty())
            return null;

        return candidates.stream()
                .filter(obj -> obj instanceof MatchRequestDTO)
                .map(obj -> (MatchRequestDTO) obj)
                .filter(candidate -> !candidate.getUserId().equals(request.getUserId()))
                .findFirst()
                .orElse(null);
    }

    private int calculateCurrentRatingRange(long timeInQueueMs) {
        if (timeInQueueMs < 30_000)
            return INITIAL_RATING_RANGE;
        if (timeInQueueMs < 60_000)
            return INITIAL_RATING_RANGE + RATING_EXPANSION_STEP;
        return INITIAL_RATING_RANGE + (2 * RATING_EXPANSION_STEP);
    }

    private void processMatchCandidate(MatchRequestDTO request, long currentTime) {
        MatchRequestDTO opponent = findSuitableOpponent(request, currentTime);
        long timeInQueue = currentTime - request.getRequestTime();
        String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

        System.out.printf("[PROCESS] User %d - Time in queue: %dms | Opponent found: %b%n",
                request.getUserId(), timeInQueue, opponent != null);

        if (opponent != null) {
            System.out.printf("[MATCH] Found pair: %d vs %d%n",
                    request.getUserId(), opponent.getUserId());

            // Remove both players from the queue
            redisTemplate.opsForZSet().remove(queueKey, request);
            redisTemplate.opsForZSet().remove(queueKey, opponent);

            System.out.println("[REDIS] Removed users from queue: " +
                    request.getUserId() + " and " + opponent.getUserId());

            createPendingMatch(request, opponent);
        } else if (timeInQueue >= MATCH_TIMEOUT_MS) {
            System.out.printf("[TIMEOUT] User %d exceeded 90s queue time%n", request.getUserId());
            handleTimeoutFallback(request);
        }
    }

    private void handleTimeoutFallback(MatchRequestDTO request) {
        System.out.println("[FALLBACK] Handling timeout for user: " + request.getUserId());
        String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

        // Get a potential opponent (handles type conversion)
        Set<Object> candidates = redisTemplate.opsForZSet().range(queueKey, 0, 1);
        if (candidates == null || candidates.isEmpty()) {
            System.out.println("[FALLBACK] No candidates in queue");
            return;
        }

        candidates.stream()
                .filter(obj -> obj instanceof MatchRequestDTO)
                .map(obj -> (MatchRequestDTO) obj)
                .filter(opponent -> !opponent.getUserId().equals(request.getUserId()))
                .findFirst()
                .ifPresentOrElse(
                        opponent -> {
                            System.out.printf("[FALLBACK] Force matching %d with %d%n",
                                    request.getUserId(), opponent.getUserId());
                            redisTemplate.opsForZSet().remove(queueKey, request);
                            redisTemplate.opsForZSet().remove(queueKey, opponent);
                            createPendingMatch(request, opponent);
                        },
                        () -> {
                            // If no opponent found, remove the user from queue
                            System.out.printf("[FALLBACK] No opponents found, removing user %d%n",
                                    request.getUserId());
                            redisTemplate.opsForZSet().remove(queueKey, request);
                        });
    }

    private void createPendingMatch(MatchRequestDTO p1, MatchRequestDTO p2) {
        String pendingMatchId = UUID.randomUUID().toString();

        PendingMatch pm = new PendingMatch();
        pm.setPendingMatchId(pendingMatchId);
        pm.setPlayer1Id(p1.getUserId());
        pm.setPlayer2Id(p2.getUserId());
        pm.setPlayer1Confirmed(false);
        pm.setPlayer2Confirmed(false);
        pm.setCreatedAt(System.currentTimeMillis());
        pm.setStatus(PendingStatus.WAITING_CONFIRMATION);

        System.out.println("[MATCH] Created pending match: " + pendingMatchId + " for players " +
                p1.getUserId() + " and " + p2.getUserId() + " <-------XXXXX------>");

        // Store the pending match in Redis
        redisTemplate.opsForValue().set(
                PENDING_MATCH_KEY_PREFIX + pendingMatchId,
                pm);

        String username1 = userRepository.findById(p1.getUserId())
                .orElseThrow().getUserName();
        String username2 = userRepository.findById(p2.getUserId())
                .orElseThrow().getUserName();

        // Notify each user with "MATCH_FOUND" + pendingMatchId
        messagingTemplate.convertAndSendToUser(
                username1,
                "/queue/match-notifications",
                new MatchResponseDTO(
                        "MATCH_FOUND",
                        null, // matchId is null
                        p1.getUserId(),
                        p2.getUserId(),
                        System.currentTimeMillis(),
                        pendingMatchId // pass the pendingMatchId
                ));

        System.out.println("[Notification for Match found] Player1 notified for a match found<---------");

        messagingTemplate.convertAndSendToUser(
                username2,
                "/queue/match-notifications",
                new MatchResponseDTO(
                        "MATCH_FOUND",
                        null,
                        p2.getUserId(),
                        p1.getUserId(),
                        System.currentTimeMillis(),
                        pendingMatchId));

        System.out.println("[Notification for Match found] Player2 notified for a match found <---------");
    }

    public void cancelPendingMatch(String pendingMatchId, Long userId) {
        System.out.println("[CANCEL] Attempting to cancel pending match: " + pendingMatchId + " by user: " + userId);
        System.out.println("[ cancelPendingMatch] pendingMatchId: " + pendingMatchId + " userId: " + userId);

        // Cast the retrieved value to PendingMatch
        Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
        if (!(obj instanceof PendingMatch)) {
            System.out.println("[CANCEL] Failed - match not found or incorrect type");
            return;
        }

        PendingMatch pm = (PendingMatch) obj;
        if (pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) {
            System.out.println("[CANCEL] Failed - invalid status");
            return;
        }

        if (pm.getPlayer1Id().equals(userId) || pm.getPlayer2Id().equals(userId)) {
            System.out.println("[CANCEL] Valid cancellation request from user: " + userId);
            pm.setStatus(PendingStatus.CANCELLED);

            // Remove from Redis
            redisTemplate.delete(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
            System.out.println("[CANCEL] Removed from Redis: " + pendingMatchId);

            // Notify the other user
            Long otherId = pm.getPlayer1Id().equals(userId) ? pm.getPlayer2Id() : pm.getPlayer1Id();
            System.out.println("[CANCEL] Notifying other user ID: " + otherId);

            try {
                String otherUsername = userRepository.findById(otherId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + otherId))
                        .getUserName();

                System.out.println("[CANCEL] Sending notification to username: " + otherUsername);

                messagingTemplate.convertAndSendToUser(
                        otherUsername,
                        "/queue/match-notifications",
                        new MatchResponseDTO(
                                "MATCH_CANCELLED",
                                null,
                                pm.getPlayer1Id(),
                                pm.getPlayer2Id(),
                                System.currentTimeMillis(),
                                pm.getPendingMatchId()));
                System.out.println("[CANCEL] Notification sent successfully to: " + otherUsername);
            } catch (Exception e) {
                System.err.println("[CANCEL ERROR] Failed to notify other user: " + e.getMessage());
            }
        }
    }

    public void confirmPendingMatch(String pendingMatchId, Long userId) {
        System.out.println("[CONFIRM] Processing confirmation for match: " + pendingMatchId + " by user: " + userId);

        // Cast the retrieved value to PendingMatch
        Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
        if (!(obj instanceof PendingMatch)) {
            System.out.println("[CONFIRM] Failed - match not found or incorrect type");
            return;
        }

        PendingMatch pm = (PendingMatch) obj;
        if (pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) {
            System.out.println("[CONFIRM] Failed - invalid status");
            return;
        }

        System.out.println("[CONFIRM] Current status - P1 confirmed: " + pm.isPlayer1Confirmed()
                + ", P2 confirmed: " + pm.isPlayer2Confirmed());

        if (pm.getPlayer1Id().equals(userId)) {
            pm.setPlayer1Confirmed(true);
            System.out.println("[CONFIRM] Player1 (" + userId + ") confirmed");
        }
        if (pm.getPlayer2Id().equals(userId)) {
            pm.setPlayer2Confirmed(true);
            System.out.println("[CONFIRM] Player2 (" + userId + ") confirmed");
        }

        if (pm.isPlayer1Confirmed() && pm.isPlayer2Confirmed()) {
            System.out.println("[CONFIRM] Both players confirmed match: " + pendingMatchId);
            pm.setStatus(PendingStatus.CONFIRMED);

            try {
                System.out.println("[CONFIRM] Retrieving usernames for notification");
                String username1 = userRepository.findById(pm.getPlayer1Id())
                        .orElseThrow(() -> new RuntimeException("User not found: " + pm.getPlayer1Id()))
                        .getUserName();
                String username2 = userRepository.findById(pm.getPlayer2Id())
                        .orElseThrow(() -> new RuntimeException("User not found: " + pm.getPlayer2Id()))
                        .getUserName();

                System.out.println("[CONFIRM] Sending MATCH_OK to: " + username1 + " and " + username2);

                messagingTemplate.convertAndSendToUser(
                        username1,
                        "/queue/match-notifications",
                        new MatchResponseDTO(
                                "MATCH_OK",
                                null,
                                pm.getPlayer1Id(),
                                pm.getPlayer2Id(),
                                System.currentTimeMillis(),
                                pm.getPendingMatchId()));
                messagingTemplate.convertAndSendToUser(
                        username2,
                        "/queue/match-notifications",
                        new MatchResponseDTO(
                                "MATCH_OK",
                                null,
                                pm.getPlayer2Id(),
                                pm.getPlayer1Id(),
                                System.currentTimeMillis(),
                                pm.getPendingMatchId()));
                System.out.println("[CONFIRM] Notifications sent successfully");

                long scheduledTime = System.currentTimeMillis() + 10_000;
                pm.setScheduledStartTime(scheduledTime);
                System.out.println("[CONFIRM] Scheduled tournament creation at: " + scheduledTime);

                redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingMatchId, pm);
                System.out.println("[CALLING CREATE DUEL TOURNAMENT] XXXX---------XXX----------XXXX");
                createDuelTournament(pm);

            } catch (Exception e) {
                System.err.println("[CONFIRM ERROR] Failed to process confirmation: " + e.getMessage());
            }
        } else {
            System.out.println("[CONFIRM] Partial confirmation - updating status");
            redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingMatchId, pm);
        }
    }

    private void createDuelTournament(PendingMatch pm) {
        // Use the generic redisTemplate for PendingMatch with proper type casting
        System.out.println("[INSIDE CREATE DUEL TOURNAMENT] XXXX---------XXX----------XXXX");
        PendingMatch current = (PendingMatch) redisTemplate.opsForValue().get(
                PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId());
        if (current == null)
            return; // possibly canceled

        if (current.getStatus() != PendingStatus.CONFIRMED)
            return;

        DuelTournamentEntity duel = new DuelTournamentEntity();
        duel.setPlayer1(current.getPlayer1Id());
        duel.setPlayer2(current.getPlayer2Id());
        duel.setStartTime(current.getScheduledStartTime()); // now + 10s
        duel.setRated(true);
        duel.setDurationInSeconds(1800);
        duel.setTournamentType(DuelTournamentEntity.TournamentType.FREE_STYLE);

        // We will add kafka message queue here in order to achieve scalability,
        // fault-tolerance and loose coupling
        DuelTournamentEntity savedDuel = duelRepository.save(duel);

        JoinTournamentResponseDTO response1 = duelTournamentService.joinTournament(savedDuel.getTournamentId(),
                current.getPlayer1Id());
        JoinTournamentResponseDTO response2 = duelTournamentService.joinTournament(savedDuel.getTournamentId(),
                current.getPlayer2Id());

        MatchResponseDTO response = new MatchResponseDTO(
                "MATCH_CREATED",
                savedDuel.getTournamentId(),
                current.getPlayer1Id(),
                current.getPlayer2Id(),
                duel.getStartTime(),
                current.getPendingMatchId());

        TournamentCacheDTO cacheDTO = new TournamentCacheDTO();
        cacheDTO.setTournamentId(savedDuel.getTournamentId());
        cacheDTO.setStartTime(savedDuel.getStartTime());
        cacheDTO.setDurationInSeconds(savedDuel.getDurationInSeconds());
        cacheDTO.setTournamentType(savedDuel.getTournamentType());
        cacheDTO.setScheduled(Boolean.FALSE);
        cacheDTO.setPenaltyFactor(savedDuel.getPenaltyFactor());
        cacheDTO.setTournamentType(savedDuel.getTournamentType());

        logger.info("Starting tournament with ID: {} at {}", savedDuel.getTournamentId(), LocalDateTime.now());

        // Use the generic redisTemplate for TournamentCacheDTO
        redisTemplate.opsForValue().set("$" + savedDuel.getTournamentId(), cacheDTO, cacheDTO.getDurationInSeconds(),
                TimeUnit.SECONDS);
        producer.startTournamentInit(cacheDTO);

        // Get usernames for notifications
        String username1 = userRepository.findById(current.getPlayer1Id())
                .orElseThrow().getUserName();
        String username2 = userRepository.findById(current.getPlayer2Id())
                .orElseThrow().getUserName();

        messagingTemplate.convertAndSendToUser(
                username1,
                "/queue/match-notifications",
                response);
        System.out.println("Player1 notified <---------");

        messagingTemplate.convertAndSendToUser(
                username2,
                "/queue/match-notifications",
                response);
        System.out.println("Player2 notified <---------");

        // remove from Redis or mark as complete
        redisTemplate.delete(PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId());
    }

    // optional method to check status
    public PendingMatch getPendingMatch(String pendingMatchId) {
        return (PendingMatch) redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
    }
}

//
//
// private void createDuelTournament(PendingMatch pm) {
//
// PendingMatch current = pendingRedisTemplate.opsForValue().get(
// PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId()
// );
// if (current == null) return; // possibly canceled
//
// if (current.getStatus() != PendingStatus.CONFIRMED) return;
//
// DuelTournamentEntity duel = new DuelTournamentEntity();
// duel.setPlayer1(current.getPlayer1Id());
// duel.setPlayer2(current.getPlayer2Id());
// duel.setStartTime(current.getScheduledStartTime()); // now + 10s
// duel.setRated(true);
// duel.setDurationInSeconds(1800);
// duel.setTournamentType(DuelTournamentEntity.TournamentType.FREE_STYLE);
//
// // We will add kafka message queue here in order to achieve scalability,
// fault-tolerance and loose coupling
// DuelTournamentEntity savedDuel = duelRepository.save(duel);
//
// JoinTournamentResponseDTO response1 =
// duelTournamentService.joinTournament(savedDuel.getTournamentId(),
// current.getPlayer1Id());
// JoinTournamentResponseDTO response2 =
// duelTournamentService.joinTournament(savedDuel.getTournamentId(),
// current.getPlayer2Id());
//
//
// MatchResponseDTO response = new MatchResponseDTO(
// "MATCH_CREATED",
// savedDuel.getTournamentId(),
// current.getPlayer1Id(),
// current.getPlayer2Id(),
// duel.getStartTime(),
// current.getPendingMatchId()
// );
//
//
// TournamentCacheDTO cacheDTO = new TournamentCacheDTO();
// cacheDTO.setTournamentId(savedDuel.getTournamentId());
// cacheDTO.setStartTime(savedDuel.getStartTime());
// cacheDTO.setDurationInSeconds(savedDuel.getDurationInSeconds());
// cacheDTO.setTournamentType(savedDuel.getTournamentType());
// cacheDTO.setScheduled(Boolean.FALSE);
// cacheDTO.setPenaltyFactor(savedDuel.getPenaltyFactor());
//
//
// logger.info("Starting tournament with ID: {} at {}",
// savedDuel.getTournamentId(), LocalDateTime.now());
// stringRedisTemplate.opsForValue().set("$" + savedDuel.getTournamentId(),
// cacheDTO, cacheDTO.getDurationInSeconds(), TimeUnit.SECONDS);
// producer.startTournamentInit(cacheDTO);
//
// // Get usernames for notifications
// String username1 = userRepository.findById(current.getPlayer1Id())
// .orElseThrow().getUserName();
// String username2 = userRepository.findById(current.getPlayer2Id())
// .orElseThrow().getUserName();
//
// messagingTemplate.convertAndSendToUser(
// username1, // Changed to username
// "/queue/match-notifications",
// response
// );
// System.out.println("Player1 notified <---------");
//
// messagingTemplate.convertAndSendToUser(
// username2, // Changed to username
// "/queue/match-notifications",
// response
// );
// System.out.println("Player2 notified <---------");
//
//
// // remove from Redis or mark as complete
// pendingRedisTemplate.delete(PENDING_MATCH_KEY_PREFIX +
// pm.getPendingMatchId());
// }
//
//// optional method to check status
// public PendingMatch getPendingMatch(String pendingMatchId) {
// return pendingRedisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX +
// pendingMatchId);
// }
