package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.OneToOneGameEntity;
import me.rkycse.coderush.entity.QuestionEntity;
import me.rkycse.coderush.entity.TestcaseEntity;
import me.rkycse.coderush.entity.TournamentPlayerEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.OneToOneGameRepository;
import me.rkycse.coderush.repository.QuestionRepository;
import me.rkycse.coderush.repository.TestcaseRepository;
import me.rkycse.coderush.util.StringComparator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class OneToOneMatchService {

    private final OneToOneGameRepository oneToOneGameRepository;
    long[] timeIntervals = {1, 3, 5, 10, 15, 20, 30, 45, 60};
    long MAX_WAITING_TIME = 600000;

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<Long, PriorityBlockingQueue<OneToOneMatchRequestDTO>> oneToOneMatchQueue;
    private final ScheduledExecutorService[] scheduledExecutorServices = new ScheduledExecutorService[timeIntervals.length];
    private final RedisTemplate<String, OneToOneGameDTO> oneToOneGameRedisTemplate;
    private final ConcurrentMap<String,Long>wantToPlay = new ConcurrentHashMap<>();
    private final TestcaseRepository testcaseRepository;
    private final QuestionRepository questionRepository;
    private final RedisTemplate<String , QuestionDTO> questionDTORedisTemplate;
    private final RedisTemplate<String, TestcaseDTO> testcaseDTORedisTemplate;
    private final RedisTemplate<String, UserTestcaseDTO>userTestcaseDTORedisTemplate;
    private final RedisTemplate<String,RankDTO>rankDTORedisTemplate;
    private final ConcurrentMap<String,Boolean>playerACK=new ConcurrentHashMap<>();


    public OneToOneMatchService(
            SimpMessagingTemplate messagingTemplate, @Qualifier("initializedQueue")
            ConcurrentMap<Long, PriorityBlockingQueue<OneToOneMatchRequestDTO>> oneToOneMatchQueue, RedisTemplate<String, OneToOneGameDTO> oneToOneGameRedisTemplate, TestcaseRepository testcaseRepository, QuestionRepository questionRepository, RedisTemplate<String, QuestionDTO> questionDTORedisTemplate, RedisTemplate<String, TestcaseDTO> testcaseDTORedisTemplate, RedisTemplate<String, UserTestcaseDTO> userTestcaseDTORedisTemplate, OneToOneGameRepository oneToOneGameRepository, RedisTemplate<String, RankDTO> rankDTORedisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.oneToOneMatchQueue = oneToOneMatchQueue;
        this.oneToOneGameRedisTemplate = oneToOneGameRedisTemplate;
        this.testcaseRepository = testcaseRepository;
        this.questionRepository = questionRepository;
        this.questionDTORedisTemplate = questionDTORedisTemplate;
        this.testcaseDTORedisTemplate = testcaseDTORedisTemplate;
        this.userTestcaseDTORedisTemplate = userTestcaseDTORedisTemplate;
        this.rankDTORedisTemplate = rankDTORedisTemplate;


        for (int i = 0; i < timeIntervals.length; i++) {
            scheduledExecutorServices[i] = Executors.newSingleThreadScheduledExecutor();
            long timeControl = timeIntervals[i];

            scheduledExecutorServices[i].scheduleAtFixedRate(
                    () -> scheduleMatchesForTimeControl(timeControl),
                    0, 5 , TimeUnit.SECONDS
            );
        }
        this.oneToOneGameRepository = oneToOneGameRepository;
    }

    public void setPlayerACK(String userName) {
        playerACK.put(userName, true);
    }

    public void addToQueue(OneToOneMatchRequestDTO oneToOneMatchRequestDTO) {
        Long timeControl = oneToOneMatchRequestDTO.getTimeControl();
        if (timeControl == null) {
            throw new NoSuchElementException("TimeControl is null");
        }
        if (oneToOneMatchQueue.containsKey(timeControl)) {
            if (oneToOneMatchQueue.get(timeControl).size() > 1000000) {
                throw new RuntimeException("Queue Limit Exceeded");
            }
            if(wantToPlay.containsKey(oneToOneMatchRequestDTO.getUserName())){
                if(wantToPlay.get(oneToOneMatchRequestDTO.getUserName())
                        .equals(oneToOneMatchRequestDTO.getTimeControl())){
                    System.out.println("failed at line 59");
                    wantToPlay.put(oneToOneMatchRequestDTO.getUserName(), 0L);


                }
                else{

                    wantToPlay.put(oneToOneMatchRequestDTO.getUserName(),
                            oneToOneMatchRequestDTO.getTimeControl());
                    oneToOneMatchQueue.get(timeControl).add(oneToOneMatchRequestDTO);
                }
            }
            else{
                wantToPlay.put(oneToOneMatchRequestDTO.getUserName(),
                        oneToOneMatchRequestDTO.getTimeControl());
                oneToOneMatchQueue.get(timeControl).add(oneToOneMatchRequestDTO);

            }

        }
    }


    public void scheduleMatchesForTimeControl(long timeControl) {
        PriorityBlockingQueue<OneToOneMatchRequestDTO> queue = oneToOneMatchQueue.get(timeControl);
        if (queue == null || queue.isEmpty()) {
            //System.out.println("Queue is empty");
            return;
        }

        int maxIterations = queue.size() * 2; // Prevent infinite loops, 2 attempts per element
        int iterationCount = 0;
        boolean matchFound = false;

        while (!queue.isEmpty() && iterationCount < maxIterations) {
            iterationCount++;

            OneToOneMatchRequestDTO player1 = null;
            OneToOneMatchRequestDTO player2 = null;

            if(!queue.isEmpty()) {
                player1 = queue.poll();
                System.out.println("Player 1: " + player1.getUserName());
            }
            if(!queue.isEmpty()) {
                player2 = queue.poll();
                System.out.println("Player 2: " + player2.getUserName());
            }


            if(player1!=null){
                System.out.println("Player 1: " + player1.getUserName());
            }


            long currentTime = Instant.now().toEpochMilli();

            if (player1 != null && player2 != null) {
                long timeStamp1 = player1.getTimestamp();
                long timeStamp2 = player2.getTimestamp();
                if(player1.getUserName().equals(player2.getUserName())) {
                    if(timeStamp1>timeStamp2) {
                        if(queue.size()<1000000){
                            queue.add(player1);
                        }

                    }
                    else{
                        if(queue.size()<1000000){
                            queue.add(player2);
                        }
                    }
                    continue;
                }
                Boolean flag1 = false,flag2 = false;
                if(wantToPlay.get(player1.getUserName()).equals(0L)) {
                    wantToPlay.remove(player1.getUserName());
                    System.out.println("failed at line 128");
                    flag1 = true;
                }
                if(wantToPlay.get(player2.getUserName()).equals(0L)) {
                    wantToPlay.remove(player2.getUserName());
                    System.out.println("failed at line 133");
                    flag2 = true;
                }
                if(flag1 || flag2) {
                    if(flag1 && !flag2) {
                        if (player2 != null && currentTime - player2.getTimestamp() < MAX_WAITING_TIME) {
                            if(queue.size()<1000000){
                                queue.add(player2);

                            }
                        }

                    }
                    else if(flag2 && !flag1) {
                        if (player1 != null && currentTime - player1.getTimestamp() < MAX_WAITING_TIME) {
                            if(queue.size()<1000000){
                                queue.add(player1);
                            }
                        }
                    }
                    continue;
                }

                if (currentTime - timeStamp1 < MAX_WAITING_TIME
                        && currentTime - timeStamp2 < MAX_WAITING_TIME) {
                    startMatch(player1, player2);
                    wantToPlay.remove(player1.getUserName());
                    wantToPlay.remove(player2.getUserName());
                    matchFound = true;

                    continue;
                }
            }

            if (player1 != null && currentTime - player1.getTimestamp() < MAX_WAITING_TIME) {
                if(queue.size()<1000000){
                    queue.add(player1);

                }
            }

            if (player2 != null && currentTime - player2.getTimestamp() < MAX_WAITING_TIME) {
                if(queue.size()<1000000){
                    queue.add(player2);

                }
            }
        }

        if (iterationCount >= maxIterations) {
            System.out.println("Max iterations reached, potential infinite loop prevented.");
        }
    }


    public void startMatch(OneToOneMatchRequestDTO player1, OneToOneMatchRequestDTO player2) {
        System.out.println("match started between " + player1.getUserName() + " and " + player2.getUserName());
        OneToOneGameDTO oneToOneGameDTO = new OneToOneGameDTO();
        oneToOneGameDTO.setPlayer1(player1.getUserName());
        oneToOneGameDTO.setPlayer2(player2.getUserName());
        oneToOneGameDTO.setTimeControl(player1.getTimeControl());

        OneToOneGameEntity oneToOneGameEntity=
                oneToOneGameRepository.save(Mapper.toEntity(oneToOneGameDTO));
        oneToOneGameDTO.setTournamentId(oneToOneGameEntity.getTournamentId());

        oneToOneGameRedisTemplate.opsForValue()
                .set("oneToOneGame/"+oneToOneGameDTO.getTournamentId(),
                        oneToOneGameDTO, 20, TimeUnit.SECONDS);
        messagingTemplate.convertAndSend("/topic/oneToOneGame/requestPairing/" + player1.getUserName(), oneToOneGameDTO);
        messagingTemplate.convertAndSend("/topic/oneToOneGame/requestPairing/" + player2.getUserName(), oneToOneGameDTO);
    }

    public void startGame(Long tournamentId) {

        OneToOneGameDTO oneToOneGameDTO=
                oneToOneGameRedisTemplate.opsForValue().get("oneToOneGame/"+tournamentId);

        if(oneToOneGameDTO==null) {
            throw new IllegalArgumentException("invalid oneToOneGameDTO");
        }
        if(!playerACK.containsKey(oneToOneGameDTO.getPlayer1()) || !playerACK.containsKey(oneToOneGameDTO.getPlayer2())) {
            return;

        }
        List<QuestionEntity> allQuestions = questionRepository.findAll();

        List<QuestionEntity> selectedQuestions = new ArrayList<>();

        Set<QuestionEntity> st=new HashSet<>();
        for (int i = 0;i<5; i++) {
            int randomIndex = (int) (Math.random() * allQuestions.size());
            if(st.contains(allQuestions.get(randomIndex))){
                int j=randomIndex;
                int ct=allQuestions.size();
                while(ct-->0){
                    if(!st.contains(allQuestions.get(j%(allQuestions.size())))){
                        selectedQuestions.add(allQuestions.get(j%(allQuestions.size())));
                        st.add(allQuestions.get(j%(allQuestions.size())));
                        break;
                    }
                    j++;
                }

            }
            else{
                st.add(allQuestions.get(randomIndex));
                selectedQuestions.add(allQuestions.get(randomIndex));
            }

        }
        int index=0;

        for (QuestionEntity question : selectedQuestions) {
            System.out.println("QuestionId: " +question.getQuestionId() );
            List<TestcaseEntity> testcases = testcaseRepository.
                    findByQuestionId(question.getQuestionId());
            List<String>players=new ArrayList<>();
            for (String player : players) {
                if (testcases.isEmpty()) continue;
                int randomIndex = (int) (Math.random() * testcases.size());
                TestcaseDTO testcaseDTO = Mapper.toDTO(testcases.get(randomIndex));

                if (testcaseDTO != null) {
                    UserTestcaseDTO userTestcaseDTO = new UserTestcaseDTO();
                    userTestcaseDTO.setTestcaseId(testcaseDTO.getTestcaseId());
                    userTestcaseDTO.setUserName(player);
                    userTestcaseDTO.setIsSolved(false);
                    userTestcaseDTO.setNumberOfAttempts(0);

                    testcaseDTORedisTemplate.opsForValue().set(
                            "testcaseDTO/"+tournamentId+"/"+ player
                                    +"/"+index,
                            testcaseDTO,
                            oneToOneGameDTO.getTimeControl()*60,
                            TimeUnit.SECONDS
                    );
                    userTestcaseDTORedisTemplate.opsForValue().set(
                            "userTestcaseDTO/" +tournamentId+"/"+
                                    player + "/" + index,
                            userTestcaseDTO,
                            oneToOneGameDTO.getTimeControl()*60,
                            TimeUnit.SECONDS
                    );
                    questionDTORedisTemplate.opsForValue().set(
                            "questionDTO/"+tournamentId+"/"+index,
                            Mapper.toDTO(question),
                            oneToOneGameDTO.getTimeControl()*60,TimeUnit.SECONDS
                    );
                }
            }
            index++;
        }

    }

}