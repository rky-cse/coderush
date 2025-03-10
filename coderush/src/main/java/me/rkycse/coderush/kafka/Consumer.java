package me.rkycse.coderush.kafka;

import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TestcaseDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.dto.UserTestcaseDTO;
import me.rkycse.coderush.entity.*;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.*;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class Consumer {


    private final RedisTemplate<String,Object>redisTemplate;
    private final QuestionRepository questionRepository;
    private final TestcaseRepository testcaseRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TournamentQuestionRepository tournamentQuestionRepository;

    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final UserTestcaseRepository userTestcaseRepository;

    public Consumer(RedisTemplate<String, Object> redisTemplate, QuestionRepository questionRepository, TestcaseRepository testcaseRepository, TournamentPlayerRepository tournamentPlayerRepository, TournamentQuestionRepository tournamentQuestionRepository,  RankRepository rankRepository, UserRepository userRepository, UserTestcaseRepository userTestcaseRepository) {
        this.redisTemplate = redisTemplate;
        this.questionRepository = questionRepository;
        this.testcaseRepository = testcaseRepository;
        this.tournamentPlayerRepository = tournamentPlayerRepository;
        this.tournamentQuestionRepository = tournamentQuestionRepository;

        this.rankRepository = rankRepository;
        this.userRepository = userRepository;
        this.userTestcaseRepository = userTestcaseRepository;
    }


    @KafkaListener(topics = "rank-update", groupId = "myGroup")
    public void consume(RankEntity rank) {
        System.out.println("consuming.............................");
        if(rank != null) {
            RankEntity oldRank=rankRepository.findByUserNameAndTournamentId(rank.getUserName(), rank.getTournamentId());
            if(oldRank==null) {
                rankRepository.save(rank);
            }
            else{
                oldRank.setScore(rank.getScore());
                rankRepository.save(oldRank);

            }
        }
    }

    @KafkaListener(topics="user-testcase-update",groupId = "myGroup")
    public void consume(UserTestcaseEntity userTestcase) {
        System.out.println("consuming.............."+ userTestcase);
        if(userTestcase!=null) {
            UserTestcaseEntity oldUserTestcase=userTestcaseRepository.
                    findByUserNameAndTournamentIdAndTestcaseId(userTestcase.getUserName(),
                            userTestcase.getTournamentId(),userTestcase.getTestcaseId())
                    .orElse(null);
            if(oldUserTestcase==null) {
                userTestcaseRepository.save(userTestcase);
            }
            else{
                oldUserTestcase.setNumberOfAttempts(userTestcase.getNumberOfAttempts());
                oldUserTestcase.setSolved(userTestcase.getSolved());
                userTestcaseRepository.save(oldUserTestcase);
            }
        }


    }
    @KafkaListener(topics="start-tournament-init",groupId = "myGroup")
    public void consume(TournamentCacheDTO cacheDTO) {
        System.out.println("consuming.............................");
        Long tournamentId = cacheDTO.getTournamentId();
        if(tournamentId!=null) {
            List<TournamentPlayerEntity> tournamentPlayerEntities = tournamentPlayerRepository
                    .findByTournamentId(tournamentId);

            if (tournamentPlayerEntities == null || tournamentPlayerEntities.isEmpty()) {
                System.out.println("No tournament found for ID: " + tournamentId);
            } else {
                System.out.println("Found tournament with ID: " + tournamentId);
            }


            for (TournamentPlayerEntity tournamentPlayerEntity : tournamentPlayerEntities) {
                RankDTO rankDTO = new RankDTO();
                rankDTO.setTournamentId(tournamentId);
                rankDTO.setScore(0);
                rankDTO.setUserName(tournamentPlayerEntity.getPlayerUserName());

                System.out.println("inserting rank in tournament:");
                redisTemplate.opsForValue().set(
                        "rankDTO/" + tournamentId + "/" + rankDTO.getUserName(),
                        rankDTO,
                        cacheDTO.getDurationInSeconds(),
                        TimeUnit.SECONDS
                );

            }

            List<QuestionEntity> allQuestions = questionRepository.findAll();

            List<QuestionEntity> selectedQuestions = new ArrayList<>();

            Set<QuestionEntity> st = new HashSet<>();
            for (int i = 0; i < 5; i++) {
                int randomIndex = (int) (Math.random() * allQuestions.size());
                if (st.contains(allQuestions.get(randomIndex))) {
                    int j = randomIndex;
                    int ct = allQuestions.size();
                    while (ct-- > 0) {
                        if (!st.contains(allQuestions.get(j % (allQuestions.size())))) {
                            selectedQuestions.add(allQuestions.get(j % (allQuestions.size())));

                            st.add(allQuestions.get(j % (allQuestions.size())));
                            break;
                        }
                        j++;
                    }

                } else {
                    st.add(allQuestions.get(randomIndex));
                    selectedQuestions.add(allQuestions.get(randomIndex));
                }

            }
            int index = 0;

            for (QuestionEntity question : selectedQuestions) {
                System.out.println("QuestionId: " + question.getQuestionId());
                TournamentQuestionEntity tournamentQuestionEntity = new TournamentQuestionEntity();
                tournamentQuestionEntity.setQuestionId(question.getQuestionId());
                tournamentQuestionEntity.setTournamentId(tournamentId);
                System.out.println("saving tournament question: " + tournamentQuestionEntity);
                try{
                    TournamentQuestionEntity savedTournamentQuestion=
                            tournamentQuestionRepository.save(tournamentQuestionEntity);
                    System.out.println("saved tournament question: " + savedTournamentQuestion);

                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("error saving tournament question: " + tournamentQuestionEntity);
                }

                List<TestcaseEntity> testcases = testcaseRepository.
                        findByQuestionId(question.getQuestionId());
                for (TournamentPlayerEntity player : tournamentPlayerEntities) {
                    if (testcases.isEmpty()) continue;
                    int randomIndex = (int) (Math.random() * testcases.size());
                    TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));

                    if (testcaseDTO != null) {
                        UserTestcaseDTO userTestcaseDTO = new UserTestcaseDTO();
                        userTestcaseDTO.setTournamentId(tournamentId);
                        userTestcaseDTO.setTestcaseId(testcaseDTO.getTestcaseId());
                        userTestcaseDTO.setUserName(player.getPlayerUserName());
                        userTestcaseDTO.setSolved(false);
                        userTestcaseDTO.setNumberOfAttempts(0);

                        userTestcaseRepository.save(Mapper.toEntity(userTestcaseDTO));


                        redisTemplate.opsForValue().set(
                                "testcaseDTO/" + tournamentId + "/" + player.getPlayerUserName()
                                        + "/" + index,
                                testcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        redisTemplate.opsForValue().set(
                                "userTestcaseDTO/" + tournamentId + "/" +
                                        player.getPlayerUserName() + "/" + index,
                                userTestcaseDTO,
                                cacheDTO.getDurationInSeconds(),
                                TimeUnit.SECONDS
                        );
                        redisTemplate.opsForValue().set(
                                "questionDTO/" + tournamentId + "/" + index,
                                Mapper.toDTO(question),
                                cacheDTO.getDurationInSeconds(), TimeUnit.SECONDS
                        );
                    }
                }
                index++;
            }

            //questionListRedisTemplate.opsForValue().set("questionListEntity/" + tournamentId, selectedQuestions, cacheDTO.getDuration(), TimeUnit.SECONDS);
            redisTemplate.delete("tournament:" + tournamentId);
        }
    }

}
