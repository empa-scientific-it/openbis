--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

--
-- Name: authorization_role; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY ((ARRAY['ADMIN'::character varying, 'POWER_USER'::character varying, 'USER'::character varying, 'OBSERVER'::character varying, 'ETL_SERVER'::character varying])::text[])));


--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char AS boolean DEFAULT false;


--
-- Name: boolean_char_or_unknown; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(40);


--
-- Name: column_label; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN column_label AS character varying(40);


--
-- Name: data_store_service_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN data_store_service_kind AS character varying(40)
	CONSTRAINT data_store_service_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['PROCESSING'::character varying, 'QUERIES'::character varying])::text[])));


--
-- Name: description_1000; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_1000 AS character varying(1000);


--
-- Name: event_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DELETION'::character varying, 'INVALIDATION'::character varying, 'MOVEMENT'::character varying])::text[])));


--
-- Name: file; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file AS bytea;


--
-- Name: file_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_name AS character varying(100);


--
-- Name: generic_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN generic_value AS character varying(1024);


--
-- Name: object_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN object_name AS character varying(50);


--
-- Name: real_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN real_value AS real;


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp AS timestamp with time zone;


--
-- Name: time_stamp_dfl; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();


--
-- Name: title_100; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN title_100 AS character varying(100);


--
-- Name: user_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN user_id AS character varying(50);


--
-- Name: check_dataset_relationships_on_data_table_modification(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION check_dataset_relationships_on_data_table_modification() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: check_dataset_relationships_on_relationships_table_modification(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION check_dataset_relationships_on_relationships_table_modification() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: controlled_vocabulary_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION controlled_vocabulary_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: data_set_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION data_set_property_with_material_data_type_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: experiment_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION experiment_property_with_material_data_type_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: external_data_storage_format_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION external_data_storage_format_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: material_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION material_property_with_material_data_type_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: rename_sequence(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION rename_sequence(old_name character varying, new_name character varying) RETURNS integer
    AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);
  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;
  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;
  RETURN CURR_SEQ_VAL;
END;
$$
    LANGUAGE plpgsql;


--
-- Name: sample_code_uniqueness_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_code_uniqueness_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: sample_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_property_with_material_data_type_check() RETURNS trigger
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
$$
    LANGUAGE plpgsql;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_content_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_content_id_seq', 3, true);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: attachment_contents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachment_contents (
    id tech_id NOT NULL,
    value file NOT NULL
);


--
-- Name: attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_id_seq', 3, true);


--
-- Name: attachments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachments (
    id tech_id NOT NULL,
    expe_id tech_id,
    exac_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    samp_id tech_id,
    proj_id tech_id,
    title title_100,
    description description_1000,
    CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))))
);


--
-- Name: authorization_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE authorization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: authorization_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('authorization_group_id_seq', 1, false);


--
-- Name: authorization_group_persons; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE authorization_group_persons (
    ag_id tech_id NOT NULL,
    pers_id tech_id NOT NULL
);


--
-- Name: authorization_groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE authorization_groups (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE code_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('code_seq', 1, false);


--
-- Name: controlled_vocabularies; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_chosen_from_list boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE controlled_vocabulary_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 2, true);


--
-- Name: controlled_vocabulary_terms; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabulary_terms (
    id tech_id NOT NULL,
    code object_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    covo_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    label column_label,
    description description_1000
);


--
-- Name: cvte_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cvte_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: cvte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cvte_id_seq', 5, true);


--
-- Name: data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_id_seq', 1, true);


--
-- Name: data_set_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: data_set_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_set_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_property_id_seq', 1, false);


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);


--
-- Name: data_set_relationships; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_relationships (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL
);


--
-- Name: data_set_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_set_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_type_id_seq', 10, true);


--
-- Name: data_set_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_type_property_types (
    id tech_id NOT NULL,
    dsty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: data_store_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_store_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_id_seq', 1, true);


--
-- Name: data_store_service_data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_store_service_data_set_types (
    data_store_service_id tech_id NOT NULL,
    data_set_type_id tech_id NOT NULL
);


--
-- Name: data_store_services; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_store_services (
    id tech_id NOT NULL,
    key character varying(256) NOT NULL,
    label character varying(256) NOT NULL,
    kind data_store_service_kind NOT NULL,
    data_store_id tech_id NOT NULL
);


--
-- Name: data_store_services_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_store_services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_services_id_seq', 1, false);


--
-- Name: data_stores; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_stores (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: data_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_type_id_seq', 9, true);


--
-- Name: data_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000 NOT NULL
);


--
-- Name: database_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE database_instance_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: database_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('database_instance_id_seq', 1, true);


--
-- Name: database_instances; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_instances (
    id tech_id NOT NULL,
    code code NOT NULL,
    uuid code NOT NULL,
    is_original_source boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: database_version_logs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);


--
-- Name: dstpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dstpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: dstpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('dstpt_id_seq', 10, true);


--
-- Name: etpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE etpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: etpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('etpt_id_seq', 3, true);


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('event_id_seq', 1, true);


--
-- Name: events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE events (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    description description_1000,
    reason description_1000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    entity_type character varying(80) NOT NULL,
    identifier character varying(250) NOT NULL,
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY ((ARRAY['ATTACHMENT'::character varying, 'DATASET'::character varying, 'EXPERIMENT'::character varying, 'GROUP'::character varying, 'MATERIAL'::character varying, 'PROJECT'::character varying, 'PROPERTY_TYPE'::character varying, 'SAMPLE'::character varying, 'VOCABULARY'::character varying, 'AUTHORIZATION_GROUP'::character varying])::text[])))
);


--
-- Name: experiment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_id_seq', 2, true);


--
-- Name: experiment_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: experiment_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_property_id_seq', 2, true);


--
-- Name: experiment_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_type_id_seq', 3, true);


--
-- Name: experiment_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_type_property_types (
    id tech_id NOT NULL,
    exty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: experiment_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: experiments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: external_data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    location character varying(1024) NOT NULL,
    ffty_id tech_id NOT NULL,
    loty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store tech_id
);


--
-- Name: file_format_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE file_format_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: file_format_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('file_format_type_id_seq', 12, true);


--
-- Name: file_format_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    dbin_id tech_id NOT NULL
);


--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('filter_id_seq', 1, false);


--
-- Name: filters; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE filters (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_1000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression character varying(2000) NOT NULL,
    is_public boolean NOT NULL,
    grid_id character varying(200) NOT NULL
);


--
-- Name: group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE group_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('group_id_seq', 2, true);


--
-- Name: groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE groups (
    id tech_id NOT NULL,
    code code NOT NULL,
    dbin_id tech_id NOT NULL,
    description description_1000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: invalidation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE invalidation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: invalidation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('invalidation_id_seq', 1, false);


--
-- Name: invalidations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE invalidations (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_1000
);


--
-- Name: locator_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE locator_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: locator_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('locator_type_id_seq', 1, true);


--
-- Name: locator_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000
);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_id_seq', 19, true);


--
-- Name: material_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: material_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_property_id_seq', 17, true);


--
-- Name: material_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_type_id_seq', 7, true);


--
-- Name: material_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_type_property_types (
    id tech_id NOT NULL,
    maty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: material_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: materials; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: mtpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mtpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: mtpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('mtpt_id_seq', 10, true);


--
-- Name: perm_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE perm_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: perm_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('perm_id_seq', 21, true);


--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE person_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('person_id_seq', 4, true);


--
-- Name: persons; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE project_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('project_id_seq', 2, true);


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projects (
    id tech_id NOT NULL,
    code code NOT NULL,
    grou_id tech_id NOT NULL,
    pers_id_leader tech_id,
    description description_1000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: property_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE property_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: property_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('property_type_id_seq', 15, true);


--
-- Name: property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE property_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000 NOT NULL,
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


--
-- Name: role_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE role_assignment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: role_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('role_assignment_id_seq', 6, true);


--
-- Name: role_assignments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: sample_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sample_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_id_seq', 19, true);


--
-- Name: sample_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: sample_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sample_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_property_id_seq', 17, true);


--
-- Name: sample_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sample_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_type_id_seq', 7, true);


--
-- Name: sample_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_type_property_types (
    id tech_id NOT NULL,
    saty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_displayed boolean_char DEFAULT true NOT NULL
);


--
-- Name: sample_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_1000,
    dbin_id tech_id NOT NULL,
    is_listable boolean_char DEFAULT true NOT NULL,
    generated_from_depth integer DEFAULT 0 NOT NULL,
    part_of_depth integer DEFAULT 0 NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: samples; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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


--
-- Name: stpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE stpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: stpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('stpt_id_seq', 6, true);


--
-- Data for Name: attachment_contents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachment_contents (id, value) FROM stdin;
1	processing-dir
2	Processing parameters from file '/local0/home/ci/cruisecontrol-bin-2.8.1/projects/cisd/trunk/integration-tests/targets/playground/openBIS-client/testdata/register-experiments/processing-parameters.txt'.
3	answer=42
\.


--
-- Data for Name: attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachments (id, expe_id, exac_id, file_name, registration_timestamp, version, pers_id_registerer, samp_id, proj_id, title, description) FROM stdin;
1	1	1	$processing-path-for-DATA_ACQUISITION	2009-02-10 15:41:04.90922+01	1	2	\N	\N	\N	\N
2	1	2	$processing-description-for-DATA_ACQUISITION	2009-02-10 15:41:04.90922+01	1	2	\N	\N	\N	\N
3	1	3	$processing-parameters-for-DATA_ACQUISITION	2009-02-10 15:41:04.90922+01	1	2	\N	\N	\N	\N
\.


--
-- Data for Name: authorization_group_persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY authorization_group_persons (ag_id, pers_id) FROM stdin;
\.


--
-- Data for Name: authorization_groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY authorization_groups (id, dbin_id, code, description, registration_timestamp, pers_id_registerer, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
1	PLATE_GEOMETRY	The geometry or dimensions of a plate	2009-02-10 15:40:42.184979+01	1	t	t	1	2009-09-24 08:48:34.031951+02	t	\N
2	STORAGE_FORMAT	The on-disk storage format of a data set	2009-02-10 15:40:42.184979+01	1	t	t	1	2009-09-24 08:48:34.031951+02	t	\N
\.


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer, label, description) FROM stdin;
1	96_WELLS_8X12	2009-02-10 15:40:42.184979+01	1	1	\N	\N
2	384_WELLS_16X24	2009-02-10 15:40:42.184979+01	1	1	\N	\N
3	1536_WELLS_32X48	2009-02-10 15:40:42.184979+01	1	1	\N	\N
4	PROPRIETARY	2009-02-10 15:40:42.184979+01	2	1	\N	\N
5	BDS_DIRECTORY	2009-02-10 15:40:42.184979+01	2	1	\N	\N
\.


--
-- Data for Name: data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data (id, code, dsty_id, data_producer_code, production_timestamp, registration_timestamp, is_placeholder, is_valid, modification_timestamp, expe_id, dast_id, is_derived, samp_id) FROM stdin;
\.


--
-- Data for Name: data_set_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_properties (id, ds_id, dstpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_relationships; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_relationships (data_id_parent, data_id_child) FROM stdin;
\.


--
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp) FROM stdin;
1	5	14	f	f	2	2009-09-24 08:55:51.972508+02
2	6	14	f	f	2	2009-09-24 08:56:00.388098+02
3	4	14	f	f	2	2009-09-24 08:56:05.75486+02
4	5	15	f	f	2	2009-09-24 09:00:19.318628+02
5	6	15	f	f	2	2009-09-24 09:00:27.060328+02
6	4	15	f	f	2	2009-09-24 09:00:44.585318+02
7	8	14	f	f	2	2009-09-24 14:35:34.286394+02
8	9	14	f	f	2	2009-09-24 14:35:40.595117+02
9	7	14	f	f	2	2009-09-24 14:35:46.299784+02
10	1	14	f	f	2	2009-09-25 08:57:45.057285+02
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	UNKNOWN	Unknown	1	2009-09-24 08:48:34.031951+02
2	HCS_IMAGE	High Content Screening Image	1	2009-09-24 08:48:34.031951+02
3	HCS_IMAGE_ANALYSIS_DATA	Data derived from analysis of HCS images	1	2009-09-24 08:48:34.031951+02
4	MZXML	yeastx test	1	2009-09-24 08:53:33.943924+02
5	EICML	yeastx test	1	2009-09-24 08:53:53.20956+02
6	FIAML	yeastx test	1	2009-09-24 08:54:04.772222+02
7	ZIP	\N	1	2009-09-24 09:02:16.938028+02
8	MAT	\N	1	2009-09-24 09:02:22.234529+02
9	PDF	\N	1	2009-09-24 09:02:27.698168+02
\.


--
-- Data for Name: data_store_service_data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_store_service_data_set_types (data_store_service_id, data_set_type_id) FROM stdin;
\.


--
-- Data for Name: data_store_services; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_store_services (id, key, label, kind, data_store_id) FROM stdin;
\.


--
-- Data for Name: data_stores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_stores (id, dbin_id, code, download_url, registration_timestamp, remote_url, session_token, modification_timestamp) FROM stdin;
1	1	STANDARD		2009-02-10 15:40:42.184979+01			2009-09-24 08:48:34.488603+02
\.


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_types (id, code, description) FROM stdin;
1	VARCHAR	Variable length character
2	INTEGER	Integer
3	REAL	Real number, i.e. an inexact, variable-precision numeric type
4	BOOLEAN	An enumerated type with values True and False
5	TIMESTAMP	Both date and time. Format: yyyy-mm-dd hh:mm:ss
6	CONTROLLEDVOCABULARY	Controlled Vocabulary
7	MATERIAL	Reference to a material
8	HYPERLINK	Address of a web page
9	MULTILINE_VARCHAR	Long text
\.


--
-- Data for Name: database_instances; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_instances (id, code, uuid, is_original_source, registration_timestamp) FROM stdin;
1	CISD	E96C8910-596A-409D-BDA4-BBD3FE6629A7	t	2009-02-10 15:40:42.184979+01
\.


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
028	./sql/generic/028/schema-028.sql	SUCCESS	2009-02-10 15:40:42.141	-- D:\\\\DDL\\\\postgresql\\\\schema-023.sql\\012--\\012-- Generated for ANSI SQL92 on Fri Jul 04  15:13:22 2008 by Server Generator 10.1.2.6.18\\012------------------------------------------------------------------------------------\\012--\\012--  Post-Generation Modifications:\\012--\\012--  1. Changed domain FILE from BIT(32000) to BYTEA\\012--  2. Changed domain TECH_ID from NUMERIC(20) to BIGINT\\012--  3. Changed domain BOOLEAN_CHAR from CHAR(1) DEFAULT F to BOOLEAN DEFAULT FALSE\\012--  4. Removed the check constraints to handle boolean values in Oracle for the\\012--     tables MATERIAL_TYPE_PROPERTY_TYPES, EXPERIMENT_TYPE_PROPERTY_TYPES and\\012--     SAMPLE_TYPE_PROPERTY_TYPES (AVCON_%)\\012--  5. Added the ON DELETE CASCADE qualifier to the foreign keys MAPR_MTPT_FK,\\012--     EXPR_ETPT_FK and SAPR_STPT_FK\\012--  6. Add the check constraint directly on the domain BOOLEAN_CHAR_OR_UNKNOWN\\012--     CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE in ('F', 'T', 'U')) DEFAULT 'U';\\012--  7. Add the WITH TIMEZONE qualifier to the domain TIME_STAMP\\012--     CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;\\012--  8. Add the WITH TIMEZONE and NOT NULL qualifiers to the domain TIME_STAMP_DFL\\012--     CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;\\012--  9. Extend the domain EVENT_TYPE by adding the CHECK constraint\\012--     CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE in ('DELETION', 'INVALIDATION', 'MOVEMENT'));\\012--  10. Extend the domain AUTHORIZATION_ROLE by adding the CHECK constraint\\012--     CREATE DOMAIN AUTHORIZATION_ROLE as VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012--  11. Added the Sequence and Index sections\\012--  12. Added DATABASE_INSTANCES.GLOBAL_CODE column for UUID\\012--  13. DATABASE_INSTANCES.GLOBAL_CODE renamed to DATABASE_INSTANCES.UUID\\012--  14. OBSERVABLE_TYPES renamed to DATA_SET_TYPES\\012--  15. OBSERVABLE_TYPE_ID_SEQ renamed to DATA_SET_TYPE_ID_SEQ\\012--  16. DATA.OBTY_ID renamed to DATA.DSTY_ID;\\012--  17. some others - the source model should be updated to make these Post-Generation Modifications minimal \\012------------------------------------------------------------------------------------\\012\\012-- Creating domains\\012\\012CREATE DOMAIN AUTHORIZATION_ROLE AS VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;\\012CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE IN ('F', 'T', 'U')) DEFAULT 'U';\\012CREATE DOMAIN CODE AS VARCHAR(40);\\012CREATE DOMAIN COLUMN_LABEL AS VARCHAR(40);\\012CREATE DOMAIN DESCRIPTION_1000 AS VARCHAR(1000);\\012CREATE DOMAIN DESCRIPTION_250 AS VARCHAR(250);\\012CREATE DOMAIN DESCRIPTION_80 AS VARCHAR(80);\\012CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DELETION', 'INVALIDATION', 'MOVEMENT'));\\012CREATE DOMAIN FILE AS BYTEA;\\012CREATE DOMAIN FILE_NAME AS VARCHAR(100);\\012CREATE DOMAIN GENERIC_VALUE AS VARCHAR(1024);\\012CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);\\012CREATE DOMAIN REAL_VALUE AS REAL;\\012CREATE DOMAIN TECH_ID AS BIGINT;\\012CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;\\012CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;\\012CREATE DOMAIN USER_ID AS VARCHAR(50);\\012\\012-- Creating tables\\012\\012CREATE TABLE CONTROLLED_VOCABULARIES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE CONTROLLED_VOCABULARY_TERMS (ID TECH_ID NOT NULL,CODE OBJECT_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,COVO_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE DATA (ID TECH_ID NOT NULL,CODE CODE,DSTY_ID TECH_ID NOT NULL,PROC_ID_PRODUCED_BY TECH_ID NOT NULL,DATA_PRODUCER_CODE CODE,PRODUCTION_TIMESTAMP TIME_STAMP,SAMP_ID_ACQUIRED_FROM TECH_ID,SAMP_ID_DERIVED_FROM TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,IS_PLACEHOLDER BOOLEAN_CHAR DEFAULT 'F',IS_DELETED BOOLEAN_CHAR DEFAULT 'F',IS_VALID BOOLEAN_CHAR DEFAULT 'T');\\012CREATE TABLE DATABASE_INSTANCES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,UUID CODE NOT NULL,IS_ORIGINAL_SOURCE BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DAST_ID TECH_ID);\\012CREATE TABLE DATA_SET_RELATIONSHIPS (DATA_ID_PARENT TECH_ID NOT NULL,DATA_ID_CHILD TECH_ID NOT NULL);\\012CREATE TABLE DATA_STORES (ID TECH_ID NOT NULL,DBIN_ID TECH_ID NOT NULL,CODE CODE NOT NULL,DOWNLOAD_URL VARCHAR(1024) NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE DATA_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL);\\012CREATE TABLE EVENTS (ID TECH_ID NOT NULL,EVENT_TYPE EVENT_TYPE NOT NULL,DESCRIPTION DESCRIPTION_250,DATA_ID TECH_ID,REASON DESCRIPTION_250,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,EXTY_ID TECH_ID NOT NULL,MATE_ID_STUDY_OBJECT TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PROJ_ID TECH_ID NOT NULL,INVA_ID TECH_ID,IS_PUBLIC BOOLEAN_CHAR NOT NULL DEFAULT 'F',DAST_ID TECH_ID);\\012CREATE TABLE EXPERIMENT_ATTACHMENTS (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,EXAC_ID TECH_ID NOT NULL,FILE_NAME FILE_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,VERSION INTEGER NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE EXPERIMENT_ATTACHMENT_CONTENTS (ID TECH_ID NOT NULL,VALUE FILE NOT NULL);\\012CREATE TABLE EXPERIMENT_PROPERTIES (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,ETPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE EXPERIMENT_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,EXTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXTERNAL_DATA (DATA_ID TECH_ID NOT NULL,LOCATION VARCHAR(1024) NOT NULL,FFTY_ID TECH_ID NOT NULL,LOTY_ID TECH_ID NOT NULL,CVTE_ID_STOR_FMT TECH_ID NOT NULL,IS_COMPLETE BOOLEAN_CHAR_OR_UNKNOWN NOT NULL DEFAULT 'U',CVTE_ID_STORE TECH_ID);\\012CREATE TABLE FILE_FORMAT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE GROUPS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DBIN_ID TECH_ID NOT NULL,GROU_ID_PARENT TECH_ID,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_250,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,DAST_ID TECH_ID);\\012CREATE TABLE INVALIDATIONS (ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,REASON DESCRIPTION_250);\\012CREATE TABLE LOCATOR_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80);\\012CREATE TABLE MATERIALS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,MATY_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,MATE_ID_INHIBITOR_OF TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_BATCHES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,AMOUNT REAL_VALUE,MATE_ID TECH_ID NOT NULL,PROC_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_PROPERTIES (ID TECH_ID NOT NULL,MATE_ID TECH_ID NOT NULL,MTPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,CVTE_ID TECH_ID);\\012CREATE TABLE MATERIAL_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,MATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE DATA_SET_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE PERSONS (ID TECH_ID NOT NULL,FIRST_NAME VARCHAR(30),LAST_NAME VARCHAR(30),USER_ID USER_ID NOT NULL,EMAIL OBJECT_NAME,DBIN_ID TECH_ID NOT NULL,GROU_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID);\\012CREATE TABLE PROCEDURES (ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,EXPE_ID TECH_ID NOT NULL,PCTY_ID TECH_ID NOT NULL);\\012CREATE TABLE PROCEDURE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,IS_DATA_ACQUISITION BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE PROJECTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,GROU_ID TECH_ID NOT NULL,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_1000,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DAST_ID TECH_ID);\\012CREATE TABLE PROPERTY_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,LABEL COLUMN_LABEL NOT NULL,DATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,COVO_ID TECH_ID,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE ROLE_ASSIGNMENTS (ID TECH_ID NOT NULL,ROLE_CODE AUTHORIZATION_ROLE NOT NULL,GROU_ID TECH_ID,DBIN_ID TECH_ID,PERS_ID_GRANTEE TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,SAMP_ID_TOP TECH_ID,SAMP_ID_GENERATED_FROM TECH_ID,SATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,INVA_ID TECH_ID,SAMP_ID_CONTROL_LAYOUT TECH_ID,DBIN_ID TECH_ID,GROU_ID TECH_ID,SAMP_ID_PART_OF TECH_ID);\\012CREATE TABLE SAMPLE_INPUTS (SAMP_ID TECH_ID NOT NULL,PROC_ID TECH_ID NOT NULL);\\012CREATE TABLE SAMPLE_MATERIAL_BATCHES (SAMP_ID TECH_ID NOT NULL,MABA_ID TECH_ID NOT NULL);\\012CREATE TABLE SAMPLE_PROPERTIES (ID TECH_ID NOT NULL,SAMP_ID TECH_ID NOT NULL,STPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL, IS_LISTABLE BOOLEAN_CHAR NOT NULL DEFAULT 'T', GENERATED_FROM_DEPTH INTEGER NOT NULL DEFAULT 0, PART_OF_DEPTH INTEGER NOT NULL DEFAULT 0);\\012CREATE TABLE SAMPLE_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,SATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, IS_DISPLAYED BOOLEAN_CHAR NOT NULL DEFAULT 'T');\\012\\012-- Creating sequences\\012\\012CREATE SEQUENCE CONTROLLED_VOCABULARY_ID_SEQ;\\012CREATE SEQUENCE CVTE_ID_SEQ;\\012CREATE SEQUENCE DATABASE_INSTANCE_ID_SEQ;\\012CREATE SEQUENCE DATA_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;\\012CREATE SEQUENCE DATA_STORE_ID_SEQ;\\012CREATE SEQUENCE DATA_TYPE_ID_SEQ;\\012CREATE SEQUENCE ETPT_ID_SEQ;\\012CREATE SEQUENCE EVENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ATTACHMENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ATTACHMENT_CONTENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_TYPE_ID_SEQ;\\012CREATE SEQUENCE FILE_FORMAT_TYPE_ID_SEQ;\\012CREATE SEQUENCE GROUP_ID_SEQ;\\012CREATE SEQUENCE INVALIDATION_ID_SEQ;\\012CREATE SEQUENCE LOCATOR_TYPE_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_BATCH_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_TYPE_ID_SEQ;\\012CREATE SEQUENCE MTPT_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_TYPE_ID_SEQ;\\012CREATE SEQUENCE PERSON_ID_SEQ;\\012CREATE SEQUENCE PROCEDURE_ID_SEQ;\\012CREATE SEQUENCE PROCEDURE_TYPE_ID_SEQ;\\012CREATE SEQUENCE PROJECT_ID_SEQ;\\012CREATE SEQUENCE PROPERTY_TYPE_ID_SEQ;\\012CREATE SEQUENCE ROLE_ASSIGNMENT_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_TYPE_ID_SEQ;\\012CREATE SEQUENCE STPT_ID_SEQ;\\012\\012-- Creating primary key constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PK PRIMARY KEY(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_PK PRIMARY KEY(ID);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_PK PRIMARY KEY(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENT_CONTENTS ADD CONSTRAINT EXAC_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_PK PRIMARY KEY(DATA_ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_PK PRIMARY KEY(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PK PRIMARY KEY(ID);\\012ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PK PRIMARY KEY(ID);\\012ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_PK PRIMARY KEY(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_PK PRIMARY KEY(ID);\\012ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_PK PRIMARY KEY(ID);\\012ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_PK PRIMARY KEY(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PK PRIMARY KEY(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PK PRIMARY KEY(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PK PRIMARY KEY(PROC_ID,SAMP_ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_PK PRIMARY KEY(SAMP_ID,MABA_ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PK PRIMARY KEY(ID);\\012\\012-- Creating unique constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_BK_UK UNIQUE(CODE,COVO_ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_BK_UK UNIQUE(CODE);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_BK_UK UNIQUE(CODE);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_UUID_UK UNIQUE(UUID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_BK_UK UNIQUE(DATA_ID_CHILD,DATA_ID_PARENT);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_BK_UK UNIQUE(CODE);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_BK_UK UNIQUE(EVENT_TYPE,DATA_ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_BK_UK UNIQUE(CODE,PROJ_ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_BK_UK UNIQUE(EXPE_ID,FILE_NAME,VERSION);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_BK_UK UNIQUE(EXPE_ID,ETPT_ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_BK_UK UNIQUE(EXTY_ID,PRTY_ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_BK_UK UNIQUE(LOCATION,LOTY_ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_BK_UK UNIQUE(CODE);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_BK_UK UNIQUE(CODE,MATY_ID,DBIN_ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_BK_UK UNIQUE(CODE,MATE_ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_BK_UK UNIQUE(MATE_ID,MTPT_ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_BK_UK UNIQUE(MATY_ID,PRTY_ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_BK_UK UNIQUE(DBIN_ID,USER_ID);\\012ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_BK_UK UNIQUE(CODE,GROU_ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROUP_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,GROU_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_INSTANCE_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,DBIN_ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_BK_UK UNIQUE(SAMP_ID,PROC_ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_BK_UK UNIQUE(MABA_ID,SAMP_ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_BK_UK UNIQUE(SAMP_ID,STPT_ID);\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_BK_UK UNIQUE(SATY_ID,PRTY_ID);\\012\\012-- Creating foreign key constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_OBTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_PROC_PRODUCED_BY_FK FOREIGN KEY (PROC_ID_PRODUCED_BY) REFERENCES PROCEDURES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK_ACQUIRED_FROM FOREIGN KEY (SAMP_ID_ACQUIRED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK_DERIVED_FROM FOREIGN KEY (SAMP_ID_DERIVED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_CHILD FOREIGN KEY (DATA_ID_CHILD) REFERENCES DATA(ID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_PARENT FOREIGN KEY (DATA_ID_PARENT) REFERENCES DATA(ID);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_MATE_FK FOREIGN KEY (MATE_ID_STUDY_OBJECT) REFERENCES MATERIALS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PROJ_FK FOREIGN KEY (PROJ_ID) REFERENCES PROJECTS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_CONT_FK FOREIGN KEY (EXAC_ID) REFERENCES EXPERIMENT_ATTACHMENT_CONTENTS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_ETPT_FK FOREIGN KEY (ETPT_ID) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_FK FOREIGN KEY (CVTE_ID_STOR_FMT) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_STORED_ON_FK FOREIGN KEY (CVTE_ID_STORE) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_FFTY_FK FOREIGN KEY (FFTY_ID) REFERENCES FILE_FORMAT_TYPES(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_LOTY_FK FOREIGN KEY (LOTY_ID) REFERENCES LOCATOR_TYPES(ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_GROU_FK FOREIGN KEY (GROU_ID_PARENT) REFERENCES GROUPS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATE_FK FOREIGN KEY (MATE_ID_INHIBITOR_OF) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PROC_FK FOREIGN KEY (PROC_ID) REFERENCES PROCEDURES(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MTPT_FK FOREIGN KEY (MTPT_ID) REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_PCTY_FK FOREIGN KEY (PCTY_ID) REFERENCES PROCEDURE_TYPES(ID);\\012ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DATY_FK FOREIGN KEY (DATY_ID) REFERENCES DATA_TYPES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_GRANTEE FOREIGN KEY (PERS_ID_GRANTEE) REFERENCES PERSONS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_CONTROL_LAYOUT FOREIGN KEY (SAMP_ID_CONTROL_LAYOUT) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_GENERATED_FROM FOREIGN KEY (SAMP_ID_GENERATED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_PART_OF FOREIGN KEY (SAMP_ID_PART_OF) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_TOP FOREIGN KEY (SAMP_ID_TOP) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PROC_FK FOREIGN KEY (PROC_ID) REFERENCES PROCEDURES(ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_MABA_FK FOREIGN KEY (MABA_ID) REFERENCES MATERIAL_BATCHES(ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_STPT_FK FOREIGN KEY (STPT_ID) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);\\012\\012-- Creating check constraints\\012\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_ARC_CK CHECK ((SAMP_ID_ACQUIRED_FROM IS NOT NULL AND SAMP_ID_DERIVED_FROM IS NULL) OR (SAMP_ID_ACQUIRED_FROM IS NULL AND SAMP_ID_DERIVED_FROM IS NOT NULL));\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));\\012\\012-- Creating indices\\012\\012CREATE INDEX COVO_PERS_FK_I ON CONTROLLED_VOCABULARIES (PERS_ID_REGISTERER);\\012CREATE INDEX CVTE_COVO_FK_I ON CONTROLLED_VOCABULARY_TERMS (COVO_ID);\\012CREATE INDEX CVTE_PERS_FK_I ON CONTROLLED_VOCABULARY_TERMS (PERS_ID_REGISTERER);\\012CREATE INDEX DATA_OBTY_FK_I ON DATA (DSTY_ID);\\012CREATE INDEX DATA_PROC_FK_I ON DATA (PROC_ID_PRODUCED_BY);\\012CREATE INDEX DATA_SAMP_FK_I_ACQUIRED_FROM ON DATA (SAMP_ID_ACQUIRED_FROM);\\012CREATE INDEX DATA_SAMP_FK_I_DERIVED_FROM ON DATA (SAMP_ID_DERIVED_FROM);\\012CREATE INDEX DAST_DBIN_FK_I ON DATA_STORES (DBIN_ID);\\012CREATE INDEX DSRE_DATA_FK_I_CHILD ON DATA_SET_RELATIONSHIPS (DATA_ID_CHILD);\\012CREATE INDEX DSRE_DATA_FK_I_PARENT ON DATA_SET_RELATIONSHIPS (DATA_ID_PARENT);\\012CREATE INDEX ETPT_EXTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (EXTY_ID);\\012CREATE INDEX ETPT_PERS_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX ETPT_PRTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX EVNT_DATA_FK_I ON EVENTS (DATA_ID);\\012CREATE INDEX EVNT_PERS_FK_I ON EVENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXAT_EXPE_FK_I ON EXPERIMENT_ATTACHMENTS (EXPE_ID);\\012CREATE INDEX EXAT_PERS_FK_I ON EXPERIMENT_ATTACHMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXAT_EXAC_FK_I ON EXPERIMENT_ATTACHMENTS (EXAC_ID);\\012CREATE INDEX EXDA_CVTE_FK_I ON EXTERNAL_DATA (CVTE_ID_STOR_FMT);\\012CREATE INDEX EXDA_CVTE_STORED_ON_FK_I ON EXTERNAL_DATA (CVTE_ID_STORE);\\012CREATE INDEX EXDA_FFTY_FK_I ON EXTERNAL_DATA (FFTY_ID);\\012CREATE INDEX EXDA_LOTY_FK_I ON EXTERNAL_DATA (LOTY_ID);\\012CREATE INDEX EXPE_EXTY_FK_I ON EXPERIMENTS (EXTY_ID);\\012CREATE INDEX EXPE_INVA_FK_I ON EXPERIMENTS (INVA_ID);\\012CREATE INDEX EXPE_MATE_FK_I ON EXPERIMENTS (MATE_ID_STUDY_OBJECT);\\012CREATE INDEX EXPE_PERS_FK_I ON EXPERIMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXPE_PROJ_FK_I ON EXPERIMENTS (PROJ_ID);\\012CREATE INDEX EXPR_CVTE_FK_I ON EXPERIMENT_PROPERTIES (CVTE_ID);\\012CREATE INDEX EXPR_ETPT_FK_I ON EXPERIMENT_PROPERTIES (ETPT_ID);\\012CREATE INDEX EXPR_EXPE_FK_I ON EXPERIMENT_PROPERTIES (EXPE_ID);\\012CREATE INDEX EXPR_PERS_FK_I ON EXPERIMENT_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX GROU_DBIN_FK_I ON GROUPS (DBIN_ID);\\012CREATE INDEX GROU_GROU_FK_I ON GROUPS (GROU_ID_PARENT);\\012CREATE INDEX GROU_PERS_FK_I ON GROUPS (PERS_ID_LEADER);\\012CREATE INDEX GROU_PERS_REGISTERED_BY_FK_I ON GROUPS (PERS_ID_REGISTERER);\\012CREATE INDEX INVA_PERS_FK_I ON INVALIDATIONS (PERS_ID_REGISTERER);\\012CREATE INDEX MABA_MATE_FK_I ON MATERIAL_BATCHES (MATE_ID);\\012CREATE INDEX MABA_PERS_FK_I ON MATERIAL_BATCHES (PERS_ID_REGISTERER);\\012CREATE INDEX MABA_PROC_FK_I ON MATERIAL_BATCHES (PROC_ID);\\012CREATE INDEX MAPR_CVTE_FK_I ON MATERIAL_PROPERTIES (CVTE_ID);\\012CREATE INDEX MAPR_MATE_FK_I ON MATERIAL_PROPERTIES (MATE_ID);\\012CREATE INDEX MAPR_MTPT_FK_I ON MATERIAL_PROPERTIES (MTPT_ID);\\012CREATE INDEX MAPR_PERS_FK_I ON MATERIAL_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX MATE_MATE_FK_I ON MATERIALS (MATE_ID_INHIBITOR_OF);\\012CREATE INDEX MATE_MATY_FK_I ON MATERIALS (MATY_ID);\\012CREATE INDEX MATE_PERS_FK_I ON MATERIALS (PERS_ID_REGISTERER);\\012CREATE INDEX MTPT_MATY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (MATY_ID);\\012CREATE INDEX MTPT_PERS_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX MTPT_PRTY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX PERS_GROU_FK_I ON PERSONS (GROU_ID);\\012CREATE INDEX PROC_EXPE_FK_I ON PROCEDURES (EXPE_ID);\\012CREATE INDEX PROC_PCTY_FK_I ON PROCEDURES (PCTY_ID);\\012CREATE INDEX PROJ_GROU_FK_I ON PROJECTS (GROU_ID);\\012CREATE INDEX PROJ_PERS_FK_I_LEADER ON PROJECTS (PERS_ID_LEADER);\\012CREATE INDEX PROJ_PERS_FK_I_REGISTERER ON PROJECTS (PERS_ID_REGISTERER);\\012CREATE INDEX PRTY_COVO_FK_I ON PROPERTY_TYPES (COVO_ID);\\012CREATE INDEX PRTY_DATY_FK_I ON PROPERTY_TYPES (DATY_ID);\\012CREATE INDEX PRTY_PERS_FK_I ON PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX ROAS_DBIN_FK_I ON ROLE_ASSIGNMENTS (DBIN_ID);\\012CREATE INDEX ROAS_GROU_FK_I ON ROLE_ASSIGNMENTS (GROU_ID);\\012CREATE INDEX ROAS_PERS_FK_I_GRANTEE ON ROLE_ASSIGNMENTS (PERS_ID_GRANTEE);\\012CREATE INDEX ROAS_PERS_FK_I_REGISTERER ON ROLE_ASSIGNMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX SAIN_PROC_FK_I ON SAMPLE_INPUTS (PROC_ID);\\012CREATE INDEX SAIN_SAMP_FK_I ON SAMPLE_INPUTS (SAMP_ID);\\012CREATE INDEX SAMB_MABA_FK_I ON SAMPLE_MATERIAL_BATCHES (MABA_ID);\\012CREATE INDEX SAMB_SAMP_FK_I ON SAMPLE_MATERIAL_BATCHES (SAMP_ID);\\012CREATE INDEX SAMP_INVA_FK_I ON SAMPLES (INVA_ID);\\012CREATE INDEX SAMP_PERS_FK_I ON SAMPLES (PERS_ID_REGISTERER);\\012CREATE INDEX SAMP_SAMP_FK_I_CONTROL_LAYOUT ON SAMPLES (SAMP_ID_CONTROL_LAYOUT);\\012CREATE INDEX SAMP_SAMP_FK_I_GENERATED_FROM ON SAMPLES (SAMP_ID_GENERATED_FROM);\\012CREATE INDEX SAMP_SAMP_FK_I_PART_OF ON SAMPLES (SAMP_ID_PART_OF);\\012CREATE INDEX SAMP_SAMP_FK_I_TOP ON SAMPLES (SAMP_ID_TOP);\\012CREATE INDEX SAMP_CODE_I ON SAMPLES (CODE);\\012CREATE INDEX SAMP_SATY_FK_I ON SAMPLES (SATY_ID);\\012CREATE INDEX SAPR_CVTE_FK_I ON SAMPLE_PROPERTIES (CVTE_ID);\\012CREATE INDEX SAPR_PERS_FK_I ON SAMPLE_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX SAPR_SAMP_FK_I ON SAMPLE_PROPERTIES (SAMP_ID);\\012CREATE INDEX SAPR_STPT_FK_I ON SAMPLE_PROPERTIES (STPT_ID);\\012CREATE INDEX STPT_PERS_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX STPT_PRTY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX STPT_SATY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (SATY_ID);\\012	\N
028	./sql/postgresql/028/function-028.sql	SUCCESS	2009-02-10 15:40:42.168	-- Creating Functions\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create function RENAME_SEQUENCE() that is required for renaming the sequences belonging to tables\\012------------------------------------------------------------------------------------\\012CREATE FUNCTION RENAME_SEQUENCE(OLD_NAME VARCHAR, NEW_NAME VARCHAR) RETURNS INTEGER AS $$\\012DECLARE\\012  CURR_SEQ_VAL   INTEGER;\\012BEGIN\\012  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);\\012  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;\\012  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;\\012  RETURN CURR_SEQ_VAL;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger CONTROLLED_VOCABULARY_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION CONTROLLED_VOCABULARY_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_code  CODE;\\012BEGIN\\012\\012   select code into v_code from data_types where id = NEW.daty_id;\\012\\012   -- Check if the data is of type "CONTROLLEDVOCABULARY"\\012   if v_code = 'CONTROLLEDVOCABULARY' then\\012      if NEW.covo_id IS NULL then\\012         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;\\012      end if;\\012   end if;\\012\\012   RETURN NEW;\\012\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER CONTROLLED_VOCABULARY_CHECK BEFORE INSERT OR UPDATE ON PROPERTY_TYPES\\012    FOR EACH ROW EXECUTE PROCEDURE CONTROLLED_VOCABULARY_CHECK();\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger EXTERNAL_DATA_STORAGE_FORMAT_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_covo_code  CODE;\\012   data_code CODE;\\012BEGIN\\012\\012   select code into v_covo_code from controlled_vocabularies\\012      where is_internal_namespace = true and \\012         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);\\012   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"\\012   if v_covo_code != 'STORAGE_FORMAT' then\\012      select code into data_code from data where id = NEW.data_id; \\012      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;\\012   end if;\\012\\012   RETURN NEW;\\012\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER EXTERNAL_DATA_STORAGE_FORMAT_CHECK BEFORE INSERT OR UPDATE ON EXTERNAL_DATA\\012    FOR EACH ROW EXECUTE PROCEDURE EXTERNAL_DATA_STORAGE_FORMAT_CHECK();\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger SAMPLE_CODE_UNIQUENESS_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   counter  INTEGER;\\012BEGIN\\012\\011IF (NEW.samp_id_part_of is NULL) THEN\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        ELSE\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        END IF;   \\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES\\012    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_CODE_UNIQUENESS_CHECK();\\012	\N
028	./sql/generic/028/data-028.sql	SUCCESS	2009-02-10 15:40:42.214	----------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATABASE_INSTANCES\\012----------------------------------------------------------------------------\\012\\012INSERT INTO database_instances(\\012              id\\012            , code\\012\\011    \\011, uuid\\012            , is_original_source)\\012    VALUES (  nextval('DATABASE_INSTANCE_ID_SEQ')\\012            , 'SYSTEM_DEFAULT'\\012\\011    \\011, 'SYSTEM_DEFAULT'\\012            , 'T');\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_STORES\\012----------------------------------------------------------------------\\012\\012insert into data_stores\\012(id\\012,code\\012,download_url\\012,dbin_id)\\012values\\012(nextval('DATA_STORE_ID_SEQ')\\012,'STANDARD'\\012,''\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012update database_instances set dast_id = (select id from data_stores where code = 'STANDARD');\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PERSONS\\012-----------------------------------------------------------------------\\012\\012insert into persons\\012(id\\012,first_name\\012,last_name\\012,user_id\\012,email\\012,dbin_id)\\012values\\012(nextval('PERSON_ID_SEQ')\\012,''\\012,'System User'\\012,'system'\\012,''\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT') );\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary PLATE_GEOMETRY\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabularies\\012       ( id\\012       , code\\012       , is_internal_namespace\\012       , description\\012       , pers_id_registerer\\012       , is_managed_internally\\012       , dbin_id )\\012values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')\\012       , 'PLATE_GEOMETRY'\\012       , true\\012       , 'The geometry or dimensions of a plate'\\012       , (select id from persons where user_id ='system')\\012       , true\\012       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary Terms for PLATE_GEOMETRY\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer)\\012values  (nextval('CVTE_ID_SEQ')\\012       , '96_WELLS_8X12'\\012       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer)\\012values  (nextval('CVTE_ID_SEQ')\\012       , '384_WELLS_16X24'\\012       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer)\\012values  (nextval('CVTE_ID_SEQ')\\012       , '1536_WELLS_32X48'\\012       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary STORAGE_FORMAT\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabularies \\012       ( id\\012       , code\\012       , is_internal_namespace      \\012       , description\\012       , pers_id_registerer\\012       , is_managed_internally\\012       , dbin_id )\\012values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')\\012       , 'STORAGE_FORMAT'\\012       , true\\012       , 'The on-disk storage format of a data set'\\012       , (select id from persons where user_id ='system')\\012       , true\\012       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary Terms for STORAGE_FORMAT\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer )\\012values  (nextval('CVTE_ID_SEQ')\\012       , 'PROPRIETARY'\\012       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer )\\012values  (nextval('CVTE_ID_SEQ')\\012       , 'BDS_DIRECTORY'\\012       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPES\\012--------------------------------------------------------------------------\\012\\012insert into experiment_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('EXPERIMENT_TYPE_ID_SEQ')\\012,'SIRNA_HCS'\\012,'Small Interfering RNA High Content Screening'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into experiment_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('EXPERIMENT_TYPE_ID_SEQ')\\012,'COMPOUND_HCS'\\012,'Compound High Content Screening'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table MATERIAL_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'VIRUS'\\012,'Virus'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'CELL_LINE'\\012,'Cell Line or Cell Culture. The growing of cells under controlled conditions.'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'GENE'\\012,'Gene'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'OLIGO'\\012,'Oligo nucleotide'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'CONTROL'\\012,'Control of a control layout'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'BACTERIUM'\\012,'Bacterium'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'COMPOUND'\\012,'Compound'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_TYPES\\012------------------------------------------------------------------\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'VARCHAR'\\012,'Variable length character'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'INTEGER'\\012,'Integer'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'REAL'\\012,'Real number, i.e. an inexact, variable-precision numeric type'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'BOOLEAN'\\012,'An enumerated type with values True and False'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'TIMESTAMP'\\012,'Both date and time. Format: yyyy-mm-dd hh:mm:ss'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'CONTROLLEDVOCABULARY'\\012 ,'Controlled Vocabulary'\\012);\\012\\012\\012\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PROPERTY_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'DESCRIPTION'\\012,'A Description'\\012,'Description'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'GENE_SYMBOL'\\012,'Gene Symbol, e.g. BMP15'\\012,'Gene Symbol'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'NUCLEOTIDE_SEQUENCE'\\012,'A sequence of nucleotides'\\012,'Nucleotide Sequence'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'REFSEQ'\\012,'NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein'\\012,'RefSeq'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'OFFSET'\\012,'Offset from the start of the sequence'\\012,'Offset'\\012,(select id from data_types where code ='INTEGER')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create property type PLATE_GEOMETRY\\012-----------------------------------------------------------------------------------\\012insert into property_types\\012(id\\012,code\\012,is_internal_namespace\\012,description\\012,label\\012,daty_id\\012,covo_id\\012,pers_id_registerer\\012, is_managed_internally \\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'PLATE_GEOMETRY'\\012,true\\012,'Plate Geometry'\\012,'Plate Geometry'\\012,(select id from data_types where code ='CONTROLLEDVOCABULARY')\\012,(select id from controlled_vocabularies where code ='PLATE_GEOMETRY' and is_internal_namespace = true)\\012,(select id from persons where user_id ='system')\\012,true\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table SAMPLE_TYPES\\012----------------------------------------------------------------------\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'MASTER_PLATE'\\012,'Master Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,generated_from_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'DILUTION_PLATE'\\012,'Dilution Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,1\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,generated_from_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'CELL_PLATE'\\012,'Cell Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,2\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,generated_from_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'REINFECT_PLATE'\\012,'Re-infection Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,3\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'CONTROL_LAYOUT'\\012,'Control layout'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,is_listable\\012,generated_from_depth\\012,part_of_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'WELL'\\012,'Plate Well'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,'F'\\012,0\\012,1\\012);\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPE_PROPERTY_TYPES\\012------------------------------------------------------------------------------------\\012\\012----\\012-- Note: we rely on DESCRIPTION to be present and internally_managed for all experiment types!\\012----\\012\\012   ----------------------------------\\012   --  Experiment Type SIRNA_HCS\\012   ----------------------------------\\012\\012insert into experiment_type_property_types\\012(   id\\012   ,exty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('ETPT_ID_SEQ')\\012   ,(select id from experiment_types where code = 'SIRNA_HCS')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   ----------------------------------\\012   --  Experiment Type COMPOUND_HCS\\012   ----------------------------------\\012\\012insert into experiment_type_property_types\\012(   id\\012   ,exty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('ETPT_ID_SEQ')\\012   ,(select id from experiment_types where code = 'COMPOUND_HCS')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table SAMPLE_TYPE_PROPERTY_TYPES\\012------------------------------------------------------------------------------------\\012\\012   ---------------------------------\\012   --  Sample Type   MASTER_PLATE\\012   --  Property Type PLATE_GEOMETRY   \\012   ---------------------------------\\012\\012insert into sample_type_property_types\\012(   id\\012   ,saty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('STPT_ID_SEQ')\\012   ,(select id from sample_types where code = 'MASTER_PLATE')\\012   ,(select id from property_types where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   ---------------------------------\\012   --  Sample Type   CONTROL_LAYOUT\\012   --  Property Type PLATE_GEOMETRY   \\012   ---------------------------------\\012\\012insert into sample_type_property_types\\012(   id\\012   ,saty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('STPT_ID_SEQ')\\012   ,(select id from sample_types where code = 'CONTROL_LAYOUT')\\012   ,(select id from property_types where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table MATERIAL_TYPE_PROPERTY_TYPES\\012------------------------------------------------------------------------------------\\012\\012----\\012-- Note: we rely on DESCRIPTION to be present and internally_managed for all material types!\\012----\\012\\012   -----------------------\\012   --  Material Type VIRUS\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'VIRUS')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -----------------------\\012   --  Material Type BACTERIUM\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'BACTERIUM')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -----------------------\\012   --  Material Type COMPOUND\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'COMPOUND')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -----------------------\\012   --  Material Type GENE\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'GENE')\\012   ,(select id from property_types where code = 'GENE_SYMBOL' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'GENE')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012   -----------------------\\012   --  Material Type OLIGO\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'OLIGO')\\012   ,(select id from property_types where code = 'NUCLEOTIDE_SEQUENCE' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'OLIGO')\\012   ,(select id from property_types where code = 'OFFSET' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'OLIGO')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -------------------------\\012   --  Material Type CONTROL\\012   -------------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'CONTROL')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PROCEDURE_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into procedure_types\\012(id\\012,code\\012,description\\012,is_data_acquisition\\012,dbin_id)\\012values \\012(nextval('PROCEDURE_TYPE_ID_SEQ')\\012,'UNKNOWN'\\012,'Unknown'\\012,false\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into procedure_types\\012(id\\012,code\\012,description\\012,is_data_acquisition\\012,dbin_id)\\012values \\012(nextval('PROCEDURE_TYPE_ID_SEQ')\\012,'DATA_ACQUISITION'\\012,'Data Acquisition'\\012,true\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into procedure_types\\012(id\\012,code\\012,description\\012,is_data_acquisition\\012,dbin_id)\\012values \\012(nextval('PROCEDURE_TYPE_ID_SEQ')\\012,'IMAGE_ANALYSIS'\\012,'Image Analysis'\\012,false\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012--------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_SET_TYPES\\012--------------------------------------------------------------------------\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'UNKNOWN'\\012,'Unknown'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'HCS_IMAGE'\\012,'High Content Screening Image'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'HCS_IMAGE_ANALYSIS_DATA'\\012,'Data derived from analysis of HCS images'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table FILE_FORMAT_TYPES\\012-------------------------------------------------------------------------\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'TIFF'\\012,'TIFF File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'3VPROPRIETARY'\\012,'Data Analysis 3V proprietary format'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'PLKPROPRIETARY'\\012,'Data Analysis Pelkmans group proprietary format'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012---------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table LOCATOR_TYPES\\012---------------------------------------------------------------------\\012\\012insert into locator_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('LOCATOR_TYPE_ID_SEQ')\\012,'RELATIVE_LOCATION'\\012,'Relative Location'\\012);\\012	\N
029	./source//sql/postgresql/migration/migration-028-029.sql	SUCCESS	2009-09-24 08:48:33.956	-- -------\\012-- add modification_timestamp field to allow edition with Hibernate\\012-- -------\\012\\012ALTER TABLE samples\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\012ALTER TABLE experiments\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\012ALTER TABLE materials\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\012ALTER TABLE experiment_properties\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\012ALTER TABLE material_properties\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\012ALTER TABLE sample_properties\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\012-- -------\\012-- add property data type which references materials\\012-- -------\\012\\012ALTER TABLE property_types\\012\\011ADD COLUMN maty_prop_id tech_id;\\012\\012ALTER TABLE property_types\\012\\011ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id);\\012\\012ALTER TABLE experiment_properties\\012\\011ADD COLUMN mate_prop_id tech_id;\\012\\012ALTER TABLE material_properties\\012\\011ADD COLUMN mate_prop_id tech_id;\\012\\012ALTER TABLE sample_properties\\012\\011ADD COLUMN mate_prop_id tech_id;\\012\\012ALTER TABLE experiment_properties\\012\\011ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);\\012\\012ALTER TABLE material_properties\\012\\011ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);\\012\\012ALTER TABLE sample_properties\\012\\011ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'MATERIAL'\\012 ,'Reference to a material'\\012);\\012\\012ALTER TABLE MATERIAL_PROPERTIES DROP CONSTRAINT MAPR_CK;\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012\\012ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_CK;\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012\\012ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT SAPR_CK;\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger MATERIAL/SAMPLE/EXPERIMENT _PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK\\012--            It checks that if material property value is assigned to the entity,\\012--\\011\\011\\011\\011\\011\\011then the material type is equal to the one described by property type.\\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from material_type_property_types etpt, property_types pt \\012\\011\\011\\011 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON material_properties\\012    FOR EACH ROW EXECUTE PROCEDURE MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();\\012    \\012CREATE OR REPLACE FUNCTION SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from sample_type_property_types etpt, property_types pt \\012\\011\\011\\011 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON sample_properties\\012    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();\\012    \\012CREATE OR REPLACE FUNCTION EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from experiment_type_property_types etpt, property_types pt \\012\\011\\011\\011 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON experiment_properties\\012    FOR EACH ROW EXECUTE PROCEDURE EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();\\012       \\012-- -------\\012-- remove the reminescents of the OBSERVABLE_TYPE\\012-- -------\\012\\012ALTER TABLE data\\012\\011DROP CONSTRAINT data_obty_fk;\\012\\012ALTER TABLE data_set_types\\012\\011DROP CONSTRAINT obty_pk;\\012\\012ALTER TABLE data_set_types\\012\\011ADD CONSTRAINT dsty_pk PRIMARY KEY (id);\\012\\012ALTER TABLE data\\012\\011ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);\\012\\012ALTER TABLE data_set_types\\012\\011DROP CONSTRAINT obty_bk_uk;\\012\\012ALTER TABLE data_set_types\\012\\011DROP CONSTRAINT obty_dbin_fk;\\012\\012ALTER TABLE data_set_types\\012\\011ADD CONSTRAINT dsty_bk_uk UNIQUE (code, dbin_id);\\012\\012ALTER TABLE data_set_types\\012\\011ADD CONSTRAINT dsty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);\\012\\012DROP INDEX data_obty_fk_i;\\012\\012CREATE INDEX data_dsty_fk_i ON data(dsty_id);\\012	\N
030	./source//sql/postgresql/migration/migration-029-030.sql	SUCCESS	2009-09-24 08:48:34.24	-- -------\\012-- add modification_timestamp field to allow edition with Hibernate\\012-- -------\\012ALTER TABLE data\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012ALTER TABLE experiment_types\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012ALTER TABLE sample_types\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012ALTER TABLE material_types\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012ALTER TABLE data_set_types\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012ALTER TABLE controlled_vocabularies\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012-- -------\\012-- add properties to data sets\\012-- -------\\012CREATE TABLE DATA_SET_PROPERTIES (ID TECH_ID NOT NULL,DS_ID TECH_ID NOT NULL,DSTPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE DATA_SET_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,DSTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012\\012CREATE SEQUENCE DATA_SET_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE DSTPT_ID_SEQ;\\012\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_PK PRIMARY KEY(ID);\\012\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_BK_UK UNIQUE(DSTY_ID,PRTY_ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_BK_UK UNIQUE(DS_ID,DSTPT_ID);\\012\\012\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_DSTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID);\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_DSTPT_FK FOREIGN KEY (DSTPT_ID) REFERENCES DATA_SET_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_DS_FK FOREIGN KEY (DS_ID) REFERENCES DATA(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);\\012\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012\\012CREATE INDEX DSPR_CVTE_FK_I ON DATA_SET_PROPERTIES (CVTE_ID);\\012CREATE INDEX DSPR_DSTPT_FK_I ON DATA_SET_PROPERTIES (DSTPT_ID);\\012CREATE INDEX DSPR_DS_FK_I ON DATA_SET_PROPERTIES (DS_ID);\\012CREATE INDEX DSPR_PERS_FK_I ON DATA_SET_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX DSTPT_DSTY_FK_I ON DATA_SET_TYPE_PROPERTY_TYPES (DSTY_ID);\\012CREATE INDEX DSTPT_PERS_FK_I ON DATA_SET_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX DSTPT_PRTY_FK_I ON DATA_SET_TYPE_PROPERTY_TYPES (PRTY_ID);\\012\\012CREATE OR REPLACE FUNCTION DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from data_set_type_property_types dstpt, property_types pt \\012\\011\\011\\011 where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON data_set_properties\\012    FOR EACH ROW EXECUTE PROCEDURE DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();   \\012	\N
031	./source//sql/postgresql/migration/migration-030-031.sql	SUCCESS	2009-09-24 08:48:34.447	-- -------\\012-- Change attachment table names, they should no longer mention the experiment\\012-- -------\\012\\012ALTER TABLE experiment_attachment_contents RENAME TO attachment_contents;\\012ALTER TABLE experiment_attachments RENAME TO attachments;\\012\\012ALTER TABLE EXPERIMENT_ATTACHMENT_ID_SEQ RENAME TO ATTACHMENT_ID_SEQ;\\012ALTER TABLE EXPERIMENT_ATTACHMENT_CONTENT_ID_SEQ RENAME TO ATTACHMENT_CONTENT_ID_SEQ;\\012\\012ALTER INDEX EXAT_EXPE_FK_I RENAME TO ATTA_EXPE_FK_I;\\012ALTER INDEX EXAT_PERS_FK_I RENAME TO ATTA_PERS_FK_I;\\012-- Index couldn't be renamed because it might not exist\\012DROP INDEX IF EXISTS EXAT_EXAC_FK_I; \\012CREATE INDEX ATTA_EXAC_FK_I ON ATTACHMENTS (EXAC_ID);\\012\\012-- it's not possible to rename constraints, we drop and create them with different names\\012\\012ALTER TABLE attachments\\012\\011DROP CONSTRAINT exat_cont_fk;\\012ALTER TABLE attachments\\012\\011DROP CONSTRAINT exat_expe_fk;\\012ALTER TABLE attachments\\012\\011DROP CONSTRAINT exat_pers_fk;\\012ALTER TABLE attachments\\012\\011DROP CONSTRAINT exat_pk;\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_pk PRIMARY KEY (id);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);\\012\\012-- -------\\012-- Add an arc connection from attachment to project and sample tables\\012-- -------\\012\\012ALTER TABLE attachments\\012\\011ADD COLUMN samp_id tech_id,\\012\\011ADD COLUMN proj_id tech_id,\\012\\011ALTER COLUMN expe_id DROP NOT NULL;\\012\\011\\012ALTER TABLE attachments\\012\\011DROP CONSTRAINT exat_bk_uk;\\012\\011\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))));\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);\\012\\012ALTER TABLE attachments\\012\\011ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);\\012\\012CREATE INDEX ATTA_SAMP_FK_I ON ATTACHMENTS (SAMP_ID);\\012CREATE INDEX ATTA_PROJ_FK_I ON ATTACHMENTS (PROJ_ID);\\012\\012-- -------\\012-- Add modification timestamp to project table to allow edition\\012-- -------\\012\\012ALTER TABLE projects\\012\\011ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;\\012\\011\\012-- -------\\012-- Add new datatypes\\012-- -------\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'HYPERLINK'\\012 ,'Address of a web page'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'MULTILINE_VARCHAR'\\012 ,'Long text'\\012);\\012\\012-- -------\\012-- Added explicit lock Samples table in exclusive mode to prevent race condition\\012-- -------\\012\\012CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   counter  INTEGER;\\012BEGIN\\012\\011LOCK TABLE samples IN EXCLUSIVE MODE;\\012\\011IF (NEW.samp_id_part_of is NULL) THEN\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        ELSE\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        END IF;   \\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012	\N
032	./source//sql/postgresql/migration/migration-031-032.sql	SUCCESS	2009-09-24 08:48:34.647	-- -------\\012-- Modify MATERIAL_BATCHES\\012-- -------\\012\\012ALTER TABLE material_batches\\012    DROP COLUMN proc_id;\\012\\012-- -------\\012-- Modify SAMPLES\\012-- -------\\012\\012ALTER TABLE samples\\012    ADD COLUMN expe_id tech_id;\\012\\012ALTER TABLE samples\\012    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);\\012\\012CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);\\012\\012-- relink samples directly to experiments\\012\\012UPDATE samples\\012SET expe_id = (SELECT e.id FROM experiments e, procedures p, sample_inputs si \\012               WHERE si.samp_id = samples.id AND si.proc_id = p.id AND p.expe_id = e.id AND e.inva_id IS NULL);\\012\\012\\012-- -------\\012-- Modify DATA\\012-- -------\\012\\012ALTER TABLE data\\012    ADD COLUMN expe_id tech_id;\\012\\012ALTER TABLE data\\012    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);\\012\\012CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);\\012\\012UPDATE data\\012SET expe_id = (SELECT e.id FROM experiments e, procedures p \\012               WHERE data.proc_id_produced_by = p.id AND p.expe_id = e.id);\\012\\012ALTER TABLE data\\012    ALTER COLUMN expe_id SET NOT NULL;\\012\\012ALTER TABLE data\\012    DROP COLUMN proc_id_produced_by;\\012\\012-- -------\\012-- Drop PROCEDURES, PROCEDURE_TYPES, and SAMPLE_INPUTS\\012-- -------\\012\\012DROP TABLE sample_inputs;\\012DROP TABLE procedures;\\012DROP TABLE procedure_types;\\012\\012DROP SEQUENCE procedure_id_seq;\\012DROP SEQUENCE procedure_type_id_seq;\\012\\012-- -------\\012-- Add CODE_SEQ\\012-- -------\\012\\012CREATE SEQUENCE CODE_SEQ;\\012\\012-- -------\\012-- Add new columns to DATA_STORES\\012-- -------\\012\\012ALTER TABLE data_stores\\012    ADD COLUMN remote_url character varying(250),\\012    ADD COLUMN session_token character varying(50),\\012    ADD COLUMN modification_timestamp time_stamp DEFAULT now();\\012\\012UPDATE data_stores\\012SET remote_url = '', session_token='';\\012\\012ALTER TABLE data_stores\\012    ALTER COLUMN remote_url SET NOT NULL,\\012    ALTER COLUMN session_token SET NOT NULL;\\012\\012-- -------\\012-- Drop foreign keys onto DATA_STORES\\012-- -------\\012\\012ALTER TABLE database_instances\\012    DROP COLUMN dast_id;\\012\\012ALTER TABLE groups\\012    DROP COLUMN dast_id;\\012\\012ALTER TABLE projects\\012    DROP COLUMN dast_id;\\012\\012ALTER TABLE experiments\\012    DROP COLUMN dast_id;\\012\\012-- -------\\012-- Add foreign key from DATA onto DATA_STORES\\012-- -------\\012\\012ALTER TABLE data\\012    ADD COLUMN dast_id tech_id;\\012\\012ALTER TABLE data\\012    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);\\012\\012UPDATE data\\012SET dast_id = (select id from data_stores where code = 'STANDARD');\\012\\012ALTER TABLE data\\012    ALTER COLUMN dast_id SET NOT NULL;\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Replace trigger SAMPLE_CODE_UNIQUENESS_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   counter  INTEGER;\\012BEGIN\\012    LOCK TABLE samples IN EXCLUSIVE MODE;\\012    IF (NEW.samp_id_part_of is NULL) THEN\\012        IF (NEW.dbin_id is not NULL) THEN\\012            SELECT count(*) into counter FROM samples \\012                where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;\\012            IF (counter > 0) THEN\\012                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;\\012            END IF;\\012        ELSIF (NEW.grou_id is not NULL) THEN\\012            SELECT count(*) into counter FROM samples \\012                where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;\\012            IF (counter > 0) THEN\\012                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;\\012            END IF;\\012        END IF;\\012        ELSE\\012        IF (NEW.dbin_id is not NULL) THEN\\012            SELECT count(*) into counter FROM samples \\012                where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;\\012            IF (counter > 0) THEN\\012                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;\\012            END IF;\\012        ELSIF (NEW.grou_id is not NULL) THEN\\012            SELECT count(*) into counter FROM samples \\012                where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;\\012            IF (counter > 0) THEN\\012                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;\\012            END IF;\\012        END IF;\\012        END IF;   \\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  allow longer user ids \\012------------------------------------------------------------------------------------\\012\\012alter table persons alter column user_id type varchar(50);\\012drop domain user_id;\\012create domain user_id as varchar(50);\\012alter table persons alter column user_id type user_id;\\012\\012\\012	\N
033	./source//sql/postgresql/migration/migration-032-033.sql	SUCCESS	2009-09-24 08:48:34.791	-- -------\\012-- Modify PERSON\\012-- -------\\012\\012ALTER TABLE persons\\012    ADD COLUMN display_settings file;\\012\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Insert additional initial data set into the table FILE_FORMAT_TYPES\\012-------------------------------------------------------------------------\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'HDF5'\\012,'Hierarchical Data Format File, version 5'\\012,(select id from database_instances where is_original_source = 'T')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'PROPRIETARY'\\012,'Proprietary Format File'\\012,(select id from database_instances where is_original_source = 'T')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'SRF'\\012,'Sequence Read Format File'\\012,(select id from database_instances where is_original_source = 'T')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'TSV'\\012,'Tab Separated Values File'\\012,(select id from database_instances where is_original_source = 'T')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'XML'\\012,'XML File'\\012,(select id from database_instances where is_original_source = 'T')\\012);\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Delete etpt assignments when entity-types are deleted \\012-------------------------------------------------------------------------\\012\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES DROP CONSTRAINT MTPT_MATY_FK;\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES DROP CONSTRAINT STPT_SATY_FK;\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES DROP CONSTRAINT DSTPT_DSTY_FK;\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_DSTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID)  ON DELETE CASCADE;\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES DROP CONSTRAINT ETPT_EXTY_FK;\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID) ON DELETE CASCADE;\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Delete property types when material-types are deleted \\012-------------------------------------------------------------------------\\012ALTER TABLE PROPERTY_TYPES DROP CONSTRAINT PRTY_MATY_FK;\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_MATY_FK FOREIGN KEY (MATY_PROP_ID) REFERENCES MATERIAL_TYPES(ID) ON DELETE CASCADE;\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Delete etpt assignments when property-types are deleted \\012-------------------------------------------------------------------------\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES DROP CONSTRAINT ETPT_PRTY_FK;\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES DROP CONSTRAINT MTPT_PRTY_FK;\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES DROP CONSTRAINT STPT_PRTY_FK;\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES DROP CONSTRAINT DSTPT_PRTY_FK;\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012	\N
034	./source//sql/postgresql/migration/migration-033-034.sql	SUCCESS	2009-09-24 08:48:35.114	------------------------------------------------------------------------------------\\012-- Create sequence for generating permanent identifiers starting with nextval of existing dataset sequence.\\012------------------------------------------------------------------------------------\\012CREATE FUNCTION CREATE_SEQUENCE(EXISTING_SEQUENCE VARCHAR, NEW_SEQUENCE VARCHAR) RETURNS VOID AS $$\\012DECLARE\\012  CURR_SEQ_VAL   INTEGER;\\012BEGIN\\012  SELECT INTO CURR_SEQ_VAL NEXTVAL(EXISTING_SEQUENCE);\\012  EXECUTE 'CREATE SEQUENCE ' || NEW_SEQUENCE || ' START WITH ' || CURR_SEQ_VAL;\\012  RETURN;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012SELECT CREATE_SEQUENCE('DATA_ID_SEQ', 'PERM_ID_SEQ');\\012DROP FUNCTION CREATE_SEQUENCE(VARCHAR, VARCHAR);\\012\\012------------------------------------------------------------------------------------\\012-- Create sequence data_set_relationship_id_seq if it doesn't exist\\012------------------------------------------------------------------------------------\\012\\012create function create_data_set_relationship_id_seq() returns void AS $$\\012begin\\012   perform *\\012     FROM information_schema.sequences WHERE sequence_name='data_set_relationship_id_seq';\\012   if not found\\012   then\\012     CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;\\012   end if;\\012end;\\012$$ language 'plpgsql';\\012select create_data_set_relationship_id_seq();\\012drop function create_data_set_relationship_id_seq();\\012\\012------------------------------------------------------------------------------------\\012-- Add perm_id columns to samples and experiments.\\012------------------------------------------------------------------------------------\\012ALTER TABLE SAMPLES ADD COLUMN PERM_ID CODE;\\012ALTER TABLE EXPERIMENTS ADD COLUMN PERM_ID CODE;\\012\\012UPDATE SAMPLES SET PERM_ID = to_char(registration_timestamp,'YYYYMMDDHHSSMS') || '-' || NEXTVAL('PERM_ID_SEQ');\\012UPDATE EXPERIMENTS SET PERM_ID = to_char(registration_timestamp,'YYYYMMDDHHSSMS') || '-' || NEXTVAL('PERM_ID_SEQ');\\012ALTER TABLE SAMPLES ALTER COLUMN PERM_ID SET NOT NULL;\\012ALTER TABLE EXPERIMENTS ALTER COLUMN PERM_ID SET NOT NULL;\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PI_UK UNIQUE(PERM_ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PI_UK UNIQUE(PERM_ID);\\012\\012------------------------------------------------------------------------------------\\012-- Add column is_chosen_from_list to controlled_vocabularies.\\012------------------------------------------------------------------------------------\\012ALTER TABLE controlled_vocabularies ADD COLUMN is_chosen_from_list BOOLEAN_CHAR NOT NULL DEFAULT TRUE;\\012ALTER TABLE controlled_vocabularies ADD COLUMN source_uri CHARACTER VARYING(250);\\012------------------------------------------------------------------------------------\\012-- Modify Data table - remove arc connection between Sample and Data table, use a flag instead\\012------------------------------------------------------------------------------------\\012\\012-- add new columns\\012\\012ALTER TABLE data\\012    ADD COLUMN is_derived boolean_char;\\012ALTER TABLE data\\012    ADD COLUMN samp_id tech_id;\\012\\012-- update values in new columns\\012\\012UPDATE data\\012    SET is_derived = (samp_id_derived_from IS NOT NULL); \\012UPDATE data\\012    SET samp_id = samp_id_derived_from \\012    WHERE is_derived = 'TRUE';\\012UPDATE data\\012    SET samp_id = samp_id_acquired_from \\012    WHERE is_derived = 'FALSE';\\012\\012-- remove old columns\\012\\012ALTER TABLE data\\012    DROP COLUMN samp_id_acquired_from;\\012ALTER TABLE data\\012    DROP COLUMN samp_id_derived_from;\\012    \\012-- add constraints and indexes to new columns\\012\\012ALTER TABLE data\\012    ALTER COLUMN is_derived SET NOT NULL;\\012ALTER TABLE data\\012    ALTER COLUMN samp_id SET NOT NULL;   \\012\\012ALTER TABLE data\\012    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples (id);\\012CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);\\012\\012------------------------------------------------------------------------------------\\012-- Granting SELECT privilege to group openbis_readonly\\012------------------------------------------------------------------------------------\\012\\012GRANT SELECT ON SEQUENCE attachment_content_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE attachment_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE code_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE cvte_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_set_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_set_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_store_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE database_instance_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE dstpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE etpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE event_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE experiment_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE experiment_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE experiment_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE file_format_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE group_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE invalidation_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE locator_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE material_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE material_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE material_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE mtpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE perm_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE person_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE project_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE property_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE role_assignment_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE sample_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE sample_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE sample_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE stpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE attachment_contents TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE attachments TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE controlled_vocabularies TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE controlled_vocabulary_terms TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_relationships TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_stores TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE database_instances TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE database_version_logs TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE events TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiment_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiment_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiment_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiments TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE external_data TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE file_format_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE groups TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE invalidations TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE locator_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE material_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE material_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE material_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE materials TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE persons TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE projects TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE role_assignments TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE sample_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE sample_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE sample_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE samples TO GROUP OPENBIS_READONLY;\\012\\012------------------------------------------------------------------------------------\\012-- Correct sequencers (fix bug from migration 026-027)\\012------------------------------------------------------------------------------------\\012\\012SELECT setval('material_property_id_seq', max(id)) FROM material_properties;\\012SELECT setval('sample_property_id_seq', max(id)) FROM sample_properties;\\012SELECT setval('group_id_seq', max(id)) FROM groups;\\012SELECT setval('material_id_seq', max(id)) FROM materials;\\012SELECT setval('project_id_seq', max(id)) FROM projects;\\012SELECT setval('sample_id_seq', max(id)) FROM samples;\\012\\012------------------------------------------------------------------------------------\\012-- Migrate MATERIAL_BATCHES and INHIBITOR_OF\\012------------------------------------------------------------------------------------\\012\\012-- Create property type with specified code, description, label, data_type and material type\\012create or replace function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar, material_type_code varchar) returns void as $$\\012declare\\012  material_type_id varchar;\\012begin\\012  select into material_type_id id from material_types where code = material_type_code;\\012  if material_type_id is null\\012  then\\012    material_type_id = 'null';\\012  end if;\\012  execute 'insert into property_types (id, code,  description, label, daty_id,registration_timestamp, pers_id_registerer, covo_id,  is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) values('\\012    || 'nextval(''PROPERTY_TYPE_ID_SEQ'')'\\012    || ',' || quote_literal(prop_code)\\012    || ',' || quote_literal(prop_description)\\012    || ',' || quote_literal(prop_label)\\012    || ',' || '(select id from data_types where code = ' || quote_literal(data_type_code)|| ')'\\012    || ',' || 'now()'\\012    || ',' || '(select id from persons where user_id =''system'')'\\012    || ',' || 'null'\\012    || ',' || 'false'\\012    || ',' || 'true'\\012    || ',' || '(select id from database_instances where is_original_source = true)'\\012    || ',' || material_type_id\\012    || ')';\\012end;\\012$$ language 'plpgsql';\\012\\012-- Create property type without specifing material type\\012create or replace function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar) returns void as $$\\012begin\\012\\011perform create_property_type(prop_code, prop_description, prop_label, data_type_code, null);\\012end;\\012$$ language 'plpgsql';\\012\\012create or replace function assign_property_type(entity varchar, prop_code varchar, type_id integer, mandatory boolean) returns void as $$\\012declare \\012  etpt varchar;\\012  enty varchar;\\012begin\\012  etpt := substring(entity from 1 for 1) || 'tpt';\\012  enty := substring(entity from 1 for 2) || 'ty';\\012  execute 'insert into ' || entity || '_type_property_types (id, ' || enty || '_id, prty_id, pers_id_registerer, is_mandatory) values('\\012  || 'nextval('''|| etpt ||'_id_seq'')'\\012  || ',' || type_id\\012  || ',' || '(select id from property_types where code = ' || quote_literal(prop_code) || ')'\\012  || ',' || '(select id from persons where user_id =''system'')'\\012  || ',' || quote_literal(mandatory)\\012  || ')';\\012end;\\012$$ language 'plpgsql';\\012\\012-- Assign INHIBITOR_OF property to materials of type OLIGO\\012create or replace function assign_inhibitor_property() returns void as $$\\012declare \\012  material_type_id integer;\\012begin\\012  select into material_type_id id from material_types where code = 'OLIGO';\\012  execute assign_property_type('MATERIAL', 'INHIBITOR_OF', material_type_id, true);\\012end;\\012$$ language 'plpgsql';\\012\\012-- Create property values for INHIBITOR_OF \\012create or replace function create_inhibitor_values() returns void as $$\\012declare \\012  material_with_inhibitor record;\\012  material_type_id integer;\\012  material_id integer;\\012  property_type_id integer;\\012  mtpt_id integer;\\012  inhibitor_id integer;\\012begin\\012  for material_with_inhibitor in select distinct id, maty_id, mate_id_inhibitor_of  from materials where mate_id_inhibitor_of is not null loop\\012    material_id := material_with_inhibitor.id;\\012    material_type_id := material_with_inhibitor.maty_id;\\012    inhibitor_id = material_with_inhibitor.mate_id_inhibitor_of;\\012    select into property_type_id id from property_types where code = 'INHIBITOR_OF'; \\012    select into mtpt_id id from material_type_property_types where maty_id = material_type_id and prty_id = property_type_id;\\012    execute 'insert into material_properties (id, mate_id, mtpt_id, pers_id_registerer, mate_prop_id) values('\\012      || 'nextval(''material_property_id_seq'')'\\012      || ',' || material_id\\012      || ',' || mtpt_id\\012      || ',' || '(select id from persons where user_id =''system'')'\\012      || ',' || inhibitor_id\\012      || ')';\\012  end loop;\\012end;\\012$$ language 'plpgsql';\\012\\012-- Assign specified property to samples with defined batches\\012create or replace function assign_material_batch_properties(prop_code varchar) returns void as $$\\012declare \\012  sample_type_id integer;\\012begin\\012  for sample_type_id in select distinct s.saty_id from samples s, sample_material_batches b where b.samp_id = s.id loop\\012    execute assign_property_type('SAMPLE', prop_code, sample_type_id, false);\\012  end loop;\\012end;\\012$$ language 'plpgsql';\\012\\012-- Create property values for given batch property\\012create or replace function create_batch_values(prop_code varchar) returns void as $$\\012declare \\012  sample_with_batches record;\\012  stpt_id integer;\\012  sample_id integer;\\012  registrator_id integer;\\012  registration_timestamp timestamp;\\012  prop_col_name varchar;\\012  prop_value varchar;\\012begin\\012  if prop_code = 'MATERIAL' then\\012    prop_col_name := 'mate_prop_id';\\012  else\\012    prop_col_name := 'value';\\012  end if;\\012  for sample_with_batches in select distinct mb.code, mb.amount, mb.mate_id, mb.registration_timestamp, mb.pers_id_registerer, s.saty_id, s.id from material_batches mb, sample_material_batches smb, samples s where mb.id = smb.maba_id and s.id = smb.samp_id loop\\012    select into stpt_id id from sample_type_property_types where saty_id = sample_with_batches.saty_id and prty_id = (select id from property_types where code = prop_code);\\012    sample_id := sample_with_batches.id;\\012    registrator_id := sample_with_batches.pers_id_registerer;\\012    registration_timestamp := sample_with_batches.registration_timestamp;\\012    if prop_code = 'MATERIAL' then\\012      prop_value := sample_with_batches.mate_id;\\012    elsif prop_code = 'MATERIAL_BATCH' then\\012      prop_value := quote_literal(sample_with_batches.code);\\012    else \\012    \\011prop_value := quote_literal(sample_with_batches.amount);\\012    end if;\\012    if prop_value is not null then\\012      execute 'insert into sample_properties (id, samp_id, stpt_id, pers_id_registerer, ' || prop_col_name || ' , registration_timestamp) values('\\012        || 'nextval(''sample_property_id_seq'')'\\012        || ',' || sample_id\\012        || ',' || stpt_id\\012        || ',' || registrator_id\\012        || ',' || prop_value\\012        || ',' || quote_literal(registration_timestamp)\\012        || ')';\\012    end if;\\012  end loop;\\012end;\\012$$ language 'plpgsql';\\012\\012-- Perform full INHIBITOR migration\\012create or replace function migrate_inhibitor() returns void as $$\\012begin\\012  perform create_property_type('INHIBITOR_OF','Inhibitor Of','Inhibitor Of','MATERIAL', 'GENE');\\012  perform assign_inhibitor_property();\\012  perform create_inhibitor_values();\\012  alter table materials drop column mate_id_inhibitor_of;\\012end;\\012$$ language 'plpgsql';\\012\\012-- Perform full material batch migration\\012create or replace function migrate_material_batches() returns void as $$\\012begin\\012\\011perform create_property_type('MATERIAL','Material','Material','MATERIAL');\\012\\011perform create_property_type('MATERIAL_BATCH','Material Batch','Material Batch','VARCHAR');\\012\\011perform create_property_type('MATERIAL_AMOUNT','Amount Of Material','Material Amount','REAL');\\012\\011\\012\\011perform assign_material_batch_properties('MATERIAL');\\012\\011perform assign_material_batch_properties('MATERIAL_BATCH');\\012\\011perform assign_material_batch_properties('MATERIAL_AMOUNT');\\012\\011\\012\\011perform create_batch_values('MATERIAL');\\012\\011perform create_batch_values('MATERIAL_BATCH');\\012\\011perform create_batch_values('MATERIAL_AMOUNT');\\012  \\012  drop table sample_material_batches;\\012  drop table material_batches;\\012  drop sequence material_batch_id_seq;\\012end;\\012$$ language 'plpgsql';\\012\\012-- Perform migration\\012select migrate_inhibitor();\\012select migrate_material_batches();\\012\\012-- Clean up\\012drop function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar);\\012drop function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar, material_type varchar);\\012drop function assign_property_type(entity varchar, prop_code varchar, type_id integer, mandatory boolean);\\012drop function assign_inhibitor_property();\\012drop function create_inhibitor_values();\\012drop function assign_material_batch_properties(prop_code varchar);\\012drop function create_batch_values(prop_code varchar);\\012drop function migrate_inhibitor();\\012drop function migrate_material_batches();\\012\\012	\N
035	./source//sql/postgresql/migration/migration-034-035.sql	SUCCESS	2009-09-24 08:48:35.245	------------------------------------------------------------------------------------\\012-- Extend events table\\012------------------------------------------------------------------------------------\\012\\012ALTER TABLE events\\012\\011\\011ADD COLUMN entity_type VARCHAR(80);\\012ALTER TABLE events\\012\\011\\011ADD COLUMN identifier VARCHAR(250);\\012\\011\\011\\012-- Creating check constraints\\012\\012ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK \\012\\011\\011(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY')); \\012\\012-- simple migration of events (all old rows contain data set deletion information)\\012\\012UPDATE events SET \\012\\011\\011entity_type = 'DATASET',\\012\\011\\011identifier = data_id,\\012\\011\\011description = data_id;\\012\\012ALTER TABLE events\\012    DROP COLUMN data_id;\\012\\012-- remove deleted data sets with their properties and relations to other data sets\\012\\012CREATE VIEW deleted_data_ids AS\\012    SELECT id FROM data WHERE is_deleted = 'TRUE';\\012DELETE FROM data_set_relationships \\012\\011\\011WHERE data_id_parent IN (SELECT * FROM deleted_data_ids) \\012\\011\\011OR data_id_child IN (SELECT * FROM deleted_data_ids);\\012DELETE FROM data_set_properties \\012    WHERE ds_id IN (SELECT * FROM deleted_data_ids);\\012DELETE FROM external_data\\012\\011\\011WHERE data_id IN (SELECT * FROM deleted_data_ids);\\012DELETE FROM data WHERE is_deleted = 'TRUE';    \\012DROP VIEW deleted_data_ids;\\012\\012-- remove old columns\\012\\012ALTER TABLE data\\012\\011\\011DROP COLUMN is_deleted;\\012\\011\\011\\012-- add constraints\\012\\012ALTER TABLE events\\012    ALTER COLUMN entity_type SET NOT NULL;\\012ALTER TABLE events\\012    ALTER COLUMN identifier SET NOT NULL;\\012ALTER TABLE events\\012\\011\\011ADD CONSTRAINT evnt_bk_uk UNIQUE(EVENT_TYPE,ENTITY_TYPE,IDENTIFIER);\\012	\N
036	./source//sql/postgresql/migration/migration-035-036.sql	SUCCESS	2009-09-24 08:48:35.493	-- Change type description length\\012\\012ALTER TABLE sample_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE data_set_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE experiment_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE material_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE controlled_vocabularies ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE data_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE file_format_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE locator_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012ALTER TABLE property_types ALTER COLUMN description TYPE DESCRIPTION_250;\\012\\012DROP TYPE description_80;\\012\\012-- Add attachment title and description\\012\\012CREATE DOMAIN TITLE_100 AS VARCHAR(100);\\012\\012ALTER TABLE attachments ADD COLUMN title TITLE_100;\\012ALTER TABLE attachments ADD COLUMN description DESCRIPTION_1000;\\012\\012-- Add 'POWER_USER' to AUTHORIZATION_ROLE domain\\012\\012ALTER DOMAIN authorization_role DROP CONSTRAINT authorization_role_check;\\012ALTER DOMAIN authorization_role ADD CONSTRAINT authorization_role_check CHECK (VALUE IN ('ADMIN', 'POWER_USER', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012	\N
037	./source//sql/postgresql/migration/migration-036-037.sql	SUCCESS	2009-09-24 08:48:35.692	-- JAVA ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom036To037\\012\\012-- Add Data Store Services table\\012\\012CREATE DOMAIN DATA_STORE_SERVICE_KIND AS VARCHAR(40) CHECK (VALUE IN ('PROCESSING', 'QUERIES'));\\012\\012CREATE TABLE DATA_STORE_SERVICES  (ID TECH_ID NOT NULL, KEY VARCHAR(256) NOT NULL, LABEL VARCHAR(256) NOT NULL, KIND DATA_STORE_SERVICE_KIND NOT NULL, DATA_STORE_ID TECH_ID NOT NULL);\\012CREATE TABLE DATA_STORE_SERVICE_DATA_SET_TYPES (DATA_STORE_SERVICE_ID TECH_ID NOT NULL, DATA_SET_TYPE_ID TECH_ID NOT NULL);\\012\\012CREATE SEQUENCE DATA_STORE_SERVICES_ID_SEQ;\\012\\012ALTER TABLE DATA_STORE_SERVICES ADD CONSTRAINT DSSE_PK PRIMARY KEY(ID);\\012\\012ALTER TABLE DATA_STORE_SERVICES ADD CONSTRAINT DSSE_BK_UK UNIQUE(KEY, DATA_STORE_ID);\\012ALTER TABLE DATA_STORE_SERVICE_DATA_SET_TYPES ADD CONSTRAINT DSSDST_BK_UK UNIQUE(DATA_STORE_SERVICE_ID, DATA_SET_TYPE_ID);\\012\\012ALTER TABLE DATA_STORE_SERVICES ADD CONSTRAINT DSSE_DS_FK FOREIGN KEY (DATA_STORE_ID) REFERENCES DATA_STORES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_STORE_SERVICE_DATA_SET_TYPES ADD CONSTRAINT DSSDST_DS_FK FOREIGN KEY (DATA_STORE_SERVICE_ID) REFERENCES DATA_STORE_SERVICES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_STORE_SERVICE_DATA_SET_TYPES ADD CONSTRAINT DSSDST_DST_FK FOREIGN KEY (DATA_SET_TYPE_ID) REFERENCES DATA_SET_TYPES(ID) ON DELETE CASCADE;\\012\\012CREATE INDEX DSSE_DS_FK_I ON DATA_STORE_SERVICES (DATA_STORE_ID);\\012CREATE INDEX DSSDST_DS_FK_I ON DATA_STORE_SERVICE_DATA_SET_TYPES (DATA_STORE_SERVICE_ID);\\012CREATE INDEX DSSDST_DST_FK_I ON DATA_STORE_SERVICE_DATA_SET_TYPES (DATA_SET_TYPE_ID);\\012\\012GRANT SELECT ON SEQUENCE DATA_STORE_SERVICES_ID_SEQ TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE DATA_STORE_SERVICES TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE DATA_STORE_SERVICE_DATA_SET_TYPES TO GROUP OPENBIS_READONLY;\\012	\N
038	./source//sql/postgresql/migration/migration-037-038.sql	SUCCESS	2009-09-24 08:48:35.873	-- Change events table check constraint (add 'GROUP')\\012\\012ALTER TABLE events DROP CONSTRAINT evnt_et_enum_ck;\\012ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK \\012\\011\\011(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'GROUP', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY'));\\012	\N
039	./source//sql/postgresql/migration/migration-038-039.sql	SUCCESS	2009-09-24 08:48:36.226	-- Longer descriptions\\012ALTER TABLE sample_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE data_set_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE experiment_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE material_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE controlled_vocabularies ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE data_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE file_format_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE locator_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE property_types ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE events ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE events ALTER COLUMN reason TYPE DESCRIPTION_1000;\\012ALTER TABLE groups ALTER COLUMN description TYPE DESCRIPTION_1000;\\012ALTER TABLE invalidations ALTER COLUMN reason TYPE DESCRIPTION_1000;\\012\\012\\012DROP TYPE DESCRIPTION_250;\\012\\012-- Add label and description to controlled vocabularies\\012\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD COLUMN label COLUMN_LABEL;\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD COLUMN description DESCRIPTION_1000;\\012\\012-- Add authorization groups\\012\\012CREATE TABLE AUTHORIZATION_GROUPS (ID TECH_ID NOT NULL, DBIN_ID TECH_ID NOT NULL, CODE CODE NOT NULL, DESCRIPTION DESCRIPTION_1000,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, PERS_ID_REGISTERER TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012ALTER TABLE AUTHORIZATION_GROUPS ADD CONSTRAINT AG_PK PRIMARY KEY(ID);\\012ALTER TABLE AUTHORIZATION_GROUPS ADD CONSTRAINT AG_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE AUTHORIZATION_GROUPS ADD CONSTRAINT AG_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE AUTHORIZATION_GROUPS ADD CONSTRAINT AG_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012CREATE SEQUENCE AUTHORIZATION_GROUP_ID_SEQ;\\012\\012CREATE TABLE AUTHORIZATION_GROUP_PERSONS (AG_ID TECH_ID NOT NULL, PERS_ID TECH_ID NOT NULL);\\012ALTER TABLE AUTHORIZATION_GROUP_PERSONS ADD CONSTRAINT AGP_PK PRIMARY KEY(PERS_ID,AG_ID);\\012ALTER TABLE AUTHORIZATION_GROUP_PERSONS ADD CONSTRAINT AGP_AG_FK FOREIGN KEY (AG_ID) REFERENCES AUTHORIZATION_GROUPS(ID);\\012ALTER TABLE AUTHORIZATION_GROUP_PERSONS ADD CONSTRAINT AGP_PERS_FK FOREIGN KEY (PERS_ID) REFERENCES PERSONS(ID);\\012\\012ALTER TABLE ROLE_ASSIGNMENTS ALTER COLUMN PERS_ID_GRANTEE DROP NOT NULL;\\012ALTER TABLE ROLE_ASSIGNMENTS ADD COLUMN AG_ID_GRANTEE TECH_ID;\\012\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PE_GROUP_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,GROU_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PE_INSTANCE_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,DBIN_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_AG_GROUP_BK_UK UNIQUE(AG_ID_GRANTEE,ROLE_CODE,GROU_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_AG_INSTANCE_BK_UK UNIQUE(AG_ID_GRANTEE,ROLE_CODE,DBIN_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_AG_PERS_ARC_CK CHECK ((AG_ID_GRANTEE IS NOT NULL AND PERS_ID_GRANTEE IS NULL) OR (AG_ID_GRANTEE IS NULL AND PERS_ID_GRANTEE IS NOT NULL));\\012CREATE INDEX ROAS_AG_FK_I_GRANTEE ON ROLE_ASSIGNMENTS (AG_ID_GRANTEE);\\012\\012ALTER TABLE ROLE_ASSIGNMENTS DROP CONSTRAINT ROAS_GROUP_BK_UK; \\012ALTER TABLE ROLE_ASSIGNMENTS DROP CONSTRAINT ROAS_INSTANCE_BK_UK;  \\012\\012GRANT SELECT ON SEQUENCE AUTHORIZATION_GROUP_ID_SEQ TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE AUTHORIZATION_GROUPS TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE AUTHORIZATION_GROUP_PERSONS TO GROUP OPENBIS_READONLY;\\012\\012-- remove group leader and parent\\012\\012ALTER TABLE groups DROP COLUMN grou_id_parent;\\012ALTER TABLE groups DROP COLUMN pers_id_leader;\\012	\N
040	./source//sql/postgresql/migration/migration-039-040.sql	SUCCESS	2009-09-24 08:48:36.30	-- Change events table check constraint (add 'AUTHORIZATION_GROUP')\\012\\012ALTER TABLE events DROP CONSTRAINT evnt_et_enum_ck;\\012ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK \\012\\011\\011(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'GROUP', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY', 'AUTHORIZATION_GROUP'));\\012	\N
041	./source//sql/postgresql/migration/migration-040-041.sql	SUCCESS	2009-09-24 08:48:36.397	-- Create FILTERS table\\012\\012CREATE TABLE FILTERS (ID TECH_ID NOT NULL, DBIN_ID TECH_ID NOT NULL, NAME VARCHAR(200) NOT NULL, DESCRIPTION DESCRIPTION_1000,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, PERS_ID_REGISTERER TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, EXPRESSION VARCHAR(2000) NOT NULL, IS_PUBLIC BOOLEAN NOT NULL, GRID_ID VARCHAR(200) NOT NULL);\\012CREATE SEQUENCE FILTER_ID_SEQ;\\012ALTER TABLE FILTERS ADD CONSTRAINT FILT_PK PRIMARY KEY(ID);\\012ALTER TABLE FILTERS ADD CONSTRAINT FILT_BK_UK UNIQUE(NAME, DBIN_ID, GRID_ID);\\012ALTER TABLE FILTERS ADD CONSTRAINT FILT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE FILTERS ADD CONSTRAINT FILT_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012CREATE INDEX FILT_PERS_FK_I ON FILTERS (PERS_ID_REGISTERER);\\012CREATE INDEX FILT_DBIN_FK_I ON FILTERS (DBIN_ID);\\012GRANT SELECT ON SEQUENCE filter_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE filters TO GROUP OPENBIS_READONLY;\\012\\012-- Add missing foreign key to role assignments authorization group\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_AG_FK_GRANTEE FOREIGN KEY (AG_ID_GRANTEE) REFERENCES AUTHORIZATION_GROUPS(ID);\\012\\012--------------------------------------------------------------------------------------------------\\012-- Modify dataset connections:\\012-- 1. connection with sample shouldn't be mandatory any more\\012-- 2. introduce an arc condition that dataset cannot be connected with a sample and a parent dataset at the same time\\012--------------------------------------------------------------------------------------------------\\012\\012-- Weaken data-sample connection constraint - allow dataset to have no connection with sample\\012ALTER TABLE data ALTER COLUMN samp_id DROP NOT NULL;\\012\\012-- Remove data-sample connection for datasets that have a parent\\012\\012UPDATE data \\012   SET samp_id = NULL \\012 WHERE id IN (SELECT DISTINCT data_id_child FROM data_set_relationships);\\012\\012-- With PostgreSQL 8.4 one can check that before migration there were no cycles in dataset relationships:\\012--\\012-- WITH RECURSIVE data_set_parents(id, parent_ids, cycle) AS (\\012--         SELECT  r.data_id_child AS id, \\012--                 ARRAY[CAST (r.data_id_parent AS bigint)] AS parent_ids, \\012--                 false AS cycle \\012--         FROM data_set_relationships r\\012--     UNION ALL\\012--         SELECT  r.data_id_child,\\012--                 CAST (r.data_id_parent AS bigint) || p.parent_ids,\\012--                 r.data_id_child = ANY(p.parent_ids)\\012--         FROM data_set_relationships r, data_set_parents p\\012--         WHERE r.data_id_parent = p.id AND NOT cycle\\012-- )\\012-- SELECT count(*) AS cycles FROM data_set_parents WHERE cycle = true;\\012\\012---------------------------------------------------------------------------------------------------\\012--  Purpose:  Create DEFERRED triggers:\\012--            * check_dataset_relationships_on_data_table_modification,\\012--            * check_dataset_relationships_on_relationships_table_modification.\\012--            They check that after all modifications of database (just before commit) \\012--            if 'data'/'data_set_relationships' tables are among modified tables \\012--            dataset is not connected with a sample and a parent dataset at the same time.\\012--            This connections are held in two different tables so simple immediate trigger \\012--            with arc check cannot be used and we need two deferred triggers.\\012----------------------------------------------------------------------------------------------------\\012\\012-- trigger for 'data' table\\012\\012CREATE OR REPLACE FUNCTION check_dataset_relationships_on_data_table_modification() RETURNS trigger AS $$\\012DECLARE\\012\\011counter\\011INTEGER;\\012BEGIN\\012\\011-- if there is a connection with a Sample there should not be any connection with a parent Data Set\\012\\011IF (NEW.samp_id IS NOT NULL) THEN\\012\\011\\011-- count number of parents\\012\\011\\011SELECT count(*) INTO counter \\012\\011\\011\\011FROM data_set_relationships \\012\\011\\011\\011WHERE data_id_child = NEW.id;\\012\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected with a Sample and a parent Data Set at the same time.', NEW.code;\\012\\011\\011END IF;\\012\\011END IF;\\012  RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_data_table_modification \\012  AFTER INSERT OR UPDATE ON data\\012\\011DEFERRABLE INITIALLY DEFERRED\\012\\011FOR EACH ROW \\012\\011EXECUTE PROCEDURE check_dataset_relationships_on_data_table_modification();\\012\\012-- trigger for 'data_set_relationships'\\012\\012CREATE OR REPLACE FUNCTION check_dataset_relationships_on_relationships_table_modification() RETURNS trigger AS $$\\012DECLARE\\012\\011counter\\011INTEGER;\\012\\011sample_id\\011TECH_ID;\\012\\011data_code\\011CODE;\\012BEGIN\\012\\011-- child will have a parent added so it should not be connected with any sample\\012\\011SELECT samp_id, code INTO sample_id, data_code \\012\\011\\011FROM data \\012\\011\\011WHERE id = NEW.data_id_child;\\012\\011IF (sample_id IS NOT NULL) THEN\\012\\011\\011RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected to a Sample and to a parent Data Set at the same time.', data_code;\\012\\011END IF;\\012\\011RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012  \\012CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_relationships_table_modification \\012  AFTER INSERT OR UPDATE ON data_set_relationships\\012\\011DEFERRABLE INITIALLY DEFERRED\\012\\011FOR EACH ROW \\012\\011EXECUTE PROCEDURE check_dataset_relationships_on_relationships_table_modification();\\012	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (id, event_type, description, reason, pers_id_registerer, registration_timestamp, entity_type, identifier) FROM stdin;
1	DELETION	FILENAME	x	2	2009-09-24 08:55:25.172352+02	PROPERTY_TYPE	FILENAME
\.


--
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, pers_id_registerer, registration_timestamp, modification_timestamp, mate_prop_id) FROM stdin;
1	1	1	A simple experiment	\N	2	2009-02-10 15:41:04.90922+01	2009-09-24 08:48:33.450921+02	\N
2	2	3	Praktikum Metabolic Networks 2009	\N	2	2009-09-24 08:58:19.180219+02	2009-09-24 08:58:19.333+02	\N
\.


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp) FROM stdin;
1	1	1	t	t	1	2009-02-10 15:40:42.184979+01
2	2	1	t	t	1	2009-02-10 15:40:42.184979+01
3	3	12	f	f	2	2009-09-24 08:52:26.664215+02
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	SIRNA_HCS	Small Interfering RNA High Content Screening	1	2009-09-24 08:48:34.031951+02
2	COMPOUND_HCS	Compound High Content Screening	1	2009-09-24 08:48:34.031951+02
3	YEASTX_TEST	\N	1	2009-09-24 08:51:02.119116+02
\.


--
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiments (id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, proj_id, inva_id, is_public, modification_timestamp, perm_id) FROM stdin;
1	EXP1	1	14	2	2009-02-10 15:41:04.90922+01	1	\N	f	2009-09-24 08:48:33.450921+02	200902100304909-18
2	EXP_TEST	3	\N	2	2009-09-24 08:51:16.03744+02	2	\N	f	2009-09-24 08:58:19.379+02	20090924085116254-19
\.


--
-- Data for Name: external_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY external_data (data_id, location, ffty_id, loty_id, cvte_id_stor_fmt, is_complete, cvte_id_store) FROM stdin;
\.


--
-- Data for Name: file_format_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY file_format_types (id, code, description, dbin_id) FROM stdin;
1	TIFF	TIFF File	1
2	3VPROPRIETARY	Data Analysis 3V proprietary format	1
3	PLKPROPRIETARY	Data Analysis Pelkmans group proprietary format	1
4	HDF5	Hierarchical Data Format File, version 5	1
5	PROPRIETARY	Proprietary Format File	1
6	SRF	Sequence Read Format File	1
7	TSV	Tab Separated Values File	1
8	XML	XML File	1
9	PDF	\N	1
10	MATLAB	\N	1
11	ARCHIVE	\N	1
12	UNKNOWN	\N	1
\.


--
-- Data for Name: filters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY filters (id, dbin_id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
\.


--
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY groups (id, code, dbin_id, description, registration_timestamp, pers_id_registerer) FROM stdin;
1	CISD	1	\N	2009-02-10 15:40:46.747442+01	2
2	TEST	1	\N	2009-09-24 08:50:23.441466+02	2
\.


--
-- Data for Name: invalidations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY invalidations (id, pers_id_registerer, registration_timestamp, reason) FROM stdin;
\.


--
-- Data for Name: locator_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY locator_types (id, code, description) FROM stdin;
1	RELATIVE_LOCATION	Relative Location
\.


--
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_properties (id, mate_id, mtpt_id, value, registration_timestamp, pers_id_registerer, cvte_id, modification_timestamp, mate_prop_id) FROM stdin;
1	1	1	Adenovirus 3	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
2	2	1	Adenovirus 5	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
3	3	1	Dengue Virus 1	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
4	4	1	Echovirus 1	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
5	5	1	Influenza A virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
6	6	1	Human respiratory virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
7	7	1	Herpes Simplex Virus 1	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
8	8	1	Mouse Hepatitis Virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
9	9	1	Rotavirus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
10	10	1	Rhesus Rotavirus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
11	11	1	Respiratory Syncytial Virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
12	12	1	Rhinovirus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
13	13	1	Semliki Forest Virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
14	14	1	Simian Virus 40	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
15	15	1	Vesicular Stomatitis Virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
16	16	1	Vaccinia Virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
17	17	1	Yellow Fever Virus	2009-02-10 15:40:57.725283+01	2	\N	2009-09-24 08:48:33.450921+02	\N
\.


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer) FROM stdin;
1	1	1	t	t	2009-02-10 15:40:42.184979+01	1
2	6	1	t	t	2009-02-10 15:40:42.184979+01	1
3	7	1	t	t	2009-02-10 15:40:42.184979+01	1
4	3	2	t	t	2009-02-10 15:40:42.184979+01	1
5	3	1	f	t	2009-02-10 15:40:42.184979+01	1
6	4	3	t	t	2009-02-10 15:40:42.184979+01	1
7	4	5	f	t	2009-02-10 15:40:42.184979+01	1
8	4	1	f	t	2009-02-10 15:40:42.184979+01	1
9	5	1	f	t	2009-02-10 15:40:42.184979+01	1
10	4	7	t	f	2009-09-24 08:48:34.842246+02	1
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	VIRUS	Virus	1	2009-09-24 08:48:34.031951+02
2	CELL_LINE	Cell Line or Cell Culture. The growing of cells under controlled conditions.	1	2009-09-24 08:48:34.031951+02
3	GENE	Gene	1	2009-09-24 08:48:34.031951+02
4	OLIGO	Oligo nucleotide	1	2009-09-24 08:48:34.031951+02
5	CONTROL	Control of a control layout	1	2009-09-24 08:48:34.031951+02
6	BACTERIUM	Bacterium	1	2009-09-24 08:48:34.031951+02
7	COMPOUND	Compound	1	2009-09-24 08:48:34.031951+02
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: -
--

COPY materials (id, code, maty_id, pers_id_registerer, registration_timestamp, dbin_id, modification_timestamp) FROM stdin;
1	AD3	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
2	AD5	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
3	DV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
4	EV1	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
5	FLU	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
6	HRV2	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
7	HSV1	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
8	MHV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
9	ROTAV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
10	RRV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
11	RSV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
12	RV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
13	SFV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
14	SV40	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
15	VSV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
16	VV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
17	YFV	1	2	2009-02-10 15:40:57.725283+01	1	2009-09-24 08:48:33.450921+02
18	GFP	5	2	2009-02-10 15:40:58.86855+01	1	2009-09-24 08:48:33.450921+02
19	SCRAM	5	2	2009-02-10 15:40:58.86855+01	1	2009-09-24 08:48:33.450921+02
\.


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY persons (id, first_name, last_name, user_id, email, dbin_id, grou_id, registration_timestamp, pers_id_registerer, display_settings) FROM stdin;
1		System User	system		1	\N	2009-02-10 15:40:42.184979+01	\N	\N
3	John	Etl1	etlserver1	franz-josef.elmer@systemsx.ch	1	1	2009-02-10 15:40:51.094944+01	\N	\N
4	John	Etl2	etlserver2	franz-josef.elmer@systemsx.ch	1	1	2009-02-10 15:40:51.094944+01	\N	\N
2	John	Doe	test	franz-josef.elmer@systemsx.ch	1	1	2009-02-10 15:40:45.854539+01	\N	\\254\\355\\000\\005sr\\000Ach.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\002L\\000\\016columnSettingst\\000\\017Ljava/util/Map;L\\000\\017sectionSettingsq\\000~\\000\\001xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\011t\\000\\022group-browser-gridsr\\000\\023java.util.ArrayListx\\201\\322\\035\\231\\307a\\235\\003\\000\\001I\\000\\004sizexp\\000\\000\\000\\004w\\004\\000\\000\\000\\012sr\\000?ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\004Z\\000\\011hasFilterZ\\000\\006hiddenI\\000\\005widthL\\000\\010columnIDt\\000\\022Ljava/lang/String;xp\\001\\000\\000\\000\\000\\226t\\000\\004CODEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\013DESCRIPTIONsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\013REGISTRATORsq\\000~\\000\\011\\000\\000\\000\\000\\001,t\\000\\021REGISTRATION_DATExt\\000\\034type-browser-grid-EXPERIMENTsq\\000~\\000\\007\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\014sq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000\\016sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021DATABASE_INSTANCExt\\000\\030type-browser-grid-SAMPLEsq\\000~\\000\\007\\000\\000\\000\\006w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\014sq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000\\016sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000\\030sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013IS_LISTABLEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310t\\000\\021IS_SHOW_CONTAINERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310t\\000\\036GENERATED_FROM_HIERARCHY_DEPTHxt\\000\\032type-browser-grid-DATA_SETsq\\000~\\000\\007\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\014sq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000\\016sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000\\030xt\\000\\032property-type-browser-gridsq\\000~\\000\\007\\000\\000\\000\\013w\\004\\000\\000\\000\\020sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226t\\000\\005LABELsq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\014sq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\011DATA_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\016DATA_TYPE_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012VOCABULARYsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\015MATERIAL_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000\\016sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\014SAMPLE_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\020EXPERIMENT_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\016MATERIAL_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\016DATA_SET_TYPESxt\\000$entity-browser-grid-EXPERIMENT-(all)sq\\000~\\000\\007\\000\\000\\000\\015w\\004\\000\\000\\000\\020sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\014sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\017EXPERIMENT_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\025EXPERIMENT_IDENTIFIERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000\\030sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\005GROUPsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PROJECTsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000\\020sq\\000~\\000\\011\\000\\000\\000\\000\\000\\310q\\000~\\000\\022sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012IS_INVALIDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PERM_IDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021SHOW_DETAILS_LINKsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\022property-USER-NAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\031property-USER-DESCRIPTIONxt\\000\\035file-format-type-browser-gridsq\\000~\\000\\007\\000\\000\\000\\002w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000\\014sq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000\\016xt\\000\\023person-browser-gridsq\\000~\\000\\007\\000\\000\\000\\006w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226t\\000\\007USER_IDsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\012FIRST_NAMEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\011LAST_NAMEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310t\\000\\005EMAILsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000\\020sq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000\\022xt\\000%property-type-assignment-browser-gridsq\\000~\\000\\007\\000\\000\\000\\007w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\022PROPERTY_TYPE_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000,sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000\\016sq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\020ENTITY_TYPE_CODEsq\\000~\\000\\011\\001\\000\\000\\000\\000\\226t\\000\\013ENTITY_KINDsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\014IS_MANDATORYsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310q\\000~\\000/xx\\000sq\\000~\\000\\004?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\000x
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projects (id, code, grou_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	NEMO	1	\N	\N	2	2009-02-10 15:40:56.771896+01	2009-09-24 08:48:34.310323+02
2	TEST_PROJECT	2	\N	\N	2	2009-09-24 08:50:38.436033+02	2009-09-24 08:51:16.331+02
\.


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) FROM stdin;
1	DESCRIPTION	A Description	Description	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1	\N
2	GENE_SYMBOL	Gene Symbol, e.g. BMP15	Gene Symbol	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1	\N
3	NUCLEOTIDE_SEQUENCE	A sequence of nucleotides	Nucleotide Sequence	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1	\N
4	REFSEQ	NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein	RefSeq	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1	\N
5	OFFSET	Offset from the start of the sequence	Offset	2	2009-02-10 15:40:42.184979+01	1	\N	f	f	1	\N
6	PLATE_GEOMETRY	Plate Geometry	Plate Geometry	6	2009-02-10 15:40:42.184979+01	1	1	t	t	1	\N
7	INHIBITOR_OF	Inhibitor Of	Inhibitor Of	7	2009-09-24 08:48:34.842246+02	1	\N	f	t	1	3
8	MATERIAL	Material	Material	7	2009-09-24 08:48:34.842246+02	1	\N	f	t	1	\N
9	MATERIAL_BATCH	Material Batch	Material Batch	1	2009-09-24 08:48:34.842246+02	1	\N	f	t	1	\N
10	MATERIAL_AMOUNT	Amount Of Material	Material Amount	3	2009-09-24 08:48:34.842246+02	1	\N	f	t	1	\N
11	SAMPLENAME	 	Sample name	1	2009-09-24 08:51:56.615603+02	2	\N	f	f	1	\N
12	NAME	 	Experiment name	1	2009-09-24 08:52:08.469077+02	2	\N	f	f	1	\N
14	FILE_NAME	 	File name	1	2009-09-24 08:55:09.464431+02	2	\N	f	f	1	\N
15	DATASETCOMMENTS	 	Dataset comment	1	2009-09-24 09:00:04.153802+02	2	\N	f	f	1	\N
\.


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY role_assignments (id, role_code, grou_id, dbin_id, pers_id_grantee, pers_id_registerer, registration_timestamp, ag_id_grantee) FROM stdin;
3	ETL_SERVER	\N	1	3	2	2009-02-10 15:40:53.51404+01	\N
4	ETL_SERVER	1	\N	3	2	2009-02-10 15:40:53.51404+01	\N
5	ETL_SERVER	\N	1	4	2	2009-02-10 15:40:53.51404+01	\N
6	ETL_SERVER	1	\N	4	2	2009-02-10 15:40:53.51404+01	\N
2	ADMIN	1	\N	2	2	2009-02-10 15:40:53.51404+01	\N
1	ADMIN	\N	1	2	1	2009-02-10 15:40:45.854539+01	\N
\.


--
-- Data for Name: sample_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_properties (id, samp_id, stpt_id, value, cvte_id, pers_id_registerer, registration_timestamp, modification_timestamp, mate_prop_id) FROM stdin;
1	1	1	\N	2	2	2009-02-10 15:40:59.870171+01	2009-09-24 08:48:33.450921+02	\N
2	4	1	\N	2	2	2009-02-10 15:41:00.923687+01	2009-09-24 08:48:33.450921+02	\N
3	11	2	\N	2	2	2009-02-10 15:41:02.898333+01	2009-09-24 08:48:33.450921+02	\N
4	13	3	\N	\N	2	2009-02-10 15:41:02.898333+01	2009-09-24 08:48:34.842246+02	18
5	12	3	\N	\N	2	2009-02-10 15:41:02.898333+01	2009-09-24 08:48:34.842246+02	19
6	3	3	\N	\N	2	2009-02-10 15:40:59.870171+01	2009-09-24 08:48:34.842246+02	18
7	2	3	\N	\N	2	2009-02-10 15:40:59.870171+01	2009-09-24 08:48:34.842246+02	19
8	6	3	\N	\N	2	2009-02-10 15:41:00.923687+01	2009-09-24 08:48:34.842246+02	18
9	5	3	\N	\N	2	2009-02-10 15:41:00.923687+01	2009-09-24 08:48:34.842246+02	19
10	13	4	CL-3V	\N	2	2009-02-10 15:41:02.898333+01	2009-09-24 08:48:34.842246+02	\N
11	12	4	CL-3V	\N	2	2009-02-10 15:41:02.898333+01	2009-09-24 08:48:34.842246+02	\N
12	3	4	CODE1	\N	2	2009-02-10 15:40:59.870171+01	2009-09-24 08:48:34.842246+02	\N
13	2	4	CODE1	\N	2	2009-02-10 15:40:59.870171+01	2009-09-24 08:48:34.842246+02	\N
14	6	4	CODE2	\N	2	2009-02-10 15:41:00.923687+01	2009-09-24 08:48:34.842246+02	\N
15	5	4	CODE2	\N	2	2009-02-10 15:41:00.923687+01	2009-09-24 08:48:34.842246+02	\N
16	18	6	glucose 1	\N	2	2009-09-24 08:57:36.754525+02	2009-09-24 08:57:37.089+02	\N
17	19	6	glucose 2	\N	2	2009-09-24 08:57:46.064175+02	2009-09-24 08:57:46.285+02	\N
\.


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed) FROM stdin;
1	1	6	t	t	1	2009-02-10 15:40:42.184979+01	t
2	5	6	t	t	1	2009-02-10 15:40:42.184979+01	t
3	6	8	f	f	1	2009-09-24 08:48:34.842246+02	t
4	6	9	f	f	1	2009-09-24 08:48:34.842246+02	t
5	6	10	f	f	1	2009-09-24 08:48:34.842246+02	t
6	7	11	f	f	2	2009-09-24 08:53:11.963568+02	f
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp) FROM stdin;
1	MASTER_PLATE	Master Plate	1	t	0	0	2009-09-24 08:48:34.031951+02
2	DILUTION_PLATE	Dilution Plate	1	t	1	0	2009-09-24 08:48:34.031951+02
3	CELL_PLATE	Cell Plate	1	t	2	0	2009-09-24 08:48:34.031951+02
4	REINFECT_PLATE	Re-infection Plate	1	t	3	0	2009-09-24 08:48:34.031951+02
5	CONTROL_LAYOUT	Control layout	1	t	0	0	2009-09-24 08:48:34.031951+02
6	WELL	Plate Well	1	f	0	1	2009-09-24 08:48:34.031951+02
7	YEASTX_TEST	\N	1	t	2	0	2009-09-24 08:53:05.712575+02
\.


--
-- Data for Name: samples; Type: TABLE DATA; Schema: public; Owner: -
--

COPY samples (id, code, samp_id_top, samp_id_generated_from, saty_id, registration_timestamp, pers_id_registerer, inva_id, samp_id_control_layout, dbin_id, grou_id, samp_id_part_of, modification_timestamp, expe_id, perm_id) FROM stdin;
1	MP001-1	\N	\N	1	2009-02-10 15:40:59.870171+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100359870-1
2	A03	\N	\N	6	2009-02-10 15:40:59.870171+01	2	\N	\N	\N	1	1	2009-09-24 08:48:33.450921+02	\N	200902100359870-2
3	A04	\N	\N	6	2009-02-10 15:40:59.870171+01	2	\N	\N	\N	1	1	2009-09-24 08:48:33.450921+02	\N	200902100359870-3
4	MP002-1	\N	\N	1	2009-02-10 15:41:00.923687+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100300923-4
5	A03	\N	\N	6	2009-02-10 15:41:00.923687+01	2	\N	\N	\N	1	4	2009-09-24 08:48:33.450921+02	\N	200902100300923-5
6	A04	\N	\N	6	2009-02-10 15:41:00.923687+01	2	\N	\N	\N	1	4	2009-09-24 08:48:33.450921+02	\N	200902100300923-6
7	3V-123	1	1	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100301899-7
8	3V-124	1	1	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100301899-8
9	3V-125	4	4	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100301899-9
10	3V-126	4	4	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100301899-10
11	CL-3V	\N	\N	5	2009-02-10 15:41:02.898333+01	2	\N	\N	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100302898-11
12	A01	\N	\N	6	2009-02-10 15:41:02.898333+01	2	\N	\N	\N	1	11	2009-09-24 08:48:33.450921+02	\N	200902100302898-12
13	A02	\N	\N	6	2009-02-10 15:41:02.898333+01	2	\N	\N	\N	1	11	2009-09-24 08:48:33.450921+02	\N	200902100302898-13
14	3VCP1	1	7	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N	2009-09-24 08:48:33.450921+02	1	200902100303801-14
15	3VCP2	1	7	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N	2009-09-24 08:48:33.450921+02	\N	200902100303801-15
16	3VCP3	4	9	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N	2009-09-24 08:48:33.450921+02	1	200902100303801-16
17	3VCP4	4	9	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N	2009-09-24 08:48:33.450921+02	1	200902100303801-17
18	S32	\N	\N	7	2009-09-24 08:57:36.754525+02	2	\N	\N	\N	2	\N	2009-09-24 08:57:37.087+02	2	20090924085737062-20
19	S33	\N	\N	7	2009-09-24 08:57:46.064175+02	2	\N	\N	\N	2	\N	2009-09-24 08:57:46.282+02	2	20090924085746255-21
\.


--
-- Name: ag_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_bk_uk UNIQUE (code, dbin_id);


--
-- Name: ag_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pk PRIMARY KEY (id);


--
-- Name: agp_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pk PRIMARY KEY (pers_id, ag_id);


--
-- Name: atta_expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);


--
-- Name: atta_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pk PRIMARY KEY (id);


--
-- Name: atta_proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);


--
-- Name: atta_samp_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);


--
-- Name: covo_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);


--
-- Name: covo_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);


--
-- Name: cvte_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);


--
-- Name: cvte_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);


--
-- Name: dast_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, dbin_id);


--
-- Name: dast_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);


--
-- Name: data_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_bk_uk UNIQUE (code);


--
-- Name: data_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);


--
-- Name: daty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);


--
-- Name: daty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);


--
-- Name: dbin_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_bk_uk UNIQUE (code);


--
-- Name: dbin_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_pk PRIMARY KEY (id);


--
-- Name: dbin_uuid_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_uuid_uk UNIQUE (uuid);


--
-- Name: dspr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_bk_uk UNIQUE (ds_id, dstpt_id);


--
-- Name: dspr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pk PRIMARY KEY (id);


--
-- Name: dsre_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);


--
-- Name: dssdst_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_bk_uk UNIQUE (data_store_service_id, data_set_type_id);


--
-- Name: dsse_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_bk_uk UNIQUE (key, data_store_id);


--
-- Name: dsse_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_pk PRIMARY KEY (id);


--
-- Name: dstpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_bk_uk UNIQUE (dsty_id, prty_id);


--
-- Name: dstpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pk PRIMARY KEY (id);


--
-- Name: dsty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: dsty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);


--
-- Name: etpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);


--
-- Name: etpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);


--
-- Name: evnt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_bk_uk UNIQUE (event_type, entity_type, identifier);


--
-- Name: evnt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);


--
-- Name: exac_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);


--
-- Name: exda_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);


--
-- Name: exda_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);


--
-- Name: expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);


--
-- Name: expe_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);


--
-- Name: expe_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);


--
-- Name: expr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);


--
-- Name: expr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);


--
-- Name: exty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: exty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);


--
-- Name: ffty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: ffty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);


--
-- Name: filt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_bk_uk UNIQUE (name, dbin_id, grid_id);


--
-- Name: filt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pk PRIMARY KEY (id);


--
-- Name: grou_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_bk_uk UNIQUE (code, dbin_id);


--
-- Name: grou_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pk PRIMARY KEY (id);


--
-- Name: inva_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pk PRIMARY KEY (id);


--
-- Name: loty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);


--
-- Name: loty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);


--
-- Name: mapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);


--
-- Name: mapr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);


--
-- Name: mate_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id, dbin_id);


--
-- Name: mate_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);


--
-- Name: maty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: maty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);


--
-- Name: mtpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);


--
-- Name: mtpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);


--
-- Name: pers_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (dbin_id, user_id);


--
-- Name: pers_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);


--
-- Name: proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, grou_id);


--
-- Name: proj_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);


--
-- Name: prty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);


--
-- Name: prty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);


--
-- Name: roas_ag_group_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_group_bk_uk UNIQUE (ag_id_grantee, role_code, grou_id);


--
-- Name: roas_ag_instance_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_instance_bk_uk UNIQUE (ag_id_grantee, role_code, dbin_id);


--
-- Name: roas_pe_group_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pe_group_bk_uk UNIQUE (pers_id_grantee, role_code, grou_id);


--
-- Name: roas_pe_instance_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pe_instance_bk_uk UNIQUE (pers_id_grantee, role_code, dbin_id);


--
-- Name: roas_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);


--
-- Name: samp_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);


--
-- Name: samp_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);


--
-- Name: sapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);


--
-- Name: sapr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);


--
-- Name: saty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: saty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);


--
-- Name: stpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);


--
-- Name: stpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);


--
-- Name: atta_exac_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_exac_fk_i ON attachments USING btree (exac_id);


--
-- Name: atta_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_expe_fk_i ON attachments USING btree (expe_id);


--
-- Name: atta_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_pers_fk_i ON attachments USING btree (pers_id_registerer);


--
-- Name: atta_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_proj_fk_i ON attachments USING btree (proj_id);


--
-- Name: atta_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_samp_fk_i ON attachments USING btree (samp_id);


--
-- Name: covo_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX covo_pers_fk_i ON controlled_vocabularies USING btree (pers_id_registerer);


--
-- Name: cvte_covo_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cvte_covo_fk_i ON controlled_vocabulary_terms USING btree (covo_id);


--
-- Name: cvte_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cvte_pers_fk_i ON controlled_vocabulary_terms USING btree (pers_id_registerer);


--
-- Name: dast_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dast_dbin_fk_i ON data_stores USING btree (dbin_id);


--
-- Name: data_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_dsty_fk_i ON data USING btree (dsty_id);


--
-- Name: data_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);


--
-- Name: data_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);


--
-- Name: dspr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_cvte_fk_i ON data_set_properties USING btree (cvte_id);


--
-- Name: dspr_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_ds_fk_i ON data_set_properties USING btree (ds_id);


--
-- Name: dspr_dstpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_dstpt_fk_i ON data_set_properties USING btree (dstpt_id);


--
-- Name: dspr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_pers_fk_i ON data_set_properties USING btree (pers_id_registerer);


--
-- Name: dsre_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_child ON data_set_relationships USING btree (data_id_child);


--
-- Name: dsre_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships USING btree (data_id_parent);


--
-- Name: dssdst_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dssdst_ds_fk_i ON data_store_service_data_set_types USING btree (data_store_service_id);


--
-- Name: dssdst_dst_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dssdst_dst_fk_i ON data_store_service_data_set_types USING btree (data_set_type_id);


--
-- Name: dsse_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsse_ds_fk_i ON data_store_services USING btree (data_store_id);


--
-- Name: dstpt_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_dsty_fk_i ON data_set_type_property_types USING btree (dsty_id);


--
-- Name: dstpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_pers_fk_i ON data_set_type_property_types USING btree (pers_id_registerer);


--
-- Name: dstpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_prty_fk_i ON data_set_type_property_types USING btree (prty_id);


--
-- Name: etpt_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_exty_fk_i ON experiment_type_property_types USING btree (exty_id);


--
-- Name: etpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_pers_fk_i ON experiment_type_property_types USING btree (pers_id_registerer);


--
-- Name: etpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_prty_fk_i ON experiment_type_property_types USING btree (prty_id);


--
-- Name: evnt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX evnt_pers_fk_i ON events USING btree (pers_id_registerer);


--
-- Name: exda_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_cvte_fk_i ON external_data USING btree (cvte_id_stor_fmt);


--
-- Name: exda_cvte_stored_on_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_cvte_stored_on_fk_i ON external_data USING btree (cvte_id_store);


--
-- Name: exda_ffty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_ffty_fk_i ON external_data USING btree (ffty_id);


--
-- Name: exda_loty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_loty_fk_i ON external_data USING btree (loty_id);


--
-- Name: expe_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_exty_fk_i ON experiments USING btree (exty_id);


--
-- Name: expe_inva_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_inva_fk_i ON experiments USING btree (inva_id);


--
-- Name: expe_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_mate_fk_i ON experiments USING btree (mate_id_study_object);


--
-- Name: expe_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_pers_fk_i ON experiments USING btree (pers_id_registerer);


--
-- Name: expe_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_proj_fk_i ON experiments USING btree (proj_id);


--
-- Name: expr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_cvte_fk_i ON experiment_properties USING btree (cvte_id);


--
-- Name: expr_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_etpt_fk_i ON experiment_properties USING btree (etpt_id);


--
-- Name: expr_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_expe_fk_i ON experiment_properties USING btree (expe_id);


--
-- Name: expr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_pers_fk_i ON experiment_properties USING btree (pers_id_registerer);


--
-- Name: filt_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX filt_dbin_fk_i ON filters USING btree (dbin_id);


--
-- Name: filt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX filt_pers_fk_i ON filters USING btree (pers_id_registerer);


--
-- Name: grou_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grou_dbin_fk_i ON groups USING btree (dbin_id);


--
-- Name: grou_pers_registered_by_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grou_pers_registered_by_fk_i ON groups USING btree (pers_id_registerer);


--
-- Name: inva_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inva_pers_fk_i ON invalidations USING btree (pers_id_registerer);


--
-- Name: mapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);


--
-- Name: mapr_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mate_fk_i ON material_properties USING btree (mate_id);


--
-- Name: mapr_mtpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mtpt_fk_i ON material_properties USING btree (mtpt_id);


--
-- Name: mapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_pers_fk_i ON material_properties USING btree (pers_id_registerer);


--
-- Name: mate_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);


--
-- Name: mate_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);


--
-- Name: mtpt_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_maty_fk_i ON material_type_property_types USING btree (maty_id);


--
-- Name: mtpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_pers_fk_i ON material_type_property_types USING btree (pers_id_registerer);


--
-- Name: mtpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_prty_fk_i ON material_type_property_types USING btree (prty_id);


--
-- Name: pers_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pers_grou_fk_i ON persons USING btree (grou_id);


--
-- Name: proj_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_grou_fk_i ON projects USING btree (grou_id);


--
-- Name: proj_pers_fk_i_leader; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_leader ON projects USING btree (pers_id_leader);


--
-- Name: proj_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_registerer ON projects USING btree (pers_id_registerer);


--
-- Name: prty_covo_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_covo_fk_i ON property_types USING btree (covo_id);


--
-- Name: prty_daty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_daty_fk_i ON property_types USING btree (daty_id);


--
-- Name: prty_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_pers_fk_i ON property_types USING btree (pers_id_registerer);


--
-- Name: roas_ag_fk_i_grantee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_ag_fk_i_grantee ON role_assignments USING btree (ag_id_grantee);


--
-- Name: roas_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_dbin_fk_i ON role_assignments USING btree (dbin_id);


--
-- Name: roas_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_grou_fk_i ON role_assignments USING btree (grou_id);


--
-- Name: roas_pers_fk_i_grantee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);


--
-- Name: roas_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);


--
-- Name: samp_code_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_code_i ON samples USING btree (code);


--
-- Name: samp_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);


--
-- Name: samp_inva_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_inva_fk_i ON samples USING btree (inva_id);


--
-- Name: samp_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_pers_fk_i ON samples USING btree (pers_id_registerer);


--
-- Name: samp_samp_fk_i_control_layout; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_control_layout ON samples USING btree (samp_id_control_layout);


--
-- Name: samp_samp_fk_i_generated_from; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_generated_from ON samples USING btree (samp_id_generated_from);


--
-- Name: samp_samp_fk_i_part_of; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_part_of ON samples USING btree (samp_id_part_of);


--
-- Name: samp_samp_fk_i_top; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_top ON samples USING btree (samp_id_top);


--
-- Name: samp_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_saty_fk_i ON samples USING btree (saty_id);


--
-- Name: sapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);


--
-- Name: sapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_pers_fk_i ON sample_properties USING btree (pers_id_registerer);


--
-- Name: sapr_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_samp_fk_i ON sample_properties USING btree (samp_id);


--
-- Name: sapr_stpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_stpt_fk_i ON sample_properties USING btree (stpt_id);


--
-- Name: stpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_pers_fk_i ON sample_type_property_types USING btree (pers_id_registerer);


--
-- Name: stpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_prty_fk_i ON sample_type_property_types USING btree (prty_id);


--
-- Name: stpt_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_saty_fk_i ON sample_type_property_types USING btree (saty_id);


--
-- Name: check_dataset_relationships_on_data_table_modification; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_data_table_modification
    AFTER INSERT OR UPDATE ON data
DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_dataset_relationships_on_data_table_modification();


--
-- Name: check_dataset_relationships_on_relationships_table_modification; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_relationships_table_modification
    AFTER INSERT OR UPDATE ON data_set_relationships
DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_dataset_relationships_on_relationships_table_modification();


--
-- Name: controlled_vocabulary_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER controlled_vocabulary_check
    BEFORE INSERT OR UPDATE ON property_types
    FOR EACH ROW
    EXECUTE PROCEDURE controlled_vocabulary_check();


--
-- Name: data_set_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON data_set_properties
    FOR EACH ROW
    EXECUTE PROCEDURE data_set_property_with_material_data_type_check();


--
-- Name: experiment_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON experiment_properties
    FOR EACH ROW
    EXECUTE PROCEDURE experiment_property_with_material_data_type_check();


--
-- Name: external_data_storage_format_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER external_data_storage_format_check
    BEFORE INSERT OR UPDATE ON external_data
    FOR EACH ROW
    EXECUTE PROCEDURE external_data_storage_format_check();


--
-- Name: material_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER material_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON material_properties
    FOR EACH ROW
    EXECUTE PROCEDURE material_property_with_material_data_type_check();


--
-- Name: sample_code_uniqueness_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_code_uniqueness_check
    BEFORE INSERT OR UPDATE ON samples
    FOR EACH ROW
    EXECUTE PROCEDURE sample_code_uniqueness_check();


--
-- Name: sample_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON sample_properties
    FOR EACH ROW
    EXECUTE PROCEDURE sample_property_with_material_data_type_check();


--
-- Name: ag_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: ag_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: agp_ag_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_ag_fk FOREIGN KEY (ag_id) REFERENCES authorization_groups(id);


--
-- Name: agp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pers_fk FOREIGN KEY (pers_id) REFERENCES persons(id);


--
-- Name: atta_cont_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);


--
-- Name: atta_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: atta_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: atta_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: atta_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: covo_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: covo_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: cvte_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: cvte_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dast_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: data_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: data_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);


--
-- Name: data_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: data_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: dspr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: dspr_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id) REFERENCES data(id);


--
-- Name: dspr_dstpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;


--
-- Name: dspr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: dspr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dsre_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data(id);


--
-- Name: dsre_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data(id);


--
-- Name: dssdst_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_ds_fk FOREIGN KEY (data_store_service_id) REFERENCES data_store_services(id) ON DELETE CASCADE;


--
-- Name: dssdst_dst_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_dst_fk FOREIGN KEY (data_set_type_id) REFERENCES data_set_types(id) ON DELETE CASCADE;


--
-- Name: dsse_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_ds_fk FOREIGN KEY (data_store_id) REFERENCES data_stores(id) ON DELETE CASCADE;


--
-- Name: dstpt_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id) ON DELETE CASCADE;


--
-- Name: dstpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dstpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: dsty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: etpt_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id) ON DELETE CASCADE;


--
-- Name: etpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: etpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: evnt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exda_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: exda_cvte_stored_on_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_stored_on_fk FOREIGN KEY (cvte_id_store) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: exda_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data(id);


--
-- Name: exda_ffty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);


--
-- Name: exda_loty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);


--
-- Name: expe_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);


--
-- Name: expe_inva_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);


--
-- Name: expe_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_mate_fk FOREIGN KEY (mate_id_study_object) REFERENCES materials(id);


--
-- Name: expe_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: expe_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: expr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: expr_etpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;


--
-- Name: expr_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: expr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: expr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: ffty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: filt_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: filt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: grou_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: grou_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: inva_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: mapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: mapr_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);


--
-- Name: mapr_mtpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;


--
-- Name: mapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mate_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: mate_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);


--
-- Name: mate_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: maty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: mtpt_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: mtpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mtpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: pers_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: pers_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: pers_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: proj_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: proj_pers_fk_leader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id);


--
-- Name: proj_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: prty_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: prty_daty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES data_types(id);


--
-- Name: prty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: prty_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: prty_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: roas_ag_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_fk_grantee FOREIGN KEY (ag_id_grantee) REFERENCES authorization_groups(id);


--
-- Name: roas_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: roas_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: roas_pers_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES persons(id);


--
-- Name: roas_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: samp_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: samp_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: samp_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: samp_inva_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);


--
-- Name: samp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: samp_samp_fk_control_layout; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_control_layout FOREIGN KEY (samp_id_control_layout) REFERENCES samples(id);


--
-- Name: samp_samp_fk_generated_from; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_generated_from FOREIGN KEY (samp_id_generated_from) REFERENCES samples(id);


--
-- Name: samp_samp_fk_part_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of) REFERENCES samples(id);


--
-- Name: samp_samp_fk_top; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_top FOREIGN KEY (samp_id_top) REFERENCES samples(id);


--
-- Name: samp_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);


--
-- Name: sapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: sapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: sapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: sapr_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: sapr_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: saty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: stpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: stpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: stpt_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--


