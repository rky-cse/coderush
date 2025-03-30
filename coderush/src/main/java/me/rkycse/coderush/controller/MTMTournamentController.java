package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.dto.MTMTournamentDTO;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import me.rkycse.coderush.service.MTMTournamentService;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tournament/mtm")
public class MTMTournamentController {


    private final MTMTournamentService mtmTournamentService;

    public MTMTournamentController(MTMTournamentService mtmTournamentService) {
        this.mtmTournamentService = mtmTournamentService;
    }


    @PostMapping("/createMTMTournament")
    public MTMTournamentEntity createMTMTournament(@RequestBody MTMTournamentEntity tournament) {
        System.out.println("Creating MTM tournament: " + tournament);
        return mtmTournamentService.createMTMTournament(tournament);
    }

    @GetMapping("/joinTournament/{tournamentId}")
    public ResponseEntity<JoinTournamentResponseDTO> joinTournament(@PathVariable Long tournamentId) {
        System.out.println("Joining tournament: " + tournamentId);
        JoinTournamentResponseDTO joinMTMTournamentResponseDTO =
                mtmTournamentService.joinTournament(tournamentId);
        if (joinMTMTournamentResponseDTO != null) {
            return ResponseEntity.ok(joinMTMTournamentResponseDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/startTournament/{tournamentId}")
    public ResponseEntity<String> startTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(mtmTournamentService.startTournament(tournamentId));
    }

    @GetMapping("/getLiveMTMTournaments")
    public ResponseEntity<List<MTMTournamentDTO>> getLiveMTMTournaments() {
        return ResponseEntity.ok(mtmTournamentService.getLiveMTMTournaments());
    }
    @GetMapping("/getUpcomingMTMTournaments")
    public ResponseEntity<List<MTMTournamentDTO>> getUpcomingMTMTournaments() {
        return ResponseEntity.ok(mtmTournamentService.getUpcomingMTMTournaments());
    }
    @GetMapping("/getPastMTMTournaments")
    public ResponseEntity<Page<MTMTournamentDTO>> getPastTournaments(
            @PageableDefault(
                    size = 10,
                    sort = "startTime",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(mtmTournamentService.findPastTournaments(pageable));
    }
    @GetMapping("/registeredTournamentsByUser")
    public ResponseEntity<List<Long>> getRegisteredTournamentsByUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();
        return ResponseEntity.ok(mtmTournamentService.getRegisteredMTMTournamentsByUserName(username));

    }
    @GetMapping("/getTournamentById/{id}")

    public ResponseEntity<MTMTournamentDTO> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok(mtmTournamentService.getMTMTournamentById(id));
    }


}
