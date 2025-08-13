package fr.medilabo.solutions.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HomeController {
    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @GetMapping("")
    public String home( ) {

         return "redirect:" + gatewayUrl + "/front/home";
    }
}