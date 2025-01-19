package me.rkycse.coderush.controller;

import me.rkycse.coderush.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin("*")
@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    TournamentService tournamentService;

    // Endpoint to receive messages from clients
    @MessageMapping("/send-message") // Clients send messages to /app/send-message
    @SendTo("/topic/messages")       // Broadcast to all subscribers of /topic/messages
    public String sendMessage(String message) {
        System.out.println(message);
        return message;
    }

    // Method to send "hi" to all clients every 5 seconds
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 5000)
    public void sendPeriodicHi() {
        messagingTemplate.convertAndSend("/topic/messages", "hi");
    }


    @Scheduled(fixedRate = 7000)
    public void sendPeriodicRankList() {
        Long tournamentId = 1L; // Replace with your actual logic
        messagingTemplate.convertAndSend(
                "/topic/" + tournamentId,
                tournamentService.getTournamentById(tournamentId));
    }
}
