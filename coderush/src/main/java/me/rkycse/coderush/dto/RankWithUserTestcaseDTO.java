package me.rkycse.coderush.dto;

import jakarta.persistence.*;

import java.util.*;


public class RankWithUserTestcaseDTO {

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private Long id;

    private String userName;

    private Long tournamentId;

    private long score;

    public List<UserTestcaseDTO> getUserTestcases() {
        return userTestcases;
    }

    public void setUserTestcases(List<UserTestcaseDTO> userTestcases) {
        this.userTestcases = userTestcases;
    }

    private List<UserTestcaseDTO>userTestcases=new ArrayList<>();


    // Getters and Setters
    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}