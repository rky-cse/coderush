package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserTournamentRatingRepository extends JpaRepository<UserTournamentRatingEntity, Long> {

    @Query("SELECT u FROM UserTournamentRatingEntity u WHERE u.username = :username ORDER BY u.ratingUpdateTimestamp")
    List<UserTournamentRatingEntity> findByUsernameSortedByTimestamp(@Param("username") String username);
}

