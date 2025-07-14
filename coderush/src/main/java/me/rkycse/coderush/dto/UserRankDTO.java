package me.rkycse.coderush.dto;

public class UserRankDTO {
    Long tournamentId;
    Long currentRank;
    String userName;
    Long endTime;
    RankWithSubmissionDTO rankWithSubmissionDTO;

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(Long currentRank) {
        this.currentRank = currentRank;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public RankWithSubmissionDTO getRankWithSubmissionDTO() {
        return rankWithSubmissionDTO;
    }

    public void setRankWithSubmissionDTO(RankWithSubmissionDTO rankWithSubmissionDTO) {
        this.rankWithSubmissionDTO = rankWithSubmissionDTO;
    }
}
