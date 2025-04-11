package me.rkycse.coderush.dto;

public class RankDTO {

    private Long id;
    private String userName;
    private Long tournamentId;
    private Long penalty=0L;
    private long score;
    private Long rating;
    private Long startTime;

    @Override
    public String toString() {
        return "RankDTO{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", tournamentId=" + tournamentId +
                ", penalty=" + penalty +
                ", score=" + score +
                ", rating=" + rating +
                ", startTime="+startTime+
                '}';
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

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
    
    public Long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }


}