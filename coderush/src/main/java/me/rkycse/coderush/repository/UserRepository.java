package me.rkycse.coderush.repository;

import io.lettuce.core.dynamic.annotation.Param;
import me.rkycse.coderush.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUserName(String username);
    @Query("SELECT t.rating FROM UserEntity t " +
            "WHERE t.userName= :userName")
    Long getRatingByUserName(@Param("username") String userName);

}
