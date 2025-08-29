package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.client.PatientServiceClient;
import fr.medilabo.solutions.front.dto.PatientDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final PatientServiceClient patientServiceClient;

    /**
     * Gère les requêtes GET vers l'endpoint "/home" et affiche une liste
     * de patients.
     *
     * Cette méthode récupère les patients depuis le service gateway
     * puis ajoute les attributs nécessaires au modèle pour le rendu dans la vue "home".
     *
     * @param model   l'objet modèle Spring MVC utilisé pour transmettre les données à la vue
     * @return le nom du template de vue ("home") à rendre
     *
     * @throws Exception si une erreur survient lors de la récupération des patients
     *                   depuis le service gateway
     */
    @GetMapping("home")
    public String home(Model model) {
        try {
            List<PatientDto> patientPageDto = patientServiceClient.getAllPatients();
            model.addAttribute("patients", patientPageDto);
            logger.info("Successfully with {} patients", patientPageDto.size());
        } catch (Exception e) {
            logger.error("Error retrieving patients with pagination: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des patients");
        }
        return "home";
    }

}