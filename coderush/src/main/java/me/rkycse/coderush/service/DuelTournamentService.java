package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.DuelTournamentDTO;
import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.dto.MTMTournamentDTO;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.DuelTournamentRepository;
import me.rkycse.coderush.repository.RankRepository;
import me.rkycse.coderush.repository.TournamentPlayerRepository;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class DuelTournamentService {

    private final DuelTournamentRepository duelTournamentRepository;
    private final UserRepository userRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final RankRepository rankRepository;


    public DuelTournamentService(DuelTournamentRepository duelTournamentRepository, UserRepository userRepository, TournamentPlayerRepository tournamentPlayerRepository, RankRepository rankRepository) {
        this.duelTournamentRepository = duelTournamentRepository;
        this.userRepository = userRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.rankRepository = rankRepository;
    }

    @Transactional
    public DuelTournamentEntity createDuelTournament(DuelTournamentEntity tournament) {
        return duelTournamentRepository.save(tournament);
    }


    @Transactional(readOnly = true)
    public List<DuelTournamentEntity> getPlayerDuels(Long playerId) {
        return duelTournamentRepository.findDuelsByPlayerId(playerId);
    }

    @Transactional(readOnly = true)
    public List<DuelTournamentEntity> getDuelsBetweenPlayers(Long player1Id, Long player2Id) {
        return duelTournamentRepository.findDuelsBetweenPlayers(player1Id, player2Id);
    }

    public JoinTournamentResponseDTO joinTournament(Long tournamentId, Long playerId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }

        if (!duelTournamentRepository.existsById(tournamentId)) {
            throw new NoSuchElementException("No such tournament");
        }

        // Retrieve the current user


        UserEntity user = userRepository.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        String userName = user.getUserName();

        // Retrieve tournament details
        DuelTournamentEntity tournament = duelTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NoSuchElementException("Tournament not found"));

        long currentTime = TimeUtil.getCurrentEpochMillis();
        long startTime = tournament.getStartTime();
        long endTime = tournament.getDurationInSeconds() * 1000L + startTime;

        // Prevent joining if the tournament has already ended
        if (currentTime >= endTime) {
            throw new IllegalStateException("Tournament has already ended");
        }

        // Check if the user is already registered for this tournament (unique combination of tournamentId and username)
        TournamentPlayerEntity tournamentPlayer = tournamentPlayerRepository
                .findByTournamentIdAndPlayerUserName(tournamentId, userName);

        // Only save if the player doesn't exist already
        if (tournamentPlayer == null) {
            tournamentPlayer = new TournamentPlayerEntity();
            tournamentPlayer.setTournamentId(tournamentId);
            tournamentPlayer.setPlayerUserName(userName);
            tournamentPlayer.setRating(user.getRating());
            tournamentPlayerRepository.save(tournamentPlayer);
        }


        // Return the join tournament response
        return getJoinTournamentResponseDTO(tournamentId, userName);
    }


    private JoinTournamentResponseDTO getJoinTournamentResponseDTO(Long tournamentId, String userName) {
        List<TournamentPlayerEntity> tournamentPlayerEntities =
                tournamentPlayerRepository
                        .findByTournamentId(tournamentId);

        RankEntity rankEntity = new RankEntity();
        rankEntity.setUserName(userName);
        rankEntity.setTournamentId(tournamentId);
        rankRepository.save(rankEntity);
        JoinTournamentResponseDTO joinTournamentResponseDTO = new JoinTournamentResponseDTO();
        for (TournamentPlayerEntity tournamentPlayerEntity : tournamentPlayerEntities) {
            joinTournamentResponseDTO.setTournamentId(tournamentId);
            joinTournamentResponseDTO.getTournamentPlayerList().add(Mapper.toDTO(tournamentPlayerEntity));
        }

        joinTournamentResponseDTO.setTournament(Mapper.toDTO(duelTournamentRepository
                .findById(tournamentId).orElse(null)));

        return joinTournamentResponseDTO;
    }

    public DuelTournamentDTO getDuelTournamentById(Long tournamentId) {
        DuelTournamentEntity entity = duelTournamentRepository.findByTournamentId(tournamentId);
        if (entity == null) {
            throw new me.rkycse.coderush.exception.TournamentNotFoundException(tournamentId);
        }
        return Mapper.toDTO(entity);
    }

}