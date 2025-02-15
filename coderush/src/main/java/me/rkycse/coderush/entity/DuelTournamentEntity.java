package me.rkycse.coderush.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "duel_tournaments")

public class DuelTournamentEntity extends TournamentBaseEntity {

    @Column(name = "player1", nullable = false)
    private Long player1;

    @Column(name = "player2", nullable = false)
    private Long player2;

    @Override
    public String toString() {
        return "DuelTournamentEntity{" +
                "tournamentId=" + getTournamentId() +
                ", startTime=" + getStartTime() +
                ", rated=" + getRated() +
                ", durationInSeconds=" + getDurationInSeconds() +
                ", visibility=" + getVisibility() +
                ", password='" + getPassword() + '\'' +
                ", player1=" + player1 +
                ", player2=" + player2 +
                '}';
    }

    public Long getPlayer1() {
        return player1;
    }

    public void setPlayer1(Long player1) {
        this.player1 = player1;
    }

    public Long getPlayer2() {
        return player2;
    }

    public void setPlayer2(Long player2) {
        this.player2 = player2;
    }
}
