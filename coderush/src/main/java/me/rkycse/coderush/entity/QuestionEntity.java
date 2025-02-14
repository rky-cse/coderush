package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

//    // Many-to-One relationship with UserEntity (Creator)
//    @ManyToOne
//    @JoinColumn(name = "creator_id", nullable = false)
//    private UserEntity creator;

    // New field for "rated" or "unrated"
    @Column(name = "rated")
    private boolean rated=false;

//    // One-to-Many relationship with TestcaseEntity
//    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TestcaseEntity> testcases;
}
