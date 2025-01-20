package me.rkycse.coderush.mapper;

import me.rkycse.coderush.dto.RankDTO;
import me.rkycse.coderush.dto.UserTestcaseDTO;
import me.rkycse.coderush.entity.RankEntity;
import me.rkycse.coderush.entity.UserTestcaseEntity;
import org.springframework.stereotype.Component;

@Component
public class Mapper {


    public static RankDTO toDTO(RankEntity entity) {
        if (entity == null) {
            return null;
        }

        RankDTO dto = new RankDTO();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setScore(entity.getScore());
        dto.setTournamentId(entity.getTournamentId());
        return dto;
    }

    public static RankEntity toEntity(RankDTO dto) {
        if (dto == null) {
            return null;
        }

        RankEntity entity = new RankEntity();
        entity.setId(dto.getId());
        entity.setUserName(dto.getUserName());
        entity.setScore(dto.getScore());
        entity.setTournamentId(dto.getTournamentId());
        return entity;
    }

    public static UserTestcaseDTO toDTO(UserTestcaseEntity entity) {
        if (entity == null) {
            return null;
        }

        UserTestcaseDTO dto = new UserTestcaseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setTestcaseId(entity.getTestcaseId());
        dto.setIsSolved(entity.getIsSolved());
        dto.setNumberOfAttempts(entity.getNumberOfAttempts());

        return dto;
    }

    public static UserTestcaseEntity toEntity(UserTestcaseDTO dto) {
        if (dto == null) {
            return null;
        }

        UserTestcaseEntity entity = new UserTestcaseEntity();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setTestcaseId(dto.getTestcaseId());
        entity.setIsSolved(dto.getIsSolved());
        entity.setNumberOfAttempts(dto.getNumberOfAttempts());

        return entity;
    }
}