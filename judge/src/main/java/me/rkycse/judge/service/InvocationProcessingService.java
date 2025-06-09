package me.rkycse.judge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.rkycse.judge.dto.InvocationPayload;
import me.rkycse.judge.dto.InvocationResultDTO;
import me.rkycse.judge.dto.TestcaseResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import com.sun.management.OperatingSystemMXBean;

@Service
public class InvocationProcessingService {
    private static final Logger log = LoggerFactory.getLogger(InvocationProcessingService.class);
    private static final String TOPIC = "invocation-result";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final long MEMORY_LIMIT_BYTES = 256L * 1024 * 1024;
    private static final int MAX_OUTPUT_BYTES = 10 * 1024 * 1024;

    // resolve partial paths under parent/files
    private static final Path FILES_ROOT =
            Paths.get("").toAbsolutePath().getParent().resolve("files");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OperatingSystemMXBean osBean;

    public InvocationProcessingService(KafkaTemplate<String, String> kafkaTemplate,
                                       ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public void processInvocation(String payloadJson) {
        log.info("Starting invocation processing");
        log.debug("Raw payload: {}", payloadJson);
        long startTime = System.currentTimeMillis();
        InvocationResultDTO result = new InvocationResultDTO();
        CompiledArtifact checkerArt = null, validatorArt = null, solutionArt = null;

        try {
            InvocationPayload payload = objectMapper.readValue(payloadJson, InvocationPayload.class);
            result.setQuestionId(payload.getQuestionId());
            log.info("Parsed payload for questionId={}", payload.getQuestionId());

            // 1) Prepare artifacts
            checkerArt   = prepareArtifact(payload.getCheckerFilePath(),   "Checker");
            validatorArt = prepareArtifact(payload.getValidatorFilePath(), "Validator");
            solutionArt  = prepareArtifact(payload.getSolutionFilePath(),  "Solution");

            // 2) Run validator on each testcase’s input
            for (InvocationPayload.TestcaseInfo tc : payload.getTestcases()) {
                log.info("Validating testcaseId={}", tc.getTestcaseId());
                Path in = resolvePath(tc.getInputFilePath());
                try {
                    log.info("Reading input file {}", in);
                    int exit = runValidator(validatorArt, in);
                    log.info("Validator exit code for testcaseId={} = {}", tc.getTestcaseId(), exit);
                    if (exit != 0) {
                        log.warn("Validation failed on testcaseId={} exit={}", tc.getTestcaseId(), exit);
                        result.setVerdict("Validation Failed on testcaseId=" + tc.getTestcaseId());
                        sendResult(result);
                        return;
                    }
                } catch (TimeoutException te) {
                    log.error("Validator timeout for testcaseId={}: {}", tc.getTestcaseId(), te.getMessage());
                    result.setVerdict("Validation TLE");
                    sendResult(result);
                    return;
                }
            }

            // 3) Run solution on each testcase
            List<TestcaseResultDTO> tcResults = new ArrayList<>();
            for (InvocationPayload.TestcaseInfo tc : payload.getTestcases()) {
                log.info("Running solution for testcaseId={}", tc.getTestcaseId());
                TestcaseResultDTO tcRes = new TestcaseResultDTO();
                tcRes.setTestcaseId(tc.getTestcaseId());

                Path in  = resolvePath(tc.getInputFilePath());
                Path out = resolvePath(tc.getOutputFilePath());
                Files.createDirectories(out.getParent());

                try {
                    long beforeMem = osBean.getCommittedVirtualMemorySize();
                    long t0 = System.nanoTime();

                    byte[] stdout = runSolutionWithInput(solutionArt, in, tcRes);

                    long elapsed = System.nanoTime() - t0;
                    long usedMem = osBean.getCommittedVirtualMemorySize() - beforeMem;

                    Files.write(out, stdout,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    tcRes.setTimeNano(elapsed);
                    tcRes.setMemoryBytes(usedMem);
                    tcRes.setStatus("OK");
                    log.info("Solution OK for testcaseId={} time={}ns mem={}B",
                            tc.getTestcaseId(), elapsed, usedMem);
                } catch (TimeoutException te) {
                    tcRes.setStatus("TLE");
                    log.error("Solution TLE for testcaseId={}: {}", tc.getTestcaseId(), te.getMessage());
                } catch (Exception e) {
                    tcRes.setStatus("RE: " + e.getMessage());
                    log.error("Solution RE for testcaseId={}", tc.getTestcaseId(), e);
                }

                tcResults.add(tcRes);
            }

            result.setTestcaseResults(tcResults);
            result.setVerdict(allPassed(tcResults) ? "OK" : "FAIL");
            log.info("Final verdict={}", result.getVerdict());

        } catch (Exception e) {
            log.error("Invocation processing failed", e);
            result.setVerdict("Error: " + e.getMessage());
        } finally {
            result.setElapsedMillis(System.currentTimeMillis() - startTime);
            log.info("Total elapsed time={}ms", result.getElapsedMillis());
            sendResult(result);
            deleteArtifactQuietly(checkerArt);
            deleteArtifactQuietly(validatorArt);
            deleteArtifactQuietly(solutionArt);
        }
    }

    private Path resolvePath(String p) {
        String norm = p.replace("\\", File.separator).replace("/", File.separator);
        Path path = Paths.get(norm);
        Path resolved = path.isAbsolute() ? path : FILES_ROOT.resolve(path);
        log.debug("Resolved path '{}' -> '{}'", p, resolved);
        return resolved;
    }

    private CompiledArtifact prepareArtifact(String sourcePath, String label)
            throws IOException, InterruptedException, TimeoutException {
        Path src = resolvePath(sourcePath);
        String s = src.toString();
        log.info("Preparing {} artifact from '{}'", label, s);

        if (s.endsWith(".py")) {
            log.debug("{} is a Python script", label);
            return new CompiledArtifact(s);
        }
        if (s.endsWith(".cpp")) {
            Path exe = Files.createTempFile("temp_" + label + "_", "");
            log.debug("Compiling C++ {} -> {}", label, exe);
            ProcessBuilder pb = new ProcessBuilder("g++", s, "-O2", "-o", exe.toString());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            if (!p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                throw new TimeoutException(label + " compilation timeout");
            }
            if (p.exitValue() != 0) {
                String err = readStream(p.getInputStream(), MAX_OUTPUT_BYTES);
                log.error("{} compilation failed: {}", label, err);
                throw new RuntimeException(label + " compilation failed: " + err);
            }
            log.info("{} compiled successfully to {}", label, exe);
            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ,
                        PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
                        PosixFilePermission.OTHERS_EXECUTE
                );
                Files.setPosixFilePermissions(exe, perms);
            } catch (UnsupportedOperationException ignored) { }
            return new CompiledArtifact(exe);
        }
        if (s.endsWith(".java")) {
            Path dir = Files.createTempDirectory("temp_" + label + "_classes_");
            log.debug("Compiling Java {} -> {}", label, dir);
            ProcessBuilder pb = new ProcessBuilder("javac", "-d", dir.toString(), s);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            if (!p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                throw new TimeoutException(label + " compilation timeout");
            }
            if (p.exitValue() != 0) {
                String err = readStream(p.getInputStream(), MAX_OUTPUT_BYTES);
                log.error("{} compilation failed: {}", label, err);
                throw new RuntimeException(label + " compilation failed: " + err);
            }
            String cls = src.getFileName().toString().replace(".java", "");
            log.info("{} compiled successfully, main class={}", label, cls);
            return new CompiledArtifact(dir, cls);
        }
        throw new IllegalArgumentException("Unsupported extension: " + s);
    }

    private int runValidator(CompiledArtifact art, Path input)
            throws IOException, InterruptedException, TimeoutException {
        log.info("runValidator on input path: {}", input);
        ProcessBuilder pb;

        switch (art.type) {
            case PYTHON:
                pb = new ProcessBuilder("python3", art.scriptPath.toString(), input.toString());
                break;

            case CPP:
                // 🔥 Run the executable and redirect stdin from input file
                pb = new ProcessBuilder(art.exePath.toString());
                pb.redirectInput(input.toFile());  // 👈 sends input file into cin
                break;

            case JAVA:
                pb = new ProcessBuilder(
                        "java",
                        "-cp", art.classesDir.toString(),
                        art.className,
                        input.toString()
                );
                pb.directory(art.classesDir.toFile());
                break;

            default:
                throw new IllegalStateException("Unknown validator type");
        }

        // Combine stdout and stderr so we can read both easily if needed
        log.debug("Running validator: {}", String.join(" ", pb.command()));
        pb.redirectErrorStream(true);

        Process p = pb.start();

        // Wait for process to complete within timeout
        if (!p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            throw new TimeoutException("Validator execution timeout");
        }

        log.debug("Validator exit code: {}", p.exitValue());
        return p.exitValue();
    }


    private byte[] runSolutionWithInput(CompiledArtifact art, Path input, TestcaseResultDTO tcRes)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        ProcessBuilder pb;
        switch (art.type) {
            case PYTHON:
                pb = new ProcessBuilder("python3", art.scriptPath.toString());
                break;
            case CPP:
                pb = new ProcessBuilder(art.exePath.toString());
                break;
            case JAVA:
                pb = new ProcessBuilder("java", "-cp", art.classesDir.toString(), art.className);
                pb.directory(art.classesDir.toFile());
                break;
            default:
                throw new IllegalStateException("Unknown solution type");
        }
        log.debug("Running solution: {} < {}", String.join(" ", pb.command()), input);
        pb.redirectInput(input.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        StreamGobbler gob = new StreamGobbler(p.getInputStream(), buf, MAX_OUTPUT_BYTES);
        Future<?> fut = Executors.newSingleThreadExecutor().submit(gob);

        if (!p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            fut.cancel(true);
            throw new TimeoutException("Solution execution timeout");
        }
        fut.get(1, TimeUnit.SECONDS);
        log.debug("Solution exit code: {}", p.exitValue());
        if (p.exitValue() != 0) {
            throw new RuntimeException("Runtime error, exit code " + p.exitValue());
        }
        return buf.toByteArray();
    }

    private boolean allPassed(List<TestcaseResultDTO> lst) {
        return lst.stream().allMatch(r -> "OK".equals(r.getStatus()));
    }

    private String readStream(InputStream is, int max) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int total = 0, r;
        while ((r = is.read(b)) != -1) {
            total += r;
            if (total > max) throw new IOException("Output exceeds limit");
            baos.write(b, 0, r);
        }
        return baos.toString();
    }

    private void sendResult(InvocationResultDTO res) {
        try {
            String msg = objectMapper.writeValueAsString(res);
            log.info("Sending result: questionId={} verdict={}", res.getQuestionId(), res.getVerdict());
            kafkaTemplate.send(TOPIC, msg);
        } catch (Exception e) {
            log.error("Failed to send result", e);
        }
    }

    private void deleteArtifactQuietly(CompiledArtifact art) {
        if (art == null) return;
        try {
            switch (art.type) {
                case CPP:
                    Files.deleteIfExists(art.exePath);
                    log.debug("Deleted temp exe {}", art.exePath);
                    break;
                case JAVA:
                    deleteDirectoryRecursively(art.classesDir);
                    log.debug("Deleted temp classes dir {}", art.classesDir);
                    break;
                case PYTHON:
                    // no temp for script
                    break;
            }
        } catch (IOException ignored) { }
    }

    private void deleteDirectoryRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path c : ds) {
                if (Files.isDirectory(c)) deleteDirectoryRecursively(c);
                else Files.deleteIfExists(c);
            }
        }
        Files.deleteIfExists(dir);
    }

    private static class CompiledArtifact {
        enum Type { PYTHON, CPP, JAVA }
        final Type type;
        final Path scriptPath, exePath, classesDir;
        final String className;
        CompiledArtifact(String script) {
            this.type = Type.PYTHON; this.scriptPath = Paths.get(script);
            this.exePath = null; this.classesDir = null; this.className = null;
        }
        CompiledArtifact(Path exe) {
            this.type = Type.CPP; this.exePath = exe;
            this.scriptPath = null; this.classesDir = null; this.className = null;
        }
        CompiledArtifact(Path dir, String cls) {
            this.type = Type.JAVA; this.classesDir = dir; this.className = cls;
            this.scriptPath = null; this.exePath = null;
        }
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream is; private final OutputStream os; private final int max;
        private int written = 0;
        StreamGobbler(InputStream is, OutputStream os, int max) {
            this.is = is; this.os = os; this.max = max;
        }
        @Override
        public void run() {
            try {
                byte[] b = new byte[4096];
                int r;
                while ((r = is.read(b)) != -1) {
                    written += r;
                    if (written > max) throw new IOException("Output exceeds buffer limit");
                    os.write(b, 0, r);
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(StreamGobbler.class).error("Error reading stream", e);
            }
        }
    }
}