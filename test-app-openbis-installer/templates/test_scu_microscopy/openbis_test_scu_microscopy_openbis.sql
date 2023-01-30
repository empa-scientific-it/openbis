--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: archiving_status; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN archiving_status AS character varying(100)
	CONSTRAINT archiving_status_check CHECK (((VALUE)::text = ANY ((ARRAY['LOCKED'::character varying, 'AVAILABLE'::character varying, 'ARCHIVED'::character varying, 'ARCHIVE_PENDING'::character varying, 'UNARCHIVE_PENDING'::character varying, 'BACKUP_PENDING'::character varying])::text[])));


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

CREATE DOMAIN code AS character varying(60);


--
-- Name: column_label; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN column_label AS character varying(128);


--
-- Name: data_set_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN data_set_kind AS character varying(40)
	CONSTRAINT data_set_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['PHYSICAL'::character varying, 'LINK'::character varying, 'CONTAINER'::character varying])::text[])));


--
-- Name: data_store_service_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN data_store_service_kind AS character varying(40)
	CONSTRAINT data_store_service_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['PROCESSING'::character varying, 'QUERIES'::character varying])::text[])));


--
-- Name: data_store_service_reporting_plugin_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN data_store_service_reporting_plugin_type AS character varying(40)
	CONSTRAINT data_store_service_reporting_plugin_type_check CHECK (((VALUE)::text = ANY ((ARRAY['TABLE_MODEL'::character varying, 'DSS_LINK'::character varying, 'AGGREGATION_TABLE_MODEL'::character varying])::text[])));


--
-- Name: description_2000; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_2000 AS character varying(2000);


--
-- Name: entity_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN entity_kind AS character varying(40)
	CONSTRAINT entity_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['SAMPLE'::character varying, 'EXPERIMENT'::character varying, 'DATA_SET'::character varying, 'MATERIAL'::character varying])::text[])));


--
-- Name: event_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DELETION'::character varying, 'MOVEMENT'::character varying])::text[])));


--
-- Name: file; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file AS bytea;


--
-- Name: file_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_name AS character varying(255);


--
-- Name: grid_expression; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN grid_expression AS character varying(2000);


--
-- Name: grid_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN grid_id AS character varying(200);


--
-- Name: identifier; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN identifier AS character varying(200);


--
-- Name: object_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN object_name AS character varying(50);


--
-- Name: operation_execution_state; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN operation_execution_state AS character varying(40)
	CONSTRAINT operation_execution_state_check CHECK (((VALUE)::text = ANY ((ARRAY['NEW'::character varying, 'SCHEDULED'::character varying, 'RUNNING'::character varying, 'FINISHED'::character varying, 'FAILED'::character varying])::text[])));


--
-- Name: ordinal_int; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN ordinal_int AS bigint
	CONSTRAINT ordinal_int_check CHECK ((VALUE > 0));


--
-- Name: plugin_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN plugin_type AS character varying(40)
	CONSTRAINT plugin_type_check CHECK (((VALUE)::text = ANY ((ARRAY['JYTHON'::character varying, 'PREDEPLOYED'::character varying])::text[])));


--
-- Name: query_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN query_type AS character varying(40)
	CONSTRAINT query_type_check CHECK (((VALUE)::text = ANY ((ARRAY['GENERIC'::character varying, 'EXPERIMENT'::character varying, 'SAMPLE'::character varying, 'DATA_SET'::character varying, 'MATERIAL'::character varying])::text[])));


--
-- Name: real_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN real_value AS real;


--
-- Name: script_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN script_type AS character varying(40)
	CONSTRAINT script_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DYNAMIC_PROPERTY'::character varying, 'MANAGED_PROPERTY'::character varying, 'ENTITY_VALIDATION'::character varying])::text[])));


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: text_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN text_value AS text;


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
-- Name: check_created_or_modified_data_set_owner_is_alive(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL) THEN
		RETURN NEW;
	END IF;

  -- check sample
  IF (NEW.samp_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM samples 
  	  WHERE id = NEW.samp_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to a Sample (Code: %) %.', 
			                NEW.code, owner_code, deletion_description(owner_del_id);
		END IF;
	END IF;
	-- check experiment
  IF (NEW.expe_id IS NOT NULL) THEN
		SELECT del_id, code INTO owner_del_id, owner_code
	    FROM experiments 
	    WHERE id = NEW.expe_id;
	  IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to an Experiment (Code: %) %.', 
			                NEW.code, owner_code, deletion_description(owner_del_id);
		END IF;	
	END IF;	
	RETURN NEW;
END;
$$;


--
-- Name: check_created_or_modified_sample_owner_is_alive(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION check_created_or_modified_sample_owner_is_alive() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL) THEN
		RETURN NEW;
	END IF;

  -- check experiment (can't be deleted)
  IF (NEW.expe_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM experiments 
  	  WHERE id = NEW.expe_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Sample (Code: %) cannot be connected to an Experiment (Code: %) %.', 
   		                NEW.code, owner_code, deletion_description(owner_del_id);
		END IF;
	END IF;
	RETURN NEW;
END;
$$;


--
-- Name: check_deletion_consistency_on_experiment_deletion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION check_deletion_consistency_on_experiment_deletion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  counter  INTEGER;
BEGIN
	IF (OLD.del_id IS NOT NULL OR NEW.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.expe_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
	-- check samples
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.expe_id = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$;


--
-- Name: check_deletion_consistency_on_sample_deletion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION check_deletion_consistency_on_sample_deletion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  counter  INTEGER;
BEGIN
	IF (OLD.del_id IS NOT NULL OR NEW.del_id IS NULL) THEN
		RETURN NEW;
	END IF;

  -- all directly connected data sets need to be deleted
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.samp_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
  -- all components need to be deleted
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.samp_id_part_of = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its component samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$;


--
-- Name: controlled_vocabulary_check(); Type: FUNCTION; Schema: public; Owner: -
--

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


--
-- Name: data_exp_or_sample_link_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION data_exp_or_sample_link_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  space_id CODE;
  sample_code CODE;
BEGIN
  if NEW.expe_id IS NOT NULL then
    RETURN NEW;
  end if;
  if NEW.samp_id IS NULL then
    RAISE EXCEPTION 'Neither experiment nor sample is specified for data set %', NEW.code;
  end if;
  select s.id, s.code into space_id, sample_code from samples_all s where s.id = NEW.samp_id;
  if space_id is NULL then
    RAISE EXCEPTION 'Sample % is a shared sample.', sample_code;
  end if;
  RETURN NEW;
END;
$$;


--
-- Name: data_set_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

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


--
-- Name: deletion_description(tech_id); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION deletion_description(del_id tech_id) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
  del_person VARCHAR;
  del_date VARCHAR;
  del_reason VARCHAR;
BEGIN
  SELECT p.last_name || ' ' || p.first_name || ' (' || p.email || ')', 
         to_char(d.registration_timestamp, 'YYYY-MM-DD HH:MM:SS'), d.reason 
    INTO del_person, del_date, del_reason FROM deletions d, persons p 
    WHERE d.pers_id_registerer = p.id AND d.id = del_id;
  RETURN 'deleted by ' || del_person || ' on ' || del_date || ' with reason: "' || del_reason || '"';
END;
$$;


--
-- Name: disable_project_level_samples(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION disable_project_level_samples() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF (NEW.proj_id IS NOT NULL) THEN
    RAISE EXCEPTION 'Project level samples are disabled';
  END IF;
  
  RETURN NEW;
END;
$$;


--
-- Name: experiment_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

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


--
-- Name: external_data_storage_format_check(); Type: FUNCTION; Schema: public; Owner: -
--

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
      select code into data_code from data_all where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$;


--
-- Name: material_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

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


--
-- Name: preserve_deletion_consistency_on_data_set_relationships(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION preserve_deletion_consistency_on_data_set_relationships() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  delid  TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL OR OLD.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	SELECT del_id INTO delid
		FROM DATA_ALL where id = NEW.data_id_parent;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	SELECT del_id INTO delid
		FROM DATA_ALL where id = NEW.data_id_child;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	RETURN NEW;
END;
$$;


--
-- Name: preserve_deletion_consistency_on_sample_relationships(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION preserve_deletion_consistency_on_sample_relationships() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  delid  TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL OR OLD.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	SELECT del_id INTO delid
		FROM SAMPLES_ALL where id = NEW.sample_id_parent;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	SELECT del_id INTO delid
		FROM SAMPLES_ALL where id = NEW.sample_id_child;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	RETURN NEW;
END;
$$;


--
-- Name: rename_sequence(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

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


--
-- Name: sample_fill_code_unique_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_fill_code_unique_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.code_unique_check = NEW.code || ',' || coalesce(NEW.samp_id_part_of, -1) || ',' || coalesce(NEW.proj_id, -1) || ',' || coalesce(NEW.space_id, -1);
  RETURN NEW;
END;
$$;


--
-- Name: sample_fill_subcode_unique_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_fill_subcode_unique_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    unique_subcode  BOOLEAN_CHAR;
BEGIN
    SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
    
    IF (unique_subcode) THEN
    NEW.subcode_unique_check = NEW.code || ',' || coalesce(NEW.saty_id, -1) || ',' || coalesce(NEW.proj_id, -1) || ',' || coalesce(NEW.space_id, -1);
    ELSE
    NEW.subcode_unique_check = NULL;
  END IF;
  
  RETURN NEW;
END;
$$;


--
-- Name: sample_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

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


--
-- Name: sample_type_fill_subcode_unique_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_type_fill_subcode_unique_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF (NEW.is_subcode_unique::boolean <> OLD.is_subcode_unique::boolean) THEN
      UPDATE samples_all SET subcode_unique_check = subcode_unique_check WHERE saty_id = NEW.id;
  END IF;
    RETURN NEW;
END;
$$;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attachments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachments (
    id tech_id NOT NULL,
    expe_id tech_id,
    samp_id tech_id,
    proj_id tech_id,
    exac_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    title title_100,
    description description_2000,
    CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))))
);


--
-- Name: authorization_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE authorization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    code code NOT NULL,
    description description_2000,
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
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: controlled_vocabularies; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_chosen_from_list boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE controlled_vocabulary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    description description_2000,
    ordinal ordinal_int NOT NULL,
    is_official boolean_char DEFAULT true NOT NULL,
    CONSTRAINT cvte_ck CHECK (((ordinal)::bigint > 0))
);


--
-- Name: core_plugin_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE core_plugin_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_plugins; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE core_plugins (
    id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    version integer NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    master_reg_script text_value
);


--
-- Name: cvte_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cvte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_all; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_all (
    id tech_id NOT NULL,
    code code,
    dsty_id tech_id NOT NULL,
    dast_id tech_id NOT NULL,
    expe_id tech_id,
    data_producer_code code,
    production_timestamp time_stamp,
    samp_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id,
    is_valid boolean_char DEFAULT true,
    modification_timestamp time_stamp DEFAULT now(),
    access_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_derived boolean_char NOT NULL,
    del_id tech_id,
    orig_del tech_id,
    pers_id_modifier tech_id,
    version integer DEFAULT 0,
    CONSTRAINT data_ck CHECK (((expe_id IS NOT NULL) OR (samp_id IS NOT NULL)))
);


--
-- Name: data; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW data AS
 SELECT data_all.id,
    data_all.code,
    data_all.dsty_id,
    data_all.dast_id,
    data_all.expe_id,
    data_all.data_producer_code,
    data_all.production_timestamp,
    data_all.samp_id,
    data_all.registration_timestamp,
    data_all.access_timestamp,
    data_all.pers_id_registerer,
    data_all.pers_id_modifier,
    data_all.is_valid,
    data_all.modification_timestamp,
    data_all.is_derived,
    data_all.del_id,
    data_all.orig_del,
    data_all.version
   FROM data_all
  WHERE (data_all.del_id IS NULL);


--
-- Name: data_deleted; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW data_deleted AS
 SELECT data_all.id,
    data_all.code,
    data_all.dsty_id,
    data_all.dast_id,
    data_all.expe_id,
    data_all.data_producer_code,
    data_all.production_timestamp,
    data_all.samp_id,
    data_all.registration_timestamp,
    data_all.access_timestamp,
    data_all.pers_id_registerer,
    data_all.pers_id_modifier,
    data_all.is_valid,
    data_all.modification_timestamp,
    data_all.is_derived,
    data_all.del_id,
    data_all.orig_del,
    data_all.version
   FROM data_all
  WHERE (data_all.del_id IS NOT NULL);


--
-- Name: data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_properties_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_properties_history (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value text_value,
    vocabulary_term identifier,
    material identifier,
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp DEFAULT now(),
    CONSTRAINT dsprh_ck CHECK ((((((value IS NOT NULL) AND (vocabulary_term IS NULL)) AND (material IS NULL)) OR (((value IS NULL) AND (vocabulary_term IS NOT NULL)) AND (material IS NULL))) OR (((value IS NULL) AND (vocabulary_term IS NULL)) AND (material IS NOT NULL))))
);


--
-- Name: data_set_relationships_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_relationships_history (
    id tech_id NOT NULL,
    main_data_id tech_id NOT NULL,
    relation_type text_value,
    ordinal integer,
    expe_id tech_id,
    samp_id tech_id,
    data_id tech_id,
    entity_perm_id text_value,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp
);


--
-- Name: data_set_history_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW data_set_history_view AS
         SELECT (2 * (data_set_relationships_history.id)::bigint) AS id,
            data_set_relationships_history.main_data_id,
            data_set_relationships_history.relation_type,
            data_set_relationships_history.ordinal,
            data_set_relationships_history.expe_id,
            data_set_relationships_history.samp_id,
            data_set_relationships_history.data_id,
            data_set_relationships_history.entity_perm_id,
            NULL::bigint AS dstpt_id,
            NULL::text AS value,
            NULL::character varying AS vocabulary_term,
            NULL::character varying AS material,
            data_set_relationships_history.pers_id_author,
            data_set_relationships_history.valid_from_timestamp,
            data_set_relationships_history.valid_until_timestamp
           FROM data_set_relationships_history
          WHERE (data_set_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
         SELECT ((2 * (data_set_properties_history.id)::bigint) + 1) AS id,
            data_set_properties_history.ds_id AS main_data_id,
            NULL::text AS relation_type,
            NULL::integer AS ordinal,
            NULL::bigint AS expe_id,
            NULL::bigint AS samp_id,
            NULL::bigint AS data_id,
            NULL::text AS entity_perm_id,
            data_set_properties_history.dstpt_id,
            data_set_properties_history.value,
            data_set_properties_history.vocabulary_term,
            data_set_properties_history.material,
            data_set_properties_history.pers_id_author,
            data_set_properties_history.valid_from_timestamp,
            data_set_properties_history.valid_until_timestamp
           FROM data_set_properties_history;


--
-- Name: data_set_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_properties (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_author tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT dspr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: data_set_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_relationships_all; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_relationships_all (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL,
    relationship_id tech_id NOT NULL,
    ordinal integer,
    del_id tech_id,
    pers_id_author tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: data_set_relationships; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW data_set_relationships AS
 SELECT data_set_relationships_all.data_id_parent,
    data_set_relationships_all.data_id_child,
    data_set_relationships_all.relationship_id,
    data_set_relationships_all.ordinal,
    data_set_relationships_all.del_id,
    data_set_relationships_all.pers_id_author,
    data_set_relationships_all.registration_timestamp,
    data_set_relationships_all.modification_timestamp
   FROM data_set_relationships_all
  WHERE (data_set_relationships_all.del_id IS NULL);


--
-- Name: data_set_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL
);


--
-- Name: data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    modification_timestamp time_stamp DEFAULT now(),
    main_ds_pattern character varying(300),
    main_ds_path character varying(1000),
    deletion_disallow boolean_char DEFAULT false,
    data_set_kind data_set_kind DEFAULT 'PHYSICAL'::character varying NOT NULL,
    validation_script_id tech_id
);


--
-- Name: data_store_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    data_store_id tech_id NOT NULL,
    reporting_plugin_type data_store_service_reporting_plugin_type
);


--
-- Name: data_store_services_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_stores; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_stores (
    id tech_id NOT NULL,
    uuid code NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_archiver_configured boolean_char DEFAULT false NOT NULL,
    data_source_definitions text_value
);


--
-- Name: data_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000 NOT NULL
);


--
-- Name: database_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE database_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
-- Name: deletion_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE deletion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: deletions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE deletions (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_2000 NOT NULL
);


--
-- Name: dstpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dstpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entity_operations_log; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entity_operations_log (
    id tech_id NOT NULL,
    registration_id tech_id NOT NULL
);


--
-- Name: entity_operations_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE entity_operations_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: etpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE etpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE events (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    description text_value,
    reason description_2000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    entity_type character varying(80) NOT NULL,
    identifiers text_value NOT NULL,
    content text_value,
    exac_id tech_id,
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY ((ARRAY['ATTACHMENT'::character varying, 'DATASET'::character varying, 'EXPERIMENT'::character varying, 'SPACE'::character varying, 'MATERIAL'::character varying, 'PROJECT'::character varying, 'PROPERTY_TYPE'::character varying, 'SAMPLE'::character varying, 'VOCABULARY'::character varying, 'AUTHORIZATION_GROUP'::character varying, 'METAPROJECT'::character varying])::text[])))
);


--
-- Name: experiment_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_properties_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_properties_history (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value text_value,
    vocabulary_term identifier,
    material identifier,
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp DEFAULT now(),
    CONSTRAINT exprh_ck CHECK ((((((value IS NOT NULL) AND (vocabulary_term IS NULL)) AND (material IS NULL)) OR (((value IS NULL) AND (vocabulary_term IS NOT NULL)) AND (material IS NULL))) OR (((value IS NULL) AND (vocabulary_term IS NULL)) AND (material IS NOT NULL))))
);


--
-- Name: experiment_relationships_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_relationships_history (
    id tech_id NOT NULL,
    main_expe_id tech_id NOT NULL,
    relation_type text_value,
    samp_id tech_id,
    data_id tech_id,
    entity_perm_id text_value,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp,
    proj_id tech_id
);


--
-- Name: experiment_history_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW experiment_history_view AS
         SELECT (2 * (experiment_relationships_history.id)::bigint) AS id,
            experiment_relationships_history.main_expe_id,
            experiment_relationships_history.relation_type,
            experiment_relationships_history.proj_id,
            experiment_relationships_history.samp_id,
            experiment_relationships_history.data_id,
            experiment_relationships_history.entity_perm_id,
            NULL::bigint AS etpt_id,
            NULL::text AS value,
            NULL::character varying AS vocabulary_term,
            NULL::character varying AS material,
            experiment_relationships_history.pers_id_author,
            experiment_relationships_history.valid_from_timestamp,
            experiment_relationships_history.valid_until_timestamp
           FROM experiment_relationships_history
          WHERE (experiment_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
         SELECT ((2 * (experiment_properties_history.id)::bigint) + 1) AS id,
            experiment_properties_history.expe_id AS main_expe_id,
            NULL::text AS relation_type,
            NULL::bigint AS proj_id,
            NULL::bigint AS samp_id,
            NULL::bigint AS data_id,
            NULL::text AS entity_perm_id,
            experiment_properties_history.etpt_id,
            experiment_properties_history.value,
            experiment_properties_history.vocabulary_term,
            experiment_properties_history.material,
            experiment_properties_history.pers_id_author,
            experiment_properties_history.valid_from_timestamp,
            experiment_properties_history.valid_until_timestamp
           FROM experiment_properties_history;


--
-- Name: experiment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_author tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT expr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: experiment_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL
);


--
-- Name: experiment_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    modification_timestamp time_stamp DEFAULT now(),
    validation_script_id tech_id
);


--
-- Name: experiments_all; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiments_all (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    proj_id tech_id NOT NULL,
    del_id tech_id,
    orig_del tech_id,
    is_public boolean_char DEFAULT false NOT NULL,
    pers_id_modifier tech_id,
    version integer DEFAULT 0
);


--
-- Name: experiments; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW experiments AS
 SELECT experiments_all.id,
    experiments_all.perm_id,
    experiments_all.code,
    experiments_all.exty_id,
    experiments_all.pers_id_registerer,
    experiments_all.pers_id_modifier,
    experiments_all.registration_timestamp,
    experiments_all.modification_timestamp,
    experiments_all.proj_id,
    experiments_all.del_id,
    experiments_all.orig_del,
    experiments_all.is_public,
    experiments_all.version
   FROM experiments_all
  WHERE (experiments_all.del_id IS NULL);


--
-- Name: experiments_deleted; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW experiments_deleted AS
 SELECT experiments_all.id,
    experiments_all.perm_id,
    experiments_all.code,
    experiments_all.exty_id,
    experiments_all.pers_id_registerer,
    experiments_all.pers_id_modifier,
    experiments_all.registration_timestamp,
    experiments_all.modification_timestamp,
    experiments_all.proj_id,
    experiments_all.del_id,
    experiments_all.orig_del,
    experiments_all.is_public,
    experiments_all.version
   FROM experiments_all
  WHERE (experiments_all.del_id IS NOT NULL);


--
-- Name: external_data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    share_id code,
    size ordinal_int,
    location character varying(1024) NOT NULL,
    ffty_id tech_id NOT NULL,
    loty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store tech_id,
    status archiving_status DEFAULT 'AVAILABLE'::character varying NOT NULL,
    present_in_archive boolean_char DEFAULT false,
    speed_hint integer DEFAULT (-50) NOT NULL,
    storage_confirmation boolean_char DEFAULT false NOT NULL
);


--
-- Name: external_data_management_system_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE external_data_management_system_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: external_data_management_systems; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE external_data_management_systems (
    id tech_id NOT NULL,
    code code,
    label text_value,
    url_template text_value,
    is_openbis boolean DEFAULT false NOT NULL
);


--
-- Name: file_format_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE file_format_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: file_format_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000
);


--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: filters; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE filters (
    id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression text NOT NULL,
    is_public boolean NOT NULL,
    grid_id character varying(200) NOT NULL
);


--
-- Name: grid_custom_columns; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE grid_custom_columns (
    id tech_id NOT NULL,
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


--
-- Name: grid_custom_columns_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE grid_custom_columns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE link_data (
    data_id tech_id NOT NULL,
    edms_id tech_id NOT NULL,
    external_code text_value NOT NULL
);


--
-- Name: locator_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE locator_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: locator_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000
);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value text_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_author tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    mate_prop_id tech_id,
    CONSTRAINT mapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: material_properties_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_properties_history (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value text_value,
    vocabulary_term identifier,
    material identifier,
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp DEFAULT now(),
    CONSTRAINT maprh_ck CHECK ((((((value IS NOT NULL) AND (vocabulary_term IS NULL)) AND (material IS NULL)) OR (((value IS NULL) AND (vocabulary_term IS NOT NULL)) AND (material IS NULL))) OR (((value IS NULL) AND (vocabulary_term IS NULL)) AND (material IS NOT NULL))))
);


--
-- Name: material_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    pers_id_registerer tech_id NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL
);


--
-- Name: material_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    modification_timestamp time_stamp DEFAULT now(),
    validation_script_id tech_id
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
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: metaproject_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE metaproject_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: metaproject_assignments_all; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE metaproject_assignments_all (
    id tech_id NOT NULL,
    mepr_id tech_id NOT NULL,
    expe_id tech_id,
    samp_id tech_id,
    data_id tech_id,
    mate_id tech_id,
    del_id tech_id,
    creation_date time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT metaproject_assignments_all_check_nn CHECK ((((((((expe_id IS NOT NULL) AND (samp_id IS NULL)) AND (data_id IS NULL)) AND (mate_id IS NULL)) OR ((((expe_id IS NULL) AND (samp_id IS NOT NULL)) AND (data_id IS NULL)) AND (mate_id IS NULL))) OR ((((expe_id IS NULL) AND (samp_id IS NULL)) AND (data_id IS NOT NULL)) AND (mate_id IS NULL))) OR ((((expe_id IS NULL) AND (samp_id IS NULL)) AND (data_id IS NULL)) AND (mate_id IS NOT NULL))))
);


--
-- Name: metaproject_assignments; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW metaproject_assignments AS
 SELECT metaproject_assignments_all.id,
    metaproject_assignments_all.mepr_id,
    metaproject_assignments_all.expe_id,
    metaproject_assignments_all.samp_id,
    metaproject_assignments_all.data_id,
    metaproject_assignments_all.mate_id,
    metaproject_assignments_all.del_id,
    metaproject_assignments_all.creation_date
   FROM metaproject_assignments_all
  WHERE (metaproject_assignments_all.del_id IS NULL);


--
-- Name: metaproject_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE metaproject_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: metaprojects; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE metaprojects (
    id tech_id NOT NULL,
    name code NOT NULL,
    description description_2000,
    owner tech_id NOT NULL,
    private boolean_char DEFAULT true NOT NULL,
    creation_date time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: mtpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mtpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operation_executions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE operation_executions (
    id tech_id NOT NULL,
    code code NOT NULL,
    state operation_execution_state NOT NULL,
    description text_value NOT NULL,
    error text_value,
    creation_date time_stamp_dfl,
    start_date time_stamp,
    finish_date time_stamp,
    CONSTRAINT operation_executions_state_error_check CHECK (((((state)::text <> 'FAILED'::text) AND (error IS NULL)) OR (((state)::text = 'FAILED'::text) AND (error IS NOT NULL)))),
    CONSTRAINT operation_executions_state_finish_date_check CHECK (((((state)::text = ANY ((ARRAY['NEW'::character varying, 'SCHEDULED'::character varying, 'RUNNING'::character varying])::text[])) AND (finish_date IS NULL)) OR (((state)::text = ANY ((ARRAY['FINISHED'::character varying, 'FAILED'::character varying])::text[])) AND (finish_date IS NOT NULL)))),
    CONSTRAINT operation_executions_state_start_date_check CHECK (((((state)::text = ANY ((ARRAY['NEW'::character varying, 'SCHEDULED'::character varying])::text[])) AND (start_date IS NULL)) OR (((state)::text = ANY ((ARRAY['RUNNING'::character varying, 'FINISHED'::character varying, 'FAILED'::character varying])::text[])) AND (start_date IS NOT NULL))))
);


--
-- Name: operation_executions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE operation_executions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: perm_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE perm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: persons; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id user_id NOT NULL,
    email object_name,
    space_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id,
    display_settings file,
    is_active boolean DEFAULT true
);


--
-- Name: post_registration_dataset_queue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE post_registration_dataset_queue (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL
);


--
-- Name: post_registration_dataset_queue_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE post_registration_dataset_queue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: project_relationships_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE project_relationships_history (
    id tech_id NOT NULL,
    main_proj_id tech_id NOT NULL,
    relation_type text_value,
    expe_id tech_id,
    space_id tech_id,
    entity_perm_id text_value,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp
);


--
-- Name: project_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE project_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projects (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    space_id tech_id NOT NULL,
    pers_id_leader tech_id,
    description text_value,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_modifier tech_id,
    version integer DEFAULT 0
);


--
-- Name: property_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE property_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

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
    maty_prop_id tech_id,
    schema text_value,
    transformation text_value
);


--
-- Name: queries; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE queries (
    id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression text NOT NULL,
    is_public boolean NOT NULL,
    query_type query_type NOT NULL,
    entity_type_code code,
    db_key code DEFAULT '1'::character varying NOT NULL
);


--
-- Name: query_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: relationship_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE relationship_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: relationship_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE relationship_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    label column_label,
    parent_label column_label,
    child_label column_label,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL
);


--
-- Name: role_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE role_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_assignments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE role_assignments (
    id tech_id NOT NULL,
    role_code authorization_role NOT NULL,
    space_id tech_id,
    pers_id_grantee tech_id,
    ag_id_grantee tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT roas_ag_pers_arc_ck CHECK ((((ag_id_grantee IS NOT NULL) AND (pers_id_grantee IS NULL)) OR ((ag_id_grantee IS NULL) AND (pers_id_grantee IS NOT NULL))))
);


--
-- Name: sample_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_properties_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_properties_history (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value text_value,
    vocabulary_term identifier,
    material identifier,
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp DEFAULT now(),
    CONSTRAINT saprh_ck CHECK ((((((value IS NOT NULL) AND (vocabulary_term IS NULL)) AND (material IS NULL)) OR (((value IS NULL) AND (vocabulary_term IS NOT NULL)) AND (material IS NULL))) OR (((value IS NULL) AND (vocabulary_term IS NULL)) AND (material IS NOT NULL))))
);


--
-- Name: sample_relationships_history; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_relationships_history (
    id tech_id NOT NULL,
    main_samp_id tech_id NOT NULL,
    relation_type text_value,
    expe_id tech_id,
    samp_id tech_id,
    data_id tech_id,
    entity_perm_id text_value,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp,
    space_id tech_id
);


--
-- Name: sample_history_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW sample_history_view AS
         SELECT (2 * (sample_relationships_history.id)::bigint) AS id,
            sample_relationships_history.main_samp_id,
            sample_relationships_history.relation_type,
            sample_relationships_history.space_id,
            sample_relationships_history.expe_id,
            sample_relationships_history.samp_id,
            sample_relationships_history.data_id,
            sample_relationships_history.entity_perm_id,
            NULL::bigint AS stpt_id,
            NULL::text AS value,
            NULL::character varying AS vocabulary_term,
            NULL::character varying AS material,
            sample_relationships_history.pers_id_author,
            sample_relationships_history.valid_from_timestamp,
            sample_relationships_history.valid_until_timestamp
           FROM sample_relationships_history
          WHERE (sample_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
         SELECT ((2 * (sample_properties_history.id)::bigint) + 1) AS id,
            sample_properties_history.samp_id AS main_samp_id,
            NULL::text AS relation_type,
            NULL::bigint AS space_id,
            NULL::bigint AS expe_id,
            NULL::bigint AS samp_id,
            NULL::bigint AS data_id,
            NULL::text AS entity_perm_id,
            sample_properties_history.stpt_id,
            sample_properties_history.value,
            sample_properties_history.vocabulary_term,
            sample_properties_history.material,
            sample_properties_history.pers_id_author,
            sample_properties_history.valid_from_timestamp,
            sample_properties_history.valid_until_timestamp
           FROM sample_properties_history;


--
-- Name: sample_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_author tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT sapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: sample_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_relationships_all; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_relationships_all (
    id tech_id NOT NULL,
    sample_id_parent tech_id NOT NULL,
    relationship_id tech_id NOT NULL,
    sample_id_child tech_id NOT NULL,
    del_id tech_id,
    pers_id_author tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: sample_relationships; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW sample_relationships AS
 SELECT sample_relationships_all.id,
    sample_relationships_all.sample_id_parent,
    sample_relationships_all.relationship_id,
    sample_relationships_all.sample_id_child,
    sample_relationships_all.del_id,
    sample_relationships_all.pers_id_author,
    sample_relationships_all.registration_timestamp,
    sample_relationships_all.modification_timestamp
   FROM sample_relationships_all
  WHERE (sample_relationships_all.del_id IS NULL);


--
-- Name: sample_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


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
    is_displayed boolean_char DEFAULT true NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL
);


--
-- Name: sample_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    is_listable boolean_char DEFAULT true NOT NULL,
    generated_from_depth integer DEFAULT 0 NOT NULL,
    part_of_depth integer DEFAULT 0 NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_auto_generated_code boolean_char DEFAULT false NOT NULL,
    generated_code_prefix code DEFAULT 'S'::character varying NOT NULL,
    is_subcode_unique boolean_char DEFAULT false NOT NULL,
    inherit_properties boolean_char DEFAULT false NOT NULL,
    validation_script_id tech_id,
    show_parent_metadata boolean_char DEFAULT false NOT NULL
);


--
-- Name: samples_all; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE samples_all (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    expe_id tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_registerer tech_id NOT NULL,
    del_id tech_id,
    orig_del tech_id,
    space_id tech_id,
    samp_id_part_of tech_id,
    pers_id_modifier tech_id,
    code_unique_check character varying(300),
    subcode_unique_check character varying(300),
    version integer DEFAULT 0,
    proj_id tech_id
);


--
-- Name: samples; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW samples AS
 SELECT samples_all.id,
    samples_all.perm_id,
    samples_all.code,
    samples_all.proj_id,
    samples_all.expe_id,
    samples_all.saty_id,
    samples_all.registration_timestamp,
    samples_all.modification_timestamp,
    samples_all.pers_id_registerer,
    samples_all.pers_id_modifier,
    samples_all.del_id,
    samples_all.orig_del,
    samples_all.space_id,
    samples_all.samp_id_part_of,
    samples_all.version
   FROM samples_all
  WHERE (samples_all.del_id IS NULL);


--
-- Name: samples_deleted; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW samples_deleted AS
 SELECT samples_all.id,
    samples_all.perm_id,
    samples_all.code,
    samples_all.expe_id,
    samples_all.saty_id,
    samples_all.registration_timestamp,
    samples_all.modification_timestamp,
    samples_all.pers_id_registerer,
    samples_all.pers_id_modifier,
    samples_all.del_id,
    samples_all.orig_del,
    samples_all.space_id,
    samples_all.samp_id_part_of,
    samples_all.version
   FROM samples_all
  WHERE (samples_all.del_id IS NOT NULL);


--
-- Name: script_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: scripts; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE scripts (
    id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    script_type script_type NOT NULL,
    description description_2000,
    script text_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    entity_kind entity_kind,
    plugin_type plugin_type DEFAULT 'JYTHON'::character varying NOT NULL,
    is_available boolean_char DEFAULT true NOT NULL,
    CONSTRAINT script_nn_ck CHECK ((((plugin_type)::text = 'PREDEPLOYED'::text) OR (script IS NOT NULL)))
);


--
-- Name: space_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE space_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: spaces; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE spaces (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: stpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE stpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_content_id_seq', 1, false);


--
-- Data for Name: attachment_contents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachment_contents (id, value) FROM stdin;
\.


--
-- Name: attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_id_seq', 1, false);


--
-- Data for Name: attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachments (id, expe_id, samp_id, proj_id, exac_id, file_name, registration_timestamp, version, pers_id_registerer, title, description) FROM stdin;
\.


--
-- Name: authorization_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('authorization_group_id_seq', 1, false);


--
-- Data for Name: authorization_group_persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY authorization_group_persons (ag_id, pers_id) FROM stdin;
\.


--
-- Data for Name: authorization_groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY authorization_groups (id, code, description, registration_timestamp, pers_id_registerer, modification_timestamp) FROM stdin;
\.


--
-- Name: code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('code_seq', 1, false);


--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
1	STORAGE_FORMAT	The on-disk storage format of a data set	2016-04-20 11:16:39.786005+02	1	t	t	2016-04-20 11:16:39.786005+02	t	\N
\.


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 1, true);


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer, label, description, ordinal, is_official) FROM stdin;
1	PROPRIETARY	2016-04-20 11:16:39.786005+02	1	1	\N	\N	1	t
2	BDS_DIRECTORY	2016-04-20 11:16:39.786005+02	1	1	\N	\N	2	t
\.


--
-- Name: core_plugin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('core_plugin_id_seq', 1, false);


--
-- Data for Name: core_plugins; Type: TABLE DATA; Schema: public; Owner: -
--

COPY core_plugins (id, name, version, registration_timestamp, master_reg_script) FROM stdin;
\.


--
-- Name: cvte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cvte_id_seq', 2, true);


--
-- Data for Name: data_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_all (id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, is_valid, modification_timestamp, access_timestamp, is_derived, del_id, orig_del, pers_id_modifier, version) FROM stdin;
\.


--
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_id_seq', 1, false);


--
-- Data for Name: data_set_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_properties (id, ds_id, dstpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, pers_id_author, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Name: data_set_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_property_id_seq', 1, false);


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);


--
-- Data for Name: data_set_relationships_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_relationships_all (data_id_parent, data_id_child, relationship_id, ordinal, del_id, pers_id_author, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_relationships_history (id, main_data_id, relation_type, ordinal, expe_id, samp_id, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Name: data_set_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_relationships_history_id_seq', 1, false);


--
-- Name: data_set_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_type_id_seq', 1, true);


--
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_types (id, code, description, modification_timestamp, main_ds_pattern, main_ds_path, deletion_disallow, data_set_kind, validation_script_id) FROM stdin;
1	UNKNOWN	Unknown	2016-04-20 11:16:39.786005+02	\N	\N	f	PHYSICAL	\N
\.


--
-- Name: data_store_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_id_seq', 1, false);


--
-- Data for Name: data_store_service_data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_store_service_data_set_types (data_store_service_id, data_set_type_id) FROM stdin;
\.


--
-- Data for Name: data_store_services; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_store_services (id, key, label, kind, data_store_id, reporting_plugin_type) FROM stdin;
\.


--
-- Name: data_store_services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_services_id_seq', 1, false);


--
-- Data for Name: data_stores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_stores (id, uuid, code, download_url, remote_url, session_token, registration_timestamp, modification_timestamp, is_archiver_configured, data_source_definitions) FROM stdin;
\.


--
-- Name: data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_type_id_seq', 10, true);


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_types (id, code, description) FROM stdin;
1	VARCHAR	Short text
2	MULTILINE_VARCHAR	Long text
3	INTEGER	Integer number
4	REAL	Real number, i.e. an inexact, variable-precision numeric type
5	BOOLEAN	True or False
6	TIMESTAMP	Both date and time. Format: yyyy-mm-dd hh:mm:ss
7	CONTROLLEDVOCABULARY	Controlled Vocabulary
8	MATERIAL	Reference to a material
9	HYPERLINK	Address of a web page
10	XML	XML document
\.


--
-- Name: database_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('database_instance_id_seq', 1, false);


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
154	./sql/postgresql/154/domains-154.sql	SUCCESS	2016-04-20 11:16:38.83	\\x2d2d204372656174696e6720646f6d61696e730a0a43524541544520444f4d41494e20415554484f52495a4154494f4e5f524f4c4520415320564152434841522834302920434845434b202856414c554520494e20282741444d494e272c2027504f5745525f55534552272c202755534552272c20274f42534552564552272c202745544c5f5345525645522729293b0a43524541544520444f4d41494e20424f4f4c45414e5f4348415220415320424f4f4c45414e2044454641554c542046414c53453b0a43524541544520444f4d41494e20424f4f4c45414e5f434841525f4f525f554e4b4e4f574e20415320434841522831292044454641554c542027552720434845434b202856414c554520494e20282746272c202754272c2027552729293b0a43524541544520444f4d41494e20434f44452041532056415243484152283630293b0a43524541544520444f4d41494e20434f4c554d4e5f4c4142454c204153205641524348415228313238293b0a43524541544520444f4d41494e20444154415f53544f52455f534552564943455f4b494e4420415320564152434841522834302920434845434b202856414c554520494e20282750524f43455353494e47272c2027515545524945532729293b0a43524541544520444f4d41494e20444154415f53544f52455f534552564943455f5245504f5254494e475f504c5547494e5f5459504520415320564152434841522834302920434845434b202856414c554520494e2028275441424c455f4d4f44454c272c20274453535f4c494e4b272c20274147475245474154494f4e5f5441424c455f4d4f44454c2729293b0a43524541544520444f4d41494e204556454e545f5459504520415320564152434841522834302920434845434b202856414c554520494e20282744454c4554494f4e272c20274d4f56454d454e542729293b0a43524541544520444f4d41494e2046494c452041532042595445413b0a43524541544520444f4d41494e2046494c455f4e414d45204153205641524348415228323535293b0a43524541544520444f4d41494e20544558545f56414c554520415320544558543b0a43524541544520444f4d41494e204f424a4543545f4e414d452041532056415243484152283530293b0a43524541544520444f4d41494e205245414c5f56414c5545204153205245414c3b0a43524541544520444f4d41494e20544543485f494420415320424947494e543b0a43524541544520444f4d41494e2054494d455f5354414d502041532054494d455354414d5020574954482054494d45205a4f4e453b0a43524541544520444f4d41494e2054494d455f5354414d505f44464c2041532054494d455354414d5020574954482054494d45205a4f4e45204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d503b0a43524541544520444f4d41494e20555345525f49442041532056415243484152283530293b0a43524541544520444f4d41494e205449544c455f313030204153205641524348415228313030293b0a43524541544520444f4d41494e20475249445f45585052455353494f4e20415320564152434841522832303030293b0a43524541544520444f4d41494e20475249445f4944204153205641524348415228323030293b0a43524541544520444f4d41494e204f5244494e414c5f494e5420415320424947494e5420434845434b202856414c5545203e2030293b0a43524541544520444f4d41494e204445534352495054494f4e5f3230303020415320564152434841522832303030293b0a43524541544520444f4d41494e20415243484956494e475f5354415455532041532056415243484152283130302920434845434b202856414c554520494e2028274c4f434b4544272c2027415641494c41424c45272c20274152434849564544272c2027415243484956455f50454e44494e47272c2027554e415243484956455f50454e44494e47272c20274241434b55505f50454e44494e472729293b0a43524541544520444f4d41494e2051554552595f5459504520415320564152434841522834302920434845434b202856414c554520494e20282747454e45524943272c20274558504552494d454e54272c202753414d504c45272c2027444154415f534554272c20274d4154455249414c2729293b0a43524541544520444f4d41494e20454e544954595f4b494e4420415320564152434841522834302920434845434b202856414c554520494e20282753414d504c45272c20274558504552494d454e54272c2027444154415f534554272c20274d4154455249414c2729293b0a43524541544520444f4d41494e205343524950545f5459504520415320564152434841522834302920434845434b202856414c554520494e20282744594e414d49435f50524f5045525459272c20274d414e414745445f50524f5045525459272c2027454e544954595f56414c49444154494f4e2729293b0a43524541544520444f4d41494e204944454e544946494552204153205641524348415228323030293b0a43524541544520444f4d41494e20444154415f5345545f4b494e4420415320564152434841522834302920434845434b202856414c554520494e202827504859534943414c272c20274c494e4b272c2027434f4e5441494e45522729293b0a43524541544520444f4d41494e20504c5547494e5f5459504520415320564152434841522834302920434845434b202856414c554520494e2028274a5954484f4e272c20275052454445504c4f5945442729293b0a43524541544520444f4d41494e204f5045524154494f4e5f455845435554494f4e5f535441544520415320564152434841522834302920434845434b202856414c554520494e2028274e4557272c20275343484544554c4544272c202752554e4e494e47272c202746494e4953484544272c20274641494c45442729293b0a	\N
154	./sql/generic/154/schema-154.sql	SUCCESS	2016-04-20 11:16:39.586	\\x2d2d204372656174696e67207461626c65730a0a435245415445205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f494e5445524e414c5f4e414d45535041434520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2049535f43484f53454e5f46524f4d5f4c49535420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420545255452c20534f555243455f555249204348415241435445522056415259494e472832353029293b0a435245415445205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532028494420544543485f4944204e4f54204e554c4c2c434f4445204f424a4543545f4e414d45204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c434f564f5f494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c4c4142454c20434f4c554d4e5f4c4142454c2c204445534352495054494f4e204445534352495054494f4e5f323030302c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2049535f4f4646494349414c20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420275427293b0a435245415445205441424c4520444154415f414c4c2028494420544543485f4944204e4f54204e554c4c2c434f444520434f44452c445354595f494420544543485f4944204e4f54204e554c4c2c444153545f494420544543485f4944204e4f54204e554c4c2c455850455f494420544543485f49442c444154415f50524f44554345525f434f444520434f44452c50524f44554354494f4e5f54494d455354414d502054494d455f5354414d502c53414d505f494420544543485f49442c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f49442c49535f56414c494420424f4f4c45414e5f434841522044454641554c54202754272c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c204143434553535f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c2049535f4445524956454420424f4f4c45414e5f43484152204e4f54204e554c4c2c2044454c5f494420544543485f49442c204f5249475f44454c20544543485f49442c20504552535f49445f4d4f44494649455220544543485f49442c2056455253494f4e20494e54454745522044454641554c542030293b0a435245415445205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2028444154415f49445f504152454e5420544543485f4944204e4f54204e554c4c2c444154415f49445f4348494c4420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e534849505f494420544543485f4944204e4f54204e554c4c2c204f5244494e414c20494e54454745522c2044454c5f494420544543485f49442c20504552535f49445f415554484f5220544543485f49442c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c4520444154415f53544f5245532028494420544543485f4944204e4f54204e554c4c2c5555494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c444f574e4c4f41445f55524c2056415243484152283130323429204e4f54204e554c4c2c52454d4f54455f55524c20564152434841522832353029204e4f54204e554c4c2c53455353494f4e5f544f4b454e205641524348415228353029204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c4d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2049535f41524348495645525f434f4e4649475552454420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20444154415f534f555243455f444546494e4954494f4e5320544558545f56414c5545293b0a435245415445205441424c4520444154415f53544f52455f5345525649434553202028494420544543485f4944204e4f54204e554c4c2c204b455920564152434841522832353629204e4f54204e554c4c2c204c4142454c20564152434841522832353629204e4f54204e554c4c2c204b494e4420444154415f53544f52455f534552564943455f4b494e44204e4f54204e554c4c2c20444154415f53544f52455f494420544543485f4944204e4f54204e554c4c2c205245504f5254494e475f504c5547494e5f5459504520444154415f53544f52455f534552564943455f5245504f5254494e475f504c5547494e5f54595045293b0a435245415445205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532028444154415f53544f52455f534552564943455f494420544543485f4944204e4f54204e554c4c2c20444154415f5345545f545950455f494420544543485f4944204e4f54204e554c4c293b0a435245415445205441424c4520444154415f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030204e4f54204e554c4c293b0a435245415445205441424c45204556454e54532028494420544543485f4944204e4f54204e554c4c2c4556454e545f54595045204556454e545f54595045204e4f54204e554c4c2c4445534352495054494f4e20544558545f56414c55452c524541534f4e204445534352495054494f4e5f323030302c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20656e746974795f74797065205641524348415228383029204e4f54204e554c4c2c206964656e7469666965727320544558545f56414c5545204e4f54204e554c4c2c20434f4e54454e5420544558545f56414c55452c20455841435f494420544543485f4944293b0a435245415445205441424c45204558504552494d454e54535f414c4c2028494420544543485f4944204e4f54204e554c4c2c5045524d5f494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c455854595f494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2050524f4a5f494420544543485f4944204e4f54204e554c4c2c44454c5f494420544543485f49442c204f5249475f44454c20544543485f49442c2049535f5055424c494320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20504552535f49445f4d4f44494649455220544543485f49442c2056455253494f4e20494e54454745522044454641554c542030293b0a435245415445205441424c45204154544143484d454e54532028494420544543485f4944204e4f54204e554c4c2c455850455f494420544543485f49442c53414d505f494420544543485f49442c50524f4a5f494420544543485f49442c455841435f494420544543485f4944204e4f54204e554c4c2c46494c455f4e414d452046494c455f4e414d45204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c56455253494f4e20494e5445474552204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c207469746c65205449544c455f3130302c206465736372697074696f6e204445534352495054494f4e5f32303030293b0a435245415445205441424c45204154544143484d454e545f434f4e54454e54532028494420544543485f4944204e4f54204e554c4c2c56414c55452046494c45204e4f54204e554c4c293b0a435245415445205441424c45204558504552494d454e545f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c455850455f494420544543485f4944204e4f54204e554c4c2c455450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c435654455f494420544543485f49442c204d4154455f50524f505f494420544543485f49442c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c20455850455f494420544543485f4944204e4f54204e554c4c2c20455450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204558504552494d454e545f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2056414c49444154494f4e5f5343524950545f494420544543485f4944293b0a435245415445205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c455854595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c49535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452045585445524e414c5f444154412028444154415f494420544543485f4944204e4f54204e554c4c2c53484152455f494420434f44452c53495a45204f5244494e414c5f494e542c4c4f434154494f4e2056415243484152283130323429204e4f54204e554c4c2c464654595f494420544543485f4944204e4f54204e554c4c2c4c4f54595f494420544543485f4944204e4f54204e554c4c2c435654455f49445f53544f525f464d5420544543485f4944204e4f54204e554c4c2c49535f434f4d504c45544520424f4f4c45414e5f434841525f4f525f554e4b4e4f574e204e4f54204e554c4c2044454641554c54202755272c435654455f49445f53544f524520544543485f49442c2053544154555320415243484956494e475f535441545553204e4f54204e554c4c2044454641554c542027415641494c41424c45272c2050524553454e545f494e5f4152434849564520424f4f4c45414e5f434841522044454641554c54202746272c2053504545445f48494e5420494e5445474552204e4f54204e554c4c2044454641554c54202d35302c2053544f524147455f434f4e4649524d4154494f4e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452046494c455f464f524d41545f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030293b0a435245415445205441424c4520475249445f435553544f4d5f434f4c554d4e532028494420544543485f4944204e4f54204e554c4c2c20434f444520564152434841522832303029204e4f54204e554c4c2c204c4142454c20636f6c756d6e5f6c6162656c204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2045585052455353494f4e20475249445f45585052455353494f4e204e4f54204e554c4c2c2049535f5055424c494320424f4f4c45414e204e4f54204e554c4c2c20475249445f494420475249445f4944204e4f54204e554c4c293b0a435245415445205441424c45205350414345532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c293b0a435245415445205441424c452044454c4554494f4e532028494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c524541534f4e204445534352495054494f4e5f32303030204e4f54204e554c4c293b0a435245415445205441424c45204c4f4341544f525f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030293b0a435245415445205441424c45204d4154455249414c532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4d4154595f494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204d4154455249414c5f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c4d4154455f494420544543485f4944204e4f54204e554c4c2c4d5450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c435654455f494420544543485f49442c204d4154455f50524f505f494420544543485f4944293b0a435245415445205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d4154455f494420544543485f4944204e4f54204e554c4c2c204d5450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204d4154455249414c5f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2056414c49444154494f4e5f5343524950545f494420544543485f4944293b0a435245415445205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c4d4154595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c49535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c4520444154415f5345545f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c204d41494e5f44535f5041545445524e205641524348415228333030292c204d41494e5f44535f5041544820564152434841522831303030292c2044454c4554494f4e5f444953414c4c4f5720424f4f4c45414e5f434841522044454641554c54202746272c20444154415f5345545f4b494e4420444154415f5345545f4b494e442044454641554c542027504859534943414c27204e4f54204e554c4c2c2056414c49444154494f4e5f5343524950545f494420544543485f4944293b0a435245415445205441424c4520504552534f4e532028494420544543485f4944204e4f54204e554c4c2c46495253545f4e414d452056415243484152283330292c4c4153545f4e414d452056415243484152283330292c555345525f494420555345525f4944204e4f54204e554c4c2c454d41494c204f424a4543545f4e414d452c53504143455f494420544543485f49442c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f49442c20444953504c41595f53455454494e47532046494c452c2049535f41435449564520424f4f4c45414e2044454641554c542054525545293b0a435245415445205441424c452050524f4a454354532028494420544543485f4944204e4f54204e554c4c2c5045524d5f494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c53504143455f494420544543485f4944204e4f54204e554c4c2c504552535f49445f4c454144455220544543485f49442c4445534352495054494f4e20544558545f56414c55452c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f4d4f44494649455220544543485f49442c2056455253494f4e20494e54454745522044454641554c542030293b0a435245415445205441424c452050524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030204e4f54204e554c4c2c4c4142454c20434f4c554d4e5f4c4142454c204e4f54204e554c4c2c444154595f494420544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c434f564f5f494420544543485f49442c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f494e5445524e414c5f4e414d45535041434520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c204d4154595f50524f505f494420544543485f49442c20534348454d4120544558545f56414c55452c205452414e53464f524d4154494f4e20544558545f56414c5545293b0a435245415445205441424c4520524f4c455f41535349474e4d454e54532028494420544543485f4944204e4f54204e554c4c2c524f4c455f434f444520415554484f52495a4154494f4e5f524f4c45204e4f54204e554c4c2c53504143455f494420544543485f49442c504552535f49445f4752414e54454520544543485f49442c2041475f49445f4752414e54454520544543485f49442c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c452053414d504c45535f414c4c2028494420544543485f4944204e4f54204e554c4c2c5045524d5f494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c20455850455f494420544543485f49442c534154595f494420544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c4d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c44454c5f494420544543485f49442c204f5249475f44454c20544543485f49442c2053504143455f494420544543485f49442c2053414d505f49445f504152545f4f4620544543485f49442c20504552535f49445f4d4f44494649455220544543485f49442c20636f64655f756e697175655f636865636b206368617261637465722076617279696e6728333030292c20737562636f64655f756e697175655f636865636b206368617261637465722076617279696e6728333030292c2056455253494f4e20494e54454745522044454641554c5420302c2050524f4a5f494420544543485f4944293b0a435245415445205441424c452053414d504c455f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c53414d505f494420544543485f4944204e4f54204e554c4c2c535450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c435654455f494420544543485f49442c4d4154455f50524f505f494420544543485f49442c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c452053414d504c455f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c2053414d505f494420544543485f4944204e4f54204e554c4c2c20535450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c452053414d504c455f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c2049535f4c49535441424c4520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c2047454e4552415445445f46524f4d5f444550544820494e5445474552204e4f54204e554c4c2044454641554c5420302c20504152545f4f465f444550544820494e5445474552204e4f54204e554c4c2044454641554c5420302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2069735f6175746f5f67656e6572617465645f636f646520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2067656e6572617465645f636f64655f70726566697820434f4445204e4f54204e554c4c2044454641554c54202753272c2069735f737562636f64655f756e6971756520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20494e48455249545f50524f5045525449455320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2056414c49444154494f4e5f5343524950545f494420544543485f49442c2053484f575f504152454e545f4d4554414441544120424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452053414d504c455f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c534154595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c2049535f444953504c4159454420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c49535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a0a435245415445205441424c4520444154415f5345545f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c44535f494420544543485f4944204e4f54204e554c4c2c44535450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c435654455f494420544543485f49442c204d4154455f50524f505f494420544543485f49442c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c4520444154415f5345545f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c2044535f494420544543485f4944204e4f54204e554c4c2c2044535450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c445354595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c2049535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a0a435245415445205441424c4520415554484f52495a4154494f4e5f47524f5550532028494420544543485f4944204e4f54204e554c4c2c20434f444520434f4445204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e53202841475f494420544543485f4944204e4f54204e554c4c2c20504552535f494420544543485f4944204e4f54204e554c4c293b0a0a435245415445205441424c452046494c544552532028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2045585052455353494f4e2054455854204e4f54204e554c4c2c2049535f5055424c494320424f4f4c45414e204e4f54204e554c4c2c20475249445f494420564152434841522832303029204e4f54204e554c4c293b0a435245415445205441424c4520515545524945532028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2045585052455353494f4e2054455854204e4f54204e554c4c2c2049535f5055424c494320424f4f4c45414e204e4f54204e554c4c2c2051554552595f545950452051554552595f54595045204e4f54204e554c4c2c20454e544954595f545950455f434f444520434f44452c2044425f4b455920434f4445204e4f54204e554c4c2044454641554c5420273127293b0a0a435245415445205441424c452072656c6174696f6e736869705f74797065732028696420544543485f4944204e4f54204e554c4c2c20636f646520434f4445204e4f54204e554c4c2c206c6162656c20434f4c554d4e5f4c4142454c2c20706172656e745f6c6162656c20434f4c554d4e5f4c4142454c2c206368696c645f6c6162656c20434f4c554d4e5f4c4142454c2c206465736372697074696f6e204445534352495054494f4e5f323030302c20726567697374726174696f6e5f74696d657374616d702054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20706572735f69645f7265676973746572657220544543485f4944204e4f54204e554c4c2c2069735f6d616e616765645f696e7465726e616c6c7920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2069735f696e7465726e616c5f6e616d65737061636520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2028696420544543485f4944204e4f54204e554c4c2c2073616d706c655f69645f706172656e7420544543485f4944204e4f54204e554c4c2c2072656c6174696f6e736869705f696420544543485f4944204e4f54204e554c4c2c2073616d706c655f69645f6368696c6420544543485f4944204e4f54204e554c4c2c2064656c5f696420544543485f49442c20504552535f49445f415554484f5220544543485f49442c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a0a435245415445205441424c4520736372697074732028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c205343524950545f54595045205343524950545f54595045204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c53435249505420544558545f56414c55452c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c454e544954595f4b494e4420454e544954595f4b494e442c20504c5547494e5f5459504520504c5547494e5f54595045204e4f54204e554c4c2044454641554c5420274a5954484f4e272c2049535f415641494c41424c4520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c542054525545293b0a0a435245415445205441424c4520434f52455f504c5547494e532028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c2056455253494f4e20494e5445474552204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d41535445525f5245475f53435249505420544558545f56414c5545293b0a0a435245415445205441424c4520504f53545f524547495354524154494f4e5f444154415345545f51554555452028494420544543485f4944204e4f54204e554c4c2c2044535f494420544543485f4944204e4f54204e554c4c293b0a0a435245415445205441424c4520454e544954595f4f5045524154494f4e535f4c4f472028494420544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f494420544543485f4944204e4f54204e554c4c293b0a0a435245415445205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f50524f4a5f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c20455850455f494420544543485f49442c2053504143455f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d50293b0a435245415445205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f455850455f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502c2050524f4a5f494420544543485f4944293b0a435245415445205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f53414d505f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c20455850455f494420544543485f49442c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502c2053504143455f494420544543485f4944293b0a435245415445205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f444154415f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c204f5244494e414c20494e54454745522c20455850455f494420544543485f49442c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d50293b0a0a435245415445205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d532028494420544543485f49442c20434f444520434f44452c204c4142454c20544558545f56414c55452c2055524c5f54454d504c41544520544558545f56414c55452c2049535f4f50454e42495320424f4f4c45414e2044454641554c542046414c5345204e4f54204e554c4c293b0a435245415445205441424c45204c494e4b5f444154412028444154415f494420544543485f4944204e4f54204e554c4c2c2045444d535f494420544543485f4944204e4f54204e554c4c2c2045585445524e414c5f434f444520544558545f56414c5545204e4f54204e554c4c293b0a0a435245415445205441424c45204d45544150524f4a454354532028494420544543485f4944204e4f54204e554c4c2c204e414d4520434f4445204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c204f574e455220544543485f4944204e4f54204e554c4c2c205052495641544520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420545255452c204352454154494f4e5f444154452054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2028494420544543485f4944204e4f54204e554c4c2c204d4550525f494420544543485f4944204e4f54204e554c4c2c20455850455f494420544543485f49442c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c204d4154455f494420544543485f49442c2044454c5f494420544543485f49442c204352454154494f4e5f44415445202054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d50293b0a0a435245415445205441424c45204f5045524154494f4e5f455845435554494f4e532028494420544543485f4944204e4f54204e554c4c2c20434f444520434f4445204e4f54204e554c4c2c205354415445204f5045524154494f4e5f455845435554494f4e5f5354415445204e4f54204e554c4c2c204445534352495054494f4e20544558545f56414c5545204e4f54204e554c4c2c204552524f5220544558545f56414c55452c204352454154494f4e5f444154452054494d455f5354414d505f44464c2c2053544152545f444154452054494d455f5354414d502c2046494e4953485f444154452054494d455f5354414d50293b0a0a2d2d204372656174696e67207669657773202d20636f706965642066726f6d20736368656d612067656e65726174656420666f722074657374732c20272a272063616e277420626520757365642062656361757365206f66205067446966665669657773206c696d69746174696f6e20696e207669657720636f6d70617269736f6e0a0a435245415445205649455720646174612041530a202020202053454c4543542069642c20636f64652c20647374795f69642c20646173745f69642c20657870655f69642c20646174615f70726f64756365725f636f64652c2070726f64756374696f6e5f74696d657374616d702c2073616d705f69642c20726567697374726174696f6e5f74696d657374616d702c206163636573735f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2069735f76616c69642c206d6f64696669636174696f6e5f74696d657374616d702c2069735f646572697665642c2064656c5f69642c206f7269675f64656c2c2076657273696f6e200a2020202020202046524f4d20646174615f616c6c200a20202020202057484552452064656c5f6964204953204e554c4c3b0a0a435245415445205649455720646174615f64656c657465642041530a202020202053454c4543542069642c20636f64652c20647374795f69642c20646173745f69642c20657870655f69642c20646174615f70726f64756365725f636f64652c2070726f64756374696f6e5f74696d657374616d702c2073616d705f69642c20726567697374726174696f6e5f74696d657374616d702c206163636573735f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2069735f76616c69642c206d6f64696669636174696f6e5f74696d657374616d702c2069735f646572697665642c2064656c5f69642c206f7269675f64656c2c2076657273696f6e200a2020202020202046524f4d20646174615f616c6c200a20202020202057484552452064656c5f6964204953204e4f54204e554c4c3b0a0a4352454154452056494557206578706572696d656e74732041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c20657874795f69642c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c2070726f6a5f69642c2064656c5f69642c206f7269675f64656c2c2069735f7075626c69632c2076657273696f6e200a2020202020202046524f4d206578706572696d656e74735f616c6c200a20202020202057484552452064656c5f6964204953204e554c4c3b0a0a4352454154452056494557206578706572696d656e74735f64656c657465642041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c20657874795f69642c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c2070726f6a5f69642c2064656c5f69642c206f7269675f64656c2c2069735f7075626c69632c2076657273696f6e200a2020202020202046524f4d206578706572696d656e74735f616c6c200a20202020202057484552452064656c5f6964204953204e4f54204e554c4c3b0a0a43524541544520564945572073616d706c65732041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c2070726f6a5f69642c20657870655f69642c20736174795f69642c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2064656c5f69642c206f7269675f64656c2c2073706163655f69642c2073616d705f69645f706172745f6f662c2076657273696f6e200a2020202020202046524f4d2073616d706c65735f616c6c200a20202020202057484552452064656c5f6964204953204e554c4c3b0a0a43524541544520564945572073616d706c65735f64656c657465642041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c20657870655f69642c20736174795f69642c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2064656c5f69642c206f7269675f64656c2c2073706163655f69642c2073616d705f69645f706172745f6f662c2076657273696f6e200a2020202020202046524f4d2073616d706c65735f616c6c200a20202020202057484552452064656c5f6964204953204e4f54204e554c4c3b0a0a435245415445205649455720646174615f7365745f72656c6174696f6e73686970732041530a20202053454c45435420646174615f69645f706172656e742c20646174615f69645f6368696c642c2072656c6174696f6e736869705f69642c206f7264696e616c2c2064656c5f69642c20706572735f69645f617574686f722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d700a20202046524f4d20646174615f7365745f72656c6174696f6e73686970735f616c6c200a20202057484552452064656c5f6964204953204e554c4c3b0a2020200a43524541544520564945572073616d706c655f72656c6174696f6e73686970732041530a20202053454c4543542069642c2073616d706c655f69645f706172656e742c2072656c6174696f6e736869705f69642c2073616d706c655f69645f6368696c642c2064656c5f69642c20706572735f69645f617574686f722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d700a20202046524f4d2073616d706c655f72656c6174696f6e73686970735f616c6c0a20202057484552452064656c5f6964204953204e554c4c3b0a2020202020200a4352454154452056494557204d45544150524f4a4543545f41535349474e4d454e54532041530a20202053454c4543542049442c204d4550525f49442c20455850455f49442c2053414d505f49442c20444154415f49442c204d4154455f49442c2044454c5f49442c204352454154494f4e5f444154450a20202046524f4d204d45544150524f4a4543545f41535349474e4d454e54535f414c4c200a20202057484552452044454c5f4944204953204e554c4c3b0a2020200a43524541544520564945572073616d706c655f686973746f72795f7669657720415320280a202053454c4543540a20202020322a69642061732069642c0a202020206d61696e5f73616d705f69642c0a2020202072656c6174696f6e5f747970652c0a2020202073706163655f69642c0a20202020657870655f69642c0a2020202073616d705f69642c0a20202020646174615f69642c0a20202020656e746974795f7065726d5f69642c0a202020206e756c6c20617320737470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a2020202053414d504c455f52454c4154494f4e53484950535f484953544f52590a202057484552450a2020202076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c290a554e494f4e0a202053454c4543540a20202020322a69642b312061732069642c0a2020202073616d705f6964206173206d61696e5f73616d705f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c2061732073706163655f69642c0a202020206e756c6c20617320657870655f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a20202020737470745f69642c0a2020202076616c75652c0a20202020766f636162756c6172795f7465726d2c0a202020206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a2020202053414d504c455f50524f504552544945535f484953544f52593b0a0a435245415445205649455720646174615f7365745f686973746f72795f7669657720415320280a202053454c4543540a20202020322a69642061732069642c0a202020206d61696e5f646174615f69642c0a2020202072656c6174696f6e5f747970652c0a202020206f7264696e616c2c0a20202020657870655f69642c0a2020202073616d705f69642c0a20202020646174615f69642c0a20202020656e746974795f7065726d5f69642c0a202020206e756c6c2061732064737470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a20202020444154415f5345545f52454c4154494f4e53484950535f484953544f52590a202057484552450a2020202076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c290a554e494f4e0a202053454c4543540a20202020322a69642b312061732069642c0a2020202064735f6964206173206d61696e5f646174615f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c206173206f7264696e616c2c0a202020206e756c6c20617320657870655f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a2020202064737470745f69642c0a2020202076616c75652c0a20202020766f636162756c6172795f7465726d2c0a202020206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a20202020444154415f5345545f50524f504552544945535f484953544f52593b0a0a4352454154452056494557206578706572696d656e745f686973746f72795f7669657720415320280a202053454c4543540a20202020322a69642061732069642c0a202020206d61696e5f657870655f69642c0a2020202072656c6174696f6e5f747970652c0a2020202070726f6a5f69642c0a2020202073616d705f69642c0a20202020646174615f69642c0a20202020656e746974795f7065726d5f69642c0a202020206e756c6c20617320657470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a202020204558504552494d454e545f52454c4154494f4e53484950535f484953544f52590a202057484552452076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c290a554e494f4e0a202053454c4543540a20202020322a69642b312061732069642c0a20202020657870655f6964206173206d61696e5f657870655f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c2061732070726f6a5f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a20202020657470745f69642c0a2020202076616c75652c0a20202020766f636162756c6172795f7465726d2c0a202020206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a202020204558504552494d454e545f50524f504552544945535f484953544f52593b0a0a2d2d204372656174696e672073657175656e6365730a0a4352454154452053455155454e434520434f4e54524f4c4c45445f564f434142554c4152595f49445f5345513b0a4352454154452053455155454e434520435654455f49445f5345513b0a4352454154452053455155454e43452044415441424153455f494e5354414e43455f49445f5345513b0a4352454154452053455155454e434520444154415f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f52454c4154494f4e534849505f49445f5345513b0a4352454154452053455155454e434520444154415f53544f52455f49445f5345513b0a4352454154452053455155454e434520444154415f53544f52455f53455256494345535f49445f5345513b0a4352454154452053455155454e434520444154415f545950455f49445f5345513b0a4352454154452053455155454e434520455450545f49445f5345513b0a4352454154452053455155454e4345204556454e545f49445f5345513b0a4352454154452053455155454e4345204154544143484d454e545f49445f5345513b0a4352454154452053455155454e4345204154544143484d454e545f434f4e54454e545f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f50524f50455254595f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f545950455f49445f5345513b0a4352454154452053455155454e43452046494c455f464f524d41545f545950455f49445f5345513b0a4352454154452053455155454e43452053504143455f49445f5345513b0a4352454154452053455155454e43452044454c4554494f4e5f49445f5345513b0a4352454154452053455155454e4345204c4f4341544f525f545950455f49445f5345513b0a4352454154452053455155454e4345204d4154455249414c5f49445f5345513b0a4352454154452053455155454e4345204d4154455249414c5f50524f50455254595f49445f5345513b0a4352454154452053455155454e4345204d4154455249414c5f545950455f49445f5345513b0a4352454154452053455155454e4345204d5450545f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f545950455f49445f5345513b0a4352454154452053455155454e434520504552534f4e5f49445f5345513b0a4352454154452053455155454e43452050524f4a4543545f49445f5345513b0a4352454154452053455155454e43452050524f50455254595f545950455f49445f5345513b0a4352454154452053455155454e434520524f4c455f41535349474e4d454e545f49445f5345513b0a4352454154452053455155454e43452053414d504c455f49445f5345513b0a4352454154452053455155454e43452053414d504c455f50524f50455254595f49445f5345513b0a4352454154452053455155454e43452053414d504c455f545950455f49445f5345513b0a4352454154452053455155454e434520535450545f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f50524f50455254595f49445f5345513b0a4352454154452053455155454e43452044535450545f49445f5345513b0a4352454154452053455155454e434520434f44455f5345513b0a4352454154452053455155454e4345204558504552494d454e545f434f44455f5345513b0a4352454154452053455155454e43452053414d504c455f434f44455f5345513b0a4352454154452053455155454e4345205045524d5f49445f5345513b0a4352454154452053455155454e434520415554484f52495a4154494f4e5f47524f55505f49445f5345513b0a4352454154452053455155454e43452046494c5445525f49445f5345513b0a4352454154452053455155454e434520475249445f435553544f4d5f434f4c554d4e535f49445f5345513b0a4352454154452053455155454e43452051554552595f49445f5345513b0a4352454154452053455155454e43452052454c4154494f4e534849505f545950455f49445f5345513b0a4352454154452053455155454e43452053414d504c455f52454c4154494f4e534849505f49445f5345513b0a4352454154452053455155454e4345205343524950545f49445f5345513b0a4352454154452053455155454e434520434f52455f504c5547494e5f49445f5345513b0a4352454154452053455155454e434520504f53545f524547495354524154494f4e5f444154415345545f51554555455f49445f5345513b0a4352454154452053455155454e434520454e544954595f4f5045524154494f4e535f4c4f475f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452053414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452050524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d5f49445f5345513b0a4352454154452053455155454e4345204d45544150524f4a4543545f49445f5345513b0a4352454154452053455155454e4345204d45544150524f4a4543545f41535349474e4d454e545f49445f5345513b0a4352454154452053455155454e4345204f5045524154494f4e5f455845435554494f4e535f49445f5345513b0a0a2d2d204372656174696e67207072696d617279206b657920636f6e73747261696e74730a0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532041444420434f4e53545241494e5420434f564f5f504b205052494d415259204b4559284944293b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f53544f5245532041444420434f4e53545241494e5420444153545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f53544f52455f53455256494345532041444420434f4e53545241494e5420445353455f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f54595045532041444420434f4e53545241494e5420444154595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204556454e54532041444420434f4e53545241494e542045564e545f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f504b205052494d415259204b4559284944293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f504b205052494d415259204b4559284944293b0a414c544552205441424c45204154544143484d454e545f434f4e54454e54532041444420434f4e53545241494e5420455841435f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f54595045532041444420434f4e53545241494e5420455854595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f504b205052494d415259204b4559284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f504b205052494d415259204b455928444154415f4944293b0a414c544552205441424c452046494c455f464f524d41545f54595045532041444420434f4e53545241494e5420464654595f504b205052494d415259204b4559284944293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f504b205052494d415259204b4559284944293b0a414c544552205441424c452044454c4554494f4e532041444420434f4e53545241494e542044454c5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204c4f4341544f525f54595045532041444420434f4e53545241494e54204c4f54595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f54595045532041444420434f4e53545241494e54204d4154595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f54595045532041444420434f4e53545241494e5420445354595f504b205052494d415259204b4559284944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f504b205052494d415259204b4559284944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504b205052494d415259204b4559284944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f504b205052494d415259204b4559284944293b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f54595045532041444420434f4e53545241494e5420534154595f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f504b205052494d415259204b4559284944293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f5550532041444420434f4e53545241494e542041475f504b205052494d415259204b4559284944293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e532041444420434f4e53545241494e54204147505f504b205052494d415259204b455928504552535f49442c41475f4944293b0a414c544552205441424c452046494c544552532041444420434f4e53545241494e542046494c545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520475249445f435553544f4d5f434f4c554d4e532041444420434f4e53545241494e5420475249445f435553544f4d5f434f4c554d4e535f504b205052494d415259204b4559284944293b0a414c544552205441424c4520515545524945532041444420434f4e53545241494e5420515545525f504b205052494d415259204b4559284944293b0a414c544552205441424c452072656c6174696f6e736869705f74797065732041444420434f4e53545241494e5420726574795f706b205052494d415259204b455920286964293b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f706b205052494d415259204b455920286964293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f504b205052494d415259204b4559284944293b0a414c544552205441424c4520504f53545f524547495354524154494f4e5f444154415345545f51554555452041444420434f4e53545241494e5420505244515f504b205052494d415259204b4559284944293b0a414c544552205441424c4520454e544954595f4f5045524154494f4e535f4c4f472041444420434f4e53545241494e5420454f4c5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d532041444420434f4e53545241494e542045444d535f504b205052494d415259204b4559284944293b0a414c544552205441424c45204c494e4b5f444154412041444420434f4e53545241494e54204c4e44415f504b205052494d415259204b455928444154415f4944293b0a414c544552205441424c45204d45544150524f4a454354532041444420434f4e53545241494e54204d45544150524f4a454354535f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f504b205052494d415259204b4559284944293b0a0a2d2d204372656174696e6720756e6971756520636f6e73747261696e74730a0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532041444420434f4e53545241494e5420434f564f5f424b5f554b20554e4951554528434f44452c49535f494e5445524e414c5f4e414d455350414345293b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f424b5f554b20554e4951554528434f44452c434f564f5f4944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f424b5f554b20554e4951554528444154415f49445f4348494c442c444154415f49445f504152454e542c52454c4154494f4e534849505f4944293b0a414c544552205441424c4520444154415f53544f52455f53455256494345532041444420434f4e53545241494e5420445353455f424b5f554b20554e49515545284b45592c20444154415f53544f52455f4944293b0a414c544552205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532041444420434f4e53545241494e54204453534453545f424b5f554b20554e4951554528444154415f53544f52455f534552564943455f49442c20444154415f5345545f545950455f4944293b0a414c544552205441424c4520444154415f53544f5245532041444420434f4e53545241494e5420444153545f424b5f554b20554e4951554528434f44452c55554944293b0a414c544552205441424c4520444154415f54595045532041444420434f4e53545241494e5420444154595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f424b5f554b20554e4951554528434f44452c50524f4a5f4944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f50495f554b20554e49515545285045524d5f4944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f424b5f554b20554e4951554528455850455f49442c455450545f4944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f424b5f554b20554e4951554528455854595f49442c505254595f4944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f424b5f554b20554e49515545284c4f434154494f4e2c4c4f54595f4944293b0a414c544552205441424c452046494c455f464f524d41545f54595045532041444420434f4e53545241494e5420464654595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204c4f4341544f525f54595045532041444420434f4e53545241494e54204c4f54595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f424b5f554b20554e4951554528434f44452c4d4154595f4944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f424b5f554b20554e49515545284d4154455f49442c4d5450545f4944293b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f424b5f554b20554e49515545284d4154595f49442c505254595f4944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f424b5f554b20554e4951554528555345525f4944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f424b5f554b20554e4951554528434f44452c53504143455f4944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f50495f554b20554e49515545285045524d5f4944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f424b5f554b20554e4951554528434f44452c49535f494e5445524e414c5f4e414d455350414345293b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f50455f53504143455f424b5f554b20554e4951554528504552535f49445f4752414e5445452c524f4c455f434f44452c53504143455f4944293b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f41475f53504143455f424b5f554b20554e495155452841475f49445f4752414e5445452c524f4c455f434f44452c53504143455f4944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f50495f554b20554e49515545285045524d5f4944293b0a414c544552205441424c452073616d706c65735f616c6c2041444420434f4e53545241494e542073616d705f636f64655f756e697175655f636865636b5f756b20554e4951554528636f64655f756e697175655f636865636b293b0a414c544552205441424c452073616d706c65735f616c6c2041444420434f4e53545241494e542073616d705f737562636f64655f756e697175655f636865636b5f756b20554e4951554528737562636f64655f756e697175655f636865636b293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f424b5f554b20554e495155452853414d505f49442c535450545f4944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f424b5f554b20554e4951554528534154595f49442c505254595f4944293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f424b5f554b20554e4951554528445354595f49442c505254595f4944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f424b5f554b20554e495155452844535f49442c44535450545f4944293b0a2d2d204e4f54453a20666f6c6c6f77696e6720756e697175656e65737320636f6e73747261696e747320666f72206174746163686d656e747320776f726b2c206265636175736520286e756c6c20213d206e756c6c2920696e20506f737467726573200a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f455850455f424b5f554b20554e4951554528455850455f49442c46494c455f4e414d452c56455253494f4e293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f50524f4a5f424b5f554b20554e495155452850524f4a5f49442c46494c455f4e414d452c56455253494f4e293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f53414d505f424b5f554b20554e495155452853414d505f49442c46494c455f4e414d452c56455253494f4e293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f5550532041444420434f4e53545241494e542041475f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c452046494c544552532041444420434f4e53545241494e542046494c545f424b5f554b20554e49515545284e414d452c20475249445f4944293b0a414c544552205441424c4520475249445f435553544f4d5f434f4c554d4e532041444420434f4e53545241494e5420475249445f435553544f4d5f434f4c554d4e535f424b5f554b20554e4951554528434f44452c20475249445f4944293b0a414c544552205441424c4520515545524945532041444420434f4e53545241494e5420515545525f424b5f554b20554e49515545284e414d45293b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f626b5f756b20554e495155452873616d706c655f69645f6368696c642c73616d706c655f69645f706172656e742c72656c6174696f6e736869705f6964293b0a414c544552205441424c452072656c6174696f6e736869705f74797065732041444420434f4e53545241494e5420726574795f756b20554e4951554528636f6465293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f554b20554e49515545284e414d45293b0a414c544552205441424c4520434f52455f504c5547494e532041444420434f4e53545241494e5420434f504c5f4e414d455f5645525f554b20554e49515545284e414d452c56455253494f4e293b0a414c544552205441424c4520454e544954595f4f5045524154494f4e535f4c4f472041444420434f4e53545241494e5420454f4c5f5245475f49445f554b20554e4951554528524547495354524154494f4e5f4944293b0a414c544552205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d532041444420434f4e53545241494e542045444d535f434f44455f554b20554e4951554528434f4445293b0a2d2d204e4f54453a20666f6c6c6f77696e6720756e697175656e65737320636f6e73747261696e747320666f72206d65746170726f6a6563742061737369676e6d656e747320776f726b2c206265636175736520286e756c6c20213d206e756c6c2920696e20506f737467726573200a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f455850455f49445f554b20554e4951554520284d4550525f49442c20455850455f4944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f53414d505f49445f554b20554e4951554520284d4550525f49442c2053414d505f4944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f444154415f49445f554b20554e4951554520284d4550525f49442c20444154415f4944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f4d4154455f49445f554b20554e4951554520284d4550525f49442c204d4154455f4944293b0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f434f44455f554b20554e495155452028434f4445293b0a0a2d2d204372656174696e6720666f726569676e206b657920636f6e73747261696e74730a0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532041444420434f4e53545241494e5420434f564f5f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f434f564f5f464b20464f524549474e204b45592028434f564f5f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152494553284944293b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f445354595f464b20464f524549474e204b45592028445354595f494429205245464552454e43455320444154415f5345545f5459504553284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f444153545f464b20464f524549474e204b45592028444153545f494429205245464552454e43455320444154415f53544f524553284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f4348494c4420464f524549474e204b45592028444154415f49445f4348494c4429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f504152454e5420464f524549474e204b45592028444154415f49445f504152454e5429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f52454c4154494f4e5348495020464f524549474e204b4559202852454c4154494f4e534849505f494429205245464552454e4345532052454c4154494f4e534849505f5459504553284944293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420444154415f5345545f52454c4154494f4e53484950535f504552535f464b20464f524549474e204b45592028504552535f49445f415554484f5229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f646174615f666b5f6368696c6420464f524549474e204b4559202873616d706c655f69645f6368696c6429205245464552454e4345532053414d504c45535f414c4c28696429204f4e2044454c45544520434153434144453b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f646174615f666b5f706172656e7420464f524549474e204b4559202873616d706c655f69645f706172656e7429205245464552454e4345532053414d504c45535f414c4c28696429204f4e2044454c45544520434153434144453b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f646174615f666b5f72656c6174696f6e7368697020464f524549474e204b4559202872656c6174696f6e736869705f696429205245464552454e4345532072656c6174696f6e736869705f7479706573286964293b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f64656c5f666b20464f524549474e204b4559202864656c5f696429205245464552454e4345532064656c6574696f6e73286964293b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e542053414d504c455f52454c4154494f4e53484950535f504552535f464b20464f524549474e204b45592028504552535f49445f415554484f5229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f53544f52455f53455256494345532041444420434f4e53545241494e5420445353455f44535f464b20464f524549474e204b45592028444154415f53544f52455f494429205245464552454e43455320444154415f53544f52455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532041444420434f4e53545241494e54204453534453545f44535f464b20464f524549474e204b45592028444154415f53544f52455f534552564943455f494429205245464552454e43455320444154415f53544f52455f534552564943455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532041444420434f4e53545241494e54204453534453545f4453545f464b20464f524549474e204b45592028444154415f5345545f545950455f494429205245464552454e43455320444154415f5345545f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204556454e54532041444420434f4e53545241494e542045564e545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204556454e54532041444420434f4e53545241494e542045564e545f455841435f464b20464f524549474e204b45592028455841435f494429205245464552454e434553204154544143484d454e545f434f4e54454e5453284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f455854595f464b20464f524549474e204b45592028455854595f494429205245464552454e434553204558504552494d454e545f5459504553284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f50524f4a5f464b20464f524549474e204b4559202850524f4a5f494429205245464552454e4345532050524f4a45435453284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c284944293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f50524f4a5f464b20464f524549474e204b4559202850524f4a5f494429205245464552454e4345532050524f4a45435453284944293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c284944293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f434f4e545f464b20464f524549474e204b45592028455841435f494429205245464552454e434553204154544143484d454e545f434f4e54454e5453284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f455450545f464b20464f524549474e204b45592028455450545f494429205245464552454e434553204558504552494d454e545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f455450545f464b20464f524549474e204b45592028455450545f494429205245464552454e434553204558504552494d454e545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f54595045532041444420434f4e53545241494e5420455854595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f455854595f464b20464f524549474e204b45592028455854595f494429205245464552454e434553204558504552494d454e545f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f435654455f464b20464f524549474e204b45592028435654455f49445f53544f525f464d5429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f435654455f53544f5245445f4f4e5f464b20464f524549474e204b45592028435654455f49445f53544f524529205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f464654595f464b20464f524549474e204b45592028464654595f494429205245464552454e4345532046494c455f464f524d41545f5459504553284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f4c4f54595f464b20464f524549474e204b455920284c4f54595f494429205245464552454e434553204c4f4341544f525f5459504553284944293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f504552535f464b5f5245474953544552455220464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452044454c4554494f4e532041444420434f4e53545241494e542044454c5f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f4d4154595f464b20464f524549474e204b455920284d4154595f494429205245464552454e434553204d4154455249414c5f5459504553284944293b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f4d4154455f464b20464f524549474e204b455920284d4154455f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f4d5450545f464b20464f524549474e204b455920284d5450545f494429205245464552454e434553204d4154455249414c5f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f4d4154455f464b20464f524549474e204b455920284d4154455f494429205245464552454e434553204d4154455249414c5328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f4d5450545f464b20464f524549474e204b455920284d5450545f494429205245464552454e434553204d4154455249414c5f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f54595045532041444420434f4e53545241494e54204d4154595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f4d4154595f464b20464f524549474e204b455920284d4154595f494429205245464552454e434553204d4154455249414c5f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f54595045532041444420434f4e53545241494e5420445354595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e43455320535041434553284944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e43455320535041434553284944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504552535f464b5f4c454144455220464f524549474e204b45592028504552535f49445f4c454144455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504552535f464b5f5245474953544552455220464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f434f564f5f464b20464f524549474e204b45592028434f564f5f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152494553284944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f444154595f464b20464f524549474e204b45592028444154595f494429205245464552454e43455320444154415f5459504553284944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f4d4154595f464b20464f524549474e204b455920284d4154595f50524f505f494429205245464552454e434553204d4154455249414c5f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e4345532053504143455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f504552535f464b5f4752414e54454520464f524549474e204b45592028504552535f49445f4752414e54454529205245464552454e43455320504552534f4e5328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f41475f464b5f4752414e54454520464f524549474e204b4559202841475f49445f4752414e54454529205245464552454e43455320415554484f52495a4154494f4e5f47524f55505328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f504552535f464b5f5245474953544552455220464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e43455320535041434553284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f50524f4a5f464b20464f524549474e204b4559202850524f4a5f494429205245464552454e4345532050524f4a45435453284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f53414d505f464b5f504152545f4f4620464f524549474e204b4559202853414d505f49445f504152545f4f4629205245464552454e4345532053414d504c45535f414c4c284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f534154595f464b20464f524549474e204b45592028534154595f494429205245464552454e4345532053414d504c455f5459504553284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f535450545f464b20464f524549474e204b45592028535450545f494429205245464552454e4345532053414d504c455f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f535450545f464b20464f524549474e204b45592028535450545f494429205245464552454e4345532053414d504c455f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f54595045532041444420434f4e53545241494e5420534154595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f534154595f464b20464f524549474e204b45592028534154595f494429205245464552454e4345532053414d504c455f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f445354595f464b20464f524549474e204b45592028445354595f494429205245464552454e43455320444154415f5345545f54595045532849442920204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f44535450545f464b20464f524549474e204b4559202844535450545f494429205245464552454e43455320444154415f5345545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f44535f464b20464f524549474e204b4559202844535f494429205245464552454e43455320444154415f414c4c284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f44535450545f464b20464f524549474e204b4559202844535450545f494429205245464552454e43455320444154415f5345545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f44535f464b20464f524549474e204b4559202844535f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e532041444420434f4e53545241494e54204147505f41475f464b20464f524549474e204b4559202841475f494429205245464552454e43455320415554484f52495a4154494f4e5f47524f555053284944293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e532041444420434f4e53545241494e54204147505f504552535f464b20464f524549474e204b45592028504552535f494429205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f5550532041444420434f4e53545241494e542041475f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a0a414c544552205441424c452046494c544552532041444420434f4e53545241494e542046494c545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520475249445f435553544f4d5f434f4c554d4e532041444420434f4e53545241494e5420475249445f435553544f4d5f434f4c554d4e535f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520515545524945532041444420434f4e53545241494e5420515545525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204f4e4c5920504f53545f524547495354524154494f4e5f444154415345545f51554555452041444420434f4e53545241494e5420707264715f64735f666b20464f524549474e204b4559202864735f696429205245464552454e43455320646174615f616c6c28696429204f4e2044454c45544520434153434144453b0a0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f4d41494e5f455850455f464b20464f524549474e204b455920284d41494e5f455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f50524f4a5f464b20464f524549474e204b4559202850524f4a5f494429205245464552454e4345532050524f4a4543545328494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f4d41494e5f53414d505f464b20464f524549474e204b455920284d41494e5f53414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e4345532053504143455328494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f4d41494e5f444154415f464b20464f524549474e204b455920284d41494e5f444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f4d41494e5f50524f4a5f464b20464f524549474e204b455920284d41494e5f50524f4a5f494429205245464552454e4345532050524f4a4543545328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e4345532053504143455328494429204f4e2044454c45544520534554204e554c4c3b0a0a414c544552205441424c45204c494e4b5f444154412041444420434f4e53545241494e54204c4e44415f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204c494e4b5f444154412041444420434f4e53545241494e54204c4e44415f45444d535f464b20464f524549474e204b4559202845444d535f494429205245464552454e4345532045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d53284944293b0a0a414c544552205441424c45204d45544150524f4a454354532041444420434f4e53545241494e54204d45544150524f4a454354535f4f574e45525f464b20464f524549474e204b455920284f574e455229205245464552454e43455320504552534f4e5328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f464b20464f524549474e204b455920284d4550525f494429205245464552454e434553204d45544150524f4a4543545328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f455850455f49445f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f53414d505f49445f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f444154415f49445f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4154455f49445f464b20464f524549474e204b455920284d4154455f494429205245464552454e434553204d4154455249414c5328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f44454c5f49445f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a0a2d2d204372656174696e6720636865636b20636f6e73747261696e74730a0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f434b20434845434b2028455850455f4944204953204e4f54204e554c4c204f522053414d505f4944204953204e4f54204e554c4c293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f4152435f434b20434845434b200a092828455850455f4944204953204e4f54204e554c4c20414e442050524f4a5f4944204953204e554c4c20414e442053414d505f4944204953204e554c4c29204f52200a092028455850455f4944204953204e554c4c20414e442050524f4a5f4944204953204e4f54204e554c4c20414e442053414d505f4944204953204e554c4c29204f520a092028455850455f4944204953204e554c4c20414e442050524f4a5f4944204953204e554c4c20414e442053414d505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c45206576656e74732041444420434f4e53545241494e542065766e745f65745f656e756d5f636b20434845434b200a0928656e746974795f7479706520494e2028274154544143484d454e54272c202744415441534554272c20274558504552494d454e54272c20275350414345272c20274d4154455249414c272c202750524f4a454354272c202750524f50455254595f54595045272c202753414d504c45272c2027564f434142554c415259272c2027415554484f52495a4154494f4e5f47524f5550272c20274d45544150524f4a4543542729293b200a414c544552205441424c4520636f6e74726f6c6c65645f766f636162756c6172795f7465726d732041444420434f4e53545241494e5420637674655f636b20434845434b20286f7264696e616c203e2030293b0a0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f434845434b5f4e4e20434845434b20280a0928455850455f4944204953204e4f54204e554c4c20414e442053414d505f4944204953204e554c4c20414e4420444154415f4944204953204e554c4c20414e44204d4154455f4944204953204e554c4c29204f520a0928455850455f4944204953204e554c4c20414e442053414d505f4944204953204e4f54204e554c4c20414e4420444154415f4944204953204e554c4c20414e44204d4154455f4944204953204e554c4c29204f520a0928455850455f4944204953204e554c4c20414e442053414d505f4944204953204e554c4c20414e4420444154415f4944204953204e4f54204e554c4c20414e44204d4154455f4944204953204e554c4c29204f520a0928455850455f4944204953204e554c4c20414e442053414d505f4944204953204e554c4c20414e4420444154415f4944204953204e554c4c20414e44204d4154455f4944204953204e4f54204e554c4c29293b0a0a414c544552205441424c4520534352495054532041444420434f4e53545241494e54205343524950545f4e4e5f434b20434845434b0a202028504c5547494e5f54595045203d20275052454445504c4f59454427204f5220534352495054204953204e4f54204e554c4c293b0a0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f53544154455f4552524f525f434845434b20434845434b20280a0928535441544520213d20274641494c45442720414e44204552524f52204953204e554c4c29204f520a09285354415445203d20274641494c45442720414e44204552524f52204953204e4f54204e554c4c290a293b0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f53544154455f53544152545f444154455f434845434b20434845434b20280a0928535441544520494e2028274e4557272c275343484544554c4544272920414e442053544152545f44415445204953204e554c4c29204f52200a0928535441544520494e20282752554e4e494e47272c2746494e4953484544272c274641494c4544272920414e442053544152545f44415445204953204e4f54204e554c4c290a293b0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f53544154455f46494e4953485f444154455f434845434b20434845434b20280a0928535441544520494e2028274e4557272c275343484544554c4544272c2752554e4e494e47272920414e442046494e4953485f44415445204953204e554c4c29204f52200a0928535441544520494e20282746494e4953484544272c274641494c4544272920414e442046494e4953485f44415445204953204e4f54204e554c4c290a293b0a20200a2d2d204372656174696e6720696e64696365730a0a43524541544520494e44455820434f564f5f504552535f464b5f49204f4e20434f4e54524f4c4c45445f564f434142554c41524945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820435654455f434f564f5f464b5f49204f4e20434f4e54524f4c4c45445f564f434142554c4152595f5445524d532028434f564f5f4944293b0a43524541544520494e44455820435654455f504552535f464b5f49204f4e20434f4e54524f4c4c45445f564f434142554c4152595f5445524d532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820444154415f445354595f464b5f49204f4e20444154415f414c4c2028445354595f4944293b0a43524541544520494e44455820444154415f53414d505f464b5f49204f4e20444154415f414c4c202853414d505f4944293b0a43524541544520494e44455820444154415f455850455f464b5f49204f4e20444154415f414c4c2028455850455f4944293b0a43524541544520494e44455820444154415f44454c5f464b5f49204f4e20444154415f414c4c202844454c5f4944293b0a43524541544520494e44455820444154415f414343545f49204f4e20444154415f414c4c20284143434553535f54494d455354414d50293b0a43524541544520494e44455820445352455f444154415f464b5f495f4348494c44204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c2028444154415f49445f4348494c44293b0a43524541544520494e44455820445352455f444154415f464b5f495f504152454e54204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c2028444154415f49445f504152454e54293b0a43524541544520494e44455820445352455f44454c5f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c202844454c5f4944293b0a43524541544520494e44455820736172655f646174615f666b5f695f6368696c64204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202873616d706c655f69645f6368696c64293b0a43524541544520494e44455820736172655f646174615f666b5f695f706172656e74204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202873616d706c655f69645f706172656e74293b0a43524541544520494e44455820736172655f646174615f666b5f695f72656c6174696f6e73686970204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202872656c6174696f6e736869705f6964293b0a43524541544520494e44455820736172655f64656c5f666b5f69204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202864656c5f6964293b0a43524541544520494e44455820445353455f44535f464b5f49204f4e20444154415f53544f52455f53455256494345532028444154415f53544f52455f4944293b0a43524541544520494e444558204453534453545f44535f464b5f49204f4e20444154415f53544f52455f534552564943455f444154415f5345545f54595045532028444154415f53544f52455f534552564943455f4944293b0a43524541544520494e444558204453534453545f4453545f464b5f49204f4e20444154415f53544f52455f534552564943455f444154415f5345545f54595045532028444154415f5345545f545950455f4944293b0a43524541544520494e44455820455450545f455854595f464b5f49204f4e204558504552494d454e545f545950455f50524f50455254595f54595045532028455854595f4944293b0a43524541544520494e44455820455450545f504552535f464b5f49204f4e204558504552494d454e545f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820455450545f505254595f464b5f49204f4e204558504552494d454e545f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e4445582045564e545f504552535f464b5f49204f4e204556454e54532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582045564e545f455841435f464b5f49204f4e204556454e54532028455841435f4944293b0a43524541544520494e44455820415454415f455850455f464b5f49204f4e204154544143484d454e54532028455850455f4944293b0a43524541544520494e44455820415454415f53414d505f464b5f49204f4e204154544143484d454e5453202853414d505f4944293b0a43524541544520494e44455820415454415f50524f4a5f464b5f49204f4e204154544143484d454e5453202850524f4a5f4944293b0a43524541544520494e44455820415454415f504552535f464b5f49204f4e204154544143484d454e54532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820415454415f455841435f464b5f49204f4e204154544143484d454e54532028455841435f4944293b0a43524541544520494e44455820455844415f435654455f464b5f49204f4e2045585445524e414c5f444154412028435654455f49445f53544f525f464d54293b0a43524541544520494e44455820455844415f435654455f53544f5245445f4f4e5f464b5f49204f4e2045585445524e414c5f444154412028435654455f49445f53544f5245293b0a43524541544520494e44455820455844415f464654595f464b5f49204f4e2045585445524e414c5f444154412028464654595f4944293b0a43524541544520494e44455820455844415f4c4f54595f464b5f49204f4e2045585445524e414c5f4441544120284c4f54595f4944293b0a43524541544520494e44455820455850455f455854595f464b5f49204f4e204558504552494d454e54535f414c4c2028455854595f4944293b0a43524541544520494e44455820455850455f44454c5f464b5f49204f4e204558504552494d454e54535f414c4c202844454c5f4944293b0a43524541544520494e44455820455850455f504552535f464b5f49204f4e204558504552494d454e54535f414c4c2028504552535f49445f52454749535445524552293b0a43524541544520494e44455820455850455f50524f4a5f464b5f49204f4e204558504552494d454e54535f414c4c202850524f4a5f4944293b0a43524541544520494e44455820455850525f435654455f464b5f49204f4e204558504552494d454e545f50524f504552544945532028435654455f4944293b0a43524541544520494e44455820455850525f455450545f464b5f49204f4e204558504552494d454e545f50524f504552544945532028455450545f4944293b0a43524541544520494e44455820455850525f455850455f464b5f49204f4e204558504552494d454e545f50524f504552544945532028455850455f4944293b0a43524541544520494e44455820455850525f504552535f464b5f49204f4e204558504552494d454e545f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820455850525f4d4150525f464b5f49204f4e204558504552494d454e545f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e4445582045585052485f455450545f464b5f49204f4e204558504552494d454e545f50524f504552544945535f484953544f52592028455450545f4944293b0a43524541544520494e4445582045585052485f455850455f464b5f49204f4e204558504552494d454e545f50524f504552544945535f484953544f52592028455850455f4944293b0a43524541544520494e4445582045585052485f565554535f464b5f49204f4e204558504552494d454e545f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e4445582053504143455f504552535f524547495354455245445f42595f464b5f49204f4e205350414345532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582044454c5f504552535f464b5f49204f4e2044454c4554494f4e532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d4150525f435654455f464b5f49204f4e204d4154455249414c5f50524f504552544945532028435654455f4944293b0a43524541544520494e444558204d4150525f4d4154455f464b5f49204f4e204d4154455249414c5f50524f5045525449455320284d4154455f4944293b0a43524541544520494e444558204d4150525f4d5450545f464b5f49204f4e204d4154455249414c5f50524f5045525449455320284d5450545f4944293b0a43524541544520494e444558204d4150525f504552535f464b5f49204f4e204d4154455249414c5f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d4150525f4d4150525f464b5f49204f4e204d4154455249414c5f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e444558204d415052485f455450545f464b5f49204f4e204d4154455249414c5f50524f504552544945535f484953544f525920284d5450545f4944293b0a43524541544520494e444558204d415052485f455850455f464b5f49204f4e204d4154455249414c5f50524f504552544945535f484953544f525920284d4154455f4944293b0a43524541544520494e444558204d415052485f565554535f464b5f49204f4e204d4154455249414c5f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e444558204d4154455f4d4154595f464b5f49204f4e204d4154455249414c5320284d4154595f4944293b0a43524541544520494e444558204d4154455f504552535f464b5f49204f4e204d4154455249414c532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d5450545f4d4154595f464b5f49204f4e204d4154455249414c5f545950455f50524f50455254595f545950455320284d4154595f4944293b0a43524541544520494e444558204d5450545f504552535f464b5f49204f4e204d4154455249414c5f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d5450545f505254595f464b5f49204f4e204d4154455249414c5f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e44455820504552535f53504143455f464b5f49204f4e20504552534f4e53202853504143455f4944293b0a43524541544520494e44455820504552535f49535f4143544956455f49204f4e20504552534f4e53202849535f414354495645293b0a43524541544520494e4445582050524f4a5f53504143455f464b5f49204f4e2050524f4a45435453202853504143455f4944293b0a43524541544520494e4445582050524f4a5f504552535f464b5f495f4c4541444552204f4e2050524f4a454354532028504552535f49445f4c4541444552293b0a43524541544520494e4445582050524f4a5f504552535f464b5f495f52454749535445524552204f4e2050524f4a454354532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820505254595f434f564f5f464b5f49204f4e2050524f50455254595f54595045532028434f564f5f4944293b0a43524541544520494e44455820505254595f444154595f464b5f49204f4e2050524f50455254595f54595045532028444154595f4944293b0a43524541544520494e44455820505254595f504552535f464b5f49204f4e2050524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820524f41535f53504143455f464b5f49204f4e20524f4c455f41535349474e4d454e5453202853504143455f4944293b0a43524541544520494e44455820524f41535f504552535f464b5f495f4752414e544545204f4e20524f4c455f41535349474e4d454e54532028504552535f49445f4752414e544545293b0a43524541544520494e44455820524f41535f41475f464b5f495f4752414e544545204f4e20524f4c455f41535349474e4d454e5453202841475f49445f4752414e544545293b0a43524541544520494e44455820524f41535f504552535f464b5f495f52454749535445524552204f4e20524f4c455f41535349474e4d454e54532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582053414d505f44454c5f464b5f49204f4e2053414d504c45535f414c4c202844454c5f4944293b0a43524541544520494e4445582053414d505f504552535f464b5f49204f4e2053414d504c45535f414c4c2028504552535f49445f52454749535445524552293b0a43524541544520494e4445582053414d505f53414d505f464b5f495f504152545f4f46204f4e2053414d504c45535f414c4c202853414d505f49445f504152545f4f46293b0a43524541544520494e4445582053414d505f455850455f464b5f49204f4e2053414d504c45535f414c4c2028455850455f4944293b0a43524541544520494e4445582053414d505f50524f4a5f464b5f49204f4e2053414d504c45535f414c4c202850524f4a5f4944293b0a43524541544520494e4445582053414d505f434f44455f49204f4e2053414d504c45535f414c4c2028434f4445293b0a43524541544520494e4445582053414d505f534154595f464b5f49204f4e2053414d504c45535f414c4c2028534154595f4944293b0a43524541544520494e44455820534150525f435654455f464b5f49204f4e2053414d504c455f50524f504552544945532028435654455f4944293b0a43524541544520494e44455820534150525f504552535f464b5f49204f4e2053414d504c455f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820534150525f53414d505f464b5f49204f4e2053414d504c455f50524f50455254494553202853414d505f4944293b0a43524541544520494e44455820534150525f535450545f464b5f49204f4e2053414d504c455f50524f504552544945532028535450545f4944293b0a43524541544520494e44455820534150525f4d4150525f464b5f49204f4e2053414d504c455f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e4445582053415052485f455450545f464b5f49204f4e2053414d504c455f50524f504552544945535f484953544f52592028535450545f4944293b0a43524541544520494e4445582053415052485f455850455f464b5f49204f4e2053414d504c455f50524f504552544945535f484953544f5259202853414d505f4944293b0a43524541544520494e4445582053415052485f565554535f464b5f49204f4e2053414d504c455f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e44455820535450545f504552535f464b5f49204f4e2053414d504c455f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820535450545f505254595f464b5f49204f4e2053414d504c455f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e44455820535450545f534154595f464b5f49204f4e2053414d504c455f545950455f50524f50455254595f54595045532028534154595f4944293b0a43524541544520494e44455820445350525f435654455f464b5f49204f4e20444154415f5345545f50524f504552544945532028435654455f4944293b0a43524541544520494e44455820445350525f44535450545f464b5f49204f4e20444154415f5345545f50524f50455254494553202844535450545f4944293b0a43524541544520494e44455820445350525f44535f464b5f49204f4e20444154415f5345545f50524f50455254494553202844535f4944293b0a43524541544520494e44455820445350525f504552535f464b5f49204f4e20444154415f5345545f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820445350525f4d4150525f464b5f49204f4e20444154415f5345545f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e4445582044535052485f455450545f464b5f49204f4e20444154415f5345545f50524f504552544945535f484953544f5259202844535450545f4944293b0a43524541544520494e4445582044535052485f455850455f464b5f49204f4e20444154415f5345545f50524f504552544945535f484953544f5259202844535f4944293b0a43524541544520494e4445582044535052485f565554535f464b5f49204f4e20444154415f5345545f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e4445582044535450545f445354595f464b5f49204f4e20444154415f5345545f545950455f50524f50455254595f54595045532028445354595f4944293b0a43524541544520494e4445582044535450545f504552535f464b5f49204f4e20444154415f5345545f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582044535450545f505254595f464b5f49204f4e20444154415f5345545f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e4445582046494c545f504552535f464b5f49204f4e2046494c544552532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820475249445f435553544f4d5f434f4c554d4e535f504552535f464b5f49204f4e20475249445f435553544f4d5f434f4c554d4e532028504552535f49445f52454749535445524552293b0a43524541544520494e444558205343524950545f504552535f464b5f49204f4e20534352495054532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820454e544954595f4f5045524154494f4e535f4c4f475f5249445f49204f4e20454e544954595f4f5045524154494f4e535f4c4f4728524547495354524154494f4e5f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f53414d505f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f49442c2053414d505f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f444154415f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f49442c20444154415f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f50524f4a5f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f49442c2050524f4a5f4944293b0a43524541544520494e44455820455852454c485f53414d505f49445f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259202853414d505f4944293b0a43524541544520494e44455820455852454c485f444154415f49445f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592028444154415f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f455850455f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c20455850455f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f53414d505f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c2053414d505f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f444154415f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c20444154415f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f53504143455f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c2053504143455f4944293b0a43524541544520494e4445582053414d5052454c485f53414d505f49445f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f5259202853414d505f4944293b0a43524541544520494e4445582053414d5052454c485f444154415f49445f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f52592028444154415f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f455850455f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f49442c20455850455f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f53414d505f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f49442c2053414d505f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f444154415f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f49442c20444154415f4944293b0a43524541544520494e444558204441544152454c485f444154415f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f52592028444154415f4944293b0a43524541544520494e44455820505252454c485f4d41494e5f50524f4a5f464b5f49204f4e2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920284d41494e5f50524f4a5f4944293b0a43524541544520494e44455820505252454c485f4d41494e5f50524f4a5f464b5f455850455f464b5f49204f4e2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920284d41494e5f50524f4a5f49442c20455850455f4944293b0a43524541544520494e44455820505252454c485f4d41494e5f50524f4a5f464b5f53504143455f464b5f49204f4e2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920284d41494e5f50524f4a5f49442c2053504143455f4944293b0a43524541544520494e444558204d45544150524f4a454354535f4f574e45525f464b5f49204f4e204d45544150524f4a4543545320284f574e4552293b0a43524541544520494e444558204d45544150524f4a454354535f4e414d455f49204f4e204d45544150524f4a4543545320284e414d45293b0a43524541544520554e4951554520494e444558204d45544150524f4a454354535f4e414d455f4f574e45525f554b204f4e204d45544150524f4a4543545320286c6f776572284e414d45292c204f574e4552293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20284d4550525f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f455850455f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2028455850455f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f53414d505f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c202853414d505f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f444154415f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2028444154415f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4154455f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20284d4154455f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f44454c5f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c202844454c5f4944293b0a43524541544520494e444558204f5045524154494f4e5f455845435554494f4e535f434f44455f49204f4e204f5045524154494f4e5f455845435554494f4e532028434f4445293b0a0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f41475f504552535f4152435f434b20434845434b20282841475f49445f4752414e544545204953204e4f54204e554c4c20414e4420504552535f49445f4752414e544545204953204e554c4c29204f52202841475f49445f4752414e544545204953204e554c4c20414e4420504552535f49445f4752414e544545204953204e4f54204e554c4c29293b0a414c544552205441424c45204d4154455249414c5f54595045532041444420434f4e53545241494e54204d4154595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204558504552494d454e545f54595045532041444420434f4e53545241494e5420455854595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c452053414d504c455f54595045532041444420434f4e53545241494e5420534154595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c4520444154415f5345545f54595045532041444420434f4e53545241494e5420445354595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f424b5f554b20554e4951554528434f4445293b0a	\N
154	./sql/postgresql/154/function-154.sql	SUCCESS	2016-04-20 11:16:39.756	\\x2d2d204372656174696e672046756e6374696f6e730a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652066756e6374696f6e2052454e414d455f53455155454e43452829207468617420697320726571756972656420666f722072656e616d696e67207468652073657175656e6365732062656c6f6e67696e6720746f207461626c65730a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a4352454154452046554e4354494f4e2052454e414d455f53455155454e4345284f4c445f4e414d4520564152434841522c204e45575f4e414d452056415243484152292052455455524e5320494e54454745522041532024240a4445434c4152450a2020435552525f5345515f56414c202020494e54454745523b0a424547494e0a202053454c45435420494e544f20435552525f5345515f56414c204e45585456414c284f4c445f4e414d45293b0a20204558454355544520274352454154452053455155454e43452027207c7c204e45575f4e414d45207c7c202720535441525420574954482027207c7c20435552525f5345515f56414c3b0a202045584543555445202744524f502053455155454e43452027207c7c204f4c445f4e414d453b0a202052455455524e20435552525f5345515f56414c3b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020437265617465207472696767657220434f4e54524f4c4c45445f564f434142554c4152595f434845434b200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e20434f4e54524f4c4c45445f564f434142554c4152595f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f636f64652020434f44453b0a424547494e0a0a20202073656c65637420636f646520696e746f20765f636f64652066726f6d20646174615f7479706573207768657265206964203d204e45572e646174795f69643b0a0a2020202d2d20436865636b206966207468652064617461206973206f6620747970652022434f4e54524f4c4c4544564f434142554c415259220a202020696620765f636f6465203d2027434f4e54524f4c4c4544564f434142554c41525927207468656e0a2020202020206966204e45572e636f766f5f6964204953204e554c4c207468656e0a202020202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f662050726f706572747920547970652028436f64653a202529206661696c65642c206173206974732044617461205479706520697320434f4e54524f4c4c4544564f434142554c4152592c20627574206974206973206e6f74206c696e6b656420746f206120436f6e74726f6c6c656420566f636162756c6172792e272c204e45572e636f64653b0a202020202020656e642069663b0a202020656e642069663b0a0a20202052455455524e204e45573b0a0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220434f4e54524f4c4c45445f564f434142554c4152595f434845434b204245464f524520494e53455254204f5220555044415445204f4e2050524f50455254595f54595045530a20202020464f52204541434820524f5720455845435554452050524f43454455524520434f4e54524f4c4c45445f564f434142554c4152595f434845434b28293b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520747269676765722045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e2045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f636f766f5f636f64652020434f44453b0a202020646174615f636f646520434f44453b0a424547494e0a0a20202073656c65637420636f646520696e746f20765f636f766f5f636f64652066726f6d20636f6e74726f6c6c65645f766f636162756c61726965730a20202020202077686572652069735f696e7465726e616c5f6e616d657370616365203d207472756520616e64200a2020202020202020206964203d202873656c65637420636f766f5f69642066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d73207768657265206964203d204e45572e637674655f69645f73746f725f666d74293b0a2020202d2d20436865636b2069662074686520646174612073746f7261676520666f726d61742069732061207465726d206f662074686520636f6e74726f6c6c656420766f636162756c617279202253544f524147455f464f524d4154220a202020696620765f636f766f5f636f646520213d202753544f524147455f464f524d415427207468656e0a20202020202073656c65637420636f646520696e746f20646174615f636f64652066726f6d20646174615f616c6c207768657265206964203d204e45572e646174615f69643b200a202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f6620446174612028436f64653a202529206661696c65642c206173206974732053746f7261676520466f726d617420697320252c2062757420697320726571756972656420746f2062652053544f524147455f464f524d41542e272c20646174615f636f64652c20765f636f766f5f636f64653b0a202020656e642069663b0a0a20202052455455524e204e45573b0a0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b204245464f524520494e53455254204f5220555044415445204f4e2045585445524e414c5f444154410a20202020464f52204541434820524f5720455845435554452050524f4345445552452045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b28293b0a0a2020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520747269676765727320666f7220636865636b696e672073616d706c6520636f646520756e697175656e657373200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a202020200a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f66696c6c5f636f64655f756e697175655f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20204e45572e636f64655f756e697175655f636865636b203d204e45572e636f6465207c7c20272c27207c7c20636f616c65736365284e45572e73616d705f69645f706172745f6f662c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e70726f6a5f69642c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e73706163655f69642c202d31293b0a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a20200a20200a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f66696c6c5f737562636f64655f756e697175655f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a4445434c4152450a20202020756e697175655f737562636f64652020424f4f4c45414e5f434841523b0a424547494e0a2020202053454c4543542069735f737562636f64655f756e6971756520696e746f20756e697175655f737562636f64652046524f4d2073616d706c655f7479706573205748455245206964203d204e45572e736174795f69643b0a202020200a2020202049462028756e697175655f737562636f646529205448454e0a202020204e45572e737562636f64655f756e697175655f636865636b203d204e45572e636f6465207c7c20272c27207c7c20636f616c65736365284e45572e736174795f69642c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e70726f6a5f69642c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e73706163655f69642c202d31293b0a20202020454c53450a202020204e45572e737562636f64655f756e697175655f636865636b203d204e554c4c3b0a2020454e442049463b0a20200a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2064697361626c655f70726f6a6563745f6c6576656c5f73616d706c657328290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20202020494620284e45572e70726f6a5f6964204953204e4f54204e554c4c29205448454e0a20202020524149534520455843455054494f4e202750726f6a656374206c6576656c2073616d706c6573206172652064697361626c6564273b0a2020454e442049463b0a20200a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a20200a20200a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f747970655f66696c6c5f737562636f64655f756e697175655f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20202020494620284e45572e69735f737562636f64655f756e697175653a3a626f6f6c65616e203c3e204f4c442e69735f737562636f64655f756e697175653a3a626f6f6c65616e29205448454e0a2020202020205550444154452073616d706c65735f616c6c2053455420737562636f64655f756e697175655f636865636b203d20737562636f64655f756e697175655f636865636b20574845524520736174795f6964203d204e45572e69643b0a2020454e442049463b0a2020202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a20200a43524541544520545249474745522073616d706c655f66696c6c5f636f64655f756e697175655f636865636b0a20204245464f524520494e53455254204f52205550444154450a20204f4e2073616d706c65735f616c6c0a2020464f52204541434820524f570a2020455845435554452050524f4345445552452073616d706c655f66696c6c5f636f64655f756e697175655f636865636b28293b0a0a43524541544520545249474745522064697361626c655f70726f6a6563745f6c6576656c5f73616d706c65730a20204245464f524520494e53455254204f52205550444154450a20204f4e2073616d706c65735f616c6c0a2020464f52204541434820524f570a2020455845435554452050524f4345445552452064697361626c655f70726f6a6563745f6c6576656c5f73616d706c657328293b0a0a0a43524541544520545249474745522073616d706c655f66696c6c5f737562636f64655f756e697175655f636865636b0a20204245464f524520494e53455254204f52205550444154450a20204f4e2073616d706c65735f616c6c0a2020464f52204541434820524f570a2020455845435554452050524f4345445552452073616d706c655f66696c6c5f737562636f64655f756e697175655f636865636b28293b0a20200a43524541544520545249474745522073616d706c655f747970655f66696c6c5f737562636f64655f756e697175655f636865636b0a20204146544552205550444154450a20204f4e2073616d706c655f74797065730a2020464f52204541434820524f570a2020455845435554452050524f4345445552452073616d706c655f747970655f66696c6c5f737562636f64655f756e697175655f636865636b28293b0a202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a207472696767657220666f72206461746120736574733a20546865792073686f756c64206265206c696e6b656420746f20616e206578706572696d656e74206f7220612073616d706c6520776974682073706163650a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e20646174615f6578705f6f725f73616d706c655f6c696e6b5f636865636b28292052455455524e5320747269676765722041532024240a4445434c4152450a202073706163655f696420434f44453b0a202073616d706c655f636f646520434f44453b0a424547494e0a20206966204e45572e657870655f6964204953204e4f54204e554c4c207468656e0a2020202052455455524e204e45573b0a2020656e642069663b0a20206966204e45572e73616d705f6964204953204e554c4c207468656e0a20202020524149534520455843455054494f4e20274e656974686572206578706572696d656e74206e6f722073616d706c652069732073706563696669656420666f722064617461207365742025272c204e45572e636f64653b0a2020656e642069663b0a202073656c65637420732e69642c20732e636f646520696e746f2073706163655f69642c2073616d706c655f636f64652066726f6d2073616d706c65735f616c6c207320776865726520732e6964203d204e45572e73616d705f69643b0a202069662073706163655f6964206973204e554c4c207468656e0a20202020524149534520455843455054494f4e202753616d706c6520252069732061207368617265642073616d706c652e272c2073616d706c655f636f64653b0a2020656e642069663b0a202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220646174615f6578705f6f725f73616d706c655f6c696e6b5f636865636b204245464f524520494e53455254204f5220555044415445204f4e20646174615f616c6c0a464f52204541434820524f5720455845435554452050524f43454455524520646174615f6578705f6f725f73616d706c655f6c696e6b5f636865636b28293b0a20200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652074726967676572204d4154455249414c2f53414d504c452f4558504552494d454e542f444154415f534554205f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b0a2d2d202020202020202020202020497420636865636b732074686174206966206d6174657269616c2070726f70657274792076616c75652069732061737369676e656420746f2074686520656e746974792c0a2d2d0909090909097468656e20746865206d6174657269616c207479706520697320657175616c20746f20746865206f6e65206465736372696265642062792070726f706572747920747970652e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e204d4154455249414c5f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d206d6174657269616c5f747970655f70726f70657274795f747970657320657470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e6d7470745f6964203d20657470742e696420414e4420657470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a09090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a4352454154452054524947474552204d4154455249414c5f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e206d6174657269616c5f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f434544555245204d4154455249414c5f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a202020200a435245415445204f52205245504c4143452046554e4354494f4e2053414d504c455f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d2073616d706c655f747970655f70726f70657274795f747970657320657470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e737470745f6964203d20657470742e696420414e4420657470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a090909090909090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522053414d504c455f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e2073616d706c655f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f4345445552452053414d504c455f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a202020200a435245415445204f52205245504c4143452046554e4354494f4e204558504552494d454e545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d206578706572696d656e745f747970655f70726f70657274795f747970657320657470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e657470745f6964203d20657470742e696420414e4420657470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a090909090909090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a4352454154452054524947474552204558504552494d454e545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e206578706572696d656e745f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f434544555245204558504552494d454e545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a200a202d2d2064617461207365740a435245415445204f52205245504c4143452046554e4354494f4e20444154415f5345545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d20646174615f7365745f747970655f70726f70657274795f74797065732064737470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e64737470745f6964203d2064737470742e696420414e442064737470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a090909090909090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220444154415f5345545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e20646174615f7365745f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f43454455524520444154415f5345545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20507572706f73653a2043726561746520444546455252454420747269676765727320666f7220636865636b696e6720636f6e73697374656e6379206f662064656c6574696f6e2073746174652e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d207574696c6974792066756e6374696f6e2064657363726962696e6720612064656c6574696f6e0a0a435245415445204f52205245504c4143452046554e4354494f4e2064656c6574696f6e5f6465736372697074696f6e2864656c5f696420544543485f4944292052455455524e5320564152434841522041532024240a4445434c4152450a202064656c5f706572736f6e20564152434841523b0a202064656c5f6461746520564152434841523b0a202064656c5f726561736f6e20564152434841523b0a424547494e0a202053454c45435420702e6c6173745f6e616d65207c7c20272027207c7c20702e66697273745f6e616d65207c7c2027202827207c7c20702e656d61696c207c7c202729272c200a202020202020202020746f5f6368617228642e726567697374726174696f6e5f74696d657374616d702c2027595959592d4d4d2d44442048483a4d4d3a535327292c20642e726561736f6e200a20202020494e544f2064656c5f706572736f6e2c2064656c5f646174652c2064656c5f726561736f6e2046524f4d2064656c6574696f6e7320642c20706572736f6e732070200a20202020574845524520642e706572735f69645f72656769737465726572203d20702e696420414e4420642e6964203d2064656c5f69643b0a202052455455524e202764656c657465642062792027207c7c2064656c5f706572736f6e207c7c2027206f6e2027207c7c2064656c5f64617465207c7c2027207769746820726561736f6e3a202227207c7c2064656c5f726561736f6e207c7c202722273b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20312e2064617461207365740a2d2d2d206f6e20696e736572742f757064617465202d2064656c65746564206578706572696d656e74206f722073616d706c652063616e277420626520636f6e6e65637465640a2d2d2d2020202020202020202020202020202020202d20706172656e74732f6368696c6472656e2072656c6174696f6e7368697020737461797320756e6368616e676564200a0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f637265617465645f6f725f6d6f6469666965645f646174615f7365745f6f776e65725f69735f616c69766528292052455455524e5320747269676765722041532024240a4445434c4152450a096f776e65725f636f646509434f44453b0a096f776e65725f64656c5f696409544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0a20202d2d20636865636b2073616d706c650a2020494620284e45572e73616d705f6964204953204e4f54204e554c4c29205448454e0a20200953454c4543542064656c5f69642c20636f646520494e544f206f776e65725f64656c5f69642c206f776e65725f636f64650a202009202046524f4d2073616d706c6573200a20200920205748455245206964203d204e45572e73616d705f69643b0a202009494620286f776e65725f64656c5f6964204953204e4f54204e554c4c29205448454e200a090909524149534520455843455054494f4e202744617461205365742028436f64653a2025292063616e6e6f7420626520636f6e6e656374656420746f20612053616d706c652028436f64653a20252920252e272c200a090909202020202020202020202020202020204e45572e636f64652c206f776e65725f636f64652c2064656c6574696f6e5f6465736372697074696f6e286f776e65725f64656c5f6964293b0a0909454e442049463b0a09454e442049463b0a092d2d20636865636b206578706572696d656e740a2020494620284e45572e657870655f6964204953204e4f54204e554c4c29205448454e0a090953454c4543542064656c5f69642c20636f646520494e544f206f776e65725f64656c5f69642c206f776e65725f636f64650a092020202046524f4d206578706572696d656e7473200a09202020205748455245206964203d204e45572e657870655f69643b0a092020494620286f776e65725f64656c5f6964204953204e4f54204e554c4c29205448454e200a090909524149534520455843455054494f4e202744617461205365742028436f64653a2025292063616e6e6f7420626520636f6e6e656374656420746f20616e204578706572696d656e742028436f64653a20252920252e272c200a090909202020202020202020202020202020204e45572e636f64652c206f776e65725f636f64652c2064656c6574696f6e5f6465736372697074696f6e286f776e65725f64656c5f6964293b0a0909454e442049463b090a09454e442049463b090a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a20200a43524541544520434f4e53545241494e54205452494747455220636865636b5f637265617465645f6f725f6d6f6469666965645f646174615f7365745f6f776e65725f69735f616c697665200a09414654455220494e53455254204f5220555044415445204f4e20646174615f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f570a09455845435554452050524f43454455524520636865636b5f637265617465645f6f725f6d6f6469666965645f646174615f7365745f6f776e65725f69735f616c69766528293b0a090a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20322e2073616d706c650a2d2d2d206f6e20696e736572742f757064617465202d3e206578706572696d656e742063616e27742062652064656c6574656420756e6c657373207468652073616d706c652069732064656c657465640a2d2d2d2064656c6574696f6e200a2d2d2d2d3e20616c6c206469726563746c7920636f6e6e656374656420646174612073657473206e65656420746f2062652064656c657465640a2d2d2d2d3e20616c6c20636f6d706f6e656e747320616e64206368696c6472656e206e65656420746f2062652064656c657465640a0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f637265617465645f6f725f6d6f6469666965645f73616d706c655f6f776e65725f69735f616c69766528292052455455524e5320747269676765722041532024240a4445434c4152450a096f776e65725f636f646509434f44453b0a096f776e65725f64656c5f696409544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0a20202d2d20636865636b206578706572696d656e74202863616e27742062652064656c65746564290a2020494620284e45572e657870655f6964204953204e4f54204e554c4c29205448454e0a20200953454c4543542064656c5f69642c20636f646520494e544f206f776e65725f64656c5f69642c206f776e65725f636f64650a202009202046524f4d206578706572696d656e7473200a20200920205748455245206964203d204e45572e657870655f69643b0a202009494620286f776e65725f64656c5f6964204953204e4f54204e554c4c29205448454e200a090909524149534520455843455054494f4e202753616d706c652028436f64653a2025292063616e6e6f7420626520636f6e6e656374656420746f20616e204578706572696d656e742028436f64653a20252920252e272c200a2020200909202020202020202020202020202020204e45572e636f64652c206f776e65725f636f64652c2064656c6574696f6e5f6465736372697074696f6e286f776e65725f64656c5f6964293b0a0909454e442049463b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a20200a43524541544520434f4e53545241494e54205452494747455220636865636b5f637265617465645f6f725f6d6f6469666965645f73616d706c655f6f776e65725f69735f616c697665200a2020414654455220494e53455254204f5220555044415445204f4e2073616d706c65735f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f570a09455845435554452050524f43454455524520636865636b5f637265617465645f6f725f6d6f6469666965645f73616d706c655f6f776e65725f69735f616c69766528293b0a090a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f64656c6574696f6e28292052455455524e5320747269676765722041532024240a4445434c4152450a2020636f756e7465722020494e54454745523b0a424547494e0a09494620284f4c442e64656c5f6964204953204e4f54204e554c4c204f52204e45572e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0a20202d2d20616c6c206469726563746c7920636f6e6e656374656420646174612073657473206e65656420746f2062652064656c657465640a20202d2d20636865636b2064617461736574730a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d20646174610a092020574845524520646174612e73616d705f6964203d204e45572e696420414e4420646174612e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e202753616d706c652028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f66206974732064617461207365747320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a20202d2d20616c6c20636f6d706f6e656e7473206e65656420746f2062652064656c657465640a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d2073616d706c6573200a09202057484552452073616d706c65732e73616d705f69645f706172745f6f66203d204e45572e696420414e442073616d706c65732e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e202753616d706c652028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f662069747320636f6d706f6e656e742073616d706c657320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a20200a43524541544520434f4e53545241494e54205452494747455220636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f64656c6574696f6e200a2020414654455220555044415445204f4e2073616d706c65735f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f57200a09455845435554452050524f43454455524520636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f64656c6574696f6e28293b090a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d207570646174652073616d706c652072656c6174696f6e7368697073206f6e20726576657274200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e2070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f72656c6174696f6e736869707328292052455455524e5320747269676765722041532024240a4445434c4152450a202064656c69642020544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c204f52204f4c442e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d2053414d504c45535f414c4c207768657265206964203d204e45572e73616d706c655f69645f706172656e743b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d2053414d504c45535f414c4c207768657265206964203d204e45572e73616d706c655f69645f6368696c643b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f72656c6174696f6e7368697073200a20204245464f524520555044415445204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c0a09464f52204541434820524f57200a09455845435554452050524f4345445552452070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f72656c6174696f6e736869707328293b0a090a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2075706461746520646174617365742072656c6174696f6e7368697073206f6e20726576657274200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f646174615f7365745f72656c6174696f6e736869707328292052455455524e5320747269676765722041532024240a4445434c4152450a202064656c69642020544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c204f52204f4c442e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d20444154415f414c4c207768657265206964203d204e45572e646174615f69645f706172656e743b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d20444154415f414c4c207768657265206964203d204e45572e646174615f69645f6368696c643b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f646174615f7365745f72656c6174696f6e7368697073200a20204245464f524520555044415445204f4e20646174615f7365745f72656c6174696f6e73686970735f616c6c0a09464f52204541434820524f57200a09455845435554452050524f4345445552452070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f646174615f7365745f72656c6174696f6e736869707328293b0a090a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20332e206578706572696d656e740a2d2d2d2064656c6574696f6e202d3e20616c6c206469726563746c7920636f6e6e65637465642073616d706c657320616e6420646174612073657473206e65656420746f2062652064656c657465640a0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f6578706572696d656e745f64656c6574696f6e28292052455455524e5320747269676765722041532024240a4445434c4152450a2020636f756e7465722020494e54454745523b0a424547494e0a09494620284f4c442e64656c5f6964204953204e4f54204e554c4c204f52204e45572e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a090a20202d2d20636865636b2064617461736574730a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d20646174610a092020574845524520646174612e657870655f6964203d204e45572e696420414e4420646174612e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e20274578706572696d656e742028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f66206974732064617461207365747320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a092d2d20636865636b2073616d706c65730a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d2073616d706c6573200a09202057484552452073616d706c65732e657870655f6964203d204e45572e696420414e442073616d706c65732e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e20274578706572696d656e742028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f66206974732073616d706c657320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520434f4e53545241494e54205452494747455220636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f6578706572696d656e745f64656c6574696f6e200a2020414654455220555044415445204f4e206578706572696d656e74735f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f57200a09455845435554452050524f43454455524520636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f6578706572696d656e745f64656c6574696f6e28293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2052756c657320666f722076696577730a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452052554c452073616d706c655f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c657320444f20494e5354454144200a20202020202020494e5345525420494e544f2073616d706c65735f616c6c20280a20202020202020202069642c200a202020202020202020636f64652c200a20202020202020202064656c5f69642c0a2020202020202020206f7269675f64656c2c0a202020202020202020657870655f69642c0a20202020202020202070726f6a5f69642c0a2020202020202020206d6f64696669636174696f6e5f74696d657374616d702c0a2020202020202020207065726d5f69642c0a202020202020202020706572735f69645f726567697374657265722c200a202020202020202020706572735f69645f6d6f6469666965722c200a202020202020202020726567697374726174696f6e5f74696d657374616d702c200a20202020202020202073616d705f69645f706172745f6f662c0a202020202020202020736174795f69642c200a20202020202020202073706163655f69642c0a20202020202020202076657273696f6e0a20202020202020292056414c55455320280a2020202020202020204e45572e69642c200a2020202020202020204e45572e636f64652c200a2020202020202020204e45572e64656c5f69642c0a2020202020202020204e45572e6f7269675f64656c2c0a2020202020202020204e45572e657870655f69642c0a2020202020202020204e45572e70726f6a5f69642c0a2020202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a2020202020202020204e45572e7065726d5f69642c0a2020202020202020204e45572e706572735f69645f726567697374657265722c200a2020202020202020204e45572e706572735f69645f6d6f6469666965722c200a2020202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c200a2020202020202020204e45572e73616d705f69645f706172745f6f662c0a2020202020202020204e45572e736174795f69642c200a2020202020202020204e45572e73706163655f69642c0a2020202020202020204e45572e76657273696f6e0a20202020202020293b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c657320444f20494e5354454144200a202020202020205550444154452073616d706c65735f616c6c0a2020202020202020202053455420636f6465203d204e45572e636f64652c0a202020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a2020202020202020202020202020657870655f6964203d204e45572e657870655f69642c0a202020202020202020202020202070726f6a5f6964203d204e45572e70726f6a5f69642c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a20202020202020202020202020207065726d5f6964203d204e45572e7065726d5f69642c0a2020202020202020202020202020706572735f69645f72656769737465726572203d204e45572e706572735f69645f726567697374657265722c0a2020202020202020202020202020706572735f69645f6d6f646966696572203d204e45572e706572735f69645f6d6f6469666965722c0a2020202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020202073616d705f69645f706172745f6f66203d204e45572e73616d705f69645f706172745f6f662c0a2020202020202020202020202020736174795f6964203d204e45572e736174795f69642c0a202020202020202020202020202073706163655f6964203d204e45572e73706163655f69642c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c657320444f20494e53544541440a2020202020202044454c4554452046524f4d2073616d706c65735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f64656c657465645f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f64656c6574656420444f20494e53544541440a202020202020205550444154452073616d706c65735f616c6c0a202020202020202020205345542064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f64656c657465645f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c65735f64656c6574656420444f20494e53544541440a2020202020202044454c4554452046524f4d2073616d706c65735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a20202020202020202020202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d206578706572696d656e74202d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452052554c45206578706572696d656e745f696e736572742041530a20204f4e20494e5345525420544f206578706572696d656e747320444f20494e5354454144200a2020202020494e5345525420494e544f206578706572696d656e74735f616c6c20280a2020202020202069642c200a20202020202020636f64652c200a2020202020202064656c5f69642c0a202020202020206f7269675f64656c2c0a20202020202020657874795f69642c200a2020202020202069735f7075626c69632c0a202020202020206d6f64696669636174696f6e5f74696d657374616d702c0a202020202020207065726d5f69642c0a20202020202020706572735f69645f726567697374657265722c200a20202020202020706572735f69645f6d6f6469666965722c200a2020202020202070726f6a5f69642c0a20202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202076657273696f6e0a2020202020292056414c55455320280a202020202020204e45572e69642c200a202020202020204e45572e636f64652c200a202020202020204e45572e64656c5f69642c0a202020202020204e45572e6f7269675f64656c2c0a202020202020204e45572e657874795f69642c200a202020202020204e45572e69735f7075626c69632c0a202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020204e45572e7065726d5f69642c0a202020202020204e45572e706572735f69645f726567697374657265722c200a202020202020204e45572e706572735f69645f6d6f6469666965722c200a202020202020204e45572e70726f6a5f69642c0a202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020204e45572e76657273696f6e0a2020202020293b0a20202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e747320444f20494e5354454144200a20202020202020555044415445206578706572696d656e74735f616c6c0a2020202020202020202053455420636f6465203d204e45572e636f64652c0a202020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a2020202020202020202020202020657874795f6964203d204e45572e657874795f69642c0a202020202020202020202020202069735f7075626c6963203d204e45572e69735f7075626c69632c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a20202020202020202020202020207065726d5f6964203d204e45572e7065726d5f69642c0a2020202020202020202020202020706572735f69645f72656769737465726572203d204e45572e706572735f69645f726567697374657265722c0a2020202020202020202020202020706572735f69645f6d6f646966696572203d204e45572e706572735f69645f6d6f6469666965722c0a202020202020202020202020202070726f6a5f6964203d204e45572e70726f6a5f69642c0a2020202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e747320444f20494e53544541440a2020202020202044454c4554452046524f4d206578706572696d656e74735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a2020202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e74735f64656c657465645f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e74735f64656c6574656420444f20494e5354454144200a20202020202020555044415445206578706572696d656e74735f616c6c0a202020202020202020205345542064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e74735f64656c657465645f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e74735f64656c6574656420444f20494e53544541440a2020202020202044454c4554452046524f4d206578706572696d656e74735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a2020202020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202020200a2d2d2d2d2d2d2d2d2d2d0a2d2d2064617461202d2d0a2d2d2d2d2d2d2d2d2d2d0a2020202020200a435245415445204f52205245504c4143452052554c4520646174615f696e736572742041530a20204f4e20494e5345525420544f206461746120444f20494e5354454144200a2020202020494e5345525420494e544f20646174615f616c6c20280a2020202020202069642c200a20202020202020636f64652c200a2020202020202064656c5f69642c0a202020202020206f7269675f64656c2c0a20202020202020657870655f69642c0a20202020202020646173745f69642c0a20202020202020646174615f70726f64756365725f636f64652c0a20202020202020647374795f69642c0a2020202020202069735f646572697665642c0a2020202020202069735f76616c69642c0a202020202020206d6f64696669636174696f6e5f74696d657374616d702c0a202020202020206163636573735f74696d657374616d702c0a20202020202020706572735f69645f726567697374657265722c0a20202020202020706572735f69645f6d6f6469666965722c0a2020202020202070726f64756374696f6e5f74696d657374616d702c0a20202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202073616d705f69642c0a2020202020202076657273696f6e0a2020202020292056414c55455320280a202020202020204e45572e69642c200a202020202020204e45572e636f64652c200a202020202020204e45572e64656c5f69642c200a202020202020204e45572e6f7269675f64656c2c0a202020202020204e45572e657870655f69642c0a202020202020204e45572e646173745f69642c0a202020202020204e45572e646174615f70726f64756365725f636f64652c0a202020202020204e45572e647374795f69642c0a202020202020204e45572e69735f646572697665642c200a202020202020204e45572e69735f76616c69642c0a202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020204e45572e6163636573735f74696d657374616d702c0a202020202020204e45572e706572735f69645f726567697374657265722c0a202020202020204e45572e706572735f69645f6d6f6469666965722c0a202020202020204e45572e70726f64756374696f6e5f74696d657374616d702c0a202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020204e45572e73616d705f69642c0a202020202020204e45572e76657273696f6e0a2020202020293b0a20202020200a435245415445204f52205245504c4143452052554c4520646174615f7570646174652041530a202020204f4e2055504441544520544f206461746120444f20494e5354454144200a2020202020202055504441544520646174615f616c6c0a2020202020202020202053455420636f6465203d204e45572e636f64652c0a202020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a2020202020202020202020202020657870655f6964203d204e45572e657870655f69642c0a2020202020202020202020202020646173745f6964203d204e45572e646173745f69642c0a2020202020202020202020202020646174615f70726f64756365725f636f6465203d204e45572e646174615f70726f64756365725f636f64652c0a2020202020202020202020202020647374795f6964203d204e45572e647374795f69642c0a202020202020202020202020202069735f64657269766564203d204e45572e69735f646572697665642c0a202020202020202020202020202069735f76616c6964203d204e45572e69735f76616c69642c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a20202020202020202020202020206163636573735f74696d657374616d70203d204e45572e6163636573735f74696d657374616d702c0a2020202020202020202020202020706572735f69645f72656769737465726572203d204e45572e706572735f69645f726567697374657265722c0a2020202020202020202020202020706572735f69645f6d6f646966696572203d204e45572e706572735f69645f6d6f6469666965722c0a202020202020202020202020202070726f64756374696f6e5f74696d657374616d70203d204e45572e70726f64756374696f6e5f74696d657374616d702c0a2020202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020202073616d705f6964203d204e45572e73616d705f69642c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020205748455245206964203d204e45572e69643b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c4520646174615f616c6c2041530a202020204f4e2044454c45544520544f206461746120444f20494e53544541440a2020202020202044454c4554452046524f4d20646174615f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c4520646174615f64656c657465645f7570646174652041530a202020204f4e2055504441544520544f20646174615f64656c6574656420444f20494e5354454144200a2020202020202055504441544520646174615f616c6c0a202020202020202020205345542064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c4520646174615f64656c657465645f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f64656c6574656420444f20494e53544541440a2020202020202044454c4554452046524f4d20646174615f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b2020202020202020202020202020200a20202020202020202020202020200a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2052756c657320666f722070726f7065727469657320686973746f72790a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a2d2d204d6174657269616c2050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c45206d6174657269616c5f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f206d6174657269616c5f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f200a20202020202020494e5345525420494e544f206d6174657269616c5f70726f706572746965735f686973746f727920280a20202020202020202049442c200a2020202020202020204d4154455f49442c200a2020202020202020204d5450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274d4154455249414c5f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e4d4154455f49442c200a2020202020202020204f4c442e4d5450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c45206d6174657269616c5f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f206d6174657269616c5f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c0a20202020444f20414c534f200a20202020202020494e5345525420494e544f206d6174657269616c5f70726f706572746965735f686973746f727920280a20202020202020202049442c200a2020202020202020204d4154455f49442c200a2020202020202020204d5450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274d4154455249414c5f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e4d4154455f49442c200a2020202020202020204f4c442e4d5450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d204578706572696d656e742050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e745f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f200a20202020202020494e5345525420494e544f206578706572696d656e745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a202020202020202020455850455f49442c0a202020202020202020455450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e455850455f49442c200a2020202020202020204f4c442e455450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e745f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c0a20202020444f20414c534f200a20202020202020494e5345525420494e544f206578706572696d656e745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a202020202020202020455850455f49442c0a202020202020202020455450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e455850455f49442c200a2020202020202020204f4c442e455450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d2053616d706c652050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c452073616d706c655f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f70726f706572746965730a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f0a20202020202020494e5345525420494e544f2073616d706c655f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202053414d505f49442c0a202020202020202020535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e53414d505f49442c200a2020202020202020204f4c442e535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c655f70726f70657274696573200a2020202057484552452028284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c290a09202020414e44202853454c4543542044454c5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204f4c442e53414d505f494429204953204e554c4c0a2020202020444f20414c534f0a20202020202020494e5345525420494e544f2073616d706c655f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202053414d505f49442c0a202020202020202020535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e53414d505f49442c200a2020202020202020204f4c442e535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d2044617461205365742050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f0a20202020202020494e5345525420494e544f20646174615f7365745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202044535f49442c0a20202020202020202044535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e44535f49442c200a2020202020202020204f4c442e44535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f7365745f70726f70657274696573200a2020202057484552452028284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c290a09202020414e44202853454c4543542044454c5f49442046524f4d20444154415f414c4c205748455245204944203d204f4c442e44535f494429204953204e554c4c0a20202020444f20414c534f0a20202020202020494e5345525420494e544f20646174615f7365745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202044535f49442c0a20202020202020202044535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e44535f49442c200a2020202020202020204f4c442e44535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d20456e64206f662072756c657320666f722070726f7065727469657320686973746f72790a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f72656c6174696f6e73686970735f696e736572742041530a202020204f4e20494e5345525420544f20646174615f7365745f72656c6174696f6e736869707320444f20494e5354454144200a20202020202020494e5345525420494e544f20646174615f7365745f72656c6174696f6e73686970735f616c6c20280a202020202020202020646174615f69645f706172656e742c200a202020202020202020646174615f69645f6368696c642c0a202020202020202020706572735f69645f617574686f722c0a20202020202020202072656c6174696f6e736869705f69642c0a2020202020202020206f7264696e616c2c0a202020202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202020206d6f64696669636174696f6e5f74696d657374616d700a20202020202020292056414c55455320280a2020202020202020204e45572e646174615f69645f706172656e742c200a2020202020202020204e45572e646174615f69645f6368696c642c0a2020202020202020204e45572e706572735f69645f617574686f722c0a2020202020202020204e45572e72656c6174696f6e736869705f69642c0a2020202020202020204e45572e6f7264696e616c2c0a2020202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a2020202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d700a20202020202020293b0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f72656c6174696f6e73686970735f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e736869707320444f20494e5354454144200a2020202020202055504441544520646174615f7365745f72656c6174696f6e73686970735f616c6c0a20202020202020202020534554200a202020202020202020202020646174615f69645f706172656e74203d204e45572e646174615f69645f706172656e742c200a202020202020202020202020646174615f69645f6368696c64203d204e45572e646174615f69645f6368696c642c200a20202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69642c0a2020202020202020202020206f7264696e616c203d204e45572e6f7264696e616c2c0a202020202020202020202020706572735f69645f617574686f72203d204e45572e706572735f69645f617574686f722c0a202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a2020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d700a20202020202020202020574845524520646174615f69645f706172656e74203d204e45572e646174615f69645f706172656e7420616e6420646174615f69645f6368696c64203d204e45572e646174615f69645f6368696c64200a20202020202020202020202020202020616e642072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69643b0a202020202020202020200a435245415445204f52205245504c4143452052554c4520646174615f7365745f72656c6174696f6e73686970735f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f7365745f72656c6174696f6e736869707320444f20494e53544541440a2020202020202044454c4554452046524f4d20646174615f7365745f72656c6174696f6e73686970735f616c6c0a2020202020202020202020202020574845524520646174615f69645f706172656e74203d204f4c442e646174615f69645f706172656e7420616e6420646174615f69645f6368696c64203d204f4c442e646174615f69645f6368696c640a2020202020202020202020202020202020202020616e642072656c6174696f6e736869705f6964203d204f4c442e72656c6174696f6e736869705f69643b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f72656c6174696f6e73686970735f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c655f72656c6174696f6e736869707320444f20494e5354454144200a20202020202020494e5345525420494e544f2073616d706c655f72656c6174696f6e73686970735f616c6c20280a20202020202020202069642c200a20202020202020202073616d706c655f69645f706172656e742c200a20202020202020202072656c6174696f6e736869705f69642c200a20202020202020202073616d706c655f69645f6368696c642c0a202020202020202020706572735f69645f617574686f722c0a090909202020726567697374726174696f6e5f74696d657374616d702c0a2020200920202020206d6f64696669636174696f6e5f74696d657374616d700a20202020202020292056414c55455320280a2020202020202020204e45572e69642c200a2020202020202020204e45572e73616d706c655f69645f706172656e742c200a2020202020202020204e45572e72656c6174696f6e736869705f69642c200a2020202020202020204e45572e73616d706c655f69645f6368696c642c0a2020202020202020204e45572e706572735f69645f617574686f722c0a0909092020204e45572e726567697374726174696f6e5f74696d657374616d702c0a0909092020204e45572e6d6f64696669636174696f6e5f74696d657374616d700a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f72656c6174696f6e73686970735f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f72656c6174696f6e736869707320444f20494e5354454144200a202020202020205550444154452073616d706c655f72656c6174696f6e73686970735f616c6c0a20202020202020202020534554200a09090920202020202073616d706c655f69645f706172656e74203d204e45572e73616d706c655f69645f706172656e742c200a09090920202020202072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69642c200a09090920202020202073616d706c655f69645f6368696c64203d204e45572e73616d706c655f69645f6368696c642c0a09090920202020202064656c5f6964203d204e45572e64656c5f69642c0a090909202020202020706572735f69645f617574686f72203d204e45572e706572735f69645f617574686f722c0a090909202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a0909092020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d700a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f72656c6174696f6e73686970735f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c655f72656c6174696f6e736869707320444f20494e53544541440a2020202020202044454c4554452046524f4d2073616d706c655f72656c6174696f6e73686970735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a0a435245415445204f52205245504c4143452052554c45204d45544150524f4a4543545f41535349474e4d454e54535f494e534552542041530a202020204f4e20494e5345525420544f204d45544150524f4a4543545f41535349474e4d454e545320444f20494e5354454144200a20202020202020494e5345525420494e544f204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20280a20202020202020202049442c200a2020202020202020204d4550525f49442c0a202020202020202020455850455f49442c0a09090920202053414d505f49442c0a090909202020444154415f49442c0a0909092020204d4154455f49442c0a09090920202044454c5f49442c0a0909092020204352454154494f4e5f444154450a20202020202020292056414c55455320280a2020202020202020204e45572e49442c200a2020202020202020204e45572e4d4550525f49442c0a2020202020202020204e45572e455850455f49442c0a0909092020204e45572e53414d505f49442c0a0909092020204e45572e444154415f49442c0a0909092020204e45572e4d4154455f49442c0a0909092020204e45572e44454c5f49442c0a0909092020204e45572e4352454154494f4e5f444154450a20202020202020293b0a0a435245415445204f52205245504c4143452052554c45204d45544150524f4a4543545f41535349474e4d454e54535f5550444154452041530a202020204f4e2055504441544520544f204d45544150524f4a4543545f41535349474e4d454e545320444f20494e5354454144200a20202020202020555044415445204d45544150524f4a4543545f41535349474e4d454e54535f414c4c0a20202020202020202020534554200a0909092020202020204944203d204e45572e49442c200a20202020202020202009094d4550525f4944203d204e45572e4d4550525f49442c0a2020202020202020200909455850455f4944203d204e45572e455850455f49442c0a090909202020090953414d505f4944203d204e45572e53414d505f49442c0a0909092020200909444154415f4944203d204e45572e444154415f49442c0a09090920202009094d4154455f4944203d204e45572e4d4154455f49442c0a090909202020090944454c5f4944203d204e45572e44454c5f49442c0a09090920202009094352454154494f4e5f44415445203d204e45572e4352454154494f4e5f444154450a202020202020202020205748455245204944203d204e45572e49443b0a202020202020202020200a435245415445204f52205245504c4143452052554c45204d45544150524f4a4543545f41535349474e4d454e54535f44454c4554452041530a202020204f4e2044454c45544520544f204d45544150524f4a4543545f41535349474e4d454e545320444f20494e53544541440a2020202020202044454c4554452046524f4d204d45544150524f4a4543545f41535349474e4d454e54535f414c4c0a202020202020202020205748455245204944203d204f4c442e49443b0a20202020202020202020202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2052756c657320666f722072656c6174696f6e736869707320686973746f72790a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a2d2d2073616d706c65202d3e206578706572696d656e740a0a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e455850455f494420213d204e45572e455850455f4944204f52204f4c442e455850455f4944204953204e554c4c2920414e44204e45572e455850455f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e455850455f4944204953204e4f54204e554c4c20414e44204e45572e455850455f4944204953204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e455850455f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e455850455f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f200a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020200a2d2d20636f6e7461696e65722073616d706c65730a202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e53414d505f49445f504152545f4f4620213d204e45572e53414d505f49445f504152545f4f46204f52204f4c442e53414d505f49445f504152545f4f46204953204e554c4c2920414e44204e45572e53414d505f49445f504152545f4f46204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e455227290a20202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e454427293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a20202020202020202027434f4e5441494e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a20202020202020202027434f4e5441494e4544272c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f49445f504152545f4f46292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e53414d505f49445f504152545f4f46204953204e4f54204e554c4c20414e44204e45572e53414d505f49445f504152545f4f46204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e455227290a20202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e454427293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e53414d505f49445f504152545f4f46204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a20202020202020202027434f4e5441494e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a20202020202020202027434f4e5441494e4544272c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f49445f504152545f4f46292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e53414d505f49445f504152545f4f46204953204e4f54204e554c4c0a20202020202020444f20414c534f200a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e4552273b0a0a2d2d2064617461736574202d3e2065706572696d656e740a0a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a20202020574845524520284f4c442e455850455f494420213d204e45572e455850455f4944204f52204f4c442e53414d505f4944204953204e4f54204e554c4c2920414e44204e45572e53414d505f4944204953204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a202020205748455245204f4c442e53414d505f4944204953204e554c4c20414e44204e45572e53414d505f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f444154415f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f696e736572742041530a202020204f4e20494e5345525420544f20646174615f616c6c200a202020205748455245204e45572e455850455f4944204953204e4f54204e554c4c20414e44204e45572e53414d505f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f616c6c200a202020205748455245204f4c442e455850455f4944204953204e4f54204e554c4c20414e44204f4c442e53414d505f4944204953204e554c4c0a20202020202020444f20414c534f200a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a0a2d2d2064617461736574202d3e2073616d706c650a0a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a20202020574845524520284f4c442e53414d505f494420213d204e45572e53414d505f4944204f52204f4c442e53414d505f4944204953204e554c4c2920414e44204e45572e53414d505f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53414d505f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a202020205748455245204f4c442e53414d505f4944204953204e4f54204e554c4c20414e44204e45572e53414d505f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f444154415f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f696e736572742041530a202020204f4e20494e5345525420544f20646174615f616c6c200a202020205748455245204e45572e53414d505f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53414d505f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f616c6c200a202020205748455245204f4c442e53414d505f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f200a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a0a2d2d2064617461207365742072656c6174696f6e736869700a0a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f696e736572742041530a202020204f4e20494e5345525420544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a20202020202020293b0a0a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e444154415f49445f504152454e54200a202020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f4348494c440a202020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a202020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f444154415f4944203d204f4c442e444154415f49445f4348494c44200a2020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f504152454e54200a2020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a2020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c20414e44204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e444154415f49445f504152454e54200a202020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f4348494c440a202020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a202020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f444154415f4944203d204f4c442e444154415f49445f4348494c44200a2020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f504152454e54200a2020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a2020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f74726173685f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e4f54204e554c4c20414e44204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e444154415f49445f504152454e54200a202020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f4348494c440a202020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a202020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f444154415f4944203d204f4c442e444154415f49445f4348494c44200a2020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f504152454e54200a2020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a2020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f74726173685f7265766572745f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204f4c442e44454c5f4944204953204e4f54204e554c4c20414e44204e45572e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a20202020202020293b0a0a2d2d2073616d706c657320706172656e742d6368696c642072656c6174696f6e736869700a0a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a202020202020202020202027504152454e54272c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a2020202020202020202020274348494c44272c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a2020202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442053414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442053414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e4f54204e554c4c20414e44204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a2020202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442053414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442053414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f7265766572745f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c20414e44204f4c442e44454c5f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a202020202020202020202027504152454e54272c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a2020202020202020202020274348494c44272c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a20202020202020293b0a0a2d2d206578706572696d656e74202d3e2070726f6a6563740a0a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e74735f616c6c200a20202020574845524520284f4c442e50524f4a5f494420213d204e45572e50524f4a5f4944204f52204f4c442e50524f4a5f4944204953204e554c4c2920414e44204e45572e50524f4a5f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e50524f4a5f494420414e4420455850455f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e50524f4a5f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e494420414e442050524f4a5f4944203d204f4c442e50524f4a5f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202050524f4a5f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e50524f4a5f49442c200a2020202020202020202853454c454354207065726d5f69642046524f4d2050524f4a45435453205748455245204944203d204e45572e50524f4a5f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e74735f616c6c200a202020205748455245204f4c442e50524f4a5f4944204953204e4f54204e554c4c20414e44204e45572e50524f4a5f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e50524f4a5f494420414e4420455850455f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e494420414e442050524f4a5f4944203d204f4c442e50524f4a5f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f696e736572742041530a202020204f4e20494e5345525420544f206578706572696d656e74735f616c6c200a202020205748455245204e45572e50524f4a5f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e50524f4a5f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202050524f4a5f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e50524f4a5f49442c200a2020202020202020202853454c454354207065726d5f69642046524f4d2050524f4a45435453205748455245204944203d204e45572e50524f4a5f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e74735f616c6c200a202020205748455245204f4c442e50524f4a5f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f200a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e50524f4a5f494420414e4420455850455f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a0a2d2d2070726f6a656374202d3e2073706163650a0a435245415445204f52205245504c4143452052554c452070726f6a6563745f73706163655f7570646174652041530a202020204f4e2055504441544520544f2070726f6a65637473200a20202020574845524520284f4c442e53504143455f494420213d204e45572e53504143455f4944204f52204f4c442e53504143455f4944204953204e554c4c2920414e44204e45572e53504143455f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452070726f6a6563745f73706163655f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2070726f6a65637473200a202020205748455245204f4c442e53504143455f4944204953204e4f54204e554c4c20414e44204e45572e53504143455f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452070726f6a6563745f73706163655f696e736572742041530a202020204f4e20494e5345525420544f2070726f6a65637473200a202020205748455245204e45572e53504143455f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a0a2d2d2073616d706c65202d3e2073706163650a0a435245415445204f52205245504c4143452052554c452073616d706c655f73706163655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e53504143455f494420213d204e45572e53504143455f4944204f52204f4c442e53504143455f4944204953204e554c4c204f52204f4c442e455850455f4944204953204e4f54204e554c4c2920414e44204e45572e53504143455f4944204953204e4f54204e554c4c20414e44204e45572e455850455f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f73706163655f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e53504143455f4944204953204e4f54204e554c4c20414e4420284e45572e53504143455f4944204953204e554c4c204f5220284f4c442e455850455f4944204953204e554c4c20414e44204e45572e455850455f4944204953204e4f54204e554c4c29290a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f73706163655f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e455850455f4944204953204e554c4c20414e44204e45572e53504143455f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2d2d20656e64206f662072756c657320666f722072656c6174696f6e736869707320686973746f72790a	\N
154	./sql/postgresql/154/grants-154.sql	SUCCESS	2016-04-20 11:16:39.78	\\x2d2d204772616e74696e672053454c4543542070726976696c65676520746f2067726f7570204f50454e4249535f524541444f4e4c590a0a4752414e542053454c454354204f4e2053455155454e4345206174746163686d656e745f636f6e74656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206174746163686d656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520636f64655f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f636f64655f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f636f64655f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520636f6e74726f6c6c65645f766f636162756c6172795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520636f72655f706c7567696e5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520637674655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f7365745f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f7365745f72656c6174696f6e736869705f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f7365745f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f73746f72655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520444154415f53544f52455f53455256494345535f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452064617461626173655f696e7374616e63655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452064737470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520657470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206576656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452066696c655f666f726d61745f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073706163655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452064656c6574696f6e5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206c6f6361746f725f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d6174657269616c5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d6174657269616c5f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d6174657269616c5f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d7470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345207065726d5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520706572736f6e5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452070726f6a6563745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452070726f70657274795f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520726f6c655f61737369676e6d656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520737470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520617574686f72697a6174696f6e5f67726f75705f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452066696c7465725f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452071756572795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520504f53545f524547495354524154494f4e5f444154415345545f51554555455f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520454e544954595f4f5045524154494f4e535f4c4f475f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345204558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452053414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452050524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d5f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345204d45544150524f4a4543545f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b200a4752414e542053454c454354204f4e2053455155454e4345204d45544150524f4a4543545f41535349474e4d454e545f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b200a4752414e542053454c454354204f4e2053455155454e434520677269645f637573746f6d5f636f6c756d6e735f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f72656c6174696f6e736869705f69645f7365712020544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345207363726970745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452072656c6174696f6e736869705f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206174746163686d656e745f636f6e74656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206174746163686d656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520636f6e74726f6c6c65645f766f636162756c617269657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520636f6e74726f6c6c65645f766f636162756c6172795f7465726d7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520636f72655f706c7567696e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206461746120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e20646174615f64656c6574656420544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e20646174615f7365745f72656c6174696f6e736869707320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f72656c6174696f6e73686970735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f73746f72657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520444154415f53544f52455f534552564943455320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f545950455320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452064617461626173655f76657273696f6e5f6c6f677320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206576656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e74735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206578706572696d656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206578706572696d656e74735f64656c6574656420544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452065787465726e616c5f6461746120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452066696c655f666f726d61745f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073706163657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452064656c6574696f6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206c6f6361746f725f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520706572736f6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452070726f6a6563747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452070726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520726f6c655f61737369676e6d656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c65735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c65735f64656c6574656420544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c655f72656c6174696f6e736869707320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520617574686f72697a6174696f6e5f67726f75707320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520617574686f72697a6174696f6e5f67726f75705f706572736f6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452066696c7465727320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45207175657269657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45207363726970747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520504f53545f524547495354524154494f4e5f444154415345545f515545554520544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520454e544954595f4f5045524154494f4e535f4c4f4720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c655f686973746f72795f7669657720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e20646174615f7365745f686973746f72795f7669657720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206578706572696d656e745f686973746f72795f7669657720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d5320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204c494e4b5f4441544120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520677269645f637573746f6d5f636f6c756d6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452072656c6174696f6e736869705f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204d45544150524f4a4543545320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e204d45544150524f4a4543545f41535349474e4d454e545320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204f5045524154494f4e5f455845435554494f4e5320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a	\N
154	./sql/generic/154/data-154.sql	SUCCESS	2016-04-20 11:16:39.886	\\x2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c6520504552534f4e530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f20706572736f6e730a2869640a2c66697273745f6e616d650a2c6c6173745f6e616d650a2c757365725f69640a2c656d61696c290a76616c7565730a286e65787476616c2827504552534f4e5f49445f53455127290a2c27270a2c2753797374656d2055736572270a2c2773797374656d270a2c2727293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520436f6e74726f6c6c656420566f636162756c6172792053544f524147455f464f524d41540a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a696e7365727420696e746f20636f6e74726f6c6c65645f766f636162756c6172696573200a20202020202020282069640a202020202020202c20636f64650a202020202020202c2069735f696e7465726e616c5f6e616d6573706163652020202020200a202020202020202c206465736372697074696f6e0a202020202020202c20706572735f69645f726567697374657265720a202020202020202c2069735f6d616e616765645f696e7465726e616c6c79290a76616c7565732020286e65787476616c2827434f4e54524f4c4c45445f564f434142554c4152595f49445f53455127290a202020202020202c202753544f524147455f464f524d4154270a202020202020202c20747275650a202020202020202c2027546865206f6e2d6469736b2073746f7261676520666f726d6174206f662061206461746120736574270a202020202020202c202873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a202020202020202c2074727565293b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520436f6e74726f6c6c656420566f636162756c617279205465726d7320666f722053544f524147455f464f524d41540a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a696e7365727420696e746f20636f6e74726f6c6c65645f766f636162756c6172795f7465726d73200a20202020202020282069640a202020202020202c20636f64650a202020202020202c20636f766f5f6964200a202020202020202c20706572735f69645f726567697374657265720a202020202020202c206f7264696e616c20290a76616c7565732020286e65787476616c2827435654455f49445f53455127290a202020202020202c202750524f5052494554415259270a202020202020202c202873656c6563742069642066726f6d20636f6e74726f6c6c65645f766f636162756c617269657320776865726520636f6465203d202753544f524147455f464f524d41542720616e642069735f696e7465726e616c5f6e616d657370616365203d2074727565290a202020202020202c202873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a202020202020202c2031293b0a0a696e7365727420696e746f20636f6e74726f6c6c65645f766f636162756c6172795f7465726d73200a20202020202020282069640a202020202020202c20636f64650a202020202020202c20636f766f5f6964200a202020202020202c20706572735f69645f726567697374657265720a202020202020202c206f7264696e616c290a76616c7565732020286e65787476616c2827435654455f49445f53455127290a202020202020202c20274244535f4449524543544f5259270a202020202020202c202873656c6563742069642066726f6d20636f6e74726f6c6c65645f766f636162756c617269657320776865726520636f6465203d202753544f524147455f464f524d41542720616e642069735f696e7465726e616c5f6e616d657370616365203d2074727565290a202020202020202c202873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a202020202020202c2032293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c6520444154415f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c2756415243484152270a2c2753686f72742074657874270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c274d554c54494c494e455f56415243484152270a202c274c6f6e672074657874270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c27494e5445474552270a2c27496e7465676572206e756d626572270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c275245414c270a2c275265616c206e756d6265722c20692e652e20616e20696e65786163742c207661726961626c652d707265636973696f6e206e756d657269632074797065270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c27424f4f4c45414e270a2c2754727565206f722046616c7365270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c2754494d455354414d50270a2c27426f7468206461746520616e642074696d652e20466f726d61743a20797979792d6d6d2d64642068683a6d6d3a7373270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c27434f4e54524f4c4c4544564f434142554c415259270a202c27436f6e74726f6c6c656420566f636162756c617279270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c274d4154455249414c270a202c275265666572656e636520746f2061206d6174657269616c270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c2748595045524c494e4b270a202c2741646472657373206f662061207765622070616765270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c27584d4c270a202c27584d4c20646f63756d656e74270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c652050524f50455254595f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2070726f70657274795f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a2c6c6162656c0a2c646174795f69640a2c706572735f69645f72656769737465726572290a76616c756573200a286e65787476616c282750524f50455254595f545950455f49445f53455127290a2c274445534352495054494f4e270a2c2741204465736372697074696f6e270a2c274465736372697074696f6e270a2c2873656c6563742069642066726f6d20646174615f747970657320776865726520636f6465203d275641524348415227290a2c2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c65204558504552494d454e545f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f206578706572696d656e745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c28274558504552494d454e545f545950455f49445f53455127290a2c27554e4b4e4f574e270a2c27556e6b6e6f776e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c652053414d504c455f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2073616d706c655f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c282753414d504c455f545950455f49445f53455127290a2c27554e4b4e4f574e270a2c27556e6b6e6f776e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c6520444154415f5345545f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f20646174615f7365745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f5345545f545950455f49445f53455127290a2c27554e4b4e4f574e270a2c27556e6b6e6f776e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c652046494c455f464f524d41545f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c2748444635270a2c2748696572617263686963616c204461746120466f726d61742046696c652c2076657273696f6e2035270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c2750524f5052494554415259270a2c2750726f707269657461727920466f726d61742046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c27535246270a2c2753657175656e6365205265616420466f726d61742046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c2754494646270a2c27544946462046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c27545356270a2c27546162205365706172617465642056616c7565732046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c27584d4c270a2c27584d4c2046696c65270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c65204c4f4341544f525f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f206c6f6361746f725f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c28274c4f4341544f525f545950455f49445f53455127290a2c2752454c41544956455f4c4f434154494f4e270a2c2752656c6174697665204c6f636174696f6e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c206461746120696e746f207461626c652052454c4154494f4e534849505f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2072656c6174696f6e736869705f74797065730a2869642c200a636f64652c200a6c6162656c2c200a706172656e745f6c6162656c2c200a6368696c645f6c6162656c2c200a6465736372697074696f6e2c200a706572735f69645f726567697374657265722c200a69735f6d616e616765645f696e7465726e616c6c792c200a69735f696e7465726e616c5f6e616d6573706163650a29200a76616c7565730a280a6e65787476616c282752454c4154494f4e534849505f545950455f49445f53455127292c0a27504152454e545f4348494c44272c0a27506172656e74202d204368696c64272c200a27506172656e74272c200a274368696c64272c200a27506172656e74202d204368696c642072656c6174696f6e73686970272c200a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27292c200a2754272c200a2754270a293b0a0a696e7365727420696e746f2072656c6174696f6e736869705f74797065730a2869642c200a636f64652c200a6c6162656c2c200a706172656e745f6c6162656c2c200a6368696c645f6c6162656c2c200a6465736372697074696f6e2c200a706572735f69645f726567697374657265722c200a69735f6d616e616765645f696e7465726e616c6c792c200a69735f696e7465726e616c5f6e616d65737061636529200a76616c7565730a280a6e65787476616c282752454c4154494f4e534849505f545950455f49445f53455127292c0a27504c4154455f434f4e54524f4c5f4c41594f5554272c0a27506c617465202d20436f6e74726f6c204c61796f7574272c200a27506c617465272c200a27436f6e74726f6c204c61796f7574272c200a27506c617465202d20436f6e74726f6c204c61796f75742072656c6174696f6e73686970272c200a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27292c200a2754272c200a2754270a293b0a0a696e7365727420696e746f2072656c6174696f6e736869705f74797065730a2869642c200a636f64652c200a6c6162656c2c200a706172656e745f6c6162656c2c200a6368696c645f6c6162656c2c200a6465736372697074696f6e2c200a706572735f69645f726567697374657265722c200a69735f6d616e616765645f696e7465726e616c6c792c200a69735f696e7465726e616c5f6e616d65737061636529200a76616c7565730a280a6e65787476616c282752454c4154494f4e534849505f545950455f49445f53455127292c0a27434f4e5441494e45525f434f4d504f4e454e54272c0a27436f6e7461696e6572202d20436f6d706f6e656e74272c200a27436f6e7461696e6572272c200a27436f6d706f6e656e74272c200a27436f6e7461696e6572202d20436f6d706f6e656e742072656c6174696f6e73686970272c200a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27292c200a2754272c200a275427293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c742073706163650a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f207370616365730a2869642c0a636f64652c0a706572735f69645f72656769737465726572290a76616c7565730a280a6e65787476616c282753504143455f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c742070726f6a6563740a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a73656c656374206e65787476616c282750524f4a4543545f49445f53455127293b0a696e7365727420696e746f2070726f6a656374730a2869642c0a7065726d5f69642c0a636f64652c0a73706163655f69642c0a706572735f69645f72656769737465726572290a76616c7565730a280a6375727276616c282750524f4a4543545f49445f53455127292c0a746f5f63686172286e6f7728292c2027595959594d4d4444484832344d4953534d5327297c7c272d277c7c6375727276616c282750524f4a4543545f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d2073706163657320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c74206578706572696d656e740a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a73656c656374206e65787476616c28274558504552494d454e545f49445f53455127293b0a696e7365727420696e746f206578706572696d656e74735f616c6c0a2869642c0a7065726d5f69642c0a636f64652c0a70726f6a5f69642c0a657874795f69642c0a706572735f69645f72656769737465726572290a76616c7565730a280a6375727276616c28274558504552494d454e545f49445f53455127292c0a746f5f63686172286e6f7728292c2027595959594d4d4444484832344d4953534d5327297c7c272d277c7c6375727276616c28274558504552494d454e545f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d2070726f6a6563747320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d206578706572696d656e745f747970657320776865726520636f6465203d2027554e4b4e4f574e27292c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c742073616d706c650a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a73656c656374206e65787476616c282753414d504c455f49445f53455127293b0a696e7365727420696e746f2073616d706c65735f616c6c0a2869642c0a7065726d5f69642c0a636f64652c0a657870655f69642c0a73706163655f69642c0a736174795f69642c0a706572735f69645f72656769737465726572290a76616c7565730a280a6375727276616c282753414d504c455f49445f53455127292c0a746f5f63686172286e6f7728292c2027595959594d4d4444484832344d4953534d5327297c7c272d277c7c6375727276616c282753414d504c455f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d206578706572696d656e747320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d2073706163657320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d2073616d706c655f747970657320776865726520636f6465203d2027554e4b4e4f574e27292c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a	\N
\.


--
-- Name: deletion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('deletion_id_seq', 1, false);


--
-- Data for Name: deletions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY deletions (id, pers_id_registerer, registration_timestamp, reason) FROM stdin;
\.


--
-- Name: dstpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('dstpt_id_seq', 1, false);


--
-- Data for Name: entity_operations_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY entity_operations_log (id, registration_id) FROM stdin;
\.


--
-- Name: entity_operations_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('entity_operations_log_id_seq', 1, false);


--
-- Name: etpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('etpt_id_seq', 1, false);


--
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('event_id_seq', 1, false);


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (id, event_type, description, reason, pers_id_registerer, registration_timestamp, entity_type, identifiers, content, exac_id) FROM stdin;
\.


--
-- Name: experiment_code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_code_seq', 1, false);


--
-- Name: experiment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_id_seq', 1, true);


--
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, pers_id_author, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: experiment_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Name: experiment_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_property_id_seq', 1, false);


--
-- Data for Name: experiment_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_relationships_history (id, main_expe_id, relation_type, samp_id, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp, proj_id) FROM stdin;
1	1	OWNED	\N	\N	20160420111639786-1	\N	2016-04-20 11:16:39.786005+02	\N	1
2	1	OWNER	1	\N	20160420111639786-1	\N	2016-04-20 11:16:39.786005+02	\N	\N
\.


--
-- Name: experiment_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_relationships_history_id_seq', 2, true);


--
-- Name: experiment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_type_id_seq', 1, true);


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_types (id, code, description, modification_timestamp, validation_script_id) FROM stdin;
1	UNKNOWN	Unknown	2016-04-20 11:16:39.786005+02	\N
\.


--
-- Data for Name: experiments_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiments_all (id, perm_id, code, exty_id, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, pers_id_modifier, version) FROM stdin;
1	20160420111639786-1	DEFAULT	1	1	2016-04-20 11:16:39.786005+02	2016-04-20 11:16:39.786005+02	1	\N	\N	f	\N	0
\.


--
-- Data for Name: external_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY external_data (data_id, share_id, size, location, ffty_id, loty_id, cvte_id_stor_fmt, is_complete, cvte_id_store, status, present_in_archive, speed_hint, storage_confirmation) FROM stdin;
\.


--
-- Name: external_data_management_system_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('external_data_management_system_id_seq', 1, false);


--
-- Data for Name: external_data_management_systems; Type: TABLE DATA; Schema: public; Owner: -
--

COPY external_data_management_systems (id, code, label, url_template, is_openbis) FROM stdin;
\.


--
-- Name: file_format_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('file_format_type_id_seq', 6, true);


--
-- Data for Name: file_format_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY file_format_types (id, code, description) FROM stdin;
1	HDF5	Hierarchical Data Format File, version 5
2	PROPRIETARY	Proprietary Format File
3	SRF	Sequence Read Format File
4	TIFF	TIFF File
5	TSV	Tab Separated Values File
6	XML	XML File
\.


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('filter_id_seq', 1, false);


--
-- Data for Name: filters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY filters (id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
\.


--
-- Data for Name: grid_custom_columns; Type: TABLE DATA; Schema: public; Owner: -
--

COPY grid_custom_columns (id, code, label, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
\.


--
-- Name: grid_custom_columns_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('grid_custom_columns_id_seq', 1, false);


--
-- Data for Name: link_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY link_data (data_id, edms_id, external_code) FROM stdin;
\.


--
-- Name: locator_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('locator_type_id_seq', 1, true);


--
-- Data for Name: locator_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY locator_types (id, code, description) FROM stdin;
1	RELATIVE_LOCATION	Relative Location
\.


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_id_seq', 1, false);


--
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_properties (id, mate_id, mtpt_id, value, registration_timestamp, pers_id_author, modification_timestamp, pers_id_registerer, cvte_id, mate_prop_id) FROM stdin;
\.


--
-- Data for Name: material_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Name: material_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_property_id_seq', 1, false);


--
-- Name: material_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_type_id_seq', 1, false);


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_types (id, code, description, modification_timestamp, validation_script_id) FROM stdin;
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: -
--

COPY materials (id, code, maty_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Name: metaproject_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('metaproject_assignment_id_seq', 1, false);


--
-- Data for Name: metaproject_assignments_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY metaproject_assignments_all (id, mepr_id, expe_id, samp_id, data_id, mate_id, del_id, creation_date) FROM stdin;
\.


--
-- Name: metaproject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('metaproject_id_seq', 1, false);


--
-- Data for Name: metaprojects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY metaprojects (id, name, description, owner, private, creation_date) FROM stdin;
\.


--
-- Name: mtpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('mtpt_id_seq', 1, false);


--
-- Data for Name: operation_executions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY operation_executions (id, code, state, description, error, creation_date, start_date, finish_date) FROM stdin;
\.


--
-- Name: operation_executions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('operation_executions_id_seq', 1, false);


--
-- Name: perm_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('perm_id_seq', 1, true);


--
-- Name: person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('person_id_seq', 3, true);


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY persons (id, first_name, last_name, user_id, email, space_id, registration_timestamp, pers_id_registerer, display_settings, is_active) FROM stdin;
1		System User	system		\N	2016-04-20 11:16:39.786005+02	\N	\N	t
2			admin		\N	2016-04-20 11:18:16.588543+02	1	\\xaced00057372004163682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e446973706c617953657474696e6773000000000000000102000f5a0009646562756767696e675a001669676e6f72654c617374486973746f7279546f6b656e5a00176c65676163794d656461646174615549456e61626c65644c000e636f6c756d6e53657474696e677374000f4c6a6176612f7574696c2f4d61703b4c001b637573746f6d576562417070446973706c617953657474696e677371007e00014c000e64656661756c7450726f6a6563747400124c6a6176612f6c616e672f537472696e673b4c001064726f70446f776e53657474696e677371007e00014c00166c617374486973746f7279546f6b656e4f724e756c6c71007e00024c001670616e656c436f6c6c617073656453657474696e677371007e00014c001170616e656c53697a6553657474696e677371007e00014c0015706f72746c6574436f6e66696775726174696f6e7371007e00014c001d7265616c4e756d626572466f726d6174696e67506172616d65746572737400514c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f5265616c4e756d626572466f726d6174696e67506172616d65746572733b4c000b74616253657474696e677371007e00014c001a746563686e6f6c6f6779537065636966696353657474696e677371007e00014c00067669736974737400104c6a6176612f7574696c2f4c6973743b7870000000737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000574001c747970652d62726f777365722d677269642d4558504552494d454e54737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a657870000000047704000000047372003f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e436f6c756d6e53657474696e6700000000000000010200055a000968617346696c7465725a000668696464656e49000577696474684c0008636f6c756d6e494471007e00024c0007736f72744469727400444c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f536f7274496e666f24536f72744469723b7870010000000096740004434f4445707371007e000b00000000012c74000b4445534352495054494f4e707371007e000b00010000012c7400114d4f44494649434154494f4e5f44415445707371007e000b00010000009674001156414c49444154494f4e5f534352495054707874001273706163652d62726f777365722d677269647371007e0009000000047704000000047371007e000b000000000096740004434f4445707371007e000b0000000000c874000b4445534352495054494f4e707371007e000b0000000000c874000b5245474953545241544f52707371007e000b00010000012c7400114d4f44494649434154494f4e5f444154457078740018747970652d62726f777365722d677269642d53414d504c457371007e00090000000b77040000000b7371007e000b010000000096740004434f4445707371007e000b00000000012c74000b4445534352495054494f4e707371007e000b00010000012c7400114d4f44494649434154494f4e5f44415445707371007e000b00010000009674001156414c49444154494f4e5f534352495054707371007e000b00010000009674000b49535f4c49535441424c45707371007e000b0001000000c874001149535f53484f575f434f4e5441494e4552707371007e000b0001000000c874000f49535f53484f575f504152454e5453707371007e000b000100000096740014535542434f44455f554e495155455f4c4142454c707371007e000b0001000000967400194155544f5f47454e45524154455f434f4445535f4c4142454c707371007e000b00010000009674001749535f53484f575f504152454e545f4d45544144415441707371007e000b00010000009674001547454e4552415445445f434f44455f505245464958707874001a747970652d62726f777365722d677269642d4d4154455249414c7371007e0009000000047704000000047371007e000b010000000096740004434f4445707371007e000b00000000012c74000b4445534352495054494f4e707371007e000b00010000012c7400114d4f44494649434154494f4e5f44415445707371007e000b00010000009674001156414c49444154494f4e5f534352495054707874001a747970652d62726f777365722d677269642d444154415f5345547371007e0009000000087704000000087371007e000b010000000096740004434f4445707371007e000b00000000012c74000b4445534352495054494f4e707371007e000b00010000012c7400114d4f44494649434154494f4e5f44415445707371007e000b00010000009674001156414c49444154494f4e5f534352495054707371007e000b00000000009674000d444154415f5345545f4b494e44707371007e000b00010000009674001144454c4554494f4e5f444953414c4c4f57707371007e000b0001000000967400124d41494e5f444154415f5345545f50415448707371007e000b0001000000967400154d41494e5f444154415f5345545f5041545445524e70787870707371007e00063f400000000000007708000000100000000078707371007e00063f4000000000000077080000001000000000787371007e00063f400000000000007708000000100000000078707372004f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e5265616c4e756d626572466f726d6174696e67506172616d657465727300000000000000010200035a0010666f726d6174696e67456e61626c6564490009707265636973696f6e5a000a736369656e746966696378700100000004007371007e00063f4000000000000077080000001000000000787371007e00063f4000000000000077080000001000000000787371007e00090000000077040000000078	t
3			pontia		\N	2016-04-20 11:16:39.786005+02	\N	\N	t
\.


--
-- Data for Name: post_registration_dataset_queue; Type: TABLE DATA; Schema: public; Owner: -
--

COPY post_registration_dataset_queue (id, ds_id) FROM stdin;
\.


--
-- Name: post_registration_dataset_queue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('post_registration_dataset_queue_id_seq', 1, false);


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('project_id_seq', 2, true);


--
-- Data for Name: project_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY project_relationships_history (id, main_proj_id, relation_type, expe_id, space_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
1	1	OWNED	\N	1	DEFAULT	\N	2016-04-20 11:16:39.786005+02	\N
2	1	OWNER	1	\N	20160420111639786-1	\N	2016-04-20 11:16:39.786005+02	\N
3	2	OWNED	\N	2	SCU	2	2016-04-20 11:19:13.082405+02	\N
\.


--
-- Name: project_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('project_relationships_history_id_seq', 3, true);


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projects (id, perm_id, code, space_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp, pers_id_modifier, version) FROM stdin;
1	20160420111639786-1	DEFAULT	1	\N	\N	1	2016-04-20 11:16:39.786005+02	2016-04-20 11:16:39.786005+02	\N	0
2	20160420111913082-1	AARON	2	\N	Aaron Ponti	2	2016-04-20 11:19:13.082405+02	2016-04-20 11:19:13.082405+02	2	0
\.


--
-- Name: property_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('property_type_id_seq', 1, true);


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, maty_prop_id, schema, transformation) FROM stdin;
1	DESCRIPTION	A Description	Description	1	2016-04-20 11:16:39.786005+02	1	\N	f	f	\N	\N	\N
\.


--
-- Data for Name: queries; Type: TABLE DATA; Schema: public; Owner: -
--

COPY queries (id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, query_type, entity_type_code, db_key) FROM stdin;
\.


--
-- Name: query_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('query_id_seq', 1, false);


--
-- Name: relationship_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('relationship_type_id_seq', 3, true);


--
-- Data for Name: relationship_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relationship_types (id, code, label, parent_label, child_label, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace) FROM stdin;
1	PARENT_CHILD	Parent - Child	Parent	Child	Parent - Child relationship	2016-04-20 11:16:39.786005+02	1	t	t
2	PLATE_CONTROL_LAYOUT	Plate - Control Layout	Plate	Control Layout	Plate - Control Layout relationship	2016-04-20 11:16:39.786005+02	1	t	t
3	CONTAINER_COMPONENT	Container - Component	Container	Component	Container - Component relationship	2016-04-20 11:16:39.786005+02	1	t	t
\.


--
-- Name: role_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('role_assignment_id_seq', 1, true);


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY role_assignments (id, role_code, space_id, pers_id_grantee, ag_id_grantee, pers_id_registerer, registration_timestamp) FROM stdin;
1	ADMIN	\N	2	\N	1	2016-04-20 11:18:16.588543+02
\.


--
-- Name: sample_code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_code_seq', 1, false);


--
-- Name: sample_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_id_seq', 1, true);


--
-- Data for Name: sample_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_properties (id, samp_id, stpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, pers_id_author, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: sample_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Name: sample_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_property_id_seq', 1, false);


--
-- Name: sample_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_relationship_id_seq', 1, false);


--
-- Data for Name: sample_relationships_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_relationships_all (id, sample_id_parent, relationship_id, sample_id_child, del_id, pers_id_author, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: sample_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_relationships_history (id, main_samp_id, relation_type, expe_id, samp_id, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp, space_id) FROM stdin;
1	1	OWNED	1	\N	\N	20160420111639786-1	\N	2016-04-20 11:16:39.786005+02	\N	\N
\.


--
-- Name: sample_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_relationships_history_id_seq', 1, true);


--
-- Name: sample_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_type_id_seq', 1, true);


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_types (id, code, description, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique, inherit_properties, validation_script_id, show_parent_metadata) FROM stdin;
1	UNKNOWN	Unknown	t	0	0	2016-04-20 11:16:39.786005+02	f	S	f	f	\N	f
\.


--
-- Data for Name: samples_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY samples_all (id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, del_id, orig_del, space_id, samp_id_part_of, pers_id_modifier, code_unique_check, subcode_unique_check, version, proj_id) FROM stdin;
1	20160420111639786-1	DEFAULT	1	1	2016-04-20 11:16:39.786005+02	2016-04-20 11:16:39.786005+02	1	\N	\N	1	\N	\N	DEFAULT,-1,-1,1	\N	0	\N
\.


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('script_id_seq', 1, false);


--
-- Data for Name: scripts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY scripts (id, name, script_type, description, script, registration_timestamp, pers_id_registerer, entity_kind, plugin_type, is_available) FROM stdin;
\.


--
-- Name: space_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('space_id_seq', 2, true);


--
-- Data for Name: spaces; Type: TABLE DATA; Schema: public; Owner: -
--

COPY spaces (id, code, description, registration_timestamp, pers_id_registerer) FROM stdin;
1	DEFAULT	\N	2016-04-20 11:16:39.786005+02	1
2	SCU	Single Cell Unit	2016-04-20 11:18:39.328+02	2
\.


--
-- Name: stpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('stpt_id_seq', 1, false);


--
-- Name: ag_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_bk_uk UNIQUE (code);


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
-- Name: copl_name_ver_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY core_plugins
    ADD CONSTRAINT copl_name_ver_uk UNIQUE (name, version);


--
-- Name: covo_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace);


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
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, uuid);


--
-- Name: dast_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);


--
-- Name: data_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_bk_uk UNIQUE (code);


--
-- Name: data_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_pk PRIMARY KEY (id);


--
-- Name: datarelh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_pk PRIMARY KEY (id);


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
-- Name: del_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY deletions
    ADD CONSTRAINT del_pk PRIMARY KEY (id);


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
-- Name: dsprh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties_history
    ADD CONSTRAINT dsprh_pk PRIMARY KEY (id);


--
-- Name: dsre_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent, relationship_id);


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
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code);


--
-- Name: dsty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);


--
-- Name: edms_code_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data_management_systems
    ADD CONSTRAINT edms_code_uk UNIQUE (code);


--
-- Name: edms_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data_management_systems
    ADD CONSTRAINT edms_pk PRIMARY KEY (id);


--
-- Name: eol_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entity_operations_log
    ADD CONSTRAINT eol_pk PRIMARY KEY (id);


--
-- Name: eol_reg_id_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entity_operations_log
    ADD CONSTRAINT eol_reg_id_uk UNIQUE (registration_id);


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

ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);


--
-- Name: expe_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);


--
-- Name: expe_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments_all
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
-- Name: exprh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties_history
    ADD CONSTRAINT exprh_pk PRIMARY KEY (id);


--
-- Name: exrelh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_pk PRIMARY KEY (id);


--
-- Name: exty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code);


--
-- Name: exty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);


--
-- Name: ffty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code);


--
-- Name: ffty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);


--
-- Name: filt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_bk_uk UNIQUE (name, grid_id);


--
-- Name: filt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pk PRIMARY KEY (id);


--
-- Name: grid_custom_columns_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_bk_uk UNIQUE (code, grid_id);


--
-- Name: grid_custom_columns_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pk PRIMARY KEY (id);


--
-- Name: lnda_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY link_data
    ADD CONSTRAINT lnda_pk PRIMARY KEY (data_id);


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
-- Name: maprh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties_history
    ADD CONSTRAINT maprh_pk PRIMARY KEY (id);


--
-- Name: mate_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id);


--
-- Name: mate_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);


--
-- Name: maty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code);


--
-- Name: maty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);


--
-- Name: metaproject_assignments_all_mepr_id_data_id_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_data_id_uk UNIQUE (mepr_id, data_id);


--
-- Name: metaproject_assignments_all_mepr_id_expe_id_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_expe_id_uk UNIQUE (mepr_id, expe_id);


--
-- Name: metaproject_assignments_all_mepr_id_mate_id_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_mate_id_uk UNIQUE (mepr_id, mate_id);


--
-- Name: metaproject_assignments_all_mepr_id_samp_id_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_samp_id_uk UNIQUE (mepr_id, samp_id);


--
-- Name: metaproject_assignments_all_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_pk PRIMARY KEY (id);


--
-- Name: metaprojects_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY metaprojects
    ADD CONSTRAINT metaprojects_pk PRIMARY KEY (id);


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
-- Name: operation_executions_code_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY operation_executions
    ADD CONSTRAINT operation_executions_code_uk UNIQUE (code);


--
-- Name: operation_executions_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY operation_executions
    ADD CONSTRAINT operation_executions_pk PRIMARY KEY (id);


--
-- Name: pers_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (user_id);


--
-- Name: pers_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);


--
-- Name: prdq_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY post_registration_dataset_queue
    ADD CONSTRAINT prdq_pk PRIMARY KEY (id);


--
-- Name: proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, space_id);


--
-- Name: proj_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pi_uk UNIQUE (perm_id);


--
-- Name: proj_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);


--
-- Name: prrelh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_pk PRIMARY KEY (id);


--
-- Name: prty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace);


--
-- Name: prty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);


--
-- Name: quer_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_bk_uk UNIQUE (name);


--
-- Name: quer_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_pk PRIMARY KEY (id);


--
-- Name: rety_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relationship_types
    ADD CONSTRAINT rety_pk PRIMARY KEY (id);


--
-- Name: rety_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relationship_types
    ADD CONSTRAINT rety_uk UNIQUE (code);


--
-- Name: roas_ag_space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_space_bk_uk UNIQUE (ag_id_grantee, role_code, space_id);


--
-- Name: roas_pe_space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pe_space_bk_uk UNIQUE (pers_id_grantee, role_code, space_id);


--
-- Name: roas_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);


--
-- Name: samp_code_unique_check_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_code_unique_check_uk UNIQUE (code_unique_check);


--
-- Name: samp_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);


--
-- Name: samp_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);


--
-- Name: samp_subcode_unique_check_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_subcode_unique_check_uk UNIQUE (subcode_unique_check);


--
-- Name: samprelh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_pk PRIMARY KEY (id);


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
-- Name: saprh_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties_history
    ADD CONSTRAINT saprh_pk PRIMARY KEY (id);


--
-- Name: sare_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_bk_uk UNIQUE (sample_id_child, sample_id_parent, relationship_id);


--
-- Name: sare_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_pk PRIMARY KEY (id);


--
-- Name: saty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code);


--
-- Name: saty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);


--
-- Name: scri_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_pk PRIMARY KEY (id);


--
-- Name: scri_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_uk UNIQUE (name);


--
-- Name: space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_bk_uk UNIQUE (code);


--
-- Name: space_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_pk PRIMARY KEY (id);


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
-- Name: data_acct_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_acct_i ON data_all USING btree (access_timestamp);


--
-- Name: data_del_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_del_fk_i ON data_all USING btree (del_id);


--
-- Name: data_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_dsty_fk_i ON data_all USING btree (dsty_id);


--
-- Name: data_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_expe_fk_i ON data_all USING btree (expe_id);


--
-- Name: data_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_samp_fk_i ON data_all USING btree (samp_id);


--
-- Name: datarelh_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX datarelh_data_fk_i ON data_set_relationships_history USING btree (data_id);


--
-- Name: datarelh_main_data_fk_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX datarelh_main_data_fk_data_fk_i ON data_set_relationships_history USING btree (main_data_id, data_id);


--
-- Name: datarelh_main_data_fk_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX datarelh_main_data_fk_expe_fk_i ON data_set_relationships_history USING btree (main_data_id, expe_id);


--
-- Name: datarelh_main_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX datarelh_main_data_fk_i ON data_set_relationships_history USING btree (main_data_id);


--
-- Name: datarelh_main_data_fk_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX datarelh_main_data_fk_samp_fk_i ON data_set_relationships_history USING btree (main_data_id, samp_id);


--
-- Name: del_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX del_pers_fk_i ON deletions USING btree (pers_id_registerer);


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
-- Name: dspr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_mapr_fk_i ON data_set_properties USING btree (mate_prop_id);


--
-- Name: dspr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_pers_fk_i ON data_set_properties USING btree (pers_id_registerer);


--
-- Name: dsprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsprh_etpt_fk_i ON data_set_properties_history USING btree (dstpt_id);


--
-- Name: dsprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsprh_expe_fk_i ON data_set_properties_history USING btree (ds_id);


--
-- Name: dsprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsprh_vuts_fk_i ON data_set_properties_history USING btree (valid_until_timestamp);


--
-- Name: dsre_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_child ON data_set_relationships_all USING btree (data_id_child);


--
-- Name: dsre_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships_all USING btree (data_id_parent);


--
-- Name: dsre_del_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_del_fk_i ON data_set_relationships_all USING btree (del_id);


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
-- Name: entity_operations_log_rid_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX entity_operations_log_rid_i ON entity_operations_log USING btree (registration_id);


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
-- Name: evnt_exac_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX evnt_exac_fk_i ON events USING btree (exac_id);


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
-- Name: expe_del_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_del_fk_i ON experiments_all USING btree (del_id);


--
-- Name: expe_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_exty_fk_i ON experiments_all USING btree (exty_id);


--
-- Name: expe_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_pers_fk_i ON experiments_all USING btree (pers_id_registerer);


--
-- Name: expe_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_proj_fk_i ON experiments_all USING btree (proj_id);


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
-- Name: expr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_mapr_fk_i ON experiment_properties USING btree (mate_prop_id);


--
-- Name: expr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_pers_fk_i ON experiment_properties USING btree (pers_id_registerer);


--
-- Name: exprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exprh_etpt_fk_i ON experiment_properties_history USING btree (etpt_id);


--
-- Name: exprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exprh_expe_fk_i ON experiment_properties_history USING btree (expe_id);


--
-- Name: exprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exprh_vuts_fk_i ON experiment_properties_history USING btree (valid_until_timestamp);


--
-- Name: exrelh_data_id_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exrelh_data_id_fk_i ON experiment_relationships_history USING btree (data_id);


--
-- Name: exrelh_main_expe_fk_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exrelh_main_expe_fk_data_fk_i ON experiment_relationships_history USING btree (main_expe_id, data_id);


--
-- Name: exrelh_main_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exrelh_main_expe_fk_i ON experiment_relationships_history USING btree (main_expe_id);


--
-- Name: exrelh_main_expe_fk_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exrelh_main_expe_fk_proj_fk_i ON experiment_relationships_history USING btree (main_expe_id, proj_id);


--
-- Name: exrelh_main_expe_fk_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exrelh_main_expe_fk_samp_fk_i ON experiment_relationships_history USING btree (main_expe_id, samp_id);


--
-- Name: exrelh_samp_id_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exrelh_samp_id_fk_i ON experiment_relationships_history USING btree (samp_id);


--
-- Name: filt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX filt_pers_fk_i ON filters USING btree (pers_id_registerer);


--
-- Name: grid_custom_columns_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grid_custom_columns_pers_fk_i ON grid_custom_columns USING btree (pers_id_registerer);


--
-- Name: mapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);


--
-- Name: mapr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mapr_fk_i ON material_properties USING btree (mate_prop_id);


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
-- Name: maprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX maprh_etpt_fk_i ON material_properties_history USING btree (mtpt_id);


--
-- Name: maprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX maprh_expe_fk_i ON material_properties_history USING btree (mate_id);


--
-- Name: maprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX maprh_vuts_fk_i ON material_properties_history USING btree (valid_until_timestamp);


--
-- Name: mate_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);


--
-- Name: mate_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);


--
-- Name: metaproject_assignments_all_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaproject_assignments_all_data_fk_i ON metaproject_assignments_all USING btree (data_id);


--
-- Name: metaproject_assignments_all_del_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaproject_assignments_all_del_fk_i ON metaproject_assignments_all USING btree (del_id);


--
-- Name: metaproject_assignments_all_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaproject_assignments_all_expe_fk_i ON metaproject_assignments_all USING btree (expe_id);


--
-- Name: metaproject_assignments_all_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaproject_assignments_all_mate_fk_i ON metaproject_assignments_all USING btree (mate_id);


--
-- Name: metaproject_assignments_all_mepr_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaproject_assignments_all_mepr_fk_i ON metaproject_assignments_all USING btree (mepr_id);


--
-- Name: metaproject_assignments_all_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaproject_assignments_all_samp_fk_i ON metaproject_assignments_all USING btree (samp_id);


--
-- Name: metaprojects_name_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaprojects_name_i ON metaprojects USING btree (name);


--
-- Name: metaprojects_name_owner_uk; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX metaprojects_name_owner_uk ON metaprojects USING btree (lower((name)::text), owner);


--
-- Name: metaprojects_owner_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX metaprojects_owner_fk_i ON metaprojects USING btree (owner);


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
-- Name: operation_executions_code_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX operation_executions_code_i ON operation_executions USING btree (code);


--
-- Name: pers_is_active_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pers_is_active_i ON persons USING btree (is_active);


--
-- Name: pers_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pers_space_fk_i ON persons USING btree (space_id);


--
-- Name: proj_pers_fk_i_leader; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_leader ON projects USING btree (pers_id_leader);


--
-- Name: proj_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_registerer ON projects USING btree (pers_id_registerer);


--
-- Name: proj_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_space_fk_i ON projects USING btree (space_id);


--
-- Name: prrelh_main_proj_fk_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prrelh_main_proj_fk_expe_fk_i ON project_relationships_history USING btree (main_proj_id, expe_id);


--
-- Name: prrelh_main_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prrelh_main_proj_fk_i ON project_relationships_history USING btree (main_proj_id);


--
-- Name: prrelh_main_proj_fk_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prrelh_main_proj_fk_space_fk_i ON project_relationships_history USING btree (main_proj_id, space_id);


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
-- Name: roas_pers_fk_i_grantee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);


--
-- Name: roas_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);


--
-- Name: roas_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_space_fk_i ON role_assignments USING btree (space_id);


--
-- Name: samp_code_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_code_i ON samples_all USING btree (code);


--
-- Name: samp_del_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_del_fk_i ON samples_all USING btree (del_id);


--
-- Name: samp_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_expe_fk_i ON samples_all USING btree (expe_id);


--
-- Name: samp_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_pers_fk_i ON samples_all USING btree (pers_id_registerer);


--
-- Name: samp_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_proj_fk_i ON samples_all USING btree (proj_id);


--
-- Name: samp_samp_fk_i_part_of; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_part_of ON samples_all USING btree (samp_id_part_of);


--
-- Name: samp_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_saty_fk_i ON samples_all USING btree (saty_id);


--
-- Name: samprelh_data_id_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_data_id_fk_i ON sample_relationships_history USING btree (data_id);


--
-- Name: samprelh_main_samp_fk_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_main_samp_fk_data_fk_i ON sample_relationships_history USING btree (main_samp_id, data_id);


--
-- Name: samprelh_main_samp_fk_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_main_samp_fk_expe_fk_i ON sample_relationships_history USING btree (main_samp_id, expe_id);


--
-- Name: samprelh_main_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_main_samp_fk_i ON sample_relationships_history USING btree (main_samp_id);


--
-- Name: samprelh_main_samp_fk_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_main_samp_fk_samp_fk_i ON sample_relationships_history USING btree (main_samp_id, samp_id);


--
-- Name: samprelh_main_samp_fk_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_main_samp_fk_space_fk_i ON sample_relationships_history USING btree (main_samp_id, space_id);


--
-- Name: samprelh_samp_id_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samprelh_samp_id_fk_i ON sample_relationships_history USING btree (samp_id);


--
-- Name: sapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);


--
-- Name: sapr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_mapr_fk_i ON sample_properties USING btree (mate_prop_id);


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
-- Name: saprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX saprh_etpt_fk_i ON sample_properties_history USING btree (stpt_id);


--
-- Name: saprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX saprh_expe_fk_i ON sample_properties_history USING btree (samp_id);


--
-- Name: saprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX saprh_vuts_fk_i ON sample_properties_history USING btree (valid_until_timestamp);


--
-- Name: sare_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_data_fk_i_child ON sample_relationships_all USING btree (sample_id_child);


--
-- Name: sare_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_data_fk_i_parent ON sample_relationships_all USING btree (sample_id_parent);


--
-- Name: sare_data_fk_i_relationship; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_data_fk_i_relationship ON sample_relationships_all USING btree (relationship_id);


--
-- Name: sare_del_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_del_fk_i ON sample_relationships_all USING btree (del_id);


--
-- Name: script_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX script_pers_fk_i ON scripts USING btree (pers_id_registerer);


--
-- Name: space_pers_registered_by_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX space_pers_registered_by_fk_i ON spaces USING btree (pers_id_registerer);


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
-- Name: data_all; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_all AS
    ON DELETE TO data DO INSTEAD  DELETE FROM data_all
  WHERE ((data_all.id)::bigint = (old.id)::bigint);


--
-- Name: data_deleted_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD  DELETE FROM data_all
  WHERE ((data_all.id)::bigint = (old.id)::bigint);


--
-- Name: data_deleted_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD  UPDATE data_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((data_all.id)::bigint = (new.id)::bigint);


--
-- Name: data_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_insert AS
    ON INSERT TO data DO INSTEAD  INSERT INTO data_all (id, code, del_id, orig_del, expe_id, dast_id, data_producer_code, dsty_id, is_derived, is_valid, modification_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, production_timestamp, registration_timestamp, samp_id, version)
  VALUES (new.id, new.code, new.del_id, new.orig_del, new.expe_id, new.dast_id, new.data_producer_code, new.dsty_id, new.is_derived, new.is_valid, new.modification_timestamp, new.access_timestamp, new.pers_id_registerer, new.pers_id_modifier, new.production_timestamp, new.registration_timestamp, new.samp_id, new.version);


--
-- Name: data_relationship_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_delete AS
    ON DELETE TO data_set_relationships_all
   WHERE (old.del_id IS NULL) DO  UPDATE data_set_relationships_history SET valid_until_timestamp = now()
  WHERE ((((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: data_relationship_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_insert AS
    ON INSERT TO data_set_relationships_all
   WHERE (new.del_id IS NULL) DO ( INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);


--
-- Name: data_relationship_trash_revert_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_trash_revert_update AS
    ON UPDATE TO data_set_relationships_all
   WHERE ((old.del_id IS NOT NULL) AND (new.del_id IS NULL)) DO ( INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);


--
-- Name: data_relationship_trash_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_trash_update AS
    ON UPDATE TO data_set_relationships_all
   WHERE ((new.del_id IS NOT NULL) AND (old.del_id IS NULL)) DO  UPDATE data_set_relationships_history SET valid_until_timestamp = now()
  WHERE ((((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: data_relationship_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_update AS
    ON UPDATE TO data_set_relationships_all
   WHERE ((new.del_id IS NULL) AND (old.del_id IS NULL)) DO ( UPDATE data_set_relationships_history SET valid_until_timestamp = now()
  WHERE ((((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);


--
-- Name: data_set_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) AND (( SELECT data_all.del_id
           FROM data_all
          WHERE ((data_all.id)::bigint = (old.ds_id)::bigint)) IS NULL)) DO  INSERT INTO data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('data_set_property_id_seq'::regclass), old.ds_id, old.dstpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());


--
-- Name: data_set_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('data_set_property_id_seq'::regclass), old.ds_id, old.dstpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: data_set_relationships_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD  DELETE FROM data_set_relationships_all
  WHERE ((((data_set_relationships_all.data_id_parent)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_all.data_id_child)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_all.relationship_id)::bigint = (old.relationship_id)::bigint));


--
-- Name: data_set_relationships_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD  INSERT INTO data_set_relationships_all (data_id_parent, data_id_child, pers_id_author, relationship_id, ordinal, registration_timestamp, modification_timestamp)
  VALUES (new.data_id_parent, new.data_id_child, new.pers_id_author, new.relationship_id, new.ordinal, new.registration_timestamp, new.modification_timestamp);


--
-- Name: data_set_relationships_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_relationships_update AS
    ON UPDATE TO data_set_relationships DO INSTEAD  UPDATE data_set_relationships_all SET data_id_parent = new.data_id_parent, data_id_child = new.data_id_child, del_id = new.del_id, relationship_id = new.relationship_id, ordinal = new.ordinal, pers_id_author = new.pers_id_author, registration_timestamp = new.registration_timestamp, modification_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_all.data_id_parent)::bigint = (new.data_id_parent)::bigint) AND ((data_set_relationships_all.data_id_child)::bigint = (new.data_id_child)::bigint)) AND ((data_set_relationships_all.relationship_id)::bigint = (new.relationship_id)::bigint));


--
-- Name: data_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_update AS
    ON UPDATE TO data DO INSTEAD  UPDATE data_all SET code = new.code, del_id = new.del_id, orig_del = new.orig_del, expe_id = new.expe_id, dast_id = new.dast_id, data_producer_code = new.data_producer_code, dsty_id = new.dsty_id, is_derived = new.is_derived, is_valid = new.is_valid, modification_timestamp = new.modification_timestamp, access_timestamp = new.access_timestamp, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, production_timestamp = new.production_timestamp, registration_timestamp = new.registration_timestamp, samp_id = new.samp_id, version = new.version
  WHERE ((data_all.id)::bigint = (new.id)::bigint);


--
-- Name: dataset_experiment_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_delete AS
    ON DELETE TO data_all
   WHERE ((old.expe_id IS NOT NULL) AND (old.samp_id IS NULL)) DO  UPDATE experiment_relationships_history SET valid_until_timestamp = now()
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: dataset_experiment_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_insert AS
    ON INSERT TO data_all
   WHERE ((new.expe_id IS NOT NULL) AND (new.samp_id IS NULL)) DO ( INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: dataset_experiment_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_remove_update AS
    ON UPDATE TO data_all
   WHERE ((old.samp_id IS NULL) AND (new.samp_id IS NOT NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: dataset_experiment_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_update AS
    ON UPDATE TO data_all
   WHERE ((((old.expe_id)::bigint <> (new.expe_id)::bigint) OR (old.samp_id IS NOT NULL)) AND (new.samp_id IS NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: dataset_sample_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_delete AS
    ON DELETE TO data_all
   WHERE (old.samp_id IS NOT NULL) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: dataset_sample_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_insert AS
    ON INSERT TO data_all
   WHERE (new.samp_id IS NOT NULL) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.samp_id, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: dataset_sample_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_remove_update AS
    ON UPDATE TO data_all
   WHERE ((old.samp_id IS NOT NULL) AND (new.samp_id IS NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.samp_id)::bigint = (old.samp_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: dataset_sample_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_update AS
    ON UPDATE TO data_all
   WHERE ((((old.samp_id)::bigint <> (new.samp_id)::bigint) OR (old.samp_id IS NULL)) AND (new.samp_id IS NOT NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.samp_id)::bigint = (old.samp_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.samp_id, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: experiment_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD  DELETE FROM experiments_all
  WHERE ((experiments_all.id)::bigint = (old.id)::bigint);


--
-- Name: experiment_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_insert AS
    ON INSERT TO experiments DO INSTEAD  INSERT INTO experiments_all (id, code, del_id, orig_del, exty_id, is_public, modification_timestamp, perm_id, pers_id_registerer, pers_id_modifier, proj_id, registration_timestamp, version)
  VALUES (new.id, new.code, new.del_id, new.orig_del, new.exty_id, new.is_public, new.modification_timestamp, new.perm_id, new.pers_id_registerer, new.pers_id_modifier, new.proj_id, new.registration_timestamp, new.version);


--
-- Name: experiment_project_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_delete AS
    ON DELETE TO experiments_all
   WHERE (old.proj_id IS NOT NULL) DO  UPDATE project_relationships_history SET valid_until_timestamp = now()
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: experiment_project_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_insert AS
    ON INSERT TO experiments_all
   WHERE (new.proj_id IS NOT NULL) DO ( INSERT INTO project_relationships_history (id, main_proj_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.proj_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.perm_id
           FROM projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: experiment_project_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_remove_update AS
    ON UPDATE TO experiments_all
   WHERE ((old.proj_id IS NOT NULL) AND (new.proj_id IS NULL)) DO ( UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
 UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.id)::bigint) AND ((experiment_relationships_history.proj_id)::bigint = (old.proj_id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: experiment_project_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_update AS
    ON UPDATE TO experiments_all
   WHERE ((((old.proj_id)::bigint <> (new.proj_id)::bigint) OR (old.proj_id IS NULL)) AND (new.proj_id IS NOT NULL)) DO ( UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO project_relationships_history (id, main_proj_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.proj_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.id)::bigint) AND ((experiment_relationships_history.proj_id)::bigint = (old.proj_id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.perm_id
           FROM projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: experiment_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties
   WHERE ((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) DO  INSERT INTO experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('experiment_property_id_seq'::regclass), old.expe_id, old.etpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());


--
-- Name: experiment_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('experiment_property_id_seq'::regclass), old.expe_id, old.etpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: experiment_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD  UPDATE experiments_all SET code = new.code, del_id = new.del_id, orig_del = new.orig_del, exty_id = new.exty_id, is_public = new.is_public, modification_timestamp = new.modification_timestamp, perm_id = new.perm_id, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, proj_id = new.proj_id, registration_timestamp = new.registration_timestamp, version = new.version
  WHERE ((experiments_all.id)::bigint = (new.id)::bigint);


--
-- Name: experiments_deleted_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD  DELETE FROM experiments_all
  WHERE ((experiments_all.id)::bigint = (old.id)::bigint);


--
-- Name: experiments_deleted_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD  UPDATE experiments_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((experiments_all.id)::bigint = (new.id)::bigint);


--
-- Name: material_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE material_properties_delete AS
    ON DELETE TO material_properties
   WHERE ((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) DO  INSERT INTO material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('material_property_id_seq'::regclass), old.mate_id, old.mtpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());


--
-- Name: material_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE material_properties_update AS
    ON UPDATE TO material_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('material_property_id_seq'::regclass), old.mate_id, old.mtpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: metaproject_assignments_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE metaproject_assignments_delete AS
    ON DELETE TO metaproject_assignments DO INSTEAD  DELETE FROM metaproject_assignments_all
  WHERE ((metaproject_assignments_all.id)::bigint = (old.id)::bigint);


--
-- Name: metaproject_assignments_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE metaproject_assignments_insert AS
    ON INSERT TO metaproject_assignments DO INSTEAD  INSERT INTO metaproject_assignments_all (id, mepr_id, expe_id, samp_id, data_id, mate_id, del_id, creation_date)
  VALUES (new.id, new.mepr_id, new.expe_id, new.samp_id, new.data_id, new.mate_id, new.del_id, new.creation_date);


--
-- Name: metaproject_assignments_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE metaproject_assignments_update AS
    ON UPDATE TO metaproject_assignments DO INSTEAD  UPDATE metaproject_assignments_all SET id = new.id, mepr_id = new.mepr_id, expe_id = new.expe_id, samp_id = new.samp_id, data_id = new.data_id, mate_id = new.mate_id, del_id = new.del_id, creation_date = new.creation_date
  WHERE ((metaproject_assignments_all.id)::bigint = (new.id)::bigint);


--
-- Name: project_space_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE project_space_insert AS
    ON INSERT TO projects
   WHERE (new.space_id IS NOT NULL) DO  INSERT INTO project_relationships_history (id, main_proj_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);


--
-- Name: project_space_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE project_space_remove_update AS
    ON UPDATE TO projects
   WHERE ((old.space_id IS NOT NULL) AND (new.space_id IS NULL)) DO  UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.id)::bigint) AND ((project_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: project_space_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE project_space_update AS
    ON UPDATE TO projects
   WHERE ((((old.space_id)::bigint <> (new.space_id)::bigint) OR (old.space_id IS NULL)) AND (new.space_id IS NOT NULL)) DO ( UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.id)::bigint) AND ((project_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO project_relationships_history (id, main_proj_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_container_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_delete AS
    ON DELETE TO samples_all
   WHERE (old.samp_id_part_of IS NOT NULL) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE (((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text));


--
-- Name: sample_container_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_insert AS
    ON INSERT TO samples_all
   WHERE (new.samp_id_part_of IS NOT NULL) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id_part_of, 'CONTAINER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'CONTAINED'::text, new.samp_id_part_of, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id_part_of)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_container_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_remove_update AS
    ON UPDATE TO samples_all
   WHERE ((old.samp_id_part_of IS NOT NULL) AND (new.samp_id_part_of IS NULL)) DO  UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text)) OR (((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.samp_id_part_of)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINED'::text)));


--
-- Name: sample_container_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_update AS
    ON UPDATE TO samples_all
   WHERE ((((old.samp_id_part_of)::bigint <> (new.samp_id_part_of)::bigint) OR (old.samp_id_part_of IS NULL)) AND (new.samp_id_part_of IS NOT NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text)) OR (((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.samp_id_part_of)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINED'::text)));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id_part_of, 'CONTAINER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'CONTAINED'::text, new.samp_id_part_of, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id_part_of)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD  DELETE FROM samples_all
  WHERE ((samples_all.id)::bigint = (old.id)::bigint);


--
-- Name: sample_deleted_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_deleted_delete AS
    ON DELETE TO samples_deleted DO INSTEAD  DELETE FROM samples_all
  WHERE ((samples_all.id)::bigint = (old.id)::bigint);


--
-- Name: sample_deleted_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_deleted_update AS
    ON UPDATE TO samples_deleted DO INSTEAD  UPDATE samples_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((samples_all.id)::bigint = (new.id)::bigint);


--
-- Name: sample_experiment_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_delete AS
    ON DELETE TO samples_all
   WHERE (old.expe_id IS NOT NULL) DO  UPDATE experiment_relationships_history SET valid_until_timestamp = now()
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: sample_experiment_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_insert AS
    ON INSERT TO samples_all
   WHERE (new.expe_id IS NOT NULL) DO ( INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_experiment_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_remove_update AS
    ON UPDATE TO samples_all
   WHERE ((old.expe_id IS NOT NULL) AND (new.expe_id IS NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: sample_experiment_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_update AS
    ON UPDATE TO samples_all
   WHERE ((((old.expe_id)::bigint <> (new.expe_id)::bigint) OR (old.expe_id IS NULL)) AND (new.expe_id IS NOT NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD  INSERT INTO samples_all (id, code, del_id, orig_del, expe_id, proj_id, modification_timestamp, perm_id, pers_id_registerer, pers_id_modifier, registration_timestamp, samp_id_part_of, saty_id, space_id, version)
  VALUES (new.id, new.code, new.del_id, new.orig_del, new.expe_id, new.proj_id, new.modification_timestamp, new.perm_id, new.pers_id_registerer, new.pers_id_modifier, new.registration_timestamp, new.samp_id_part_of, new.saty_id, new.space_id, new.version);


--
-- Name: sample_parent_child_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_delete AS
    ON DELETE TO sample_relationships_all
   WHERE (old.del_id IS NULL) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE (((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_parent)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_child)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) OR ((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_child)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_parent)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: sample_parent_child_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_insert AS
    ON INSERT TO sample_relationships_all
   WHERE (new.del_id IS NULL) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_parent, 'PARENT'::text, new.sample_id_child, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_child)::bigint)), new.pers_id_author, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_child, 'CHILD'::text, new.sample_id_parent, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp);
);


--
-- Name: sample_parent_child_revert_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_revert_update AS
    ON UPDATE TO sample_relationships_all
   WHERE ((new.del_id IS NULL) AND (old.del_id IS NOT NULL)) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_parent, 'PARENT'::text, new.sample_id_child, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_child)::bigint)), new.pers_id_author, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_child, 'CHILD'::text, new.sample_id_parent, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp);
);


--
-- Name: sample_parent_child_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_update AS
    ON UPDATE TO sample_relationships_all
   WHERE ((new.del_id IS NOT NULL) AND (old.del_id IS NULL)) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE (((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_parent)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_child)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) OR ((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_child)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_parent)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: sample_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_properties_delete AS
    ON DELETE TO sample_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) AND (( SELECT samples_all.del_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (old.samp_id)::bigint)) IS NULL)) DO  INSERT INTO sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('sample_property_id_seq'::regclass), old.samp_id, old.stpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());


--
-- Name: sample_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('sample_property_id_seq'::regclass), old.samp_id, old.stpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: sample_relationships_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD  DELETE FROM sample_relationships_all
  WHERE ((sample_relationships_all.id)::bigint = (old.id)::bigint);


--
-- Name: sample_relationships_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_relationships_insert AS
    ON INSERT TO sample_relationships DO INSTEAD  INSERT INTO sample_relationships_all (id, sample_id_parent, relationship_id, sample_id_child, pers_id_author, registration_timestamp, modification_timestamp)
  VALUES (new.id, new.sample_id_parent, new.relationship_id, new.sample_id_child, new.pers_id_author, new.registration_timestamp, new.modification_timestamp);


--
-- Name: sample_relationships_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_relationships_update AS
    ON UPDATE TO sample_relationships DO INSTEAD  UPDATE sample_relationships_all SET sample_id_parent = new.sample_id_parent, relationship_id = new.relationship_id, sample_id_child = new.sample_id_child, del_id = new.del_id, pers_id_author = new.pers_id_author, registration_timestamp = new.registration_timestamp, modification_timestamp = new.modification_timestamp
  WHERE ((sample_relationships_all.id)::bigint = (new.id)::bigint);


--
-- Name: sample_space_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_space_insert AS
    ON INSERT TO samples_all
   WHERE ((new.expe_id IS NULL) AND (new.space_id IS NOT NULL)) DO  INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);


--
-- Name: sample_space_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_space_remove_update AS
    ON UPDATE TO samples_all
   WHERE ((old.space_id IS NOT NULL) AND ((new.space_id IS NULL) OR ((old.expe_id IS NULL) AND (new.expe_id IS NOT NULL)))) DO  UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: sample_space_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_space_update AS
    ON UPDATE TO samples_all
   WHERE ((((((old.space_id)::bigint <> (new.space_id)::bigint) OR (old.space_id IS NULL)) OR (old.expe_id IS NOT NULL)) AND (new.space_id IS NOT NULL)) AND (new.expe_id IS NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD  UPDATE samples_all SET code = new.code, del_id = new.del_id, orig_del = new.orig_del, expe_id = new.expe_id, proj_id = new.proj_id, modification_timestamp = new.modification_timestamp, perm_id = new.perm_id, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, registration_timestamp = new.registration_timestamp, samp_id_part_of = new.samp_id_part_of, saty_id = new.saty_id, space_id = new.space_id, version = new.version
  WHERE ((samples_all.id)::bigint = (new.id)::bigint);


--
-- Name: check_created_or_modified_data_set_owner_is_alive; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_created_or_modified_data_set_owner_is_alive AFTER INSERT OR UPDATE ON data_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_created_or_modified_data_set_owner_is_alive();


--
-- Name: check_created_or_modified_sample_owner_is_alive; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_created_or_modified_sample_owner_is_alive AFTER INSERT OR UPDATE ON samples_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_created_or_modified_sample_owner_is_alive();


--
-- Name: check_deletion_consistency_on_experiment_deletion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_experiment_deletion AFTER UPDATE ON experiments_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_deletion_consistency_on_experiment_deletion();


--
-- Name: check_deletion_consistency_on_sample_deletion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_sample_deletion AFTER UPDATE ON samples_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_deletion_consistency_on_sample_deletion();


--
-- Name: controlled_vocabulary_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER controlled_vocabulary_check BEFORE INSERT OR UPDATE ON property_types FOR EACH ROW EXECUTE PROCEDURE controlled_vocabulary_check();


--
-- Name: data_exp_or_sample_link_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_exp_or_sample_link_check BEFORE INSERT OR UPDATE ON data_all FOR EACH ROW EXECUTE PROCEDURE data_exp_or_sample_link_check();


--
-- Name: data_set_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE data_set_property_with_material_data_type_check();


--
-- Name: disable_project_level_samples; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER disable_project_level_samples BEFORE INSERT OR UPDATE ON samples_all FOR EACH ROW EXECUTE PROCEDURE disable_project_level_samples();


--
-- Name: experiment_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE experiment_property_with_material_data_type_check();


--
-- Name: external_data_storage_format_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER external_data_storage_format_check BEFORE INSERT OR UPDATE ON external_data FOR EACH ROW EXECUTE PROCEDURE external_data_storage_format_check();


--
-- Name: material_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER material_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON material_properties FOR EACH ROW EXECUTE PROCEDURE material_property_with_material_data_type_check();


--
-- Name: preserve_deletion_consistency_on_data_set_relationships; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER preserve_deletion_consistency_on_data_set_relationships BEFORE UPDATE ON data_set_relationships_all FOR EACH ROW EXECUTE PROCEDURE preserve_deletion_consistency_on_data_set_relationships();


--
-- Name: preserve_deletion_consistency_on_sample_relationships; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER preserve_deletion_consistency_on_sample_relationships BEFORE UPDATE ON sample_relationships_all FOR EACH ROW EXECUTE PROCEDURE preserve_deletion_consistency_on_sample_relationships();


--
-- Name: sample_fill_code_unique_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_fill_code_unique_check BEFORE INSERT OR UPDATE ON samples_all FOR EACH ROW EXECUTE PROCEDURE sample_fill_code_unique_check();


--
-- Name: sample_fill_subcode_unique_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_fill_subcode_unique_check BEFORE INSERT OR UPDATE ON samples_all FOR EACH ROW EXECUTE PROCEDURE sample_fill_subcode_unique_check();


--
-- Name: sample_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON sample_properties FOR EACH ROW EXECUTE PROCEDURE sample_property_with_material_data_type_check();


--
-- Name: sample_type_fill_subcode_unique_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_type_fill_subcode_unique_check AFTER UPDATE ON sample_types FOR EACH ROW EXECUTE PROCEDURE sample_type_fill_subcode_unique_check();


--
-- Name: ag_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: agp_ag_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_ag_fk FOREIGN KEY (ag_id) REFERENCES authorization_groups(id);


--
-- Name: agp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pers_fk FOREIGN KEY (pers_id) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: atta_cont_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);


--
-- Name: atta_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);


--
-- Name: atta_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: atta_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: atta_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id);


--
-- Name: covo_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: cvte_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: cvte_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: data_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);


--
-- Name: data_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);


--
-- Name: data_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);


--
-- Name: data_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id);


--
-- Name: data_set_relationships_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT data_set_relationships_pers_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: datarelh_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE SET NULL;


--
-- Name: datarelh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE SET NULL;


--
-- Name: datarelh_main_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_main_data_fk FOREIGN KEY (main_data_id) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: datarelh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE SET NULL;


--
-- Name: del_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY deletions
    ADD CONSTRAINT del_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: dspr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: dspr_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id);


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
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: dsprh_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties_history
    ADD CONSTRAINT dsprh_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: dsprh_dstpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties_history
    ADD CONSTRAINT dsprh_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;


--
-- Name: dsre_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: dsre_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: dsre_data_fk_relationship; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);


--
-- Name: dsre_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);


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
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: dstpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: dstpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: dsty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);


--
-- Name: etpt_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id) ON DELETE CASCADE;


--
-- Name: etpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: etpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: etpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: evnt_exac_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_exac_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);


--
-- Name: evnt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


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
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id);


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
-- Name: expe_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);


--
-- Name: expe_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);


--
-- Name: expe_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: expe_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: expe_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments_all
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
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);


--
-- Name: expr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: expr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: exprh_etpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties_history
    ADD CONSTRAINT exprh_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;


--
-- Name: exprh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties_history
    ADD CONSTRAINT exprh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE CASCADE;


--
-- Name: exrelh_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE SET NULL;


--
-- Name: exrelh_main_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_main_expe_fk FOREIGN KEY (main_expe_id) REFERENCES experiments_all(id) ON DELETE CASCADE;


--
-- Name: exrelh_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id) ON DELETE SET NULL;


--
-- Name: exrelh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE SET NULL;


--
-- Name: exty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);


--
-- Name: filt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: grid_custom_columns_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: lnda_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY link_data
    ADD CONSTRAINT lnda_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: lnda_edms_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY link_data
    ADD CONSTRAINT lnda_edms_fk FOREIGN KEY (edms_id) REFERENCES external_data_management_systems(id);


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
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: maprh_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties_history
    ADD CONSTRAINT maprh_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id) ON DELETE CASCADE;


--
-- Name: maprh_mtpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties_history
    ADD CONSTRAINT maprh_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;


--
-- Name: mate_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);


--
-- Name: mate_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: maty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);


--
-- Name: metaproject_assignments_all_data_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_data_id_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all_del_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_del_id_fk FOREIGN KEY (del_id) REFERENCES deletions(id);


--
-- Name: metaproject_assignments_all_expe_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_expe_id_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all_mate_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mate_id_fk FOREIGN KEY (mate_id) REFERENCES materials(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all_mepr_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_fk FOREIGN KEY (mepr_id) REFERENCES metaprojects(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all_samp_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_samp_id_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE CASCADE;


--
-- Name: metaprojects_owner_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY metaprojects
    ADD CONSTRAINT metaprojects_owner_fk FOREIGN KEY (owner) REFERENCES persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: mtpt_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: mtpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: mtpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: mtpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: pers_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: pers_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: prdq_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY post_registration_dataset_queue
    ADD CONSTRAINT prdq_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id) ON DELETE CASCADE;


--
-- Name: proj_pers_fk_leader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: proj_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: proj_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: proj_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: prrelh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE SET NULL;


--
-- Name: prrelh_main_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_main_proj_fk FOREIGN KEY (main_proj_id) REFERENCES projects(id) ON DELETE CASCADE;


--
-- Name: prrelh_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE SET NULL;


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
-- Name: prty_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: prty_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: quer_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: roas_ag_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_fk_grantee FOREIGN KEY (ag_id_grantee) REFERENCES authorization_groups(id) ON DELETE CASCADE;


--
-- Name: roas_pers_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: roas_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: roas_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE CASCADE;


--
-- Name: samp_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);


--
-- Name: samp_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);


--
-- Name: samp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: samp_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: samp_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: samp_samp_fk_part_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of) REFERENCES samples_all(id);


--
-- Name: samp_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);


--
-- Name: samp_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: sample_relationships_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sample_relationships_pers_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: samprelh_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE SET NULL;


--
-- Name: samprelh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE SET NULL;


--
-- Name: samprelh_main_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_main_samp_fk FOREIGN KEY (main_samp_id) REFERENCES samples_all(id) ON DELETE CASCADE;


--
-- Name: samprelh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE SET NULL;


--
-- Name: samprelh_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE SET NULL;


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
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sapr_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id);


--
-- Name: sapr_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: saprh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties_history
    ADD CONSTRAINT saprh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE CASCADE;


--
-- Name: saprh_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties_history
    ADD CONSTRAINT saprh_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: sare_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child) REFERENCES samples_all(id) ON DELETE CASCADE;


--
-- Name: sare_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent) REFERENCES samples_all(id) ON DELETE CASCADE;


--
-- Name: sare_data_fk_relationship; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);


--
-- Name: sare_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);


--
-- Name: saty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);


--
-- Name: scri_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: space_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: stpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;


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
-- Name: stpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


