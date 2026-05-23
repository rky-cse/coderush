package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class FileTooLargeException extends ApiException {

    public FileTooLargeException(long maxBytes) {
        super("File exceeds size limit (" + (maxBytes / (1024 * 1024)) + " MB)");
    }

    public FileTooLargeException(String message) {
        super(message);
    }

    @Override
    public String code() {
        return "FILE_TOO_LARGE";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.PAYLOAD_TOO_LARGE;
    }
}
