package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.dto.PatientDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.medilabo.solutions.front.client.GatewayServiceClient;
import fr.medilabo.solutions.front.dto.PatientPageDto;

import java.util.List;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private GatewayServiceClient gatewayServiceClient;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Gère les requêtes GET vers l'endpoint "/home" et affiche une liste paginée
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
    @GetMapping("/home")
    @Cacheable(value = "patient")
    public String home(Model model) {
        // Affichage du contenu du cache
        if (cacheManager != null) {
            org.springframework.cache.Cache cache = cacheManager.getCache("patient");
            if (cache != null) {
                logger.debug("Cache 'patient' est présent : {}", cache.getName());
                logger.debug("Cache natif : {}", cache.getNativeCache());
            } else {
                logger.debug("Cache 'patient' non trouvé");
            }
        }
        try {
            List<PatientDto> patientPageDto = gatewayServiceClient.getAllPatients();
            model.addAttribute("patients", patientPageDto);
            logger.info("Successfully with {} patients", patientPageDto.size());
        } catch (Exception e) {
            logger.error("Error retrieving patients with pagination: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des patients");
        }
        return "home";
    }

}