package me.rkycse.coderush.repository;

import io.lettuce.core.dynamic.annotation.Param;
import me.rkycse.coderush.entity.RankEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // method to enable sorting by startTime
    @Query("SELECT r.tournamentId FROM RankEntity r WHERE r.userName = :userName AND r.penalty > 0")
    Page<Long> findTournamentIdsWithPenaltyByUserName(@Param("userName") String userName, Pageable pageable);
    
    // Keep the existing method for non-paginated use
    @Query("SELECT r.tournamentId FROM RankEntity r WHERE r.userName = :userName AND r.penalty > 0")
    List<Long> findTournamentIdsWithPenaltyByUserName(@Param("userName") String userName);
    
    RankEntity findByTournamentIdAndUserName(Long tournamentId, String userName);
}
