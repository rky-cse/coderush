package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        if (mtmTournamentRepository.existsById(tournamentId)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            MTMTournamentEntity tournament = mtmTournamentRepository.findById(tournamentId)
                    .orElse(null);
            long currentTime = TimeUtil.getCurrentEpochMillis();
            long startTime=tournament.getStartTime();
            long endTime=tournament.getDurationInSeconds()*1000L+startTime;

            if (tournamentPlayerRepository.findByTournamentIdAndPlayerUserName(
                    tournamentId, userName) == null) {
                TournamentPlayerEntity tournamentPlayer = new TournamentPlayerEntity();
                tournamentPlayer.setTournamentId(tournamentId);
                tournamentPlayer.setPlayerUserName(userName);
                tournamentPlayerRepository.save(tournamentPlayer);
                if(currentTime < endTime && currentTime > startTime) {
                    startTournament(tournamentId);
                }
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
                    UserTestcaseDTO userTestcaseDTO = new UserTestcaseDTO();
                    userTestcaseDTO.setTestcaseId(testcaseDTO.getTestcaseId());
                    userTestcaseDTO.setUserName(tournamentPlayerEntity.getPlayerUserName());
                    userTestcaseDTO.setSolved(false);
                    userTestcaseDTO.setNumberOfAttempts(0);

                    redisTemplate.opsForValue().set(
                            "testcaseDTO/" + tournamentId + "/" +
                                    tournamentPlayerEntity.getPlayerUserName()
                                    + "/" + index,
                            testcaseDTO,
                            remainingTime,
                            TimeUnit.SECONDS
                    );
                    redisTemplate.opsForValue().set(
                            "userTestcaseDTO/" + tournamentId + "/" +
                                    tournamentPlayerEntity.getPlayerUserName() + "/" + index,
                            userTestcaseDTO,
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


}


