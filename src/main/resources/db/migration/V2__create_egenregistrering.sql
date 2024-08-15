CREATE TABLE bygning
(
    id BIGINT PRIMARY KEY
);

CREATE TABLE bruksenhet
(
    id         BIGINT PRIMARY KEY,
    bygning_id BIGINT NOT NULL REFERENCES bygning (id)
);

CREATE TABLE egenregistrering
(
    id          BIGSERIAL    PRIMARY KEY,
    registrerer VARCHAR(255) NOT NULL
);

-- Er dette noe nice måte å gjøre det på? Da slipper man at alle de forskjellige feltene man kan registrere
-- må definere registreringsmetadata
CREATE TABLE registrering
(
    id                     BIGSERIAL                PRIMARY KEY,
    egenregistrering_id    BIGINT                   NOT NULL REFERENCES egenregistrering (id),
    registrering_objekt_id BIGINT                   NOT NULL,
    registrering_tidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    gyldig_fra             DATE,
    gyldig_til             DATE
);

--- Dette er ikke mulig, da den forventer å referere til noe som finnes i både bygninger og bruksenheter
--- Hvordan kan vi gjøre noe liknende på en god måte?
-- ALTER TABLE registrering
--     ADD CONSTRAINT fk_registrering_objekt_bygning
--         FOREIGN KEY (registrering_objekt_id)
--             REFERENCES bygning (id);
--
-- ALTER TABLE registrering
--     ADD CONSTRAINT fk_registrering_objekt_bruksenhet
--         FOREIGN KEY (registrering_objekt_id)
--             REFERENCES bruksenhet (id);

CREATE TABLE byggeaar_registrering
(
    id       BIGINT PRIMARY KEY REFERENCES registrering (id),
    byggeaar INTEGER            NOT NULL
);
