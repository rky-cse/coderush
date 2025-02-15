package me.rkycse.coderush.entity;

import jakarta.persistence.*;


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
    private Boolean solved;

    @Column(name = "number_of_attempts", nullable = false)
    private int numberOfAttempts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        solved = solved;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }
}