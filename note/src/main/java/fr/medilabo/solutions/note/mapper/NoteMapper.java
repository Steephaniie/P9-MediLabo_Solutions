package fr.medilabo.solutions.note.mapper;

import fr.medilabo.solutions.note.dto.NoteDto;
import fr.medilabo.solutions.note.model.Note;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    public NoteDto toDto(Note note);

    public Note toEntity(NoteDto noteDto);

}
