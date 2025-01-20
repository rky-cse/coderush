package me.rkycse.coderush.controller;

import me.rkycse.coderush.entity.TournamentEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/tournament")
public class TournamentController {

    @Autowired
    TournamentService tournamentService;

    @GetMapping("/getTournamentById/{tournamentId}")
    public TournamentEntity getTournamentById(@PathVariable Long tournamentId){
        return tournamentService.getTournamentById(tournamentId);
    }

    @PostMapping("/createTournament")

    public TournamentEntity createTournament(@RequestBody TournamentEntity tournament){
        System.out.println("Creating tournament : " + tournament);
        return tournamentService.createTournament(tournament);
    }

    @GetMapping("joinTournament/{tournamentId}")
    public ResponseEntity<TournamentPlayerEntity> joinTournament(@PathVariable Long tournamentId){
        TournamentPlayerEntity tournamentPlayer = tournamentService.joinTournament(tournamentId);
        if(tournamentPlayer!=null){
            return ResponseEntity.ok(tournamentPlayer);
        }
        return ResponseEntity.notFound().build();
    }



}

