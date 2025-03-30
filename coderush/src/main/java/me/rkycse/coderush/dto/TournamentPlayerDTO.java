package me.rkycse.coderush.dto;

public class TournamentPlayerDTO {

    private Long id;
    private Long tournamentId;
    private String playerUserName;
    private Long rating;

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

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

    public String getPlayerUserName() {
        return playerUserName;
    }

    public void setPlayerUserName(String playerUserName) {
        this.playerUserName = playerUserName;
    }
}