package com.example.employeeapi.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Route d'accueil : permet de vérifier que le serveur répond. */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "OK";
    }
}
