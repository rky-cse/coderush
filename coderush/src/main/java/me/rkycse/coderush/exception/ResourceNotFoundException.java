// src/main/java/me/rkycse/coderush/exception/ResourceNotFoundException.java
package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public String code() {
        return "RESOURCE_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
