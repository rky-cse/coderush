package me.rkycse.coderush.controller;

import jakarta.servlet.http.HttpServletRequest;
import me.rkycse.coderush.entity.ClassicTestcaseEntity;
import me.rkycse.coderush.repository.ClassicTestcaseRepository;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions/tests")
public class UploadController {

    private final ClassicTestcaseRepository testcaseRepo;

    public UploadController(ClassicTestcaseRepository testcaseRepo) {
        this.testcaseRepo = testcaseRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleUpload(HttpServletRequest request) {
        // 1) Wrap the Jakarta request in Commons RequestContext
        RequestContext ctx = new RequestContext() {
            @Override public String getCharacterEncoding() {
                return request.getCharacterEncoding();
            }
            @Override public String getContentType() {
                return request.getContentType();
            }
            @Override public int getContentLength() {
                return request.getContentLength();
            }
            @Override public InputStream getInputStream() throws java.io.IOException {
                return request.getInputStream();
            }
        };

        // 2) Verify multipart/form-data
        if (!ServletFileUpload.isMultipartContent(ctx)) {
            return ResponseEntity
                    .badRequest()
                    .body("Form must be multipart/form-data");
        }

        // 3) Configure Commons FileUpload
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1 * 1024 * 1024);   // 1 MB in-memory
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(5 * 1024 * 1024);      // 5 MB per-file
        upload.setSizeMax(10 * 1024 * 1024);         // 10 MB total request

        Long questionId = null;
        FileItem fileItem = null;

        try {
            // 4) Parse the request (streams + mid-stream abort)
            List<FileItem> items = upload.parseRequest(ctx);
            for (FileItem item : items) {
                if (item.isFormField() && "questionId".equals(item.getFieldName())) {
                    String raw = item.getString(StandardCharsets.UTF_8.name());
                    if (raw != null && !raw.isBlank()) {
                        questionId = Long.valueOf(raw.trim());
                    }
                } else if (!item.isFormField() && fileItem == null) {
                    fileItem = item;
                }
            }

            // 5) Validate presence
            if (questionId == null || fileItem == null) {
                return ResponseEntity
                        .badRequest()
                        .body("Missing questionId or file upload");
            }

            // 6) Persist entity to get generated testcaseId
            ClassicTestcaseEntity tc = new ClassicTestcaseEntity();
            tc.setQuestionId(questionId);
            tc.setInputFilePath("");
            tc.setOutputFilePath("");
            tc = testcaseRepo.save(tc);
            Long testcaseId = tc.getId();

            // 7) Build directory and write file
            String originalName = Paths
                    .get(fileItem.getName())
                    .getFileName()
                    .toString();
            Path targetDir = Paths.get(
                    "E:/files",
                    questionId.toString(),
                    "testcases",
                    testcaseId.toString()
            );
            Files.createDirectories(targetDir);

            File dest = targetDir.resolve(originalName).toFile();
            fileItem.write(dest);

            // 8) Update the DB record with actual path
            tc.setInputFilePath(dest.getAbsolutePath());
            testcaseRepo.save(tc);

            // 9) Respond with generated IDs and paths
            return ResponseEntity.ok(Map.of(
                    "message",    "Uploaded successfully",
                    "testcaseId", testcaseId,
                    "inputPath",  tc.getInputFilePath()
            ));

        } catch (FileUploadBase.FileSizeLimitExceededException ex) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File too large (max 5 MB)");
        } catch (FileUploadBase.SizeLimitExceededException ex) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("Total request size too large (max 10 MB)");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed");
        }
    }
}
