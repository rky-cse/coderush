package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TournamentBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TournamentBaseRepository extends JpaRepository<TournamentBaseEntity, Long> {
    @Query("SELECT t.tournamentId, t.startTime, t.durationInSeconds " +
            "FROM TournamentBaseEntity t " +
            "WHERE t.startTime >= :now AND t.startTime <= :upperLimit")
    List<Object[]> findTournamentsBetween(@Param("now") Long now,
                                          @Param("upperLimit") Long upperLimit);
}

