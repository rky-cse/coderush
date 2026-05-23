package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class TournamentEndedException extends ApiException {

    public TournamentEndedException() {
        super("This tournament has already ended");
    }

    @Override
    public String code() {
        return "TOURNAMENT_ENDED";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
