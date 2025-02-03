package me.rkycse.coderush.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class RankDTO {

    private Long id;
    private String userName;
    private Long tournamentId;
    private long score;
}