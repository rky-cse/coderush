package me.rkycse.coderush.dto;


import java.util.*;

public class RankWithUserTestcaseDTO {

    private Long id;
    private String userName;
    private Long tournamentId;
    private long score;
    private List<UserTestcaseDTO>userTestcases=new ArrayList<>();

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

    public List<UserTestcaseDTO> getUserTestcases() {
        return userTestcases;
    }

    public void setUserTestcases(List<UserTestcaseDTO> userTestcases) {
        this.userTestcases = userTestcases;
    }
}