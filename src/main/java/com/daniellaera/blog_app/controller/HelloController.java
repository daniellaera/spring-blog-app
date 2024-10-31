package com.daniellaera.blog_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // hello
    @GetMapping
    public String hello() {
        return "Welcome to Spring Boot Application";
    }

    @GetMapping("/hello")
    public String world() {
        return "Hello World";
    }
}
