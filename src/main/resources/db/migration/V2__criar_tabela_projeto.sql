CREATE TABLE projeto (
     id BIGSERIAL PRIMARY KEY,
     nome VARCHAR(200) NOT NULL,
     descricao TEXT,
     data_inicio DATE NOT NULL,
     previsao_termino DATE NOT NULL,
     data_real_termino DATE,
     orcamento_total NUMERIC(15, 2) NOT NULL,
     status VARCHAR(30) NOT NULL,
     gerente_id BIGINT NOT NULL,
     CONSTRAINT fk_projeto_gerente FOREIGN KEY (gerente_id) REFERENCES membro(id)
);