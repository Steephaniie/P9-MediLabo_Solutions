package fr.medilabo.solutions.front.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.medilabo.solutions.front.config.FrontFeignConfig;
import fr.medilabo.solutions.front.dto.PatientDto;
import fr.medilabo.solutions.front.dto.PatientPageDto;

@FeignClient(name = "gateway", configuration = FrontFeignConfig.class)
public interface GatewayServiceClient {

    @GetMapping("/api/patient")
    List<PatientDto> getAllPatients();

    @GetMapping("/api/patient/{id}")
    PatientDto getPatientById(@PathVariable("id") Long patientId);

    @PostMapping("/api/patient")
    PatientDto createPatient(PatientDto patientDto);

    @PutMapping("/api/patient/{id}")
    PatientDto updatePatient(@PathVariable("id") Long patientId, PatientDto patientDto);

}
