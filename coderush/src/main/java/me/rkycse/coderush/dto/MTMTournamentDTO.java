package me.rkycse.coderush.dto;


public class MTMTournamentDTO extends TournamentBaseDTO {
    private String name;
    private Long creatorId;
    private String description;
    private long minRatingReq;
    private long maxRatingReq;
    private Boolean teamStyle = false; // Default to false

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
