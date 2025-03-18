package me.rkycse.coderush.dto;

public class ClassicSubmissionResponseDTO {
    private Long id;
    private int index;
    private String username;
    private Long tournamentId;
    private Long questionId;
    private String language;
    private String verdict;//AC,CE,WA,RE,TLE,MLE
    private Long submissionTime; // in milliseconds
    private Long judgingTime;// time when judging starts

    public Long getMaxTimeTaken() {
        return maxTimeTaken;
    }

    public void setMaxTimeTaken(Long maxTimeTaken) {
        this.maxTimeTaken = maxTimeTaken;
    }

    public Long getJudgingTime() {
        return judgingTime;
    }

    public void setJudgingTime(Long judgingTime) {
        this.judgingTime = judgingTime;
    }

    public Long getMaxMemoryUsed() {
        return maxMemoryUsed;
    }

    public void setMaxMemoryUsed(Long maxMemoryUsed) {
        this.maxMemoryUsed = maxMemoryUsed;
    }

    private Long maxTimeTaken;
    private Long maxMemoryUsed;// in milliseconds

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
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


}
