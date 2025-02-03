package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTestcaseDTO {
    private Long id;
    private Long tournamentId;
    private String userName;
    private Long testcaseId;
    private Long questionId;
    private Boolean isSolved;
    private int numberOfAttempts;

}