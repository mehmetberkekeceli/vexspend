package com.wallet.vexspend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/swagger-ui")
    public String redirectSwaggerUiRoot() {
        return "redirect:/swagger-ui.html";
    }
}
