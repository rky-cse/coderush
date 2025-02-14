package me.rkycse.coderush.dto;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentCacheDTO{
    private Long tournamentId;
    private long startTime;
    private long durationInSeconds;
    private boolean scheduled;
}

