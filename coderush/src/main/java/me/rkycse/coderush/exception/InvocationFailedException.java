package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class InvocationFailedException extends ApiException {

    public InvocationFailedException(String message) {
        super(message);
    }

    @Override
    public String code() {
        return "INVOCATION_FAILED";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
