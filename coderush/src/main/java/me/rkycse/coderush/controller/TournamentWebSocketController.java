package me.rkycse.coderush.controller;


import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.dto.UserResponseDTO;
import me.rkycse.coderush.service.TournamentWebSocketService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller

public class TournamentWebSocketController {

   private final SimpMessagingTemplate messagingTemplate;
   private final TournamentWebSocketService tournamentWebSocketService;

    public TournamentWebSocketController(SimpMessagingTemplate messagingTemplate, TournamentWebSocketService tournamentWebSocketService) {
        this.messagingTemplate = messagingTemplate;
        this.tournamentWebSocketService = tournamentWebSocketService;
    }

    @MessageMapping("/tournament/getQuestion/{tournamentId}/{index}")
    public void getQuestion(@PathVariable Long tournamentId, int index) {
        QuestionDTO question= tournamentWebSocketService.getQuestion(tournamentId,index);
                messagingTemplate.convertAndSend("/topic/tournament/getQuestion/"
                        +tournamentId+"/"+index,question);
    }
    @MessageMapping("/tournament/getTestcase/{questionId}")
    public void getTestcase(@PathVariable Long questionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        TestcaseDTO testcase= tournamentWebSocketService.getTestcase(questionId,userName);
        messagingTemplate.convertAndSend("/topic/tournament/getTestcase/"
                +questionId+"/"+userName,testcase);
    }

    @MessageMapping("/tournament/submit")
    public void submit(@RequestBody UserResponseDTO responseDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();
        Long testcaseId = responseDTO.getTestcaseId();
        messagingTemplate.convertAndSend("topic/tournament/submit/"+testcaseId+"/"+userName,responseDTO);

    }
}
