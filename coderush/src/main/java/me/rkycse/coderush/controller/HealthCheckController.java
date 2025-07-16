package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.HealthStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller for application health checks.
 */
@RestController
@RequestMapping("api")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<HealthStatusDTO> health() {
        long now = Instant.now().toEpochMilli();
        HealthStatusDTO dto = new HealthStatusDTO("UP", now);
        return ResponseEntity.ok(dto);
    }
}
