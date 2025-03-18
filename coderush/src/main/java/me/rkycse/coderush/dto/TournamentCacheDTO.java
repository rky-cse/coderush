package me.rkycse.coderush.dto;


import me.rkycse.coderush.entity.TournamentBaseEntity;

public class TournamentCacheDTO{
    private Long tournamentId;
    private long startTime;
    private long penaltyFactor;
    private long durationInSeconds;
    private TournamentBaseEntity.TournamentType tournamentType;
    private boolean scheduled;

    public TournamentBaseEntity.TournamentType getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(TournamentBaseEntity.TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }

    public TournamentCacheDTO() {
    }

    public Long getTournamentId() {
        return tournamentId;
    }


    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getPenaltyFactor() {
        return penaltyFactor;
    }

    public void setPenaltyFactor(long penaltyFactor) {
        this.penaltyFactor = penaltyFactor;
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

    @Override
    public String toString() {
        return "TournamentCacheDTO{" +
                "tournamentId=" + tournamentId +
                ", startTime=" + startTime +
                ", penaltyFactor=" + penaltyFactor +
                ", durationInSeconds=" + durationInSeconds +
                ", tournamentType=" + tournamentType +
                ", scheduled=" + scheduled +
                '}';
    }
}

