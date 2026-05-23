package me.rkycse.coderush.problemgeneratoragent.dto;
public record GenerationRequest(String topic, String difficulty, String language, String additionalNotes, AgentMode mode) {}
