DROP SEQUENCE IF EXISTS hibernate_sequence;
DROP TABLE IF EXISTS RxPerson;
DROP TABLE IF EXISTS RxDog;

CREATE SEQUENCE hibernate_sequence;
CREATE TABLE RxPerson (id integer not null, name varchar, status integer, PRIMARY KEY (id));
CREATE TABLE RxDog (id integer not null, name varchar, race varchar, owner_id integer, PRIMARY KEY (id));
