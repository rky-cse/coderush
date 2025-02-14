package me.rkycse.coderush.dto;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(long submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getUserOutput() {
        return userOutput;
    }

    public void setUserOutput(String userOutput) {
        this.userOutput = userOutput;
    }
}

