package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TournamentEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayerEntity, Long> {
    TournamentPlayerEntity findByTournamentIdAndPlayerUserName(Long tournamentId, String playerUserName);
}
