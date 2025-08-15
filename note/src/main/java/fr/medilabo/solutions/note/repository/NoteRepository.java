package fr.medilabo.solutions.note.repository;

import fr.medilabo.solutions.note.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByPatId(Integer patId);
}
