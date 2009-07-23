-- Creating domains

CREATE DOMAIN AUTHORIZATION_ROLE AS VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'POWER_USER', 'USER', 'OBSERVER', 'ETL_SERVER'));
CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;
CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) DEFAULT 'U' CHECK (VALUE IN ('F', 'T', 'U'));
CREATE DOMAIN CODE AS VARCHAR(40);
CREATE DOMAIN COLUMN_LABEL AS VARCHAR(40);
CREATE DOMAIN DATA_STORE_SERVICE_KIND AS VARCHAR(40) CHECK (VALUE IN ('PROCESSING', 'QUERIES'));
CREATE DOMAIN DESCRIPTION_1000 AS VARCHAR(1000);
CREATE DOMAIN DESCRIPTION_250 AS VARCHAR(250);
CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DELETION', 'INVALIDATION', 'MOVEMENT'));
CREATE DOMAIN FILE AS BYTEA;
CREATE DOMAIN FILE_NAME AS VARCHAR(100);
CREATE DOMAIN GENERIC_VALUE AS VARCHAR(1024);
CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);
CREATE DOMAIN REAL_VALUE AS REAL;
CREATE DOMAIN TECH_ID AS BIGINT;
CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;
CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE DOMAIN USER_ID AS VARCHAR(50);
CREATE DOMAIN TITLE_100 AS VARCHAR(100);
