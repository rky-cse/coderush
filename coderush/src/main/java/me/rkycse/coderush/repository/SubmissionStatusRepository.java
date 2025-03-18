package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SubmissionStatusRepository extends JpaRepository<SubmissionStatus, Long> {

    @Query("SELECT u FROM SubmissionStatus u WHERE u.userName = :userName AND u.tournamentId = :tournamentId AND u.questionId = :questionId")
    Optional<SubmissionStatus> findByUserNameAndTournamentIdAndQuestionId(@Param("userName") String userName,
                                                                                   @Param("tournamentId") Long tournamentId,
                                                                                   @Param("questionId") Long questionId);
}
