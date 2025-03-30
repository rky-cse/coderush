package me.rkycse.coderush.service;

import me.rkycse.coderush.dto.ClassicSubmissionDTO;
import me.rkycse.coderush.entity.ClassicSubmissionEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.ClassicSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClassicSubmissionService {
    private final ClassicSubmissionRepository repository;
    private final ClassicSubmissionRepository classicSubmissionRepository;


    public ClassicSubmissionService(ClassicSubmissionRepository repository, ClassicSubmissionRepository classicSubmissionRepository) {

        this.repository = repository;
        this.classicSubmissionRepository = classicSubmissionRepository;
    }
    public void saveToDB(ClassicSubmissionDTO classicSubmissionDTO) {
        ClassicSubmissionEntity entity = new ClassicSubmissionEntity(classicSubmissionDTO);
        classicSubmissionRepository.save(entity);

    }
    public List<ClassicSubmissionDTO> getClassicSubmissionsByTournamentIdAndUsername(Long tournamentId,String username) {
        List<ClassicSubmissionEntity> classicSubmissionEntities=classicSubmissionRepository.findSubmissionsForUser(tournamentId, username);
        List<ClassicSubmissionDTO> classicSubmissionDTOS=new ArrayList<>();
        for(ClassicSubmissionEntity entity:classicSubmissionEntities) {
            classicSubmissionDTOS.add(Mapper.toDTO(entity));
        }
        return classicSubmissionDTOS;
    }


}
