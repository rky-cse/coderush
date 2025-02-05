package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.OneToOneMatchRequestDTO;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.service.OneToOneMatchService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.time.Instant;

@Controller
public class OneToOneGameWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(OneToOneGameWebSocketController.class);

    private final OneToOneMatchService matchService;
    private final UserRepository userRepository;

    public OneToOneGameWebSocketController(OneToOneMatchService matchService, UserRepository userRepository) {
        this.matchService = matchService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/oneToOneGame/requestPairing")
    public void requestPairing(OneToOneMatchRequestDTO oneToOneMatchRequestDTO, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No principal found in security context");
        }

        if (!(principal instanceof UsernamePasswordAuthenticationToken authenticationToken)) {
            throw new IllegalStateException("Invalid principal type");
        }

        UserDetails userDetails = (UserDetails) authenticationToken.getPrincipal();
        String userName = userDetails.getUsername();

        if (oneToOneMatchRequestDTO == null) {
            throw new IllegalStateException("No oneToOneMatchRequestDTO found");
        }
        long timestamp = Instant.now().toEpochMilli();
        oneToOneMatchRequestDTO.setTimestamp(timestamp);

        oneToOneMatchRequestDTO.setUserName(userName);
        oneToOneMatchRequestDTO.setRating(userRepository.getRatingByUserName(userName));

        logger.info("requestPairing {}", oneToOneMatchRequestDTO);
        matchService.addToQueue(oneToOneMatchRequestDTO);
    }

    @MessageMapping("/oneToOneGame/startGame")
    public void startOneToOneGame(Long tournamentId,Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No principal found in security context");
        }

        if (!(principal instanceof UsernamePasswordAuthenticationToken authenticationToken)) {
            throw new IllegalStateException("Invalid principal type");
        }

        UserDetails userDetails = (UserDetails) authenticationToken.getPrincipal();
        String userName = userDetails.getUsername();
        matchService.setPlayerACK(userName);
        matchService.startGame(tournamentId);

    }
}
