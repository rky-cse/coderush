package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.TestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestcaseRepository extends JpaRepository<TestcaseEntity, Long> {

    // Ensure the field name matches 'questionId' in TestcaseEntity
    @Query("SELECT t FROM TestcaseEntity t WHERE t.questionId = :questionId")
    List<TestcaseEntity> findByQuestionId(long questionId);
    //List<TestcaseEntity> findByQuestion_QuestionId(Long questionId);


}
