package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Boolean createQuestion(QuestionDTO question) {
        if(question!=null){
            if(question.getName()!=null && !question.getLegend().isEmpty()){
                QuestionEntity questionEntity = new QuestionEntity();
                questionEntity.setLegend(question.getLegend());
                questionEntity.setInputFormat(question.getInputFormat());
                questionEntity.setOutputFormat(question.getOutputFormat());
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                questionEntity.setCreaterUserName(userDetails.getUsername());
                questionEntity.setName(question.getName());
                QuestionEntity savedQuestion= questionRepository.save(questionEntity);
                return true;
            }
        }
        return false;
    }



}
