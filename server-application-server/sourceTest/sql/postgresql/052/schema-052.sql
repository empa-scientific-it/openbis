SET statement_timeout = 0;
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET search_path = public, pg_catalog;
CREATE DOMAIN archiving_status AS character varying(100)
	CONSTRAINT archiving_status_check CHECK (((VALUE)::text = ANY (ARRAY[('LOCKED'::character varying)::text, ('AVAILABLE'::character varying)::text, ('ARCHIVED'::character varying)::text, ('ARCHIVE_PENDING'::character varying)::text, ('UNARCHIVE_PENDING'::character varying)::text])));
CREATE DOMAIN authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY (ARRAY[('ADMIN'::character varying)::text, ('POWER_USER'::character varying)::text, ('USER'::character varying)::text, ('OBSERVER'::character varying)::text, ('ETL_SERVER'::character varying)::text])));
CREATE DOMAIN boolean_char AS boolean DEFAULT false;
CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));
CREATE DOMAIN code AS character varying(60);
CREATE DOMAIN column_label AS character varying(128);
CREATE DOMAIN data_store_service_kind AS character varying(40)
	CONSTRAINT data_store_service_kind_check CHECK (((VALUE)::text = ANY (ARRAY[('PROCESSING'::character varying)::text, ('QUERIES'::character varying)::text])));
CREATE DOMAIN description_2000 AS character varying(2000);
CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY (ARRAY[('DELETION'::character varying)::text, ('INVALIDATION'::character varying)::text, ('MOVEMENT'::character varying)::text])));
CREATE DOMAIN file AS bytea;
CREATE DOMAIN file_name AS character varying(100);
CREATE DOMAIN generic_value AS character varying(1024);
CREATE DOMAIN grid_expression AS character varying(2000);
CREATE DOMAIN grid_id AS character varying(200);
CREATE DOMAIN object_name AS character varying(50);
CREATE DOMAIN ordinal_int AS bigint
	CONSTRAINT ordinal_int_check CHECK ((VALUE > 0));
CREATE DOMAIN query_type AS character varying(40)
	CONSTRAINT query_type_check CHECK (((VALUE)::text = ANY ((ARRAY['GENERIC'::character varying, 'EXPERIMENT'::character varying, 'SAMPLE'::character varying, 'DATA_SET'::character varying, 'MATERIAL'::character varying])::text[])));
CREATE DOMAIN real_value AS real;
CREATE DOMAIN tech_id AS bigint;
CREATE DOMAIN time_stamp AS timestamp with time zone;
CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();
CREATE DOMAIN title_100 AS character varying(100);
CREATE DOMAIN user_id AS character varying(50);
CREATE FUNCTION check_dataset_relationships_on_data_table_modification() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	counter	INTEGER;
BEGIN
	-- if there is a connection with a Sample there should not be any connection with a parent Data Set
	IF (NEW.samp_id IS NOT NULL) THEN
		-- count number of parents
		SELECT count(*) INTO counter 
			FROM data_set_relationships 
			WHERE data_id_child = NEW.id;
		IF (counter > 0) THEN
			RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected with a Sample and a parent Data Set at the same time.', NEW.code;
		END IF;
	END IF;
  RETURN NEW;
END;
$$;
CREATE FUNCTION check_dataset_relationships_on_relationships_table_modification() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	counter	INTEGER;
	sample_id	TECH_ID;
	data_code	CODE;
BEGIN
	-- child will have a parent added so it should not be connected with any sample
	SELECT samp_id, code INTO sample_id, data_code 
		FROM data 
		WHERE id = NEW.data_id_child;
	IF (sample_id IS NOT NULL) THEN
		RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected to a Sample and to a parent Data Set at the same time.', data_code;
	END IF;
	RETURN NEW;
END;
$$;
CREATE FUNCTION controlled_vocabulary_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_code  CODE;
BEGIN
   select code into v_code from data_types where id = NEW.daty_id;
   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;
   RETURN NEW;
END;
$$;
CREATE FUNCTION data_set_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from data_set_type_property_types dstpt, property_types pt 
			 where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;
CREATE FUNCTION experiment_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from experiment_type_property_types etpt, property_types pt 
			 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;
CREATE FUNCTION external_data_storage_format_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN
   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;
   RETURN NEW;
END;
$$;
CREATE FUNCTION material_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from material_type_property_types etpt, property_types pt 
			 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
							 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;
CREATE FUNCTION rename_sequence(old_name character varying, new_name character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);
  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;
  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;
  RETURN CURR_SEQ_VAL;
END;
$$;
CREATE FUNCTION sample_code_uniqueness_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   counter  INTEGER;
BEGIN
    LOCK TABLE samples IN EXCLUSIVE MODE;
    IF (NEW.samp_id_part_of is NULL) THEN
        IF (NEW.dbin_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
            END IF;
        ELSIF (NEW.grou_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;
            END IF;
        END IF;
        ELSE
        IF (NEW.dbin_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;
            END IF;
        ELSIF (NEW.grou_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;
            END IF;
        END IF;
        END IF;   
   RETURN NEW;
END;
$$;
CREATE FUNCTION sample_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from sample_type_property_types etpt, property_types pt 
			 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;
CREATE SEQUENCE attachment_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('attachment_content_id_seq', 7, true);
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE attachment_contents (
    id tech_id NOT NULL,
    value file NOT NULL
);
CREATE SEQUENCE attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('attachment_id_seq', 7, true);
CREATE TABLE attachments (
    id tech_id NOT NULL,
    expe_id tech_id,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    exac_id tech_id NOT NULL,
    samp_id tech_id,
    proj_id tech_id,
    title title_100,
    description description_2000,
    CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))))
);
CREATE SEQUENCE authorization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('authorization_group_id_seq', 1, false);
CREATE TABLE authorization_group_persons (
    ag_id tech_id NOT NULL,
    pers_id tech_id NOT NULL
);
CREATE TABLE authorization_groups (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);
CREATE SEQUENCE code_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('code_seq', 1, false);
CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_chosen_from_list boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);
CREATE SEQUENCE controlled_vocabulary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 5, true);
CREATE TABLE controlled_vocabulary_terms (
    id tech_id NOT NULL,
    code object_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    covo_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    label column_label,
    description description_2000,
    ordinal ordinal_int NOT NULL,
    CONSTRAINT cvte_ck CHECK (((ordinal)::bigint > 0))
);
CREATE SEQUENCE cvte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('cvte_id_seq', 15, true);
CREATE TABLE data (
    id tech_id NOT NULL,
    code code,
    dsty_id tech_id NOT NULL,
    data_producer_code code,
    production_timestamp time_stamp,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_placeholder boolean_char DEFAULT false,
    is_valid boolean_char DEFAULT true,
    modification_timestamp time_stamp DEFAULT now(),
    expe_id tech_id NOT NULL,
    dast_id tech_id NOT NULL,
    is_derived boolean_char NOT NULL,
    samp_id tech_id
);
CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_id_seq', 12, true);
CREATE TABLE data_set_properties (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT dspr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);
CREATE SEQUENCE data_set_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_set_property_id_seq', 13, true);
CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);
CREATE TABLE data_set_relationships (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL
);
CREATE SEQUENCE data_set_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_set_type_id_seq', 3, true);
CREATE TABLE data_set_type_property_types (
    id tech_id NOT NULL,
    dsty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL
);
CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    main_ds_pattern character varying(300),
    main_ds_path character varying(1000)
);
CREATE SEQUENCE data_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_store_id_seq', 1, true);
CREATE TABLE data_store_service_data_set_types (
    data_store_service_id tech_id NOT NULL,
    data_set_type_id tech_id NOT NULL
);
CREATE TABLE data_store_services (
    id tech_id NOT NULL,
    key character varying(256) NOT NULL,
    label character varying(256) NOT NULL,
    kind data_store_service_kind NOT NULL,
    data_store_id tech_id NOT NULL
);
CREATE SEQUENCE data_store_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_store_services_id_seq', 1, false);
CREATE TABLE data_stores (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_archiver_configured boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE data_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_type_id_seq', 9, true);
CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000 NOT NULL
);
CREATE SEQUENCE database_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('database_instance_id_seq', 1, true);
CREATE TABLE database_instances (
    id tech_id NOT NULL,
    code code NOT NULL,
    uuid code NOT NULL,
    is_original_source boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);
CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);
CREATE SEQUENCE dstpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('dstpt_id_seq', 4, true);
CREATE SEQUENCE etpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('etpt_id_seq', 7, true);
CREATE SEQUENCE event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('event_id_seq', 1, false);
CREATE TABLE events (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    description description_2000,
    reason description_2000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    entity_type character varying(80) NOT NULL,
    identifier character varying(250) NOT NULL,
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY (ARRAY[('ATTACHMENT'::character varying)::text, ('DATASET'::character varying)::text, ('EXPERIMENT'::character varying)::text, ('GROUP'::character varying)::text, ('MATERIAL'::character varying)::text, ('PROJECT'::character varying)::text, ('PROPERTY_TYPE'::character varying)::text, ('SAMPLE'::character varying)::text, ('VOCABULARY'::character varying)::text, ('AUTHORIZATION_GROUP'::character varying)::text])))
);
CREATE SEQUENCE experiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_id_seq', 20, true);
CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    mate_prop_id tech_id,
    CONSTRAINT expr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);
CREATE SEQUENCE experiment_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_property_id_seq', 19, true);
CREATE SEQUENCE experiment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_type_id_seq', 2, true);
CREATE TABLE experiment_type_property_types (
    id tech_id NOT NULL,
    exty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL
);
CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);
CREATE TABLE experiments (
    id tech_id NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    mate_id_study_object tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    proj_id tech_id NOT NULL,
    inva_id tech_id,
    is_public boolean_char DEFAULT false NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    perm_id code NOT NULL
);
CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    location character varying(1024) NOT NULL,
    ffty_id tech_id NOT NULL,
    loty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store tech_id,
    status archiving_status DEFAULT 'AVAILABLE'::character varying NOT NULL
);
CREATE SEQUENCE file_format_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('file_format_type_id_seq', 8, true);
CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL
);
CREATE SEQUENCE filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('filter_id_seq', 1, false);
CREATE TABLE filters (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression character varying(2000) NOT NULL,
    is_public boolean NOT NULL,
    grid_id character varying(200) NOT NULL
);
CREATE TABLE grid_custom_columns (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code character varying(200) NOT NULL,
    label column_label NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression grid_expression NOT NULL,
    is_public boolean NOT NULL,
    grid_id grid_id NOT NULL
);
CREATE SEQUENCE grid_custom_columns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('grid_custom_columns_id_seq', 1, false);
CREATE SEQUENCE group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('group_id_seq', 2, true);
CREATE TABLE groups (
    id tech_id NOT NULL,
    code code NOT NULL,
    dbin_id tech_id NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE SEQUENCE invalidation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('invalidation_id_seq', 4, true);
CREATE TABLE invalidations (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_2000
);
CREATE SEQUENCE locator_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('locator_type_id_seq', 1, true);
CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000
);
CREATE SEQUENCE material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_id_seq', 3734, true);
CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value generic_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    modification_timestamp time_stamp DEFAULT now(),
    mate_prop_id tech_id,
    CONSTRAINT mapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);
CREATE SEQUENCE material_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_property_id_seq', 9321, true);
CREATE SEQUENCE material_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_type_id_seq', 7, true);
CREATE TABLE material_type_property_types (
    id tech_id NOT NULL,
    maty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL
);
CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);
CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);
CREATE SEQUENCE mtpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('mtpt_id_seq', 22, true);
CREATE SEQUENCE perm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('perm_id_seq', 1035, true);
CREATE SEQUENCE person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('person_id_seq', 4, true);
CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id user_id NOT NULL,
    email object_name,
    dbin_id tech_id NOT NULL,
    grou_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id,
    display_settings file
);
CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('project_id_seq', 4, true);
CREATE TABLE projects (
    id tech_id NOT NULL,
    code code NOT NULL,
    grou_id tech_id NOT NULL,
    pers_id_leader tech_id,
    description description_2000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);
CREATE SEQUENCE property_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('property_type_id_seq', 26, true);
CREATE TABLE property_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000 NOT NULL,
    label column_label NOT NULL,
    daty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    covo_id tech_id,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL,
    maty_prop_id tech_id
);
CREATE TABLE queries (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression character varying(2000) NOT NULL,
    is_public boolean NOT NULL,
    query_type query_type NOT NULL,
    db_key code DEFAULT '1'::character varying NOT NULL
);
CREATE SEQUENCE query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('query_id_seq', 1, false);
CREATE SEQUENCE role_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('role_assignment_id_seq', 7, true);
CREATE TABLE role_assignments (
    id tech_id NOT NULL,
    role_code authorization_role NOT NULL,
    grou_id tech_id,
    dbin_id tech_id,
    pers_id_grantee tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    ag_id_grantee tech_id,
    CONSTRAINT roas_ag_pers_arc_ck CHECK ((((ag_id_grantee IS NOT NULL) AND (pers_id_grantee IS NULL)) OR ((ag_id_grantee IS NULL) AND (pers_id_grantee IS NOT NULL)))),
    CONSTRAINT roas_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);
CREATE SEQUENCE sample_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_id_seq', 1044, true);
CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    mate_prop_id tech_id,
    CONSTRAINT sapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);
CREATE SEQUENCE sample_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_property_id_seq', 39, true);
CREATE SEQUENCE sample_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_type_id_seq', 6, true);
CREATE TABLE sample_type_property_types (
    id tech_id NOT NULL,
    saty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_displayed boolean_char DEFAULT true NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL
);
CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    is_listable boolean_char DEFAULT true NOT NULL,
    generated_from_depth integer DEFAULT 0 NOT NULL,
    part_of_depth integer DEFAULT 0 NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_auto_generated_code boolean_char DEFAULT false NOT NULL,
    generated_code_prefix code DEFAULT 'S'::character varying NOT NULL
);
CREATE TABLE samples (
    id tech_id NOT NULL,
    code code NOT NULL,
    samp_id_top tech_id,
    samp_id_generated_from tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    inva_id tech_id,
    samp_id_control_layout tech_id,
    dbin_id tech_id,
    grou_id tech_id,
    samp_id_part_of tech_id,
    modification_timestamp time_stamp DEFAULT now(),
    expe_id tech_id,
    perm_id code NOT NULL,
    CONSTRAINT samp_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);
CREATE SEQUENCE stpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('stpt_id_seq', 12, true);

