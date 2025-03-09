package me.rkycse.coderush.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "free_style_tournament_testcases")
public class FreeStyleTournamentTestcaseEntity {

    @Id
    private Long id;

    @Column(name = "tournamentId", unique = true, nullable = false)
    private Long tournamentId;

    @Column(name = "userId", unique = true, nullable = false)
    private Long userId;

    @Column(name = "questionId", nullable = false)
    private Long questionId;

    @Column(name = "testcaseId", nullable = false)
    private Long testcaseId;
    @Column(name = "number_of_attempts", nullable = false)
    private Long numberOfAttempts=0L;

    @Column(name = "solved", nullable = false)
    private Boolean solved = false;

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Long getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(Long numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }



    // Default constructor
    public FreeStyleTournamentTestcaseEntity() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }
}

