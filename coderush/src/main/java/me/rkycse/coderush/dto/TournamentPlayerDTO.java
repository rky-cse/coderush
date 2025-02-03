package me.rkycse.coderush.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TournamentPlayerDTO {

    private Long id;
    private Long tournamentId;
    private String playerUserName;

}