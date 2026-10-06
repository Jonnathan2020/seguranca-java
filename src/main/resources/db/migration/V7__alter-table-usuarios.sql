-- Adiciona a coluna com um valor padrão (ex: 'PACIENTE') para os registros existentes
ALTER TABLE usuarios
ADD perfil VARCHAR(15) NOT NULL
    CONSTRAINT DF_usuarios_perfil DEFAULT 'PACIENTE';

-- Adiciona a restrição para aceitar apenas os três valores desejados
ALTER TABLE usuarios 
ADD CONSTRAINT CHK_usuarios_perfil 
    CHECK (perfil IN ('ATENDENTE', 'MEDICO', 'PACIENTE'));
