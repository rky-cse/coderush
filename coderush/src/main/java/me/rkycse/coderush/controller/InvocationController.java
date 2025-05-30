// src/main/java/me/rkycse/coderush/controller/InvocationController.java
package me.rkycse.coderush.controller;

import me.rkycse.coderush.exception.ResourceNotFoundException;
import me.rkycse.coderush.service.InvocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class InvocationController {
    private static final Logger log = LoggerFactory.getLogger(InvocationController.class);
    private final InvocationService invocationService;

    public InvocationController(InvocationService invocationService) {
        this.invocationService = invocationService;
    }

    @GetMapping("/invocation/{questionId}")
    public ResponseEntity<String> invokeQuestion(@PathVariable Long questionId) {
        try {
            log.debug("Received invocation request for questionId={}", questionId);
            invocationService.handleInvocation(questionId);
            return ResponseEntity.ok("Invocation payload sent to Kafka.");
        } catch (ResourceNotFoundException rnfe) {
            log.warn("Resource not found: {}", rnfe.getMessage());
            return ResponseEntity.status(404).body(rnfe.getMessage());
        } catch (IllegalArgumentException iae) {
            log.warn("Bad request: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during invocation for questionId={}", questionId, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
