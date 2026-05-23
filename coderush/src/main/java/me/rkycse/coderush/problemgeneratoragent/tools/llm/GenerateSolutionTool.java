package me.rkycse.coderush.problemgeneratoragent.tools.llm;

import me.rkycse.coderush.problemgeneratoragent.dto.ProblemDraft;
import me.rkycse.coderush.problemgeneratoragent.dto.SolutionArtifact;
import me.rkycse.coderush.problemgeneratoragent.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GenerateSolutionTool {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSolutionTool.class);
    private final LlmClient llm;

    public GenerateSolutionTool(LlmClient llm) { this.llm = llm; }

    public SolutionArtifact generate(ProblemDraft draft, String language) {
        SolutionArtifact raw = llm.generateStructured(system(language), generatePrompt(draft, language), SolutionArtifact.class);
        String sanitized = sanitizeCode(raw.code());
        logger.info("Generated solution ({}):\n---\n{}\n---", language, sanitized);
        return new SolutionArtifact(sanitized, raw.language(), raw.explanation(), raw.complexity());
    }

    public SolutionArtifact fix(ProblemDraft draft, String language, String code, String error) {
        String prompt = "Fix this %s solution.\nProblem: %s\nCode:\n%s\nError: %s\nReturn JSON: {\"code\":\"...\",\"explanation\":\"...\",\"complexity\":\"...\"}"
            .formatted(language, fmt(draft), code, error);
        SolutionArtifact raw = llm.generateStructured(system(language), prompt, SolutionArtifact.class);
        return new SolutionArtifact(sanitizeCode(raw.code()), raw.language(), raw.explanation(), raw.complexity());
    }

    public SolutionArtifact fixWithIssue(ProblemDraft draft, String language, String code, String issue) {
        String prompt = "Fix this %s solution.\nProblem: %s\nCode:\n%s\nIssue: %s\nReturn JSON: {\"code\":\"...\",\"explanation\":\"...\",\"complexity\":\"...\"}"
            .formatted(language, fmt(draft), code, issue);
        SolutionArtifact raw = llm.generateStructured(system(language), prompt, SolutionArtifact.class);
        return new SolutionArtifact(sanitizeCode(raw.code()), raw.language(), raw.explanation(), raw.complexity());
    }

    /**
     * Strips common LLM artifacts from generated code:
     * - Leading/trailing whitespace
     * - Stray opening braces before #include
     * - Markdown fences that slipped through JSON parsing
     */
    private String sanitizeCode(String code) {
        if (code == null) return "";
        String s = code.trim();
        // Remove stray leading brace before #include
        if (s.startsWith("{") && s.contains("#include")) {
            s = s.substring(1).trim();
        }
        // Fix char literals split by real newlines: '\n' (literal) -> '\n' (escape)
        s = s.replace("'\n'", "'\\n'");
        s = s.replace("'\t'", "'\\t'");
        s = s.replace("'\r'", "'\\r'");
        return s;
    }

    private String system(String lang) {
        return "You are an expert competitive programmer. Write correct, efficient " + lang + " solutions. Always respond with valid JSON only.";
    }

    private String generatePrompt(ProblemDraft d, String lang) {
        return """
            Write a correct, efficient %s solution for:
            %s
            CRITICAL REQUIREMENTS:
            - The code must be COMPLETE and COMPILABLE — include all braces, all closing brackets
            - The code must print the answer using cout/printf/print
            - NO markdown fences, NO ```cpp, NO ``` — raw source code only
            - For C++: must start with #include and end with closing } for main
            Return JSON: {"code":"complete compilable source code","explanation":"algorithm used","complexity":"time and space complexity"}
            """.formatted(lang, fmt(d));
    }

    private String fmt(ProblemDraft d) {
        return d.name() + "\n" + d.legend() + "\nInput: " + d.inputFormat() + "\nOutput: " + d.outputFormat() + "\nConstraints: " + d.constraints();
    }
}
