create schema if not exists bygning;

CREATE TABLE egenregistrering
(
    id                     UUID PRIMARY KEY,
    registrerer            VARCHAR(255)             NOT NULL,
    registrering_tidspunkt TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE registrering
(
    id                  UUID PRIMARY KEY,
    egenregistrering_id UUID  NOT NULL REFERENCES egenregistrering (id),
    registrering        JSONB NOT NULL
);
