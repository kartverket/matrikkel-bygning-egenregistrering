create schema if not exists bygning;

CREATE TABLE kodeliste
(
    initialVerdi      VARCHAR NOT NULL PRIMARY KEY,
    kodenavn          VARCHAR NOT NULL,
    presentasjonsnavn VARCHAR NOT NULL,
    beskrivelse       VARCHAR NOT NULL
);

create table avlopskoder
(
) inherits (kodeliste);

create table vannforsyningskoder
(
) inherits (kodeliste);

create table energikildekoder
(
) inherits (kodeliste);

create table oppvarmingskoder
(
) inherits (kodeliste);