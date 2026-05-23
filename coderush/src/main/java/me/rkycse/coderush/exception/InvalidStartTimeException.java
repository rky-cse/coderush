package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class InvalidStartTimeException extends ApiException {

    public InvalidStartTimeException() {
        super("Start time must be in the future");
    }

    public InvalidStartTimeException(String message) {
        super(message);
    }

    @Override
    public String code() {
        return "INVALID_START_TIME";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String field() {
        return "startTime";
    }
}
