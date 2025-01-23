package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.entity.TournamentEntity;

import me.rkycse.coderush.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



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
    public ResponseEntity<JoinTournamentResponseDTO> joinTournament(@PathVariable Long tournamentId){
        System.out.println("Joining tournament : " + tournamentId);
        JoinTournamentResponseDTO   joinTournamentResponseDTO = tournamentService.joinTournament(tournamentId);
        if(joinTournamentResponseDTO!=null){
            return ResponseEntity.ok(joinTournamentResponseDTO);
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/startTournament/{tournamentId}")

    public ResponseEntity<String> startTournament(@PathVariable Long tournamentId){
        return ResponseEntity.ok(tournamentService.startTournament(tournamentId));
    }



}

