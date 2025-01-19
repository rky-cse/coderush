package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.UserTestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTestcaseRepository extends JpaRepository<UserTestcaseEntity, Long> {

}
