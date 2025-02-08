package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDTO {

    private Long questionId;
    private UserDto creator;
    private String name;
    private String legend;
    private String inputFormat;
    private String outputFormat;
    private String notes;
    private String tutorial;
    private Boolean rated;
    private List<String> imageUrls;
}
