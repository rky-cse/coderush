package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import org.apache.juli.logging.Log;

@Entity
@Table(name = "user_testcases")
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsSolved() {
        return isSolved;
    }

    public void setIsSolved(Boolean solved) {
        isSolved = solved;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Long getTestcaseId() {
        return testcaseId;
    }


    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }
}