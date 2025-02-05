package me.rkycse.coderush.config;

import me.rkycse.coderush.dto.OneToOneMatchRequestDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

@Configuration
public class MatchQueueConfig {

    @Bean("initializedQueue")
    public ConcurrentMap<Long, PriorityBlockingQueue<OneToOneMatchRequestDTO>> initializedQueue() {
        return initializeQueueMap(); // Fully initialized map
    }

    @Bean("uninitializedQueue")
    public ConcurrentMap<Long, PriorityBlockingQueue<OneToOneMatchRequestDTO>> uninitializedQueue() {
        return new ConcurrentHashMap<>(); // Empty map with no pre-set time intervals
    }

    private ConcurrentMap<Long, PriorityBlockingQueue<OneToOneMatchRequestDTO>> initializeQueueMap() {
        ConcurrentMap<Long, PriorityBlockingQueue<OneToOneMatchRequestDTO>> map = new ConcurrentHashMap<>();
        long[] timeIntervals = {1, 3, 5, 10, 15, 20, 30, 45, 60};
        for (long time : timeIntervals) {
            map.put(time, new PriorityBlockingQueue<>(11, Comparator.comparingLong(OneToOneMatchRequestDTO::getRating)));
        }
        return map;
    }
}
