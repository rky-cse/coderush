package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.service.TestcaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/testcase")
public class TestcaseController {

    private final TestcaseService testcaseService;

    public TestcaseController(TestcaseService testcaseService) {
        this.testcaseService = testcaseService;
    }

    @PostMapping("/createTestcase")
    public ResponseEntity<TestcaseDTO> createTestcase(@RequestBody TestcaseDTO testcase) {
        System.out.println(testcase);
        TestcaseDTO createdTestcase = testcaseService.createTestcase(testcase);
        if (createdTestcase != null) {
            return ResponseEntity.ok(createdTestcase);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{testcaseId}")
    public ResponseEntity<TestcaseDTO> getTestcaseById(@PathVariable Long testcaseId) {
        Optional<TestcaseDTO> testcase = testcaseService.getTestcaseById(testcaseId);
        if (testcase.isPresent()) {
            return ResponseEntity.ok(testcase.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getTestcasesByQuestionId/{questionId}")
    public ResponseEntity<List<TestcaseDTO>> getTestcasesByQuestionId(@PathVariable Long questionId) {
        List<TestcaseDTO> testcases = testcaseService.getTestcasesByQuestionId(questionId);
        return ResponseEntity.ok(testcases);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TestcaseDTO>> getAllTestcases() {
        List<TestcaseDTO> testcases = testcaseService.getAllTestcases();
        return ResponseEntity.ok(testcases);
    }

    @PutMapping("/{testcaseId}")
    public ResponseEntity<TestcaseDTO> updateTestcase(@PathVariable Long testcaseId, @RequestBody TestcaseDTO testcase) {
        testcase.setTestcaseId(testcaseId);
        TestcaseDTO updatedTestcase = testcaseService.updateTestcase(testcase);
        if (updatedTestcase != null) {
            return ResponseEntity.ok(updatedTestcase);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{testcaseId}")
    public ResponseEntity<String> deleteTestcase(@PathVariable Long testcaseId) {
        boolean deleted = testcaseService.deleteTestcase(testcaseId);
        if (deleted) {
            return ResponseEntity.ok("Testcase deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteTestcasesByQuestionId/{questionId}")
    public ResponseEntity<String> deleteTestcasesByQuestionId(@PathVariable Long questionId) {
        boolean deleted = testcaseService.deleteTestcasesByQuestionId(questionId);
        if (deleted) {
            return ResponseEntity.ok("All testcases for question deleted successfully");
        }
        return ResponseEntity.badRequest().build();
    }
}