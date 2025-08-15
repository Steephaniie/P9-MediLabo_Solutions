package fr.medilabo.solutions.patient.mapper;

import fr.medilabo.solutions.patient.dto.PatientDto;
import fr.medilabo.solutions.patient.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    public PatientDto toDto(Patient patient);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public Patient toEntity(PatientDto patientDto);

}