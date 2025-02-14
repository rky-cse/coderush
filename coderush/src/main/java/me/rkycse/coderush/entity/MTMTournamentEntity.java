package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mtm_tournaments")
@Getter
@Setter
public class MTMTournamentEntity extends TournamentBaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "description",columnDefinition = "TEXT")
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
                ", creator='" + creatorId+ '\'' +
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
