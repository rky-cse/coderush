# CodeRush System Architecture

```mermaid
graph TD
    Client[Frontend - NextJS] <--> API[Spring Boot API Layer]
    Client <-.WebSocket.-> WebSocket[WebSocket Controller]
    API --> AuthService[Authentication Service]
    API --> TournamentService[Tournament Service]
    API --> QuestionService[Question Service]
    API --> UserService[User Service]
    API --> MatchmakingService[Matchmaking Service]
    
    WebSocket --> TournamentWebSocketService
    
    TournamentService --> MTMTournamentService
    TournamentService --> DuelTournamentService
    TournamentService --> RankListService
    
    AuthService --> UserRepo[(User Repository)]
    TournamentService --> TournamentRepo[(Tournament Repository)]
    QuestionService --> QuestionRepo[(Question Repository)]
    
    TournamentService -- Publishes --> KafkaProducer[Kafka Producer]
    KafkaProducer --> Kafka[Kafka Message Broker]
    Kafka --> KafkaConsumer[Kafka Consumer]
    KafkaConsumer --> JudgeService[Judge Service]
    JudgeService --> KafkaProducer
    
    MatchmakingService <--> Redis[(Redis Cache)]
    TournamentWebSocketService <--> Redis
    TournamentService <--> Redis
    
    UserRepo --> PostgreSQL[(PostgreSQL Database)]
    TournamentRepo --> PostgreSQL
    QuestionRepo --> PostgreSQL
    
    FileStorage[File Storage] <--> QuestionService
    FileStorage <--> JudgeService
```

## Key Components:

1. **Frontend**: NextJS-based client application
2. **Backend Services**: Spring Boot microservices
3. **Data Stores**: PostgreSQL for persistence, Redis for caching and real-time data
4. **Message Queue**: Kafka for asynchronous processing
5. **File Storage**: For storing question testcases, solutions, checkers, and validators
6. **WebSocket**: For real-time communication during tournaments

## Communication Patterns:

1. **REST API**: For standard CRUD operations
2. **WebSocket/STOMP**: For real-time updates during tournaments
3. **Kafka**: For asynchronous processing of submissions and tournament events
4. **Redis**: For fast data access, caching, and matchmaking



# Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant AuthenticationManager
    participant UserService
    participant UserRepo
    participant PasswordEncoder
    participant JWTProvider
    
    %% Registration Flow
    User->>Frontend: Fills registration form
    Frontend->>AuthController: POST /api/auth/register
    AuthController->>PasswordEncoder: encode(password)
    PasswordEncoder-->>AuthController: encodedPassword
    AuthController->>UserService: createUser(userDTO)
    UserService->>UserRepo: save(userEntity)
    UserRepo-->>UserService: savedUser
    UserService-->>AuthController: userDTO
    AuthController-->>Frontend: User registered (200 OK)
    Frontend-->>User: Registration success
    
    %% Login Flow
    User->>Frontend: Enter credentials
    Frontend->>AuthController: POST /api/auth/login
    AuthController->>AuthenticationManager: authenticate(username, password)
    AuthenticationManager->>UserService: loadUserByUsername(username)
    UserService->>UserRepo: findByUsername(username)
    UserRepo-->>UserService: userEntity
    UserService-->>AuthenticationManager: userDetails
    AuthenticationManager->>PasswordEncoder: matches(password, encodedPassword)
    PasswordEncoder-->>AuthenticationManager: true/false
    AuthenticationManager-->>AuthController: Authentication object
    AuthController->>JWTProvider: generateToken(authentication)
    JWTProvider-->>AuthController: JWT token
    AuthController-->>Frontend: Token + user info
    Frontend->>Frontend: Store token in cookies
    Frontend-->>User: Redirect to dashboard
```

## Authentication Process:

1. **Registration**: User details are captured, password is hashed with BCrypt, and user entity is persisted
2. **Login**: Username and password are validated, JWT token is generated upon successful authentication
3. **Authorization**: JWT token is included in every subsequent request (REST or WebSocket) for authorization
4. **Security**: Spring Security with JWT for stateless authentication



# Tournament Lifecycle

```mermaid
graph TD
    Creation[Tournament Creation] --> Scheduled[Tournament Scheduled]
    Scheduled -- 7s before start --> Cached[Cached in Redis]
    Cached -- At start time --> Started[Tournament Started]
    Started --> InitData[Initialize Tournament Data]
    InitData --> Running[Tournament Running]
    Running -- Duration ended --> Ended[Tournament Ended]
    Ended --> RatingUpdate[Rating Update]
    RatingUpdate --> ActivityUpdate[Update User Activity]
    
    subgraph Creation
        Creator[Creator] --> CreateAPI[Create Tournament API]
        CreateAPI --> Validate[Validate Parameters]
        Validate --> SaveDB[Save to Database]
    end
    
    subgraph Scheduled
        SchedulerService[Tournament Scheduler] -- Every 5s --> FetchUpcoming[Fetch Upcoming Tournaments]
        FetchUpcoming --> CacheRedis[Cache in Redis]
    end
    
    subgraph InitData
        InitKafka[Send Start Tournament Event] --> InitRanks[Initialize Ranks]
        InitRanks --> SelectQuestions[Select Tournament Questions]
        SelectQuestions --> PrepareTestcases[Prepare Testcases]
        PrepareTestcases --> CacheTournData[Cache Tournament Data]
    end
    
    subgraph Running
        Player[Player] --> SubmitSolution[Submit Solution]
        SubmitSolution --> KafkaJudge[Send to Judge via Kafka]
        KafkaJudge --> ProcessResult[Process Result]
        ProcessResult --> UpdateRanks[Update Ranks]
        UpdateRanks --> BroadcastRanks[Broadcast Ranks]
    end
    
    subgraph RatingUpdate
        FetchRanks[Fetch All Ranks] --> CalculateElo[Calculate ELO Changes]
        CalculateElo --> StagingTable[Create Staging Table]
        StagingTable --> BulkUpdate[Bulk Update Ratings]
        BulkUpdate --> RecordHistory[Record Rating History]
    end
```

## Tournament States:

1. **Creation**: Users create tournaments with specific parameters (duration, visibility, type)
2. **Scheduled**: Upcoming tournaments are tracked and cached before start time
3. **Started**: At the exact start time, tournament initialization occurs
4. **Running**: Players solve problems, submit solutions, and rankings are updated in real-time
5. **Ended**: After duration expires, final rankings are established
6. **Rating Update**: Player ratings are recalculated using an ELO-based algorithm
7. **Activity Update**: User activity records are updated for dashboard display



# Submission and Judging Pipeline

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant WebSocketController
    participant KafkaProducer
    participant KafkaQueue as Kafka
    participant JudgeService
    participant KafkaConsumer
    participant Redis
    participant TournamentWebSocketService
    participant Database
    
    %% Classic Submission Flow
    User->>Frontend: Submit code solution
    Frontend->>WebSocketController: STOMP /app/tournament/classicSubmit
    WebSocketController->>KafkaProducer: sendClassicSubmission()
    KafkaProducer->>Redis: Increment judgeCount
    KafkaProducer->>KafkaQueue: Publish to classical-submission topic
    KafkaQueue->>JudgeService: Consume submission
    
    JudgeService->>JudgeService: Compile code
    JudgeService->>JudgeService: Run test cases
    JudgeService->>JudgeService: Evaluate results
    
    JudgeService->>KafkaQueue: Publish to classical-submission-response topic
    KafkaQueue->>KafkaConsumer: Consume response
    KafkaConsumer->>Redis: Decrement judgeCount
    KafkaConsumer->>TournamentWebSocketService: classicRankAndSubUpdate()
    TournamentWebSocketService->>Redis: Update submission status
    TournamentWebSocketService->>Redis: Update rank data
    KafkaConsumer->>WebSocketController: Send result
    WebSocketController->>Frontend: STOMP /topic/tournament/classicSubmit/{user}/{index}
    Frontend->>User: Show verdict
    
    %% Freestyle Submission Flow
    User->>Frontend: Submit output only
    Frontend->>WebSocketController: STOMP /app/tournament/freeStyleSubmit
    WebSocketController->>TournamentWebSocketService: isCorrect()
    TournamentWebSocketService->>Redis: Get expected output
    TournamentWebSocketService->>TournamentWebSocketService: Compare outputs
    TournamentWebSocketService->>Redis: Update submission status
    TournamentWebSocketService->>Redis: Update rank data
    TournamentWebSocketService->>WebSocketController: Return result
    WebSocketController->>Frontend: STOMP /topic/tournament/freeStyleSubmit/{username}/{index}
    Frontend->>User: Show verdict
    
    %% Periodic updates
    loop Every 5 seconds
        TournamentWebSocketService->>Redis: Fetch rankings
        TournamentWebSocketService->>WebSocketController: Send rankings
        WebSocketController->>Frontend: STOMP /topic/userRank/{tournamentId}/{userName}
        Frontend->>User: Update leaderboard
    end
    
    %% Eventual consistency
    KafkaConsumer->>Database: Asynchronously persist submissions
    KafkaConsumer->>Database: Asynchronously persist rankings
```

## Judging Pipeline Features:

1. **Dual Submission Modes**:
   - Classic mode: Full code submission judged by external service
   - Freestyle mode: Output-only submission checked instantly against expected output

2. **Performance Optimizations**:
   - Redis for real-time data during tournaments
   - Asynchronous persistence to database via Kafka
   - Real-time updates via WebSockets
   
3. **Judging Process**:
   - Code compilation and execution in isolated environment
   - Memory and time constraints enforcement
   - Multiple verdict types (AC, WA, TLE, MLE, RE, CE)
   - Custom checkers for flexible output validation


# Matchmaking System

```mermaid
sequenceDiagram
    participant User1
    participant Frontend1
    participant User2
    participant Frontend2
    participant MatchmakingController
    participant MatchmakingService
    participant Redis
    participant DuelTournamentService
    participant KafkaProducer
    
    %% Match Request Flow
    User1->>Frontend1: Request match (time control, type)
    Frontend1->>MatchmakingController: POST /api/match/request
    MatchmakingController->>MatchmakingService: processMatchRequest(MatchRequestDTO)
    MatchmakingService->>Redis: Remove existing request if any
    MatchmakingService->>Redis: Add to queue Z-set with score (rating + random)
    MatchmakingService-->>MatchmakingController: MatchResponseDTO (QUEUED)
    MatchmakingController-->>Frontend1: Match queued
    Frontend1-->>User1: Waiting for opponent
    
    %% User2 follows same process
    User2->>Frontend2: Request match (same time control, type)
    Frontend2->>MatchmakingController: POST /api/match/request
    MatchmakingController->>MatchmakingService: processMatchRequest(MatchRequestDTO)
    MatchmakingService->>Redis: Remove existing request if any
    MatchmakingService->>Redis: Add to queue Z-set with score (rating + random)
    
    %% Matchmaking Process
    loop Every 5 seconds
        MatchmakingService->>Redis: Scan matchmaking queue
        MatchmakingService->>MatchmakingService: Check for suitable opponents
        MatchmakingService->>MatchmakingService: processMatchCandidate()
        MatchmakingService->>Redis: Remove both players from queue
        MatchmakingService->>Redis: Create pending match with UUID
    end
    
    %% Match Found Notification
    MatchmakingService->>Frontend1: STOMP /queue/match-notifications (MATCH_FOUND)
    Frontend1->>User1: Show match found dialog
    MatchmakingService->>Frontend2: STOMP /queue/match-notifications (MATCH_FOUND)
    Frontend2->>User2: Show match found dialog
    
    %% Confirmation Flow
    User1->>Frontend1: Accept match
    Frontend1->>MatchmakingController: STOMP /app/match/confirm
    MatchmakingController->>MatchmakingService: confirmPendingMatch(id, user1)
    MatchmakingService->>Redis: Mark User1 confirmed
    
    User2->>Frontend2: Accept match
    Frontend2->>MatchmakingController: STOMP /app/match/confirm
    MatchmakingController->>MatchmakingService: confirmPendingMatch(id, user2)
    MatchmakingService->>Redis: Mark User2 confirmed
    MatchmakingService->>Redis: Set match status CONFIRMED
    
    %% Match Creation (after 10s delay)
    MatchmakingService->>DuelTournamentService: createDuelTournament(match)
    DuelTournamentService->>Database: Save DuelTournamentEntity
    DuelTournamentService->>DuelTournamentService: joinTournament(user1)
    DuelTournamentService->>DuelTournamentService: joinTournament(user2)
    DuelTournamentService->>Redis: Cache TournamentCacheDTO
    DuelTournamentService->>KafkaProducer: startTournamentInit event
    MatchmakingService->>Frontend1: STOMP /queue/match-notifications (MATCH_CREATED)
    MatchmakingService->>Frontend2: STOMP /queue/match-notifications (MATCH_CREATED)
    Frontend1->>User1: Redirect to tournament
    Frontend2->>User2: Redirect to tournament
```

## Matchmaking Features:

1. **Dynamic Rating-Based Matching**: Finds opponents with similar ratings
2. **Expanding Search Range**: Increases rating range tolerance over time
3. **Confirmation System**: Requires both players to confirm within 15 seconds
4. **Auto-Cancellation**: Automatically cancels unconfirmed matches
5. **Redis-Backed Queue**: Z-sets for efficient rating-based opponent finding
6. **Real-Time Notifications**: WebSocket notifications for match state changes


# Data Model

```mermaid
erDiagram
    UserEntity {
        string username PK
        string firstname
        string lastname
        string email
        string password
        int rating
        list roles
    }
    
    UserTournamentRatingEntity {
        Long id PK
        Long tournamentId
        string username FK
        int oldRating
        int newRating
        timestamp updateTimestamp
    }
    
    TournamentBaseEntity {
        Long id PK
        timestamp startTime
        boolean isRated
        int durationInSeconds
        string visibility
        string password
        string type
    }
    
    MTMTournamentEntity {
        Long id PK
        string name
        string creatorId FK
        string description
        int minRatingReq
        int maxRatingReq
        boolean isTeamStyle
    }
    
    DuelTournamentEntity {
        Long id PK
        Long player1 FK
        Long player2 FK
    }
    
    QuestionEntity {
        Long id PK
        string title
        string description
        int difficulty
    }
    
    RankEntity {
        Long id PK
        Long tournamentId FK
        string username FK
        int score
        int penalty
        int rating
        timestamp startTime
    }
    
    TestcaseEntity {
        Long id PK
        Long questionId FK
        string input
        string output
        int rating
    }
    
    ClassicTestcaseEntity {
        Long id PK
        Long questionId FK
        string inputFilePath
        string outputFilePath
    }
    
    TournamentPlayerEntity {
        Long id PK
        Long tournamentId FK
        string playerUsername FK
        int rating
    }
    
    TournamentQuestionEntity {
        Long id PK
        Long tournamentId FK
        Long questionId FK
    }
    
    CheckerValidatorSolutionEntity {
        Long id PK
        Long questionId FK
        string checkerFilePath
        string validatorFilePath
        string solutionFilePath
    }
    
    ClassicSubmissionEntity {
        Long id PK
        int index
        string username FK
        Long tournamentId FK
        string code
        int maxTimeTaken
        int maxMemoryUsed
        Long questionId FK
        string verdict
        timestamp submissionTime
        timestamp judgeTime
    }
    
    RecentActivityEntity {
        Long id PK
        string username FK
        string activityJson
    }
    
    SubmissionStatus {
        Long id PK
        string username FK
        boolean solved
        Long tournamentId FK
        Long questionId FK
        int numberOfAttempts
        timestamp submissionTime
    }
    
    UserEntity ||--o{ UserTournamentRatingEntity : "has rating history"
    UserEntity ||--o{ RecentActivityEntity : "has activity"
    UserEntity ||--o{ SubmissionStatus : "makes submissions"
    UserEntity ||--o{ RankEntity : "has rank in"
    UserEntity ||--o{ TournamentPlayerEntity : "participates in"
    UserEntity ||--o{ ClassicSubmissionEntity : "submits"
    
    TournamentBaseEntity ||--|{ MTMTournamentEntity : "specializes as"
    TournamentBaseEntity ||--|{ DuelTournamentEntity : "specializes as"
    
    TournamentBaseEntity ||--o{ RankEntity : "has rankings"
    TournamentBaseEntity ||--o{ TournamentPlayerEntity : "has players"
    TournamentBaseEntity ||--o{ TournamentQuestionEntity : "has questions"
    TournamentBaseEntity ||--o{ ClassicSubmissionEntity : "has submissions"
    TournamentBaseEntity ||--o{ UserTournamentRatingEntity : "affects ratings"
    
    QuestionEntity ||--o{ TestcaseEntity : "has testcases"
    QuestionEntity ||--o{ ClassicTestcaseEntity : "has file testcases"
    QuestionEntity ||--o{ TournamentQuestionEntity : "included in"
    QuestionEntity ||--o{ CheckerValidatorSolutionEntity : "has validation"
    QuestionEntity ||--o{ ClassicSubmissionEntity : "has submissions"
    QuestionEntity ||--o{ SubmissionStatus : "has submission status"
```

## Key Entity Relationships:

1. **Users & Tournaments**: Users participate in tournaments, receive rankings, and have rating history
2. **Tournament Types**: Base tournament class extended by MTM (Many-to-Many) and Duel tournament types
3. **Questions & Testcases**: Questions have various types of testcases and validation tools
4. **Submissions & Results**: Tracking of all user submissions and their outcomes
5. **User Activity**: Historical data on user participation and performance


# CodeRush Application Flow

```mermaid
graph TD
    Start[User Access] --> Register{Registered?}
    Register -->|No| Registration[Register with username, email, password]
    Register -->|Yes| Login[Login with username & password]
    Registration --> Login
    
    Login --> Dashboard[User Dashboard]
    
    Dashboard --> ViewTournaments[Browse Tournaments]
    Dashboard --> CreateTournament[Create Tournament]
    Dashboard --> PlayDuel[Play Duel]
    Dashboard --> ViewProfile[View Profile/Rating History]
    Dashboard --> ManageQuestions[Manage Questions]
    
    CreateTournament --> ConfigureTournament[Configure Parameters]
    ConfigureTournament --> SaveTournament[Save Tournament]
    
    ViewTournaments --> JoinTournament[Join Tournament]
    JoinTournament --> WaitingArea[Tournament Waiting Area]
    
    PlayDuel --> SelectTimeControl[Select Time Control]
    SelectTimeControl --> MatchmakingQueue[Enter Matchmaking Queue]
    MatchmakingQueue --> MatchFound{Match Found?}
    MatchFound -->|Yes| ConfirmMatch[Confirm Match]
    MatchFound -->|No| ContinueWaiting[Continue Waiting]
    ConfirmMatch --> StartDuel[Start Duel]
    
    WaitingArea --> TournamentStart{Tournament Started?}
    TournamentStart -->|No| ContinueWaiting2[Continue Waiting]
    TournamentStart -->|Yes| ActiveTournament[Enter Tournament]
    
    StartDuel --> ActiveTournament
    
    ActiveTournament --> TournamentType{Tournament Type?}
    TournamentType -->|Classic| ClassicMode[Solve Problems & Submit Code]
    TournamentType -->|Freestyle| FreestyleMode[Generate & Submit Output]
    
    ClassicMode --> SubmitCode[Submit Code to Judge]
    SubmitCode --> WaitVerdict[Wait for Judge Verdict]
    WaitVerdict --> UpdateRanking1[Update Ranking]
    
    FreestyleMode --> SubmitOutput[Submit Output]
    SubmitOutput --> InstantCheck[Instant Verification]
    InstantCheck --> UpdateRanking2[Update Ranking]
    
    UpdateRanking1 --> MonitorLeaderboard1[Monitor Leaderboard]
    UpdateRanking2 --> MonitorLeaderboard2[Monitor Leaderboard]
    
    MonitorLeaderboard1 --> TournamentEnded1{Tournament Ended?}
    MonitorLeaderboard2 --> TournamentEnded2{Tournament Ended?}
    
    TournamentEnded1 -->|No| ClassicMode
    TournamentEnded1 -->|Yes| ViewResults1[View Final Results]
    
    TournamentEnded2 -->|No| FreestyleMode
    TournamentEnded2 -->|Yes| ViewResults2[View Final Results]
    
    ViewResults1 --> RatingUpdate1[Rating Update]
    ViewResults2 --> RatingUpdate2[Rating Update]
    
    RatingUpdate1 --> Dashboard
    RatingUpdate2 --> Dashboard
    
    ManageQuestions --> CreateQuestion[Create Question]
    ManageQuestions --> EditQuestion[Edit Question]
    
    CreateQuestion --> DefineQuestion[Define Problem]
    DefineQuestion --> UploadTestcases[Upload Testcases]
    UploadTestcases --> UploadValidation[Upload Checker/Validator/Solution]
    UploadValidation --> InvokeQuestion[Invoke Question]
    InvokeQuestion --> QuestionReady[Question Ready for Use]
    
    EditQuestion --> DefineQuestion
```

## Application Flow Overview:

1. **User Access & Authentication**:
   - Registration with secure password hashing
   - Login with JWT token generation
   - Token-based authentication for all requests

2. **User Dashboard**:
   - Tournament browsing and creation
   - Duel matchmaking
   - Profile and rating history
   - Question management

3. **Tournament Lifecycle**:
   - Creation with configurable parameters
   - Joining and waiting for start time
   - Active participation (Classic or Freestyle)
   - Real-time leaderboard updates
   - Final results and rating updates

4. **Duel System**:
   - Time control selection
   - Rating-based matchmaking
   - Confirmation system
   - 1-on-1 competition

5. **Question Management**:
   - Problem definition
   - Testcase creation
   - Checker/validator/solution uploads
   - Invocation for validation
