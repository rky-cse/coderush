package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class TournamentNotFoundException extends ApiException {

    public TournamentNotFoundException() {
        super("Tournament not found");
    }

    public TournamentNotFoundException(Long tournamentId) {
        super("Tournament " + tournamentId + " not found");
    }

    @Override
    public String code() {
        return "TOURNAMENT_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
