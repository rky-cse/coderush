package me.rkycse.coderush.controller;

//for testing purpose only

import me.rkycse.coderush.dto.ClassicSubmissionDTO;
import me.rkycse.coderush.kafka.Producer;
import me.rkycse.coderush.util.JsonConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/submit")
public class ClassicSubmissionController {

    private final Producer producer;

    public ClassicSubmissionController(Producer producer) {
        this.producer = producer;
    }


    @PostMapping("/{tournamentId}/{questionId}")
    ResponseEntity<String>response(@RequestBody ClassicSubmissionDTO submission, @PathVariable long tournamentId, @PathVariable long questionId) {
        String stringSubmission = JsonConverter.toJson(submission);
        System.out.println("stringSubmission: " + stringSubmission);
        producer.sendClassicSubmission(stringSubmission,tournamentId);

        return ResponseEntity.ok().body("Classic Submission Submitted");

    }




}
