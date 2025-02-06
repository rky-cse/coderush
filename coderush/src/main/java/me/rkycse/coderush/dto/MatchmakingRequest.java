// MatchmakingRequest.java
package me.rkycse.coderush.dto;

public class MatchmakingRequest {
    private String playerId;
    private long rating;

    // Getters and setters
    public String getPlayerId() {
        return playerId;
    }
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    public long getRating() {
        return rating;
    }
    public void setRating(long rating) {
        this.rating = rating;
    }
}
