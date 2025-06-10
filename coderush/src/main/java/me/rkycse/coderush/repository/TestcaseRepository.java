package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestcaseRepository extends JpaRepository<TestcaseEntity, Long> {

    @Query("SELECT t FROM TestcaseEntity t WHERE t.questionId = :questionId")
    List<TestcaseEntity> findByQuestionId(long questionId);

    @Query("SELECT COUNT(t) FROM TestcaseEntity t WHERE t.questionId = :questionId")
    long countByQuestionId(long questionId);
}
