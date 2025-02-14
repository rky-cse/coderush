package me.rkycse.coderush.dto;

import lombok.Data;

import me.rkycse.coderush.entity.TournamentBaseEntity.TournamentType;
import me.rkycse.coderush.entity.TournamentBaseEntity.Visibility;


@Data
public class TournamentBaseDTO {
    private Long tournamentId;
    private Long startTime;
    private Boolean rated;
    private Long durationInSeconds;
    private Visibility visibility;
    private String password;
    private TournamentType tournamentType;
}
