package me.rkycse.coderush.repository;

import io.lettuce.core.dynamic.annotation.Param;
import me.rkycse.coderush.entity.RankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankRepository extends JpaRepository<RankEntity, Long> {
    @Query("SELECT r FROM RankEntity r WHERE r.userName = :userName AND r.tournamentId = :tournamentId")
    RankEntity findByUserNameAndTournamentId(@Param("userName") String userName,
                                             @Param("tournamentId") Long tournamentId);
    List<RankEntity> findByTournamentId(Long tournamentId);
}
