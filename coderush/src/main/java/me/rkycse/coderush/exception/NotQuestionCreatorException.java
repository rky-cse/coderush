package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user tries to modify a question they didn't create.
 */
public class NotQuestionCreatorException extends ApiException {

    public NotQuestionCreatorException() {
        super("You can only edit your own questions");
    }

    @Override
    public String code() {
        return "NOT_QUESTION_CREATOR";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }
}
