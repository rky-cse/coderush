package me.rkycse.coderush.service;

import me.rkycse.coderush.entity.DuelTournamentEntity;
import me.rkycse.coderush.repository.DuelTournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DuelTournamentService {

    private final DuelTournamentRepository duelTournamentRepository;

    @Autowired
    public DuelTournamentService(DuelTournamentRepository duelTournamentRepository) {
        this.duelTournamentRepository = duelTournamentRepository;
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
}