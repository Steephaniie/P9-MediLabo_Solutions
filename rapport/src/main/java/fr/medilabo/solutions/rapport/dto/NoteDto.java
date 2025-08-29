package fr.medilabo.solutions.rapport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteDto {

    private String id;
    private int patId;
    private String patient;

    @NotBlank(message = "Note cannot be blank")
    private String note;

}
