package com.daniellaera.blog_app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;

    @Lob
    @Column(length = 1000000)
    private String publicKeyJson;

    @Lob
    @Column(length = 1000000)
    private String assertion;

    private byte[] userHandle;

    private boolean registrationComplete;
}