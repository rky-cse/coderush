package me.rkycse.coderush.controller;

import me.rkycse.coderush.entity.TournamentBaseEntity;
import me.rkycse.coderush.service.DuelTournamentService;
import me.rkycse.coderush.service.MTMTournamentService;
import me.rkycse.coderush.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tournament")
public class TournamentController {


    private final TournamentService tournamentService;
    private final MTMTournamentService mtmTournamentService;
    private final DuelTournamentService duelTournamentService;

    public TournamentController(TournamentService tournamentService, MTMTournamentService mtmTournamentService, DuelTournamentService duelTournamentService) {
        this.tournamentService = tournamentService;
        this.mtmTournamentService = mtmTournamentService;
        this.duelTournamentService = duelTournamentService;
    }

    @GetMapping("/getTournamentById/{tournamentId}")
    public TournamentBaseEntity getTournamentById(@PathVariable Long tournamentId){
        return tournamentService.getTournamentById(tournamentId);
    }

}
