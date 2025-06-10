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
    private static final int WALL_CLOCK_TIMEOUT_SECONDS = 10;
    private static final double TIME_LIMIT_SECONDS = 2.0;
    private static final long MEMORY_LIMIT_KB = 256 * 1024;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String executeCode(String jsonInput) {
        logger.info("Received execution request");
        long judgeStart = System.currentTimeMillis();

        String verdict;
        double maxTime = 0.0;
        long maxMemory = 0;
        boolean hasRuntimeError = false;
        boolean hasTLE = false;
        boolean hasMLE = false;
        boolean allCorrect = true;
        Path tempDir = null;

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonInput);
            String questionId = jsonNode.get("questionId").asText();
            String code = jsonNode.get("code").asText();
            String language = jsonNode.get("language").asText().toLowerCase();
            String username = jsonNode.get("username").asText();
            Long tournamentId = jsonNode.get("tournamentId").asLong();
            int index = jsonNode.get("index").asInt();
            long submissionTime = jsonNode.get("submissionTime").asLong();

            tempDir = Files.createTempDirectory("execution_");
            Path codeFile = writeCodeToFile(tempDir, code, language);

            // Compile if needed
            if (language.equals("c++") || language.equals("java")) {
                String compilationError = compileCode(tempDir, language);
                if (compilationError != null) {
                    verdict = "CE\n" + compilationError;
                    return buildResponse(null, index, username, tournamentId,
                            Long.parseLong(questionId), language, code,
                            verdict, submissionTime, judgeStart, 0L, 0L);
                }
            }

            // Locate checker
            Path checkerBinary = null;
            Path checkerDir = null;
            Path cwd = Paths.get("").toAbsolutePath();
            Path checkerBase = cwd.resolveSibling("files").resolve(questionId).resolve("checker");
            if (Files.exists(checkerBase) && Files.isDirectory(checkerBase)) {
                Optional<Path> firstFolder = Files.list(checkerBase)
                        .filter(Files::isDirectory)
                        .findFirst();
                if (firstFolder.isPresent()) {
                    Path chkDir = firstFolder.get();
                    checkerDir = chkDir;
                    // find existing executable
                    for (Path f : Files.list(chkDir).collect(Collectors.toList())) {
                        if (!f.getFileName().toString().endsWith(".cpp")) {
                            checkerBinary = f;
                            break;
                        }
                    }
                    // compile if not present
                    if (checkerBinary == null) {
                        Optional<Path> src = Files.list(chkDir)
                                .filter(p -> p.getFileName().toString().endsWith(".cpp"))
                                .findFirst();
                        if (src.isPresent()) {
                            ProcessBuilder chkPb = new ProcessBuilder("g++",
                                    src.get().getFileName().toString(), "-o", "checker");
                            chkPb.directory(chkDir.toFile());
                            Process chkProc = chkPb.start();
                            if (chkProc.waitFor() == 0) {
                                checkerBinary = chkDir.resolve("checker");
                            }
                        }
                    }
                }
            }

            List<Path> testCaseDirs = getTestCaseDirectories(questionId);
            if (testCaseDirs.isEmpty()) {
                verdict = "Error: No test cases found for questionId " + questionId;
                return buildResponse(null, index, username, tournamentId,
                        Long.parseLong(questionId), language, code,
                        verdict, submissionTime, judgeStart, 0L, 0L);
            }

            for (Path testCaseDir : testCaseDirs) {
                Path inputFile = testCaseDir.resolve("input/input.txt");
                Path expectedOutputFile = testCaseDir.resolve("output/output.txt");
                Path tempOutputFile = tempDir.resolve("tempOutput_" + testCaseDir.getFileName() + ".txt");
                Path tempTimeFile = tempDir.resolve("tempTime_" + testCaseDir.getFileName() + ".txt");

                // Run user code
                ProcessBuilder pb = createProcessBuilder(tempDir, language, codeFile, tempTimeFile);
                pb.redirectInput(inputFile.toFile());
                pb.redirectOutput(tempOutputFile.toFile());
                Process process = pb.start();
                drainStream(process.getErrorStream(), "ERROR");

                if (!process.waitFor(WALL_CLOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    hasTLE = true;
                    process.destroyForcibly();
                    continue;
                }
                if (process.exitValue() != 0) {
                    hasRuntimeError = true;
                    continue;
                }

                Map<String, String> timeData = parseTimeFile(tempTimeFile);
                double totalTime = Double.parseDouble(timeData.getOrDefault("User time (seconds)", "0"))
                        + Double.parseDouble(timeData.getOrDefault("System time (seconds)", "0"));
                long memory = Long.parseLong(timeData.getOrDefault("Maximum resident set size (kbytes)", "0"));
                if (totalTime > TIME_LIMIT_SECONDS) hasTLE = true;
                if (memory > MEMORY_LIMIT_KB) hasMLE = true;

                // Read outputs into strings
                String userOutput = Files.readString(tempOutputFile).trim();
                String expectedOutput = Files.readString(expectedOutputFile).trim();

                boolean passed;
                if (checkerBinary != null && checkerDir != null) {
                    ProcessBuilder chkRun = new ProcessBuilder(checkerBinary.toString());
                    chkRun.directory(checkerDir.toFile());
                    Process chkProc = chkRun.start();
                    // feed user and expected outputs via stdin
                    try (BufferedWriter bw = new BufferedWriter(
                            new OutputStreamWriter(chkProc.getOutputStream()))) {
                        bw.write(userOutput);
                        bw.newLine();
                        bw.write(expectedOutput);
                        bw.flush();
                    }
                    if (!chkProc.waitFor(WALL_CLOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS) || chkProc.exitValue() != 0) {
                        passed = false;
                    } else {
                        passed = true;
                    }
                } else {
                    passed = userOutput.equals(expectedOutput);
                }

                if (!passed) allCorrect = false;
                maxTime = Math.max(maxTime, totalTime);
                maxMemory = Math.max(maxMemory, memory);
            }

            if (hasRuntimeError) verdict = "RE";
            else if (hasTLE) verdict = "TLE";
            else if (hasMLE) verdict = "MLE";
            else if (!allCorrect) verdict = "WA";
            else verdict = "AC";

            return buildResponse(null, index, username, tournamentId,
                    Long.parseLong(objectMapper.readTree(jsonInput).get("questionId").asText()),
                    language, code, verdict, submissionTime,
                    judgeStart, (long)(maxTime * 1000), maxMemory);

        } catch (Exception e) {
            logger.error("Error during code execution", e);
            return buildResponse(null, -1, null, null, null,
                    null, "", "Error: " + e.getMessage(), judgeStart, judgeStart, 0L, 0L);
        } finally {
            if (tempDir != null) {
                try { deleteDirectory(tempDir); } catch (IOException ignored) {}
            }
        }
    }

    private String buildResponse(Long id, int index, String username, Long tournamentId,
                                 Long questionId, String language, String code,
                                 String verdict, long submissionTime, long judgingTime,
                                 long maxTimeTaken, long maxMemoryUsed) {
        try {
            ClassicSubmissionDTO response = new ClassicSubmissionDTO();
            response.setId(id);
            response.setIndex(index);
            response.setUsername(username);
            response.setTournamentId(tournamentId);
            response.setQuestionId(questionId);
            response.setLanguage(language);
            response.setCode(code);
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
            case "c++": fileName = "code.cpp"; break;
            case "java": fileName = "Main.java"; break;
            case "python": fileName = "code.py"; break;
            default: throw new IllegalArgumentException("Unsupported language: " + language);
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
            return null;
        }
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        if (process.waitFor() != 0) {
            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
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
        Path testcasesPath = Paths.get("..", "files", questionId, "testcases");
        if (Files.notExists(testcasesPath)) return Collections.emptyList();
        try (Stream<Path> paths = Files.list(testcasesPath)) {
            return paths.filter(Files::isDirectory).collect(Collectors.toList());
        }
    }

    private ProcessBuilder createProcessBuilder(Path tempDir, String language,
                                                Path codeFile, Path tempTimeFile) {
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

    private Map<String,String> parseTimeFile(Path timeFile) throws IOException {
        Map<String,String> result = new HashMap<>();
        for (String line : Files.readAllLines(timeFile)) {
            if (line.contains(":")) {
                String[] parts = line.split(":",2);
                result.put(parts[0].trim(), parts[1].trim());
            }
        }
        return result;
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); return FileVisitResult.CONTINUE;
            }
        });
    }

    private void drainStream(InputStream stream, String streamName) {
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    logger.debug("["+streamName+"]: "+line);
                }
            } catch (IOException e) {
                logger.error("Error draining "+streamName+" stream", e);
            }
        }).start();
    }
}
