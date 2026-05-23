package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class InvalidDurationException extends ApiException {

    public InvalidDurationException() {
        super("Duration must be greater than zero");
    }

    public InvalidDurationException(String message) {
        super(message);
    }

    @Override
    public String code() {
        return "INVALID_DURATION";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String field() {
        return "durationInSeconds";
    }
}
