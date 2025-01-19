package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.RankListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankListRepository extends JpaRepository<RankListEntity, Long> {
}
