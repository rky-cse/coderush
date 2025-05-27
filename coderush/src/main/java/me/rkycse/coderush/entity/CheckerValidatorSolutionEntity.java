package me.rkycse.coderush.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "checker_validator_solution")
public class CheckerValidatorSolutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;

    private String checkerFilePath;

    private String validatorFilePath;

    private String solutionFilePath;

    // Constructors
    public CheckerValidatorSolutionEntity() {
    }

    public CheckerValidatorSolutionEntity(Long id, Long questionId, String checkerFilePath, String validatorFilePath, String solutionFilePath) {
        this.id = id;
        this.questionId = questionId;
        this.checkerFilePath = checkerFilePath;
        this.validatorFilePath = validatorFilePath;
        this.solutionFilePath = solutionFilePath;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getCheckerFilePath() {
        return checkerFilePath;
    }

    public void setCheckerFilePath(String checkerFilePath) {
        this.checkerFilePath = checkerFilePath;
    }

    public String getValidatorFilePath() {
        return validatorFilePath;
    }

    public void setValidatorFilePath(String validatorFilePath) {
        this.validatorFilePath = validatorFilePath;
    }

    public String getSolutionFilePath() {
        return solutionFilePath;
    }

    public void setSolutionFilePath(String solutionFilePath) {
        this.solutionFilePath = solutionFilePath;
    }
}
