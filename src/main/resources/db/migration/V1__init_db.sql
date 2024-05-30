create schema if not exists bygning;

set search_path to bygning;

create table if not exists oppvarming
(
    id   serial primary key,
    navn varchar(50) not null
);

create table if not exists energikilde
(
    id   serial primary key,
    navn varchar(50) not null
);

create table if not exists bygning
(
    id       varchar(50) primary key,
    byggeaar int,
    vann     boolean not null,
    avlop    boolean not null
);

create table if not exists bygning_oppvarminger
(
    bygning_id    varchar(50) not null,
    oppvarming_id int         not null
);

create table if not exists bygning_energikilder
(
    bygning_id     varchar(50) not null,
    energikilde_id int         not null
);

alter table bygning_oppvarminger
    add primary key (bygning_id, oppvarming_id);
alter table bygning_energikilder
    add primary key (bygning_id, energikilde_id);

insert into oppvarming (navn)
values ('varmepumpe');
insert into energikilde (navn)
values ('geotermisk');

insert into bygning (id, vann, avlop)
values ('1', true, true);
insert into bygning (id, byggeaar, vann, avlop)
values ('2', 1983, true, false);

insert into bygning_oppvarminger (bygning_id, oppvarming_id)
values ('1', 1);
insert into bygning_energikilder (bygning_id, energikilde_id)
VALUES ('2', 1);