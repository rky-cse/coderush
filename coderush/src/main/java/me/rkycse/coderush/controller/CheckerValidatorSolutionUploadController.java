package me.rkycse.coderush.controller;

import jakarta.servlet.http.HttpServletRequest;
import me.rkycse.coderush.entity.CheckerValidatorSolutionEntity;
import me.rkycse.coderush.repository.CheckerValidatorSolutionRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class CheckerValidatorSolutionUploadController {

    private final CheckerValidatorSolutionRepository repo;
    private static final long MAX_FILE_SIZE = 1 * 1024 * 1024;   // 1 MB

    public CheckerValidatorSolutionUploadController(CheckerValidatorSolutionRepository repo) {
        this.repo = repo;
    }

    // CREATE / UPLOAD (also used for UPDATE via PUT)
    @PostMapping("/{type}/{questionId}")
    public ResponseEntity<?> upload(
            @PathVariable String type,
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        return handleUpload(request, questionId, type);
    }

    @PutMapping("/{type}/{questionId}")
    public ResponseEntity<?> update(
            @PathVariable String type,
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        return handleUpload(request, questionId, type);
    }


    // GET checker file for a questionId
    @GetMapping("/checker/{questionId}")
    public ResponseEntity<Resource> getChecker(@PathVariable Long questionId) {
        return serveFile(questionId, "checker");
    }

    // GET validator file for a questionId
    @GetMapping("/validator/{questionId}")
    public ResponseEntity<Resource> getValidator(@PathVariable Long questionId) {
        return serveFile(questionId, "validator");
    }

    // GET solution file for a questionId
    @GetMapping("/solution/{questionId}")
    public ResponseEntity<Resource> getSolution(@PathVariable Long questionId) {
        return serveFile(questionId, "solution");
    }

    // DELETE entire record by questionId
    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> delete(@PathVariable Long questionId) {
        Optional<CheckerValidatorSolutionEntity> opt = repo.findByQuestionId(questionId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No record for questionId: " + questionId);
        }
        CheckerValidatorSolutionEntity ent = opt.get();
        // delete files if present
        try {
            if (ent.getCheckerFilePath()   != null) new File(ent.getCheckerFilePath()).delete();
            if (ent.getValidatorFilePath() != null) new File(ent.getValidatorFilePath()).delete();
            if (ent.getSolutionFilePath()  != null) new File(ent.getSolutionFilePath()).delete();
        } catch (Exception ignored) {}
        repo.delete(ent);
        return ResponseEntity.noContent().build();
    }

    // --- Internal Helpers ---

    private ResponseEntity<Resource> serveFile(Long questionId, String type) {
        Optional<CheckerValidatorSolutionEntity> opt = repo.findByQuestionId(questionId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CheckerValidatorSolutionEntity ent = opt.get();

        // Resolve file path based on type
        String path = switch (type) {
            case "checker" -> ent.getCheckerFilePath();
            case "validator" -> ent.getValidatorFilePath();
            case "solution" -> ent.getSolutionFilePath();
            default -> null;
        };

        if (path == null || path.isBlank()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        File file = new File(path);
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

        // Return the file with headers
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }


    private ResponseEntity<?> handleUpload(
            HttpServletRequest request,
            Long questionId,
            String type
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

        // 3) Configure Commons FileUpload for mid-stream abort >1MB
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);

        try {
            // 4) Parse; throws FileSizeLimitExceededException if >1MB
            List<org.apache.commons.fileupload.FileItem> items = upload.parseRequest(ctx);
            org.apache.commons.fileupload.FileItem fileItem = items.stream()
                    .filter(i -> !i.isFormField())
                    .findFirst()
                    .orElse(null);

            if (fileItem == null) {
                return ResponseEntity.badRequest().body("No file part in request");
            }

            // 5) Load or create entity by questionId
            CheckerValidatorSolutionEntity ent = repo.findByQuestionId(questionId)
                    .orElseGet(() -> {
                        CheckerValidatorSolutionEntity e = new CheckerValidatorSolutionEntity();
                        e.setQuestionId(questionId);
                        return repo.save(e);
                    });

            // 6) If updating, delete old file
            String oldPath;
            switch (type) {
                case "checker":   oldPath = ent.getCheckerFilePath();   break;
                case "validator": oldPath = ent.getValidatorFilePath(); break;
                case "solution":  oldPath = ent.getSolutionFilePath();  break;
                default: return ResponseEntity.badRequest().body("Unknown type: " + type);
            }
            if (oldPath != null) new File(oldPath).delete();

            // 7) Write new file under E:/files/{questionId}/{type}/{entityId}/
            String originalName = Paths.get(fileItem.getName()).getFileName().toString();
            Path targetDir = Paths.get(
                    "E:/files",
                    questionId.toString(),
                    type,
                    ent.getId().toString()
            );
            Files.createDirectories(targetDir);
            File dest = targetDir.resolve(originalName).toFile();
            fileItem.write(dest);

            // 8) Update entity's path field
            String absPath = dest.getAbsolutePath();
            switch (type) {
                case "checker":   ent.setCheckerFilePath(absPath);   break;
                case "validator": ent.setValidatorFilePath(absPath); break;
                case "solution":  ent.setSolutionFilePath(absPath);  break;
            }
            repo.save(ent);

            // 9) Return success
            return ResponseEntity.ok(Map.of(
                    "message",  "Uploaded/Updated successfully",
                    "entityId", ent.getId(),
                    "filePath", absPath
            ));
        }
        catch (FileUploadBase.FileSizeLimitExceededException ex) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File too large (max 1 MB)");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Operation failed");
        }
    }
}
