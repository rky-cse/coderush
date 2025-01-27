package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.service.TestcaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcase")
public class TestcaseController {

    private final TestcaseService testcaseService;


    public TestcaseController(TestcaseService testcaseService) {
        this.testcaseService = testcaseService;
    }


    @PostMapping("/createTestcase")
    public ResponseEntity<String > createTestcase(@RequestBody TestcaseDTO testcase) {
        System.out.println(testcase);
        if (testcaseService.createTestcase(testcase)) {
            return ResponseEntity.ok("Success");

        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("getTestcasesByQuestionId/{questionId}")
    public ResponseEntity<List<TestcaseDTO>> getTestcasesByQuestionId( @PathVariable Long questionId) {
        return ResponseEntity.ok(testcaseService.getTestcasesByQuestionId(questionId));
    }


}
