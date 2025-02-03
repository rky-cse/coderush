package me.rkycse.coderush.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionWithTestcaseDTO {
    private QuestionDTO question;
    private TestcaseDTO testcase;

}
