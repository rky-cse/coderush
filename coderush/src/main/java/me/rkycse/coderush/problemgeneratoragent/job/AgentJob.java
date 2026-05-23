package me.rkycse.coderush.problemgeneratoragent.job;

import me.rkycse.coderush.problemgeneratoragent.dto.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.ArrayList;
import java.util.List;

@RedisHash("agent_job")
public class AgentJob {

    @Id
    private String jobId;
    private AgentMode mode;
    private AgentJobStatus status;
    private int currentStage;
    private Integer currentCheckpoint;
    private String createdByUsername;  // username of the user who started the job

    // Artifacts
    private ProblemDraft draft;
    private SolutionArtifact solution;
    private ValidatorArtifact validator;
    private CheckerArtifact checker;
    private TestInputsArtifact testInputs;
    private VerificationResult verification;

    private List<AgentProgressEvent> progress = new ArrayList<>();

    // Terminal state
    private Long questionId;
    private String errorReason;

    private long createdAt;
    private long updatedAt;

    @TimeToLive
    private long ttl = 3600L;

    // Getters and setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public AgentMode getMode() { return mode; }
    public void setMode(AgentMode mode) { this.mode = mode; }
    public AgentJobStatus getStatus() { return status; }
    public void setStatus(AgentJobStatus status) { this.status = status; }
    public int getCurrentStage() { return currentStage; }
    public void setCurrentStage(int currentStage) { this.currentStage = currentStage; }
    public Integer getCurrentCheckpoint() { return currentCheckpoint; }
    public void setCurrentCheckpoint(Integer currentCheckpoint) { this.currentCheckpoint = currentCheckpoint; }
    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
    public ProblemDraft getDraft() { return draft; }
    public void setDraft(ProblemDraft draft) { this.draft = draft; }
    public SolutionArtifact getSolution() { return solution; }
    public void setSolution(SolutionArtifact solution) { this.solution = solution; }
    public ValidatorArtifact getValidator() { return validator; }
    public void setValidator(ValidatorArtifact validator) { this.validator = validator; }
    public CheckerArtifact getChecker() { return checker; }
    public void setChecker(CheckerArtifact checker) { this.checker = checker; }
    public TestInputsArtifact getTestInputs() { return testInputs; }
    public void setTestInputs(TestInputsArtifact testInputs) { this.testInputs = testInputs; }
    public VerificationResult getVerification() { return verification; }
    public void setVerification(VerificationResult verification) { this.verification = verification; }
    public List<AgentProgressEvent> getProgress() { return progress; }
    public void setProgress(List<AgentProgressEvent> progress) { this.progress = progress; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }
}
