package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.client.NoteServiceClient;
import fr.medilabo.solutions.front.client.PatientServiceClient;
import fr.medilabo.solutions.front.client.RapportPatientServiceClient;
import fr.medilabo.solutions.front.config.UrlConfiguration;
import fr.medilabo.solutions.front.dto.DiabeteNiveauRisqueEnum;
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
    private final RapportPatientServiceClient rapportPatientServiceClient;

    /**
     * Récupère et affiche les notes du patient avec ses informations et l'évaluation
     * du risque.
     *
     * Cette méthode récupère les données complètes du patient incluant :
     * - Les détails du patient par ID
     * - Toutes les notes associées au patient
     * - Le niveau d'évaluation du risque pour le patient
     *
     * Elle prépare également un nouvel objet note vide pour une potentielle création.
     * En cas d'erreur lors de la récupération des données, un message d'erreur est 
     * ajouté au modèle.
     *
     * @param patientId l'identifiant unique du patient dont les notes doivent être
     *                  récupérées
     * @param model     le modèle Spring MVC auquel les attributs sont ajoutés pour
     *                  le rendu de la vue
     * @return le nom du template de vue ("notes") à afficher
     *
     * @throws Exception si une erreur survient pendant la récupération des données
     *                   depuis les services gateway
     */
    @GetMapping("{id}")
    public String getPatientNote(@PathVariable("id") Long patientId, Model model) {
        try {
            PatientDto patient = patientServiceClient.getPatientById(patientId);
            model.addAttribute("patient", patient);

            List<NoteDto> notes = noteServiceClient.getNoteByPatientId(patientId.intValue());
            model.addAttribute("notes", notes);

            DiabeteNiveauRisqueEnum niveauRisque = rapportPatientServiceClient.getRapportByIdPatient(patientId);
            model.addAttribute("niveauRisque", niveauRisque.name());

            NoteDto newNote = new NoteDto();
            newNote.setPatId(patientId.intValue());
            model.addAttribute("newNote", newNote);

            logger.info("Successfully retrieved patient {} with {} notes", patientId, notes.size());

        } catch (Exception e) {
            logger.error("Error retrieving patient notes for ID {}: {}", patientId, e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des données du patient");
            // Assurer le binding Thymeleaf même en cas d'erreur
            model.addAttribute("newNote", new NoteDto());
        }

        return "patientNote";
    }

    /**
     * Ajoute une nouvelle note pour un patient spécifique.
     *
     * Cette méthode gère la requête POST pour créer une nouvelle note associée à un
     * patient.
     * Elle valide les données de la note, récupère les informations du patient pour
     * définir son nom,
     * et sauvegarde la note via le client service gateway.
     *
     * @param patientId          l'identifiant unique du patient pour lequel la note
     *                           est ajoutée
     * @param noteDto            l'objet de transfert de données contenant les
     *                           informations de la note à sauvegarder
     * @param bindingResult      le résultat du processus de validation pour le
     *                           noteDto
     * @param redirectAttributes attributs à transmettre à la vue de redirection pour
     *                          l'affichage des messages
     * @return une URL de redirection vers la page des notes du patient spécifié
     *
     * @throws Exception si une erreur survient pendant la récupération du patient ou
     *                   la création de la note
     *
     *                   La méthode effectue les opérations suivantes :
     *                   - Valide les données de la note et retourne avec un message
     *                   d'erreur si la validation échoue
     *                   - Récupère les informations du patient pour définir son nom
     *                   dans la note
     *                   - Définit l'ID du patient et efface tout ID de note existant
     *                   pour l'auto-génération
     *                   - Crée la note via le service gateway
     *                   - Ajoute des messages de succès ou d'erreur aux attributs de
     *                   redirection
     *                   - Enregistre le résultat de l'opération pour le suivi
     */
    @PostMapping("{id}")
    public String addNote(@PathVariable("id") Long patientId,
            @Valid @ModelAttribute("newNote") NoteDto noteDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("noteError", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:" +urlConfiguration.getUrlSitePublic()+"/note/" + patientId;
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
     * Met à jour les informations d'un patient existant.
     *
     * Ce point d'entrée gère les requêtes POST pour mettre à jour les données du
     * patient. Il valide les données,
     * met à jour le patient via le service gateway, et redirige vers la page des
     * notes du patient
     * avec les messages de succès ou d'erreur appropriés.
     *
     * @param patientId          l'identifiant unique du patient à mettre à jour
     * @param patientDto         l'objet de transfert de données contenant les
     *                           informations mises à jour,
     *                           validé avec l'annotation @Valid
     * @param bindingResult      le résultat du processus de validation, contient les
     *                           erreurs de validation
     * @param redirectAttributes attributs à transmettre à la cible de redirection
     *                          pour les messages flash
     * @return URL de redirection vers la page des notes du patient (/notes/{patientId})
     *
     * @throws Exception si une erreur survient pendant le processus de mise à jour
     *                   du patient
     *
     *                   Attributs flash ajoutés :
     *                   - "success" : message de confirmation quand la mise à jour
     *                   réussit
     *                   - "patientError" : message d'erreur quand la validation
     *                   échoue ou l'opération de mise à jour échoue
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