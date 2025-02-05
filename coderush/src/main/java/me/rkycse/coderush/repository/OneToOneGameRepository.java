package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.OneToOneGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneToOneGameRepository extends JpaRepository<OneToOneGameEntity, Long> {
}