package me.rkycse.coderush.problemgeneratoragent.exception;
public class StageFailedException extends RuntimeException {
    private final int stage;
    private final String step;
    private final int retryCount;
    public StageFailedException(int stage, String step, String message, int retryCount) {
        super(message);
        this.stage = stage;
        this.step = step;
        this.retryCount = retryCount;
    }
    public int getStage() { return stage; }
    public String getStep() { return step; }
    public int getRetryCount() { return retryCount; }
}
