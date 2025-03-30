package me.rkycse.coderush.entity;


import jakarta.persistence.*;
import me.rkycse.coderush.dto.ClassicSubmissionDTO;

import java.util.Objects;

@Entity
@Table(name = "classic_submissions",
        indexes = {
                @Index(name = "idx_tournament_user", columnList = "tournament_id, username"),
                @Index(name = "idx_tournament_question", columnList = "tournament_id, question_id")
        })
public class ClassicSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_index", nullable = false)
    private int index;

    @Column(nullable = false)
    private String username;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(name = "max_time_taken")
    private Long maxTimeTaken;

    @Column(name = "max_memory_used")
    private Long maxMemoryUsed;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(length = 20, nullable = false)
    private String language;

    @Column(length = 10)
    private String verdict; // AC,CE,WA,RE,TLE,MLE

    @Column(name = "submission_time", nullable = false)
    private Long submissionTime; // in milliseconds

    @Column(name = "judging_time")
    private Long judgingTime; // time when judging starts

    // Constructors
    public ClassicSubmissionEntity() {
    }

    public ClassicSubmissionEntity(ClassicSubmissionDTO dto) {
        this.id = dto.getId();
        this.index = dto.getIndex();
        this.username = dto.getUsername();
        this.tournamentId = dto.getTournamentId();
        this.code = dto.getCode();
        this.maxTimeTaken = dto.getMaxTimeTaken();
        this.maxMemoryUsed = dto.getMaxMemoryUsed();
        this.questionId = dto.getQuestionId();
        this.language = dto.getLanguage();
        this.verdict = dto.getVerdict();
        this.submissionTime = dto.getSubmissionTime();
        this.judgingTime = dto.getJudgingTime();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getMaxTimeTaken() {
        return maxTimeTaken;
    }

    public void setMaxTimeTaken(Long maxTimeTaken) {
        this.maxTimeTaken = maxTimeTaken;
    }

    public Long getMaxMemoryUsed() {
        return maxMemoryUsed;
    }

    public void setMaxMemoryUsed(Long maxMemoryUsed) {
        this.maxMemoryUsed = maxMemoryUsed;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public Long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Long submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Long getJudgingTime() {
        return judgingTime;
    }

    public void setJudgingTime(Long judgingTime) {
        this.judgingTime = judgingTime;
    }

    // Helper method to convert Entity to DTO
    public ClassicSubmissionDTO toDTO() {
        ClassicSubmissionDTO dto = new ClassicSubmissionDTO();
        dto.setId(this.id);
        dto.setIndex(this.index);
        dto.setUsername(this.username);
        dto.setTournamentId(this.tournamentId);
        dto.setCode(this.code);
        dto.setMaxTimeTaken(this.maxTimeTaken);
        dto.setMaxMemoryUsed(this.maxMemoryUsed);
        dto.setQuestionId(this.questionId);
        dto.setLanguage(this.language);
        dto.setVerdict(this.verdict);
        dto.setSubmissionTime(this.submissionTime);
        dto.setJudgingTime(this.judgingTime);
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassicSubmissionEntity that = (ClassicSubmissionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClassicSubmissionEntity{" +
                "id=" + id +
                ", index=" + index +
                ", username='" + username + '\'' +
                ", tournamentId=" + tournamentId +
                ", language='" + language + '\'' +
                ", verdict='" + verdict + '\'' +
                ", submissionTime=" + submissionTime +
                '}';
    }
}
