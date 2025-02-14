package me.rkycse.coderush.dto;



import me.rkycse.coderush.entity.TournamentBaseEntity.TournamentType;
import me.rkycse.coderush.entity.TournamentBaseEntity.Visibility;



public class TournamentBaseDTO {
    private Long tournamentId;
    private Long startTime;
    private Boolean rated;
    private Long durationInSeconds;
    private Visibility visibility;
    private String password;
    private TournamentType tournamentType;

    public Long getStartTime() {
        return startTime;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Boolean getRated() {
        return rated;
    }

    public void setRated(Boolean rated) {
        this.rated = rated;
    }

    public Long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
