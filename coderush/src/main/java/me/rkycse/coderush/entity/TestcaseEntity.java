package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "testcases")

public class TestcaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testcaseId;

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    private String output;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "question_id",nullable = false)
    private Long questionId;

//    // Many-to-One relationship with QuestionEntity
//    @ManyToOne
//    @JoinColumn(name = "question_id", nullable = false)
//    private QuestionEntity question;


    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
}
