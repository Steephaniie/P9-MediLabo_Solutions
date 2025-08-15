package fr.medilabo.solutions.front.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientDto {

    private int id;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le prénom ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String firstname;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le nom ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String lastname;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;

    @NotBlank(message = "Le genre est obligatoire")
    @Pattern(regexp = "^(M|F)$", message = "Le genre doit être MALE, FEMALE")
    @Size(min = 1, max = 1, message = "Le genre doit être M ou F")
    private String gender;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @Pattern(regexp = "^(\\+33|0)[1-9]([0-9]{8})$", message = "Le numéro de téléphone doit être valide")
    private String phoneNumber;

}
