package me.rkycse.coderush.repository;

import me.rkycse.coderush.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    Long findQuestionIdByCreaterUserName(String createrUserName);
}
