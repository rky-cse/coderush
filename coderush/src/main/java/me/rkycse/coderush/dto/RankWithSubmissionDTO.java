package me.rkycse.coderush.dto;


import java.util.*;

public class RankWithSubmissionDTO {

    private Long id;
    private String userName;
    private Long tournamentId;
    private long score;
    private Long penalty=0L;
    private List<SubmissionStatusDTO> SubmissionStatusDTOS =new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public Long getPenalty() {
        return penalty;
    }

    public void setPenalty(Long penalty) {
        this.penalty = penalty;
    }

    public List<SubmissionStatusDTO> getSubmissionDTOS() {
        return SubmissionStatusDTOS;
    }

    public void setSubmissionDTOS(List<SubmissionStatusDTO> SubmissionStatusDTOS) {
        this.SubmissionStatusDTOS = SubmissionStatusDTOS;
    }
}