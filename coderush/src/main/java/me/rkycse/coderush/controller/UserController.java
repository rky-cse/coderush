package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.RecentActivityDTO;
import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.dto.UserTournamentRatingDTO;
import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import me.rkycse.coderush.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin("*")
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String userName = userDetails.getUsername();
        return ResponseEntity.ok(userService.getUserByUsername(userName));
    }

    @GetMapping("/{userName}")
    public ResponseEntity<UserDTO> getUserByUserName(@PathVariable String userName) {
        return ResponseEntity.ok(userService.getUserByUsername(userName));
    }

    @GetMapping("/getRatingHistory")
    public ResponseEntity<List<UserTournamentRatingDTO>> getRatingHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String userName = userDetails.getUsername();
        return ResponseEntity.ok(userService.getUserTournamentRating(userName));

    }
    @GetMapping("/getRecentActivity/{userName}")
    public ResponseEntity<RecentActivityDTO> getRecentActivity(@PathVariable String userName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();
        if(!Objects.equals(username, userName)) System.out.println("I will check that auth stuff later!");
        else System.out.println("WHATS GOING ON!!!!!!!!!!!!\n\n\n\n\n\n\n");
        RecentActivityDTO dto = userService.getRecentActivityForUser(userName);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/{userName}/tournaments")
    public ResponseEntity<?> getUserTournaments(@PathVariable String userName,@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "startDate") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(userService.getUserTournaments(userName, page, size, sortBy, direction));
    }

    
}
