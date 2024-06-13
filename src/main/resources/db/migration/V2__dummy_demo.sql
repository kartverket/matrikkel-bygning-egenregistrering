create table if not exists demospokelser
(
    id            serial primary key,
    spokelsesnavn varchar(50) not null
);

insert into demospokelser(spokelsesnavn)
values ('Mr. Spooky'),
       ('Ghostman'),
       ('Hattifnatten'),
       ('Mr. Bones');