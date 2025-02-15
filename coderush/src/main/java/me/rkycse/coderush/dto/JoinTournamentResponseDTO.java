package me.rkycse.coderush.dto;


import java.util.HashSet;

public class JoinTournamentResponseDTO {
    private Long tournamentId;
    private TournamentBaseDTO tournament;
    private HashSet<TournamentPlayerDTO>tournamentPlayerList=new HashSet<>();

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public TournamentBaseDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentBaseDTO tournament) {
        this.tournament = tournament;
    }

    public HashSet<TournamentPlayerDTO> getTournamentPlayerList() {
        return tournamentPlayerList;
    }

    public void setTournamentPlayerList(HashSet<TournamentPlayerDTO> tournamentPlayerList) {
        this.tournamentPlayerList = tournamentPlayerList;
    }
}
