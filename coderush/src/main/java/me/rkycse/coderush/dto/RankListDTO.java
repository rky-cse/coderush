package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class RankListDTO {
    private Long tournamentId;
    private List<RankWithUserTestcaseDTO>rankList=new ArrayList<>();
    @Override
    public String toString() {
        return "RankListDTO{" +
                "tournamentId=" + tournamentId +
                ", rankList=" + rankList +
                '}';
    }
}
