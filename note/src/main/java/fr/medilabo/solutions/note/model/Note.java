package fr.medilabo.solutions.note.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "note")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    private String id;

    @Field("patId")
    private int patId;

    @Field("patient")
    private String patient;

    @Field("note")
    private String note;

}
