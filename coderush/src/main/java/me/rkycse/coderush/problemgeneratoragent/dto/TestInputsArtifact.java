package me.rkycse.coderush.problemgeneratoragent.dto;
import java.util.List;
import java.util.Map;
public record TestInputsArtifact(List<String> inputs, Map<Integer, InputStatus> validationStatus, int validatedCount) {}
