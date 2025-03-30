package me.rkycse.judge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.rkycse.judge.dto.ClassicSubmissionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);

    private static final int WALL_CLOCK_TIMEOUT_SECONDS = 10; // Wall clock timeout for process execution
    private static final double TIME_LIMIT_SECONDS = 2.0;       // CPU time limit per test case
    private static final long MEMORY_LIMIT_KB = 256 * 1024;       // Memory limit (256 MB in KB)
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String executeCode(String jsonInput) {
        logger.info("Received execution request");
        // Record submission time (milliseconds)

        // Record judge start time (this timestamp will be returned as judgingTime)
        long judgeStart = System.currentTimeMillis();

        String verdict = "";
        double maxTime = 0.0;
        long maxMemory = 0;
        boolean hasRuntimeError = false;
        boolean hasTLE = false;
        boolean hasMLE = false;
        boolean allCorrect = true;

        Path tempDir = null;
        try {
            // Parse JSON input
            JsonNode jsonNode = objectMapper.readTree(jsonInput);
            String questionId = jsonNode.get("questionId").asText();
            String code = jsonNode.get("code").asText();
            String language = jsonNode.get("language").asText().toLowerCase();
            String username = jsonNode.get("username").asText();
            Long tournamentId = jsonNode.get("tournamentId").asLong();
            int index=jsonNode.get("index").asInt();
            Long submissionTime = jsonNode.get("submissionTime").asLong();

            logger.info("Parsed jsonInput: questionId: {}," +
                    " index: {}, tournamentId: {}, code: {} ," +
                    "language: {}, ", questionId, index, tournamentId,code, language);



            // Create a temporary directory for execution
            tempDir = Files.createTempDirectory("execution_");
            logger.info("Created temporary directory: {}", tempDir);

            // Write code to file and compile if necessary
            Path codeFile = writeCodeToFile(tempDir, code, language);
            logger.info("Wrote code to file: {}", codeFile);
            if (language.equals("c++") || language.equals("java")) {
                String compilationError = compileCode(tempDir, language);
                if (compilationError != null) {
                    logger.error("Compilation failed: {}", compilationError);
                    verdict = "Compilation Error (CE)\n" + compilationError;
                    // Build response with verdict "CE" (after mapping) and zero time/memory.
                    return buildResponse(null, index, username, tournamentId, Long.parseLong(questionId), language,
                            code,verdict, submissionTime, judgeStart, 0L, 0L);
                }
                logger.info("Compilation succeeded");
            }

            // Get all test case directories
            List<Path> testCaseDirs = getTestCaseDirectories(questionId);
            if (testCaseDirs.isEmpty()) {
                String errorMsg = "No test cases found for questionId " + questionId;
                logger.error(errorMsg);
                verdict = "Error: " + errorMsg;
                return buildResponse(null, index, username, tournamentId, Long.parseLong(questionId), language,
                        code,verdict, submissionTime, judgeStart, 0L, 0L);
            }
            logger.info("Found {} test case directories", testCaseDirs.size());

            // Process each test case
            for (Path testCaseDir : testCaseDirs) {
                logger.info("Processing test case directory: {}", testCaseDir);
                Path inputFile = testCaseDir.resolve("input.txt");
                Path expectedOutputFile = testCaseDir.resolve("output.txt");
                // Use unique temporary files per test case
                Path tempOutputFile = tempDir.resolve("tempOutput_" + testCaseDir.getFileName() + ".txt");
                Path tempTimeFile = tempDir.resolve("tempTime_" + testCaseDir.getFileName() + ".txt");

                // Set up ProcessBuilder with time measurement
                ProcessBuilder pb = createProcessBuilder(tempDir, language, codeFile, tempTimeFile);
                pb.redirectInput(inputFile.toFile());
                pb.redirectOutput(tempOutputFile.toFile());
                logger.info("Starting process for test case: {} using command: {}",
                        testCaseDir, Arrays.toString(pb.command().toArray()));

                Process process = pb.start();

                // Drain error stream in a separate thread to prevent blocking
                drainStream(process.getErrorStream(), "ERROR");

                boolean finished = process.waitFor(WALL_CLOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!finished) {
                    logger.warn("Process did not finish in {} seconds. Attempting to terminate.", WALL_CLOCK_TIMEOUT_SECONDS);
                    process.destroy();
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        logger.warn("Process still running after destroy(), forcing termination.");
                        process.destroyForcibly();
                    }
                    hasTLE = true;
                    continue;
                }

                int exitCode = process.exitValue();
                logger.info("Process finished with exit code: {}", exitCode);
                if (exitCode != 0) {
                    logger.error("Non-zero exit code detected. Marking as runtime error.");
                    hasRuntimeError = true;
                    continue;
                }

                // Parse time and memory usage from the temporary time file
                Map<String, String> timeData = parseTimeFile(tempTimeFile);
                double userTime = Double.parseDouble(timeData.getOrDefault("User time (seconds)", "0"));
                double sysTime = Double.parseDouble(timeData.getOrDefault("System time (seconds)", "0"));
                double totalTime = userTime + sysTime;
                long memory = Long.parseLong(timeData.getOrDefault("Maximum resident set size (kbytes)", "0"));

                logger.info("Test case {}: time = {} s, memory = {} KB", testCaseDir.getFileName(), totalTime, memory);

                if (totalTime > TIME_LIMIT_SECONDS) {
                    logger.warn("Time limit exceeded for test case: {} ({} s)", testCaseDir.getFileName(), totalTime);
                    hasTLE = true;
                }
                if (memory > MEMORY_LIMIT_KB) {
                    logger.warn("Memory limit exceeded for test case: {} ({} KB)", testCaseDir.getFileName(), memory);
                    hasMLE = true;
                }

                // Compare actual output with expected output
                String actualOutput = Files.readString(tempOutputFile).trim();
                String expectedOutput = Files.readString(expectedOutputFile).trim();
                if (!actualOutput.equals(expectedOutput)) {
                    logger.warn("Wrong answer for test case: {}", testCaseDir.getFileName());
                    allCorrect = false;
                } else {
                    logger.info("Test case {} passed", testCaseDir.getFileName());
                }

                // Update maximum time and memory usage across test cases
                maxTime = Math.max(maxTime, totalTime);
                maxMemory = Math.max(maxMemory, memory);
            }

            // Determine final verdict based on test cases outcomes
            if (hasRuntimeError) {
                verdict = "Runtime Error";
            } else if (hasTLE) {
                verdict = "TLE (Time Limit Exceeded)";
            } else if (hasMLE) {
                verdict = "MLE (Memory Limit Exceeded)";
            } else if (!allCorrect) {
                verdict = "Wrong Answer";
            } else {
                verdict = "Accepted";
            }

            // Calculate max CPU time (in ms) and maximum memory (in KB) used
            long maxTimeTaken = (long) (maxTime * 1000);
            long maxMemoryUsed = maxMemory;

            // Build and return the response DTO as JSON;
            // judgeTime is the timestamp when judging started.
            return buildResponse(null, index, username, tournamentId, Long.parseLong(questionId), language,
                    code,verdict, submissionTime, judgeStart, maxTimeTaken, maxMemoryUsed);
        } catch (Exception e) {
            logger.error("Error during code execution", e);
            return buildResponse(null, -1, null, null, null, null,
                    "","Error: " + e.getMessage(),judgeStart, judgeStart, 0L, 0L);
        } finally {
            if (tempDir != null) {
                try {
                    deleteDirectory(tempDir);
                    logger.info("Cleaned up temporary directory: {}", tempDir);
                } catch (IOException e) {
                    logger.error("Failed to delete temporary directory: {}", tempDir, e);
                }
            }
        }
    }

    private String buildResponse(Long id, int index, String username, Long tournamentId, Long questionId,
                                 String language,String code, String verdict, long submissionTime,
                                 long judgingTime, long maxTimeTaken, long maxMemoryUsed) {
        try {
            // Map the verbose verdict to the required abbreviations
            if (verdict != null) {
                if (verdict.startsWith("Compilation Error")) {
                    verdict = "CE";
                } else if (verdict.equals("Runtime Error")) {
                    verdict = "RE";
                } else if (verdict.startsWith("TLE")) {
                    verdict = "TLE";
                } else if (verdict.startsWith("MLE")) {
                    verdict = "MLE";
                } else if (verdict.equals("Wrong Answer")) {
                    verdict = "WA";
                } else if (verdict.equals("Accepted")) {
                    verdict = "AC";
                }
            }
            ClassicSubmissionDTO response = new ClassicSubmissionDTO();
            response.setId(id);
            response.setCode(code);
            response.setIndex(index);
            response.setUsername(username);
            response.setTournamentId(tournamentId);
            response.setQuestionId(questionId);
            response.setLanguage(language);
            response.setVerdict(verdict);
            response.setSubmissionTime(submissionTime);
            response.setJudgingTime(judgingTime);
            response.setMaxTimeTaken(maxTimeTaken);
            response.setMaxMemoryUsed(maxMemoryUsed);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Error building response DTO", e);
            return "{\"verdict\":\"Error building response DTO\"}";
        }
    }

    private Path writeCodeToFile(Path tempDir, String code, String language) throws IOException {
        String fileName;
        switch (language) {
            case "c++":
                fileName = "code.cpp";
                break;
            case "java":
                fileName = "Main.java";
                break;
            case "python":
                fileName = "code.py";
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
        Path codeFile = tempDir.resolve(fileName);
        Files.writeString(codeFile, code);
        return codeFile;
    }

    private String compileCode(Path tempDir, String language) throws IOException, InterruptedException {
        ProcessBuilder pb;
        if (language.equals("c++")) {
            pb = new ProcessBuilder("g++", "code.cpp", "-o", "a.out");
        } else if (language.equals("java")) {
            pb = new ProcessBuilder("javac", "Main.java");
        } else {
            return null; // No compilation needed for Python
        }
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        logger.info("Starting compilation for language: {}", language);
        Process process = pb.start();
        if (process.waitFor() != 0) {
            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }
            return error.toString();
        }
        return null;
    }

    private List<Path> getTestCaseDirectories(String questionId) throws IOException {
        Path testcasesPath = Paths.get("testcases", questionId);
        try (Stream<Path> paths = Files.list(testcasesPath)) {
            return paths.filter(Files::isDirectory).collect(Collectors.toList());
        }
    }

    private ProcessBuilder createProcessBuilder(Path tempDir, String language, Path codeFile, Path tempTimeFile) {
        // Use the "time" command from the PATH rather than hardcoding "/usr/bin/time"
        String timeCmd = "time";
        String[] command;
        switch (language) {
            case "c++":
                command = new String[]{timeCmd, "-v", "-o", tempTimeFile.toString(), "./a.out"};
                break;
            case "java":
                command = new String[]{timeCmd, "-v", "-o", tempTimeFile.toString(), "java", "Main"};
                break;
            case "python":
                command = new String[]{timeCmd, "-v", "-o", tempTimeFile.toString(), "python3", "code.py"};
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(tempDir.toFile());
        return pb;
    }

    private Map<String, String> parseTimeFile(Path timeFile) throws IOException {
        Map<String, String> result = new HashMap<>();
        List<String> lines = Files.readAllLines(timeFile);
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();
                result.put(key, value);
            }
        }
        logger.debug("Parsed time file {}: {}", timeFile, result);
        return result;
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // Helper method to drain an InputStream in a separate thread to prevent process blocking.
    private void drainStream(InputStream stream, String streamName) {
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    logger.debug("[" + streamName + "]: " + line);
                }
            } catch (IOException e) {
                logger.error("Error draining " + streamName + " stream", e);
            }
        }).start();
    }
}
