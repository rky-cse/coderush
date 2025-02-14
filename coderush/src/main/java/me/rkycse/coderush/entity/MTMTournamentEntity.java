package me.rkycse.coderush.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "mtm_tournaments")

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getTeamStyle() {
        return teamStyle;
    }

    public void setTeamStyle(Boolean teamStyle) {
        this.teamStyle = teamStyle;
    }
}
