package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "testcases")
public class TestcaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testcase_id", nullable = false, unique = true)
    private Long testcaseId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Lob
    @Column(name = "input", nullable = false)
    private String input;

    @Lob
    @Column(name = "output", nullable = false)
    private String output;

    @Column(name = "rating", nullable = false)
    private int rating;

    // Getters and Setters
    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
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
}