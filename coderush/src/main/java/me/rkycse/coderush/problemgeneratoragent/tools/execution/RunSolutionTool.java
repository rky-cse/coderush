package me.rkycse.coderush.problemgeneratoragent.tools.execution;

import me.rkycse.coderush.problemgeneratoragent.dto.RunResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 1 stub: always returns OK with empty output.
 * Sprint 2 will implement real execution via the invocation pipeline.
 */
@Component
public class RunSolutionTool {

    public List<RunResult> runAll(String solutionCode, String language, List<String> inputs) {
        // TODO Sprint 2: wire to invocation pipeline
        List<RunResult> results = new ArrayList<>();
        for (String ignored : inputs) {
            results.add(new RunResult("OK", "0", null, 100L, 1024L));
        }
        return results;
    }
}
