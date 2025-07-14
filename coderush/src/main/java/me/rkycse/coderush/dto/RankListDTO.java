package me.rkycse.coderush.dto;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankListDTO {
    private Long tournamentId;
    private Long endTime;
    private List<RankWithSubmissionDTO>rankList=new ArrayList<>();
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

    public List<RankWithSubmissionDTO> getRankList() {
        return rankList;
    }

    public void setRankList(List<RankWithSubmissionDTO> rankList) {
        this.rankList = rankList;
    }
    public void sortByScore() {
        rankList.sort(
                Comparator.comparingLong(RankWithSubmissionDTO::getScore).reversed()
                        .thenComparingLong(RankWithSubmissionDTO::getPenalty)
        );
    }

}
