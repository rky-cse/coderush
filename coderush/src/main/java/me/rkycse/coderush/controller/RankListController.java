package me.rkycse.coderush.controller;

import jakarta.validation.constraints.Positive;
import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.exception.ResourceNotFoundException;
import me.rkycse.coderush.service.RankListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tournaments")
@Validated  // Enables method parameter validation
public class RankListController {

    private final RankListService rankListService;

    public RankListController(RankListService rankListService) {
        this.rankListService = rankListService;
    }

    @GetMapping("/{tournamentId}/ranks")
    public ResponseEntity<List<RankDTO>> getRanksByTournamentId(
            @PathVariable @Positive(message = "Tournament ID must be positive") Long tournamentId) {

        List<RankDTO> ranks = rankListService.getRankListByTournamentId(tournamentId);

        if (ranks.isEmpty()) {
            throw new ResourceNotFoundException("No ranks found for tournament ID: " + tournamentId);
        }

        return ResponseEntity.ok(ranks);
    }

    // Exception handlers
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body("Invalid tournament ID format: " + ex.getValue());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }
}