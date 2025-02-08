package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;this.userRepository = userRepository;
    }
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
    public Boolean createQuestion(QuestionDTO question) {
        if(question!=null){
            String username = getCurrentUsername();
            UserEntity user = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(question.getRated()==null)question.setRated(false);
            boolean rated = question.getRated();

            if (rated && !user.getRoles().contains("ROLE_QUESTION_SETTER")) {
                throw new RuntimeException("Only users with role 'question_setter' can create rated questions.");
            }
            if(question.getName()!=null && !question.getLegend().isEmpty()){

                QuestionEntity newQuestion = new QuestionEntity();
                newQuestion.setName(question.getName());
                newQuestion.setCreator(user);
                newQuestion.setLegend(question.getLegend());
                newQuestion.setInputFormat(question.getInputFormat());
                newQuestion.setOutputFormat(question.getOutputFormat());
                newQuestion.setNotes(question.getNotes());
                newQuestion.setTutorial(question.getTutorial());
                newQuestion.setRated(rated);

                QuestionEntity savedQuestion= questionRepository.save(newQuestion);
                return true;
            }
        }
        return false;
    }



}
