DROP SEQUENCE IF EXISTS hibernate_sequence;
DROP TABLE IF EXISTS RxPerson;
DROP TABLE IF EXISTS RxDog;
DROP TABLE IF EXISTS RxDataTypeEntity;

CREATE SEQUENCE hibernate_sequence;
CREATE TABLE RxPerson (id bigint not null, name varchar, status integer, PRIMARY KEY (id));
CREATE TABLE RxDog (id bigint not null, name varchar, race varchar, owner_id bigint, PRIMARY KEY (id));

CREATE TABLE RxDataTypeEntity (id bigint not null, 
 primitiveBoolean boolean NOT NULL,
 boxedBoolean boolean,

 primitiveCharacter character (1) NOT NULL,
 boxedCharacter character (1),

 primitiveByte smallint NOT NULL,
 boxedByte smallint,
 primitiveShort smallint NOT NULL,
 boxedShort smallint,
 primitiveInteger integer NOT NULL,
 boxedInteger integer,
 primitiveLong bigint NOT NULL,
 boxedLong bigint,

 primitiveFloat real NOT NULL,
 boxedFloat real,
 primitiveDouble double precision NOT NULL,
 boxedDouble double precision,

 PRIMARY KEY (id)
);
