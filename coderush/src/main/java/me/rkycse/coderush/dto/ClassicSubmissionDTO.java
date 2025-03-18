package me.rkycse.coderush.dto;

public class ClassicSubmissionDTO {
    private Long id;
    private int index;
    private String username;
    private Long tournamentId;
    private Long questionId;
    private String code;
    private String language;
    private String verdict;
    private Long submissionTime; // in milliseconds
    private Long executionTime; // in milliseconds
    private Long judgeTime; // in milliseconds

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private Long memory; // in KB or MB

    public ClassicSubmissionDTO() {}

    public ClassicSubmissionDTO(Long id, String username, Long tid, Long questionId, String code, String language, String verdict, Long submissionTime, Long executionTime, Long judgeTime, Long memory) {
        this.id = id;
        this.username = username;
        this.tournamentId = tid;
        this.questionId = questionId;
        this.code = code;
        this.language = language;
        this.verdict = verdict;
        this.submissionTime = submissionTime;
        this.executionTime = executionTime;
        this.judgeTime = judgeTime;
        this.memory = memory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tid) {
        this.tournamentId = tid;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public Long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Long submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getJudgeTime() {
        return judgeTime;
    }

    public void setJudgeTime(Long judgeTime) {
        this.judgeTime = judgeTime;
    }



    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    @Override
    public String toString() {
        return "ClassicSubmissionDTO{" +
                "id=" + id +
                ", index=" + index +
                ", username='" + username + '\'' +
                ", tournamentId=" + tournamentId +
                ", questionId=" + questionId +
                ", code='" + code + '\'' +
                ", language='" + language + '\'' +
                ", verdict='" + verdict + '\'' +
                ", submissionTime=" + submissionTime +
                ", executionTime=" + executionTime +
                ", judgeTime=" + judgeTime +
                ", memory=" + memory +
                '}';
    }
}


