# Local Development Setup

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21 (Corretto/Temurin) | Backend |
| Node.js | 18+ | Frontend |
| PostgreSQL | 16 | Database |
| Redis | Latest | Job store & caching |
| Docker | Latest | Kafka broker + Judge container |

### Platform-specific Docker setup

- **Windows**: Docker Desktop
- **macOS**: [Colima](https://github.com/abiosoft/colima)
  ```bash
  brew install colima docker docker-compose
  colima start
  ```

---

## 1. Configure volume path

The judge container mounts the project directory to access solution/test files.

Edit `sandbox/docker-compose.yml` — find the `volumes:` section under the `judge` service:

```yaml
volumes:
  - E:\coderush:/coderush                          # Windows (default)
  # - /Users/you/path/to/coderush:/coderush        # macOS/Linux — adjust to your checkout
```

**Comment out** the path that doesn't match your OS and **uncomment/add** yours. The path should point to the root of the `coderush/` directory (the one containing `sandbox/`, `coderush/`, `judge/`, `client/`).

---

## 2. Start Database & Cache

### macOS (Homebrew)
```bash
brew services start postgresql@16
brew services start redis
```

### Windows
Start PostgreSQL 16 and Redis via their respective services/installers.

### Verify
```bash
psql -U postgres -h localhost -c "SELECT 1;"
redis-cli ping
```

---

## 3. Start Kafka + Judge (Docker)

```bash
cd sandbox/
docker compose up -d broker judge
```

Verify:
```bash
docker ps  # should show 'broker' and 'judge' running
```

---

## 4. Start Backend

The AI Problem Generator requires a Groq API key (free tier).

1. Get a key from [console.groq.com](https://console.groq.com)
2. Run:

```bash
cd coderush/
GROQ_API_KEY=gsk_your_key_here JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run
```

> Without the key, the backend starts fine but AI generation will fail.

Wait for `Started CoderushApplication` in the logs.

The backend runs on **port 8084**.

---

## 5. Start Frontend

```bash
cd client/coderush/
npm install    # first time only
npm run dev
```

The frontend runs on **port 3000**.

---

## Ports Summary

| Service    | Port  |
|------------|-------|
| Frontend   | 3000  |
| Backend    | 8084  |
| Kafka      | 9092  |
| PostgreSQL | 5432  |
| Redis      | 6379  |

---

## Shutdown

```bash
# Stop frontend & backend: Ctrl+C in their terminals

# Stop Docker services
docker compose -f sandbox/docker-compose.yml stop

# Stop brew services (macOS)
brew services stop postgresql@16
brew services stop redis

# Stop Colima (macOS)
colima stop
```

---

## Troubleshooting

- **Judge stuck / compilation hanging**: Restart the judge container — `docker restart judge`
- **Kafka stale messages**: Reset consumer offset:
  ```bash
  docker stop judge
  docker exec broker /opt/kafka/bin/kafka-consumer-groups.sh \
    --bootstrap-server broker:29092 --group myGroup \
    --topic invocation --reset-offsets --to-latest --execute
  docker start judge
  ```
- **Docker socket not found (macOS)**: Run `colima start` first
- **Port already in use**: Check with `lsof -i :PORT` and kill the process
