package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.UserTestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserTestcaseRepository extends JpaRepository<UserTestcaseEntity, Long> {

    @Query("SELECT u FROM UserTestcaseEntity u WHERE u.userName = :userName AND u.tournamentId = :tournamentId AND u.testcaseId = :testcaseId")
    Optional<UserTestcaseEntity> findByUserNameAndTournamentIdAndTestcaseId(@Param("userName") String userName,
                                                                            @Param("tournamentId") Long tournamentId,
                                                                            @Param("testcaseId") Long testcaseId);
}
