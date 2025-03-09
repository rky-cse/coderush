package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getUserByUsername(String username) {
        return Mapper.toDTO(userRepository.findByUserName(username).orElse(null));
    }
}
