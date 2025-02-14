package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;
import me.rkycse.coderush.entity.QuestionEntity;

import java.util.List;

@Getter
@Setter
public class UserDto {

    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private String password;
    private List<QuestionEntity> createdQuestions;
}
