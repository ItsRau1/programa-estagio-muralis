CREATE SCHEMA financas;

CREATE TABLE financas.usuario (
id bigserial NOT NULL,
nome varchar(150) NULL,
email varchar(100) NULL,
senha varchar(255) NULL,
data_cadastro date DEFAULT now() NULL,
CONSTRAINT usuario_pkey PRIMARY KEY (id)
);

CREATE TABLE financas.lancamento (
id bigserial NOT NULL,
descricao varchar(100) NULL,
mes int4 NOT NULL,
ano int4 NOT NULL,
valor numeric(16, 2) NOT NULL,
tipo varchar(20) NULL,
status varchar(20) NULL,
id_usuario bigserial NOT NULL,
data_cadastro date DEFAULT now() NULL,
data_atualizacao date NULL,
CONSTRAINT lancamento_pkey PRIMARY KEY (id),
CONSTRAINT lancamento_status_check CHECK (((status)::text = ANY (ARRAY[('PENDENTE'::character varying)::text, ('CANCELADO'::character varying)::text, ('EFETIVADO'::character varying)::text]))),
CONSTRAINT lancamento_tipo_check CHECK (((tipo)::text = ANY (ARRAY[('RECEITA'::character varying)::text, ('DESPESA'::character varying)::text])))
);

ALTER TABLE financas.lancamento ADD CONSTRAINT lancamento_usuario_fk FOREIGN KEY (id_usuario) REFERENCES financas.usuario(id) ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE financas.categoria (
id bigserial NOT NULL,
descricao varchar(255) NOT NULL,
ativa bool DEFAULT true NULL,
data_cadastro date NULL,
CONSTRAINT categoria_pkey PRIMARY KEY (id)
);

CREATE TABLE financas.categorias_lancamentos (
id_categoria bigserial NOT NULL,
id_lancamento bigserial NOT NULL,
CONSTRAINT categorias_lancamentos_pkey PRIMARY KEY (id_categoria, id_lancamento)
);

ALTER TABLE financas.categorias_lancamentos ADD CONSTRAINT categorias_lancamentos_categoria_fk FOREIGN KEY (id_categoria) REFERENCES financas.categoria(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE financas.categorias_lancamentos ADD CONSTRAINT categorias_lancamentos_lancamento_fk FOREIGN KEY (id_lancamento) REFERENCES financas.lancamento(id) ON DELETE CASCADE ON UPDATE CASCADE;

