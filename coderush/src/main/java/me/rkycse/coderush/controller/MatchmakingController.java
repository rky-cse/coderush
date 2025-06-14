package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.CancelMatchDTO;
import me.rkycse.coderush.dto.ConfirmMatchDTO;
import me.rkycse.coderush.dto.MatchRequestDTO;
import me.rkycse.coderush.dto.MatchResponseDTO;
import me.rkycse.coderush.service.MTMTournamentSchedulerService;
import me.rkycse.coderush.service.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;



@Controller
@RestController
// @RestController would work too, but typically for STOMP endpoints we use @Controller.
// We can still define REST endpoints with @ResponseBody or @RestController if you prefer
public class MatchmakingController {

    private static final Logger logger = LoggerFactory.getLogger(MatchmakingController.class);
    private final MatchmakingService matchmakingService;

    @Autowired
    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    /**
     * 1) REST endpoint for requesting a match
     * The client does a POST to /api/match/request with JSON body: { userId, rating, ... }
     * Returns a MatchResponseDTO (e.g. status = QUEUED).
     */
    @PostMapping("/api/match/request")
    @ResponseBody
    public MatchResponseDTO requestMatch(@RequestBody MatchRequestDTO payload) {
        // The service processes the match request, puts the user in the Redis queue
        payload.setTimeControl((60* payload.getTimeControl())); // Convert minutes to seconds
        System.out.println("Received match request:" + payload.getUserId() +  " rating: {}" + payload.getRating());
        return matchmakingService.processMatchRequest(payload);
    }

    /**
     * 2) WebSocket endpoint: user confirms a pending match
     * The client sends a STOMP message to /app/match/confirm
     * Used when user clicks on Decline Pairing on frontend
     * with a ConfirmMatchDTO that has pendingMatchId + userId
     */
    @MessageMapping("/match/confirm")
    public void confirmMatch(@Payload ConfirmMatchDTO payload) {
        matchmakingService.confirmPendingMatch(payload.getPendingMatchId(), payload.getUserId());
    }

    /**
     * 3) WebSocket endpoint: user is getting remove from the waiting queue ( Before even getting matched )
     * Used when user clicks on Abandon Search on the frontend
     * The client sends a STOMP message to /app/match/remove
     * with a userId
     */
    @MessageMapping("/match/remove")
    public void cancelMatch(@Payload Long userId) {
        matchmakingService.removeUserFromQueue(userId);
    }


    /**
     * 4) WebSocket endpoint: user cancels a pending match
     * The client sends a STOMP message to /app/match/cancels
     * with a CancelMatchDTO that has pendingMatchId + userId
     */
    @MessageMapping("/match/cancel")
    public void cancelMatch(@Payload CancelMatchDTO payload) {
        matchmakingService.cancelPendingMatch(payload.getPendingMatchId(), payload.getUserId());
    }


    /**
     * 5) REST endpoint for removing user from the queue.
     * Use this endpoint for actions like a page refresh.
     */
    @PostMapping("/api/match/remove")
    @ResponseBody
    public String removeMatchViaRest(@RequestBody Long userId) {
        System.out.println("Received match removal request via REST for user: " + userId);
        matchmakingService.removeUserFromQueue(userId);
        return "User removed from queue";
    }

    /**
     * 6) REST endpoint for cancelling a pending match.
     * Use this endpoint for actions like a page refresh.
     */
    @PostMapping("/api/match/cancel")
    @ResponseBody
    public String cancelMatchViaRest(@RequestBody CancelMatchDTO payload) {
        System.out.println("Received match cancellation request via REST for pendingMatchId: "+ payload.getPendingMatchId() + "by user: " +
                 payload.getUserId());
        matchmakingService.cancelPendingMatch(payload.getPendingMatchId(), payload.getUserId());
        return "Cancellation processed";
    }
}
