package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String userName;

    @NotNull
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotNull
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name="rating",nullable = false)
    private Long rating;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles;

}