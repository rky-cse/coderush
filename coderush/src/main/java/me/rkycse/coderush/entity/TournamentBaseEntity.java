package me.rkycse.coderush.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "base_tournaments")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class TournamentBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id", nullable = false, unique = true)
    private Long tournamentId;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "rated", nullable = false)
    private Boolean rated = false;

    @Column(name = "duration_in_seconds", nullable = false)
    private long durationInSeconds;

    public long getPenaltyFactor() {
        return penaltyFactor;
    }

    public void setPenaltyFactor(long penaltyFactor) {
        this.penaltyFactor = penaltyFactor;
    }

    @Column(name = "penalty_factor", nullable = false)
    private long penaltyFactor;

    // Visibility using an enum
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_type", nullable = false)
    private TournamentType tournamentType=TournamentType.CLASSIC;


    // Enum for Visibility
    public enum Visibility {
        PUBLIC,
        PRIVATE
    }
    public enum TournamentType{
        CLASSIC,
        FREE_STYLE
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Boolean getRated() {
        return rated;
    }

    public void setRated(Boolean rated) {
        this.rated = rated;
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }
}
