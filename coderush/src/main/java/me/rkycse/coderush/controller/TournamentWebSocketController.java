package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.service.TournamentWebSocketService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@Controller
public class TournamentWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final TournamentWebSocketService tournamentWebSocketService;
    private final RedisTemplate<String,Object>redisTemplate;

    public TournamentWebSocketController(SimpMessagingTemplate messagingTemplate, TournamentWebSocketService tournamentWebSocketService, RedisTemplate<String, Object> redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.tournamentWebSocketService = tournamentWebSocketService;
        this.redisTemplate = redisTemplate;
    }


    @MessageMapping("/tournament/getQuestionWithTestcase")
    public void getQuestionWithTestcase(@RequestBody String payload, Principal principal) {
        try {
            // Trim and clean the payload to remove any unexpected characters
            payload = payload.trim().replace("\"", ""); // Remove quotes if present

            // Split the payload into parts
            String[] parts = payload.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid payload format. Expected format: tournamentId/index");
            }

            // Parse tournamentId and index
            Long tournamentId = Long.parseLong(parts[0]);
            int index = Integer.parseInt(parts[1]);

            // Get user details from the principal
            UserDetails userDetails = (UserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            String userName = userDetails.getUsername();

            // Fetch question and testcase
            QuestionDTO question = tournamentWebSocketService.getQuestion(tournamentId, index);
            TestcaseDTO testcaseDTO = tournamentWebSocketService.getTestcase(tournamentId, userName, index);

            // Create and send the response DTO
            QuestionWithTestcaseDTO questionWithTestcaseDTO = new QuestionWithTestcaseDTO();
            questionWithTestcaseDTO.setTestcase(testcaseDTO);
            questionWithTestcaseDTO.setQuestion(question);

            messagingTemplate.convertAndSend("/topic/tournament/getQuestionWithTestcase/" + tournamentId + "/" + index, questionWithTestcaseDTO);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in payload: " + payload, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process request: " + e.getMessage(), e);
        }
    }

    @MessageMapping("/tournament/freeStyleSubmit")
    public void submit(@RequestBody UserResponseDTO responseDTO, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No principal found in security context");
        }
        UserDetails userDetails = (UserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        String userName = userDetails.getUsername();
        int index = responseDTO.getIndex();
        System.out.println("received response from user "+responseDTO);

        Boolean res=tournamentWebSocketService.isCorrect(userName,index,responseDTO);
        messagingTemplate.convertAndSend("/topic/tournament/freeStyleSubmit/" + userName+"/" + index, res);
    }
    @MessageMapping("/tournament/classicSubmit")
    public void submit(@RequestBody ClassicSubmissionDTO classicSubmissionDTO, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No principal found in security context");
        }
        UserDetails userDetails = (UserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        String userName = userDetails.getUsername();
        int index = classicSubmissionDTO.getIndex();
        System.out.println("received response from user "+classicSubmissionDTO);

        Boolean res=null;// to complete
        messagingTemplate.convertAndSend("/topic/tournament/classicSubmit/" + userName+"/" + index, res);
    }
}