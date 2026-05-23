package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("api/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // Create a new question
    @PostMapping("/createQuestion")
    public ResponseEntity<QuestionDTO> createQuestion(@RequestBody QuestionDTO question) {
        QuestionDTO questionDTO = questionService.createQuestion(question);
        if (questionDTO!=null) {
            return new ResponseEntity<QuestionDTO>(questionDTO, HttpStatus.CREATED);
        }
        return new ResponseEntity<QuestionDTO>(questionDTO, HttpStatus.BAD_REQUEST);
    }

    // Update an existing question
    @PutMapping("/{questionId}")
    public ResponseEntity<String> updateQuestion(@PathVariable Long questionId, @RequestBody QuestionDTO updatedQuestion) {
        boolean isUpdated = questionService.updateQuestion(questionId, updatedQuestion);

        if (isUpdated) {
            return new ResponseEntity<>("Question updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Question not found or update failed", HttpStatus.NOT_FOUND);
        }
    }

    // Get questions created by the authenticated user
    @GetMapping("/user")
    public List<QuestionDTO> getUserQuestions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return questionService.getQuestionsByUsername(username);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable Long questionId) {
        // Throws QuestionNotFoundException -> handled globally as 404 ApiError.
        return ResponseEntity.ok(questionService.getQuestionById(questionId));
    }
}
