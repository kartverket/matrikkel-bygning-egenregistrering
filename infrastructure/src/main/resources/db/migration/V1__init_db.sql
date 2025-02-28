create schema if not exists bygning;

CREATE TABLE egenregistrering
(
    id                     UUID PRIMARY KEY,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    eier                   VARCHAR(32)              NOT NULL,
    registrering           JSONB                    NOT NULL
);


CREATE TABLE bruksenhet
(
    id                     UUID                     NOT NULL,
    bruksenhet_bubble_id   BIGINT                   NOT NULL,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    data                   JSONB                    NOT NULL,
    PRIMARY KEY (id, registreringstidspunkt)
);

CREATE TABLE hendelse
(
    sekvensnummer          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    object_id              BIGINT                   NOT NULL,
    type                   VARCHAR(32)              NOT NULL
);
