package me.rkycse.coderush.service;
import me.rkycse.coderush.dto.UserDTO;
import me.rkycse.coderush.dto.UserTournamentRatingDTO;
import me.rkycse.coderush.entity.UserEntity;
import me.rkycse.coderush.entity.UserTournamentRatingEntity;
import me.rkycse.coderush.mapper.Mapper;
import me.rkycse.coderush.repository.UserRepository;
import me.rkycse.coderush.repository.UserTournamentRatingRepository;
import me.rkycse.coderush.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // Even though there are two fields of the same type, the one used in the service methods is 'userTournamentRatingRepository'.
    @Mock
    private UserTournamentRatingRepository tournamentRatingRepository;

    @Mock
    private UserTournamentRatingRepository userTournamentRatingRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        // No further setup required; MockitoExtension handles the injection.
    }

    @Test
    public void testGetUserByUsername_UserFound() {
        String username = "testuser";
        // Create a dummy UserEntity (not a generic Object) for proper type matching.
        UserEntity dummyUserEntity = new UserEntity();
        dummyUserEntity.setId(1L);
        dummyUserEntity.setUserName(username);
        // Initialize other fields as needed...

        // Create a dummy UserDTO that we expect from the Mapper.
        UserDTO dummyUserDTO = new UserDTO();
        dummyUserDTO.setId(dummyUserEntity.getId());
        dummyUserDTO.setUserName(dummyUserEntity.getUserName());
        // Initialize other fields as needed...

        // Stub the repository call to return an Optional of the dummy UserEntity.
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(dummyUserEntity));

        // Use static mocking for the Mapper. Ensure that you have the "mockito-inline" dependency.
        try (var mapperMock = Mockito.mockStatic(Mapper.class)) {
            mapperMock.when(() -> Mapper.toDTO(dummyUserEntity)).thenReturn(dummyUserDTO);

            // Act: Call the service method.
            UserDTO result = userService.getUserByUsername(username);

            // Assert: Verify the expected result and interactions.
            assertEquals(dummyUserDTO, result);
            verify(userRepository, times(1)).findByUserName(username);
            mapperMock.verify(() -> Mapper.toDTO(dummyUserEntity), times(1));
        }
    }

    @Test
    public void testGetUserTournamentRating() {
        String username = "testuser";
        // Create dummy tournament rating entities.
        UserTournamentRatingEntity entity1 = new UserTournamentRatingEntity();
        UserTournamentRatingEntity entity2 = new UserTournamentRatingEntity();
        List<UserTournamentRatingEntity> entityList = Arrays.asList(entity1, entity2);

        // Create corresponding dummy DTOs.
        UserTournamentRatingDTO dto1 = new UserTournamentRatingDTO();
        UserTournamentRatingDTO dto2 = new UserTournamentRatingDTO();

        // Stub the repository call.
        when(userTournamentRatingRepository.findByUsernameSortedByTimestamp(username))
                .thenReturn(entityList);

        // Use static mocking for the Mapper.
        try (var mapperMock = Mockito.mockStatic(Mapper.class)) {
            // Make sure the method names exactly match your Mapper implementation.
            mapperMock.when(() -> Mapper.toDto(entity1)).thenReturn(dto1);
            mapperMock.when(() -> Mapper.toDto(entity2)).thenReturn(dto2);

            // Act: Call the service method.
            List<UserTournamentRatingDTO> result = userService.getUserTournamentRating(username);

            // Assert: Verify the list of DTOs returned.
            assertEquals(2, result.size());
            assertEquals(dto1, result.get(0));
            assertEquals(dto2, result.get(1));

            verify(userTournamentRatingRepository, times(1)).findByUsernameSortedByTimestamp(username);
            mapperMock.verify(() -> Mapper.toDto(entity1), times(1));
            mapperMock.verify(() -> Mapper.toDto(entity2), times(1));
        }
    }
}
