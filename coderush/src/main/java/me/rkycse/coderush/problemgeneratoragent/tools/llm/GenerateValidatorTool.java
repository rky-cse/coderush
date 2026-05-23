package me.rkycse.coderush.problemgeneratoragent.tools.llm;

import me.rkycse.coderush.problemgeneratoragent.dto.ProblemDraft;
import me.rkycse.coderush.problemgeneratoragent.dto.ValidatorArtifact;
import me.rkycse.coderush.problemgeneratoragent.llm.LlmClient;
import org.springframework.stereotype.Component;

@Component
public class GenerateValidatorTool {

    private final LlmClient llm;

    public GenerateValidatorTool(LlmClient llm) { this.llm = llm; }

    public ValidatorArtifact generate(ProblemDraft draft) {
        String prompt = """
            Write a C++ validator for this problem.
            Reads from stdin. Exits 0 if input is valid, 1 if invalid.
            Must check all constraints.

            CRITICAL RULES:
            - Start with: #include <bits/stdc++.h>
            - Then: using namespace std;
            - Then: int main() { ... }
            - Do NOT print anything to stdout or stderr — only use return 0 or return 1
            - The validator communicates ONLY via exit code, never via cout/cerr

            Input format: %s
            Constraints: %s
            Sample valid input: %s

            Return JSON: {"code":"complete C++ validator source"}
            """.formatted(draft.inputFormat(), draft.constraints(), draft.sampleInput());

        return llm.generateStructured(
            "You are an expert at writing competitive programming validators in C++. Always respond with valid JSON only.",
            prompt, ValidatorArtifact.class);
    }

    public ValidatorArtifact fix(ProblemDraft draft, String code, String issue) {
        String prompt = "Fix this C++ validator.\nConstraints: %s\nInput format: %s\nCode:\n%s\nIssue: %s\nReturn JSON: {\"code\":\"...\"}"
            .formatted(draft.constraints(), draft.inputFormat(), code, issue);
        return llm.generateStructured(
            "You are an expert at writing competitive programming validators in C++. Always respond with valid JSON only.",
            prompt, ValidatorArtifact.class);
    }
}
