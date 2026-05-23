package me.rkycse.coderush.problemgeneratoragent.tools.llm;

import me.rkycse.coderush.problemgeneratoragent.dto.CheckerArtifact;
import me.rkycse.coderush.problemgeneratoragent.dto.ProblemDraft;
import me.rkycse.coderush.problemgeneratoragent.llm.LlmClient;
import org.springframework.stereotype.Component;

@Component
public class GenerateCheckerTool {

    private final LlmClient llm;

    public GenerateCheckerTool(LlmClient llm) { this.llm = llm; }

    public CheckerArtifact generate(ProblemDraft draft) {
        String prompt = """
            Write a C++ checker for this competitive programming problem.

            Problem: %s
            Output format: %s
            Sample input: %s
            Sample output: %s

            STRICT REQUIREMENTS:
            1. Start with: #include <bits/stdc++.h>
               Then: using namespace std;
            2. Signature: int main(int argc, char* argv[])
            3. Open files using: ifstream fin(argv[1]); ifstream fout(argv[2]); ifstream ans(argv[3]);
               where argv[1]=input, argv[2]=expected_output, argv[3]=user_output
            4. Read expected output from fout, read user output from ans
            5. Return 0 if user output is correct, 1 if wrong
            6. For problems with a UNIQUE answer: read one value from fout and one from ans, compare them
            7. For problems with MULTIPLE valid answers: implement proper validation logic

            For this problem, the output is a single integer — use EXACT_MATCH (compare integers).

            Return JSON: {"code":"complete C++ checker source code","checkerType":"EXACT_MATCH or CUSTOM"}
            """.formatted(draft.name(), draft.outputFormat(), draft.sampleInput(), draft.sampleOutput());

        return llm.generateStructured(
            "You are an expert at writing competitive programming checkers in C++. Always respond with valid JSON only.",
            prompt, CheckerArtifact.class);
    }

    public CheckerArtifact fix(ProblemDraft draft, String code, String issue) {
        String prompt = """
            Fix this C++ checker. It must:
            - Start with #include <bits/stdc++.h> and using namespace std;
            - Have signature: int main(int argc, char* argv[])
            - Open files: ifstream fin(argv[1]); ifstream fout(argv[2]); ifstream ans(argv[3]);
            - Return 0 if correct, 1 if wrong

            Problem: %s
            Broken code: %s
            Issue: %s
            Return JSON: {"code":"...","checkerType":"EXACT_MATCH or CUSTOM"}
            """.formatted(draft.name(), code, issue);
        return llm.generateStructured(
            "You are an expert at writing competitive programming checkers in C++. Always respond with valid JSON only.",
            prompt, CheckerArtifact.class);
    }
}
