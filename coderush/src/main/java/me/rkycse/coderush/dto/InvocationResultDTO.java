package me.rkycse.coderush.dto;



import java.util.List;

public class InvocationResultDTO {
    private Long questionId;
    private String verdict;
    private long elapsedMillis;
    private List<TestcaseResultDTO> testcaseResults;

    public InvocationResultDTO() {
    }

    public InvocationResultDTO(Long questionId, String verdict, long elapsedMillis, List<TestcaseResultDTO> testcaseResults) {
        this.questionId = questionId;
        this.verdict = verdict;
        this.elapsedMillis = elapsedMillis;
        this.testcaseResults = testcaseResults;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(long elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }

    public List<TestcaseResultDTO> getTestcaseResults() {
        return testcaseResults;
    }

    public void setTestcaseResults(List<TestcaseResultDTO> testcaseResults) {
        this.testcaseResults = testcaseResults;
    }

    @Override
    public String toString() {
        return "InvocationResultDTO{" +
                "questionId=" + questionId +
                ", verdict='" + verdict + '\'' +
                ", elapsedMillis=" + elapsedMillis +
                ", testcaseResults=" + testcaseResults +
                '}';
    }
}
