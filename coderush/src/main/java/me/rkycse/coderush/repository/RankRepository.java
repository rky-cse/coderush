package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.RankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankRepository extends JpaRepository<RankEntity, Long> {
}
