package me.rkycse.coderush.problemgeneratoragent.dto;
import java.util.List;
import java.util.Map;
public record VerificationResult(boolean allInputsRan, Map<Integer, String> verdicts, List<String> outputs, boolean checkerVerified, String correctnessCheck, long maxTimeMs, long maxMemoryKb) {}
