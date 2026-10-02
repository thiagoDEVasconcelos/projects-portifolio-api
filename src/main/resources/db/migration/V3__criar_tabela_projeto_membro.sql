CREATE TABLE projeto_membro (
    projeto_id BIGINT NOT NULL,
    membro_id BIGINT NOT NULL,
    PRIMARY KEY (projeto_id, membro_id),
    CONSTRAINT fk_pm_projeto FOREIGN KEY (projeto_id) REFERENCES projeto(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_membro FOREIGN KEY (membro_id) REFERENCES membro(id)
);