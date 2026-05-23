package me.rkycse.coderush.problemgeneratoragent.agent;

import me.rkycse.coderush.problemgeneratoragent.dto.*;
import me.rkycse.coderush.problemgeneratoragent.exception.LlmException;
import me.rkycse.coderush.problemgeneratoragent.exception.StageFailedException;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJob;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJobRepository;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJobStatus;
import me.rkycse.coderush.problemgeneratoragent.sse.SseEmitterRegistry;
import me.rkycse.coderush.problemgeneratoragent.tools.execution.RunCheckerTool;
import me.rkycse.coderush.problemgeneratoragent.tools.execution.RunSolutionTool;
import me.rkycse.coderush.problemgeneratoragent.tools.execution.RunValidatorTool;
import me.rkycse.coderush.problemgeneratoragent.tools.llm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

@Component
public class AgentPipeline {

    private static final Logger logger = LoggerFactory.getLogger(AgentPipeline.class);

    private final DraftProblemTool draftTool;
    private final GenerateSolutionTool solutionTool;
    private final GenerateValidatorTool validatorTool;
    private final GenerateCheckerTool checkerTool;
    private final GenerateTestInputsTool testInputsTool;
    private final RunValidatorTool runValidatorTool;
    private final RunSolutionTool runSolutionTool;
    private final RunCheckerTool runCheckerTool;
    private final AgentJobRepository jobRepo;
    private final SseEmitterRegistry sseRegistry;

    @Value("${agent.retry.llm:3}")     private int MAX_LLM;
    @Value("${agent.retry.input:2}")   private int MAX_INPUT;
    @Value("${agent.retry.verify:2}")  private int MAX_VERIFY;
    @Value("${agent.checkpoint.timeout.ms:1800000}") private long CHECKPOINT_TIMEOUT;

    public AgentPipeline(DraftProblemTool draftTool, GenerateSolutionTool solutionTool,
                          GenerateValidatorTool validatorTool, GenerateCheckerTool checkerTool,
                          GenerateTestInputsTool testInputsTool,
                          RunValidatorTool runValidatorTool, RunSolutionTool runSolutionTool,
                          RunCheckerTool runCheckerTool, AgentJobRepository jobRepo,
                          SseEmitterRegistry sseRegistry) {
        this.draftTool = draftTool; this.solutionTool = solutionTool;
        this.validatorTool = validatorTool; this.checkerTool = checkerTool;
        this.testInputsTool = testInputsTool;
        this.runValidatorTool = runValidatorTool; this.runSolutionTool = runSolutionTool;
        this.runCheckerTool = runCheckerTool; this.jobRepo = jobRepo;
        this.sseRegistry = sseRegistry;
    }

    public void run(String jobId, GenerationRequest request) {
        AgentJob job = load(jobId);
        job.setStatus(AgentJobStatus.RUNNING);
        save(job);
        try {
            stage1(job, request);
            checkpoint(job, 1);
            stage2(job, request.language());
            checkpoint(job, 2);
            stage3(job);
            checkpoint(job, 3);
            stage4(job);
            checkpoint(job, 4);
            // Stage 5 is called by the service after checkpoint 4
            job.setStatus(AgentJobStatus.DONE);
            save(job);
            emit(job, 5, "COMPLETE", "DONE", "Pipeline complete. Saving...", 0);
        } catch (StageFailedException e) {
            job.setStatus(AgentJobStatus.FAILED);
            job.setErrorReason(e.getMessage());
            save(job);
            emit(job, e.getStage(), e.getStep(), "FAILED", e.getMessage(), e.getRetryCount());
        }
    }

    // ─── Stage 1 ──────────────────────────────────────────────────────────────

    private void stage1(AgentJob job, GenerationRequest req) {
        job.setCurrentStage(1);
        emit(job, 1, "DRAFT_PROBLEM", "RUNNING", "Generating problem draft...", 0);
        ProblemDraft draft = retryLlm(() -> draftTool.draft(req), MAX_LLM, 1, "DRAFT_PROBLEM");
        job.setDraft(draft);
        save(job);
        emit(job, 1, "DRAFT_PROBLEM", "DONE", "Problem drafted: \"" + draft.name() + "\"", 0);
    }

    // ─── Stage 2 ──────────────────────────────────────────────────────────────

    private void stage2(AgentJob job, String lang) {
        job.setCurrentStage(2);

        emit(job, 2, "GENERATE_SOLUTION", "RUNNING", "Generating " + lang + " solution...", 0);
        SolutionArtifact sol = retryLlm(() -> solutionTool.generate(job.getDraft(), lang), MAX_LLM, 2, "GENERATE_SOLUTION");
        job.setSolution(sol);
        save(job);
        emit(job, 2, "GENERATE_SOLUTION", "DONE", "Solution generated.", 0);

        emit(job, 2, "GENERATE_VALIDATOR", "RUNNING", "Generating validator...", 0);
        ValidatorArtifact val = retryLlm(() -> validatorTool.generate(job.getDraft()), MAX_LLM, 2, "GENERATE_VALIDATOR");
        job.setValidator(val);
        save(job);
        emit(job, 2, "GENERATE_VALIDATOR", "DONE", "Validator generated.", 0);

        emit(job, 2, "GENERATE_CHECKER", "RUNNING", "Generating checker...", 0);
        CheckerArtifact chk = retryLlm(() -> checkerTool.generate(job.getDraft()), MAX_LLM, 2, "GENERATE_CHECKER");
        job.setChecker(chk);
        save(job);
        emit(job, 2, "GENERATE_CHECKER", "DONE", "Checker generated. Type: " + chk.checkerType(), 0);
    }

    // ─── Stage 3 ──────────────────────────────────────────────────────────────

    private void stage3(AgentJob job) {
        job.setCurrentStage(3);
        emit(job, 3, "GENERATE_INPUTS", "RUNNING", "Generating test inputs...", 0);

        List<String> raw = retryLlm(() -> testInputsTool.generate(job.getDraft(), 10), MAX_LLM, 3, "GENERATE_INPUTS");
        if (raw == null || raw.size() < 3) throw new StageFailedException(3, "GENERATE_INPUTS", "LLM returned fewer than 3 inputs.", MAX_LLM);

        Map<Integer, InputStatus> statuses = new LinkedHashMap<>();
        List<String> valid = new ArrayList<>();

        for (int i = 0; i < raw.size(); i++) {
            String input = raw.get(i);
            boolean ok = runValidatorTool.validate(job.getValidator().code(), input);
            int r = 0;
            while (!ok && r < MAX_INPUT) {
                r++;
                emit(job, 3, "VALIDATE_INPUT_" + i, "RETRYING", "Input " + i + " failed, regenerating (attempt " + r + ")...", r);
                final String failed = input;
                input = retryLlm(() -> testInputsTool.regenerateOne(job.getDraft(), failed, "Input failed validator."), 1, 3, "REGEN_INPUT_" + i);
                ok = runValidatorTool.validate(job.getValidator().code(), input);
            }
            if (ok) {
                valid.add(input);
                statuses.put(i, r > 0 ? InputStatus.FAIL_REGENERATED : InputStatus.PASS);
            } else {
                statuses.put(i, InputStatus.SKIPPED);
                emit(job, 3, "VALIDATE_INPUT_" + i, "SKIPPED", "Input " + i + " skipped.", MAX_INPUT);
            }
        }

        if (valid.size() < 3) throw new StageFailedException(3, "VALIDATE_INPUTS", "Only " + valid.size() + " valid inputs. Minimum is 3.", 0);

        job.setTestInputs(new TestInputsArtifact(valid, statuses, valid.size()));
        save(job);
        emit(job, 3, "GENERATE_INPUTS", "DONE", valid.size() + "/" + raw.size() + " inputs validated.", 0);
    }

    // ─── Stage 4 ──────────────────────────────────────────────────────────────

    private void stage4(AgentJob job) {
        job.setCurrentStage(4);
        emit(job, 4, "RUN_SOLUTION", "RUNNING", "Running solution on " + job.getTestInputs().validatedCount() + " inputs...", 0);

        for (int cycle = 0; cycle < MAX_VERIFY; cycle++) {
            List<RunResult> results = runSolutionTool.runAll(job.getSolution().code(), job.getSolution().language(), job.getTestInputs().inputs());

            long tleCount = results.stream().filter(r -> "TLE".equals(r.verdict())).count();
            long reCount  = results.stream().filter(r -> "RE".equals(r.verdict())).count();

            if (tleCount > results.size() / 2) {
                emit(job, 4, "RUN_SOLUTION", "RETRYING", "TLE on " + tleCount + " inputs. Optimizing... (cycle " + (cycle+1) + ")", cycle+1);
                String tleInput = firstWithVerdict(results, job.getTestInputs().inputs(), "TLE");
                final SolutionArtifact cur = job.getSolution();
                SolutionArtifact fixed = retryLlm(() -> solutionTool.fixWithIssue(job.getDraft(), cur.language(), cur.code(), "TLE on: " + trunc(tleInput, 100)), MAX_LLM, 4, "FIX_TLE");
                job.setSolution(fixed); save(job); continue;
            }
            if (reCount > 0) {
                emit(job, 4, "RUN_SOLUTION", "RETRYING", "RE on " + reCount + " inputs. Fixing... (cycle " + (cycle+1) + ")", cycle+1);
                final SolutionArtifact cur = job.getSolution();
                SolutionArtifact fixed = retryLlm(() -> solutionTool.fixWithIssue(job.getDraft(), cur.language(), cur.code(), "Runtime error on some inputs."), MAX_LLM, 4, "FIX_RE");
                job.setSolution(fixed); save(job); continue;
            }

            List<String> outputs = results.stream().map(RunResult::output).toList();

            // Checker sanity on outputs
            boolean checkerOk = true;
            for (int i = 0; i < job.getTestInputs().inputs().size(); i++) {
                if (!runCheckerTool.check(job.getChecker().code(), job.getTestInputs().inputs().get(i), outputs.get(i), outputs.get(i))) {
                    checkerOk = false; break;
                }
            }
            if (!checkerOk) {
                emit(job, 4, "VERIFY_CHECKER", "RETRYING", "Checker rejected its own output. Regenerating checker...", cycle+1);
                // Regenerate checker inline
                CheckerArtifact newChk = retryLlm(() -> checkerTool.generate(job.getDraft()), MAX_LLM, 4, "REGEN_CHECKER");
                job.setChecker(newChk);
                save(job);
                continue;
            }

            long maxTime = results.stream().mapToLong(RunResult::timeMs).max().orElse(0);
            long maxMem  = results.stream().mapToLong(RunResult::memoryKb).max().orElse(0);
            Map<Integer, String> verdicts = new LinkedHashMap<>();
            for (int i = 0; i < results.size(); i++) verdicts.put(i, results.get(i).verdict());

            job.setVerification(new VerificationResult(true, verdicts, outputs, true, "CORRECT", maxTime, maxMem));
            save(job);
            emit(job, 4, "RUN_SOLUTION", "DONE", "All " + outputs.size() + " inputs verified. Max time: " + maxTime + "ms.", 0);
            return;
        }
        throw new StageFailedException(4, "RUN_SOLUTION", "Could not produce a passing solution after " + MAX_VERIFY + " cycles.", MAX_VERIFY);
    }

    // ─── Checkpoint ───────────────────────────────────────────────────────────

    private void checkpoint(AgentJob job, int cp) {
        if (job.getMode() == AgentMode.AUTO) return;

        job.setStatus(AgentJobStatus.AWAITING_HUMAN_INPUT);
        job.setCurrentCheckpoint(cp);
        save(job);
        emit(job, cp, "CHECKPOINT_" + cp, "AWAITING", "Waiting for review at checkpoint " + cp + ".", 0);

        long deadline = System.currentTimeMillis() + CHECKPOINT_TIMEOUT;
        while (System.currentTimeMillis() < deadline) {
            AgentJob current = load(job.getJobId());
            if (current.getStatus() == AgentJobStatus.RUNNING) {
                // Reload edited artifacts
                job.setDraft(current.getDraft()); job.setSolution(current.getSolution());
                job.setValidator(current.getValidator()); job.setChecker(current.getChecker());
                job.setTestInputs(current.getTestInputs());
                return;
            }
            if (current.getStatus() == AgentJobStatus.ABORTED)
                throw new StageFailedException(cp, "CHECKPOINT", "Aborted by user.", 0);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        throw new StageFailedException(cp, "CHECKPOINT", "Timed out waiting for approval.", 0);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private <T> T retryLlm(Supplier<T> call, int max, int stage, String step) {
        LlmException last = null;
        for (int i = 1; i <= max; i++) {
            try { return call.get(); }
            catch (LlmException e) {
                last = e;
                if (i < max) {
                    emit(null, stage, step, "RETRYING", "LLM failed (attempt " + i + "/" + max + "): " + trunc(e.getMessage(), 60), i);
                    try { Thread.sleep(1000L * i); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new StageFailedException(stage, step, "LLM failed after " + max + " attempts: " + (last != null ? last.getMessage() : ""), max);
    }

    private void emit(AgentJob job, int stage, String step, String status, String message, int retryCount) {
        AgentProgressEvent event = new AgentProgressEvent(
            job != null ? job.getJobId() : "unknown", stage, step, status, message, retryCount, System.currentTimeMillis()
        );
        if (job != null) { job.getProgress().add(event); save(job); }
        sseRegistry.broadcast(job != null ? job.getJobId() : "unknown", event);
    }

    private AgentJob load(String jobId) {
        return jobRepo.findById(jobId).orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
    }

    private void save(AgentJob job) {
        job.setUpdatedAt(System.currentTimeMillis());
        jobRepo.save(job);
    }

    private String firstWithVerdict(List<RunResult> results, List<String> inputs, String verdict) {
        for (int i = 0; i < results.size(); i++)
            if (verdict.equals(results.get(i).verdict())) return inputs.get(i);
        return "";
    }

    private String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
