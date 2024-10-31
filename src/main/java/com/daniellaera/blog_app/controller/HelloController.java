package com.daniellaera.blog_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping
    public String hello() {
        return "Hello";
    }

    @GetMapping("/world")
    public String world() {
        return "Another one bite the duster";
    }
}
