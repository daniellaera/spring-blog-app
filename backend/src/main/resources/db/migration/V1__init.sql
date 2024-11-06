CREATE TABLE post
(
    id      SERIAL PRIMARY KEY,
    title   VARCHAR(255),
    content TEXT
);

CREATE TABLE comment
(
    id      SERIAL PRIMARY KEY,
    text TEXT,
    post_id BIGINT,
    FOREIGN KEY (post_id) REFERENCES post(id)
);

INSERT INTO post (title, content)
VALUES ('Title 1', 'Content 1'),
       ('Title 2', 'Content 2'),
       ('Title 3', 'Content 3'),
       ('Title 4', 'Content 4 ');
INSERT INTO comment (text, post_id)
VALUES ('comment 1', 1),
       ('comment 2', 2),
       ('comment 3', 3);