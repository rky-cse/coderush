package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.ClassicTestcaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ClassicTestcaseRepository extends JpaRepository<ClassicTestcaseEntity, Long> {

    List<ClassicTestcaseEntity> findByQuestionId(Long questionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassicTestcaseEntity t WHERE t.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
}