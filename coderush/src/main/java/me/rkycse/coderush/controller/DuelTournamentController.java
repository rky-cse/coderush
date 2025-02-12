package me.rkycse.coderush.controller;

import me.rkycse.coderush.entity.DuelTournamentEntity;
import me.rkycse.coderush.service.DuelTournamentService;
import me.rkycse.coderush.service.MTMTournamentService;
import me.rkycse.coderush.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tournament/duel")
public class DuelTournamentController {

    @Autowired
    TournamentService tournamentService;
    MTMTournamentService mtmTournamentService;
    DuelTournamentService duelTournamentService;


    @PostMapping("/createDuelTournament")
    public DuelTournamentEntity createDuelTournament(@RequestBody DuelTournamentEntity tournament){
        System.out.println("Creating Duel tournament: " + tournament);
        return duelTournamentService.createDuelTournament(tournament);
    }

}
