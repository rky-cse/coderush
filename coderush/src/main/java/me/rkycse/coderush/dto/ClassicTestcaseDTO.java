package me.rkycse.coderush.dto;

public class ClassicTestcaseDTO {
    private Long id;
    private Long questionId;
    private String input;
    private String output;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "ClassicTestcaseDTO{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                '}';
    }
}
