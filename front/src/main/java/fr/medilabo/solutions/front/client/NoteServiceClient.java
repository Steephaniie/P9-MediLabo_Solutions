package fr.medilabo.solutions.front.client;

import fr.medilabo.solutions.front.dto.NoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "note")
public interface NoteServiceClient {

    @GetMapping("/api/note/{patId}")
    List<NoteDto> getNoteByPatientId(@PathVariable("patId") int patId);

    @PostMapping("/api/note")
    NoteDto createNote(NoteDto noteDto);

}
