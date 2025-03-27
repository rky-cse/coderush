package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.DuelTournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DuelTournamentRepository extends JpaRepository<DuelTournamentEntity, Long> {

    @Query("SELECT d FROM DuelTournamentEntity d WHERE d.player1 = :playerId OR d.player2 = :playerId")
    List<DuelTournamentEntity> findDuelsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT d FROM DuelTournamentEntity d WHERE d.player1 = :player1 AND d.player2 = :player2")
    List<DuelTournamentEntity> findDuelsBetweenPlayers(@Param("player1") Long player1,
                                                       @Param("player2") Long player2);
}