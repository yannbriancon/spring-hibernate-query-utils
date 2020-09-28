INSERT INTO users(id, name, pseudo)
VALUES (1, 'name', 'name1'),
       (2, 'name', 'name2');

INSERT INTO messages(id, text, author_id)
VALUES (1, 'small desc', 1),
       (2, 'long desc', 2);

INSERT INTO posts(id, message_id)
VALUES (1, 1),
       (2, 2);

INSERT INTO avatars(id, pseudo)
VALUES (1, 'name1'),
       (2, 'name1');
