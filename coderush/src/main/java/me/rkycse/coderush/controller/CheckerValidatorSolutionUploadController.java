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
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class CheckerValidatorSolutionUploadController {

    private final CheckerValidatorSolutionRepository repo;
    private static final long MAX_FILE_SIZE = 1 * 1024 * 1024;   // 1 MB
    private static final Path FILES_BASE = Paths.get(System.getProperty("user.dir")).getParent().resolve("files");

    public CheckerValidatorSolutionUploadController(CheckerValidatorSolutionRepository repo) {
        this.repo = repo;
    }

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

    @GetMapping("/checker/{questionId}")
    public ResponseEntity<Resource> getChecker(@PathVariable Long questionId) {
        return serveFile(questionId, "checker");
    }

    @GetMapping("/validator/{questionId}")
    public ResponseEntity<Resource> getValidator(@PathVariable Long questionId) {
        return serveFile(questionId, "validator");
    }

    @GetMapping("/solution/{questionId}")
    public ResponseEntity<Resource> getSolution(@PathVariable Long questionId) {
        return serveFile(questionId, "solution");
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> delete(@PathVariable Long questionId) {
        Optional<CheckerValidatorSolutionEntity> opt = repo.findByQuestionId(questionId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No record for questionId: " + questionId);
        }
        CheckerValidatorSolutionEntity ent = opt.get();

        try {
            deleteFileByRelativePath(ent.getCheckerFilePath());
            deleteFileByRelativePath(ent.getValidatorFilePath());
            deleteFileByRelativePath(ent.getSolutionFilePath());
        } catch (Exception ignored) {}

        repo.delete(ent);
        return ResponseEntity.noContent().build();
    }

    private void deleteFileByRelativePath(String relativePath) throws IOException {
        if (relativePath != null && !relativePath.isBlank()) {
            Path filePath = FILES_BASE.resolve(relativePath);
            Files.deleteIfExists(filePath);
        }
    }

    private ResponseEntity<Resource> serveFile(Long questionId, String type) {
        Optional<CheckerValidatorSolutionEntity> opt = repo.findByQuestionId(questionId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CheckerValidatorSolutionEntity ent = opt.get();
        String relPath = switch (type) {
            case "checker" -> ent.getCheckerFilePath();
            case "validator" -> ent.getValidatorFilePath();
            case "solution" -> ent.getSolutionFilePath();
            default -> null;
        };

        if (relPath == null || relPath.isBlank()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        Path filePath = FILES_BASE.resolve(relPath);
        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        String mime;
        try {
            mime = Files.probeContentType(filePath);
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

    private ResponseEntity<?> handleUpload(
            HttpServletRequest request,
            Long questionId,
            String type
    ) {
        RequestContext ctx = new RequestContext() {
            @Override public String getCharacterEncoding() { return request.getCharacterEncoding(); }
            @Override public String getContentType()       { return request.getContentType(); }
            @Override public int    getContentLength()     { return request.getContentLength(); }
            @Override public InputStream getInputStream() throws IOException {
                return request.getInputStream();
            }
        };

        if (!ServletFileUpload.isMultipartContent(ctx)) {
            return ResponseEntity.badRequest().body("Form must be multipart/form-data");
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);

        try {
            List<org.apache.commons.fileupload.FileItem> items = upload.parseRequest(ctx);
            org.apache.commons.fileupload.FileItem fileItem = items.stream()
                    .filter(i -> !i.isFormField())
                    .findFirst()
                    .orElse(null);

            if (fileItem == null) {
                return ResponseEntity.badRequest().body("No file part in request");
            }

            CheckerValidatorSolutionEntity ent = repo.findByQuestionId(questionId)
                    .orElseGet(() -> {
                        CheckerValidatorSolutionEntity e = new CheckerValidatorSolutionEntity();
                        e.setQuestionId(questionId);
                        return repo.save(e);
                    });

            String oldRelPath = switch (type) {
                case "checker" -> ent.getCheckerFilePath();
                case "validator" -> ent.getValidatorFilePath();
                case "solution" -> ent.getSolutionFilePath();
                default -> null;
            };
            if (oldRelPath != null) deleteFileByRelativePath(oldRelPath);

            String originalName = Paths.get(fileItem.getName()).getFileName().toString();
            Path relTargetDir = Paths.get(
                    questionId.toString(),
                    type,
                    ent.getId().toString()
            );
            Path absTargetDir = FILES_BASE.resolve(relTargetDir);
            Files.createDirectories(absTargetDir);

            Path absFilePath = absTargetDir.resolve(originalName);
            fileItem.write(absFilePath.toFile());

            String storedPath = relTargetDir.resolve(originalName).toString().replace("\\", "/");

            switch (type) {
                case "checker" -> ent.setCheckerFilePath(storedPath);
                case "validator" -> ent.setValidatorFilePath(storedPath);
                case "solution" -> ent.setSolutionFilePath(storedPath);
            }

            repo.save(ent);

            return ResponseEntity.ok(Map.of(
                    "message", "Uploaded/Updated successfully",
                    "entityId", ent.getId(),
                    "storedPath", storedPath
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
