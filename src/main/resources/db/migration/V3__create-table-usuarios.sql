CREATE TABLE usuarios (
    id BIGINT NOT NULL IDENTITY(1,1),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,

    PRIMARY KEY(id)
);


 --INSERT INTO usuarios(nome, email, senha) VALUES ('Joao', 'joao@email.com', 'joao123');
 --INSERT INTO usuarios(nome, email, senha) VALUES ('Maria', 'maria@email.com', 'maria123');