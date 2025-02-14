package me.rkycse.coderush.dto;


public class TournamentCacheDTO{
    private Long tournamentId;
    private long startTime;
    private long durationInSeconds;
    private boolean scheduled;

    public TournamentCacheDTO() {
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public TournamentCacheDTO(Long tournamentId, long startTime, long durationInSeconds, boolean scheduled) {
        this.tournamentId = tournamentId;
        this.startTime = startTime;
        this.durationInSeconds = durationInSeconds;
        this.scheduled = scheduled;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}

