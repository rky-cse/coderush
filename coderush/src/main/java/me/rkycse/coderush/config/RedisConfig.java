package me.rkycse.coderush.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
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
    public RedisTemplate<String, List<RankDTO>> rankListCacheRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<RankDTO>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Define the JavaType for List<RankDTO>
        JavaType javaType = TypeFactory.defaultInstance().constructCollectionType(List.class, RankDTO.class);

        // Use the constructor that accepts ObjectMapper and JavaType
        Jackson2JsonRedisSerializer<List<RankDTO>> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, javaType);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

}