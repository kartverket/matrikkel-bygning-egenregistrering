create schema if not exists bygning;

CREATE TABLE egenregistrering
(
    id                     UUID PRIMARY KEY,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    eier                   VARCHAR(32)              NOT NULL,
    registrering           JSONB                    NOT NULL
);


-- TODO: Gir det mening som primary key? hva ellers burde det vært? Eget løpenummer? Versjon?
-- Navngi etter matrikkel/boble på id
CREATE TABLE bruksenhet
(
    id                     UUID                     NOT NULL,
--     TODO: riktig type på bruksenhetid
    bruksenhetId           VARCHAR(32)              NOT NULL,
    bygningId              UUID                     NOT NULL,
    registreringstidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    data                   JSONB                    NOT NULL,
    PRIMARY KEY (id, registreringstidspunkt)
);
