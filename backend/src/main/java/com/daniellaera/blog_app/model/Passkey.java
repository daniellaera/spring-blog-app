package com.daniellaera.blog_app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Passkey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private byte[] userHandle;
    private byte[] publicKey;
    @Column(unique = true)
    private byte[] keyId;
    @Column(name = "user_id")
    private UUID userId;
    private String type;
    private String transport;

    private long signatureCount;

    @Column(columnDefinition = "TEXT")
    private String clientDataJSON;

    @Column(columnDefinition = "TEXT")
    private String attestationObject;

    @Column
    private String authenticatorAttachment;

    private String origin;

    private boolean userPresence;
    private boolean userVerification;
    private boolean residentKey;

    @CreationTimestamp
    private Instant createdAt;

    private Instant lastUsedAt;

    private String deviceName;

    private String aaguid;

    private boolean backedUp;
}
