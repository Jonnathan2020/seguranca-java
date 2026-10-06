ALTER TABLE consultas DROP FOREIGN KEY fk_consultas_medico_id;

ALTER TABLE medicos MODIFY id BIGINT NOT NULL;

ALTER TABLE consultas
ADD CONSTRAINT fk_consultas_medico_id
FOREIGN KEY (medico_id) REFERENCES medicos(id);