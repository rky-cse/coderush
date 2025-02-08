package me.rkycse.coderush.dto;

import lombok.Data;
import java.time.LocalDateTime;
import me.rkycse.coderush.entity.TournamentBase.Visibility;

@Data
public class TournamentBaseDTO {
    private Long tournamentId;
    private LocalDateTime startTime;
    private Boolean rated;
    private long durationInSeconds;
    private Visibility visibility;
    private String password;
}
