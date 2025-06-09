FROM openjdk:23-slim
WORKDIR /app

COPY judge /app
COPY files /files

CMD ["./mvnw", "spring-boot:run"]
