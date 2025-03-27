package me.rkycse.coderush.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import me.rkycse.coderush.dto.MatchRequestDTO;
import me.rkycse.coderush.dto.PendingMatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RedisTemplate<String, MatchRequestDTO> matchRequestDTORedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, MatchRequestDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use typed Jackson serializer for values
        Jackson2JsonRedisSerializer<MatchRequestDTO> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, MatchRequestDTO.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, PendingMatch> pendingMatchRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, PendingMatch> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use typed Jackson serializer for values
        Jackson2JsonRedisSerializer<PendingMatch> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, PendingMatch.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }



    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Use String serializer for keys
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper(), Object.class);

        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}