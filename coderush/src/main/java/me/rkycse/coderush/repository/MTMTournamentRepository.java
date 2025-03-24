package me.rkycse.coderush.repository;

import io.lettuce.core.dynamic.annotation.Param;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MTMTournamentRepository extends JpaRepository<MTMTournamentEntity, Long> {
    MTMTournamentEntity findByTournamentId(long tournamentId);
    @Query("SELECT t FROM MTMTournamentEntity t " +
            "WHERE t.startTime <= :now " +
            "AND (t.startTime + (t.durationInSeconds * 1000)) >= :now")
    List<MTMTournamentEntity> findLiveMTMTournaments(@Param("now") Long now);

    @Query("SELECT t FROM MTMTournamentEntity t " +
            "WHERE t.startTime > :now " +
            "ORDER BY t.startTime ASC")
    List<MTMTournamentEntity> findUpcomingMTMTournaments(@Param("now") Long now);




}



