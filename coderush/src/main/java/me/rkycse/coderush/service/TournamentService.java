package me.rkycse.coderush.service;
import me.rkycse.coderush.dto.MTMTournamentDTO;
import me.rkycse.coderush.dto.TournamentBaseDTO;
import me.rkycse.coderush.dto.TournamentCacheDTO;
import me.rkycse.coderush.entity.MTMTournamentEntity;
import me.rkycse.coderush.entity.TournamentBaseEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.MTMTournamentRepository;
import me.rkycse.coderush.repository.TournamentBaseRepository;
import me.rkycse.coderush.repository.TournamentRepository;
import me.rkycse.coderush.util.TimeUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TournamentService {

    private final TournamentBaseRepository tournamentBaseRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MTMTournamentRepository mtmTournamentRepository;

    public TournamentService(TournamentBaseRepository tournamentBaseRepository, RedisTemplate<String,Object> redisTemplate, MTMTournamentRepository mtmTournamentRepository) {
        this.tournamentBaseRepository = tournamentBaseRepository;
        this.redisTemplate = redisTemplate;
        this.mtmTournamentRepository = mtmTournamentRepository;
    }

    public TournamentBaseEntity getTournamentById(Long tournamentId) {
        return tournamentBaseRepository.findById(tournamentId)
                .orElse(null);
    }



}
