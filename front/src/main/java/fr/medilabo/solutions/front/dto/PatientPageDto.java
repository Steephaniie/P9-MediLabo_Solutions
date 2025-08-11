package fr.medilabo.solutions.front.dto;

import java.util.List;

import lombok.Data;

@Data
public class PatientPageDto {

    private List<PatientDto> content;
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private boolean empty;

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isEmpty() {
        return empty;
    }
}
