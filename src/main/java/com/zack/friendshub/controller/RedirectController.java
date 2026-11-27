package com.zack.friendshub.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class RedirectController {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/reset-password")
    public void redirectResetPassword(@RequestParam(value = "token", required = false) String token,
                                      HttpServletResponse response) throws IOException {
        String target = frontendUrl + "/reset-password" + (token != null && !token.isBlank() ? "?token=" + token : "");
        response.sendRedirect(target);
    }
}
