package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.dto.UserDto;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

        List<QuestionEntity> questions = questionRepository.findByCreator(user);

        return questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean updateQuestion(Long questionId, QuestionDTO updatedQuestion) {
        String username = getCurrentUsername();

        // Find the existing question
        Optional<QuestionEntity> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            return false; // Question not found
        }

        QuestionEntity existingQuestion = questionOpt.get();

        // Check if the user is the creator of the question
        if (!existingQuestion.getCreator().getUserName().equals(username)) {
            throw new RuntimeException("You are not authorized to update this question.");
        }

        // Update only non-null fields
        if (updatedQuestion.getName() != null) existingQuestion.setName(updatedQuestion.getName());
        if (updatedQuestion.getLegend() != null) existingQuestion.setLegend(updatedQuestion.getLegend());
        if (updatedQuestion.getInputFormat() != null) existingQuestion.setInputFormat(updatedQuestion.getInputFormat());
        if (updatedQuestion.getOutputFormat() != null) existingQuestion.setOutputFormat(updatedQuestion.getOutputFormat());
        if (updatedQuestion.getNotes() != null) existingQuestion.setNotes(updatedQuestion.getNotes());
        if (updatedQuestion.getTutorial() != null) existingQuestion.setTutorial(updatedQuestion.getTutorial());
        if (updatedQuestion.getRated() != null) existingQuestion.setRated(updatedQuestion.getRated());

        questionRepository.save(existingQuestion);
        return true;
    }
    public QuestionDTO getQuestionById(Long questionId) {
        Optional<QuestionEntity> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new RuntimeException("Question not found with ID: " + questionId);
        }
        return convertToDTO(questionOpt.get());
    }

    private QuestionDTO convertToDTO(QuestionEntity questionEntity) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionId(questionEntity.getQuestionId());
        dto.setName(questionEntity.getName());
        dto.setLegend(questionEntity.getLegend());
        dto.setInputFormat(questionEntity.getInputFormat());
        dto.setOutputFormat(questionEntity.getOutputFormat());
        dto.setNotes(questionEntity.getNotes());
        dto.setTutorial(questionEntity.getTutorial());
        dto.setRated(questionEntity.isRated());
        dto.setImageUrls(List.of()); // Modify this if image URLs are stored

        UserDto creatorDto = new UserDto();
        creatorDto.setId(questionEntity.getCreator().getId());
        creatorDto.setUserName(questionEntity.getCreator().getUserName());
        creatorDto.setFirstName(questionEntity.getCreator().getFirstName());
        creatorDto.setLastName(questionEntity.getCreator().getLastName());
        creatorDto.setEmail(questionEntity.getCreator().getEmail());
        creatorDto.setRoles(List.of()); // Modify this if roles are stored

        dto.setCreator(creatorDto);
        return dto;
    }
}
