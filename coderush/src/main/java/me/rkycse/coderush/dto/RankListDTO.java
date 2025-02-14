package me.rkycse.coderush.dto;


import java.util.ArrayList;
import java.util.List;

public class RankListDTO {
    private Long tournamentId;
    private Long endTime;
    private List<RankWithUserTestcaseDTO>rankList=new ArrayList<>();
    @Override
    public String toString() {
        return "RankListDTO{" +
                "tournamentId=" + tournamentId +
                ", rankList=" + rankList +
                '}';
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<RankWithUserTestcaseDTO> getRankList() {
        return rankList;
    }

    public void setRankList(List<RankWithUserTestcaseDTO> rankList) {
        this.rankList = rankList;
    }
}
