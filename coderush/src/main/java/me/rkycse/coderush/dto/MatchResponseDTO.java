package me.rkycse.coderush.dto;

public class MatchResponseDTO {

    private String status;
    private Long matchId;
    private Long player1Id;
    private String player1Name;
    private int  player1Rating;
    private Long player2Id;
    private String player2Name;
    private int  player2Rating;
    private Long startTime;
    private String pendingMatchId;

    public MatchResponseDTO(
            String status,
            Long   matchId,
            Long   player1Id,
            String player1Name,
            int    player1Rating,
            Long   player2Id,
            String player2Name,
            int    player2Rating,
            Long   startTime,
            String pendingMatchId
    ) {
        this.status         = status;
        this.matchId        = matchId;
        this.player1Id      = player1Id;
        this.player1Name    = player1Name;
        this.player1Rating  = player1Rating;
        this.player2Id      = player2Id;
        this.player2Name    = player2Name;
        this.player2Rating  = player2Rating;
        this.startTime      = startTime;
        this.pendingMatchId = pendingMatchId;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public int getPlayer1Rating() {
        return player1Rating;
    }

    public void setPlayer1Rating(int player1Rating) {
        this.player1Rating = player1Rating;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public int getPlayer2Rating() {
        return player2Rating;
    }

    public void setPlayer2Rating(int player2Rating) {
        this.player2Rating = player2Rating;
    }

    public String getPendingMatchId() {
        return pendingMatchId;
    }

    public void setPendingMatchId(String pendingMatchId) {
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