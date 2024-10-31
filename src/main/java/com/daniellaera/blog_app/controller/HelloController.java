package com.daniellaera.blog_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // main
    @GetMapping
    public String home() {
        return "Welcome to Spring Boot Application !";
    }

    // hello
    @GetMapping("/hello")
    public String hello() {
        return "Hello...";
    }

    // world
    @GetMapping("/world")
    public String world() {
        return "World!";
    }
}
