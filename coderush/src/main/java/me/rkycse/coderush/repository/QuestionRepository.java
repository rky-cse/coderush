package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    //List<QuestionEntity> findByCreator(UserEntity creator);
    List<QuestionEntity> findByCreatorId(Long creatorId);
    Optional<QuestionEntity> findById(Long id);

}
