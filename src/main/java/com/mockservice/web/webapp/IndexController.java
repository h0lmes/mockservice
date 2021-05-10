package com.mockservice.web.webapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @Value("${spring.application.name}")
    String applicationName;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("applicationName", applicationName);
        return "index";
    }

    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("applicationName", applicationName);
        return "error";
    }
}
