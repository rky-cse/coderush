package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_status",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_name", "tournament_id", "question_id"})})
public class SubmissionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "is_solved", nullable = false)
    private Boolean solved;

    @Column(name = "number_of_attempts", nullable = false)
    private int numberOfAttempts;

    @Column(name = "submission_time", nullable = false)
    private Long submissionTime;

    public Long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Long submissionTime) {
        this.submissionTime = submissionTime;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }



    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "SubmissionStatus{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", tournamentId=" + tournamentId +
                ", questionId=" + questionId +
                ", solved=" + solved +
                ", numberOfAttempts=" + numberOfAttempts +
                ", submissionTime=" + submissionTime +
                '}';
    }
}
