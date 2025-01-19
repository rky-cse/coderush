package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "legend")
    private String legend;

    @Lob
    @Column(name = "input_format")
    private String inputFormat;

    @Lob
    @Column(name = "output_format")
    private String outputFormat;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Lob
    @Column(name = "tutorial")
    private String tutorial;

    @ElementCollection
    @CollectionTable(name = "question_images", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;


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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}