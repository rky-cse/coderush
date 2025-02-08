package me.rkycse.coderush.mapper;

import me.rkycse.coderush.dto.*;
import me.rkycse.coderush.entity.*;
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
        dto.setUserName(entity.getUserName());
        dto.setTestcaseId(entity.getTestcaseId());
        dto.setIsSolved(entity.getIsSolved());
        dto.setNumberOfAttempts(entity.getNumberOfAttempts());
        dto.setTournamentId(entity.getTournamentId());

        return dto;
    }

    public static UserTestcaseEntity toEntity(UserTestcaseDTO dto) {
        if (dto == null) {
            return null;
        }

        UserTestcaseEntity entity = new UserTestcaseEntity();
        entity.setId(dto.getId());
        entity.setUserName(dto.getUserName());
        entity.setTestcaseId(dto.getTestcaseId());
        entity.setIsSolved(dto.getIsSolved());
        entity.setNumberOfAttempts(dto.getNumberOfAttempts());
        entity.setTournamentId(dto.getTournamentId());

        return entity;
    }

    public static TournamentPlayerDTO toDTO(TournamentPlayerEntity entity) {
        if (entity == null) {
            return null;

        }
        TournamentPlayerDTO dto = new TournamentPlayerDTO();
        dto.setId(entity.getId());
        dto.setPlayerUserName(entity.getPlayerUserName());
        dto.setTournamentId(entity.getTournamentId());
        return dto;
    }
    public static TournamentDTO toDTO(TournamentEntity entity) {
        if (entity == null) {
            return null;

        }
        TournamentDTO dto = new TournamentDTO();
        dto.setDescription(entity.getDescription());
        dto.setCreatorUserName(entity.getCreatorUserName());
        dto.setDurationInSeconds(entity.getDurationInSeconds());
        dto.setMaxRatingReq(entity.getMaxRatingReq());
        dto.setRated(entity.getRated());
        dto.setMinRatingReq(entity.getMinRatingReq());
        dto.setName(entity.getName());
        dto.setTournamentId(entity.getTournamentId());
        dto.setStartTime(entity.getStartTime());



        return dto;
    }
    public static TestcaseDTO toDTO(TestcaseEntity entity) {
        if (entity == null) {
            return null;

        }
        TestcaseDTO dto = new TestcaseDTO();

        dto.setInput(entity.getInput());
        dto.setOutput(entity.getOutput());
        dto.setTestcaseId(entity.getTestcaseId());
        dto.setRating(entity.getRating());
        dto.setQuestionId(entity.getQuestion().getQuestionId());

        return dto;
    }

    public static QuestionDTO toDTO(QuestionEntity entity) {
        if (entity == null) {
            return null;
        }
        QuestionDTO dto = new QuestionDTO();

        dto.setQuestionId(entity.getQuestionId());
        if (entity.getCreator() != null) {
            UserDto creatorDto = new UserDto();
            creatorDto.setId(entity.getCreator().getId());
            creatorDto.setUserName(entity.getCreator().getUserName());
            creatorDto.setFirstName(entity.getCreator().getFirstName());
            creatorDto.setLastName(entity.getCreator().getLastName());
            creatorDto.setEmail(entity.getCreator().getEmail());
            creatorDto.setRoles(entity.getCreator().getRoles());

            dto.setCreator(creatorDto);
        }
        dto.setLegend(entity.getLegend());
        dto.setInputFormat(entity.getInputFormat());
        dto.setOutputFormat(entity.getOutputFormat());
        dto.setName(entity.getName());
        dto.setNotes(entity.getNotes());
        dto.setTutorial(entity.getTutorial());
        return dto;

    }
}