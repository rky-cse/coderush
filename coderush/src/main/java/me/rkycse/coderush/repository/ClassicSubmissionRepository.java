package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.ClassicSubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassicSubmissionRepository extends JpaRepository<ClassicSubmissionEntity, Long> {

    /**
     * Find all submissions for a specific user in a tournament
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId AND c.username = :username")
    List<ClassicSubmissionEntity> findSubmissionsForUser(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username);

    /**
     * Find all submissions ordered by submission time (newest first)
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId AND c.username = :username ORDER BY c.submissionTime DESC")
    List<ClassicSubmissionEntity> findSubmissionsByTimeDesc(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username);

    /**
     * Find all submissions for a specific problem in a tournament for a user
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId AND c.username = :username AND c.index = :problemIndex")
    List<ClassicSubmissionEntity> findSubmissionsForProblem(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username,
            @Param("problemIndex") int problemIndex);

    /**
     * Find only the latest submission for each problem by a user in a tournament
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId AND c.username = :username " +
            "AND c.submissionTime = (SELECT MAX(c2.submissionTime) FROM ClassicSubmissionEntity c2 " +
            "WHERE c2.tournamentId = c.tournamentId AND c2.username = c.username AND c2.index = c.index)")
    List<ClassicSubmissionEntity> findLatestSubmissionsForEachProblem(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username);

    /**
     * Find only accepted submissions
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId " +
            "AND c.username = :username AND c.verdict = 'AC'")
    List<ClassicSubmissionEntity> findAcceptedSubmissions(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username);

    /**
     * Get the last submission time for a specific problem
     */
    @Query("SELECT MAX(c.submissionTime) FROM ClassicSubmissionEntity c " +
            "WHERE c.tournamentId = :tournamentId AND c.username = :username AND c.index = :problemIndex")
    Long findLastSubmissionTimeForProblem(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username,
            @Param("problemIndex") int problemIndex);

    /**
     * Find submissions with paging and sorting
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId AND c.username = :username")
    Page<ClassicSubmissionEntity> findSubmissionsWithPaging(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username,
            Pageable pageable);

    /**
     * Count submissions by problem
     */
    @Query("SELECT c.index, COUNT(c) FROM ClassicSubmissionEntity c " +
            "WHERE c.tournamentId = :tournamentId AND c.username = :username GROUP BY c.index")
    List<Object[]> countSubmissionsByProblemIndex(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username);

    /**
     * Find first accepted submission for each problem (important for scoring)
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId " +
            "AND c.username = :username AND c.verdict = 'AC' AND c.submissionTime = " +
            "(SELECT MIN(c2.submissionTime) FROM ClassicSubmissionEntity c2 WHERE " +
            "c2.tournamentId = c.tournamentId AND c2.username = c.username AND " +
            "c2.index = c.index AND c2.verdict = 'AC')")
    List<ClassicSubmissionEntity> findFirstAcceptedSubmissionsForEachProblem(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username);

    /**
     * Find submissions within a specific time range
     */
    @Query("SELECT c FROM ClassicSubmissionEntity c WHERE c.tournamentId = :tournamentId " +
            "AND c.username = :username AND c.submissionTime BETWEEN :startTime AND :endTime")
    List<ClassicSubmissionEntity> findSubmissionsInTimeRange(
            @Param("tournamentId") Long tournamentId,
            @Param("username") String username,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
}