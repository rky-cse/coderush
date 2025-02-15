package me.rkycse.coderush.dto;


public class QuestionWithTestcaseDTO {
    private QuestionDTO question;
    private TestcaseDTO testcase;

    public QuestionDTO getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDTO question) {
        this.question = question;
    }

    public TestcaseDTO getTestcase() {
        return testcase;
    }

    public void setTestcase(TestcaseDTO testcase) {
        this.testcase = testcase;
    }
}
