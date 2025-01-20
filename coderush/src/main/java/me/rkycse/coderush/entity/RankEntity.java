package me.rkycse.coderush.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "ranks")
public class RankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id", nullable = false, unique = true)
    private Long id;

    private Long tournamentId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "score", nullable = false)
    private long score;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}