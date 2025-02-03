package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
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


}
