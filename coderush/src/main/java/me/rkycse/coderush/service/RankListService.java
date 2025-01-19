package me.rkycse.coderush.service;


import me.rkycse.coderush.entity.RankListEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RankListService {
    List<RankListEntity> rankListOfTournaments=new ArrayList<>();

    public RankListEntity getRankListByTournamentId(String tournamentId){
        if (tournamentId == null || tournamentId.isEmpty()) {
            throw new IllegalArgumentException("Tournament ID cannot be null or empty");
        }

        for(RankListEntity rankList:rankListOfTournaments){
            if(rankList.getTournamentId().equals(tournamentId)){
                return rankList;
            }
        }
        throw new NoSuchElementException("RankList for Tournament ID " + tournamentId + " not found");


    }



}

