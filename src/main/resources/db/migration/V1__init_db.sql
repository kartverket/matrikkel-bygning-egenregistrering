create schema if not exists bygning;

CREATE TABLE egenregistrering
(
    id                     UUID PRIMARY KEY,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    registrering           JSONB                    NOT NULL
);
