package me.rkycse.coderush.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tournament_questions")
public class TournamentQuestionEntity {

    @Id
    private Long id;

    @Column(name = "tournamentId", unique = true, nullable = false)
    private Long tournamentId;

    @Column(name = "questionId", nullable = false)
    private Long questionId;

    // Default constructor
    public TournamentQuestionEntity() {
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

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
}
