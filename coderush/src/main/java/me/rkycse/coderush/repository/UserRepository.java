package me.rkycse.coderush.repository;

import io.lettuce.core.dynamic.annotation.Param;
import me.rkycse.coderush.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUserName(String username);
    Optional<UserEntity> findByEmail(String email);
    @Query("SELECT u.id FROM UserEntity u WHERE u.userName = :username")
    Optional<Long> findIdByUserName(@Param("username") String username);

    @Query("SELECT u.rating FROM UserEntity u WHERE u.userName = :userName")
    Optional<Long> getRatingByUserName(@Param("userName") String userName);
}
