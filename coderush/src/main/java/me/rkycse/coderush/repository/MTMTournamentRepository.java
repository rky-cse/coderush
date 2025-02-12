package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.MTMTournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
public interface MTMTournamentRepository extends JpaRepository<MTMTournamentEntity, Long> {
    MTMTournamentEntity findByTournamentId(long tournamentId);


}



