package me.rkycse.coderush.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TournamentCacheDTO implements Serializable {
    private Long tournamentId;
    private LocalDateTime startTime;
    private boolean scheduled;

    // Constructors, getters, and setters
    public TournamentCacheDTO() {}

    public TournamentCacheDTO(Long tournamentId, LocalDateTime startTime, boolean scheduled) {
        this.tournamentId = tournamentId;
        this.startTime = startTime;
        this.scheduled = scheduled;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}
