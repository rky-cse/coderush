package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.FreeStyleSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserTestcaseRepository extends JpaRepository<FreeStyleSubmissionStatus, Long> {

    @Query("SELECT u FROM FreeStyleSubmissionStatus u WHERE u.userName = :userName AND u.tournamentId = :tournamentId AND u.testcaseId = :testcaseId")
    Optional<FreeStyleSubmissionStatus> findByUserNameAndTournamentIdAndTestcaseId(@Param("userName") String userName,
                                                                                   @Param("tournamentId") Long tournamentId,
                                                                                   @Param("testcaseId") Long testcaseId);
}
