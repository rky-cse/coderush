package me.rkycse.coderush.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import me.rkycse.coderush.entity.RecentActivityEntity;

public interface RecentActivityRepository extends JpaRepository<RecentActivityEntity, Long> {
    Optional<RecentActivityEntity> findByUsername(String username);
}
