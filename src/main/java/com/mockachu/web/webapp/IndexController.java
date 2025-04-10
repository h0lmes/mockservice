package com.mockachu.web.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    /**
     * Thymeleaf redirect
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
