package com.zack.friendshub.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping(value = {"/", "/login", "/register", "/friends", "/availability", "/common-slots", "/meetings"})
    public String forward() {
        return "forward:/index.html";
    }
}
