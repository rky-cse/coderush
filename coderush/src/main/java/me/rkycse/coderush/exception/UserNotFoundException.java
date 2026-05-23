package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException() {
        super("User not found");
    }

    public UserNotFoundException(String userName) {
        super("User '" + userName + "' not found");
    }

    @Override
    public String code() {
        return "USER_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
