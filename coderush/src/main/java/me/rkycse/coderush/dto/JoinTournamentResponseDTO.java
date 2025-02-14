package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
@Getter
@Setter
public class JoinTournamentResponseDTO {
    private Long tournamentId;
    private TournamentBaseDTO tournament;
    private HashSet<TournamentPlayerDTO>tournamentPlayerList=new HashSet<>();

}
