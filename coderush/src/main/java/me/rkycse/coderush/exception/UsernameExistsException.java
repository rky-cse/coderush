package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class UsernameExistsException extends ApiException {

    public UsernameExistsException() {
        super("That username is already taken");
    }

    @Override
    public String code() {
        return "USERNAME_EXISTS";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String field() {
        return "userName";
    }
}
