package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.JoinTournamentResponseDTO;
import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MTMTournamentService {


    private final MTMTournamentRepository mtmTournamentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final RankRepository rankRepository;
    private final UserRepository userRepository;

    public MTMTournamentService(MTMTournamentRepository mtmTournamentRepository, RedisTemplate<String, Object> redisTemplate, TournamentPlayerRepository tournamentPlayerRepository, RankRepository rankRepository, UserRepository userRepository) {
        this.mtmTournamentRepository = mtmTournamentRepository;
        this.redisTemplate = redisTemplate;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.rankRepository = rankRepository;
        this.userRepository = userRepository;
    }

    public MTMTournamentEntity createMTMTournament(MTMTournamentEntity tournament) {

        if (tournament == null) {
            throw new IllegalArgumentException("Tournament object cannot be null");
        }


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String creatorUserName = userDetails.getUsername();
        tournament.setCreatorId(userRepository.findIdByUserName(creatorUserName).orElse(null));


        if (tournament.getStartTime() == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        else{
            System.out.println("currentTime: "+TimeUtil.getCurrentEpochMillis());
            System.out.println("startTime: "+tournament.getStartTime());
            System.out.println("start Time:"+
                    TimeUtil.convertEpochToDateTime(tournament.getStartTime()) );
            System.out.println("createMTMTournament:"+tournament);
        }

        if (tournament.getDurationInSeconds() <= 0) {
            System.out.println("duration is: " + tournament.getDurationInSeconds());
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        MTMTournamentEntity savedTournament = mtmTournamentRepository.save(tournament);
        System.out.println("Tournament created successfully: " + savedTournament);

        return savedTournament;
    }


    public JoinTournamentResponseDTO joinTournament(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        if (mtmTournamentRepository.existsById(tournamentId)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            if (tournamentPlayerRepository.findByTournamentIdAndPlayerUserName(
                    tournamentId, userName) == null) {
                TournamentPlayerEntity tournamentPlayer = new TournamentPlayerEntity();
                tournamentPlayer.setTournamentId(tournamentId);
                tournamentPlayer.setPlayerUserName(userName);
                tournamentPlayerRepository.save(tournamentPlayer);
                return getJoinTournamentResponseDTO(tournamentId, userName);

            } else {
                TournamentPlayerEntity tournamentPlayer =
                        tournamentPlayerRepository
                                .findByTournamentIdAndPlayerUserName(tournamentId, userName);
                return getJoinTournamentResponseDTO(tournamentId, userName);

            }

        } else {
            throw new NoSuchElementException("No such tournament");
        }


    }

    private JoinTournamentResponseDTO getJoinTournamentResponseDTO(Long tournamentId, String userName) {
        List<TournamentPlayerEntity> tournamentPlayerEntities =
                tournamentPlayerRepository
                        .findByTournamentId(tournamentId);

        RankEntity rankEntity = new RankEntity();
        rankEntity.setUserName(userName);
        rankEntity.setTournamentId(tournamentId);
        rankRepository.save(rankEntity);
        JoinTournamentResponseDTO joinTournamentResponseDTO = new JoinTournamentResponseDTO();
        for (TournamentPlayerEntity tournamentPlayerEntity : tournamentPlayerEntities) {
            joinTournamentResponseDTO.setTournamentId(tournamentId);
            joinTournamentResponseDTO.getTournamentPlayerList().add(Mapper.toDTO(tournamentPlayerEntity));
        }

        joinTournamentResponseDTO.setTournament(Mapper.toDTO(mtmTournamentRepository
                .findById(tournamentId).orElse(null)));

        return joinTournamentResponseDTO;
    }


    public String startTournament(Long tournamentId) {
        if (redisTemplate.opsForValue().get("$" + tournamentId) != null) {
            return "Tournament already started";
        }
        if (!mtmTournamentRepository.existsById(tournamentId)) {
            throw new NoSuchElementException("Tournament with ID " + tournamentId + " not found");
        } else {
            MTMTournamentEntity tournament = mtmTournamentRepository
                    .findById(tournamentId).orElse(null);
            Long currentTime = TimeUtil.getCurrentEpochMillis() / 1000;
            Long startTime = tournament.getStartTime();
            Long durationInSeconds = tournament.getDurationInSeconds();
            Long endTime = startTime + durationInSeconds;


            if (currentTime < startTime) {
                return "Tournament Not Started Yet";
            }

            if (currentTime < endTime) {
                TournamentCacheDTO tournamentCacheDTO = new TournamentCacheDTO();
                tournamentCacheDTO.setTournamentId(tournamentId);
                tournamentCacheDTO.setStartTime(startTime);
                tournamentCacheDTO.setScheduled(true);
                tournamentCacheDTO.setDurationInSeconds(durationInSeconds);
                redisTemplate
                        .opsForValue()
                        .set("$" + tournamentId,
                                tournamentCacheDTO,
                                endTime - currentTime,
                                TimeUnit.SECONDS);
                List<RankEntity> rankListEntity = rankRepository.findByTournamentId(tournamentId);
                if (rankListEntity != null) {


                    for (RankEntity rankEntity : rankListEntity) {
                        RankDTO rankDTO = Mapper.toDTO(rankEntity);
                        if (rankDTO != null) {
                            redisTemplate.opsForValue()
                                    .set("@" + tournamentId
                                                    + "/" + rankDTO.getUserName(),
                                            rankDTO, endTime - currentTime,
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


