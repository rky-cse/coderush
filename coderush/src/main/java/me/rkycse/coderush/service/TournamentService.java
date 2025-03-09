package me.rkycse.coderush.service;
import me.rkycse.coderush.entity.TournamentBaseEntity;
import me.rkycse.coderush.repository.TournamentBaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentService {

    private final TournamentBaseRepository tournamentBaseRepository;

    public TournamentService(TournamentBaseRepository tournamentBaseRepository) {
        this.tournamentBaseRepository = tournamentBaseRepository;
    }

    public TournamentBaseEntity getTournamentById(Long tournamentId) {
        return tournamentBaseRepository.findById(tournamentId)
                .orElse(null);
    }


}
