package fr.medilabo.solutions.rapport.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fr.medilabo.solutions.rapport.dto.NoteDto;

@FeignClient(name = "note")
public interface NoteServiceClient {

    @GetMapping("/api/note/{patId}")
    List<NoteDto> getNoteByPatientId(@PathVariable("patId") int patId);

}
