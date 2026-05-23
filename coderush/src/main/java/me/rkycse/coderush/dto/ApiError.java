package me.rkycse.coderush.dto;

/**
 * Standard error response envelope returned by the API for any failed request.
 * Frontend reads {@code message} to display to users and {@code code} to drive
 * conditional logic (e.g. auto-redirect on TOKEN_EXPIRED).
 */
public class ApiError {

    private String code;
    private String message;
    private String field;       // optional - for form-validation errors
    private long timestamp;

    public ApiError() {}

    public ApiError(String code, String message, String field, long timestamp) {
        this.code = code;
        this.message = message;
        this.field = field;
        this.timestamp = timestamp;
    }

    public ApiError(String code, String message) {
        this(code, message, null, System.currentTimeMillis());
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
