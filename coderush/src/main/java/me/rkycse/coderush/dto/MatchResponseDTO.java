package me.rkycse.coderush.dto;

public class MatchResponseDTO {
    private String status;
    private Long matchId;
    private Long player1Id;
    private Long player2Id;
    private Long startTime; // Changed to Long
    private String pendingMatchId;

    public String getPendingMatchId() {
        return pendingMatchId;
    }

    public void setPendingMatchId(String pendingMatchId) {
        this.pendingMatchId = pendingMatchId;
    }

    public MatchResponseDTO(String status, Long matchId,
                            Long player1Id, Long player2Id, Long startTime, String pendingMatchId) {
        this.status = status;
        this.matchId = matchId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.startTime = startTime;
        this.pendingMatchId = pendingMatchId;
    }

    // Getters/Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(Long player1Id) { this.player1Id = player1Id; }

    public Long getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(Long player2Id) { this.player2Id = player2Id; }

    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
}