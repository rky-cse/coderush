package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when login fails (either username doesn't exist OR password is wrong).
 * The message is intentionally generic to prevent username enumeration attacks.
 */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }

    @Override
    public String code() {
        return "INVALID_CREDENTIALS";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }
}
