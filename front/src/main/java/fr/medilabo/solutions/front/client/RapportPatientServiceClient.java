package fr.medilabo.solutions.front.client;

import fr.medilabo.solutions.front.dto.DiabeteNiveauRisqueEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rapport")
public interface RapportPatientServiceClient {

    @GetMapping("api/rapport/{id}")
    DiabeteNiveauRisqueEnum getRapportByIdPatient(@PathVariable("id") Long patientId);

}
