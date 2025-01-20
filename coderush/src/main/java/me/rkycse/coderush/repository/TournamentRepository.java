package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TournamentRepository extends JpaRepository<TournamentEntity, Long> {

    @Query("SELECT t.tournamentId, t.startTime FROM TournamentEntity t " +
            "WHERE t.startTime >= :now AND t.startTime <= :upperLimit")
    List<Object[]> findTournamentsBetween(@Param("now") LocalDateTime now,
                                          @Param("upperLimit") LocalDateTime upperLimit);
}
