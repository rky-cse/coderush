package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import me.rkycse.coderush.service.MTMTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
