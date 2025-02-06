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

    // New field: Visibility (e.g., "PUBLIC" or "PRIVATE")
    @Column(name = "visibility")
    private String visibility = "PUBLIC"; // Default visibility is public

    // New field: Password for private tournaments (can be null for public tournaments)
    @Column(name = "password")
    private String password;

    // New field: Tournament mode/type (e.g., "1 vs 1 tournament", "many vs many", "single")
    @Column(name = "tournament_mode")
    private String tournamentMode;

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
                ", visibility='" + visibility + '\'' +
                ", password='" + password + '\'' +
                ", tournamentMode='" + tournamentMode + '\'' +
                '}';
    }
}
