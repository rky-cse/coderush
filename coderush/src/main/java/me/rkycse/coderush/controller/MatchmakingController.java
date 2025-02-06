package me.rkycse.coderush.controller;
import jakarta.servlet.http.HttpServletRequest;
import me.rkycse.coderush.dto.MatchResult;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.service.MatchmakingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {
    private final MatchmakingService matchmakingService;
    private final UserController userController;
    private final UserRepository userRepository;

    // Constructor injection for UserController
    public MatchmakingController(MatchmakingService matchmakingService, UserController userController, UserRepository userRepository) {
        this.matchmakingService = matchmakingService;
        this.userController = userController;
        this.userRepository = userRepository;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(HttpServletRequest request) {
        // Extract username using the UserController
        String username = userController.getUser();
        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: Missing or invalid username");
        }

        // Retrieve user from database
        Optional<UserEntity> user = userRepository.findByUserName(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        // Get user ID and rating
        Long userId = user.get().getId();
        Long rating = user.get().getRating();

        if( rating == null){
            System.out.println("Rating is null");
            return ResponseEntity.status(404).body("User rating not found");
        }

        // Process matchmaking
        MatchResult result = matchmakingService.joinQueue(String.valueOf(userId), rating);

        if (result != null) {
            System.out.println("Successfully made the tournament" + result.getTournamentID());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok("Waiting for opponent");
        }
    }
}
