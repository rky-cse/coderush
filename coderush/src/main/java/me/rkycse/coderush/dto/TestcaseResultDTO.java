package me.rkycse.coderush.dto;



public class TestcaseResultDTO {
    private Long testcaseId;
    private String status;
    private long timeNano;
    private long memoryBytes;

    public TestcaseResultDTO() {
    }

    public TestcaseResultDTO(Long testcaseId, String status, long timeNano, long memoryBytes) {
        this.testcaseId = testcaseId;
        this.status = status;
        this.timeNano = timeNano;
        this.memoryBytes = memoryBytes;
    }

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimeNano() {
        return timeNano;
    }

    public void setTimeNano(long timeNano) {
        this.timeNano = timeNano;
    }

    public long getMemoryBytes() {
        return memoryBytes;
    }

    public void setMemoryBytes(long memoryBytes) {
        this.memoryBytes = memoryBytes;
    }

    @Override
    public String toString() {
        return "TestcaseResultDTO{" +
                "testcaseId='" + testcaseId + '\'' +
                ", status='" + status + '\'' +
                ", timeNano=" + timeNano +
                ", memoryBytes=" + memoryBytes +
                '}';
    }
}

