package me.rkycse.coderush.dto;

import lombok.Data;

@Data
public class ClassicTournamentDTO extends TournamentBaseDTO {
    private String name;
    private String creator;
    private String description;
    private long minRatingReq;
    private long maxRatingReq;
    private Boolean teamStyle = false; // Default to false
}
