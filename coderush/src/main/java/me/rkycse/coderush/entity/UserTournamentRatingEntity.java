package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_tournament_ratings",
        indexes = {
                @Index(name = "idx_username_tournament", columnList = "username, tournament_id"),
                @Index(name = "idx_rating_update", columnList = "rating_update_timestamp")
        })
public class UserTournamentRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "old_rating", nullable = false)
    private Long oldRating;

    @Column(name = "new_rating", nullable = false)
    private Long newRating;

    // Using long to store the timestamp (e.g., epoch milliseconds/seconds)
    @Column(name = "rating_update_timestamp", nullable = false)
    private long ratingUpdateTimestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getOldRating() {
        return oldRating;
    }

    public void setOldRating(Long oldRating) {
        this.oldRating = oldRating;
    }

    public Long getNewRating() {
        return newRating;
    }

    public void setNewRating(Long newRating) {
        this.newRating = newRating;
    }

    public long getRatingUpdateTimestamp() {
        return ratingUpdateTimestamp;
    }

    public void setRatingUpdateTimestamp(long ratingUpdateTimestamp) {
        this.ratingUpdateTimestamp = ratingUpdateTimestamp;
    }

    public UserTournamentRatingEntity(Long id, Long tournamentId, String username, Long oldRating, Long newRating, long ratingUpdateTimestamp) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.username = username;
        this.oldRating = oldRating;
        this.newRating = newRating;
        this.ratingUpdateTimestamp = ratingUpdateTimestamp;
    }

    public UserTournamentRatingEntity() {}
}
