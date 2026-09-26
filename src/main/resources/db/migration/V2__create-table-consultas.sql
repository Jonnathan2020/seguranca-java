CREATE TABLE consultas (
    id BIGINT NOT NULL IDENTITY(1,1),
    medico_id BIGINT NOT NULL,
    paciente VARCHAR(100) NOT NULL,
    data DATETIME NOT NULL,

    PRIMARY KEY(id),
    CONSTRAINT fk_consultas_medico_id FOREIGN KEY(medico_id) REFERENCES medicos(id)
);