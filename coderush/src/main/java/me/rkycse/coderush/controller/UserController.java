package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLOutput;

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

}
