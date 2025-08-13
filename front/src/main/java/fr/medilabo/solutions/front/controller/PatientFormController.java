package fr.medilabo.solutions.front.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.medilabo.solutions.front.client.GatewayServiceClient;
import fr.medilabo.solutions.front.dto.PatientDto;

import jakarta.validation.Valid;

@Controller
public class PatientFormController {

    private static final Logger logger = LoggerFactory.getLogger(PatientFormController.class);

    @Autowired
    private GatewayServiceClient gatewayServiceClient;
    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;


    /**
     * Affiche le formulaire de création d'un nouveau patient.
     *
     * Cette méthode gère les requêtes GET vers "/patient/new" et prépare le modèle
     * avec un nouvel objet PatientDto vide et les attributs nécessaires pour
     * l'affichage du formulaire de création de patient.
     *
     * @param model l'objet Spring Model utilisé pour passer les attributs à la vue
     * @return le nom du template de vue "patient-form" à afficher
     */
    @GetMapping("/front/patient/new")
    public String showNewPatientForm(Model model) {
        PatientDto patient = new PatientDto();
        model.addAttribute("patient", patient);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Nouveau Patient");
        return "patientform";
    }


    /**
     * Affiche le formulaire de modification d'un patient existant.
     *
     * @param patientId l'identifiant unique du patient à modifier
     * @param model l'objet Spring Model pour passer les données à la vue
     * @return le nom du template de vue du formulaire patient, ou redirige vers l'accueil en cas d'erreur
     *
     * @throws Exception si la récupération du patient échoue ou si le patient n'est pas trouvé
     *
     * Cette méthode récupère les données du patient par ID et prépare le modèle avec :
     * - patient : l'objet PatientDto contenant les informations du patient
     * - isEdit : indicateur booléen mis à true pour le mode édition
     * - pageTitle : titre localisé pour le formulaire d'édition
     *
     * En cas de succès, retourne la vue "patient-form". En cas d'erreur, enregistre l'exception
     * et redirige vers "/home" avec un message d'erreur.
     */
    @GetMapping("/front/patient/{id}/edit")
    public String showEditPatientForm(@PathVariable("id") Long patientId, Model model) {
        try {
            PatientDto patient = gatewayServiceClient.getPatientById(patientId);
            model.addAttribute("patient", patient);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Modifier Patient");
            logger.info("Loaded patient {} for editing", patientId);
        } catch (Exception e) {
            logger.error("Error loading patient {} for editing: {}", patientId, e.getMessage());
            model.addAttribute("error", "Erreur lors du chargement du patient");
            return "redirect:"+gatewayUrl+"/front/home";
        }
        return "patientform";
    }


    /**
     * Gère la soumission du formulaire pour sauvegarder un patient (création ou mise à jour).
     *
     * Cette méthode traite les données du patient depuis un formulaire, valide les entrées,
     * et soit crée un nouveau patient soit met à jour un existant selon la présence
     * de l'ID du patient.
     *
     * @param patientDto L'objet de transfert de données patient contenant les données du formulaire, validé avec @Valid
     * @param bindingResult Le résultat du processus de validation, contient les erreurs de validation
     * @param model Le modèle Spring MVC pour ajouter des attributs à la vue
     * @param redirectAttributes Attributs à transmettre lors des opérations de redirection
     * @return String représentant le nom de la vue ou l'URL de redirection :
     *         - "patient-form" si des erreurs de validation surviennent ou si une exception est levée
     *         - "redirect:/home" si le patient est sauvegardé avec succès
     *
     * @throws Exception s'il y a une erreur pendant l'opération de sauvegarde (capturée et gérée en interne)
     *
     * La méthode effectue les opérations suivantes :
     * - Valide les données du patient et retourne à la vue du formulaire si la validation échoue
     * - Détermine s'il s'agit d'une création ou d'une mise à jour selon l'ID du patient
     * - Appelle la méthode appropriée du service gateway (création ou mise à jour)
     * - Ajoute des messages de succès/erreur aux attributs flash ou au modèle
     * - Enregistre les résultats de l'opération
     * - Redirige vers la page d'accueil en cas de succès ou retourne au formulaire en cas d'erreur
     */
    @PostMapping("/front/patient/save")
    public String savePatient(@Valid @ModelAttribute("patient") PatientDto patientDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", patientDto.getId() != 0);
            model.addAttribute("pageTitle", patientDto.getId() != 0 ? "Modifier Patient" : "Nouveau Patient");
            return "patientform";
        }

        try {
            boolean isEdit = patientDto.getId() != 0;

            if (isEdit) {
                gatewayServiceClient.updatePatient((long) patientDto.getId(), patientDto);
                redirectAttributes.addFlashAttribute("success", "Patient mis à jour avec succès");
                logger.info("Successfully updated patient {}", patientDto.getId());
            } else {
                PatientDto savedPatient = gatewayServiceClient.createPatient(patientDto);
                redirectAttributes.addFlashAttribute("success", "Patient créé avec succès");
                logger.info("Successfully created new patient with ID {}", savedPatient.getId());
            }
            return "redirect:" + gatewayUrl + "/front/home";

        } catch (Exception e) {
            logger.error("Error saving patient: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de l'enregistrement du patient");
            model.addAttribute("isEdit", patientDto.getId() != 0);
            model.addAttribute("pageTitle", patientDto.getId() != 0 ? "Modifier Patient" : "Nouveau Patient");
            return "patientform";
        }
    }
}