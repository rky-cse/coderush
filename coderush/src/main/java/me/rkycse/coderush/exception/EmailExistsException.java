package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class EmailExistsException extends ApiException {

    public EmailExistsException() {
        super("An account already uses this email");
    }

    @Override
    public String code() {
        return "EMAIL_EXISTS";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String field() {
        return "email";
    }
}
