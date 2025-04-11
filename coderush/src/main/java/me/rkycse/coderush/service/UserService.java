package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.MTMTournamentDTO;
import me.rkycse.coderush.dto.MTMTournamentTabDTO;
import me.rkycse.coderush.dto.RecentActivityDTO;
import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.dto.UserTournamentRatingDTO;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.RecentActivityEntity;
import me.rkycse.coderush.entity.TournamentEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.MTMTournamentRepository;
import me.rkycse.coderush.repository.RankRepository;
import me.rkycse.coderush.repository.RecentActivityRepository;
import me.rkycse.coderush.repository.TournamentPlayerRepository;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.repository.UserTournamentRatingRepository;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final MTMTournamentRepository mtmTournamentRepository;

    private final UserRepository userRepository;
    private final UserTournamentRatingRepository tournamentRatingRepository;
    private final UserTournamentRatingRepository userTournamentRatingRepository;
    private final RecentActivityRepository recentActivityRepository;
    private final RankRepository rankRepository;

    public UserService(UserRepository userRepository, UserTournamentRatingRepository tournamentRatingRepository, UserTournamentRatingRepository userTournamentRatingRepository, RecentActivityRepository recentActivityRepository, TournamentPlayerRepository tournamentPlayerRepository, RankRepository rankRepository, MTMTournamentRepository mtmTournamentRepository) {
        this.userRepository = userRepository;
        this.tournamentRatingRepository = tournamentRatingRepository;
        this.userTournamentRatingRepository = userTournamentRatingRepository;
        this.recentActivityRepository = recentActivityRepository;
        this.rankRepository = rankRepository;
        this.mtmTournamentRepository = mtmTournamentRepository;
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
    public Page<MTMTournamentTabDTO> getUserTournaments(String username, int page, int size, String sortBy, String direction) {
        // Create sort object
        System.out.println("maamla service tak bhi pahucha!");
        if (sortBy.equals("startDate")) {
            sortBy = "startTime"; // Map to the actual field name
        }
        Sort sort = Sort.by(direction.equalsIgnoreCase("asc") ? 
                    Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get tournament IDs where the user has penalty > 0
        // Need to update this repository method to support pagination
        Page<Long> tournamentIdPage = rankRepository.findTournamentIdsWithPenaltyByUserName(username, pageable);
        
        if (tournamentIdPage.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Get tournament details for these IDs
        List<MTMTournamentEntity> tournaments = mtmTournamentRepository.findAllById(tournamentIdPage.getContent());
        
        // Convert to DTOs with user-specific information
        List<MTMTournamentTabDTO> dtoList = tournaments.stream()
            .map(tournament -> {
                MTMTournamentTabDTO dto = new MTMTournamentTabDTO();

                // Copy base fields from entity to DTO
            dto.setName(tournament.getName());
            dto.setDescription(tournament.getDescription());
            dto.setStartTime(tournament.getStartTime());
            dto.setTournamentId(tournament.getTournamentId()); // Make sure to set ID for frontend linking
            
            // Copy MTM-specific fields
            dto.setCreatorId(tournament.getCreatorId());
            dto.setMinRatingReq(tournament.getMinRatingReq());
            dto.setMaxRatingReq(tournament.getMaxRatingReq());
            dto.setTeamStyle(tournament.getTeamStyle());
            
            // // Set status based on dates
            // if (tournament.getStartTime() != null) {
            //     if (tournament.getStartTime().after(new Date())) {
            //         dto.setStatus("upcoming");
            //     } else if (tournament.getEndTime() == null || tournament.getEndTime().after(new Date())) {
            //         dto.setStatus("ongoing");
            //     } else {
            //         dto.setStatus("completed");
            //     }
            // } else {
            //     dto.setStatus("unknown");
            // }
            // Add user-specific information from RankEntity
            RankEntity rank = rankRepository.findByTournamentIdAndUserName(tournament.getTournamentId(), username);
            if (rank != null) {
                dto.setScore(rank.getScore());
                dto.setPenalty(rank.getPenalty());
                dto.setRating(rank.getRating());
                
                // Set user role if available
                // dto.setUserRole("participant"); // Modify as needed based on your data model
            }
            
            return dto;
        })
        .collect(Collectors.toList());
    
    // Return a Page object with the total count from the original tournament ID page
    return new PageImpl<>(dtoList, pageable, tournamentIdPage.getTotalElements());
    }

    /**
 * Migration method to populate startTime field in RankEntity from tournament data
 * @return Number of records updated
 */
    @Transactional
    public int migrateStartTimeToRankEntities() {
        List<RankEntity> allRanks = rankRepository.findAll();
        int updatedCount = 0;
        
        for (RankEntity rank : allRanks) {
            if (rank.getStartTime() == null) { // Only update if not already set
                MTMTournamentEntity tournament = mtmTournamentRepository.findById(rank.getTournamentId())
                    .orElse(null);
                
                if (tournament != null && tournament.getStartTime() != null) {
                    rank.setStartTime(tournament.getStartTime());
                    updatedCount++;
                }
            }
        }
        
        rankRepository.saveAll(allRanks);
        System.out.println("Migration completed: " + updatedCount + " rank records updated with startTime.");
        return updatedCount;
    }
}
