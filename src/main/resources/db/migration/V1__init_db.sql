create schema if not exists bygning;

CREATE TABLE egenregistrering
(
    id                     UUID PRIMARY KEY,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    eier                   VARCHAR(32)              NOT NULL,
    registrering           JSONB                    NOT NULL
);
