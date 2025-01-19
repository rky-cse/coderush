package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tournaments")
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

    // Getters and Setters
    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Boolean getRated() {
        return rated;
    }

    public void setRated(Boolean rated) {
        this.rated = rated;
    }

    public long getMinRatingReq() {
        return minRatingReq;
    }

    public void setMinRatingReq(long minRatingReq) {
        this.minRatingReq = minRatingReq;
    }

    public long getMaxRatingReq() {
        return maxRatingReq;
    }

    public void setMaxRatingReq(long maxRatingReq) {
        this.maxRatingReq = maxRatingReq;
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

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