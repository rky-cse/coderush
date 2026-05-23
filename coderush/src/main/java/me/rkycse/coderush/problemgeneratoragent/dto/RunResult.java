package me.rkycse.coderush.problemgeneratoragent.dto;
public record RunResult(String verdict, String output, String errorMessage, long timeMs, long memoryKb) {}
