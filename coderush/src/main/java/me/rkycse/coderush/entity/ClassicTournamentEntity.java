package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "classic_tournaments")
@Getter
@Setter
public class ClassicTournamentEntity extends TournamentBase {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "creator", nullable = false)
    private String creator;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "min_rating_req")
    private long minRatingReq;

    @Column(name = "max_rating_req")
    private long maxRatingReq;

    @Column(name = "team_style", nullable = false)
    private Boolean teamStyle = false;

    @Override
    public String toString() {
        return "ClassicTournamentEntity{" +
                "tournamentId=" + getTournamentId() +
                ", name='" + name + '\'' +
                ", creator='" + creator + '\'' +
                ", description='" + description + '\'' +
                ", startTime=" + getStartTime() +
                ", rated=" + getRated() +
                ", minRatingReq=" + minRatingReq +
                ", maxRatingReq=" + maxRatingReq +
                ", durationInSeconds=" + getDurationInSeconds() +
                ", visibility=" + getVisibility() +
                ", password='" + getPassword() + '\'' +
                ", teamStyle=" + teamStyle +
                '}';
    }
}
