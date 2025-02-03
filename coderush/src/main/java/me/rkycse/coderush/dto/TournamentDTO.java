package me.rkycse.coderush.dto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class TournamentDTO {
    private Long tournamentId;
    private String creatorUserName;
    private String description;
    private String name;
    private LocalDateTime startTime;
    private Boolean rated = false;
    private long minRatingReq;
    private long maxRatingReq;
    private long durationInSeconds;

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
                '}';
    }
}
