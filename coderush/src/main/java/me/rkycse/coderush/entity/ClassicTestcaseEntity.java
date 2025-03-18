package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name="classic_testcases")
public class ClassicTestcaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id",nullable = false)
    private Long questionId;
    @Column(name = "input_file_path",nullable = false)
    private String inputFilePath;
    @Column(name = "output_file_path",nullable = false)
    private String outputFilePath;

    // Default constructor
    public ClassicTestcaseEntity() {
    }

    // Parameterized constructor
    public ClassicTestcaseEntity(Long questionId, String inputFilePath, String outputFilePath) {
        this.questionId = questionId;
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    @Override
    public String toString() {
        return "ClassicTestcaseEntity{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", inputFilePath='" + inputFilePath + '\'' +
                ", outputFilePath='" + outputFilePath + '\'' +
                '}';
    }
}
