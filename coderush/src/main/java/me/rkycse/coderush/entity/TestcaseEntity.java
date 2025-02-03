package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "testcases")
@Transactional
@Getter
@Setter
public class TestcaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testcaseId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    private String output;

    @Column(name = "rating", nullable = false)
    private int rating;

}
