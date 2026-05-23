package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all expected, user-visible errors thrown by services/controllers.
 * Subclasses must declare an HTTP status and a stable error code. The message is
 * passed to {@code RuntimeException} and is what the user will see in the UI.
 */
public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }

    /** Stable, machine-readable error code (e.g. INVALID_CREDENTIALS). */
    public abstract String code();

    /** HTTP status to return. */
    public abstract HttpStatus status();

    /** Optional: form field this error pertains to (e.g. "email"). */
    public String field() {
        return null;
    }
}
