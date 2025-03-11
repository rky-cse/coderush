package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TournamentQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentQuestionRepository extends JpaRepository<TournamentQuestionEntity, Long> {

    @Query("SELECT tq FROM TournamentQuestionEntity tq " +
            "WHERE tq.tournamentId = :tournamentId " +
            "AND tq.questionId = :questionId")
    TournamentQuestionEntity findByTournamentIdAndQuestionId(@Param("tournamentId") Long tournamentId,
                                                             @Param("questionId") Long questionId);
}
