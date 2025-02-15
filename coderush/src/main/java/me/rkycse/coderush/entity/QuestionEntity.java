package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")

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

    @Column(name = "rated")
    private boolean rated=false;

//    // One-to-Many relationship with TestcaseEntity
//    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TestcaseEntity> testcases;


    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTutorial() {
        return tutorial;
    }

    public void setTutorial(String tutorial) {
        this.tutorial = tutorial;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }
}
