create table if not exists demodatasettings
(
    executed boolean not null
);

insert into demodatasettings (executed)
values (false)
