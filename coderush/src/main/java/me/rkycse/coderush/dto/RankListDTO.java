package me.rkycse.coderush.dto;

import java.util.ArrayList;
import java.util.List;

public class RankListDTO {
    private Long tournamentId;
    private List<RankWithUserTestcaseDTO>rankList=new ArrayList<>();

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public List<RankWithUserTestcaseDTO> getRankList() {
        return rankList;
    }

    @Override
    public String toString() {
        return "RankListDTO{" +
                "tournamentId=" + tournamentId +
                ", rankList=" + rankList +
                '}';
    }

    public void setRankList(List<RankWithUserTestcaseDTO> rankList) {
        this.rankList = rankList;
    }
}
