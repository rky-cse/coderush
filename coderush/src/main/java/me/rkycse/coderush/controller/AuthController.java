package me.rkycse.coderush.controller;

import me.rkycse.coderush.dto.LoginRequest;
import me.rkycse.coderush.dto.LoginResponse;
import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.exception.EmailExistsException;
import me.rkycse.coderush.exception.InvalidCredentialsException;
import me.rkycse.coderush.exception.UsernameExistsException;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.security.CustomUserDetailsService;
import me.rkycse.coderush.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        // Catch any auth failure (bad password, no such user, locked, etc.) and
        // collapse to a single generic error so we don't leak which usernames exist.
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails.getUsername());
        return new LoginResponse(token);
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody UserDTO userDto) {
        // Pre-validate unique fields so we can return a friendly error instead of
        // a generic 500 from the DB-level unique constraint violation.
        if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
            throw new UsernameExistsException();
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailExistsException();
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userDto.getUserName());
        userEntity.setFirstName(userDto.getFirstName());
        userEntity.setLastName(userDto.getLastName());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setRoles(List.of("ROLE_USER"));

        UserEntity savedUser = userRepository.save(userEntity);

        UserDTO responseDto = new UserDTO();
        responseDto.setId(savedUser.getId());
        responseDto.setUserName(savedUser.getUserName());
        responseDto.setFirstName(savedUser.getFirstName());
        responseDto.setLastName(savedUser.getLastName());
        responseDto.setEmail(savedUser.getEmail());
        responseDto.setRoles(savedUser.getRoles());
        return responseDto;
    }
}
