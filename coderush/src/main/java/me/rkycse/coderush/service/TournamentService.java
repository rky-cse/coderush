package me.rkycse.coderush.service;

import me.rkycse.coderush.config.RedisConfig;
import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.dto.TournamentDTO;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.TournamentEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.RankRepository;
import me.rkycse.coderush.repository.TournamentPlayerRepository;
import me.rkycse.coderush.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TournamentService {

    //HashMap<Long,TournamentEntity> tournaments=new HashMap<>();
    private final TournamentRepository tournamentRepository;

    private final TournamentPlayerRepository tournamentPlayerRepository;

    private final RankRepository rankRepository;

    private final RedisTemplate<String, TournamentCacheDTO> tournamentCacheDTORedisTemplate;
    private final RedisTemplate<String, RankDTO> rankListRedisTemplate;

    public TournamentService(
            TournamentRepository tournamentRepository,
            TournamentPlayerRepository tournamentPlayerRepository,
            RankRepository rankRepository,
            RedisTemplate<String, TournamentCacheDTO> tournamentCacheDTORedisTemplate, RedisTemplate<String, RankDTO> rankListRedisTemplate) {
        this.tournamentRepository = tournamentRepository;

        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.rankRepository = rankRepository;
        this.tournamentCacheDTORedisTemplate = tournamentCacheDTORedisTemplate;
        this.rankListRedisTemplate = rankListRedisTemplate;
    }


    public TournamentEntity getTournamentById(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null or empty");
        }

        // Search for the tournament in the list
        if(tournamentRepository.existsById(tournamentId)) {

            System.out.println("fetched tournament using "+tournamentId);
            return tournamentRepository.findById(tournamentId).orElse(null);

        }
        throw new NoSuchElementException("Tournament with ID " + tournamentId + " not found");
    }
    public TournamentEntity createTournament(TournamentEntity tournament) {


        if (tournament == null) {
            throw new IllegalArgumentException("Tournament object cannot be null");
        }

        // Assign a unique tournament ID
        //tournament.setTournamentId(UUID.randomUUID().toString());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String creatorUserName = userDetails.getUsername();
        tournament.setCreatorUserName(creatorUserName);


        if (tournament.getStartTime() == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        if (tournament.getDurationInSeconds() <= 0) {
            System.out.println("duration is: "+tournament.getDurationInSeconds());
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        TournamentEntity savedTournament=  tournamentRepository.save(tournament);
        System.out.println("Tournament created successfully: " + tournament);

        return savedTournament;
    }

    public JoinTournamentResponseDTO joinTournament(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        if(tournamentRepository.existsById(tournamentId)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            if(tournamentPlayerRepository.findByTournamentIdAndPlayerUserName(
                    tournamentId, userName) == null) {
                TournamentPlayerEntity tournamentPlayer=new TournamentPlayerEntity();
                tournamentPlayer.setTournamentId(tournamentId);
                tournamentPlayer.setPlayerUserName(userName);
                tournamentPlayerRepository.save(tournamentPlayer);
                return getJoinTournamentResponseDTO(tournamentId, userName);

            }
            else{
                TournamentPlayerEntity tournamentPlayer=
                        tournamentPlayerRepository
                                .findByTournamentIdAndPlayerUserName(tournamentId, userName);
                return getJoinTournamentResponseDTO(tournamentId, userName);

            }

        }
        else{
            throw  new NoSuchElementException("No such tournament");
        }


    }

    private JoinTournamentResponseDTO getJoinTournamentResponseDTO(Long tournamentId, String userName) {
        List<TournamentPlayerEntity> tournamentPlayerEntities=
                tournamentPlayerRepository
                        .findByTournamentId(tournamentId);

        RankEntity rankEntity=new RankEntity();
        rankEntity.setUserName(userName);
        rankEntity.setTournamentId(tournamentId);
        rankRepository.save(rankEntity);
        JoinTournamentResponseDTO joinTournamentResponseDTO=new JoinTournamentResponseDTO();
        for(TournamentPlayerEntity tournamentPlayerEntity : tournamentPlayerEntities) {
            joinTournamentResponseDTO.setTournamentId(tournamentId);
            joinTournamentResponseDTO.getTournamentPlayerList().add(Mapper.toDTO(tournamentPlayerEntity));
        }

        joinTournamentResponseDTO.setTournament(Mapper.toDTO(tournamentRepository
                .findById(tournamentId).orElse(null)));

        return joinTournamentResponseDTO;
    }


    public String startTournament(Long tournamentId) {
        if(tournamentCacheDTORedisTemplate.opsForValue().get("$"+tournamentId) != null){
            return "Tournament already started";
        }
        if(!tournamentRepository.existsById(tournamentId)) {
            throw new NoSuchElementException("Tournament with ID " + tournamentId + " not found");
        }
        else{
            TournamentEntity tournament=tournamentRepository
                    .findById(tournamentId).orElse(null);
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime startTime = tournament.getStartTime();
            Long durationInSeconds = tournament.getDurationInSeconds();
            LocalDateTime endTime = startTime.plusSeconds(durationInSeconds);
            Duration duration = Duration.between(currentTime, endTime);

            if(currentTime.isBefore(startTime)){
                return "Tournament Not Started Yet";
            }

            if(currentTime.isBefore(endTime) ) {
                TournamentCacheDTO tournamentCacheDTO=new TournamentCacheDTO();
                tournamentCacheDTO.setTournamentId(tournamentId);
                tournamentCacheDTO.setStartTime(startTime);
                tournamentCacheDTO.setScheduled(true);
                tournamentCacheDTO.setDurationInSeconds(durationInSeconds);
                tournamentCacheDTORedisTemplate
                        .opsForValue()
                        .set("$"+tournamentId,
                                tournamentCacheDTO,
                                duration.getSeconds(),
                                TimeUnit.SECONDS);
                List<RankEntity> rankListEntity= rankRepository.findByTournamentId(tournamentId);
                if(rankListEntity != null) {


                    for(RankEntity rankEntity:rankListEntity) {
                        RankDTO rankDTO= Mapper.toDTO(rankEntity);
                        if(rankDTO != null) {
                            rankListRedisTemplate.opsForValue()
                                    .set("@"+tournamentId
                                            +"/"+rankDTO.getUserName(),
                                            rankDTO, duration.getSeconds(),
                                            TimeUnit.SECONDS);

                        }
                    }

                }
                return "Tournament Started";

            }



        }
        throw new NoSuchElementException("Tournament with ID " + tournamentId + "not in the scheduled");

    }



}

