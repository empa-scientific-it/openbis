----------------------------------------------------------------------------------------------
--  File: migration-015-016.sql
--
-- 
--  This script enables the migration of the database schema from 015 to 016.
-- 
--  Update History
--
--
--	Who			When		What
--	---			----		----
--	Bernd Rinn	2008-03-22	Initial Version 
--  
----------------------------------------------------------------------------------------------


--=================================
-- New Domains
--=================================

-----------------------------------------------------------------------------------
--  Purpose:  Change all CODE columns to LONG_CODE, then re-create DOMAIN CODE identical to LONG_CODE
--  and change LONG_CODE columns back to the (new) CODE 
-----------------------------------------------------------------------------------

-- First step: create new domain LONG_CODE
CREATE DOMAIN LONG_CODE AS VARCHAR(40);

-- Second step: change all CODE to LONG_CODE
ALTER TABLE CONTROLLED_VOCABULARIES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE DATA_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE EXPERIMENTS ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE EXPERIMENT_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE FILE_FORMAT_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE LOCATOR_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE MATERIALS ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE MATERIAL_BATCHES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE MATERIAL_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE OBSERVABLE_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE ORGANIZATIONS ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE PROCEDURE_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE PROJECTS ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE PROPERTY_TYPES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE SAMPLES ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE SAMPLE_COMPONENTS ALTER COLUMN CODE TYPE LONG_CODE;
ALTER TABLE SAMPLE_TYPES ALTER COLUMN CODE TYPE LONG_CODE;

-- Third step: re-create CODE as VARCHAR(40)
DROP DOMAIN CODE;
CREATE DOMAIN CODE AS VARCHAR(40);

-- Forth step: change all LONG_CODE columns back to CODE
ALTER TABLE CONTROLLED_VOCABULARIES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE DATA_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE EXPERIMENTS ALTER COLUMN CODE TYPE CODE;
ALTER TABLE EXPERIMENT_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE FILE_FORMAT_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE LOCATOR_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE MATERIALS ALTER COLUMN CODE TYPE CODE;
ALTER TABLE MATERIAL_BATCHES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE MATERIAL_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE OBSERVABLE_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE ORGANIZATIONS ALTER COLUMN CODE TYPE CODE;
ALTER TABLE PROCEDURE_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE PROJECTS ALTER COLUMN CODE TYPE CODE;
ALTER TABLE PROPERTY_TYPES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE SAMPLES ALTER COLUMN CODE TYPE CODE;
ALTER TABLE SAMPLE_COMPONENTS ALTER COLUMN CODE TYPE CODE;
ALTER TABLE SAMPLE_TYPES ALTER COLUMN CODE TYPE CODE;

-- Fifth step: drop domain LONG_CODE
DROP DOMAIN LONG_CODE;

-----------------------------------------------------------------------------------
--  Purpose:  Create new domain TIME_STAMP_DFL and change current users of TIME_STAMP to TIME_STAMP_DFL
--  This adds time zone information and allows for a clean separation between registration time stamps
--  which should have a default of CURRENT_TIMESTAMP and other time stamps which should not. 
-----------------------------------------------------------------------------------

-- First step: create new domain TIME_STAMP_DFL
CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Second step: change all TIME_STAMP columns to TIME_STAMP_DFL
ALTER TABLE CONTROLLED_VOCABULARIES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE CONTROLLED_VOCABULARIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE CONTROLLED_VOCABULARIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE DATA ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE DATA ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE DATA ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE EXPERIMENTS ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE EXPERIMENTS ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE EXPERIMENTS ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE EXPERIMENT_ATTACHMENTS ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE EXPERIMENT_ATTACHMENTS ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE EXPERIMENT_ATTACHMENTS ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE EXPERIMENT_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE EXPERIMENT_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE EXPERIMENT_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE INVALIDATIONS ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE INVALIDATIONS ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE INVALIDATIONS ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE MATERIALS ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE MATERIALS ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE MATERIALS ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE MATERIAL_BATCHES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE MATERIAL_BATCHES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE MATERIAL_BATCHES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE MATERIAL_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE MATERIAL_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE MATERIAL_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE PROCEDURES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE PROCEDURES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE PROCEDURES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE SAMPLES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE SAMPLES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE SAMPLES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE SAMPLE_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE SAMPLE_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE SAMPLE_PROPERTIES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP TYPE TIME_STAMP_DFL;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP NOT NULL;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ALTER COLUMN REGISTRATION_TIMESTAMP DROP DEFAULT;

-- Third step: drop old domain TIME_STAMP
DROP DOMAIN TIME_STAMP;

-- Forth step: create new domain time_stamp
CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;

--=================================
-- New Tables and related objects
--=================================

------------------------------------------------------------------------------------
-- table DATA_SET_RELATIONSHIPS (new)
------------------------------------------------------------------------------------

-- Creating tables

CREATE TABLE DATA_SET_RELATIONSHIPS (ID TECH_ID NOT NULL,DATA_ID_PARENT TECH_ID NOT NULL,DATA_ID_CHILD TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL);

-- Creating primary key constraints

ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_PK PRIMARY KEY(ID);

-- Creating unique constraints

ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_BK_UK UNIQUE(DATA_ID_CHILD,DATA_ID_PARENT);

-- Creating foreign key constraints

ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_CHILD FOREIGN KEY (DATA_ID_CHILD) REFERENCES DATA(ID);
ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_PARENT FOREIGN KEY (DATA_ID_PARENT) REFERENCES DATA(ID);

-- Creating sequences

CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;

-- Creating indexes

CREATE INDEX DSRE_DATA_FK_I_CHILD ON DATA_SET_RELATIONSHIPS (DATA_ID_CHILD);
CREATE INDEX DSRE_DATA_FK_I_PARENT ON DATA_SET_RELATIONSHIPS (DATA_ID_PARENT);

--====================================
-- Drop Tables and related objects
--====================================

------------------------------------------------------------------------------------
-- table DATA_VALUES (deleted)
------------------------------------------------------------------------------------

-- Delete constraints

ALTER TABLE DATA_VALUES DROP CONSTRAINT DAVA_PK;
ALTER TABLE DATA_VALUES DROP CONSTRAINT DAVA_BK_UK;
ALTER TABLE DATA_VALUES DROP CONSTRAINT DAVA_DATA_FK;
ALTER TABLE DATA_VALUES DROP CONSTRAINT DAVA_SACO_FK;

-- Delete indices

DROP INDEX DAVA_DATA_FK_I;
DROP INDEX DAVA_SACO_FK_I;

-- Delete sequences

DROP SEQUENCE DATA_VALUE_ID_SEQ;

-- Delete tables

DROP TABLE DATA_VALUES;

--====================================
-- Modifications of existing objects
--====================================

------------------------------------------------------------------------------------
-- table ORGANIZATIONS (changed)
-- * rename to GROUPS
--
-- table DATA (changed)
-- * rename column PROC_ID_ACQUIRED_BY into: PROC_ID_PRODUCED_BY
-- * add columns:
-- ** IS_PLACEHOLDER BOOLEAN_CHAR
-- ** CODE CODE
-- ** DATA_PRODUCER_CODE CODE
-- ** PRODUCTION_TIMESTAMP TIME_STAMP
--
-- table EXTERNAL_DATA (changed)
-- * add column:
-- ** CVTE_ID_STOR_FMT TECH_ID NOT NULL
--
-- table PROPERTY_TYPES (changed)
-- * add column IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F'
-- * add unique constraint PRTY_BK_UK_LBL for column LABEL
--
-- tables EXPERIMENT_TYPE_PROPERTY_TYPES, MATERIAL_TYPE_PROPERTY_TYPES, SAMPLE_TYPE_PROPERTY_TYPES
-- * make the assignments of property types DESCRIPTION, NUCLEOTIDE_SEQUENCE, OFFSET and GENE_SYMBOL 
--   to material types internally managed 
--
-- table PROCEDURE_TYPES (changed)
-- * add column IS_DATA_ACQUISITION BOOLEAN_CHAR NOT NULL DEFAULT 'F'.
--
-- all tables
-- * convert all codes to upper case
------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------
--  Purpose:  Rename table ORGANIZATIONS to GROUPS.
---------------------------------------------------------------------------------------------------

ALTER TABLE PROJECTS DROP CONSTRAINT PROJ_ORGA_FK;
ALTER TABLE ORGANIZATIONS DROP CONSTRAINT ORGA_BK_UK;
ALTER TABLE ORGANIZATIONS DROP CONSTRAINT ORGA_PK;
ALTER TABLE ORGANIZATIONS RENAME TO GROUPS;
ALTER INDEX PROJ_ORGA_FK_I RENAME TO PROJ_GROU_FK_I;
select RENAME_SEQUENCE('ORGANIZATION_ID_SEQ', 'GROUP_ID_SEQ');
ALTER TABLE PROJECTS RENAME COLUMN ORGA_ID TO GROU_ID;
ALTER TABLE GROUPS ADD CONSTRAINT GROU_PK PRIMARY KEY(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_BK_UK UNIQUE(CODE);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);


---------------------------------------------------------------------------------------------------
--  Purpose:  Add columnd IS_PLACEHOLDER of table DATA.
---------------------------------------------------------------------------------------------------

ALTER TABLE DATA ADD COLUMN IS_PLACEHOLDER BOOLEAN_CHAR NOT NULL;


---------------------------------------------------------------------------------------------------
--  Purpose:  Add and populate column CODE of table DATA.
---------------------------------------------------------------------------------------------------

ALTER TABLE DATA ADD COLUMN CODE CODE;

CREATE OR REPLACE FUNCTION populate_data_codes() RETURNS integer AS $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM data LOOP
        rec.code := to_char(rec.registration_timestamp, 'YYYYMMDDHH24MISSMS') || '-' || to_char(rec.id, 'FM99999999999999999999');
        update data set code=rec.code where id=rec.id;
    END LOOP;
    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

SELECT populate_data_codes();
DROP FUNCTION populate_data_codes();

ALTER TABLE DATA ALTER COLUMN CODE SET NOT NULL;
ALTER TABLE DATA ADD CONSTRAINT DATA_BK_UK UNIQUE(CODE);

---------------------------------------------------------------------------------------------------
--  Purpose:  Add and populate columns CVTE_ID_STOR_FMT.
---------------------------------------------------------------------------------------------------

ALTER TABLE EXTERNAL_DATA ADD COLUMN CVTE_ID_STOR_FMT TECH_ID;
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_STOR_FMT_FK FOREIGN KEY (CVTE_ID_STOR_FMT) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);

------------------------------------------------------------------------------------
--  Purpose:  Create trigger EXTERNAL_DATA_STORAGE_FORMAT_CHECK 
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$
DECLARE
   v_covo_code  CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);

   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != '.STORAGE_FORMAT' then
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', NEW.code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER EXTERNAL_DATA_STORAGE_FORMAT_CHECK BEFORE INSERT OR UPDATE ON EXTERNAL_DATA
    FOR EACH ROW EXECUTE PROCEDURE EXTERNAL_DATA_STORAGE_FORMAT_CHECK();

-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary .STORAGE_FORMAT
-----------------------------------------------------------------------------------
insert into controlled_vocabularies 
       ( id
       , code
       , description
       , pers_id_registerer
       , is_managed_internally )
values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')
       , '.STORAGE_FORMAT'
       , 'The on-disk storage format of a data set'
       , (select id from persons where user_id ='system')
       , true);


-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary Terms for STORAGE_FORMAT
-----------------------------------------------------------------------------------
insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer )
values  (nextval('CVTE_ID_SEQ')
       , 'PROPRIETARY'
       , (select id from controlled_vocabularies where code = '.STORAGE_FORMAT')
       , (select id from persons where user_id ='system'));

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer )
values  (nextval('CVTE_ID_SEQ')
       , 'BDS_DIRECTORY'
       , (select id from controlled_vocabularies where code = '.STORAGE_FORMAT')
       , (select id from persons where user_id ='system'));


--------------------------------------------------------------------------
--  Purpose:  Insert an entry for OBSERVABLE_TYPE UNKNOWN
--------------------------------------------------------------------------

insert into observable_types
(id
,code
,description)
values 
(nextval('OBSERVABLE_TYPE_ID_SEQ')
,'UNKNOWN'
,'Unknown'
);

----------------------------------------------------------------------
--  Purpose:  Insert an entry for PROCEDURE_TYPE UNKNOWN
-----------------------------------------------------------------------

insert into procedure_types
(id
,code
,description)
values 
(nextval('PROCEDURE_TYPE_ID_SEQ')
,'UNKNOWN'
,'Unknown'
);


---------------------------------------------------------------------------------------------------
--  Purpose:  Populate external_data.cvte_id_stor_fmt with a default value.
---------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION polulate_external_data_cvte_id_stor_fmt() RETURNS integer AS $$
DECLARE
    rec RECORD;
    dfl_stor_fmt_code VARCHAR := 'PROPRIETARY';
    dfl_stor_fmt_id TECH_ID;
BEGIN
    select cvte.id into dfl_stor_fmt_id from controlled_vocabulary_terms cvte, controlled_vocabularies cv 
        where cvte.code = dfl_stor_fmt_code and cvte.covo_id = cv.id and cv.code = '.STORAGE_FORMAT'; 
    update external_data set cvte_id_stor_fmt = dfl_stor_fmt_id;
    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

SELECT polulate_external_data_cvte_id_stor_fmt();
DROP FUNCTION polulate_external_data_cvte_id_stor_fmt();

-- Now that the field is populated, create the not null constraint for it.

ALTER TABLE EXTERNAL_DATA ALTER COLUMN CVTE_ID_STOR_FMT SET NOT NULL;

---------------------------------------------------------------------------------------------------
--  Purpose:  Add and populate columns DATA_PRODUCER_CODE and PRODUCTION_TIMESTAMP to table DATA.
---------------------------------------------------------------------------------------------------

ALTER TABLE DATA ADD COLUMN DATA_PRODUCER_CODE CODE;
ALTER TABLE DATA ADD COLUMN PRODUCTION_TIMESTAMP TIME_STAMP;

CREATE OR REPLACE FUNCTION populate_data_producer_code_and_production_timestamp() RETURNS integer AS $$
DECLARE
    rec RECORD;
    loty_rel_code VARCHAR := 'RELATIVE_LOCATION';
    loty_rel_id TECH_ID;
    meas_ts TIMESTAMP;
    ds_code CODE;
    meas_ts_str TEXT;
BEGIN
    select id into loty_rel_id from locator_types where code = loty_rel_code; 
    FOR rec IN SELECT d.id, ed.location FROM data d, external_data ed where d.id = ed.data_id and ed.loty_id = loty_rel_id LOOP
        meas_ts_str = substring(rec.location from '^.*/([0-9]{14})_[^_]+_(.*)$');
        ds_code := substring(rec.location from '^.*/[0-9]{14}_([^_]+)_(.*)$');
        IF meas_ts_str is not null and ds_code is not null THEN
            meas_ts := to_timestamp(meas_ts_str, 'YYYYMMDDHH24MISS');
            update data set production_timestamp=meas_ts, data_producer_code=ds_code where id=rec.id;
        END IF;
    END LOOP;
    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

SELECT populate_data_producer_code_and_production_timestamp();
DROP FUNCTION populate_data_producer_code_and_production_timestamp();

-- Rename columns

ALTER TABLE DATA RENAME COLUMN PROC_ID_ACQUIRED_BY to PROC_ID_PRODUCED_BY;

---------------------------------------------------------------------------------------------------
--  Purpose:  Populate table DATA_SET_RELATIONSHIPS, by using heuristics.
---------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION abs_interval(ts1 time_stamp_dfl, ts2 time_stamp_dfl) returns interval AS $$
BEGIN
    IF ts1 < ts2 THEN
        RETURN ts2 - ts1;
    ELSE
        RETURN ts1 - ts2;
    END IF;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION populate_data_set_relationships() RETURNS INTEGER AS $$
DECLARE
    source_id TECH_ID;
    rec_derived RECORD;
    rec_acquired RECORD;
    rel_location TEXT;
    data_set_code TEXT;
    data_set_code_trial TEXT;
    loty_rel_code TEXT := 'RELATIVE_LOCATION';
    loty_rel_id TECH_ID;
    count INTEGER := 0;
BEGIN
    select id into loty_rel_id from locator_types where code = loty_rel_code;
    FOR rec_derived IN select id, samp_id_derived_from, registration_timestamp from data where samp_id_derived_from is not null LOOP
        source_id := NULL;
        select location into rel_location from external_data where data_id = rec_derived.id and loty_id = loty_rel_id;
        IF rel_location is not null then
            data_set_code := substring(rel_location from '^.*/([0-9]{14}_[^_]+)_.*$');
            IF data_set_code is not null THEN
                FOR rec_acquired IN select id, samp_id_acquired_from from data where samp_id_acquired_from = rec_derived.samp_id_derived_from LOOP
                    select location into rel_location from external_data where data_id = rec_acquired.id and loty_id = loty_rel_id;
                    IF rel_location is not null THEN
                        data_set_code_trial := substring(rel_location from '^.*/([0-9]{14}_[^_]+)_.*$');
                        IF data_set_code_trial = data_set_code THEN
                            source_id = rec_acquired.id;
                            EXIT;
                        END IF;
                    END IF;
                END LOOP;
            END IF;
        END IF;
        -- If the location didn't tell us about data set relationships, try the backup strategy.
        IF source_id is null THEN
            -- Use the measured data set as source that is closest in registration timestamp.
            select id into source_id from data where samp_id_acquired_from = rec_derived.samp_id_derived_from 
                and abs_interval(registration_timestamp, rec_derived.registration_timestamp) 
                    = (select min(abs_interval(registration_timestamp, rec_derived.registration_timestamp)) 
                        from data where samp_id_acquired_from = rec_derived.samp_id_derived_from);
        END IF;
        IF source_id is not null THEN
            insert into data_set_relationships (id, data_id_parent, data_id_child)
                values (nextval('DATA_SET_RELATIONSHIP_ID_SEQ'), source_id, rec_derived.id);
            count := count + 1;
        END IF;
    END LOOP;
    RETURN count;
END;
$$ LANGUAGE 'plpgsql';

SELECT populate_data_set_relationships();
DROP FUNCTION populate_data_set_relationships();
DROP FUNCTION abs_interval(time_stamp_dfl, time_stamp_dfl);

---------------------------------------------------------------------------------------------------
--  Purpose:  Add column IS_MANAGED_INTERNALLY to table PROPERTY_TYPES. Internally managed 
--  properties can not be assigned by users of openBIS, but only by the system. 
---------------------------------------------------------------------------------------------------

ALTER TABLE PROPERTY_TYPES ADD COLUMN IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F';

-- Update already existing property type PLATE_GEOMETRY
update property_types set is_managed_internally = true, code = '.' || code where code = 'PLATE_GEOMETRY';

-- Update already existing controlled vocabulary PLATE_GEOMETRY 
update controlled_vocabularies set code = '.' || code where code = 'PLATE_GEOMETRY';

---------------------------------------------------------------------------------------------------
--  Purpose:  Add unique constraint on column LABEL. 
---------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION make_prty_labels_unique() RETURNS INTEGER AS $$
DECLARE
    prev_key TEXT := NULL;
    this_key TEXT;
    dup_count INTEGER;
    cnt INTEGER := 0;
    already_exists INTEGER;
    new_label TEXT;
    this_id TECH_ID;
BEGIN
    FOR this_id, this_key IN select id, label from property_types order by label,id LOOP
        IF this_key = prev_key THEN
            dup_count := dup_count + 1;
            cnt := cnt + 1;
        ELSE
            dup_count := 0;
        END IF;
        IF dup_count > 0 THEN
            -- Check whether the new key already exists.
            LOOP
                new_label := this_key || '(' || dup_count::text || ')';
                select count(*) into already_exists from property_types where label = new_label;
                IF already_exists = 0 THEN
                    EXIT;
                END IF;
                dup_count := dup_count + 1;
            END LOOP;
            update property_types set label = new_label where id = this_id;
        END IF;
        prev_key := this_key;
    END LOOP;
    RETURN cnt;
END;
$$ LANGUAGE 'plpgsql';

SELECT make_prty_labels_unique();
DROP FUNCTION make_prty_labels_unique();

ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK_LBL UNIQUE(LABEL);

-----------------------------------------------------------------------------------
--  Purpose:  Add Controlled Vocabulary Terms to PLATE_GEOMETRY
-----------------------------------------------------------------------------------

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer)
values  (nextval('CVTE_ID_SEQ')
       , '96_WELLS_8X12'
       , (select id from controlled_vocabularies where code = '.PLATE_GEOMETRY')
       , (select id from persons where user_id = 'system'));

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer)
values  (nextval('CVTE_ID_SEQ')
       , '1536_WELLS_32X48'
       , (select id from controlled_vocabularies where code = '.PLATE_GEOMETRY')
       , (select id from persons where user_id = 'system'));


-----------------------------------------------------------------------------------
--  Purpose:  Rename Observable Types: IMAGE -> HCS_IMAGE, IMAGE_ANALYSIS_DATA -> HCS_IMAGE_ANALYSIS_DATA
--  and descriptions accordingly
-----------------------------------------------------------------------------------

update observable_types set code = 'HCS_IMAGE', description = 'Data derived from analysis of HCS images' where code = 'IMAGE';
update observable_types set code = 'HCS_IMAGE_ANALYSIS_DATA', description = 'Data derived from analysis of HCS images' where code = 'IMAGE_ANALYSIS_DATA';


-----------------------------------------------------------------------------------
--  Purpose: Make the assignments of property types DESCRIPTION, NUCLEOTIDE_SEQUENCE, OFFSET 
--  and GENE_SYMBOL to material types internally managed. 
-----------------------------------------------------------------------------------

update material_type_property_types set is_managed_internally = true
    where prty_id in (select id from property_types where code = 'DESCRIPTION' 
        or code = 'NUCLEOTIDE_SEQUENCE' or code = 'OFFSET' or code = 'GENE_SYMBOL');

-----------------------------------------------------------------------------------
--  Purpose: Add column IS_DATA_ACQUISITION BOOLEAN_CHAR NOT NULL DEFAULT 'F' to table PROCEDURE_TYPES.
-----------------------------------------------------------------------------------

ALTER TABLE PROCEDURE_TYPES ADD COLUMN IS_DATA_ACQUISITION BOOLEAN_CHAR NOT NULL DEFAULT 'F';

-- Set the new attribute for procedure type DATA_ACQUISITION
update procedure_types set is_data_acquisition = true where code = 'DATA_ACQUISITION';

-----------------------------------------------------------------------------------
--  Purpose: Convert all codes to upper case. 
-----------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION column_to_uppercase_fix_duplicates(tbl TEXT, clm TEXT) RETURNS INTEGER AS $$
DECLARE
    prev_key TEXT := NULL;
    this_key TEXT;
    dup_count INTEGER;
    count INTEGER := 0;
    already_exists INTEGER;
    new_clm TEXT;
BEGIN
    FOR this_key IN EXECUTE 'select ' || clm || ' from ' || tbl 
        || ' order by upper(' || clm || '),id' LOOP
        IF upper(this_key) = upper(prev_key) THEN
            dup_count := dup_count + 1;
            count := count + 1;
        ELSE
            dup_count := 0;
        END IF;
        IF dup_count > 0 THEN
            -- Check whether the new key already exists.
            LOOP
                new_clm := this_key || '(' || dup_count::text || ')';
                EXECUTE 'select count(*) from ' || tbl 
                    || ' where upper(' || clm || ') = ' || quote_literal(upper(new_clm)) 
                    into already_exists;
                IF already_exists = 0 THEN
                    EXIT;
                END IF;
                dup_count := dup_count + 1;
            END LOOP;
            EXECUTE 'update ' || tbl || ' set ' || clm || ' = ' || quote_literal(new_clm) || 
                ' where ' || clm || ' = ' || quote_literal(this_key);
        END IF;
        prev_key := this_key;
    END LOOP;
    EXECUTE 'update ' || tbl || ' set ' || clm || ' = upper(' || clm || ')';
    RETURN count;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION column_to_uppercase_fix_duplicates(tbl TEXT, clm TEXT, clm2 TEXT) RETURNS INTEGER AS $$
DECLARE
    prev_key TEXT := NULL;
    prev_key2 TECH_ID;
    this_key TEXT;
    this_key2 TECH_ID;
    dup_count INTEGER;
    count INTEGER := 0;
    already_exists INTEGER;
    new_clm TEXT;
BEGIN
    FOR this_key, this_key2 IN EXECUTE 'select ' || clm || ',' || clm2 || ' from ' || tbl 
        || ' order by ' || clm2 || ',upper(' ||  clm || '),id' LOOP
        IF upper(this_key) = upper(prev_key) and this_key2 = prev_key2 THEN
            dup_count := dup_count + 1;
            count := count + 1;
        ELSE
            dup_count := 0;
        END IF;
        IF dup_count > 0 THEN
            -- Check whether the new key already exists.
            LOOP
                new_clm := this_key || '(' || dup_count::text || ')';
                EXECUTE 'select count(*) from ' || tbl 
                    || ' where upper(' || clm || ') = ' || quote_literal(upper(new_clm)) 
                    || ' and ' || clm2 || ' = ' || quote_literal(this_key2) 
                    into already_exists;
                IF already_exists = 0 THEN
                    EXIT;
                END IF;
                dup_count := dup_count + 1;
            END LOOP;
            EXECUTE 'update ' || tbl || ' set ' || clm || ' = ' || quote_literal(new_clm) || 
                ' where ' || clm || ' = ' || quote_literal(this_key) || ' and ' 
                || clm2 || ' = ' || quote_literal(this_key2);
        END IF;
        prev_key := this_key;
        prev_key2 := this_key2;
    END LOOP;
    EXECUTE 'update ' || tbl || ' set ' || clm || ' = upper(' || clm || ')';
    RETURN count;
END;
$$ LANGUAGE 'plpgsql';


SELECT column_to_uppercase_fix_duplicates('samples', 'code');
SELECT column_to_uppercase_fix_duplicates('property_types', 'code');
SELECT column_to_uppercase_fix_duplicates('controlled_vocabularies', 'code');

SELECT column_to_uppercase_fix_duplicates('projects', 'code', 'grou_id');
SELECT column_to_uppercase_fix_duplicates('experiments', 'code', 'proj_id');
SELECT column_to_uppercase_fix_duplicates('controlled_vocabulary_terms', 'code', 'covo_id');
SELECT column_to_uppercase_fix_duplicates('materials', 'code', 'maty_id');
SELECT column_to_uppercase_fix_duplicates('material_batches', 'code', 'mate_id');
SELECT column_to_uppercase_fix_duplicates('sample_components', 'code', 'samp_id');

-- These will only need changes if someone entered them through the 'back door' directly on the server.

SELECT column_to_uppercase_fix_duplicates('groups', 'code');
SELECT column_to_uppercase_fix_duplicates('sample_types', 'code');
SELECT column_to_uppercase_fix_duplicates('material_types', 'code');
SELECT column_to_uppercase_fix_duplicates('experiment_types', 'code');
SELECT column_to_uppercase_fix_duplicates('procedure_types', 'code');
SELECT column_to_uppercase_fix_duplicates('observable_types', 'code');
SELECT column_to_uppercase_fix_duplicates('locator_types', 'code');
SELECT column_to_uppercase_fix_duplicates('file_format_types', 'code');
SELECT column_to_uppercase_fix_duplicates('data_types', 'code');

DROP FUNCTION column_to_uppercase_fix_duplicates(TEXT, TEXT);
DROP FUNCTION column_to_uppercase_fix_duplicates(TEXT, TEXT, TEXT);

-----------------------------------------------------------------------------------
--  Purpose:  Delete sample_components with no associated material_batches.
--  This deletes superfluous controls of master plates
-----------------------------------------------------------------------------------

delete from sample_components
where  id in (select sc.id 
              from   sample_components as sc left join sample_component_materials as scm 
              on     scm.saco_id = sc.id 
              where  scm.saco_id is null
              )
;
