package me.rkycse.coderush.dto;

import lombok.Data;

@Data
public class DuelTournamentDTO extends TournamentBaseDTO {
    private Long player1; // userId of player1
    private Long player2; // userId of player2
}
