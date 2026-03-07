package com.mockachu.web.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({
            "/",
            "/about",
            "/config",
            "/context",
            "/generate",
            "/import",
            "/kafka",
            "/log",
            "/request-graph",
            "/settings"
    })
    public String index() {
        return "forward:/index.html";
    }
}
