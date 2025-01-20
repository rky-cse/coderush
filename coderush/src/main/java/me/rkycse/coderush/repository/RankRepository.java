package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.RankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankRepository extends JpaRepository<RankEntity, Long> {
    RankEntity findByUserNameAndTournamentId(String userName,Long tournamentId);
    List<RankEntity> findByTournamentId(Long tournamentId);
}
