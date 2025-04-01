package me.rkycse.coderush.dto;



import jakarta.persistence.*;


public class UserTournamentRatingDTO {


    private Long id;


    private Long tournamentId;


    private String username;


    private Long oldRating;


    private Long newRating;


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

    public UserTournamentRatingDTO(Long id, Long tournamentId, String username, Long oldRating, Long newRating, long ratingUpdateTimestamp) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.username = username;
        this.oldRating = oldRating;
        this.newRating = newRating;
        this.ratingUpdateTimestamp = ratingUpdateTimestamp;
    }

    public UserTournamentRatingDTO() {}
}

