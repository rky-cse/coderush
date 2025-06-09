package me.rkycse.judge.dto;

import java.util.List;

public class InvocationPayload {
    private Long questionId;
    private String checkerFilePath;
    private String validatorFilePath;
    private String solutionFilePath;
    private List<TestcaseInfo> testcases;

    public InvocationPayload() {}

    public InvocationPayload(Long questionId, String checkerFilePath, String validatorFilePath,
                             String solutionFilePath, List<TestcaseInfo> testcases) {
        this.questionId = questionId;
        this.checkerFilePath = checkerFilePath;
        this.validatorFilePath = validatorFilePath;
        this.solutionFilePath = solutionFilePath;
        this.testcases = testcases;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getCheckerFilePath() { return checkerFilePath; }
    public void setCheckerFilePath(String checkerFilePath) { this.checkerFilePath = checkerFilePath; }

    public String getValidatorFilePath() { return validatorFilePath; }
    public void setValidatorFilePath(String validatorFilePath) { this.validatorFilePath = validatorFilePath; }

    public String getSolutionFilePath() { return solutionFilePath; }
    public void setSolutionFilePath(String solutionFilePath) { this.solutionFilePath = solutionFilePath; }

    public List<TestcaseInfo> getTestcases() { return testcases; }
    public void setTestcases(List<TestcaseInfo> testcases) { this.testcases = testcases; }

    public static class TestcaseInfo {
        private String inputFilePath;
        private String outputFilePath;
        private Long testcaseId;

        public TestcaseInfo() {}

        public TestcaseInfo(String inputFilePath, String outputFilePath, Long testcaseId) {
            this.inputFilePath = inputFilePath;
            this.outputFilePath = outputFilePath;
            this.testcaseId = testcaseId;
        }

        public String getInputFilePath() { return inputFilePath; }
        public void setInputFilePath(String inputFilePath) { this.inputFilePath = inputFilePath; }

        public String getOutputFilePath() { return outputFilePath; }
        public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }
        public Long getTestcaseId() { return testcaseId; }
        public void setTestcaseId(Long testcaseId) { this.testcaseId = testcaseId; }
    }
}
