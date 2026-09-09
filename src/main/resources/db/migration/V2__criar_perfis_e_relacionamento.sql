CREATE TABLE perfis (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(30) NOT NULL,

    CONSTRAINT uk_perfis_nome UNIQUE (nome)
);

CREATE TABLE usuarios_perfis (
    usuario_id BIGINT NOT NULL,
    perfil_id BIGINT NOT NULL,

    CONSTRAINT pk_usuarios_perfis
        PRIMARY KEY (usuario_id, perfil_id),

    CONSTRAINT fk_usuarios_perfis_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id),

    CONSTRAINT fk_usuarios_perfis_perfil
        FOREIGN KEY (perfil_id)
        REFERENCES perfis (id)
);

INSERT INTO perfis (nome)
VALUES
    ('USUARIO'),
    ('SUPORTE'),
    ('ADMIN');