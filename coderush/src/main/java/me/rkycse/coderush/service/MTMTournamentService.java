package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import me.rkycse.coderush.mapper.Mapper;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MTMTournamentService {


    private final MTMTournamentRepository mtmTournamentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final TestcaseRepository testcaseRepository;

    public MTMTournamentService(MTMTournamentRepository mtmTournamentRepository, RedisTemplate<String, Object> redisTemplate, TournamentPlayerRepository tournamentPlayerRepository, RankRepository rankRepository, UserRepository userRepository, TestcaseRepository testcaseRepository) {
        this.mtmTournamentRepository = mtmTournamentRepository;
        this.redisTemplate = redisTemplate;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.rankRepository = rankRepository;
        this.userRepository = userRepository;
        this.testcaseRepository = testcaseRepository;
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
        } else {
            System.out.println("currentTime: " + TimeUtil.getCurrentEpochMillis());
            System.out.println("startTime: " + tournament.getStartTime());
            System.out.println("start Time:" +
                    TimeUtil.convertEpochToDateTime(tournament.getStartTime()));
            System.out.println("createMTMTournament:" + tournament);
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

        if (!mtmTournamentRepository.existsById(tournamentId)) {
            throw new NoSuchElementException("No such tournament");
        }

        // Retrieve the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userName = userDetails.getUsername();

        // Retrieve tournament details
        MTMTournamentEntity tournament = mtmTournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NoSuchElementException("Tournament not found"));

        long currentTime = TimeUtil.getCurrentEpochMillis();
        long startTime = tournament.getStartTime();
        long endTime = tournament.getDurationInSeconds() * 1000L + startTime;

        // Prevent joining if the tournament has already ended
        if (currentTime >= endTime) {
            throw new IllegalStateException("Tournament has already ended");
        }

        // Check if the user is already registered for this tournament (unique combination of tournamentId and username)
        TournamentPlayerEntity tournamentPlayer = tournamentPlayerRepository
                .findByTournamentIdAndPlayerUserName(tournamentId, userName);

        // Only save if the player doesn't exist already
        if (tournamentPlayer == null) {
            tournamentPlayer = new TournamentPlayerEntity();
            tournamentPlayer.setTournamentId(tournamentId);
            tournamentPlayer.setPlayerUserName(userName);
            tournamentPlayerRepository.save(tournamentPlayer);
        }

        // If the tournament is active, start it
        if (currentTime >= startTime && currentTime < endTime) {
            System.out.println("startTournament called in mtmTournamentService for tid: " + tournamentId);
            startTournament(tournamentId);
        }

        // Return the join tournament response
        return getJoinTournamentResponseDTO(tournamentId, userName);
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
        try {

            TournamentCacheDTO cacheDTO = (TournamentCacheDTO) redisTemplate
                    .opsForValue().get("$" + tournamentId);
            if (cacheDTO == null) {
                throw new NoSuchElementException("No cache found for tournament");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            System.out.println("joined after tournament start: " + userName);

            TournamentPlayerEntity tournamentPlayerEntity = tournamentPlayerRepository
                    .findByTournamentIdAndPlayerUserName(tournamentId, userName);
            long currentTime = TimeUtil.getCurrentEpochMillis();
            long startTime=cacheDTO.getStartTime();
            long remainingTime = (cacheDTO.getDurationInSeconds()*1000L+startTime-currentTime)/1000L;


            RankDTO rankDTO = new RankDTO();
            rankDTO.setTournamentId(tournamentId);
            rankDTO.setScore(0);
            rankDTO.setUserName(tournamentPlayerEntity.getPlayerUserName());

            System.out.println("inserting rank in tournament:");
            redisTemplate.opsForValue().set(
                    "rankDTO/" + tournamentId + "/" + rankDTO.getUserName(),
                    rankDTO,

                    remainingTime,
                    TimeUnit.SECONDS
            );

            Set<String> keys = redisTemplate.keys("questionDTO/" + tournamentId + "*");
            if (keys.isEmpty()) {
                throw new NoSuchElementException("No keys found for tournament");
            }
            List<QuestionDTO> selectedQuestions = new ArrayList<>();

            for (String key : keys) {
                QuestionDTO questionDTO = (QuestionDTO) redisTemplate.opsForValue().get(key);
                selectedQuestions.add(questionDTO);
            }

            int index = 0;

            for (QuestionDTO question : selectedQuestions) {
                System.out.println("QuestionId: " + question.getQuestionId());
                List<TestcaseEntity> testcases = testcaseRepository.
                        findByQuestionId(question.getQuestionId());

                if (testcases.isEmpty()) continue;
                int randomIndex = (int) (Math.random() * testcases.size());
                TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));


                if (testcaseDTO != null) {
                    SubmissionStatusDTO SubmissionStatusDTO = new SubmissionStatusDTO();
                    SubmissionStatusDTO.setQuestionId(question.getQuestionId());
                    SubmissionStatusDTO.setUserName(tournamentPlayerEntity.getPlayerUserName());
                    SubmissionStatusDTO.setSolved(false);
                    SubmissionStatusDTO.setNumberOfAttempts(0);

                    redisTemplate.opsForValue().set(
                            "testcaseDTO/" + tournamentId + "/" +
                                    tournamentPlayerEntity.getPlayerUserName()
                                    + "/" + index,
                            testcaseDTO,
                            remainingTime,
                            TimeUnit.SECONDS
                    );
                    redisTemplate.opsForValue().set(
                            "SubmissionStatusDTO/" + tournamentId + "/" +
                                    tournamentPlayerEntity.getPlayerUserName() + "/" + index,
                            SubmissionStatusDTO,
                            remainingTime,
                            TimeUnit.SECONDS
                    );
                    redisTemplate.opsForValue().set(
                            "questionDTO/" + tournamentId + "/" + index,
                            question,
                            remainingTime, TimeUnit.SECONDS
                    );
                }
                index++;
            }

        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging
        }
        return "success";
    }

    public List<MTMTournamentDTO> getLiveMTMTournaments() {
        List<MTMTournamentEntity> tournaments =
                mtmTournamentRepository.findLiveMTMTournaments(TimeUtil.getCurrentEpochMillis());
        List<MTMTournamentDTO> tournamentDTOs = new ArrayList<>();
        for(MTMTournamentEntity tournament : tournaments) {
            tournamentDTOs.add(Mapper.toDTO(tournament));

        }

        return tournamentDTOs;
    }
    public List<MTMTournamentDTO> getUpcomingMTMTournaments() {
        List<MTMTournamentEntity> tournaments =
                mtmTournamentRepository.findUpcomingMTMTournaments(TimeUtil.getCurrentEpochMillis());
        List<MTMTournamentDTO> tournamentDTOs = new ArrayList<>();
        for(MTMTournamentEntity tournament : tournaments) {
            tournamentDTOs.add(Mapper.toDTO(tournament));

        }
        return tournamentDTOs;
    }
    public List<Long> getRegisteredMTMTournamentsByUserName(String userName) {
        return tournamentPlayerRepository.tournamentIdsByPlayerUserName(userName);
    }

    public MTMTournamentDTO getMTMTournamentById(Long tournamentId) {
        return Mapper.toDTO(mtmTournamentRepository.findByTournamentId(tournamentId));
    }

    public Page<MTMTournamentDTO> findPastTournaments(Pageable pageable) {
//        Instant now = Instant.now();
        Page<MTMTournamentEntity> mtmtournamentsEntities =
                mtmTournamentRepository.findPastTournaments(TimeUtil.getCurrentEpochMillis(), pageable);
        return mtmtournamentsEntities.map(Mapper::toDTO);
    }


}


