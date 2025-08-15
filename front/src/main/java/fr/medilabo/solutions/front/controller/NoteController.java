package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.client.NoteServiceClient;
import fr.medilabo.solutions.front.client.PatientServiceClient;
import fr.medilabo.solutions.front.config.UrlConfiguration;
import fr.medilabo.solutions.front.dto.NoteDto;
import fr.medilabo.solutions.front.dto.PatientDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("note")
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final NoteServiceClient noteServiceClient;
    private final PatientServiceClient patientServiceClient;
    private final UrlConfiguration urlConfiguration;

    /**
     * Retrieves and displays patient notes along with patient information and risk
     * assessment.
     * 
     * This method fetches comprehensive patient data including:
     * - Patient details by ID
     * - All notes associated with the patient
     * - Risk assessment level for the patient
     * 
     * It also prepares a new empty note object for potential note creation.
     * In case of any error during data retrieval, an error message is added to the
     * model.
     * 
     * @param patientId the unique identifier of the patient whose notes are to be
     *                  retrieved
     * @param model     the Spring MVC model to which attributes are added for view
     *                  rendering
     * @return the name of the view template ("notes") to be rendered
     * 
     * @throws Exception if there's an error during patient data retrieval from
     *                   gateway services
     */
    @GetMapping("{id}")
    public String getPatientNote(@PathVariable("id") Long patientId, Model model) {
        try {
            PatientDto patient = patientServiceClient.getPatientById(patientId);
            model.addAttribute("patient", patient);

            List<NoteDto> notes = noteServiceClient.getNoteByPatientId(patientId.intValue());
            model.addAttribute("notes", notes);

            NoteDto newNote = new NoteDto();
            newNote.setPatId(patientId.intValue());
            model.addAttribute("newNote", newNote);

            logger.info("Successfully retrieved patient {} with {} notes", patientId, notes.size());

        } catch (Exception e) {
            logger.error("Error retrieving patient notes for ID {}: {}", patientId, e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des données du patient");
        }

        return "patientNote";
    }

    /**
     * Adds a new note for a specific patient.
     * 
     * This method handles the POST request to create a new note associated with a
     * patient.
     * It validates the note data, retrieves patient information to set the patient
     * name,
     * and saves the note through the gateway service client.
     * 
     * @param patientId          the unique identifier of the patient for whom the
     *                           note is being added
     * @param noteDto            the note data transfer object containing the note
     *                           information to be saved
     * @param bindingResult      the result of the validation process for the
     *                           noteDto
     * @param redirectAttributes attributes to be passed to the redirect view for
     *                           displaying messages
     * @return a redirect URL to the notes page for the specified patient
     * 
     * @throws Exception if there's an error during patient retrieval or note
     *                   creation
     * 
     *                   The method performs the following operations:
     *                   - Validates the input note data and returns with error
     *                   message if validation fails
     *                   - Retrieves patient information to set the patient name in
     *                   the note
     *                   - Sets the patient ID and clears any existing note ID for
     *                   auto-generation
     *                   - Creates the note through the gateway service
     *                   - Adds success or error messages to redirect attributes
     *                   - Logs the operation result for monitoring purposes
     */
    @PostMapping("{id}")
    public String addNote(@PathVariable("id") Long patientId,
            @Valid @ModelAttribute("newNote") NoteDto noteDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("noteError", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:" +urlConfiguration.getUrlSitePublic()+"/notes/" + patientId;
        }

        try {
            PatientDto patient = patientServiceClient.getPatientById(patientId);

            noteDto.setId(null);
            noteDto.setPatId(patientId.intValue());
            noteDto.setPatient(patient.getFirstname());

            logger.info("Note to be added: {}", noteDto);

            noteServiceClient.createNote(noteDto);
            redirectAttributes.addFlashAttribute("success", "Note ajoutée avec succès");
            logger.info("Successfully added note for patient {}", patientId);

        } catch (Exception e) {
            logger.error("Error adding note for patient {}: {}", patientId, e.getMessage());
            redirectAttributes.addFlashAttribute("noteError", "Erreur lors de l'ajout de la note");
        }

        return "redirect:" +urlConfiguration.getUrlSitePublic()+"/note/" + patientId;
    }

    /**
     * Updates an existing patient's information.
     * 
     * This endpoint handles POST requests to update patient data. It validates the
     * input,
     * updates the patient through the gateway service, and redirects back to the
     * patient's
     * notes page with appropriate success or error messages.
     * 
     * @param patientId          the unique identifier of the patient to update
     * @param patientDto         the patient data transfer object containing updated
     *                           information,
     *                           validated with @Valid annotation
     * @param bindingResult      the result of the validation process, contains any
     *                           validation errors
     * @param redirectAttributes attributes to be passed to the redirect target for
     *                           flash messages
     * @return redirect URL to the patient's notes page (/notes/{patientId})
     * 
     * @throws Exception if an error occurs during the patient update process
     * 
     *                   Flash attributes added:
     *                   - "success": confirmation message when update is successful
     *                   - "patientError": error message when validation fails or
     *                   update operation fails
     */
    @PostMapping("{id}/update")
    public String updatePatient(@PathVariable("id") Long patientId,
            @Valid @ModelAttribute("patient") PatientDto patientDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("patientError", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:" +urlConfiguration.getUrlSitePublic()+"/note/" + patientId;
        }

        try {
            patientServiceClient.updatePatient(patientId, patientDto);
            redirectAttributes.addFlashAttribute("success", "Informations patient mises à jour avec succès");
            logger.info("Successfully updated patient {}", patientId);

        } catch (Exception e) {
            logger.error("Error updating patient {}: {}", patientId, e.getMessage());
            redirectAttributes.addFlashAttribute("patientError", "Erreur lors de la mise à jour des informations");
        }
        return "redirect:" +urlConfiguration.getUrlSitePublic()+"/note/" + patientId;
    }
}