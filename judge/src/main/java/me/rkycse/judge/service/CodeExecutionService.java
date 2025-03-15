package me.rkycse.judge.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private static final int TIMEOUT_SECONDS = 10;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String executeCode(String jsonInput) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonInput);
            String code = jsonNode.get("code").asText();
            String language = jsonNode.get("language").asText();

            switch (language.toLowerCase()) {
                case "c++":
                    return executeCpp(code);
                case "python":
                    return executePython(code);
                case "java":
                    return executeJava(code);
                default:
                    return "Unsupported language: " + language;
            }
        } catch (Exception e) {
            return "Error parsing JSON: " + e.getMessage();
        }
    }

    private String executeCpp(String code) throws Exception {
        File cppFile = File.createTempFile("code", ".cpp");
        writeToFile(cppFile, code);
        Process compile = new ProcessBuilder("g++", cppFile.getAbsolutePath(), "-o", "a.out").start();
        if (compile.waitFor() != 0) {
            return "Compilation Error (CE)";
        }
        return executeProcess(new ProcessBuilder("./a.out"));
    }

    private String executePython(String code) throws Exception {
        return executeProcess(new ProcessBuilder("python3", "-c", code));
    }

    private String executeJava(String code) throws Exception {
        File javaFile = File.createTempFile("Main", ".java");
        writeToFile(javaFile, code);
        Process compile = new ProcessBuilder("javac", javaFile.getAbsolutePath()).start();
        if (compile.waitFor() != 0) {
            return "Compilation Error (CE)";
        }
        return executeProcess(new ProcessBuilder("java", "-cp", javaFile.getParent(), "Main"));
    }

    private String executeProcess(ProcessBuilder processBuilder) throws Exception {
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> output = executor.submit(() -> new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().reduce("", (a, b) -> a + "\n" + b));

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroy();
            return "TLE (Time Limit Exceeded)";
        }

        return "Output:\n" + output.get();
    }

    private void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }
}
