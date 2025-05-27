package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.CheckerValidatorSolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckerValidatorSolutionRepository
        extends JpaRepository<CheckerValidatorSolutionEntity, Long> {

    Optional<CheckerValidatorSolutionEntity> findByQuestionId(Long questionId);

}
