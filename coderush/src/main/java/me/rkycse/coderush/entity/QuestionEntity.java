package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "creator_username", nullable = false)
    private String createrUserName;

    @Column(name = "legend", columnDefinition = "TEXT")
    private String legend;

    @Column(name = "input_format", columnDefinition = "TEXT")
    private String inputFormat;

    @Column(name = "output_format", columnDefinition = "TEXT")
    private String outputFormat;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tutorial", columnDefinition = "TEXT")
    private String tutorial;


}
