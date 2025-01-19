package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.LoginRequest;
import me.rkycse.coderush.dto.LoginResponse;
import me.rkycse.coderush.dto.UserDto;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.security.CustomUserDetailsService;
import me.rkycse.coderush.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          CustomUserDetailsService customUserDetailsService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public LoginResponse authenticate(@RequestBody LoginRequest loginRequest) {
        // Perform authentication with the given credentials
        System.out.println(loginRequest);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword())
        );


        // Retrieve the authenticated UserDetails from the authentication object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println(userDetails.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails.getUsername());

        // Return the JWT token in the response
        return new LoginResponse(token);
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody UserDto userDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userDto.getUserName());
        userEntity.setFirstName(userDto.getFirstName());
        userEntity.setLastName(userDto.getLastName());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setRoles(List.of("ROLE_USER")); // adjust roles as needed

        UserEntity savedUser = userRepository.save(userEntity);

        // Convert savedUser back to a DTO (excluding password)
        UserDto responseDto = new UserDto();
        responseDto.setId(savedUser.getId());
        responseDto.setUserName(savedUser.getUserName());
        responseDto.setFirstName(savedUser.getFirstName());
        responseDto.setLastName(savedUser.getLastName());
        responseDto.setEmail(savedUser.getEmail());
        responseDto.setRoles(savedUser.getRoles());

        return responseDto;
    }
}