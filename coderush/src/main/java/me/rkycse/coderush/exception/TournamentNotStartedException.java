package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class TournamentNotStartedException extends ApiException {

    public TournamentNotStartedException() {
        super("Tournament has not started yet");
    }

    @Override
    public String code() {
        return "TOURNAMENT_NOT_STARTED";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
