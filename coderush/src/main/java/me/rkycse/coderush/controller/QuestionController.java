package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.service.QuestionService;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("createQuestion")
    public ResponseEntity<String> createQuestion(@RequestBody QuestionDTO question) {
        if(questionService.createQuestion(question)){
            return new ResponseEntity<>("Question created", HttpStatus.CREATED);

        }
        return new ResponseEntity<>("Question not created", HttpStatus.BAD_REQUEST);

    }

}
