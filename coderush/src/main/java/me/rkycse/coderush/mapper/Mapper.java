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
        dto.setPenalty(entity.getPenalty());
        dto.setRating(entity.getRating());
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
        entity.setPenalty(dto.getPenalty());
        entity.setRating(dto.getRating());

        return entity;
    }

    public static SubmissionStatusDTO toDTO(SubmissionStatus entity) {
        if (entity == null) {
            return null;
        }

        SubmissionStatusDTO dto = new SubmissionStatusDTO();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setQuestionId(entity.getQuestionId());
        dto.setSolved(entity.getSolved());
        dto.setNumberOfAttempts(entity.getNumberOfAttempts());
        dto.setTournamentId(entity.getTournamentId());
        dto.setSubmissionTime(entity.getSubmissionTime());

        return dto;
    }

    public static SubmissionStatus toEntity(SubmissionStatusDTO dto) {
        if (dto == null) {
            return null;
        }

        SubmissionStatus entity = new SubmissionStatus();
        entity.setId(dto.getId());
        entity.setUserName(dto.getUserName());
        entity.setQuestionId(dto.getQuestionId());
        entity.setSolved(dto.getSolved());
        entity.setNumberOfAttempts(dto.getNumberOfAttempts());
        entity.setTournamentId(dto.getTournamentId());
        entity.setSubmissionTime(dto.getSubmissionTime());

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
        dto.setRating(entity.getRating());
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
        return dto;
    }

    public static QuestionDTO toDTO(QuestionEntity entity) {
        if (entity == null) {
            return null;
        }
        QuestionDTO dto = new QuestionDTO();

        dto.setQuestionId(entity.getQuestionId());
        if (entity.getCreatorId() != null) {


            dto.setCreatorId(entity.getCreatorId());
        }
        dto.setLegend(entity.getLegend());
        dto.setInputFormat(entity.getInputFormat());
        dto.setOutputFormat(entity.getOutputFormat());
        dto.setName(entity.getName());
        dto.setNotes(entity.getNotes());
        dto.setTutorial(entity.getTutorial());
        return dto;

    }
    public static MTMTournamentEntity toEntity(MTMTournamentDTO dto) {
        if (dto == null) {
            return null;
        }
        MTMTournamentEntity entity = new MTMTournamentEntity();
        entity.setCreatorId(dto.getCreatorId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTeamStyle(entity.getTeamStyle());
        entity.setPassword(dto.getPassword());
        entity.setDurationInSeconds(dto.getDurationInSeconds());
        entity.setMaxRatingReq(dto.getMaxRatingReq());
        entity.setRated(dto.getRated());
        entity.setMinRatingReq(dto.getMinRatingReq());
        entity.setStartTime(dto.getStartTime());
        entity.setVisibility(dto.getVisibility());
        entity.setTournamentType(dto.getTournamentType());
        entity.setPenaltyFactor(dto.getPenaltyFactor());
        return entity;
    }
    public static MTMTournamentDTO toDTO(MTMTournamentEntity entity) {
        if (entity == null) {
            return null;
        }
        MTMTournamentDTO dto = new MTMTournamentDTO();
        dto.setCreatorId(entity.getCreatorId());
        dto.setTournamentId(entity.getTournamentId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setTeamStyle(entity.getTeamStyle());
        dto.setPassword(entity.getPassword());
        dto.setDurationInSeconds(entity.getDurationInSeconds());
        dto.setMaxRatingReq(entity.getMaxRatingReq());
        dto.setRated(entity.getRated());
        dto.setMinRatingReq(entity.getMinRatingReq());
        dto.setStartTime(entity.getStartTime());
        dto.setVisibility(entity.getVisibility());
        dto.setTournamentType(entity.getTournamentType());
        dto.setPenaltyFactor(entity.getPenaltyFactor());
        return dto;
    }

    public static UserDTO toDTO(UserEntity dto) {
        if (dto == null) {
            return null;

        }
        UserDTO dtoUser = new UserDTO();
        dtoUser.setId(dto.getId());
        dtoUser.setUserName(dto.getUserName());
        dtoUser.setEmail(dto.getEmail());
        dtoUser.setFirstName(dto.getFirstName());
        dtoUser.setLastName(dto.getLastName());
        dtoUser.setRating(dto.getRating());
        return dtoUser;
    }
    private static TournamentBaseDTO toDTO(TournamentBaseEntity entity) {
        if (entity == null) {
            return null;

        }
        TournamentBaseDTO dto = new TournamentBaseDTO();
        dto.setDurationInSeconds(entity.getDurationInSeconds());
        dto.setRated(entity.getRated());
        dto.setStartTime(entity.getStartTime());
        dto.setPenaltyFactor(entity.getPenaltyFactor());
        dto.setVisibility(entity.getVisibility());
        dto.setTournamentType(entity.getTournamentType());
        dto.setTournamentId(entity.getTournamentId());

        return dto;
    }

    /**
     * Convert DTO to Entity
     *
     * @param dto The DTO to convert
     * @return The converted entity, or null if dto is null
     */
    public static ClassicSubmissionEntity toEntity(ClassicSubmissionDTO dto) {
        if (dto == null) {
            return null;
        }

        ClassicSubmissionEntity entity = new ClassicSubmissionEntity();
        entity.setId(dto.getId());
        entity.setIndex(dto.getIndex());
        entity.setUsername(dto.getUsername());
        entity.setTournamentId(dto.getTournamentId());
        entity.setCode(dto.getCode());
        entity.setMaxTimeTaken(dto.getMaxTimeTaken());
        entity.setMaxMemoryUsed(dto.getMaxMemoryUsed());
        entity.setQuestionId(dto.getQuestionId());
        entity.setLanguage(dto.getLanguage());
        entity.setVerdict(dto.getVerdict());
        entity.setSubmissionTime(dto.getSubmissionTime());
        entity.setJudgingTime(dto.getJudgingTime());

        return entity;
    }

    /**
     * Convert Entity to DTO
     *
     * @param entity The entity to convert
     * @return The converted DTO, or null if entity is null
     */
    public static ClassicSubmissionDTO toDTO(ClassicSubmissionEntity entity) {
        if (entity == null) {
            return null;
        }

        ClassicSubmissionDTO dto = new ClassicSubmissionDTO();
        dto.setId(entity.getId());
        dto.setIndex(entity.getIndex());
        dto.setUsername(entity.getUsername());
        dto.setTournamentId(entity.getTournamentId());
        dto.setCode(entity.getCode());
        dto.setMaxTimeTaken(entity.getMaxTimeTaken());
        dto.setMaxMemoryUsed(entity.getMaxMemoryUsed());
        dto.setQuestionId(entity.getQuestionId());
        dto.setLanguage(entity.getLanguage());
        dto.setVerdict(entity.getVerdict());
        dto.setSubmissionTime(entity.getSubmissionTime());
        dto.setJudgingTime(entity.getJudgingTime());

        return dto;
    }



    public static ClassicSubmissionEntity toNewEntity(ClassicSubmissionDTO dto) {
        if (dto == null) {
            return null;
        }

        ClassicSubmissionEntity entity = toEntity(dto);
        entity.setId(null); // Ensure ID is null for new entities
        return entity;
    }

    /**
     * Update an existing entity with values from a DTO
     *
     * @param existingEntity The existing entity to update
     * @param dto The DTO containing new values
     * @return The updated entity, or a new entity if existingEntity is null
     */
    public static ClassicSubmissionEntity updateEntityFromDTO(
            ClassicSubmissionEntity existingEntity,
            ClassicSubmissionDTO dto) {

        if (existingEntity == null) {
            return toEntity(dto);
        }

        if (dto == null) {
            return existingEntity;
        }

        // We keep the ID from the existing entity
        // Long id = existingEntity.getId();

        // Update all other fields
        existingEntity.setIndex(dto.getIndex());
        existingEntity.setUsername(dto.getUsername());
        existingEntity.setTournamentId(dto.getTournamentId());
        existingEntity.setCode(dto.getCode());
        existingEntity.setMaxTimeTaken(dto.getMaxTimeTaken());
        existingEntity.setMaxMemoryUsed(dto.getMaxMemoryUsed());
        existingEntity.setQuestionId(dto.getQuestionId());
        existingEntity.setLanguage(dto.getLanguage());
        existingEntity.setVerdict(dto.getVerdict());
        existingEntity.setSubmissionTime(dto.getSubmissionTime());
        existingEntity.setJudgingTime(dto.getJudgingTime());

        return existingEntity;
    }

    /**
     * Create a copy of a DTO
     *
     * @param source The DTO to copy
     * @return A new DTO with the same values, or null if source is null
     */
    public static ClassicSubmissionDTO copyDTO(ClassicSubmissionDTO source) {
        return source == null ? null : toDTO(toEntity(source));
    }


    public static UserTournamentRatingDTO toDto(UserTournamentRatingEntity entity) {
        if (entity == null) {
            return null;
        }
        UserTournamentRatingDTO dto = new UserTournamentRatingDTO();
        dto.setId(entity.getId());
        dto.setTournamentId(entity.getTournamentId());
        dto.setUsername(entity.getUsername());
        dto.setOldRating(entity.getOldRating());
        dto.setNewRating(entity.getNewRating());
        dto.setRatingUpdateTimestamp(entity.getRatingUpdateTimestamp());
        return dto;
    }

    public static UserTournamentRatingEntity toEntity(UserTournamentRatingDTO dto) {
        if (dto == null) {
            return null;
        }
        UserTournamentRatingEntity entity = new UserTournamentRatingEntity();
        entity.setId(dto.getId());
        entity.setTournamentId(dto.getTournamentId());
        entity.setUsername(dto.getUsername());
        entity.setOldRating(dto.getOldRating());
        entity.setNewRating(dto.getNewRating());
        entity.setRatingUpdateTimestamp(dto.getRatingUpdateTimestamp());
        return entity;
    }



}