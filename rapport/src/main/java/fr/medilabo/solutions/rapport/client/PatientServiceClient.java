package fr.medilabo.solutions.rapport.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fr.medilabo.solutions.rapport.dto.PatientDto;

@FeignClient(name = "patient")
public interface PatientServiceClient {

    @GetMapping("/api/patient/{id}")
    PatientDto getPatientById(@PathVariable("id") int patientId);

}