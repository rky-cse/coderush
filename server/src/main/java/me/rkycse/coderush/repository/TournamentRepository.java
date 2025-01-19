package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<TournamentEntity,Long> {

}
