// MatchResult.java
package me.rkycse.coderush.dto;

public class MatchResult {
    private String player1;
    private String player2;
    private String  opponent = "NO";
    private Long tournamentId;

    // Getters and setters
    public String getPlayer1() {
        return player1;
    }
    public void setPlayer1(String player1) {
        this.player1 = player1;
    }
    public String getPlayer2() {
        return player2;
    }
    public void setPlayer2(String player2) {
        this.player2 = player2;
    }
    public void setTournamentId(Long tournamentId) {this.tournamentId = tournamentId;}
    public Long getTournamentID() {
        return tournamentId;
    }
    public void setOpponentTrue() { this.opponent = "YES";}
    public String getOpponent() { return opponent; }
}
