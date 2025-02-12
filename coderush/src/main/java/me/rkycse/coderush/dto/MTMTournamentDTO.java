package me.rkycse.coderush.dto;

import lombok.Data;
@Data
public class MTMTournamentDTO extends TournamentBaseDTO {
    private String name;
    private Long creatorId;
    private String description;
    private long minRatingReq;
    private long maxRatingReq;
    private Boolean teamStyle = false; // Default to false

}
