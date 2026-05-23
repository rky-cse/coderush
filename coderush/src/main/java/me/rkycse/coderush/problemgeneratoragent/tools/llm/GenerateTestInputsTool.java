package me.rkycse.coderush.problemgeneratoragent.tools.llm;

import me.rkycse.coderush.problemgeneratoragent.dto.ProblemDraft;
import me.rkycse.coderush.problemgeneratoragent.llm.LlmClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenerateTestInputsTool {

    private final LlmClient llm;

    public GenerateTestInputsTool(LlmClient llm) { this.llm = llm; }

    public List<String> generate(ProblemDraft draft, int count) {
        String prompt = """
            Generate %d diverse test inputs for this competitive programming problem.
            Include: edge cases, min constraints, max constraints, typical cases.

            Input format: %s
            Constraints: %s
            Sample input (for reference): %s

            CRITICAL RULES:
            1. Each input must be EXACTLY in the raw stdin format shown above.
               NOT JSON objects. Just plain numbers/text as the program reads from stdin.
            2. If the first line is a count N, you MUST provide exactly N elements.
               For example if N=5, provide exactly 5 space-separated numbers.
            3. All values must satisfy the constraints.
            4. Keep array/list sizes to at most 20 elements. Do NOT generate N=100 or N=100000
               because you must write out every single element. Test large N with small values
               is not needed — focus on edge cases and correctness.

            Return JSON: {"inputs":["input1","input2",...]}
            Use \\n for newlines within each input string.
            Example: if input is two lines "5" then "1 2 3 4 5", return "5\\n1 2 3 4 5"
            """.formatted(count, draft.inputFormat(), draft.constraints(), draft.sampleInput());

        TestInputsResponse resp = llm.generateStructured(
            "You are an expert at creating test cases for competitive programming. Always respond with valid JSON only.",
            prompt, TestInputsResponse.class);
        return resp.inputs();
    }

    public String regenerateOne(ProblemDraft draft, String failed, String feedback) {
        String prompt = "Generate a valid replacement for this failing test input.\nInput format: %s\nConstraints: %s\nFailed input: %s\nReason: %s\nReturn JSON: {\"input\":\"replacement input\"}"
            .formatted(draft.inputFormat(), draft.constraints(), failed, feedback);
        SingleInputResponse resp = llm.generateStructured(
            "You are an expert at creating test cases for competitive programming. Always respond with valid JSON only.",
            prompt, SingleInputResponse.class);
        return resp.input();
    }

    private record TestInputsResponse(List<String> inputs) {}
    private record SingleInputResponse(String input) {}
}
