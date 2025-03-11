package me.rkycse.coderush.dto;


import java.util.*;

public class RankWithFreeStyleSubmissionDTO {

    private Long id;
    private String userName;
    private Long tournamentId;
    private long score;
    private Long penalty=0L;
    private List<FreeStyleSubmissionStatusDTO> freeStyleSubmissionStatusDTOS =new ArrayList<>();


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

    public List<FreeStyleSubmissionStatusDTO> getFreeStyleSubmissionDTOS() {
        return freeStyleSubmissionStatusDTOS;
    }

    public void setFreeStyleSubmissionDTOS(List<FreeStyleSubmissionStatusDTO> freeStyleSubmissionStatusDTOS) {
        this.freeStyleSubmissionStatusDTOS = freeStyleSubmissionStatusDTOS;
    }
}