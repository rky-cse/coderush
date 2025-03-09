package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
    private QuestionDTO convertToDTO(QuestionEntity question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setName(question.getName());
        dto.setLegend(question.getLegend());
        dto.setInputFormat(question.getInputFormat());
        dto.setOutputFormat(question.getOutputFormat());
        dto.setNotes(question.getNotes());
        dto.setTutorial(question.getTutorial());
        dto.setRated(question.isRated());
        return dto;
    }
    public QuestionDTO getQuestionById(Long questionId) {
        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return convertToDTO(question);
    }
    public Boolean createQuestion(QuestionDTO question) {
        if (question != null) {
            String username = getCurrentUsername();
            UserEntity user = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (question.getRated() == null) question.setRated(false);
            boolean rated = question.getRated();

            if (rated && !user.getRoles().contains("ROLE_QUESTION_SETTER")) {
                throw new RuntimeException("Only users with role 'question_setter' can create rated questions.");
            }
            if (question.getName() != null && !question.getLegend().isEmpty()) {

                QuestionEntity newQuestion = new QuestionEntity();
                newQuestion.setCreatorId(user.getId());
                newQuestion.setName(question.getName());
                newQuestion.setLegend(question.getLegend());
                newQuestion.setInputFormat(question.getInputFormat());
                newQuestion.setOutputFormat(question.getOutputFormat());
                newQuestion.setNotes(question.getNotes());
                newQuestion.setTutorial(question.getTutorial());
                newQuestion.setRated(rated);

                questionRepository.save(newQuestion);
                return true;
            }
        }
        return false;
    }

    public List<QuestionDTO> getQuestionsByUsername(String username) {
        UserEntity user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<QuestionEntity> questions = questionRepository.findByCreatorId(user.getId());
        return questions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public boolean updateQuestion(Long questionId, QuestionDTO updatedQuestion) {
        QuestionEntity existingQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        String username = getCurrentUsername();
        UserEntity user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingQuestion.getCreatorId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this question");
        }

        existingQuestion.setName(updatedQuestion.getName());
        existingQuestion.setLegend(updatedQuestion.getLegend());
        existingQuestion.setInputFormat(updatedQuestion.getInputFormat());
        existingQuestion.setOutputFormat(updatedQuestion.getOutputFormat());
        existingQuestion.setNotes(updatedQuestion.getNotes());
        existingQuestion.setTutorial(updatedQuestion.getTutorial());

        questionRepository.save(existingQuestion);
        return true;
    }

}
