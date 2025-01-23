package me.rkycse.coderush.repository;


import me.rkycse.coderush.entity.TournamentPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayerEntity, Long> {
    TournamentPlayerEntity findByTournamentIdAndPlayerUserName(Long tournamentId, String playerUserName);
    List<TournamentPlayerEntity> findByTournamentId(Long tournamentId);
}
