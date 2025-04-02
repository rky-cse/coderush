package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.DuelTournamentDTO;
import me.rkycse.coderush.entity.DuelTournamentEntity;
import me.rkycse.coderush.service.DuelTournamentService;
import me.rkycse.coderush.service.MTMTournamentService;
import me.rkycse.coderush.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tournament/duel")
public class DuelTournamentController {


    private final TournamentService tournamentService;
    private final MTMTournamentService mtmTournamentService;
    private final DuelTournamentService duelTournamentService;

    public DuelTournamentController(TournamentService tournamentService, MTMTournamentService mtmTournamentService, DuelTournamentService duelTournamentService) {
        this.tournamentService = tournamentService;
        this.mtmTournamentService = mtmTournamentService;
        this.duelTournamentService = duelTournamentService;
    }


    @PostMapping("/createDuelTournament")
    public DuelTournamentEntity createDuelTournament(@RequestBody DuelTournamentEntity tournament){
        System.out.println("Creating Duel tournament: " + tournament);
        return duelTournamentService.createDuelTournament(tournament);
    }

    @GetMapping("/getTournamentById/{id}")

    public ResponseEntity<DuelTournamentDTO> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok(duelTournamentService.getDuelTournamentById(id));
    }


}
