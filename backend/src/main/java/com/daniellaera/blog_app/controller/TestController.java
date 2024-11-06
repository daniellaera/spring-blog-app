package com.daniellaera.blog_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/exception")
    public String throwException() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }
}
