package com.logistics.tracking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    
    @GetMapping("/tracking")
    public String tracking() {
        return "tracking";
    }
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
} 