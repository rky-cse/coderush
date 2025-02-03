package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
public class TournamentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id", nullable = false, unique = true)
    private Long tournamentId;

    @Column(name = "creator_username", nullable = false)
    private String creatorUserName;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "rated", nullable = false)
    private Boolean rated = false;

    @Column(name = "min_rating_req")
    private long minRatingReq;

    @Column(name = "max_rating_req")
    private long maxRatingReq;

    @Column(name = "duration_in_seconds", nullable = false)
    private long durationInSeconds;

    @Override
    public String toString() {
        return "TournamentEntity{" +
                "tournamentId=" + tournamentId +
                ", creatorUserName='" + creatorUserName + '\'' +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", rated=" + rated +
                ", minRatingReq=" + minRatingReq +
                ", maxRatingReq=" + maxRatingReq +
                ", durationInSeconds=" + durationInSeconds +
                '}';
    }
}