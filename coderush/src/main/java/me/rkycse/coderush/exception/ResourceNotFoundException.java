// src/main/java/me/rkycse/coderush/exception/ResourceNotFoundException.java
package me.rkycse.coderush.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}