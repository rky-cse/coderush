package me.rkycse.coderush.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.QuestionEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, TournamentCacheDTO> tournamentCacheRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TournamentCacheDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<TournamentCacheDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, TournamentCacheDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, RankDTO> rankDTORedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, RankDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<RankDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, RankDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }



    @Bean
    public RedisTemplate<String, List<QuestionEntity>> questionListCacheRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<QuestionEntity>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Define the JavaType for List<RankDTO>
        JavaType javaType = TypeFactory.defaultInstance().constructCollectionType(List.class, QuestionEntity.class);

        // Use the constructor that accepts ObjectMapper and JavaType
        Jackson2JsonRedisSerializer<List<QuestionEntity>> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, javaType);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, TournamentDTO> tournamentRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TournamentDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<TournamentDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, TournamentDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }



    @Bean
    public RedisTemplate<String, TestcaseDTO> testcaseDTORedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TestcaseDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<TestcaseDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, TestcaseDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, UserTestcaseDTO> userTestcaseDTORedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UserTestcaseDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<UserTestcaseDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, UserTestcaseDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, QuestionDTO> questionDTORedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, QuestionDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<QuestionDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, QuestionDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, OneToOneGameDTO> oneToOneGameDTORedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, OneToOneGameDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the ObjectMapper directly into the constructor.
        Jackson2JsonRedisSerializer<OneToOneGameDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, OneToOneGameDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }




}