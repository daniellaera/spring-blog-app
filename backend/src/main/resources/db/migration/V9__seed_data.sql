-- Clear V1 dummy seed data and replace with meaningful content.
-- RESTART IDENTITY resets the SERIAL sequences on post and comment.
TRUNCATE comment, post RESTART IDENTITY CASCADE;

-- Users: UUID id, BYTEA user_handle (NOT NULL), registration_complete (NOT NULL).
-- No email or created_at columns exist in this schema.
-- Passkey records are device-bound and cannot be seeded.
INSERT INTO users (id, username, user_handle, registration_complete)
VALUES
    ('11111111-1111-1111-1111-111111111111',
     'daniel',
     decode('deadbeef01020304', 'hex'),
     false),
    ('22222222-2222-2222-2222-222222222222',
     'alice',
     decode('cafebabe05060708', 'hex'),
     false);

-- Posts: table name is 'post' (singular); no user_id FK, no created_at column.
INSERT INTO post (title, content, author)
VALUES
    ('Getting started with Passkeys',
     'Passkeys are the future of authentication. Unlike passwords, they are phishing-resistant, device-bound, and use public key cryptography under the hood. In this post we explore how to implement them with Spring Boot and the Yubico library.',
     'daniel'),
    ('Spring Boot 4 whats new',
     'Spring Boot 4 brings major changes: full Jakarta EE 11 support, modularized starters, Jackson 3 as default, virtual threads enabled by default, and a completely overhauled autoconfiguration mechanism. Here is what you need to know to migrate.',
     'daniel'),
    ('Angular 21 and PrimeNG 21',
     'Angular 21 introduces zoneless change detection as stable, improved signals API, and better SSR support. Combined with PrimeNG 21 standalone components and the Aura theme, building modern UIs has never been cleaner.',
     'alice'),
    ('Why passwordless is the future',
     'Every major platform — Apple, Google, Microsoft — has committed to passkeys. The FIDO2 standard is mature, browser support is universal, and user experience is dramatically better than passwords. Here is why you should adopt it today.',
     'alice');

-- Comments: table name is 'comment' (singular).
-- created_at has a DEFAULT so it can be omitted.
INSERT INTO comment (text, author, post_id)
VALUES
    ('Great post! Passkeys really are the future.', 'alice', 1),
    ('Thanks for the Spring Boot 4 breakdown, very helpful.', 'alice', 2),
    ('Angular signals are a game changer.', 'daniel', 3);
