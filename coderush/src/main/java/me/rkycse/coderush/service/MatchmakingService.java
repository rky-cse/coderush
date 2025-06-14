// package me.rkycse.coderush.service;
// import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
// import me.rkycse.coderush.dto.MatchRequestDTO;
// import me.rkycse.coderush.dto.MatchResponseDTO;
// import me.rkycse.coderush.dto.PendingMatch;
// import me.rkycse.coderush.dto.PendingMatch.PendingStatus;
// import me.rkycse.coderush.dto.TournamentCacheDTO;
// import me.rkycse.coderush.entity.DuelTournamentEntity;
// import me.rkycse.coderush.kafka.Producer;
// import me.rkycse.coderush.repository.DuelTournamentRepository;
// import me.rkycse.coderush.repository.UserRepository;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.ZSetOperations;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import java.time.LocalDateTime;
// import java.util.concurrent.TimeUnit;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import java.util.HashSet;
// import java.util.Set;
// import java.util.UUID;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// @Service
// public class MatchmakingService {

//     private static final String MATCHMAKING_QUEUE_KEY = "matchmaking:queue";
//     private static final String PENDING_MATCH_KEY_PREFIX = "pending-match:";

//     private static final long MATCH_TIMEOUT_MS = 90_000; // 90 seconds
//     private static final int INITIAL_RATING_RANGE = 50;
//     private static final int RATING_EXPANSION_STEP = 25;

//     // Single general-purpose RedisTemplate for the older configuration
//     private final RedisTemplate<String, Object> redisTemplate;
//     private static final Logger logger = LoggerFactory.getLogger(MatchmakingService.class);
//     private final DuelTournamentService duelTournamentService;
//     private final UserRepository userRepository;
//     private final DuelTournamentRepository duelRepository;
//     private final SimpMessagingTemplate messagingTemplate;
//     private final Producer producer;

//     private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

//     // Constructor updated to use a single RedisTemplate
//     public MatchmakingService(RedisTemplate<String, Object> redisTemplate, DuelTournamentService duelTournamentService,
//                               UserRepository userRepository,
//                               DuelTournamentRepository duelRepository,
//                               SimpMessagingTemplate messagingTemplate, Producer producer) {
//         this.redisTemplate = redisTemplate;
//         this.duelTournamentService = duelTournamentService;
//         this.userRepository = userRepository;
//         this.duelRepository = duelRepository;
//         this.messagingTemplate = messagingTemplate;
//         this.producer = producer;
//     }

//     @Transactional
//     public MatchResponseDTO processMatchRequest(MatchRequestDTO request) {
//         // Check if user is already in a queue and remove them
//         removeUserFromQueue(request.getUserId());

//         // Proceed to add to new queue
//         long timestamp = System.currentTimeMillis();
//         double score = request.getRating() + (timestamp % 1000000) / 1000000.0;

//         request.setRequestTime(timestamp);
//         String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

//         // Add to main matchmaking queue
//         redisTemplate.opsForZSet().add(queueKey, request, score);

//         redisTemplate.opsForValue().set("user_queue:" + request.getUserId(), queueKey);
//         redisTemplate.opsForValue().set("user_request:" + request.getUserId(), request);

//         // Return a basic response indicating the user is queued
//         return new MatchResponseDTO(
//                 "QUEUED",
//                 null, // matchId is null
//                 request.getUserId(),
//                 null,
//                 timestamp,
//                 null // pendingMatchId is also null at this stage
//         );
//     }

//     public void removeUserFromQueue(Long userId) {
//         // 1. Get the queue the user is in
//         String queueKey = (String) redisTemplate.opsForValue().get("user_queue:" + userId);
//         if (queueKey == null)
//             return;

//         // 2. Get the exact MatchRequestDTO to remove
//         MatchRequestDTO request = (MatchRequestDTO) redisTemplate.opsForValue().get("user_request:" + userId);
//         if (request == null)
//             return;

//         System.out.println("[RemoveUserFromQueue Service] Removing User Entry :" + userId + " request.id: "
//                 + request.getUserId() + "request.rating: " + request.getRating() + "request.timeControl: "
//                 + request.getTimeControl() + "request.requestTime: " + request.getRequestTime());

//         // 3. Remove from ZSET
//         redisTemplate.opsForZSet().remove(queueKey, request);

//         // 4. Cleanup tracking keys
//         redisTemplate.delete("user_queue:" + userId);
//         redisTemplate.delete("user_request:" + userId);
//     }

//     @Scheduled(fixedRate = 5000)
//     @Transactional
//     public void processMatchmakingQueue() {
//         for (String timeControl : new String[] { "5", "10", "15", "25", "45", "60", "75", "90", "100", "120" }) {
//             String queueKey = MATCHMAKING_QUEUE_KEY + ":" + timeControl;

//             // Create a copy to avoid concurrent modification issues
//             // Cast the result to the expected Set type
//             @SuppressWarnings("unchecked")
//             Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>(
//                     redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, -1));

// //            System.out.println(
// //                    "[DEBUG] Processing queue of timeControl " + timeControl + " with " + tuples.size() + " entries");

//             if (tuples == null || tuples.isEmpty()) {
// //                System.out.println("[DEBUG] Queue is empty");
//                 continue;
//             }

//             tuples.forEach(tuple -> {
//                 // We need to cast the value to MatchRequestDTO
//                 Object value = tuple.getValue();
//                 if (!(value instanceof MatchRequestDTO)) {
//                     System.out.println("[ERROR] Value is not a MatchRequestDTO: " + value);
//                     return;
//                 }

//                 MatchRequestDTO request = (MatchRequestDTO) value;
//                 long currentTime = System.currentTimeMillis();
//                 long timeInQueue = currentTime - request.getRequestTime();

// //                System.out.printf("[DEBUG] Processing user %d (in queue for %dms)%n",
// //                        request.getUserId(), timeInQueue);

//                 if (timeInQueue >= MATCH_TIMEOUT_MS || findSuitableOpponent(request, currentTime) != null) {
//                     System.out.println("[MATCH] Found candidate for processing: " + request.getUserId());
//                     processMatchCandidate(request, currentTime);
//                 }
//             });
//         }
//     }

//     private MatchRequestDTO findSuitableOpponent(MatchRequestDTO request, long currentTime) {
//         long timeInQueue = currentTime - request.getRequestTime();
//         int ratingRange = calculateCurrentRatingRange(timeInQueue);
//         String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

//         // Need to use raw type conversion as the older RedisTemplate returns Object
//         Set<Object> candidates = redisTemplate.opsForZSet().rangeByScore(
//                 queueKey,
//                 request.getRating() - ratingRange,
//                 request.getRating() + ratingRange);

//         if (candidates == null || candidates.isEmpty())
//             return null;

//         return candidates.stream()
//                 .filter(obj -> obj instanceof MatchRequestDTO)
//                 .map(obj -> (MatchRequestDTO) obj)
//                 .filter(candidate -> !candidate.getUserId().equals(request.getUserId()))
//                 .findFirst()
//                 .orElse(null);
//     }

//     private int calculateCurrentRatingRange(long timeInQueueMs) {
//         if (timeInQueueMs < 30_000)
//             return INITIAL_RATING_RANGE;
//         if (timeInQueueMs < 60_000)
//             return INITIAL_RATING_RANGE + RATING_EXPANSION_STEP;
//         return INITIAL_RATING_RANGE + (2 * RATING_EXPANSION_STEP);
//     }

//     private void processMatchCandidate(MatchRequestDTO request, long currentTime) {
//         MatchRequestDTO opponent = findSuitableOpponent(request, currentTime);
//         long timeInQueue = currentTime - request.getRequestTime();
//         String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

//         System.out.printf("[PROCESS] User %d - Time in queue: %dms | Opponent found: %b%n",
//                 request.getUserId(), timeInQueue, opponent != null);

//         if (opponent != null) {
//             System.out.printf("[MATCH] Found pair: %d vs %d%n",
//                     request.getUserId(), opponent.getUserId());

//             // Remove both players from the queue
//             redisTemplate.opsForZSet().remove(queueKey, request);
//             redisTemplate.opsForZSet().remove(queueKey, opponent);

//             System.out.println("[REDIS] Removed users from queue: " +
//                     request.getUserId() + " and " + opponent.getUserId());

//             createPendingMatch(request, opponent);
//         } else if (timeInQueue >= MATCH_TIMEOUT_MS) {
//             System.out.printf("[TIMEOUT] User %d exceeded 90s queue time%n", request.getUserId());
//             handleTimeoutFallback(request);
//         }
//     }

//     private void handleTimeoutFallback(MatchRequestDTO request) {
//         System.out.println("[FALLBACK] Handling timeout for user: " + request.getUserId());
//         String queueKey = MATCHMAKING_QUEUE_KEY + ":" + request.getTimeControl().toString();

//         // Get a potential opponent (handles type conversion)
//         Set<Object> candidates = redisTemplate.opsForZSet().range(queueKey, 0, 1);
//         if (candidates == null || candidates.isEmpty()) {
//             System.out.println("[FALLBACK] No candidates in queue");
//             return;
//         }

//         candidates.stream()
//                 .filter(obj -> obj instanceof MatchRequestDTO)
//                 .map(obj -> (MatchRequestDTO) obj)
//                 .filter(opponent -> !opponent.getUserId().equals(request.getUserId()))
//                 .findFirst()
//                 .ifPresentOrElse(
//                         opponent -> {
//                             System.out.printf("[FALLBACK] Force matching %d with %d%n",
//                                     request.getUserId(), opponent.getUserId());
//                             redisTemplate.opsForZSet().remove(queueKey, request);
//                             redisTemplate.opsForZSet().remove(queueKey, opponent);
//                             createPendingMatch(request, opponent);
//                         },
//                         () -> {
//                             // If no opponent found, remove the user from queue
//                             System.out.printf("[FALLBACK] No opponents found, removing user %d%n",
//                                     request.getUserId());
//                             redisTemplate.opsForZSet().remove(queueKey, request);
//                         });
//     }

//     private void createPendingMatch(MatchRequestDTO p1, MatchRequestDTO p2) {
//         String pendingMatchId = UUID.randomUUID().toString();

//         PendingMatch pm = new PendingMatch();
//         pm.setPendingMatchId(pendingMatchId);
//         pm.setPlayer1Id(p1.getUserId());
//         pm.setPlayer2Id(p2.getUserId());
//         pm.setPlayer1Confirmed(false);
//         pm.setPlayer2Confirmed(false);
//         pm.setCreatedAt(System.currentTimeMillis());
//         pm.setStatus(PendingStatus.WAITING_CONFIRMATION);

//         System.out.println("[MATCH] Created pending match: " + pendingMatchId + " for players " +
//                 p1.getUserId() + " and " + p2.getUserId() + " <-------XXXXX------>");

//         // Store the pending match in Redis
//         redisTemplate.opsForValue().set(
//                 PENDING_MATCH_KEY_PREFIX + pendingMatchId,
//                 pm);

//         String username1 = userRepository.findById(p1.getUserId())
//                 .orElseThrow().getUserName();
//         String username2 = userRepository.findById(p2.getUserId())
//                 .orElseThrow().getUserName();

//         // Notify each user with "MATCH_FOUND" + pendingMatchId
//         messagingTemplate.convertAndSendToUser(
//                 username1,
//                 "/queue/match-notifications",
//                 new MatchResponseDTO(
//                         "MATCH_FOUND",
//                         null, // matchId is null
//                         p1.getUserId(),
//                         p2.getUserId(),
//                         System.currentTimeMillis(),
//                         pendingMatchId // pass the pendingMatchId
//                 ));

//         System.out.println("[Notification for Match found] Player1 notified for a match found<---------");

//         messagingTemplate.convertAndSendToUser(
//                 username2,
//                 "/queue/match-notifications",
//                 new MatchResponseDTO(
//                         "MATCH_FOUND",
//                         null,
//                         p2.getUserId(),
//                         p1.getUserId(),
//                         System.currentTimeMillis(),
//                         pendingMatchId));

//         System.out.println("[Notification for Match found] Player2 notified for a match found <---------");
//     }

//     public void cancelPendingMatch(String pendingMatchId, Long userId) {
//         System.out.println("[CANCEL] Attempting to cancel pending match: " + pendingMatchId + " by user: " + userId);
//         System.out.println("[ cancelPendingMatch] pendingMatchId: " + pendingMatchId + " userId: " + userId);

//         // Cast the retrieved value to PendingMatch
//         Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
//         if (!(obj instanceof PendingMatch)) {
//             System.out.println("[CANCEL] Failed - match not found or incorrect type");
//             return;
//         }

//         PendingMatch pm = (PendingMatch) obj;
//         if (pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) {
//             System.out.println("[CANCEL] Failed - invalid status");
//             return;
//         }

//         if (pm.getPlayer1Id().equals(userId) || pm.getPlayer2Id().equals(userId)) {
//             System.out.println("[CANCEL] Valid cancellation request from user: " + userId);
//             pm.setStatus(PendingStatus.CANCELLED);

//             // Remove from Redis
//             redisTemplate.delete(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
//             System.out.println("[CANCEL] Removed from Redis: " + pendingMatchId);

//             // Notify the other user
//             Long otherId = pm.getPlayer1Id().equals(userId) ? pm.getPlayer2Id() : pm.getPlayer1Id();
//             System.out.println("[CANCEL] Notifying other user ID: " + otherId);

//             try {
//                 String otherUsername = userRepository.findById(otherId)
//                         .orElseThrow(() -> new RuntimeException("User not found: " + otherId))
//                         .getUserName();

//                 System.out.println("[CANCEL] Sending notification to username: " + otherUsername);

//                 messagingTemplate.convertAndSendToUser(
//                         otherUsername,
//                         "/queue/match-notifications",
//                         new MatchResponseDTO(
//                                 "MATCH_CANCELLED",
//                                 null,
//                                 pm.getPlayer1Id(),
//                                 pm.getPlayer2Id(),
//                                 System.currentTimeMillis(),
//                                 pm.getPendingMatchId()));
//                 System.out.println("[CANCEL] Notification sent successfully to: " + otherUsername);
//             } catch (Exception e) {
//                 System.err.println("[CANCEL ERROR] Failed to notify other user: " + e.getMessage());
//             }
//         }
//     }

//     public void confirmPendingMatch(String pendingMatchId, Long userId) {
//         System.out.println("[CONFIRM] Processing confirmation for match: " + pendingMatchId + " by user: " + userId);

//         // Cast the retrieved value to PendingMatch
//         Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
//         if (!(obj instanceof PendingMatch)) {
//             System.out.println("[CONFIRM] Failed - match not found or incorrect type");
//             return;
//         }

//         PendingMatch pm = (PendingMatch) obj;
//         if (pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) {
//             System.out.println("[CONFIRM] Failed - invalid status");
//             return;
//         }

//         System.out.println("[CONFIRM] Current status - P1 confirmed: " + pm.isPlayer1Confirmed()
//                 + ", P2 confirmed: " + pm.isPlayer2Confirmed());

//         if (pm.getPlayer1Id().equals(userId)) {
//             pm.setPlayer1Confirmed(true);
//             System.out.println("[CONFIRM] Player1 (" + userId + ") confirmed");
//         }
//         if (pm.getPlayer2Id().equals(userId)) {
//             pm.setPlayer2Confirmed(true);
//             System.out.println("[CONFIRM] Player2 (" + userId + ") confirmed");
//         }

//         if (pm.isPlayer1Confirmed() && pm.isPlayer2Confirmed()) {
//             System.out.println("[CONFIRM] Both players confirmed match: " + pendingMatchId);
//             pm.setStatus(PendingStatus.CONFIRMED);

//             try {
//                 System.out.println("[CONFIRM] Retrieving usernames for notification");
//                 String username1 = userRepository.findById(pm.getPlayer1Id())
//                         .orElseThrow(() -> new RuntimeException("User not found: " + pm.getPlayer1Id()))
//                         .getUserName();
//                 String username2 = userRepository.findById(pm.getPlayer2Id())
//                         .orElseThrow(() -> new RuntimeException("User not found: " + pm.getPlayer2Id()))
//                         .getUserName();

//                 System.out.println("[CONFIRM] Sending MATCH_OK to: " + username1 + " and " + username2);

//                 messagingTemplate.convertAndSendToUser(
//                         username1,
//                         "/queue/match-notifications",
//                         new MatchResponseDTO(
//                                 "MATCH_OK",
//                                 null,
//                                 pm.getPlayer1Id(),
//                                 pm.getPlayer2Id(),
//                                 System.currentTimeMillis(),
//                                 pm.getPendingMatchId()));
//                 messagingTemplate.convertAndSendToUser(
//                         username2,
//                         "/queue/match-notifications",
//                         new MatchResponseDTO(
//                                 "MATCH_OK",
//                                 null,
//                                 pm.getPlayer2Id(),
//                                 pm.getPlayer1Id(),
//                                 System.currentTimeMillis(),
//                                 pm.getPendingMatchId()));
//                 System.out.println("[CONFIRM] Notifications sent successfully");

//                 long scheduledTime = System.currentTimeMillis() + 10_000;
//                 pm.setScheduledStartTime(scheduledTime);
//                 System.out.println("[CONFIRM] Scheduled tournament creation at: " + scheduledTime);

//                 redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingMatchId, pm);
//                 System.out.println("[CALLING CREATE DUEL TOURNAMENT] XXXX---------XXX----------XXXX");
//                 createDuelTournament(pm);

//             } catch (Exception e) {
//                 System.err.println("[CONFIRM ERROR] Failed to process confirmation: " + e.getMessage());
//             }
//         } else {
//             System.out.println("[CONFIRM] Partial confirmation - updating status");
//             redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingMatchId, pm);
//         }
//     }

//     private void createDuelTournament(PendingMatch pm) {
//         // Use the generic redisTemplate for PendingMatch with proper type casting
//         System.out.println("[INSIDE CREATE DUEL TOURNAMENT] XXXX---------XXX----------XXXX");
//         PendingMatch current = (PendingMatch) redisTemplate.opsForValue().get(
//                 PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId());
//         if (current == null)
//             return; // possibly canceled

//         if (current.getStatus() != PendingStatus.CONFIRMED)
//             return;

//         DuelTournamentEntity duel = new DuelTournamentEntity();
//         duel.setPlayer1(current.getPlayer1Id());
//         duel.setPlayer2(current.getPlayer2Id());
//         duel.setStartTime(current.getScheduledStartTime()); // now + 10s
//         duel.setRated(true);
//         duel.setDurationInSeconds(1800);
//         duel.setTournamentType(DuelTournamentEntity.TournamentType.FREE_STYLE);

//         // We will add kafka message queue here in order to achieve scalability,
//         // fault-tolerance and loose coupling
//         DuelTournamentEntity savedDuel = duelRepository.save(duel);

//         JoinTournamentResponseDTO response1 = duelTournamentService.joinTournament(savedDuel.getTournamentId(),
//                 current.getPlayer1Id());
//         JoinTournamentResponseDTO response2 = duelTournamentService.joinTournament(savedDuel.getTournamentId(),
//                 current.getPlayer2Id());

//         MatchResponseDTO response = new MatchResponseDTO(
//                 "MATCH_CREATED",
//                 savedDuel.getTournamentId(),
//                 current.getPlayer1Id(),
//                 current.getPlayer2Id(),
//                 duel.getStartTime(),
//                 current.getPendingMatchId());

//         TournamentCacheDTO cacheDTO = new TournamentCacheDTO();
//         cacheDTO.setTournamentId(savedDuel.getTournamentId());
//         cacheDTO.setStartTime(savedDuel.getStartTime());
//         cacheDTO.setDurationInSeconds(savedDuel.getDurationInSeconds());
//         cacheDTO.setTournamentType(savedDuel.getTournamentType());
//         cacheDTO.setScheduled(Boolean.FALSE);
//         cacheDTO.setPenaltyFactor(savedDuel.getPenaltyFactor());
//         cacheDTO.setTournamentType(savedDuel.getTournamentType());

//         logger.info("Starting tournament with ID: {} at {}", savedDuel.getTournamentId(), LocalDateTime.now());

//         // Use the generic redisTemplate for TournamentCacheDTO
//         redisTemplate.opsForValue().set("$" + savedDuel.getTournamentId(), cacheDTO, cacheDTO.getDurationInSeconds(),
//                 TimeUnit.SECONDS);
//         producer.startTournamentInit(cacheDTO);

//         // Get usernames for notifications
//         String username1 = userRepository.findById(current.getPlayer1Id())
//                 .orElseThrow().getUserName();
//         String username2 = userRepository.findById(current.getPlayer2Id())
//                 .orElseThrow().getUserName();

//         messagingTemplate.convertAndSendToUser(
//                 username1,
//                 "/queue/match-notifications",
//                 response);
//         System.out.println("Player1 notified <---------");

//         messagingTemplate.convertAndSendToUser(
//                 username2,
//                 "/queue/match-notifications",
//                 response);
//         System.out.println("Player2 notified <---------");

//         // remove from Redis or mark as complete
//         redisTemplate.delete(PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId());
//     }

//     // optional method to check status
//     public PendingMatch getPendingMatch(String pendingMatchId) {
//         return (PendingMatch) redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
//     }
// }



package me.rkycse.coderush.service;
import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.dto.MatchRequestDTO;
import me.rkycse.coderush.dto.MatchResponseDTO;
import me.rkycse.coderush.dto.PendingMatch;
import me.rkycse.coderush.dto.PendingMatch.PendingStatus;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.DuelTournamentEntity;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.repository.DuelTournamentRepository;
import me.rkycse.coderush.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
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
    // Define supported tournament types (must match DTO values)
    private static final List<String> TOURNAMENT_TYPES = Arrays.asList("classic", "freestyle");
    private final RedisTemplate<String, Object> redisTemplate;
    private final DuelTournamentService duelTournamentService;
    private static final long AUTO_CANCEL_MS = 15_000;   // 15 seconds
    private final UserRepository userRepository;
    private final DuelTournamentRepository duelRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Producer producer;
    private static final Logger logger = LoggerFactory.getLogger(MatchmakingService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private record PlayerInfo(Long id, String name, int rating) {}

    public MatchmakingService(RedisTemplate<String, Object> redisTemplate,
                              DuelTournamentService duelTournamentService,
                              UserRepository userRepository,
                              DuelTournamentRepository duelRepository,
                              SimpMessagingTemplate messagingTemplate,
                              Producer producer) {
        this.redisTemplate = redisTemplate;
        this.duelTournamentService = duelTournamentService;
        this.userRepository = userRepository;
        this.duelRepository = duelRepository;
        this.messagingTemplate = messagingTemplate;
        this.producer = producer;
    }


    private PlayerInfo loadPlayerInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Unknown user " + userId));
        return new PlayerInfo(userId, user.getUserName(), Math.toIntExact(user.getRating()));
    }


    @Transactional
    public MatchResponseDTO processMatchRequest(MatchRequestDTO request) {
        // Remove from any existing queue
        removeUserFromQueue(request.getUserId());

        long timestamp = System.currentTimeMillis();
        request.setRequestTime(timestamp);

        String type = request.getTournamentType() != null ? request.getTournamentType().toLowerCase() : "freestyle";
        String queueKey = buildQueueKey(request.getTimeControl(), type);

        System.out.println("[MatchmakingService] Processing match request for user: " + request.getUserId() +
                ", timeControl: " + request.getTimeControl() + ", type: " + request.getTournamentType());

        double score = request.getRating() + (timestamp % 1000000) / 1000000.0;
        redisTemplate.opsForZSet().add(queueKey, request, score);

        // Track user's queue and request
        redisTemplate.opsForValue().set("user_queue:" + request.getUserId(), queueKey);
        redisTemplate.opsForValue().set("user_request:" + request.getUserId(), request);

        // 3) Load the requestor's name & rating
        PlayerInfo me = loadPlayerInfo(request.getUserId());

        // 4) Return the enriched DTO
        return new MatchResponseDTO(
                "QUEUED",
                null,                  // no matchId yet
                me.id, me.name, me.rating,
                null, "", 0,           // no opponent yet
                timestamp,
                null                   // no pendingMatchId yet
        );
    }

    public void removeUserFromQueue(Long userId) {
        String queueKey = (String) redisTemplate.opsForValue().get("user_queue:" + userId);
        if (queueKey == null) return;

        MatchRequestDTO request = (MatchRequestDTO) redisTemplate.opsForValue().get("user_request:" + userId);
        if (request != null) {
            redisTemplate.opsForZSet().remove(queueKey, request);
        }

        redisTemplate.delete("user_queue:" + userId);
        redisTemplate.delete("user_request:" + userId);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processMatchmakingQueue() {
        // Iterate over each timeControl and tournament type
        System.out.println( "[processMatchmakingQueue()] Starting matchmaking queue processing... ");
        List<String> timeControls = Arrays.asList("5", "10", "15", "25", "45", "60", "75", "90", "100", "120");
        for (String timeControl : timeControls) {
            for (String type : TOURNAMENT_TYPES) {
                String queueKey = buildQueueKey((Long.valueOf(timeControl))*(60), type);
                System.out.println("[processMatchmakingQueue()] KEY : " + queueKey + " size: " +
                        redisTemplate.opsForZSet().size(queueKey));
                Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>(
                        redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, -1)
                );
                if (tuples == null || tuples.isEmpty()) continue;

                tuples.forEach(tuple -> {
                    Object val = tuple.getValue();
                    if (!(val instanceof MatchRequestDTO)) return;
                    MatchRequestDTO request = (MatchRequestDTO) val;
                    System.out.println("[processMatchmakingQueue()] Processing match request for user: " + request.getUserId() +
                            ", timeControl: " + request.getTimeControl() + ", type: " + request.getTournamentType());
                    long now = System.currentTimeMillis();
                    if (now - request.getRequestTime() >= MATCH_TIMEOUT_MS ||
                            findSuitableOpponent(request, now) != null) {
                        System.out.println( "[processMatchmakingQueue()] Processing match candidate: " + request.getUserId());
                        processMatchCandidate(request, now);
                    }
                    else {

                        System.out.println("[processMatchmakingQueue()] No suitable opponent found for user: " +
                                request.getUserId() + " in queue: " + queueKey);
                        //handleTimeoutFallback(request);
                    }
                });
            }
        }
    }

    private MatchRequestDTO findSuitableOpponent(MatchRequestDTO request, long now) {
        long elapsed = now - request.getRequestTime();
        int range = calculateCurrentRatingRange(elapsed);
        String type = request.getTournamentType().toLowerCase();
        String queueKey = buildQueueKey(request.getTimeControl(), type);

        Set<Object> candidates = redisTemplate.opsForZSet().rangeByScore(
                queueKey,
                request.getRating() - range,
                request.getRating() + range
        );
        if (candidates == null) return null;

        return candidates.stream()
                .filter(o -> o instanceof MatchRequestDTO)
                .map(o -> (MatchRequestDTO) o)
                .filter(c -> !c.getUserId().equals(request.getUserId()))
                .findFirst()
                .orElse(null);
    }

    private int calculateCurrentRatingRange(long elapsedMs) {
        if (elapsedMs < 30_000) return INITIAL_RATING_RANGE;
        if (elapsedMs < 60_000) return INITIAL_RATING_RANGE + RATING_EXPANSION_STEP;
        return INITIAL_RATING_RANGE + 2 * RATING_EXPANSION_STEP;
    }

    private void processMatchCandidate(MatchRequestDTO request, long now) {
        String type = request.getTournamentType().toLowerCase();
        String queueKey = buildQueueKey(request.getTimeControl(), type);
        MatchRequestDTO opponent = findSuitableOpponent(request, now);

        if (opponent != null) {
            redisTemplate.opsForZSet().remove(queueKey, request);
            redisTemplate.opsForZSet().remove(queueKey, opponent);
            System.out.println("[ProcessMatchCandidate] Found opponent for user: " + request.getUserId() +
                    " - Opponent: " + opponent.getUserId());
            createPendingMatch(request, opponent);
        } else if (now - request.getRequestTime() >= MATCH_TIMEOUT_MS) {
            System.out.println("[ProcessMatchCandidate] Timeout for user: " + request.getUserId() +
                    " - Time in queue: " + (now - request.getRequestTime()) + "ms");
            handleTimeoutFallback(request);
        }
    }

    private void handleTimeoutFallback(MatchRequestDTO request) {
        String type = request.getTournamentType().toLowerCase();
        String queueKey = buildQueueKey(request.getTimeControl(), type);
        Set<Object> candidates = redisTemplate.opsForZSet().range(queueKey, 0, 1);
        if (candidates == null || candidates.isEmpty()) {
            redisTemplate.opsForZSet().remove(queueKey, request);
            return;
        }
        candidates.stream()
                .filter(o -> o instanceof MatchRequestDTO)
                .map(o -> (MatchRequestDTO) o)
                .filter(o -> !o.getUserId().equals(request.getUserId()))
                .findFirst()
                .ifPresentOrElse(
                        opp -> {
                            redisTemplate.opsForZSet().remove(queueKey, request);
                            redisTemplate.opsForZSet().remove(queueKey, opp);
                            createPendingMatch(request, opp);
                        },
                        () -> redisTemplate.opsForZSet().remove(queueKey, request)
                );
    }

    private void createPendingMatch(MatchRequestDTO p1, MatchRequestDTO p2) {
        String pendingId = UUID.randomUUID().toString();
        PendingMatch pm = new PendingMatch();
        pm.setPendingMatchId(pendingId);
        pm.setPlayer1Id(p1.getUserId());
        pm.setPlayer2Id(p2.getUserId());
        pm.setPlayer1Confirmed(false);
        pm.setPlayer2Confirmed(false);
        pm.setTournamentType(p1.getTournamentType());
        pm.setTimeControl(p1.getTimeControl());
        pm.setCreatedAt(System.currentTimeMillis());
        pm.setStatus(PendingStatus.WAITING_CONFIRMATION);

        redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingId, pm);

        notifyMatchFound(pm);
        scheduleAutoCancel(pendingId);
    }

    /**
     * Auto-cancel pending match after 15s if still unconfirmed. Does not block other operations.
     */
    private void scheduleAutoCancel(String pendingMatchId) {
        scheduler.schedule(() -> {
            Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
            if (!(obj instanceof PendingMatch pm) || pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) {
                return;
            }
            // ... mark cancelled ...
            PlayerInfo p1 = loadPlayerInfo(pm.getPlayer1Id());
            PlayerInfo p2 = loadPlayerInfo(pm.getPlayer2Id());

            MatchResponseDTO cancel1 = new MatchResponseDTO(
                    "AUTO_CANCELLED", null,
                    p1.id, p1.name, p1.rating,
                    p2.id, p2.name, p2.rating,
                    System.currentTimeMillis(),
                    pendingMatchId
            );
            MatchResponseDTO cancel2 = new MatchResponseDTO(
                    "AUTO_CANCELLED", null,
                    p2.id, p2.name, p2.rating,
                    p1.id, p1.name, p1.rating,
                    System.currentTimeMillis(),
                    pendingMatchId
            );

            messagingTemplate.convertAndSendToUser(p1.name, "/queue/match-notifications", cancel1);
            messagingTemplate.convertAndSendToUser(p2.name, "/queue/match-notifications", cancel2);
        }, AUTO_CANCEL_MS, TimeUnit.MILLISECONDS);
    }

    private void notifyMatchFound(PendingMatch pm) {

        PlayerInfo p1 = loadPlayerInfo(pm.getPlayer1Id());
        PlayerInfo p2 = loadPlayerInfo(pm.getPlayer2Id());

        MatchResponseDTO msg1 = new MatchResponseDTO(
                "MATCH_FOUND",
                null,
                p1.id, p1.name, p1.rating,
                p2.id, p2.name, p2.rating,
                System.currentTimeMillis(),
                pm.getPendingMatchId()
        );
        MatchResponseDTO msg2 = new MatchResponseDTO(
                "MATCH_FOUND",
                null,
                p2.id, p2.name, p2.rating,
                p1.id, p1.name, p1.rating,
                System.currentTimeMillis(),
                pm.getPendingMatchId()
        );

        messagingTemplate.convertAndSendToUser(p1.name, "/queue/match-notifications", msg1);
        messagingTemplate.convertAndSendToUser(p2.name, "/queue/match-notifications", msg2);


    }

    public void cancelPendingMatch(String pendingMatchId, Long userId) {
        Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
        if (!(obj instanceof PendingMatch)) return;
        PendingMatch pm = (PendingMatch) obj;
        if (pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) return;
        if (!pm.getPlayer1Id().equals(userId) && !pm.getPlayer2Id().equals(userId)) return;

        pm.setStatus(PendingStatus.CANCELLED);
        redisTemplate.delete(PENDING_MATCH_KEY_PREFIX + pendingMatchId);

        PlayerInfo me    = loadPlayerInfo(userId);
        PlayerInfo other = loadPlayerInfo(
                pm.getPlayer1Id().equals(userId)
                        ? pm.getPlayer2Id()
                        : pm.getPlayer1Id()
        );

        MatchResponseDTO cancelMsg = new MatchResponseDTO(
                "MATCH_CANCELLED", null,
                me.id, me.name, me.rating,
                other.id, other.name, other.rating,
                System.currentTimeMillis(),
                pendingMatchId
        );
        messagingTemplate.convertAndSendToUser(other.name, "/queue/match-notifications", cancelMsg);



    }

    public void confirmPendingMatch(String pendingMatchId, Long userId) {
        System.out.println("[confirmPendingMatch] Processing confirmation for match: " + pendingMatchId + " by user: " + userId);
        Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pendingMatchId);
        if (!(obj instanceof PendingMatch)) return;
        PendingMatch pm = (PendingMatch) obj;
        if (pm.getStatus() != PendingStatus.WAITING_CONFIRMATION) return;

        if (pm.getPlayer1Id().equals(userId)) pm.setPlayer1Confirmed(true);
        if (pm.getPlayer2Id().equals(userId)) pm.setPlayer2Confirmed(true);

        if (pm.isPlayer1Confirmed() && pm.isPlayer2Confirmed()) {
            pm.setStatus(PendingStatus.CONFIRMED);
            redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingMatchId, pm);
            notifyMatchConfirmed(pm);
            removeUserFromQueue(pm.getPlayer1Id());
            removeUserFromQueue(pm.getPlayer2Id());
            scheduler.schedule(() -> createDuelTournament(pm), 10, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(PENDING_MATCH_KEY_PREFIX + pendingMatchId, pm);
        }
    }

    private void notifyMatchConfirmed(PendingMatch pm) {
        PlayerInfo p1 = loadPlayerInfo(pm.getPlayer1Id());
        PlayerInfo p2 = loadPlayerInfo(pm.getPlayer2Id());

        MatchResponseDTO ok1 = new MatchResponseDTO(
                "MATCH_OK", null,
                p1.id, p1.name, p1.rating,
                p2.id, p2.name, p2.rating,
                System.currentTimeMillis(),
                pm.getPendingMatchId()
        );
        MatchResponseDTO ok2 = new MatchResponseDTO(
                "MATCH_OK", null,
                p2.id, p2.name, p2.rating,
                p1.id, p1.name, p1.rating,
                System.currentTimeMillis(),
                pm.getPendingMatchId()
        );

        messagingTemplate.convertAndSendToUser(p1.name, "/queue/match-notifications", ok1);
        messagingTemplate.convertAndSendToUser(p2.name, "/queue/match-notifications", ok2);
    }


    private void createDuelTournament(PendingMatch pm) {

        System.out.println("[createDuelTournament] Attempting to create tournament for pending match: " + pm.getPendingMatchId());

        Object obj = redisTemplate.opsForValue().get(PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId());
        if (!(obj instanceof PendingMatch)) return;
        PendingMatch current = (PendingMatch) obj;
        if (current.getStatus() != PendingStatus.CONFIRMED) return;

        DuelTournamentEntity duel = new DuelTournamentEntity();
        duel.setPlayer1(current.getPlayer1Id());
        duel.setPlayer2(current.getPlayer2Id());
        duel.setStartTime(System.currentTimeMillis());
        duel.setRated(true);
        duel.setDurationInSeconds(current.getTimeControl());

        // Map tournament type
        if (current.getTournamentType().equalsIgnoreCase("freestyle")) {
            duel.setTournamentType(DuelTournamentEntity.TournamentType.FREE_STYLE);
        } else {
            duel.setTournamentType(DuelTournamentEntity.TournamentType.CLASSIC);
        }

        DuelTournamentEntity saved = duelRepository.save(duel);
        duelTournamentService.joinTournament(saved.getTournamentId(), saved.getPlayer1());
        duelTournamentService.joinTournament(saved.getTournamentId(), saved.getPlayer2());


        PlayerInfo p1 = loadPlayerInfo(current.getPlayer1Id());
        PlayerInfo p2 = loadPlayerInfo(current.getPlayer2Id());

        MatchResponseDTO response = new MatchResponseDTO(
                "MATCH_CREATED",
                saved.getTournamentId(),
                p1.id, p1.name, p1.rating,
                p2.id, p2.name, p2.rating,
                duel.getStartTime(),
                pm.getPendingMatchId()
        );


        TournamentCacheDTO cache = new TournamentCacheDTO();
        cache.setTournamentId(saved.getTournamentId());
        cache.setStartTime(saved.getStartTime());
        cache.setDurationInSeconds(saved.getDurationInSeconds());
        cache.setTournamentType(saved.getTournamentType());
        cache.setScheduled(false);
        cache.setPenaltyFactor(saved.getPenaltyFactor());

        logger.info("Starting tournament {} at {}", saved.getTournamentId(), LocalDateTime.now());
        redisTemplate.opsForValue().set("$" + saved.getTournamentId(), cache, cache.getDurationInSeconds(), TimeUnit.SECONDS);
        producer.startTournamentInit(cache);

        String user1 = userRepository.findById(current.getPlayer1Id()).orElseThrow().getUserName();
        String user2 = userRepository.findById(current.getPlayer2Id()).orElseThrow().getUserName();
        messagingTemplate.convertAndSendToUser(user1, "/queue/match-notifications", response);
        messagingTemplate.convertAndSendToUser(user2, "/queue/match-notifications", response);
        redisTemplate.delete(PENDING_MATCH_KEY_PREFIX + pm.getPendingMatchId());
    }

    private String buildQueueKey(Long timeControl, String tournamentType) {
        return MATCHMAKING_QUEUE_KEY + ":" + timeControl + ":" + tournamentType;
    }
}
