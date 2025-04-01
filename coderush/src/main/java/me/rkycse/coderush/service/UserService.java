package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.dto.UserTournamentRatingDTO;
import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.repository.UserTournamentRatingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserTournamentRatingRepository tournamentRatingRepository;
    private final UserTournamentRatingRepository userTournamentRatingRepository;

    public UserService(UserRepository userRepository, UserTournamentRatingRepository tournamentRatingRepository, UserTournamentRatingRepository userTournamentRatingRepository) {
        this.userRepository = userRepository;
        this.tournamentRatingRepository = tournamentRatingRepository;
        this.userTournamentRatingRepository = userTournamentRatingRepository;
    }

    public UserDTO getUserByUsername(String username) {
        return Mapper.toDTO(userRepository.findByUserName(username).orElse(null));
    }
    public List<UserTournamentRatingDTO> getUserTournamentRating(String username) {
        List<UserTournamentRatingEntity>userTournamentRatingEntities=
                userTournamentRatingRepository.findByUsernameSortedByTimestamp(username);
        List<UserTournamentRatingDTO> userTournamentRatingDTOs=new ArrayList<>();
        for(UserTournamentRatingEntity userTournamentRatingEntity : userTournamentRatingEntities){
            userTournamentRatingDTOs.add(Mapper.toDto(userTournamentRatingEntity));
        }
        return userTournamentRatingDTOs;

    }
}
