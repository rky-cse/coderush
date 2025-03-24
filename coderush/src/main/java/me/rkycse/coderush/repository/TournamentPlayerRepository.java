package me.rkycse.coderush.repository;


import io.lettuce.core.dynamic.annotation.Param;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayerEntity, Long> {
    TournamentPlayerEntity findByTournamentIdAndPlayerUserName(Long tournamentId, String playerUserName);
    List<TournamentPlayerEntity> findByTournamentId(Long tournamentId);
    @Query("SELECT tr.tournamentId FROM TournamentPlayerEntity tr WHERE tr.playerUserName = :playerUserName")
    List<Long> tournamentIdsByPlayerUserName(@Param("playerUserName") String playerUserName);

}
