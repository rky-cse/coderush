package me.rkycse.coderush.controller;

import jakarta.servlet.http.HttpServletRequest;
import me.rkycse.coderush.entity.ClassicTestcaseEntity;
import me.rkycse.coderush.repository.ClassicTestcaseRepository;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class TestsController {

    private final ClassicTestcaseRepository testcaseRepo;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;   // 5 MB

    public TestsController(ClassicTestcaseRepository testcaseRepo) {
        this.testcaseRepo = testcaseRepo;
    }

    // CREATE / UPLOAD new test case
    @PostMapping("/{questionId}/tests")
    public ResponseEntity<?> uploadTest(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        return handleTestUpload(request, questionId, null);
    }

    // UPDATE existing test case
    @PutMapping("/{questionId}/tests/{testcaseId}")
    public ResponseEntity<?> updateTest(
            @PathVariable Long questionId,
            @PathVariable Long testcaseId,
            HttpServletRequest request
    ) {
        return handleTestUpload(request, questionId, testcaseId);
    }

    // GET all test cases for a question
    @GetMapping("/{questionId}/tests")
    public ResponseEntity<?> getAllTests(@PathVariable Long questionId) {
        try {
            List<ClassicTestcaseEntity> testcases = testcaseRepo.findByQuestionId(questionId);

            List<Map<String, Object>> testcaseList = testcases.stream()
                    .map(tc -> {
                        String fileName = "";
                        long fileSize = 0;

                        if (tc.getInputFilePath() != null && !tc.getInputFilePath().isEmpty()) {
                            File file = new File(tc.getInputFilePath());
                            if (file.exists()) {
                                fileName = file.getName();
                                fileSize = file.length();
                            }
                        }

                        // Use HashMap instead of Map.of() to avoid type inference issues
                        Map<String, Object> testMap = new HashMap<>();
                        testMap.put("id", tc.getId());
                        testMap.put("fileName", fileName);
                        testMap.put("size", fileSize);
                        testMap.put("inputFilePath", tc.getInputFilePath() != null ? tc.getInputFilePath() : "");
                        testMap.put("outputFilePath", tc.getOutputFilePath() != null ? tc.getOutputFilePath() : "");

                        return testMap;
                    })
                    .collect(Collectors.toList());

            // Reverse the list to show newest first
            Collections.reverse(testcaseList);

            return ResponseEntity.ok(Map.of("tests", testcaseList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch test cases");
        }
    }

    // GET specific test case file for download
    @GetMapping("/{questionId}/tests/{testcaseId}")
    public ResponseEntity<Resource> downloadTest(
            @PathVariable Long questionId,
            @PathVariable Long testcaseId
    ) {
        Optional<ClassicTestcaseEntity> opt = testcaseRepo.findById(testcaseId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        ClassicTestcaseEntity testcase = opt.get();

        // Verify the testcase belongs to the correct question
        if (!testcase.getQuestionId().equals(questionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String inputPath = testcase.getInputFilePath();
        if (inputPath == null || inputPath.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        File file = new File(inputPath);
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FileSystemResource resource = new FileSystemResource(file);

        // Determine MIME type
        String mime;
        try {
            mime = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            mime = null;
        }
        if (mime == null) {
            mime = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    // DELETE specific test case
    @DeleteMapping("/{questionId}/tests/{testcaseId}")
    public ResponseEntity<?> deleteTest(
            @PathVariable Long questionId,
            @PathVariable Long testcaseId
    ) {
        Optional<ClassicTestcaseEntity> opt = testcaseRepo.findById(testcaseId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Test case not found");
        }

        ClassicTestcaseEntity testcase = opt.get();

        // Verify the testcase belongs to the correct question
        if (!testcase.getQuestionId().equals(questionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Test case does not belong to this question");
        }

        // Delete the physical file
        try {
            if (testcase.getInputFilePath() != null) {
                new File(testcase.getInputFilePath()).delete();
            }
            if (testcase.getOutputFilePath() != null) {
                new File(testcase.getOutputFilePath()).delete();
            }
        } catch (Exception ignored) {}

        // Delete from database
        testcaseRepo.delete(testcase);

        return ResponseEntity.ok(Map.of("message", "Test case deleted successfully"));
    }

    // DELETE all test cases for a question
    @DeleteMapping("/{questionId}/tests")
    public ResponseEntity<?> deleteAllTests(@PathVariable Long questionId) {
        try {
            List<ClassicTestcaseEntity> testcases = testcaseRepo.findByQuestionId(questionId);

            // Delete physical files
            for (ClassicTestcaseEntity testcase : testcases) {
                try {
                    if (testcase.getInputFilePath() != null) {
                        new File(testcase.getInputFilePath()).delete();
                    }
                    if (testcase.getOutputFilePath() != null) {
                        new File(testcase.getOutputFilePath()).delete();
                    }
                } catch (Exception ignored) {}
            }

            // Delete from database
            testcaseRepo.deleteByQuestionId(questionId);

            return ResponseEntity.ok(Map.of("message", "All test cases deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete test cases");
        }
    }

    // --- Internal Helper ---
    private ResponseEntity<?> handleTestUpload(
            HttpServletRequest request,
            Long questionId,
            Long testcaseId // null for new upload, non-null for update
    ) {
        // 1) Wrap servlet request for Commons FileUpload
        RequestContext ctx = new RequestContext() {
            @Override public String getCharacterEncoding() { return request.getCharacterEncoding(); }
            @Override public String getContentType()       { return request.getContentType(); }
            @Override public int    getContentLength()     { return request.getContentLength(); }
            @Override public InputStream getInputStream() throws java.io.IOException {
                return request.getInputStream();
            }
        };

        // 2) Ensure multipart/form-data
        if (!ServletFileUpload.isMultipartContent(ctx)) {
            return ResponseEntity.badRequest().body("Form must be multipart/form-data");
        }

        // 3) Configure Commons FileUpload for mid-stream abort if size exceeds limit
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);

        try {
            // 4) Parse request
            List<org.apache.commons.fileupload.FileItem> items = upload.parseRequest(ctx);
            org.apache.commons.fileupload.FileItem fileItem = items.stream()
                    .filter(i -> !i.isFormField())
                    .findFirst()
                    .orElse(null);

            if (fileItem == null) {
                return ResponseEntity.badRequest().body("No file part in request");
            }

            ClassicTestcaseEntity testcase;

            if (testcaseId != null) {
                // Update existing testcase
                Optional<ClassicTestcaseEntity> opt = testcaseRepo.findById(testcaseId);
                if (opt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Test case not found");
                }
                testcase = opt.get();

                // Verify it belongs to the correct question
                if (!testcase.getQuestionId().equals(questionId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Test case does not belong to this question");
                }

                // Delete old file if exists
                if (testcase.getInputFilePath() != null) {
                    new File(testcase.getInputFilePath()).delete();
                }
            } else {
                // Create new testcase
                testcase = new ClassicTestcaseEntity();
                testcase.setQuestionId(questionId);
                testcase.setInputFilePath("");  // placeholder
                testcase.setOutputFilePath(""); // placeholder (empty as per requirement)
                testcase = testcaseRepo.save(testcase); // Get the generated ID
            }

            // 5) Save file to disk
            String originalName = Paths.get(fileItem.getName()).getFileName().toString();
            Path targetDir = Paths.get(
                    "E:/files",
                    questionId.toString(),
                    "testcases",
                    testcase.getId().toString()
                    ,"input"
            );
            Files.createDirectories(targetDir);
            File dest = targetDir.resolve(originalName).toFile();
            fileItem.write(dest);

            // 6) Update database with actual file path
            testcase.setInputFilePath(dest.getAbsolutePath());

            // 7) Create output.txt file with empty content
            Path outputDir = Paths.get(
                    "E:/files",
                    questionId.toString(),
                    "testcases",
                    testcase.getId().toString(),
                    "output"
            );
            Files.createDirectories(outputDir);
            Path outputPath = outputDir.resolve("output.txt");
            Files.write(outputPath, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            testcase.setOutputFilePath(outputPath.toAbsolutePath().toString());
            testcaseRepo.save(testcase);

            // 7) Return success response
            return ResponseEntity.ok(Map.of(
                    "message", testcaseId != null ? "Test case updated successfully" : "Test case uploaded successfully",
                    "testcaseId", testcase.getId(),
                    "fileName", originalName,
                    "filePath", dest.getAbsolutePath()
            ));

        } catch (FileUploadBase.FileSizeLimitExceededException ex) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File too large (max 5 MB)");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed");
        }
    }
}