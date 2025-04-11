package me.rkycse.coderush.dto;

public class MTMTournamentTabDTO extends TournamentBaseDTO {
    private String name;
    private Long creatorId;
    private String creatorUserName;
    private String description;
    private long minRatingReq;
    private long maxRatingReq;
    private Boolean teamStyle = false; // Default to false
    
    // Add these fields for user-specific tournament information
    private long score;
    private Long penalty;
    private Long rating;

    // Existing getters and setters...
    // ...existing code...
    public String getName() {
        return name;
    }
    public String getCreatorUserName() {
        return creatorUserName;
    }
    
    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
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
    // New getters and setters for rank information
    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public Long getPenalty() {
        return penalty;
    }

    public void setPenalty(Long penalty) {
        this.penalty = penalty;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }
}