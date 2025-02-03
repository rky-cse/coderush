package me.rkycse.coderush.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestcaseDTO {

    private Long testcaseId;
    private Long questionId;
    private String input;
    private String output;
    private int rating;
    @Override
    public String toString() {
        return "TestcaseDTO{" +
                "testcaseId=" + testcaseId +
                ", questionId=" + questionId +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", rating=" + rating +
                '}';
    }


}