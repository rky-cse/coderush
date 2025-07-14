package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.RankRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankListService {
    private final RankRepository rankRepository;

    public RankListService(RankRepository rankRepository) {
        this.rankRepository = rankRepository;
    }

    public List<RankDTO> getRankListByTournamentId(long tournamentId) {

        List<RankDTO>rankDTOList=new ArrayList<>();

        List<RankEntity>rankEntityList=rankRepository.findByTournamentId(tournamentId);
        for(RankEntity rankEntity:rankEntityList){
            RankDTO rankDTO = Mapper.toDTO(rankEntity);
            rankDTOList.add(rankDTO);
        }
        RankDTO.sortRankList(rankDTOList);
        return rankDTOList;

    }
}
