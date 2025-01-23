package me.rkycse.coderush.dto;



public class TestcaseDTO {


    private Long testcaseId;

    private Long questionId;


    private String input;

    private String output;


    private int rating;

    // Getters and Setters
    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

}