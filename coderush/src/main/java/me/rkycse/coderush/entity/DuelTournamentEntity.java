package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "duel_tournaments")
@Getter
@Setter
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
}
