package me.rkycse.coderush.exception;

import org.springframework.http.HttpStatus;

public class QuestionNotFoundException extends ApiException {

    public QuestionNotFoundException() {
        super("Question not found");
    }

    public QuestionNotFoundException(Long questionId) {
        super("Question " + questionId + " not found");
    }

    @Override
    public String code() {
        return "QUESTION_NOT_FOUND";
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
