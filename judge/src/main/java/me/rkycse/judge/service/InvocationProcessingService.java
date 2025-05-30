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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.sun.management.OperatingSystemMXBean;

@Service
public class InvocationProcessingService {
    private static final Logger log = LoggerFactory.getLogger(InvocationProcessingService.class);
    private static final String TOPIC = "invocation-result";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final long MEMORY_LIMIT_BYTES = 256L * 1024 * 1024; // 256MB
    private static final int MAX_OUTPUT_BYTES = 10 * 1024 * 1024; // 10MB

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
        long startTime = System.currentTimeMillis();
        InvocationResultDTO result = new InvocationResultDTO();
        try {
            InvocationPayload payload = objectMapper.readValue(payloadJson, InvocationPayload.class);
            result.setQuestionId(payload.getQuestionId());

            // compile all sources
            compileSource(payload.getCheckerFilePath(), result, "Checker");
            compileSource(payload.getValidatorFilePath(), result, "Validator");
            compileSource(payload.getSolutionFilePath(), result, "Solution");

            // run validation
            int vcode = runProcess(payload.getValidatorFilePath(), null, null, result, null);
            if (vcode != 0) {
                result.setVerdict("Validation Failed");
                sendResult(result);
                return;
            }

            List<TestcaseResultDTO> tcResults = new ArrayList<>();
            // run each testcase
            for (InvocationPayload.TestcaseInfo tc : payload.getTestcases()) {
                TestcaseResultDTO tcRes = new TestcaseResultDTO();
                tcRes.setTestcaseId(tc.getTestcaseId());
                Path in = Paths.get(tc.getInputFilePath());
                Path outFile = Paths.get(tc.getOutputFilePath());
                Files.createDirectories(outFile.getParent());
                try {
                    long beforeMem = osBean.getCommittedVirtualMemorySize();
                    long start = System.nanoTime();
                    byte[] output = runProcessWithInput(payload.getSolutionFilePath(), in, result, tcRes);
                    long elapsed = System.nanoTime() - start;
                    long afterMem = osBean.getCommittedVirtualMemorySize();
                    long usedMem = afterMem - beforeMem;

                    Files.write(outFile, output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    tcRes.setTimeNano(elapsed);
                    tcRes.setMemoryBytes(usedMem);
                    tcRes.setStatus("OK");
                } catch (TimeoutException te) {
                    tcRes.setStatus("TLE");
                } catch (IOException | InterruptedException | ExecutionException re) {
                    tcRes.setStatus("RE: " + re.getMessage());
                }
                tcResults.add(tcRes);
            }
            result.setTestcaseResults(tcResults);
            result.setVerdict(allPassed(tcResults) ? "OK" : "FAIL");

        } catch (Exception e) {
            log.error("Invocation processing failed", e);
            result.setVerdict("Error: " + e.getMessage());
        } finally {
            result.setElapsedMillis(System.currentTimeMillis() - startTime);
            sendResult(result);
        }
    }

    private void compileSource(String sourcePath, InvocationResultDTO result, String label)
            throws IOException, InterruptedException, TimeoutException {
        if (sourcePath.endsWith(".py")) return;
        ProcessBuilder pb;
        if (sourcePath.endsWith(".cpp")) {
            pb = new ProcessBuilder("g++", sourcePath, "-O2", "-o", deriveExecutable(sourcePath));
        } else if (sourcePath.endsWith(".java")) {
            pb = new ProcessBuilder("javac", sourcePath);
        } else {
            throw new IllegalArgumentException("Unsupported extension: " + sourcePath);
        }
        pb.redirectErrorStream(true);
        Process p = pb.start();
        if (!p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            throw new TimeoutException(label + " compilation timeout");
        }
        if (p.exitValue() != 0) {
            String err = readStream(p.getInputStream(), MAX_OUTPUT_BYTES);
            throw new RuntimeException(label + " compilation failed: " + err);
        }
    }

    private int runProcess(String exePath, Path inputFile, Path outFile,
                           InvocationResultDTO result, TestcaseResultDTO tcRes)
            throws IOException, InterruptedException, TimeoutException {
        ProcessBuilder pb = buildProcessBuilder(exePath);
        if (inputFile != null) pb.redirectInput(inputFile.toFile());
        Process p = pb.start();
        boolean done = p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!done) {
            p.destroyForcibly();
            throw new TimeoutException("Execution timeout");
        }
        return p.exitValue();
    }

    private byte[] runProcessWithInput(String exePath, Path inputFile,
                                       InvocationResultDTO result, TestcaseResultDTO tcRes)
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        ProcessBuilder pb = buildProcessBuilder(exePath);
        pb.redirectInput(inputFile.toFile());
        Process p = pb.start();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StreamGobbler gobbler = new StreamGobbler(p.getInputStream(), buffer, MAX_OUTPUT_BYTES);
        Future<?> f = Executors.newSingleThreadExecutor().submit(gobbler);
        if (!p.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly(); f.cancel(true);
            throw new TimeoutException("Execution timeout");
        }
        f.get(1, TimeUnit.SECONDS);
        if (p.exitValue() != 0) {
            throw new RuntimeException("Runtime error, exit code " + p.exitValue());
        }
        return buffer.toByteArray();
    }

    private boolean allPassed(List<TestcaseResultDTO> results) {
        return results.stream().allMatch(r -> "OK".equals(r.getStatus()));
    }

    private ProcessBuilder buildProcessBuilder(String path) {
        if (path.endsWith(".py")) {
            return new ProcessBuilder("python3", path);
        } else if (path.endsWith(".cpp") || path.endsWith(".java")) {
            String exe = deriveExecutable(path);
            return new ProcessBuilder(exe.split(" "));  // support 'java ClassName'
        }
        throw new IllegalArgumentException("Unsupported file: " + path);
    }

    private String deriveExecutable(String sourcePath) {
        if (sourcePath.endsWith(".cpp")) {
            return sourcePath.replace(".cpp", "");
        } else if (sourcePath.endsWith(".java")) {
            String className = Paths.get(sourcePath).getFileName().toString().replace(".java", "");
            return "java " + className;
        }
        return sourcePath;
    }

    private String readStream(InputStream is, int maxBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int total = 0;
        int r;
        while ((r = is.read(buf)) != -1) {
            total += r;
            if (total > maxBytes) throw new IOException("Output exceeds limit");
            baos.write(buf, 0, r);
        }
        return baos.toString();
    }

    private void sendResult(InvocationResultDTO result) {
        try {
            String msg = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(TOPIC, msg);
            log.info("Sent result for questionId={} verdict={}", result.getQuestionId(), result.getVerdict());
        } catch (Exception e) {
            log.error("Failed to send result", e);
        }
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream is;
        private final OutputStream os;
        private final int maxBytes;
        private int written = 0;
        public StreamGobbler(InputStream is, OutputStream os, int maxBytes) {
            this.is = is; this.os = os; this.maxBytes = maxBytes;
        }
        @Override public void run() {
            try {
                byte[] buf = new byte[4096]; int r;
                while ((r = is.read(buf)) != -1) {
                    written += r;
                    if (written > maxBytes) throw new IOException("Output exceeds buffer limit");
                    os.write(buf, 0, r);
                }
            } catch (Exception e) {
                log.error("Error reading process stream", e);
            }
        }
    }
}
