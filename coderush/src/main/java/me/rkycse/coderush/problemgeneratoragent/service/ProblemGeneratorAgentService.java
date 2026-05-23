package me.rkycse.coderush.problemgeneratoragent.service;

import me.rkycse.coderush.dto.QuestionDTO;
import me.rkycse.coderush.entity.CheckerValidatorSolutionEntity;
import me.rkycse.coderush.entity.ClassicTestcaseEntity;
import me.rkycse.coderush.exception.ResourceNotFoundException;
import me.rkycse.coderush.problemgeneratoragent.agent.AgentPipeline;
import me.rkycse.coderush.problemgeneratoragent.dto.*;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJob;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJobRepository;
import me.rkycse.coderush.problemgeneratoragent.job.AgentJobStatus;
import me.rkycse.coderush.repository.CheckerValidatorSolutionRepository;
import me.rkycse.coderush.repository.ClassicTestcaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;

@Service
public class ProblemGeneratorAgentService {

    private final AgentPipeline pipeline;
    private final AgentJobRepository jobRepo;
    private final Executor taskExecutor;
    private final me.rkycse.coderush.repository.QuestionRepository questionRepository;
    private final me.rkycse.coderush.repository.UserRepository userRepository;
    private final ClassicTestcaseRepository testcaseRepository;
    private final CheckerValidatorSolutionRepository cvsRepository;
    private final me.rkycse.coderush.service.InvocationService invocationService;
    private final org.springframework.kafka.core.ConsumerFactory<String, String> consumerFactory;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final Path FILES_BASE =
        Paths.get(System.getProperty("user.dir")).getParent().resolve("files");

    public ProblemGeneratorAgentService(AgentPipeline pipeline, AgentJobRepository jobRepo,
                                         Executor taskExecutor,
                                         me.rkycse.coderush.repository.QuestionRepository questionRepository,
                                         me.rkycse.coderush.repository.UserRepository userRepository,
                                         ClassicTestcaseRepository testcaseRepository,
                                         CheckerValidatorSolutionRepository cvsRepository,
                                         me.rkycse.coderush.service.InvocationService invocationService,
                                         org.springframework.kafka.core.ConsumerFactory<String, String> consumerFactory,
                                         com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.pipeline = pipeline; this.jobRepo = jobRepo; this.taskExecutor = taskExecutor;
        this.questionRepository = questionRepository; this.userRepository = userRepository;
        this.testcaseRepository = testcaseRepository; this.cvsRepository = cvsRepository;
        this.invocationService = invocationService; this.consumerFactory = consumerFactory;
        this.objectMapper = objectMapper;
    }

    public String startGeneration(GenerationRequest request) {
        // Capture the current user on the HTTP thread (SecurityContext is available here)
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();

        AgentJob job = new AgentJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setMode(request.mode());
        job.setStatus(AgentJobStatus.CREATED);
        job.setCurrentStage(0);
        job.setProgress(new ArrayList<>());
        job.setCreatedByUsername(username);
        job.setCreatedAt(System.currentTimeMillis());
        job.setUpdatedAt(System.currentTimeMillis());
        jobRepo.save(job);

        taskExecutor.execute(() -> {
            pipeline.run(job.getJobId(), request);
            // After pipeline marks DONE, save the question
            AgentJob finished = jobRepo.findById(job.getJobId()).orElse(null);
            if (finished != null && finished.getStatus() == AgentJobStatus.DONE && finished.getQuestionId() == null) {
                try {
                    Long qid = saveQuestion(finished);
                    finished.setQuestionId(qid);
                    finished.setUpdatedAt(System.currentTimeMillis());
                    jobRepo.save(finished);
                } catch (Exception e) {
                    finished.setStatus(AgentJobStatus.FAILED);
                    finished.setErrorReason("Save failed: " + e.getMessage());
                    finished.setUpdatedAt(System.currentTimeMillis());
                    jobRepo.save(finished);
                }
            }
        });

        return job.getJobId();
    }

    public AgentJob getJob(String jobId) {
        return jobRepo.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found or expired: " + jobId));
    }

    public void approve(String jobId, Map<String, Object> edits) {
        AgentJob job = getJob(jobId);
        if (job.getStatus() != AgentJobStatus.AWAITING_HUMAN_INPUT)
            throw new IllegalStateException("Job is not awaiting approval");
        if (edits != null && !edits.isEmpty()) applyEdits(job, edits);
        job.setStatus(AgentJobStatus.RUNNING);
        job.setCurrentCheckpoint(null);
        job.setUpdatedAt(System.currentTimeMillis());
        jobRepo.save(job);
    }

    public void regenerate(String jobId, int fromStage) {
        AgentJob job = getJob(jobId);
        clearFrom(job, fromStage);
        job.setStatus(AgentJobStatus.RUNNING);
        job.setCurrentCheckpoint(null);
        job.setUpdatedAt(System.currentTimeMillis());
        jobRepo.save(job);
        // Note: the original thread will wake up and exit; new thread takes over
        // For Sprint 1 we restart the full pipeline from scratch
        taskExecutor.execute(() -> pipeline.run(job.getJobId(), null));
    }

    public void abort(String jobId) {
        AgentJob job = getJob(jobId);
        job.setStatus(AgentJobStatus.ABORTED);
        job.setUpdatedAt(System.currentTimeMillis());
        jobRepo.save(job);
    }

    public Long saveQuestion(AgentJob job) throws IOException {
        // Look up the creator by username (stored when the job was created on the HTTP thread)
        me.rkycse.coderush.entity.UserEntity creator = userRepository
            .findByUserName(job.getCreatedByUsername())
            .orElseThrow(() -> new RuntimeException("Creator not found: " + job.getCreatedByUsername()));

        // Create the question entity directly — bypasses SecurityContext
        me.rkycse.coderush.entity.QuestionEntity q = new me.rkycse.coderush.entity.QuestionEntity();
        q.setCreatorId(creator.getId());
        q.setName(job.getDraft().name());
        q.setLegend(job.getDraft().legend());
        q.setInputFormat(job.getDraft().inputFormat());
        q.setOutputFormat(job.getDraft().outputFormat());
        q.setNotes(job.getDraft().notes() != null ? job.getDraft().notes() : "");
        q.setTutorial("");
        q.setRated(false);
        q.setFreeStyle(false);
        me.rkycse.coderush.entity.QuestionEntity saved = questionRepository.save(q);
        Long qid = saved.getQuestionId();

        List<String> inputs  = job.getTestInputs().inputs();
        List<String> outputs = job.getVerification().outputs();
        for (int i = 0; i < inputs.size(); i++) writeTestCase(qid, inputs.get(i), outputs.get(i));

        writeCodeFile(qid, "solution",  job.getSolution().code(),  "solution." + (job.getSolution().language() != null ? job.getSolution().language() : "cpp"));
        writeCodeFile(qid, "validator", job.getValidator().code(), "validator.cpp");

        // Only write checker for CUSTOM type — EXACT_MATCH uses the judge's built-in string comparison
        if (job.getChecker().checkerType() == me.rkycse.coderush.problemgeneratoragent.dto.CheckerType.CUSTOM) {
            writeCodeFile(qid, "checker", job.getChecker().code(), "checker.cpp");
        }
        // For EXACT_MATCH: ensure the CheckerValidatorSolutionEntity exists with empty checker path
        // so InvocationService doesn't get a null checkerFilePath
        else {
            ensureCvsEntityExists(qid);
        }

        // Sprint 2: trigger real invocation — compiles solution, runs against all inputs, writes real outputs
        runInvocationAndWait(qid);

        return qid;
    }

    /**
     * Triggers the existing invocation pipeline and waits up to 5 minutes for it to complete.
     * The judge compiles the solution, runs it against all test inputs, and writes real outputs.
     * Throws RuntimeException with the error message if invocation fails (e.g. compile error).
     */
    private void runInvocationAndWait(Long questionId) {
        invocationService.handleInvocation(questionId);

        org.apache.kafka.clients.consumer.Consumer<String, String> consumer = consumerFactory.createConsumer();
        consumer.subscribe(java.util.Collections.singletonList("invocation-result"));
        long deadline = System.currentTimeMillis() + 60 * 1000L; // 60 second timeout

        try {
            while (System.currentTimeMillis() < deadline) {
                org.apache.kafka.clients.consumer.ConsumerRecords<String, String> records =
                    consumer.poll(java.time.Duration.ofSeconds(2));
                for (org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record : records) {
                    if (record.value().contains("\"questionId\":" + questionId)) {
                        try {
                            me.rkycse.coderush.dto.InvocationResultDTO result =
                                objectMapper.readValue(record.value(), me.rkycse.coderush.dto.InvocationResultDTO.class);
                            if (!"OK".equals(result.getVerdict())) {
                                // Surface the error so the pipeline can retry with the error as context
                                throw new RuntimeException("Invocation failed: " + result.getVerdict());
                            }
                        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                            // Couldn't parse result but outputs are written — treat as success
                        }
                        return;
                    }
                }
            }
            throw new RuntimeException("Invocation timed out after 60 seconds for questionId=" + questionId + ". The solution likely has a compile error or infinite loop.");
        } finally {
            consumer.close();
        }
    }

    private void writeTestCase(Long qid, String input, String output) throws IOException {
        ClassicTestcaseEntity e = new ClassicTestcaseEntity();
        e.setQuestionId(qid); e.setInputFilePath(""); e.setOutputFilePath("");
        e = testcaseRepository.save(e);

        Path inDir = FILES_BASE.resolve(Paths.get(qid.toString(), "testcases", e.getId().toString(), "input"));
        Files.createDirectories(inDir);
        Path inFile = inDir.resolve("input.txt");
        Files.writeString(inFile, input);

        Path outDir = FILES_BASE.resolve(Paths.get(qid.toString(), "testcases", e.getId().toString(), "output"));
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve("output.txt");
        Files.writeString(outFile, output);

        e.setInputFilePath(FILES_BASE.relativize(inFile).toString().replace("\\", "/"));
        e.setOutputFilePath(FILES_BASE.relativize(outFile).toString().replace("\\", "/"));
        testcaseRepository.save(e);
    }

    private void ensureCvsEntityExists(Long qid) {
        // Creates the CheckerValidatorSolutionEntity with empty checker path so
        // InvocationService doesn't receive null for checkerFilePath.
        // The judge handles empty/null checker path by using exact string comparison.
        cvsRepository.findByQuestionId(qid).orElseGet(() -> {
            CheckerValidatorSolutionEntity n = new CheckerValidatorSolutionEntity();
            n.setQuestionId(qid);
            n.setCheckerFilePath("");
            return cvsRepository.save(n);
        });
    }

    private void writeCodeFile(Long qid, String type, String code, String filename) throws IOException {
        CheckerValidatorSolutionEntity ent = cvsRepository.findByQuestionId(qid).orElseGet(() -> {
            CheckerValidatorSolutionEntity n = new CheckerValidatorSolutionEntity();
            n.setQuestionId(qid);
            return cvsRepository.save(n);
        });
        Path dir = FILES_BASE.resolve(Paths.get(qid.toString(), type, ent.getId().toString()));
        Files.createDirectories(dir);
        Path file = dir.resolve(filename);
        Files.writeString(file, code);
        String rel = FILES_BASE.relativize(file).toString().replace("\\", "/");
        switch (type) {
            case "solution"  -> ent.setSolutionFilePath(rel);
            case "validator" -> ent.setValidatorFilePath(rel);
            case "checker"   -> ent.setCheckerFilePath(rel);
        }
        cvsRepository.save(ent);
    }

    private void applyEdits(AgentJob job, Map<String, Object> edits) {
        Integer cp = job.getCurrentCheckpoint();
        if (cp == null) return;
        switch (cp) {
            case 1 -> {
                ProblemDraft d = job.getDraft();
                job.setDraft(new ProblemDraft(
                    get(edits, "name", d.name()), get(edits, "legend", d.legend()),
                    get(edits, "inputFormat", d.inputFormat()), get(edits, "outputFormat", d.outputFormat()),
                    get(edits, "constraints", d.constraints()), get(edits, "sampleInput", d.sampleInput()),
                    get(edits, "sampleOutput", d.sampleOutput()), get(edits, "notes", d.notes())
                ));
            }
            case 2 -> {
                if (edits.containsKey("solutionCode")) {
                    SolutionArtifact s = job.getSolution();
                    job.setSolution(new SolutionArtifact((String) edits.get("solutionCode"), s.language(), s.explanation(), s.complexity()));
                }
                if (edits.containsKey("validatorCode")) job.setValidator(new ValidatorArtifact((String) edits.get("validatorCode")));
                if (edits.containsKey("checkerCode")) {
                    CheckerArtifact c = job.getChecker();
                    job.setChecker(new CheckerArtifact((String) edits.get("checkerCode"), c.checkerType()));
                }
            }
            case 3 -> {
                if (edits.containsKey("inputs")) {
                    @SuppressWarnings("unchecked") List<String> ins = (List<String>) edits.get("inputs");
                    job.setTestInputs(new TestInputsArtifact(ins, job.getTestInputs().validationStatus(), ins.size()));
                }
            }
            case 4 -> {
                if (edits.containsKey("outputs")) {
                    @SuppressWarnings("unchecked") List<String> outs = (List<String>) edits.get("outputs");
                    VerificationResult v = job.getVerification();
                    job.setVerification(new VerificationResult(v.allInputsRan(), v.verdicts(), outs, v.checkerVerified(), "MANUALLY_VERIFIED", v.maxTimeMs(), v.maxMemoryKb()));
                }
            }
        }
    }

    private void clearFrom(AgentJob job, int from) {
        if (from <= 1) job.setDraft(null);
        if (from <= 2) { job.setSolution(null); job.setValidator(null); job.setChecker(null); }
        if (from <= 3) job.setTestInputs(null);
        if (from <= 4) job.setVerification(null);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(Map<String, Object> m, String k, T def) {
        return m.containsKey(k) ? (T) m.get(k) : def;
    }
}
