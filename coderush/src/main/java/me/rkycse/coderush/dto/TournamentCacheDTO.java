package me.rkycse.coderush.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentCacheDTO implements Serializable {
    private Long tournamentId;
    private LocalDateTime startTime;
    private long durationInSeconds;
    private boolean scheduled;


}
