package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.RecentActivityDTO;
import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.dto.UserTournamentRatingDTO;
import me.rkycse.coderush.entity.RecentActivityEntity;
import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.RecentActivityRepository;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.repository.UserTournamentRatingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserTournamentRatingRepository tournamentRatingRepository;
    private final UserTournamentRatingRepository userTournamentRatingRepository;
    private final RecentActivityRepository recentActivityRepository;

    public UserService(UserRepository userRepository, UserTournamentRatingRepository tournamentRatingRepository, UserTournamentRatingRepository userTournamentRatingRepository, RecentActivityRepository recentActivityRepository) {
        this.userRepository = userRepository;
        this.tournamentRatingRepository = tournamentRatingRepository;
        this.userTournamentRatingRepository = userTournamentRatingRepository;
        this.recentActivityRepository = recentActivityRepository;
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
    public RecentActivityDTO getRecentActivityForUser(String username) {
        Optional<RecentActivityEntity> recentActivityOpt = recentActivityRepository.findByUsername(username);
        RecentActivityDTO dto = new RecentActivityDTO();
        dto.setUsername(username);

        if (recentActivityOpt.isPresent()) {
            RecentActivityEntity entity = recentActivityOpt.get();
            dto.setActivity(entity.getJson());
        } else {
            dto.setActivity(new HashMap<>()); // Return empty JSON instead of null
        }

        return dto;
    }
}
