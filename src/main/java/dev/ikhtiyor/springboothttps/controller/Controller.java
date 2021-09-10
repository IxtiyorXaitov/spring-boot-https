package dev.ikhtiyor.springboothttps.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/")
    public String helloController() {
        return "Welcome yo my HTTPS Spring web application";
    }
}
