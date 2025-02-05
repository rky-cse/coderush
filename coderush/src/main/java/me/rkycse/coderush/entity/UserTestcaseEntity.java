package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.juli.logging.Log;

@Entity
@Table(name = "user_testcases")
@Getter
@Setter
public class UserTestcaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "testcase_id", nullable = false)
    private Long testcaseId;

    @Column(name = "is_solved", nullable = false)
    private Boolean isSolved;

    @Column(name = "number_of_attempts", nullable = false)
    private int numberOfAttempts;

}