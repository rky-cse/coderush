package me.rkycse.coderush.service;

import me.rkycse.coderush.entity.TournamentEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.repository.TournamentPlayerRepository;
import me.rkycse.coderush.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TournamentService {

    //HashMap<Long,TournamentEntity> tournaments=new HashMap<>();

    @Autowired
    TournamentRepository tournamentRepository;
    @Autowired
    TournamentPlayerRepository tournamentPlayerRepository;


    public TournamentEntity getTournamentById(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null or empty");
        }

        // Search for the tournament in the list
        if(tournamentRepository.existsById(tournamentId)) {

            System.out.println("fetched tournament using "+tournamentId);
            return tournamentRepository.findById(tournamentId).orElse(null);

        }
        throw new NoSuchElementException("Tournament with ID " + tournamentId + " not found");
    }
    public TournamentEntity createTournament(TournamentEntity tournament) {


        if (tournament == null) {
            throw new IllegalArgumentException("Tournament object cannot be null");
        }

        // Assign a unique tournament ID
        //tournament.setTournamentId(UUID.randomUUID().toString());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String creatorUserName = userDetails.getUsername();
        tournament.setCreatorUserName(creatorUserName);


        if (tournament.getStartTime() == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        if (tournament.getDurationInSeconds() <= 0) {
            System.out.println("duration is: "+tournament.getDurationInSeconds());
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        TournamentEntity savedTournament=  tournamentRepository.save(tournament);
        System.out.println("Tournament created successfully: " + tournament);

        return savedTournament;
    }

    public TournamentPlayerEntity joinTournament(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        if(tournamentRepository.existsById(tournamentId)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            if(tournamentPlayerRepository.findByTournamentIdAndPlayerUserName(
                    tournamentId, userName) == null) {
                TournamentPlayerEntity tournamentPlayer=new TournamentPlayerEntity();
                tournamentPlayer.setTournamentId(tournamentId);
                tournamentPlayer.setPlayerUserName(userName);
                return tournamentPlayerRepository.save(tournamentPlayer);

            }

        }
        else{
            throw  new NoSuchElementException("No such tournament");
        }
        return null;

    }



}

