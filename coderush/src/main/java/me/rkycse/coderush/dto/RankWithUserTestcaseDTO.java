package me.rkycse.coderush.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
@Getter
@Setter
public class RankWithUserTestcaseDTO {

    private Long id;
    private String userName;
    private Long tournamentId;
    private long score;
    private List<UserTestcaseDTO>userTestcases=new ArrayList<>();

}