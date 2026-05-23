package me.rkycse.coderush.exception;

import me.rkycse.coderush.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions thrown anywhere in the controller / service layer into
 * the standard {@link ApiError} JSON envelope. Frontend reads the {@code message}
 * to show users and the {@code code} for special handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Our custom typed exceptions. Each carries its own status + code + message.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex) {
        ApiError body = new ApiError(ex.code(), ex.getMessage(), ex.field(), System.currentTimeMillis());
        return ResponseEntity.status(ex.status()).body(body);
    }

    /**
     * Spring Security throws BadCredentialsException when password is wrong,
     * UsernameNotFoundException when user doesn't exist. Both -> generic
     * INVALID_CREDENTIALS to prevent username enumeration.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleBadCredentials(Exception ex) {
        ApiError body = new ApiError("INVALID_CREDENTIALS", "Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Catch-all for unexpected errors. Logs the stack trace server-side but
     * sends only a generic message to the user.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        logger.error("Unhandled exception", ex);
        ApiError body = new ApiError("INTERNAL_ERROR", "Something went wrong. Please try again.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
