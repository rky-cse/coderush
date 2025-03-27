package me.rkycse.coderush.dto;

import java.io.Serializable;

public class PendingMatch implements Serializable {
    private String pendingMatchId;
    private Long player1Id;
    private Long player2Id;
    private boolean player1Confirmed;
    private boolean player2Confirmed;
    private long createdAt;
    private PendingStatus status;

    private long scheduledStartTime; // For the eventual DuelTournament start

    public enum PendingStatus {
        WAITING_CONFIRMATION,
        CONFIRMED,
        CANCELLED
    }

    public String getPendingMatchId() {
        return pendingMatchId;
    }

    public void setPendingMatchId(String pendingMatchId) {
        this.pendingMatchId = pendingMatchId;
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
    }

    public boolean isPlayer1Confirmed() {
        return player1Confirmed;
    }

    public void setPlayer1Confirmed(boolean player1Confirmed) {
        this.player1Confirmed = player1Confirmed;
    }

    public boolean isPlayer2Confirmed() {
        return player2Confirmed;
    }

    public void setPlayer2Confirmed(boolean player2Confirmed) {
        this.player2Confirmed = player2Confirmed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public PendingStatus getStatus() {
        return status;
    }

    public void setStatus(PendingStatus status) {
        this.status = status;
    }

    public long getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(long scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }
}