package me.rkycse.coderush.problemgeneratoragent.dto;
public record AgentProgressEvent(String jobId, int stage, String step, String status, String message, int retryCount, long timestamp) {}
