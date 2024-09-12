create schema if not exists bygning;

CREATE TABLE egenregistrering
(
    id                     UUID PRIMARY KEY,
    registrering_tidspunkt TIMESTAMP WITH TIME ZONE NOT NULL,
    registrering           JSONB                    NOT NULL
);

-- CREATE TABLE registrering
-- (
--     id                  UUID PRIMARY KEY,
--     egenregistrering_id UUID  NOT NULL REFERENCES egenregistrering (id),
-- );
