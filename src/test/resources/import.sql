INSERT INTO users(id, name)
VALUES (1, 'name'),
       (2, 'name');

INSERT INTO messages(id, text, author_id)
VALUES (1, 'small desc', 1),
       (2, 'long desc', 2);

INSERT INTO posts(id, message_id)
VALUES (1, 1),
       (2, 2);
