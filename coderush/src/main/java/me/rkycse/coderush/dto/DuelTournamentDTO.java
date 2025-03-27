package me.rkycse.coderush.dto;

public class DuelTournamentDTO extends TournamentBaseDTO {

    private Long player1; // userId of player1
    private Long player2; // userId of player2

    public Long getPlayer1() {
        return player1;
    }

    public void setPlayer1(Long player1) {
        this.player1 = player1;
    }

    public Long getPlayer2() {
        return player2;
    }

    public void setPlayer2(Long player2) {
        this.player2 = player2;
    }
}
