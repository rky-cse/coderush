package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private int index;
    private Long tournamentId;
    private long submissionTime;
    private String userOutput;
    @Override
    public String toString() {
        return "UserResponseDTO{" +
                "index=" + index +
                ", tournamentId=" + tournamentId +
                ", submissionTime=" + submissionTime +
                ", userOutput='" + userOutput + '\'' +
                '}';
    }
}

