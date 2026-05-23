package me.rkycse.coderush.problemgeneratoragent.llm;

public interface LlmClient {
    String generate(String systemPrompt, String userPrompt);
    <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType);
}
