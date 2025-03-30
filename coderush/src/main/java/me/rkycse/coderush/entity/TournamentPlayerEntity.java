package me.rkycse.coderush.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "tournament_players")

public class TournamentPlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "player_username", nullable = false)
    private String playerUserName;

    @Column(name = "rating", nullable = false)
    private Long rating;

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

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

    public String getPlayerUserName() {
        return playerUserName;
    }

    public void setPlayerUserName(String playerUserName) {
        this.playerUserName = playerUserName;
    }
}