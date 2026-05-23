package me.rkycse.coderush.problemgeneratoragent.tools.llm;

import me.rkycse.coderush.problemgeneratoragent.dto.GenerationRequest;
import me.rkycse.coderush.problemgeneratoragent.dto.ProblemDraft;
import me.rkycse.coderush.problemgeneratoragent.llm.LlmClient;
import org.springframework.stereotype.Component;

@Component
public class DraftProblemTool {

    private final LlmClient llm;

    public DraftProblemTool(LlmClient llm) { this.llm = llm; }

    public ProblemDraft draft(GenerationRequest req) {
        String system = "You are an expert competitive programming problem setter. " +
            "Always respond with valid JSON only. No markdown, no text outside JSON.";

        String user = """
            Create a %s difficulty competitive programming problem about: %s
            Solution language: %s
            %s

            Respond with this exact JSON:
            {
              "name": "short title",
              "legend": "problem narrative",
              "inputFormat": "input format description",
              "outputFormat": "output format description",
              "constraints": "all variable constraints",
              "sampleInput": "a concrete example input",
              "sampleOutput": "correct output for sample input",
              "notes": "optional hints or edge cases"
            }
            """.formatted(
                req.difficulty(), req.topic(), req.language(),
                req.additionalNotes() != null && !req.additionalNotes().isBlank()
                    ? "Additional requirements: " + req.additionalNotes() : ""
            );

        return llm.generateStructured(system, user, ProblemDraft.class);
    }
}
