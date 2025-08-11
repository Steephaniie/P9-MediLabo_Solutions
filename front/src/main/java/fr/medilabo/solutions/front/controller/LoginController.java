package fr.medilabo.solutions.front.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.medilabo.solutions.front.dto.LoginRequest;
import fr.medilabo.solutions.front.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur gérant l'authentification des utilisateurs et la génération des jetons JWT.
 *
 * Ce contrôleur fournit :
 * - Une page de connexion
 * - Un point d'accès d'authentification qui génère des jetons JWT
 * - Gestion des cookies pour stocker les jetons
 */
@Controller
@Slf4j
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Affiche la page de connexion.
     *
     * @param model le modèle Spring MVC
     * @return le nom de la vue de connexion
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    /**
     * Traite la demande de connexion et génère un jeton JWT.
     *
     * @param loginRequest       les données de connexion
     * @param bindingResult      le résultat de la validation
     * @param model              le modèle Spring MVC
     * @param response           la réponse HTTP pour définir les cookies
     * @param redirectAttributes les attributs de redirection
     * @return redirection vers la page d'accueil ou retour à la page de connexion
     */
    @PostMapping("/login")
    public String authenticate(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

            String jwt = jwtUtil.generateToken(userDetails);

            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true); 
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");

            response.addCookie(jwtCookie);

            log.info("User '{}' logged in successfully", loginRequest.getUsername());

            redirectAttributes.addFlashAttribute("success", "Connexion réussie !");
            return "redirect:/home";

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user '{}': {}",
                    loginRequest.getUsername(), e.getMessage());
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            return "login";
        }
    }
}
