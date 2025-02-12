package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "base_tournaments")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
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
}
