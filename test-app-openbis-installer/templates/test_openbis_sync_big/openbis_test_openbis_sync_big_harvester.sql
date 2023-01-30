--
-- PostgreSQL database dump
--

-- Dumped from database version 11.8
-- Dumped by pg_dump version 12.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: archiving_status; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.archiving_status AS character varying(100)
	CONSTRAINT archiving_status_check CHECK (((VALUE)::text = ANY ((ARRAY['LOCKED'::character varying, 'AVAILABLE'::character varying, 'ARCHIVED'::character varying, 'ARCHIVE_PENDING'::character varying, 'UNARCHIVE_PENDING'::character varying, 'BACKUP_PENDING'::character varying])::text[])));


--
-- Name: authorization_role; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY ((ARRAY['ADMIN'::character varying, 'POWER_USER'::character varying, 'USER'::character varying, 'OBSERVER'::character varying, 'ETL_SERVER'::character varying])::text[])));


--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.boolean_char AS boolean DEFAULT false;


--
-- Name: boolean_char_or_unknown; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.code AS character varying(100);


--
-- Name: column_label; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.column_label AS character varying(128);


--
-- Name: data_set_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.data_set_kind AS character varying(40)
	CONSTRAINT data_set_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['PHYSICAL'::character varying, 'LINK'::character varying, 'CONTAINER'::character varying])::text[])));


--
-- Name: data_store_service_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.data_store_service_kind AS character varying(40)
	CONSTRAINT data_store_service_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['PROCESSING'::character varying, 'QUERIES'::character varying])::text[])));


--
-- Name: data_store_service_reporting_plugin_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.data_store_service_reporting_plugin_type AS character varying(40)
	CONSTRAINT data_store_service_reporting_plugin_type_check CHECK (((VALUE)::text = ANY ((ARRAY['TABLE_MODEL'::character varying, 'DSS_LINK'::character varying, 'AGGREGATION_TABLE_MODEL'::character varying])::text[])));


--
-- Name: description_2000; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.description_2000 AS character varying(2000);


--
-- Name: edms_address_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.edms_address_type AS text
	CONSTRAINT edms_address_type_check CHECK ((VALUE = ANY (ARRAY['OPENBIS'::text, 'URL'::text, 'FILE_SYSTEM'::text])));


--
-- Name: entity_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.entity_kind AS character varying(40)
	CONSTRAINT entity_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['SAMPLE'::character varying, 'EXPERIMENT'::character varying, 'DATA_SET'::character varying, 'MATERIAL'::character varying])::text[])));


--
-- Name: event_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DELETION'::character varying, 'MOVEMENT'::character varying, 'FREEZING'::character varying])::text[])));


--
-- Name: file; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.file AS bytea;


--
-- Name: file_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.file_name AS character varying(255);


--
-- Name: grid_expression; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.grid_expression AS character varying(2000);


--
-- Name: grid_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.grid_id AS character varying(200);


--
-- Name: identifier; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.identifier AS character varying(200);


--
-- Name: location_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.location_type AS text
	CONSTRAINT location_type_check CHECK ((VALUE = ANY (ARRAY['OPENBIS'::text, 'URL'::text, 'FILE_SYSTEM_PLAIN'::text, 'FILE_SYSTEM_GIT'::text])));


--
-- Name: object_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.object_name AS character varying(50);


--
-- Name: operation_execution_availability; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.operation_execution_availability AS character varying(40)
	CONSTRAINT operation_execution_availability_check CHECK (((VALUE)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'DELETE_PENDING'::character varying, 'DELETED'::character varying, 'TIME_OUT_PENDING'::character varying, 'TIMED_OUT'::character varying])::text[])));


--
-- Name: operation_execution_state; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.operation_execution_state AS character varying(40)
	CONSTRAINT operation_execution_state_check CHECK (((VALUE)::text = ANY ((ARRAY['NEW'::character varying, 'SCHEDULED'::character varying, 'RUNNING'::character varying, 'FINISHED'::character varying, 'FAILED'::character varying])::text[])));


--
-- Name: ordinal_int; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.ordinal_int AS bigint
	CONSTRAINT ordinal_int_check CHECK ((VALUE > 0));


--
-- Name: plugin_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.plugin_type AS character varying(40)
	CONSTRAINT plugin_type_check CHECK (((VALUE)::text = ANY ((ARRAY['JYTHON'::character varying, 'PREDEPLOYED'::character varying])::text[])));


--
-- Name: query_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.query_type AS character varying(40)
	CONSTRAINT query_type_check CHECK (((VALUE)::text = ANY ((ARRAY['GENERIC'::character varying, 'EXPERIMENT'::character varying, 'SAMPLE'::character varying, 'DATA_SET'::character varying, 'MATERIAL'::character varying])::text[])));


--
-- Name: real_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.real_value AS real;


--
-- Name: script_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.script_type AS character varying(40)
	CONSTRAINT script_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DYNAMIC_PROPERTY'::character varying, 'MANAGED_PROPERTY'::character varying, 'ENTITY_VALIDATION'::character varying])::text[])));


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.tech_id AS bigint;


--
-- Name: text_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.text_value AS text;


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.time_stamp AS timestamp with time zone;


--
-- Name: time_stamp_dfl; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP;


--
-- Name: title_100; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.title_100 AS character varying(100);


--
-- Name: user_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN public.user_id AS character varying(50);


--
-- Name: check_created_or_modified_data_set_owner_is_alive(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.check_created_or_modified_data_set_owner_is_alive() RETURNS trigger
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

CREATE FUNCTION public.check_created_or_modified_sample_owner_is_alive() RETURNS trigger
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
-- Name: check_data_set_kind_link(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.check_data_set_kind_link() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.id;
        IF (kind <> 'LINK') THEN 
            RAISE EXCEPTION 'Link data (Data Set Code: %) must reference a data set of kind LINK (is %).', 
                            NEW.id, kind;
        END IF;
    RETURN NEW;
END;
$$;


--
-- Name: check_data_set_kind_physical(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.check_data_set_kind_physical() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.id;
        IF (kind <> 'PHYSICAL') THEN 
            RAISE EXCEPTION 'External data (Data Set Code: %) must reference a data set of kind PHYSICAL (is %).', 
                            NEW.id, kind;
        END IF;
    RETURN NEW;
END;
$$;


--
-- Name: check_deletion_consistency_on_experiment_deletion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.check_deletion_consistency_on_experiment_deletion() RETURNS trigger
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

CREATE FUNCTION public.check_deletion_consistency_on_sample_deletion() RETURNS trigger
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
-- Name: content_copies_location_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.content_copies_location_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   edms_address_type EDMS_ADDRESS_TYPE;
   index integer;
BEGIN

   select position(address_type in NEW.location_type), address_type into index, edms_address_type from external_data_management_systems
      where id = NEW.edms_id;

   if index != 1 then
      RAISE EXCEPTION 'Insert/Update to content_copies failed. Location type %, but edms.address_type %', NEW.location_type, edms_address_type;
   end if;

   RETURN NEW;

END;
$$;


--
-- Name: content_copies_uniqueness_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.content_copies_uniqueness_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.location_unique_check = NEW.data_id || ',' || 
                              NEW.edms_id || ',' ||
                              coalesce(NEW.path, '') || ',' || 
                              coalesce(NEW.git_commit_hash, '') || ',' || 
                              coalesce(NEW.git_repository_id, '') || ',' || 
                              coalesce(NEW.external_code, '');
  RETURN NEW;
END;
$$;


--
-- Name: controlled_vocabulary_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.controlled_vocabulary_check() RETURNS trigger
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

CREATE FUNCTION public.data_exp_or_sample_link_check() RETURNS trigger
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

CREATE FUNCTION public.data_set_property_with_material_data_type_check() RETURNS trigger
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
-- Name: deletion_description(public.tech_id); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.deletion_description(del_id public.tech_id) RETURNS character varying
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

CREATE FUNCTION public.disable_project_level_samples() RETURNS trigger
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

CREATE FUNCTION public.experiment_property_with_material_data_type_check() RETURNS trigger
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

CREATE FUNCTION public.external_data_storage_format_check() RETURNS trigger
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
      select code into data_code from data_all where id = NEW.id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$;


--
-- Name: material_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.material_property_with_material_data_type_check() RETURNS trigger
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
-- Name: melt_data_set_for(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.melt_data_set_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_CHILDREN = 'f';
    NEW.FROZEN_FOR_PARENTS = 'f';
    NEW.FROZEN_FOR_COMPS = 'f';
    NEW.FROZEN_FOR_CONTS = 'f';
    return NEW;
end;
$$;


--
-- Name: melt_experiment_for(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.melt_experiment_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_SAMP = 'f';
    NEW.FROZEN_FOR_DATA = 'f';
    return NEW;
end;
$$;


--
-- Name: melt_project_for(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.melt_project_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_EXP = 'f';
    NEW.FROZEN_FOR_SAMP = 'f';
    return NEW;
end;
$$;


--
-- Name: melt_sample_for(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.melt_sample_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_COMP = 'f';
    NEW.FROZEN_FOR_CHILDREN = 'f';
    NEW.FROZEN_FOR_PARENTS = 'f';
    NEW.FROZEN_FOR_DATA = 'f';
    return NEW;
end;
$$;


--
-- Name: melt_space_for(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.melt_space_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_PROJ = 'f';
    NEW.FROZEN_FOR_SAMP = 'f';
    return NEW;
end;
$$;


--
-- Name: preserve_deletion_consistency_on_data_set_relationships(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.preserve_deletion_consistency_on_data_set_relationships() RETURNS trigger
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

CREATE FUNCTION public.preserve_deletion_consistency_on_sample_relationships() RETURNS trigger
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
-- Name: raise_delete_from_data_set_exception(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_delete_from_data_set_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    data_id TECH_ID;
BEGIN
    IF (TG_ARGV[0] = 'DATA SET CHILD') THEN
        data_id = old.data_id_parent;
    ELSEIF (TG_ARGV[0] = 'DATA SET PARENT') THEN
        data_id = old.data_id_child;
    ELSEIF (TG_ARGV[0] = 'DATA SET COMPONENT') THEN
        data_id = old.data_id_parent;
    ELSEIF (TG_ARGV[0] = 'DATA SET CONTAINER') THEN
        data_id = old.data_id_child;
    END IF;
    RAISE EXCEPTION 'Operation DELETE % is not allowed because data set % is frozen.', TG_ARGV[0], 
        (select code from data_all where id = data_id);
END;
$$;


--
-- Name: raise_delete_from_experiment_exception(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_delete_from_experiment_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because experiment % is frozen.', TG_ARGV[0], 
        (select code from experiments_all where id = old.expe_id);
END;
$$;


--
-- Name: raise_delete_from_project_exception(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_delete_from_project_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because project % is frozen.', TG_ARGV[0], 
        (select code from projects where id = old.proj_id);
END;
$$;


--
-- Name: raise_delete_from_sample_exception(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_delete_from_sample_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    samp_id TECH_ID;
BEGIN
    IF (TG_ARGV[0] = 'SAMPLE CHILD') THEN
        samp_id = old.sample_id_parent;
    ELSEIF (TG_ARGV[0] = 'SAMPLE PARENT') THEN
        samp_id = old.sample_id_child;
    ELSEIF (TG_ARGV[0] = 'SAMPLE COMPONENT') THEN
        samp_id = old.samp_id_part_of;
    ELSE
        samp_id = old.samp_id;
    END IF;
    RAISE EXCEPTION 'Operation DELETE % is not allowed because sample % is frozen.', TG_ARGV[0], 
        (select code from samples_all where id = samp_id);
END;
$$;


--
-- Name: raise_delete_from_space_exception(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_delete_from_space_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because space % is frozen.', TG_ARGV[0], 
        (select code from spaces where id = old.space_id);
END;
$$;


--
-- Name: raise_exception_frozen_data_set(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_data_set() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    ds_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        ds_id = OLD.ds_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        ds_id = NEW.ds_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because data set % is frozen.', TG_OP, TG_ARGV[0],
        (select code from data_all where id = ds_id);
END;
$$;


--
-- Name: raise_exception_frozen_data_set_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_data_set_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    parent_id           TECH_ID;
    child_id            TECH_ID;
    relationship_id     TECH_ID;
    relationship        CODE;
    parent_child_frozen BOOLEAN_CHAR;
    cont_comp_frozen    BOOLEAN_CHAR;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.data_id_parent;
        child_id = OLD.data_id_child;
        relationship_id = OLD.relationship_id;
        parent_child_frozen = OLD.parent_frozen OR OLD.child_frozen;
        cont_comp_frozen = OLD.cont_frozen OR OLD.comp_frozen;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.data_id_parent;
        child_id = NEW.data_id_child;
        relationship_id = NEW.relationship_id;
        parent_child_frozen = NEW.parent_frozen OR NEW.child_frozen;
        cont_comp_frozen = NEW.cont_frozen OR NEW.comp_frozen;
    END IF;
    SELECT code INTO relationship FROM relationship_types WHERE id = relationship_id;
    IF (relationship = 'PARENT_CHILD' AND parent_child_frozen) OR (relationship = 'CONTAINER_COMPONENT' AND cont_comp_frozen) THEN
       RAISE EXCEPTION 'Operation % % is not allowed because data set % or % is frozen.', TG_OP, relationship,
            (select code from data_all where id = parent_id),
            (select code from data_all where id = child_id);
    END IF;
    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSEIF (TG_OP = 'INSERT') THEN
        RETURN NEW;
    END IF;
END;
$$;


--
-- Name: raise_exception_frozen_data_set_sample_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_data_set_sample_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    sample_id   TECH_ID;
    operation   TEXT;
BEGIN
    IF (NEW.samp_id IS NOT NULL AND NEW.samp_frozen) THEN
        sample_id = NEW.samp_id;
        operation = 'SET SAMPLE';
    ELSEIF (OLD.samp_id IS NOT NULL AND OLD.samp_frozen) THEN
        sample_id = OLD.samp_id;
        operation = 'REMOVE SAMPLE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because sample % is frozen for data set %.', operation,
        (select code from samples_all where id = sample_id), NEW.code;
END;
$$;


--
-- Name: raise_exception_frozen_entity_by_code(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_entity_by_code() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation % is not allowed because % % is frozen.', TG_ARGV[0], TG_ARGV[1], OLD.code;
END;
$$;


--
-- Name: raise_exception_frozen_experiment(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_experiment() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    experiment_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        experiment_id = OLD.expe_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        experiment_id = NEW.expe_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because experiment % is frozen.', TG_OP, TG_ARGV[0],
        (select code from experiments_all where id = experiment_id);
END;
$$;


--
-- Name: raise_exception_frozen_experiment_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_experiment_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    experiment_id   TECH_ID;
    operation       TEXT;
BEGIN
    IF (NEW.expe_id IS NOT NULL AND NEW.expe_frozen) THEN
        experiment_id = NEW.expe_id;
        operation = 'SET EXPERIMENT';
    ELSEIF (OLD.expe_id IS NOT NULL AND OLD.expe_frozen) THEN
        experiment_id = OLD.expe_id;
        operation = 'REMOVE EXPERIMENT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because experiment % is frozen for % %.', operation,
        (select code from experiments_all where id = experiment_id), TG_ARGV[0], NEW.code;
END;
$$;


--
-- Name: raise_exception_frozen_project(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_project() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    project_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        project_id = OLD.proj_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        project_id = NEW.proj_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because project % is frozen.', TG_OP, TG_ARGV[0],
        (select code from projects where id = project_id);
END;
$$;


--
-- Name: raise_exception_frozen_project_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_project_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    project_id   TECH_ID;
    operation    TEXT;
BEGIN
    IF (NEW.proj_id IS NOT NULL AND NEW.proj_frozen) THEN
        project_id = NEW.proj_id;
        operation = 'SET PROJECT';
    ELSEIF (OLD.proj_id IS NOT NULL AND OLD.proj_frozen) THEN
        project_id = OLD.proj_id;
        operation = 'REMOVE PROJECT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because project % is frozen for % %.', operation,
        (select code from projects where id = project_id), TG_ARGV[0], NEW.code;
END;
$$;


--
-- Name: raise_exception_frozen_sample(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_sample() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    sample_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        sample_id = OLD.samp_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        sample_id = NEW.samp_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because sample % is frozen.', TG_OP, TG_ARGV[0],
        (select code from samples_all where id = sample_id);
END;
$$;


--
-- Name: raise_exception_frozen_sample_container_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_sample_container_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    sample_id   TECH_ID;
    operation   TEXT;
BEGIN
    IF (NEW.samp_id_part_of IS NOT NULL AND NEW.CONT_FROZEN) THEN
        sample_id = NEW.samp_id_part_of;
        operation = 'SET CONTAINER';
    ELSEIF (OLD.samp_id_part_of IS NOT NULL AND OLD.CONT_FROZEN) THEN
        sample_id = OLD.samp_id_part_of;
        operation = 'REMOVE CONTAINER';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because sample % is frozen for sample %.', operation,
        (select code from samples_all where id = sample_id), NEW.code;
END;
$$;


--
-- Name: raise_exception_frozen_sample_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_sample_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    parent_id   TECH_ID;
    child_id    TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.sample_id_parent;
        child_id = OLD.sample_id_child;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.sample_id_parent;
        child_id = NEW.sample_id_child;
    END IF;
    RAISE EXCEPTION 'Operation % is not allowed because sample % or % is frozen.', TG_OP, 
        (select code from samples_all where id = parent_id),
        (select code from samples_all where id = child_id);
END;
$$;


--
-- Name: raise_exception_frozen_space_relationship(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.raise_exception_frozen_space_relationship() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    space_id   TECH_ID;
    operation  TEXT;
BEGIN
    IF (NEW.space_id IS NOT NULL AND NEW.space_frozen) THEN
        space_id = NEW.space_id;
        operation = 'SET SPACE';
    ELSEIF (OLD.space_id IS NOT NULL AND OLD.space_frozen) THEN
        space_id = OLD.space_id;
        operation = 'REMOVE SPACE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because space % is frozen for % %.', operation,
        (select code from spaces where id = space_id), TG_ARGV[0], NEW.code;
END;
$$;


--
-- Name: rename_sequence(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.rename_sequence(old_name character varying, new_name character varying) RETURNS integer
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

CREATE FUNCTION public.sample_fill_code_unique_check() RETURNS trigger
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

CREATE FUNCTION public.sample_fill_subcode_unique_check() RETURNS trigger
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

CREATE FUNCTION public.sample_property_with_material_data_type_check() RETURNS trigger
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

CREATE FUNCTION public.sample_type_fill_subcode_unique_check() RETURNS trigger
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

CREATE SEQUENCE public.attachment_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

--
-- Name: attachment_contents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attachment_contents (
    id public.tech_id NOT NULL,
    value public.file NOT NULL
);


--
-- Name: attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attachments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attachments (
    id public.tech_id NOT NULL,
    expe_id public.tech_id,
    samp_id public.tech_id,
    proj_id public.tech_id,
    exac_id public.tech_id NOT NULL,
    file_name public.file_name NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version integer NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    title public.title_100,
    description public.description_2000,
    proj_frozen public.boolean_char DEFAULT false NOT NULL,
    expe_frozen public.boolean_char DEFAULT false NOT NULL,
    samp_frozen public.boolean_char DEFAULT false NOT NULL,
    CONSTRAINT atta_arc_ck CHECK ((((expe_id IS NOT NULL) AND (proj_id IS NULL) AND (samp_id IS NULL)) OR ((expe_id IS NULL) AND (proj_id IS NOT NULL) AND (samp_id IS NULL)) OR ((expe_id IS NULL) AND (proj_id IS NULL) AND (samp_id IS NOT NULL))))
);


--
-- Name: authorization_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.authorization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: authorization_group_persons; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.authorization_group_persons (
    ag_id public.tech_id NOT NULL,
    pers_id public.tech_id NOT NULL
);


--
-- Name: authorization_groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.authorization_groups (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: content_copies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.content_copies (
    id public.tech_id NOT NULL,
    location_type public.location_type NOT NULL,
    data_id public.tech_id NOT NULL,
    edms_id public.tech_id NOT NULL,
    external_code public.text_value,
    path public.text_value,
    git_commit_hash public.text_value,
    git_repository_id public.text_value,
    location_unique_check public.text_value NOT NULL,
    pers_id_registerer public.tech_id,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: content_copies_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.content_copies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: controlled_vocabularies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.controlled_vocabularies (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    is_internal_namespace public.boolean_char DEFAULT false NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    is_chosen_from_list public.boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.controlled_vocabulary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: controlled_vocabulary_terms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.controlled_vocabulary_terms (
    id public.tech_id NOT NULL,
    code public.object_name NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    covo_id public.tech_id NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    label public.column_label,
    description public.description_2000,
    ordinal public.ordinal_int NOT NULL,
    is_official public.boolean_char DEFAULT true NOT NULL,
    CONSTRAINT cvte_ck CHECK (((ordinal)::bigint > 0))
);


--
-- Name: core_plugin_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.core_plugin_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: core_plugins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.core_plugins (
    id public.tech_id NOT NULL,
    name character varying(200) NOT NULL,
    version integer NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    master_reg_script public.text_value
);


--
-- Name: cvte_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cvte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_all; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_all (
    id public.tech_id NOT NULL,
    code public.code,
    data_set_kind public.data_set_kind DEFAULT 'PHYSICAL'::character varying NOT NULL,
    dsty_id public.tech_id NOT NULL,
    dast_id public.tech_id NOT NULL,
    expe_id public.tech_id,
    data_producer_code public.code,
    production_timestamp public.time_stamp,
    samp_id public.tech_id,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id,
    is_valid public.boolean_char DEFAULT true,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    access_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_derived public.boolean_char NOT NULL,
    del_id public.tech_id,
    orig_del public.tech_id,
    pers_id_modifier public.tech_id,
    version integer DEFAULT 0,
    frozen public.boolean_char DEFAULT false NOT NULL,
    frozen_for_children public.boolean_char DEFAULT false NOT NULL,
    frozen_for_parents public.boolean_char DEFAULT false NOT NULL,
    frozen_for_comps public.boolean_char DEFAULT false NOT NULL,
    frozen_for_conts public.boolean_char DEFAULT false NOT NULL,
    expe_frozen public.boolean_char DEFAULT false NOT NULL,
    samp_frozen public.boolean_char DEFAULT false NOT NULL,
    CONSTRAINT data_ck CHECK (((expe_id IS NOT NULL) OR (samp_id IS NOT NULL)))
);


--
-- Name: data; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.data AS
 SELECT data_all.id,
    data_all.code,
    data_all.dsty_id,
    data_all.dast_id,
    data_all.expe_id,
    data_all.expe_frozen,
    data_all.data_producer_code,
    data_all.production_timestamp,
    data_all.samp_id,
    data_all.samp_frozen,
    data_all.registration_timestamp,
    data_all.access_timestamp,
    data_all.pers_id_registerer,
    data_all.pers_id_modifier,
    data_all.is_valid,
    data_all.modification_timestamp,
    data_all.is_derived,
    data_all.del_id,
    data_all.orig_del,
    data_all.version,
    data_all.data_set_kind,
    data_all.frozen,
    data_all.frozen_for_children,
    data_all.frozen_for_parents,
    data_all.frozen_for_comps,
    data_all.frozen_for_conts
   FROM public.data_all
  WHERE (data_all.del_id IS NULL);


--
-- Name: data_deleted; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.data_deleted AS
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
    data_all.version,
    data_all.data_set_kind
   FROM public.data_all
  WHERE (data_all.del_id IS NOT NULL);


--
-- Name: data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_copies_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_copies_history (
    id public.tech_id NOT NULL,
    cc_id public.tech_id NOT NULL,
    data_id public.tech_id NOT NULL,
    external_code public.text_value,
    path public.text_value,
    git_commit_hash public.text_value,
    git_repository_id public.text_value,
    edms_id public.tech_id NOT NULL,
    edms_code public.code,
    edms_label public.text_value,
    edms_address public.text_value,
    pers_id_author public.tech_id,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp
);


--
-- Name: data_set_copies_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_set_copies_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_properties_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_properties_history (
    id public.tech_id NOT NULL,
    ds_id public.tech_id NOT NULL,
    dstpt_id public.tech_id NOT NULL,
    value public.text_value,
    vocabulary_term public.identifier,
    material public.identifier,
    pers_id_author public.tech_id NOT NULL,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT dsprh_ck CHECK ((((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL))))
);


--
-- Name: data_set_relationships_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_relationships_history (
    id public.tech_id NOT NULL,
    main_data_id public.tech_id NOT NULL,
    relation_type public.text_value,
    ordinal integer,
    expe_id public.tech_id,
    samp_id public.tech_id,
    data_id public.tech_id,
    entity_perm_id public.text_value,
    pers_id_author public.tech_id,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp
);


--
-- Name: data_set_history_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.data_set_history_view AS
 SELECT (3 * (data_set_relationships_history.id)::bigint) AS id,
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
    NULL::text AS external_code,
    NULL::text AS path,
    NULL::text AS git_commit_hash,
    NULL::text AS git_repository_id,
    (NULL::bigint)::public.tech_id AS edms_id,
    NULL::text AS edms_code,
    NULL::text AS edms_label,
    NULL::text AS edms_address,
    data_set_relationships_history.pers_id_author,
    data_set_relationships_history.valid_from_timestamp,
    data_set_relationships_history.valid_until_timestamp
   FROM public.data_set_relationships_history
  WHERE (data_set_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
 SELECT ((3 * (data_set_properties_history.id)::bigint) + 1) AS id,
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
    NULL::text AS external_code,
    NULL::text AS path,
    NULL::text AS git_commit_hash,
    NULL::text AS git_repository_id,
    NULL::bigint AS edms_id,
    NULL::text AS edms_code,
    NULL::text AS edms_label,
    NULL::text AS edms_address,
    data_set_properties_history.pers_id_author,
    data_set_properties_history.valid_from_timestamp,
    data_set_properties_history.valid_until_timestamp
   FROM public.data_set_properties_history
UNION
 SELECT ((3 * (data_set_copies_history.id)::bigint) + 2) AS id,
    data_set_copies_history.data_id AS main_data_id,
    NULL::text AS relation_type,
    NULL::integer AS ordinal,
    NULL::bigint AS expe_id,
    NULL::bigint AS samp_id,
    NULL::bigint AS data_id,
    NULL::text AS entity_perm_id,
    NULL::bigint AS dstpt_id,
    NULL::text AS value,
    NULL::character varying AS vocabulary_term,
    NULL::character varying AS material,
    data_set_copies_history.external_code,
    data_set_copies_history.path,
    data_set_copies_history.git_commit_hash,
    data_set_copies_history.git_repository_id,
    data_set_copies_history.edms_id,
    data_set_copies_history.edms_code,
    data_set_copies_history.edms_label,
    data_set_copies_history.edms_address,
    data_set_copies_history.pers_id_author,
    data_set_copies_history.valid_from_timestamp,
    data_set_copies_history.valid_until_timestamp
   FROM public.data_set_copies_history
  WHERE (data_set_copies_history.valid_until_timestamp IS NOT NULL);


--
-- Name: data_set_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_properties (
    id public.tech_id NOT NULL,
    ds_id public.tech_id NOT NULL,
    dstpt_id public.tech_id NOT NULL,
    value public.text_value,
    cvte_id public.tech_id,
    mate_prop_id public.tech_id,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_author public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    dase_frozen public.boolean_char DEFAULT false NOT NULL,
    CONSTRAINT dspr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: data_set_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_set_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_relationships_all; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_relationships_all (
    data_id_parent public.tech_id NOT NULL,
    data_id_child public.tech_id NOT NULL,
    relationship_id public.tech_id NOT NULL,
    ordinal integer,
    del_id public.tech_id,
    pers_id_author public.tech_id,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    parent_frozen public.boolean_char DEFAULT false NOT NULL,
    child_frozen public.boolean_char DEFAULT false NOT NULL,
    comp_frozen public.boolean_char DEFAULT false NOT NULL,
    cont_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: data_set_relationships; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.data_set_relationships AS
 SELECT data_set_relationships_all.data_id_parent,
    data_set_relationships_all.parent_frozen,
    data_set_relationships_all.cont_frozen,
    data_set_relationships_all.data_id_child,
    data_set_relationships_all.child_frozen,
    data_set_relationships_all.comp_frozen,
    data_set_relationships_all.relationship_id,
    data_set_relationships_all.ordinal,
    data_set_relationships_all.del_id,
    data_set_relationships_all.pers_id_author,
    data_set_relationships_all.registration_timestamp,
    data_set_relationships_all.modification_timestamp
   FROM public.data_set_relationships_all
  WHERE (data_set_relationships_all.del_id IS NULL);


--
-- Name: data_set_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_set_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_set_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_type_property_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_type_property_types (
    id public.tech_id NOT NULL,
    dsty_id public.tech_id NOT NULL,
    prty_id public.tech_id NOT NULL,
    is_mandatory public.boolean_char DEFAULT false NOT NULL,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ordinal public.ordinal_int NOT NULL,
    section public.description_2000,
    script_id public.tech_id,
    is_shown_edit public.boolean_char DEFAULT true NOT NULL,
    show_raw_value public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: data_set_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_set_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    main_ds_pattern character varying(300),
    main_ds_path character varying(1000),
    deletion_disallow public.boolean_char DEFAULT false,
    validation_script_id public.tech_id
);


--
-- Name: data_store_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_store_service_data_set_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_store_service_data_set_types (
    data_store_service_id public.tech_id NOT NULL,
    data_set_type_id public.tech_id NOT NULL
);


--
-- Name: data_store_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_store_services (
    id public.tech_id NOT NULL,
    key character varying(256) NOT NULL,
    label character varying(256) NOT NULL,
    kind public.data_store_service_kind NOT NULL,
    data_store_id public.tech_id NOT NULL,
    reporting_plugin_type public.data_store_service_reporting_plugin_type
);


--
-- Name: data_store_services_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_store_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_stores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_stores (
    id public.tech_id NOT NULL,
    uuid public.code NOT NULL,
    code public.code NOT NULL,
    download_url character varying(1024) NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    is_archiver_configured public.boolean_char DEFAULT false NOT NULL,
    data_source_definitions public.text_value
);


--
-- Name: data_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000 NOT NULL
);


--
-- Name: database_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.database_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: database_version_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.database_version_logs (
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

CREATE SEQUENCE public.deletion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: deletions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.deletions (
    id public.tech_id NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    reason public.description_2000 NOT NULL
);


--
-- Name: dstpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dstpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entity_operations_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.entity_operations_log (
    id public.tech_id NOT NULL,
    registration_id public.tech_id NOT NULL
);


--
-- Name: entity_operations_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.entity_operations_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: etpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.etpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.events (
    id public.tech_id NOT NULL,
    event_type public.event_type NOT NULL,
    description public.text_value,
    reason public.description_2000,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    entity_type character varying(80) NOT NULL,
    identifiers public.text_value NOT NULL,
    content public.text_value,
    exac_id public.tech_id,
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY ((ARRAY['ATTACHMENT'::character varying, 'DATASET'::character varying, 'EXPERIMENT'::character varying, 'SPACE'::character varying, 'MATERIAL'::character varying, 'PROJECT'::character varying, 'PROPERTY_TYPE'::character varying, 'SAMPLE'::character varying, 'VOCABULARY'::character varying, 'AUTHORIZATION_GROUP'::character varying, 'METAPROJECT'::character varying])::text[])))
);


--
-- Name: experiment_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.experiment_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_properties_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.experiment_properties_history (
    id public.tech_id NOT NULL,
    expe_id public.tech_id NOT NULL,
    etpt_id public.tech_id NOT NULL,
    value public.text_value,
    vocabulary_term public.identifier,
    material public.identifier,
    pers_id_author public.tech_id NOT NULL,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT exprh_ck CHECK ((((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL))))
);


--
-- Name: experiment_relationships_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.experiment_relationships_history (
    id public.tech_id NOT NULL,
    main_expe_id public.tech_id NOT NULL,
    relation_type public.text_value,
    samp_id public.tech_id,
    data_id public.tech_id,
    entity_perm_id public.text_value,
    pers_id_author public.tech_id,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp,
    proj_id public.tech_id
);


--
-- Name: experiment_history_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.experiment_history_view AS
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
   FROM public.experiment_relationships_history
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
   FROM public.experiment_properties_history;


--
-- Name: experiment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.experiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.experiment_properties (
    id public.tech_id NOT NULL,
    expe_id public.tech_id NOT NULL,
    etpt_id public.tech_id NOT NULL,
    value public.text_value,
    cvte_id public.tech_id,
    mate_prop_id public.tech_id,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_author public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    expe_frozen public.boolean_char DEFAULT false NOT NULL,
    CONSTRAINT expr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: experiment_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.experiment_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.experiment_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.experiment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_type_property_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.experiment_type_property_types (
    id public.tech_id NOT NULL,
    exty_id public.tech_id NOT NULL,
    prty_id public.tech_id NOT NULL,
    is_mandatory public.boolean_char DEFAULT false NOT NULL,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ordinal public.ordinal_int NOT NULL,
    section public.description_2000,
    script_id public.tech_id,
    is_shown_edit public.boolean_char DEFAULT true NOT NULL,
    show_raw_value public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: experiment_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.experiment_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    validation_script_id public.tech_id
);


--
-- Name: experiments_all; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.experiments_all (
    id public.tech_id NOT NULL,
    perm_id public.code NOT NULL,
    code public.code NOT NULL,
    exty_id public.tech_id NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    proj_id public.tech_id NOT NULL,
    del_id public.tech_id,
    orig_del public.tech_id,
    is_public public.boolean_char DEFAULT false NOT NULL,
    pers_id_modifier public.tech_id,
    version integer DEFAULT 0,
    frozen public.boolean_char DEFAULT false NOT NULL,
    frozen_for_samp public.boolean_char DEFAULT false NOT NULL,
    frozen_for_data public.boolean_char DEFAULT false NOT NULL,
    proj_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: experiments; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.experiments AS
 SELECT experiments_all.id,
    experiments_all.perm_id,
    experiments_all.code,
    experiments_all.exty_id,
    experiments_all.pers_id_registerer,
    experiments_all.pers_id_modifier,
    experiments_all.registration_timestamp,
    experiments_all.modification_timestamp,
    experiments_all.proj_id,
    experiments_all.proj_frozen,
    experiments_all.del_id,
    experiments_all.orig_del,
    experiments_all.is_public,
    experiments_all.version,
    experiments_all.frozen,
    experiments_all.frozen_for_samp,
    experiments_all.frozen_for_data
   FROM public.experiments_all
  WHERE (experiments_all.del_id IS NULL);


--
-- Name: experiments_deleted; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.experiments_deleted AS
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
   FROM public.experiments_all
  WHERE (experiments_all.del_id IS NOT NULL);


--
-- Name: external_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.external_data (
    id public.tech_id NOT NULL,
    share_id public.code,
    size public.ordinal_int,
    location character varying(1024) NOT NULL,
    ffty_id public.tech_id NOT NULL,
    loty_id public.tech_id NOT NULL,
    cvte_id_stor_fmt public.tech_id NOT NULL,
    is_complete public.boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store public.tech_id,
    status public.archiving_status DEFAULT 'AVAILABLE'::character varying NOT NULL,
    present_in_archive public.boolean_char DEFAULT false,
    speed_hint integer DEFAULT '-50'::integer NOT NULL,
    storage_confirmation public.boolean_char DEFAULT false NOT NULL,
    h5_folders public.boolean_char NOT NULL,
    h5ar_folders public.boolean_char NOT NULL,
    archiving_requested public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: external_data_management_system_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.external_data_management_system_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: external_data_management_systems; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.external_data_management_systems (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    label public.text_value,
    address public.text_value NOT NULL,
    address_type public.edms_address_type NOT NULL
);


--
-- Name: file_format_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.file_format_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: file_format_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file_format_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000
);


--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: filters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.filters (
    id public.tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    expression text NOT NULL,
    is_public boolean NOT NULL,
    grid_id character varying(200) NOT NULL
);


--
-- Name: grid_custom_columns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.grid_custom_columns (
    id public.tech_id NOT NULL,
    code character varying(200) NOT NULL,
    label public.column_label NOT NULL,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    expression public.grid_expression NOT NULL,
    is_public boolean NOT NULL,
    grid_id public.grid_id NOT NULL
);


--
-- Name: grid_custom_columns_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.grid_custom_columns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.link_data (
    id public.tech_id NOT NULL,
    data_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: locator_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.locator_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: locator_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.locator_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000
);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.material_properties (
    id public.tech_id NOT NULL,
    mate_id public.tech_id NOT NULL,
    mtpt_id public.tech_id NOT NULL,
    value public.text_value,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_author public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    pers_id_registerer public.tech_id NOT NULL,
    cvte_id public.tech_id,
    mate_prop_id public.tech_id,
    CONSTRAINT mapr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: material_properties_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.material_properties_history (
    id public.tech_id NOT NULL,
    mate_id public.tech_id NOT NULL,
    mtpt_id public.tech_id NOT NULL,
    value public.text_value,
    vocabulary_term public.identifier,
    material public.identifier,
    pers_id_author public.tech_id NOT NULL,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT maprh_ck CHECK ((((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL))))
);


--
-- Name: material_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.material_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.material_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_type_property_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.material_type_property_types (
    id public.tech_id NOT NULL,
    maty_id public.tech_id NOT NULL,
    prty_id public.tech_id NOT NULL,
    is_mandatory public.boolean_char DEFAULT false NOT NULL,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    ordinal public.ordinal_int NOT NULL,
    section public.description_2000,
    script_id public.tech_id,
    is_shown_edit public.boolean_char DEFAULT true NOT NULL,
    show_raw_value public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: material_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.material_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    validation_script_id public.tech_id
);


--
-- Name: materials; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.materials (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    maty_id public.tech_id NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: metaproject_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.metaproject_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: metaproject_assignments_all; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.metaproject_assignments_all (
    id public.tech_id NOT NULL,
    mepr_id public.tech_id NOT NULL,
    expe_id public.tech_id,
    samp_id public.tech_id,
    data_id public.tech_id,
    mate_id public.tech_id,
    del_id public.tech_id,
    creation_date public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT metaproject_assignments_all_check_nn CHECK ((((expe_id IS NOT NULL) AND (samp_id IS NULL) AND (data_id IS NULL) AND (mate_id IS NULL)) OR ((expe_id IS NULL) AND (samp_id IS NOT NULL) AND (data_id IS NULL) AND (mate_id IS NULL)) OR ((expe_id IS NULL) AND (samp_id IS NULL) AND (data_id IS NOT NULL) AND (mate_id IS NULL)) OR ((expe_id IS NULL) AND (samp_id IS NULL) AND (data_id IS NULL) AND (mate_id IS NOT NULL))))
);


--
-- Name: metaproject_assignments; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.metaproject_assignments AS
 SELECT metaproject_assignments_all.id,
    metaproject_assignments_all.mepr_id,
    metaproject_assignments_all.expe_id,
    metaproject_assignments_all.samp_id,
    metaproject_assignments_all.data_id,
    metaproject_assignments_all.mate_id,
    metaproject_assignments_all.del_id,
    metaproject_assignments_all.creation_date
   FROM public.metaproject_assignments_all
  WHERE (metaproject_assignments_all.del_id IS NULL);


--
-- Name: metaproject_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.metaproject_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: metaprojects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.metaprojects (
    id public.tech_id NOT NULL,
    name public.code NOT NULL,
    description public.description_2000,
    owner public.tech_id NOT NULL,
    private public.boolean_char DEFAULT true NOT NULL,
    creation_date public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: mtpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mtpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operation_executions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operation_executions (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    state public.operation_execution_state DEFAULT 'NEW'::character varying NOT NULL,
    owner public.tech_id NOT NULL,
    description public.text_value,
    notification public.text_value,
    availability public.operation_execution_availability DEFAULT 'AVAILABLE'::character varying NOT NULL,
    availability_time bigint DEFAULT 1 NOT NULL,
    summary_operations public.text_value,
    summary_progress public.text_value,
    summary_error public.text_value,
    summary_results public.text_value,
    summary_availability public.operation_execution_availability DEFAULT 'AVAILABLE'::character varying NOT NULL,
    summary_availability_time bigint DEFAULT 1 NOT NULL,
    details_path character varying(1000),
    details_availability public.operation_execution_availability DEFAULT 'AVAILABLE'::character varying NOT NULL,
    details_availability_time bigint DEFAULT 1 NOT NULL,
    creation_date public.time_stamp_dfl NOT NULL,
    start_date public.time_stamp,
    finish_date public.time_stamp,
    CONSTRAINT operation_executions_state_finish_date_check CHECK (((((state)::text = ANY ((ARRAY['NEW'::character varying, 'SCHEDULED'::character varying, 'RUNNING'::character varying])::text[])) AND (finish_date IS NULL)) OR (((state)::text = ANY ((ARRAY['FINISHED'::character varying, 'FAILED'::character varying])::text[])) AND (finish_date IS NOT NULL)))),
    CONSTRAINT operation_executions_state_start_date_check CHECK (((((state)::text = ANY ((ARRAY['NEW'::character varying, 'SCHEDULED'::character varying])::text[])) AND (start_date IS NULL)) OR (((state)::text = ANY ((ARRAY['RUNNING'::character varying, 'FINISHED'::character varying, 'FAILED'::character varying])::text[])) AND (start_date IS NOT NULL))))
);


--
-- Name: operation_executions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.operation_executions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: perm_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.perm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: persons; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.persons (
    id public.tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id public.user_id NOT NULL,
    email public.object_name,
    space_id public.tech_id,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id,
    display_settings public.file,
    is_active boolean DEFAULT true
);


--
-- Name: post_registration_dataset_queue; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.post_registration_dataset_queue (
    id public.tech_id NOT NULL,
    ds_id public.tech_id NOT NULL
);


--
-- Name: post_registration_dataset_queue_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.post_registration_dataset_queue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: project_relationships_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.project_relationships_history (
    id public.tech_id NOT NULL,
    main_proj_id public.tech_id NOT NULL,
    relation_type public.text_value,
    expe_id public.tech_id,
    space_id public.tech_id,
    entity_perm_id public.text_value,
    pers_id_author public.tech_id,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp
);


--
-- Name: project_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.project_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.projects (
    id public.tech_id NOT NULL,
    perm_id public.code NOT NULL,
    code public.code NOT NULL,
    space_id public.tech_id NOT NULL,
    pers_id_leader public.tech_id,
    description public.text_value,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    pers_id_modifier public.tech_id,
    version integer DEFAULT 0,
    frozen public.boolean_char DEFAULT false NOT NULL,
    frozen_for_exp public.boolean_char DEFAULT false NOT NULL,
    frozen_for_samp public.boolean_char DEFAULT false NOT NULL,
    space_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: property_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.property_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: property_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.property_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000 NOT NULL,
    label public.column_label NOT NULL,
    daty_id public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    covo_id public.tech_id,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    is_internal_namespace public.boolean_char DEFAULT false NOT NULL,
    maty_prop_id public.tech_id,
    schema public.text_value,
    transformation public.text_value,
    meta_data jsonb
);


--
-- Name: queries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.queries (
    id public.tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    expression text NOT NULL,
    is_public boolean NOT NULL,
    query_type public.query_type NOT NULL,
    entity_type_code public.code,
    db_key public.code DEFAULT '1'::character varying NOT NULL
);


--
-- Name: query_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: relationship_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.relationship_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: relationship_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.relationship_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    label public.column_label,
    parent_label public.column_label,
    child_label public.column_label,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    is_internal_namespace public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: role_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_assignments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_assignments (
    id public.tech_id NOT NULL,
    role_code public.authorization_role NOT NULL,
    space_id public.tech_id,
    project_id public.tech_id,
    pers_id_grantee public.tech_id,
    ag_id_grantee public.tech_id,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT roas_ag_pers_arc_ck CHECK ((((ag_id_grantee IS NOT NULL) AND (pers_id_grantee IS NULL)) OR ((ag_id_grantee IS NULL) AND (pers_id_grantee IS NOT NULL)))),
    CONSTRAINT roas_space_project_ck CHECK (((space_id IS NULL) OR (project_id IS NULL)))
);


--
-- Name: sample_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sample_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_properties_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sample_properties_history (
    id public.tech_id NOT NULL,
    samp_id public.tech_id NOT NULL,
    stpt_id public.tech_id NOT NULL,
    value public.text_value,
    vocabulary_term public.identifier,
    material public.identifier,
    pers_id_author public.tech_id NOT NULL,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT saprh_ck CHECK ((((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL)) OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL))))
);


--
-- Name: sample_relationships_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sample_relationships_history (
    id public.tech_id NOT NULL,
    main_samp_id public.tech_id NOT NULL,
    relation_type public.text_value,
    expe_id public.tech_id,
    samp_id public.tech_id,
    data_id public.tech_id,
    entity_perm_id public.text_value,
    pers_id_author public.tech_id,
    valid_from_timestamp public.time_stamp NOT NULL,
    valid_until_timestamp public.time_stamp,
    space_id public.tech_id,
    proj_id public.tech_id
);


--
-- Name: sample_history_view; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.sample_history_view AS
 SELECT (2 * (sample_relationships_history.id)::bigint) AS id,
    sample_relationships_history.main_samp_id,
    sample_relationships_history.relation_type,
    sample_relationships_history.space_id,
    sample_relationships_history.expe_id,
    sample_relationships_history.samp_id,
    sample_relationships_history.proj_id,
    sample_relationships_history.data_id,
    sample_relationships_history.entity_perm_id,
    NULL::bigint AS stpt_id,
    NULL::text AS value,
    NULL::character varying AS vocabulary_term,
    NULL::character varying AS material,
    sample_relationships_history.pers_id_author,
    sample_relationships_history.valid_from_timestamp,
    sample_relationships_history.valid_until_timestamp
   FROM public.sample_relationships_history
  WHERE (sample_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
 SELECT ((2 * (sample_properties_history.id)::bigint) + 1) AS id,
    sample_properties_history.samp_id AS main_samp_id,
    NULL::text AS relation_type,
    NULL::bigint AS space_id,
    NULL::bigint AS expe_id,
    NULL::bigint AS samp_id,
    NULL::bigint AS proj_id,
    NULL::bigint AS data_id,
    NULL::text AS entity_perm_id,
    sample_properties_history.stpt_id,
    sample_properties_history.value,
    sample_properties_history.vocabulary_term,
    sample_properties_history.material,
    sample_properties_history.pers_id_author,
    sample_properties_history.valid_from_timestamp,
    sample_properties_history.valid_until_timestamp
   FROM public.sample_properties_history;


--
-- Name: sample_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sample_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_properties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sample_properties (
    id public.tech_id NOT NULL,
    samp_id public.tech_id NOT NULL,
    stpt_id public.tech_id NOT NULL,
    value public.text_value,
    cvte_id public.tech_id,
    mate_prop_id public.tech_id,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_author public.tech_id NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    samp_frozen public.boolean_char DEFAULT false NOT NULL,
    CONSTRAINT sapr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: sample_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sample_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sample_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_relationships_all; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sample_relationships_all (
    id public.tech_id NOT NULL,
    sample_id_parent public.tech_id NOT NULL,
    relationship_id public.tech_id NOT NULL,
    sample_id_child public.tech_id NOT NULL,
    del_id public.tech_id,
    pers_id_author public.tech_id,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    parent_frozen public.boolean_char DEFAULT false NOT NULL,
    child_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: sample_relationships; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.sample_relationships AS
 SELECT sample_relationships_all.id,
    sample_relationships_all.sample_id_parent,
    sample_relationships_all.parent_frozen,
    sample_relationships_all.relationship_id,
    sample_relationships_all.sample_id_child,
    sample_relationships_all.child_frozen,
    sample_relationships_all.del_id,
    sample_relationships_all.pers_id_author,
    sample_relationships_all.registration_timestamp,
    sample_relationships_all.modification_timestamp
   FROM public.sample_relationships_all
  WHERE (sample_relationships_all.del_id IS NULL);


--
-- Name: sample_relationships_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sample_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sample_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_type_property_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sample_type_property_types (
    id public.tech_id NOT NULL,
    saty_id public.tech_id NOT NULL,
    prty_id public.tech_id NOT NULL,
    is_mandatory public.boolean_char DEFAULT false NOT NULL,
    is_managed_internally public.boolean_char DEFAULT false NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_displayed public.boolean_char DEFAULT true NOT NULL,
    ordinal public.ordinal_int NOT NULL,
    section public.description_2000,
    script_id public.tech_id,
    is_shown_edit public.boolean_char DEFAULT true NOT NULL,
    show_raw_value public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: sample_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sample_types (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    is_listable public.boolean_char DEFAULT true NOT NULL,
    generated_from_depth integer DEFAULT 0 NOT NULL,
    part_of_depth integer DEFAULT 0 NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    is_auto_generated_code public.boolean_char DEFAULT false NOT NULL,
    generated_code_prefix public.code DEFAULT 'S'::character varying NOT NULL,
    is_subcode_unique public.boolean_char DEFAULT false NOT NULL,
    inherit_properties public.boolean_char DEFAULT false NOT NULL,
    validation_script_id public.tech_id,
    show_parent_metadata public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: samples_all; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.samples_all (
    id public.tech_id NOT NULL,
    perm_id public.code NOT NULL,
    code public.code NOT NULL,
    expe_id public.tech_id,
    saty_id public.tech_id NOT NULL,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modification_timestamp public.time_stamp DEFAULT CURRENT_TIMESTAMP,
    pers_id_registerer public.tech_id NOT NULL,
    del_id public.tech_id,
    orig_del public.tech_id,
    space_id public.tech_id,
    samp_id_part_of public.tech_id,
    pers_id_modifier public.tech_id,
    code_unique_check character varying(300),
    subcode_unique_check character varying(300),
    version integer DEFAULT 0,
    proj_id public.tech_id,
    frozen public.boolean_char DEFAULT false NOT NULL,
    frozen_for_comp public.boolean_char DEFAULT false NOT NULL,
    frozen_for_children public.boolean_char DEFAULT false NOT NULL,
    frozen_for_parents public.boolean_char DEFAULT false NOT NULL,
    frozen_for_data public.boolean_char DEFAULT false NOT NULL,
    space_frozen public.boolean_char DEFAULT false NOT NULL,
    proj_frozen public.boolean_char DEFAULT false NOT NULL,
    expe_frozen public.boolean_char DEFAULT false NOT NULL,
    cont_frozen public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: samples; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.samples AS
 SELECT samples_all.id,
    samples_all.perm_id,
    samples_all.code,
    samples_all.proj_id,
    samples_all.proj_frozen,
    samples_all.expe_id,
    samples_all.expe_frozen,
    samples_all.saty_id,
    samples_all.registration_timestamp,
    samples_all.modification_timestamp,
    samples_all.pers_id_registerer,
    samples_all.pers_id_modifier,
    samples_all.del_id,
    samples_all.orig_del,
    samples_all.space_id,
    samples_all.space_frozen,
    samples_all.samp_id_part_of,
    samples_all.cont_frozen,
    samples_all.version,
    samples_all.frozen,
    samples_all.frozen_for_comp,
    samples_all.frozen_for_children,
    samples_all.frozen_for_parents,
    samples_all.frozen_for_data
   FROM public.samples_all
  WHERE (samples_all.del_id IS NULL);


--
-- Name: samples_deleted; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.samples_deleted AS
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
    samples_all.proj_id,
    samples_all.samp_id_part_of,
    samples_all.version
   FROM public.samples_all
  WHERE (samples_all.del_id IS NOT NULL);


--
-- Name: script_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: scripts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.scripts (
    id public.tech_id NOT NULL,
    name character varying(200) NOT NULL,
    script_type public.script_type NOT NULL,
    description public.description_2000,
    script public.text_value,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    entity_kind public.entity_kind,
    plugin_type public.plugin_type DEFAULT 'JYTHON'::character varying NOT NULL,
    is_available public.boolean_char DEFAULT true NOT NULL,
    CONSTRAINT script_nn_ck CHECK ((((plugin_type)::text = 'PREDEPLOYED'::text) OR (script IS NOT NULL)))
);


--
-- Name: semantic_annotation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.semantic_annotation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: semantic_annotations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.semantic_annotations (
    id public.tech_id NOT NULL,
    perm_id public.code NOT NULL,
    saty_id public.tech_id,
    stpt_id public.tech_id,
    prty_id public.tech_id,
    predicate_ontology_id text,
    predicate_ontology_version text,
    predicate_accession_id text,
    descriptor_ontology_id text,
    descriptor_ontology_version text,
    descriptor_accession_id text,
    creation_date public.time_stamp_dfl NOT NULL,
    CONSTRAINT semantic_annotations_ssp_ck CHECK ((((saty_id IS NOT NULL) AND (stpt_id IS NULL) AND (prty_id IS NULL)) OR ((saty_id IS NULL) AND (stpt_id IS NOT NULL) AND (prty_id IS NULL)) OR ((saty_id IS NULL) AND (stpt_id IS NULL) AND (prty_id IS NOT NULL))))
);


--
-- Name: space_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.space_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: spaces; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.spaces (
    id public.tech_id NOT NULL,
    code public.code NOT NULL,
    description public.description_2000,
    registration_timestamp public.time_stamp_dfl DEFAULT CURRENT_TIMESTAMP NOT NULL,
    pers_id_registerer public.tech_id NOT NULL,
    frozen public.boolean_char DEFAULT false NOT NULL,
    frozen_for_proj public.boolean_char DEFAULT false NOT NULL,
    frozen_for_samp public.boolean_char DEFAULT false NOT NULL
);


--
-- Name: stpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Data for Name: attachment_contents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.attachment_contents (id, value) FROM stdin;
\.


--
-- Data for Name: attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.attachments (id, expe_id, samp_id, proj_id, exac_id, file_name, registration_timestamp, version, pers_id_registerer, title, description, proj_frozen, expe_frozen, samp_frozen) FROM stdin;
\.


--
-- Data for Name: authorization_group_persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.authorization_group_persons (ag_id, pers_id) FROM stdin;
\.


--
-- Data for Name: authorization_groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.authorization_groups (id, code, description, registration_timestamp, pers_id_registerer, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: content_copies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.content_copies (id, location_type, data_id, edms_id, external_code, path, git_commit_hash, git_repository_id, location_unique_check, pers_id_registerer, registration_timestamp, data_frozen) FROM stdin;
\.


--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
1	STORAGE_FORMAT	The on-disk storage format of a data set	2020-10-01 08:03:14.224058+02	1	t	t	2020-10-01 08:03:14.224058+02	t	\N
\.


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer, label, description, ordinal, is_official) FROM stdin;
1	PROPRIETARY	2020-10-01 08:03:14.224058+02	1	1	\N	\N	1	t
2	BDS_DIRECTORY	2020-10-01 08:03:14.224058+02	1	1	\N	\N	2	t
\.


--
-- Data for Name: core_plugins; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.core_plugins (id, name, version, registration_timestamp, master_reg_script) FROM stdin;
1	search-store	1	2020-10-01 08:03:25.228432+02	#\n# Copyright 2014 ETH Zuerich, Scientific IT Services\n#\n# Licensed under the Apache License, Version 2.0 (the "License");\n# you may not use this file except in compliance with the License.\n# You may obtain a copy of the License at\n#\n#      http://www.apache.org/licenses/LICENSE-2.0\n#\n# Unless required by applicable law or agreed to in writing, software\n# distributed under the License is distributed on an "AS IS" BASIS,\n# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n# See the License for the specific language governing permissions and\n# limitations under the License.\n#\n# MasterDataRegistrationTransaction Class\nfrom ch.ethz.sis.openbis.generic.server.asapi.v3 import ApplicationServerApi\nfrom ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider\nfrom ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode\nfrom ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions\nfrom ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl import MasterDataRegistrationHelper\nimport sys\n\nhelper = MasterDataRegistrationHelper(sys.path)\napi = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)\nsessionToken = api.loginAsSystem()\nprops = CustomASServiceExecutionOptions().withParameter('xls', helper.listXlsByteArrays())\\\n    .withParameter('xls_name', 'SEARCH-STORE').withParameter('update_mode', 'IGNORE_EXISTING')\\\n    .withParameter('scripts', helper.getAllScripts())\nresult = api.executeCustomASService(sessionToken, CustomASServiceCode("xls-import-api"), props);\nprint("======================== master-data xls ingestion result ========================")\nprint(result)\nprint("======================== master-data xls ingestion result ========================")\n
\.


--
-- Data for Name: data_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_all (id, code, data_set_kind, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, is_valid, modification_timestamp, access_timestamp, is_derived, del_id, orig_del, pers_id_modifier, version, frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, expe_frozen, samp_frozen) FROM stdin;
\.


--
-- Data for Name: data_set_copies_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_copies_history (id, cc_id, data_id, external_code, path, git_commit_hash, git_repository_id, edms_id, edms_code, edms_label, edms_address, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_properties (id, ds_id, dstpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, pers_id_author, modification_timestamp, dase_frozen) FROM stdin;
\.


--
-- Data for Name: data_set_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_relationships_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_relationships_all (data_id_parent, data_id_child, relationship_id, ordinal, del_id, pers_id_author, registration_timestamp, modification_timestamp, parent_frozen, child_frozen, comp_frozen, cont_frozen) FROM stdin;
\.


--
-- Data for Name: data_set_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_relationships_history (id, main_data_id, relation_type, ordinal, expe_id, samp_id, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_set_types (id, code, description, modification_timestamp, main_ds_pattern, main_ds_path, deletion_disallow, validation_script_id) FROM stdin;
1	UNKNOWN	Unknown	2020-10-01 08:03:14.224058+02	\N	\N	f	\N
\.


--
-- Data for Name: data_store_service_data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_store_service_data_set_types (data_store_service_id, data_set_type_id) FROM stdin;
3	1
\.


--
-- Data for Name: data_store_services; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_store_services (id, key, label, kind, data_store_id, reporting_plugin_type) FROM stdin;
1	dropboxReporter	Jython dropbox monitor	QUERIES	1	AGGREGATION_TABLE_MODEL
2	dataset-uploader-api	Dataset Uploader API	QUERIES	1	AGGREGATION_TABLE_MODEL
3	path-info-db-consistency-check	Path Info DB consistency check	PROCESSING	1	\N
4	dss-monitoring-initialization	DSS Monitoring Initialization	QUERIES	1	AGGREGATION_TABLE_MODEL
\.


--
-- Data for Name: data_stores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_stores (id, uuid, code, download_url, remote_url, session_token, registration_timestamp, modification_timestamp, is_archiver_configured, data_source_definitions) FROM stdin;
1	7F69D222-BB69-48F0-A014-DB17D031C463	DSS1	https://localhost:8444	https://127.0.0.1:8444	201001080347596-80EBA5F27D02BC9CDC110463DE87476E	2020-10-01 08:03:48.005734+02	2020-10-01 08:03:48.038+02	t	code=multi-dataset-archiver-db\tdriverClassName=org.postgresql.Driver\thostPart=localhost\tsid=multi_dataset_archive_test\tusername=felmer\tpassword=\t\ncode=openbis-db\tdriverClassName=org.postgresql.Driver\tsid=openbis_prod\tusername=felmer\tpassword=\t\ncode=path-info-db\tdriverClassName=org.postgresql.Driver\tsid=pathinfo_prod\tusername=felmer\tpassword=\t\n
\.


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_types (id, code, description) FROM stdin;
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
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
178	./sql/postgresql/178/domains-178.sql	SUCCESS	2020-10-01 08:03:12.699	\\x2d2d204372656174696e6720646f6d61696e730a0a43524541544520444f4d41494e20415554484f52495a4154494f4e5f524f4c4520415320564152434841522834302920434845434b202856414c554520494e20282741444d494e272c2027504f5745525f55534552272c202755534552272c20274f42534552564552272c202745544c5f5345525645522729293b0a43524541544520444f4d41494e20424f4f4c45414e5f4348415220415320424f4f4c45414e2044454641554c542046414c53453b0a43524541544520444f4d41494e20424f4f4c45414e5f434841525f4f525f554e4b4e4f574e20415320434841522831292044454641554c542027552720434845434b202856414c554520494e20282746272c202754272c2027552729293b0a43524541544520444f4d41494e20434f4445204153205641524348415228313030293b0a43524541544520444f4d41494e20434f4c554d4e5f4c4142454c204153205641524348415228313238293b0a43524541544520444f4d41494e20444154415f53544f52455f534552564943455f4b494e4420415320564152434841522834302920434845434b202856414c554520494e20282750524f43455353494e47272c2027515545524945532729293b0a43524541544520444f4d41494e20444154415f53544f52455f534552564943455f5245504f5254494e475f504c5547494e5f5459504520415320564152434841522834302920434845434b202856414c554520494e2028275441424c455f4d4f44454c272c20274453535f4c494e4b272c20274147475245474154494f4e5f5441424c455f4d4f44454c2729293b0a43524541544520444f4d41494e204556454e545f5459504520415320564152434841522834302920434845434b202856414c554520494e20282744454c4554494f4e272c20274d4f56454d454e54272c2027465245455a494e472729293b0a43524541544520444f4d41494e2046494c452041532042595445413b0a43524541544520444f4d41494e2046494c455f4e414d45204153205641524348415228323535293b0a43524541544520444f4d41494e20544558545f56414c554520415320544558543b0a43524541544520444f4d41494e204f424a4543545f4e414d452041532056415243484152283530293b0a43524541544520444f4d41494e205245414c5f56414c5545204153205245414c3b0a43524541544520444f4d41494e20544543485f494420415320424947494e543b0a43524541544520444f4d41494e2054494d455f5354414d502041532054494d455354414d5020574954482054494d45205a4f4e453b0a43524541544520444f4d41494e2054494d455f5354414d505f44464c2041532054494d455354414d5020574954482054494d45205a4f4e45204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d503b0a43524541544520444f4d41494e20555345525f49442041532056415243484152283530293b0a43524541544520444f4d41494e205449544c455f313030204153205641524348415228313030293b0a43524541544520444f4d41494e20475249445f45585052455353494f4e20415320564152434841522832303030293b0a43524541544520444f4d41494e20475249445f4944204153205641524348415228323030293b0a43524541544520444f4d41494e204f5244494e414c5f494e5420415320424947494e5420434845434b202856414c5545203e2030293b0a43524541544520444f4d41494e204445534352495054494f4e5f3230303020415320564152434841522832303030293b0a43524541544520444f4d41494e20415243484956494e475f5354415455532041532056415243484152283130302920434845434b202856414c554520494e2028274c4f434b4544272c2027415641494c41424c45272c20274152434849564544272c2027415243484956455f50454e44494e47272c2027554e415243484956455f50454e44494e47272c20274241434b55505f50454e44494e472729293b0a43524541544520444f4d41494e2051554552595f5459504520415320564152434841522834302920434845434b202856414c554520494e20282747454e45524943272c20274558504552494d454e54272c202753414d504c45272c2027444154415f534554272c20274d4154455249414c2729293b0a43524541544520444f4d41494e20454e544954595f4b494e4420415320564152434841522834302920434845434b202856414c554520494e20282753414d504c45272c20274558504552494d454e54272c2027444154415f534554272c20274d4154455249414c2729293b0a43524541544520444f4d41494e205343524950545f5459504520415320564152434841522834302920434845434b202856414c554520494e20282744594e414d49435f50524f5045525459272c20274d414e414745445f50524f5045525459272c2027454e544954595f56414c49444154494f4e2729293b0a43524541544520444f4d41494e204944454e544946494552204153205641524348415228323030293b0a43524541544520444f4d41494e20444154415f5345545f4b494e4420415320564152434841522834302920434845434b202856414c554520494e202827504859534943414c272c20274c494e4b272c2027434f4e5441494e45522729293b0a43524541544520444f4d41494e20504c5547494e5f5459504520415320564152434841522834302920434845434b202856414c554520494e2028274a5954484f4e272c20275052454445504c4f5945442729293b0a43524541544520444f4d41494e204f5045524154494f4e5f455845435554494f4e5f535441544520415320564152434841522834302920434845434b202856414c554520494e2028274e4557272c20275343484544554c4544272c202752554e4e494e47272c202746494e4953484544272c20274641494c45442729293b0a43524541544520444f4d41494e204f5045524154494f4e5f455845435554494f4e5f415641494c4142494c49545920415320564152434841522834302920434845434b202856414c554520494e202827415641494c41424c45272c2744454c4554455f50454e44494e47272c2744454c45544544272c2754494d455f4f55545f50454e44494e47272c2754494d45445f4f55542729293b0a43524541544520444f4d41494e2045444d535f414444524553535f54595045204153205445585420434845434b202856414c554520494e2028274f50454e424953272c202755524c272c202746494c455f53595354454d2729293b0a43524541544520444f4d41494e204c4f434154494f4e5f54595045204153205445585420434845434b202856414c554520494e2028274f50454e424953272c202755524c272c202746494c455f53595354454d5f504c41494e272c202746494c455f53595354454d5f4749542729293b0a	\N
178	./sql/generic/178/schema-178.sql	SUCCESS	2020-10-01 08:03:13.834	\\x2d2d204372656174696e67207461626c65730a0a435245415445205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f494e5445524e414c5f4e414d45535041434520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2049535f43484f53454e5f46524f4d5f4c49535420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420545255452c20534f555243455f555249204348415241435445522056415259494e472832353029293b0a435245415445205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532028494420544543485f4944204e4f54204e554c4c2c434f4445204f424a4543545f4e414d45204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c434f564f5f494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c4c4142454c20434f4c554d4e5f4c4142454c2c204445534352495054494f4e204445534352495054494f4e5f323030302c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2049535f4f4646494349414c20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420275427293b0a435245415445205441424c4520444154415f414c4c2028494420544543485f4944204e4f54204e554c4c2c434f444520434f44452c444154415f5345545f4b494e4420444154415f5345545f4b494e442044454641554c542027504859534943414c27204e4f54204e554c4c2c445354595f494420544543485f4944204e4f54204e554c4c2c444153545f494420544543485f4944204e4f54204e554c4c2c455850455f494420544543485f49442c444154415f50524f44554345525f434f444520434f44452c50524f44554354494f4e5f54494d455354414d502054494d455f5354414d502c53414d505f494420544543485f49442c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f49442c49535f56414c494420424f4f4c45414e5f434841522044454641554c54202754272c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c204143434553535f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c2049535f4445524956454420424f4f4c45414e5f43484152204e4f54204e554c4c2c2044454c5f494420544543485f49442c204f5249475f44454c20544543485f49442c20504552535f49445f4d4f44494649455220544543485f49442c2056455253494f4e20494e54454745522044454641554c5420302c2046524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f4348494c4452454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f504152454e545320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f434f4d505320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f434f4e545320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20455850455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2053414d505f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2028444154415f49445f504152454e5420544543485f4944204e4f54204e554c4c2c444154415f49445f4348494c4420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e534849505f494420544543485f4944204e4f54204e554c4c2c204f5244494e414c20494e54454745522c2044454c5f494420544543485f49442c20504552535f49445f415554484f5220544543485f49442c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20504152454e545f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c204348494c445f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20434f4d505f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20434f4e545f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c4520444154415f53544f5245532028494420544543485f4944204e4f54204e554c4c2c5555494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c444f574e4c4f41445f55524c2056415243484152283130323429204e4f54204e554c4c2c52454d4f54455f55524c20564152434841522832353029204e4f54204e554c4c2c53455353494f4e5f544f4b454e205641524348415228353029204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c4d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2049535f41524348495645525f434f4e4649475552454420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20444154415f534f555243455f444546494e4954494f4e5320544558545f56414c5545293b0a435245415445205441424c4520444154415f53544f52455f5345525649434553202028494420544543485f4944204e4f54204e554c4c2c204b455920564152434841522832353629204e4f54204e554c4c2c204c4142454c20564152434841522832353629204e4f54204e554c4c2c204b494e4420444154415f53544f52455f534552564943455f4b494e44204e4f54204e554c4c2c20444154415f53544f52455f494420544543485f4944204e4f54204e554c4c2c205245504f5254494e475f504c5547494e5f5459504520444154415f53544f52455f534552564943455f5245504f5254494e475f504c5547494e5f54595045293b0a435245415445205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532028444154415f53544f52455f534552564943455f494420544543485f4944204e4f54204e554c4c2c20444154415f5345545f545950455f494420544543485f4944204e4f54204e554c4c293b0a435245415445205441424c4520444154415f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030204e4f54204e554c4c293b0a435245415445205441424c45204556454e54532028494420544543485f4944204e4f54204e554c4c2c4556454e545f54595045204556454e545f54595045204e4f54204e554c4c2c4445534352495054494f4e20544558545f56414c55452c524541534f4e204445534352495054494f4e5f323030302c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20656e746974795f74797065205641524348415228383029204e4f54204e554c4c2c206964656e7469666965727320544558545f56414c5545204e4f54204e554c4c2c20434f4e54454e5420544558545f56414c55452c20455841435f494420544543485f4944293b0a435245415445205441424c45204558504552494d454e54535f414c4c2028494420544543485f4944204e4f54204e554c4c2c5045524d5f494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c455854595f494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2050524f4a5f494420544543485f4944204e4f54204e554c4c2c44454c5f494420544543485f49442c204f5249475f44454c20544543485f49442c2049535f5055424c494320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20504552535f49445f4d4f44494649455220544543485f49442c2056455253494f4e20494e54454745522044454641554c5420302c2046524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f53414d5020424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f4441544120424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2050524f4a5f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c45204154544143484d454e54532028494420544543485f4944204e4f54204e554c4c2c455850455f494420544543485f49442c53414d505f494420544543485f49442c50524f4a5f494420544543485f49442c455841435f494420544543485f4944204e4f54204e554c4c2c46494c455f4e414d452046494c455f4e414d45204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c56455253494f4e20494e5445474552204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c207469746c65205449544c455f3130302c206465736372697074696f6e204445534352495054494f4e5f323030302c2050524f4a5f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20455850455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2053414d505f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c45204154544143484d454e545f434f4e54454e54532028494420544543485f4944204e4f54204e554c4c2c56414c55452046494c45204e4f54204e554c4c293b0a435245415445205441424c45204558504552494d454e545f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c455850455f494420544543485f4944204e4f54204e554c4c2c455450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c435654455f494420544543485f49442c204d4154455f50524f505f494420544543485f49442c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20455850455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c20455850455f494420544543485f4944204e4f54204e554c4c2c20455450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204558504552494d454e545f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2056414c49444154494f4e5f5343524950545f494420544543485f4944293b0a435245415445205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c455854595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c49535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452045585445524e414c5f444154412028494420544543485f4944204e4f54204e554c4c2c53484152455f494420434f44452c53495a45204f5244494e414c5f494e542c4c4f434154494f4e2056415243484152283130323429204e4f54204e554c4c2c464654595f494420544543485f4944204e4f54204e554c4c2c4c4f54595f494420544543485f4944204e4f54204e554c4c2c435654455f49445f53544f525f464d5420544543485f4944204e4f54204e554c4c2c49535f434f4d504c45544520424f4f4c45414e5f434841525f4f525f554e4b4e4f574e204e4f54204e554c4c2044454641554c54202755272c435654455f49445f53544f524520544543485f49442c2053544154555320415243484956494e475f535441545553204e4f54204e554c4c2044454641554c542027415641494c41424c45272c2050524553454e545f494e5f4152434849564520424f4f4c45414e5f434841522044454641554c54202746272c2053504545445f48494e5420494e5445474552204e4f54204e554c4c2044454641554c54202d35302c2053544f524147455f434f4e4649524d4154494f4e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2048355f464f4c4445525320424f4f4c45414e5f43484152204e4f54204e554c4c2c20483541525f464f4c4445525320424f4f4c45414e5f43484152204e4f54204e554c4c2c20415243484956494e475f52455155455354454420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452046494c455f464f524d41545f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030293b0a435245415445205441424c4520475249445f435553544f4d5f434f4c554d4e532028494420544543485f4944204e4f54204e554c4c2c20434f444520564152434841522832303029204e4f54204e554c4c2c204c4142454c20636f6c756d6e5f6c6162656c204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2045585052455353494f4e20475249445f45585052455353494f4e204e4f54204e554c4c2c2049535f5055424c494320424f4f4c45414e204e4f54204e554c4c2c20475249445f494420475249445f4944204e4f54204e554c4c293b0a435245415445205441424c45205350414345532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c2046524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f50524f4a20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f53414d5020424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452044454c4554494f4e532028494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c524541534f4e204445534352495054494f4e5f32303030204e4f54204e554c4c293b0a435245415445205441424c45204c4f4341544f525f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030293b0a435245415445205441424c45204d4154455249414c532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4d4154595f494420544543485f4944204e4f54204e554c4c2c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204d4154455249414c5f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c4d4154455f494420544543485f4944204e4f54204e554c4c2c4d5450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c435654455f494420544543485f49442c204d4154455f50524f505f494420544543485f4944293b0a435245415445205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d4154455f494420544543485f4944204e4f54204e554c4c2c204d5450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204d4154455249414c5f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2056414c49444154494f4e5f5343524950545f494420544543485f4944293b0a435245415445205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c4d4154595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c49535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c4520444154415f5345545f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c204d41494e5f44535f5041545445524e205641524348415228333030292c204d41494e5f44535f5041544820564152434841522831303030292c2044454c4554494f4e5f444953414c4c4f5720424f4f4c45414e5f434841522044454641554c54202746272c2056414c49444154494f4e5f5343524950545f494420544543485f4944293b0a435245415445205441424c4520504552534f4e532028494420544543485f4944204e4f54204e554c4c2c46495253545f4e414d452056415243484152283330292c4c4153545f4e414d452056415243484152283330292c555345525f494420555345525f4944204e4f54204e554c4c2c454d41494c204f424a4543545f4e414d452c53504143455f494420544543485f49442c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f49442c20444953504c41595f53455454494e47532046494c452c2049535f41435449564520424f4f4c45414e2044454641554c542054525545293b0a435245415445205441424c452050524f4a454354532028494420544543485f4944204e4f54204e554c4c2c5045524d5f494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c53504143455f494420544543485f4944204e4f54204e554c4c2c504552535f49445f4c454144455220544543485f49442c4445534352495054494f4e20544558545f56414c55452c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f4d4f44494649455220544543485f49442c2056455253494f4e20494e54454745522044454641554c5420302c2046524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f45585020424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f53414d5020424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2053504143455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452050524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f32303030204e4f54204e554c4c2c4c4142454c20434f4c554d4e5f4c4142454c204e4f54204e554c4c2c444154595f494420544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c434f564f5f494420544543485f49442c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f494e5445524e414c5f4e414d45535041434520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c204d4154595f50524f505f494420544543485f49442c20534348454d4120544558545f56414c55452c205452414e53464f524d4154494f4e20544558545f56414c55452c204d4554415f44415441204a534f4e42293b0a435245415445205441424c4520524f4c455f41535349474e4d454e54532028494420544543485f4944204e4f54204e554c4c2c524f4c455f434f444520415554484f52495a4154494f4e5f524f4c45204e4f54204e554c4c2c53504143455f494420544543485f49442c2050524f4a4543545f494420544543485f49442c20504552535f49445f4752414e54454520544543485f49442c2041475f49445f4752414e54454520544543485f49442c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c452053414d504c45535f414c4c2028494420544543485f4944204e4f54204e554c4c2c5045524d5f494420434f4445204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c20455850455f494420544543485f49442c534154595f494420544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c4d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c44454c5f494420544543485f49442c204f5249475f44454c20544543485f49442c2053504143455f494420544543485f49442c2053414d505f49445f504152545f4f4620544543485f49442c20504552535f49445f4d4f44494649455220544543485f49442c20636f64655f756e697175655f636865636b206368617261637465722076617279696e6728333030292c20737562636f64655f756e697175655f636865636b206368617261637465722076617279696e6728333030292c2056455253494f4e20494e54454745522044454641554c5420302c2050524f4a5f494420544543485f49442c2046524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f434f4d5020424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f4348494c4452454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f504152454e545320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2046524f5a454e5f464f525f4441544120424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2053504143455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2050524f4a5f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20455850455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20434f4e545f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452053414d504c455f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c53414d505f494420544543485f4944204e4f54204e554c4c2c535450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c435654455f494420544543485f49442c4d4154455f50524f505f494420544543485f49442c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2053414d505f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452053414d504c455f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c2053414d505f494420544543485f4944204e4f54204e554c4c2c20535450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c452053414d504c455f54595045532028494420544543485f4944204e4f54204e554c4c2c434f444520434f4445204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c2049535f4c49535441424c4520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c2047454e4552415445445f46524f4d5f444550544820494e5445474552204e4f54204e554c4c2044454641554c5420302c20504152545f4f465f444550544820494e5445474552204e4f54204e554c4c2044454641554c5420302c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2069735f6175746f5f67656e6572617465645f636f646520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2067656e6572617465645f636f64655f70726566697820434f4445204e4f54204e554c4c2044454641554c54202753272c2069735f737562636f64655f756e6971756520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c20494e48455249545f50524f5045525449455320424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2056414c49444154494f4e5f5343524950545f494420544543485f49442c2053484f575f504152454e545f4d4554414441544120424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452053414d504c455f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c534154595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c2049535f444953504c4159454420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c49535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a0a435245415445205441424c4520444154415f5345545f50524f504552544945532028494420544543485f4944204e4f54204e554c4c2c44535f494420544543485f4944204e4f54204e554c4c2c44535450545f494420544543485f4944204e4f54204e554c4c2c56414c554520544558545f56414c55452c435654455f494420544543485f49442c204d4154455f50524f505f494420544543485f49442c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20444153455f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c4520444154415f5345545f50524f504552544945535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c2044535f494420544543485f4944204e4f54204e554c4c2c2044535450545f494420544543485f4944204e4f54204e554c4c2c2056414c554520544558545f56414c55452c20564f434142554c4152595f5445524d204944454e5449464945522c204d4154455249414c204944454e5449464945522c20504552535f49445f415554484f5220544543485f4944204e4f54204e554c4c2c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532028494420544543485f4944204e4f54204e554c4c2c445354595f494420544543485f4944204e4f54204e554c4c2c505254595f494420544543485f4944204e4f54204e554c4c2c49535f4d414e4441544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c49535f4d414e414745445f494e5445524e414c4c5920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204f5244494e414c204f5244494e414c5f494e54204e4f54204e554c4c2c2053454354494f4e204445534352495054494f4e5f323030302c5343524950545f494420544543485f49442c2049535f53484f574e5f4544495420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202754272c53484f575f5241575f56414c554520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a0a435245415445205441424c4520415554484f52495a4154494f4e5f47524f5550532028494420544543485f4944204e4f54204e554c4c2c20434f444520434f4445204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e53202841475f494420544543485f4944204e4f54204e554c4c2c20504552535f494420544543485f4944204e4f54204e554c4c293b0a0a435245415445205441424c452046494c544552532028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2045585052455353494f4e2054455854204e4f54204e554c4c2c2049535f5055424c494320424f4f4c45414e204e4f54204e554c4c2c20475249445f494420564152434841522832303029204e4f54204e554c4c293b0a435245415445205441424c4520515545524945532028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c2045585052455353494f4e2054455854204e4f54204e554c4c2c2049535f5055424c494320424f4f4c45414e204e4f54204e554c4c2c2051554552595f545950452051554552595f54595045204e4f54204e554c4c2c20454e544954595f545950455f434f444520434f44452c2044425f4b455920434f4445204e4f54204e554c4c2044454641554c5420273127293b0a0a435245415445205441424c452072656c6174696f6e736869705f74797065732028696420544543485f4944204e4f54204e554c4c2c20636f646520434f4445204e4f54204e554c4c2c206c6162656c20434f4c554d4e5f4c4142454c2c20706172656e745f6c6162656c20434f4c554d4e5f4c4142454c2c206368696c645f6c6162656c20434f4c554d4e5f4c4142454c2c206465736372697074696f6e204445534352495054494f4e5f323030302c20726567697374726174696f6e5f74696d657374616d702054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20706572735f69645f7265676973746572657220544543485f4944204e4f54204e554c4c2c2069735f6d616e616765645f696e7465726e616c6c7920424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c2069735f696e7465726e616c5f6e616d65737061636520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2028696420544543485f4944204e4f54204e554c4c2c2073616d706c655f69645f706172656e7420544543485f4944204e4f54204e554c4c2c2072656c6174696f6e736869705f696420544543485f4944204e4f54204e554c4c2c2073616d706c655f69645f6368696c6420544543485f4944204e4f54204e554c4c2c2064656c5f696420544543485f49442c20504552535f49445f415554484f5220544543485f49442c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d4f44494649434154494f4e5f54494d455354414d502054494d455f5354414d502044454641554c542043555252454e545f54494d455354414d502c20504152454e545f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c204348494c445f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a0a435245415445205441424c4520736372697074732028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c205343524950545f54595045205343524950545f54595045204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c53435249505420544558545f56414c55452c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c2c454e544954595f4b494e4420454e544954595f4b494e442c20504c5547494e5f5459504520504c5547494e5f54595045204e4f54204e554c4c2044454641554c5420274a5954484f4e272c2049535f415641494c41424c4520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c542054525545293b0a0a435245415445205441424c4520434f52455f504c5547494e532028494420544543485f4944204e4f54204e554c4c2c204e414d4520564152434841522832303029204e4f54204e554c4c2c2056455253494f4e20494e5445474552204e4f54204e554c4c2c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c204d41535445525f5245475f53435249505420544558545f56414c5545293b0a0a435245415445205441424c4520504f53545f524547495354524154494f4e5f444154415345545f51554555452028494420544543485f4944204e4f54204e554c4c2c2044535f494420544543485f4944204e4f54204e554c4c293b0a0a435245415445205441424c4520454e544954595f4f5045524154494f4e535f4c4f472028494420544543485f4944204e4f54204e554c4c2c20524547495354524154494f4e5f494420544543485f4944204e4f54204e554c4c293b0a0a435245415445205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f50524f4a5f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c20455850455f494420544543485f49442c2053504143455f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d50293b0a435245415445205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f455850455f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502c2050524f4a5f494420544543485f4944293b0a435245415445205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f53414d505f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c20455850455f494420544543485f49442c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d502c2053504143455f494420544543485f49442c2050524f4a5f494420544543485f4944293b0a435245415445205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592028494420544543485f4944204e4f54204e554c4c2c204d41494e5f444154415f494420544543485f4944204e4f54204e554c4c2c2052454c4154494f4e5f5459504520544558545f56414c55452c204f5244494e414c20494e54454745522c20455850455f494420544543485f49442c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c20454e544954595f5045524d5f494420544558545f56414c55452c20504552535f49445f415554484f5220544543485f49442c2056414c49445f46524f4d5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c2c2056414c49445f554e54494c5f54494d455354414d502054494d455f5354414d50293b0a0a435245415445205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d532028494420544543485f49442c20434f444520434f4445204e4f54204e554c4c2c204c4142454c20544558545f56414c55452c204144445245535320544558545f56414c5545204e4f54204e554c4c2c20414444524553535f545950452045444d535f414444524553535f54595045204e4f54204e554c4c293b0a435245415445205441424c45204c494e4b5f4441544128494420544543485f4944204e4f54204e554c4c2c20444154415f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a435245415445205441424c4520434f4e54454e545f434f504945532028494420544543485f4944204e4f54204e554c4c2c204c4f434154494f4e5f54595045204c4f434154494f4e5f54595045204e4f54204e554c4c2c20444154415f494420544543485f4944204e4f54204e554c4c2c2045444d535f494420544543485f4944204e4f54204e554c4c2c2045585445524e414c5f434f444520544558545f56414c55452c205041544820544558545f56414c55452c204749545f434f4d4d49545f4841534820544558545f56414c55452c204749545f5245504f5349544f52595f494420544558545f56414c55452c204c4f434154494f4e5f554e495155455f434845434b20544558545f56414c5545204e4f54204e554c4c2c20504552535f49445f5245474953544552455220544543485f49442c20524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c20444154415f46524f5a454e20424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420274627293b0a0a435245415445205441424c45204d45544150524f4a454354532028494420544543485f4944204e4f54204e554c4c2c204e414d4520434f4445204e4f54204e554c4c2c204445534352495054494f4e204445534352495054494f4e5f323030302c204f574e455220544543485f4944204e4f54204e554c4c2c205052495641544520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c5420545255452c204352454154494f4e5f444154452054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d50293b0a435245415445205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2028494420544543485f4944204e4f54204e554c4c2c204d4550525f494420544543485f4944204e4f54204e554c4c2c20455850455f494420544543485f49442c2053414d505f494420544543485f49442c20444154415f494420544543485f49442c204d4154455f494420544543485f49442c2044454c5f494420544543485f49442c204352454154494f4e5f44415445202054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d50293b0a0a435245415445205441424c45204f5045524154494f4e5f455845435554494f4e5320280a09494420544543485f4944204e4f54204e554c4c2c200a09434f444520434f4445204e4f54204e554c4c2c0a095354415445204f5045524154494f4e5f455845435554494f4e5f5354415445204e4f54204e554c4c2044454641554c5420274e4557272c0a094f574e455220544543485f4944204e4f54204e554c4c2c0a094445534352495054494f4e20544558545f56414c55452c0a094e4f54494649434154494f4e20544558545f56414c55452c0a09415641494c4142494c495459204f5045524154494f4e5f455845435554494f4e5f415641494c4142494c495459204e4f54204e554c4c2044454641554c542027415641494c41424c45272c0a09415641494c4142494c4954595f54494d4520424947494e54204e4f54204e554c4c2044454641554c5420312c0a0953554d4d4152595f4f5045524154494f4e5320544558545f56414c55452c0a0953554d4d4152595f50524f475245535320544558545f56414c55452c0a0953554d4d4152595f4552524f5220544558545f56414c55452c0a0953554d4d4152595f524553554c545320544558545f56414c55452c0a0953554d4d4152595f415641494c4142494c495459204f5045524154494f4e5f455845435554494f4e5f415641494c4142494c495459204e4f54204e554c4c2044454641554c542027415641494c41424c45272c0a0953554d4d4152595f415641494c4142494c4954595f54494d4520424947494e54204e4f54204e554c4c2044454641554c5420312c0a0944455441494c535f5041544820564152434841522831303030292c0a0944455441494c535f415641494c4142494c495459204f5045524154494f4e5f455845435554494f4e5f415641494c4142494c495459204e4f54204e554c4c2044454641554c542027415641494c41424c45272c0a0944455441494c535f415641494c4142494c4954595f54494d4520424947494e54204e4f54204e554c4c2044454641554c5420312c0a094352454154494f4e5f444154452054494d455f5354414d505f44464c204e4f54204e554c4c2c200a0953544152545f444154452054494d455f5354414d502c200a0946494e4953485f444154452054494d455f5354414d500a293b0a0a435245415445205441424c4520646174615f7365745f636f706965735f686973746f727920280a2020696420544543485f4944204e4f54204e554c4c2c0a202063635f696420544543485f4944204e4f54204e554c4c2c0a2020646174615f696420544543485f4944204e4f54204e554c4c2c0a202065787465726e616c5f636f646520544558545f56414c55452c0a20207061746820544558545f56414c55452c0a20206769745f636f6d6d69745f6861736820544558545f56414c55452c200a20206769745f7265706f7369746f72795f696420544558545f56414c55452c200a202065646d735f696420544543485f4944204e4f54204e554c4c2c0a202065646d735f636f646520434f44452c0a202065646d735f6c6162656c20544558545f56414c55452c0a202065646d735f6164647265737320544558545f56414c55452c0a2020706572735f69645f617574686f7220544543485f49442c0a202076616c69645f66726f6d5f74696d657374616d702054494d455f5354414d50204e4f54204e554c4c2c200a202076616c69645f756e74696c5f74696d657374616d702054494d455f5354414d50293b0a0a435245415445205441424c452053454d414e5449435f414e4e4f544154494f4e532028494420544543485f4944204e4f54204e554c4c2c0a095045524d5f494420434f4445204e4f54204e554c4c2c0a09534154595f494420544543485f49442c200a09535450545f494420544543485f49442c0a09505254595f494420544543485f49442c0a095052454449434154455f4f4e544f4c4f47595f494420544558542c0a095052454449434154455f4f4e544f4c4f47595f56455253494f4e20544558542c0a095052454449434154455f414343455353494f4e5f494420544558542c0a0944455343524950544f525f4f4e544f4c4f47595f494420544558542c0a0944455343524950544f525f4f4e544f4c4f47595f56455253494f4e20544558542c0a0944455343524950544f525f414343455353494f4e5f494420544558542c0a094352454154494f4e5f444154452074696d655f7374616d705f64666c204e4f54204e554c4c0a09293b0a20200a2d2d204372656174696e67207669657773202d20636f706965642066726f6d20736368656d612067656e65726174656420666f722074657374732c20272a272063616e277420626520757365642062656361757365206f66205067446966665669657773206c696d69746174696f6e20696e207669657720636f6d70617269736f6e0a0a435245415445205649455720646174612041530a202020202053454c4543542069642c20636f64652c20647374795f69642c20646173745f69642c20657870655f69642c20657870655f66726f7a656e2c20646174615f70726f64756365725f636f64652c2070726f64756374696f6e5f74696d657374616d702c2073616d705f69642c2073616d705f66726f7a656e2c200a202020202020202020202020726567697374726174696f6e5f74696d657374616d702c206163636573735f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2069735f76616c69642c206d6f64696669636174696f6e5f74696d657374616d702c200a20202020202020202020202069735f646572697665642c2064656c5f69642c206f7269675f64656c2c2076657273696f6e2c20646174615f7365745f6b696e642c200a20202020202020202020202066726f7a656e2c2066726f7a656e5f666f725f6368696c6472656e2c2066726f7a656e5f666f725f706172656e74732c2066726f7a656e5f666f725f636f6d70732c2066726f7a656e5f666f725f636f6e74730a2020202020202046524f4d20646174615f616c6c200a20202020202057484552452064656c5f6964204953204e554c4c3b0a0a435245415445205649455720646174615f64656c657465642041530a202020202053454c4543542069642c20636f64652c20647374795f69642c20646173745f69642c20657870655f69642c20646174615f70726f64756365725f636f64652c2070726f64756374696f6e5f74696d657374616d702c2073616d705f69642c20726567697374726174696f6e5f74696d657374616d702c206163636573735f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2069735f76616c69642c206d6f64696669636174696f6e5f74696d657374616d702c2069735f646572697665642c2064656c5f69642c206f7269675f64656c2c2076657273696f6e2c20646174615f7365745f6b696e64200a2020202020202046524f4d20646174615f616c6c200a20202020202057484552452064656c5f6964204953204e4f54204e554c4c3b0a0a4352454154452056494557206578706572696d656e74732041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c20657874795f69642c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c200a20202020202020202020202070726f6a5f69642c2070726f6a5f66726f7a656e2c2064656c5f69642c206f7269675f64656c2c2069735f7075626c69632c2076657273696f6e2c2066726f7a656e2c2066726f7a656e5f666f725f73616d702c2066726f7a656e5f666f725f64617461200a2020202020202046524f4d206578706572696d656e74735f616c6c200a20202020202057484552452064656c5f6964204953204e554c4c3b0a0a4352454154452056494557206578706572696d656e74735f64656c657465642041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c20657874795f69642c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c2070726f6a5f69642c2064656c5f69642c206f7269675f64656c2c2069735f7075626c69632c2076657273696f6e200a2020202020202046524f4d206578706572696d656e74735f616c6c200a20202020202057484552452064656c5f6964204953204e4f54204e554c4c3b0a0a43524541544520564945572073616d706c65732041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c2070726f6a5f69642c2070726f6a5f66726f7a656e2c20657870655f69642c20657870655f66726f7a656e2c20736174795f69642c20726567697374726174696f6e5f74696d657374616d702c200a2020202020202020202020206d6f64696669636174696f6e5f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2064656c5f69642c206f7269675f64656c2c2073706163655f69642c2073706163655f66726f7a656e2c200a20202020202020202020202073616d705f69645f706172745f6f662c20636f6e745f66726f7a656e2c2076657273696f6e2c2066726f7a656e2c2066726f7a656e5f666f725f636f6d702c2066726f7a656e5f666f725f6368696c6472656e2c2066726f7a656e5f666f725f706172656e74732c2066726f7a656e5f666f725f646174610a2020202020202046524f4d2073616d706c65735f616c6c200a20202020202057484552452064656c5f6964204953204e554c4c3b0a0a43524541544520564945572073616d706c65735f64656c657465642041530a202020202053454c4543542069642c207065726d5f69642c20636f64652c20657870655f69642c20736174795f69642c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d702c20706572735f69645f726567697374657265722c20706572735f69645f6d6f6469666965722c2064656c5f69642c206f7269675f64656c2c2073706163655f69642c2070726f6a5f69642c2073616d705f69645f706172745f6f662c2076657273696f6e200a2020202020202046524f4d2073616d706c65735f616c6c200a20202020202057484552452064656c5f6964204953204e4f54204e554c4c3b0a0a435245415445205649455720646174615f7365745f72656c6174696f6e73686970732041530a20202053454c45435420646174615f69645f706172656e742c20706172656e745f66726f7a656e2c20636f6e745f66726f7a656e2c20646174615f69645f6368696c642c206368696c645f66726f7a656e2c20636f6d705f66726f7a656e2c200a2020202020202020202072656c6174696f6e736869705f69642c206f7264696e616c2c2064656c5f69642c20706572735f69645f617574686f722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d700a20202046524f4d20646174615f7365745f72656c6174696f6e73686970735f616c6c200a20202057484552452064656c5f6964204953204e554c4c3b0a2020200a43524541544520564945572073616d706c655f72656c6174696f6e73686970732041530a20202053454c4543542069642c2073616d706c655f69645f706172656e742c20706172656e745f66726f7a656e2c2072656c6174696f6e736869705f69642c2073616d706c655f69645f6368696c642c206368696c645f66726f7a656e2c2064656c5f69642c20706572735f69645f617574686f722c20726567697374726174696f6e5f74696d657374616d702c206d6f64696669636174696f6e5f74696d657374616d700a20202046524f4d2073616d706c655f72656c6174696f6e73686970735f616c6c0a20202057484552452064656c5f6964204953204e554c4c3b0a2020202020200a4352454154452056494557204d45544150524f4a4543545f41535349474e4d454e54532041530a20202053454c4543542049442c204d4550525f49442c20455850455f49442c2053414d505f49442c20444154415f49442c204d4154455f49442c2044454c5f49442c204352454154494f4e5f444154450a20202046524f4d204d45544150524f4a4543545f41535349474e4d454e54535f414c4c200a20202057484552452044454c5f4944204953204e554c4c3b0a2020200a43524541544520564945572073616d706c655f686973746f72795f7669657720415320280a202053454c4543540a20202020322a69642061732069642c0a202020206d61696e5f73616d705f69642c0a2020202072656c6174696f6e5f747970652c0a2020202073706163655f69642c0a20202020657870655f69642c0a2020202073616d705f69642c0a2020202070726f6a5f69642c0a20202020646174615f69642c0a20202020656e746974795f7065726d5f69642c0a202020206e756c6c20617320737470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a2020202053414d504c455f52454c4154494f4e53484950535f484953544f52590a202057484552450a2020202076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c290a554e494f4e0a202053454c4543540a20202020322a69642b312061732069642c0a2020202073616d705f6964206173206d61696e5f73616d705f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c2061732073706163655f69642c0a202020206e756c6c20617320657870655f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c2061732070726f6a5f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a20202020737470745f69642c0a2020202076616c75652c0a20202020766f636162756c6172795f7465726d2c0a202020206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a2020202053414d504c455f50524f504552544945535f484953544f52593b0a202020200a435245415445205649455720646174615f7365745f686973746f72795f7669657720415320280a202053454c4543540a20202020332a69642061732069642c0a202020206d61696e5f646174615f69642c0a2020202072656c6174696f6e5f747970652c0a202020206f7264696e616c2c0a20202020657870655f69642c0a2020202073616d705f69642c0a20202020646174615f69642c0a20202020656e746974795f7065726d5f69642c0a202020206e756c6c2061732064737470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a202020206e756c6c2061732065787465726e616c5f636f64652c0a202020206e756c6c20617320706174682c0a202020206e756c6c206173206769745f636f6d6d69745f686173682c0a202020206e756c6c206173206769745f7265706f7369746f72795f69642c0a202020206e756c6c3a3a544543485f49442061732065646d735f69642c0a202020206e756c6c2061732065646d735f636f64652c0a202020206e756c6c2061732065646d735f6c6162656c2c0a202020206e756c6c2061732065646d735f616464726573732c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a20202020646174615f7365745f72656c6174696f6e73686970735f686973746f72790a202057484552450a2020202076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c290a554e494f4e0a202053454c4543540a20202020332a69642b312061732069642c0a2020202064735f6964206173206d61696e5f646174615f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c206173206f7264696e616c2c0a202020206e756c6c20617320657870655f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a2020202064737470745f69642c0a2020202076616c75652c0a20202020766f636162756c6172795f7465726d2c0a202020206d6174657269616c2c0a202020206e756c6c2061732065787465726e616c5f636f64652c0a202020206e756c6c20617320706174682c0a202020206e756c6c206173206769745f636f6d6d69745f686173682c0a202020206e756c6c206173206769745f7265706f7369746f72795f69642c0a202020206e756c6c2061732065646d735f69642c0a202020206e756c6c2061732065646d735f636f64652c0a202020206e756c6c2061732065646d735f6c6162656c2c0a202020206e756c6c2061732065646d735f616464726573732c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a20202020646174615f7365745f70726f706572746965735f686973746f72790a20554e494f4e0a20202853454c4543540a202020332a69642b322061732069642c0a20202020646174615f6964206173206d61696e5f646174615f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c206173206f7264696e616c2c0a202020206e756c6c20617320657870655f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a202020206e756c6c2061732064737470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a2020202065787465726e616c5f636f64652c0a20202020706174682c0a202020206769745f636f6d6d69745f686173682c0a202020206769745f7265706f7369746f72795f69642c0a2020202065646d735f69642c0a2020202065646d735f636f64652c0a2020202065646d735f6c6162656c2c0a2020202065646d735f616464726573732c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a20202020646174615f7365745f636f706965735f686973746f72790a202057484552450a2020202076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c293b0a20202020202020200a4352454154452056494557206578706572696d656e745f686973746f72795f7669657720415320280a202053454c4543540a20202020322a69642061732069642c0a202020206d61696e5f657870655f69642c0a2020202072656c6174696f6e5f747970652c0a2020202070726f6a5f69642c0a2020202073616d705f69642c0a20202020646174615f69642c0a20202020656e746974795f7065726d5f69642c0a202020206e756c6c20617320657470745f69642c0a202020206e756c6c2061732076616c75652c0a202020206e756c6c20617320766f636162756c6172795f7465726d2c0a202020206e756c6c206173206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a202020204558504552494d454e545f52454c4154494f4e53484950535f484953544f52590a202057484552452076616c69645f756e74696c5f74696d657374616d70204953204e4f54204e554c4c290a554e494f4e0a202053454c4543540a20202020322a69642b312061732069642c0a20202020657870655f6964206173206d61696e5f657870655f69642c0a202020206e756c6c2061732072656c6174696f6e5f747970652c0a202020206e756c6c2061732070726f6a5f69642c0a202020206e756c6c2061732073616d705f69642c0a202020206e756c6c20617320646174615f69642c0a202020206e756c6c20617320656e746974795f7065726d5f69642c0a20202020657470745f69642c0a2020202076616c75652c0a20202020766f636162756c6172795f7465726d2c0a202020206d6174657269616c2c0a20202020706572735f69645f617574686f722c0a2020202076616c69645f66726f6d5f74696d657374616d702c0a2020202076616c69645f756e74696c5f74696d657374616d700a202046524f4d0a202020204558504552494d454e545f50524f504552544945535f484953544f52593b0a0a2d2d204372656174696e672073657175656e6365730a0a4352454154452053455155454e434520434f4e54524f4c4c45445f564f434142554c4152595f49445f5345513b0a4352454154452053455155454e434520435654455f49445f5345513b0a4352454154452053455155454e43452044415441424153455f494e5354414e43455f49445f5345513b0a4352454154452053455155454e434520444154415f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f52454c4154494f4e534849505f49445f5345513b0a4352454154452053455155454e434520444154415f53544f52455f49445f5345513b0a4352454154452053455155454e434520444154415f53544f52455f53455256494345535f49445f5345513b0a4352454154452053455155454e434520444154415f545950455f49445f5345513b0a4352454154452053455155454e434520455450545f49445f5345513b0a4352454154452053455155454e4345204556454e545f49445f5345513b0a4352454154452053455155454e4345204154544143484d454e545f49445f5345513b0a4352454154452053455155454e4345204154544143484d454e545f434f4e54454e545f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f50524f50455254595f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f545950455f49445f5345513b0a4352454154452053455155454e43452046494c455f464f524d41545f545950455f49445f5345513b0a4352454154452053455155454e43452053504143455f49445f5345513b0a4352454154452053455155454e43452044454c4554494f4e5f49445f5345513b0a4352454154452053455155454e4345204c4f4341544f525f545950455f49445f5345513b0a4352454154452053455155454e4345204d4154455249414c5f49445f5345513b0a4352454154452053455155454e4345204d4154455249414c5f50524f50455254595f49445f5345513b0a4352454154452053455155454e4345204d4154455249414c5f545950455f49445f5345513b0a4352454154452053455155454e4345204d5450545f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f545950455f49445f5345513b0a4352454154452053455155454e434520504552534f4e5f49445f5345513b0a4352454154452053455155454e43452050524f4a4543545f49445f5345513b0a4352454154452053455155454e43452050524f50455254595f545950455f49445f5345513b0a4352454154452053455155454e434520524f4c455f41535349474e4d454e545f49445f5345513b0a4352454154452053455155454e43452053414d504c455f49445f5345513b0a4352454154452053455155454e43452053414d504c455f50524f50455254595f49445f5345513b0a4352454154452053455155454e43452053414d504c455f545950455f49445f5345513b0a4352454154452053455155454e434520535450545f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f50524f50455254595f49445f5345513b0a4352454154452053455155454e43452044535450545f49445f5345513b0a4352454154452053455155454e434520434f44455f5345513b0a4352454154452053455155454e4345204558504552494d454e545f434f44455f5345513b0a4352454154452053455155454e43452053414d504c455f434f44455f5345513b0a4352454154452053455155454e4345205045524d5f49445f5345513b0a4352454154452053455155454e434520415554484f52495a4154494f4e5f47524f55505f49445f5345513b0a4352454154452053455155454e43452046494c5445525f49445f5345513b0a4352454154452053455155454e434520475249445f435553544f4d5f434f4c554d4e535f49445f5345513b0a4352454154452053455155454e43452051554552595f49445f5345513b0a4352454154452053455155454e43452052454c4154494f4e534849505f545950455f49445f5345513b0a4352454154452053455155454e43452053414d504c455f52454c4154494f4e534849505f49445f5345513b0a4352454154452053455155454e4345205343524950545f49445f5345513b0a4352454154452053455155454e434520434f52455f504c5547494e5f49445f5345513b0a4352454154452053455155454e434520504f53545f524547495354524154494f4e5f444154415345545f51554555455f49445f5345513b0a4352454154452053455155454e434520454e544954595f4f5045524154494f4e535f4c4f475f49445f5345513b0a4352454154452053455155454e4345204558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452053414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452050524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d5f49445f5345513b0a4352454154452053455155454e4345204d45544150524f4a4543545f49445f5345513b0a4352454154452053455155454e4345204d45544150524f4a4543545f41535349474e4d454e545f49445f5345513b0a4352454154452053455155454e4345204f5045524154494f4e5f455845435554494f4e535f49445f5345513b0a4352454154452053455155454e434520434f4e54454e545f434f504945535f49445f5345513b0a4352454154452053455155454e434520444154415f5345545f434f504945535f484953544f52595f49445f5345513b0a4352454154452053455155454e43452053454d414e5449435f414e4e4f544154494f4e5f49445f5345513b0a0a2d2d204372656174696e67207072696d617279206b657920636f6e73747261696e74730a0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532041444420434f4e53545241494e5420434f564f5f504b205052494d415259204b4559284944293b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f53544f5245532041444420434f4e53545241494e5420444153545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f53544f52455f53455256494345532041444420434f4e53545241494e5420445353455f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f54595045532041444420434f4e53545241494e5420444154595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204556454e54532041444420434f4e53545241494e542045564e545f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f504b205052494d415259204b4559284944293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f504b205052494d415259204b4559284944293b0a414c544552205441424c45204154544143484d454e545f434f4e54454e54532041444420434f4e53545241494e5420455841435f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f54595045532041444420434f4e53545241494e5420455854595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f504b205052494d415259204b4559284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f504b205052494d415259204b4559284944293b0a414c544552205441424c452046494c455f464f524d41545f54595045532041444420434f4e53545241494e5420464654595f504b205052494d415259204b4559284944293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f504b205052494d415259204b4559284944293b0a414c544552205441424c452044454c4554494f4e532041444420434f4e53545241494e542044454c5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204c4f4341544f525f54595045532041444420434f4e53545241494e54204c4f54595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f54595045532041444420434f4e53545241494e54204d4154595f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f54595045532041444420434f4e53545241494e5420445354595f504b205052494d415259204b4559284944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f504b205052494d415259204b4559284944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504b205052494d415259204b4559284944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f504b205052494d415259204b4559284944293b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f54595045532041444420434f4e53545241494e5420534154595f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f504b205052494d415259204b4559284944293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f5550532041444420434f4e53545241494e542041475f504b205052494d415259204b4559284944293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e532041444420434f4e53545241494e54204147505f504b205052494d415259204b455928504552535f49442c41475f4944293b0a414c544552205441424c452046494c544552532041444420434f4e53545241494e542046494c545f504b205052494d415259204b4559284944293b0a414c544552205441424c4520475249445f435553544f4d5f434f4c554d4e532041444420434f4e53545241494e5420475249445f435553544f4d5f434f4c554d4e535f504b205052494d415259204b4559284944293b0a414c544552205441424c4520515545524945532041444420434f4e53545241494e5420515545525f504b205052494d415259204b4559284944293b0a414c544552205441424c452072656c6174696f6e736869705f74797065732041444420434f4e53545241494e5420726574795f706b205052494d415259204b455920286964293b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f706b205052494d415259204b455920286964293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f504b205052494d415259204b4559284944293b0a414c544552205441424c4520504f53545f524547495354524154494f4e5f444154415345545f51554555452041444420434f4e53545241494e5420505244515f504b205052494d415259204b4559284944293b0a414c544552205441424c4520454e544954595f4f5045524154494f4e535f4c4f472041444420434f4e53545241494e5420454f4c5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f504b205052494d415259204b4559284944293b0a414c544552205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d532041444420434f4e53545241494e542045444d535f504b205052494d415259204b4559284944293b0a414c544552205441424c4520434f4e54454e545f434f504945532041444420434f4e53545241494e5420434f434f5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204c494e4b5f444154412041444420434f4e53545241494e54206c6e64615f706b205052494d415259204b4559286964293b0a414c544552205441424c45204d45544150524f4a454354532041444420434f4e53545241494e54204d45544150524f4a454354535f504b205052494d415259204b4559284944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f504b205052494d415259204b4559284944293b0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f504b205052494d415259204b4559284944293b0a414c544552205441424c4520444154415f5345545f434f504945535f484953544f52592041444420434f4e53545241494e5420445343485f504b205052494d415259204b4559284944293b0a414c544552205441424c452053454d414e5449435f414e4e4f544154494f4e532041444420434f4e53545241494e542053454d414e5449435f414e4e4f544154494f4e535f504b205052494d415259204b4559284944293b0a0a2d2d204372656174696e6720756e6971756520636f6e73747261696e74730a0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532041444420434f4e53545241494e5420434f564f5f424b5f554b20554e4951554528434f44452c49535f494e5445524e414c5f4e414d455350414345293b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f424b5f554b20554e4951554528434f44452c434f564f5f4944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f494446525a5f554b20554e495155452849442c2046524f5a454e293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f494446525a5f43485f554b20554e495155452849442c2046524f5a454e5f464f525f4348494c4452454e293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f494446525a5f505f554b20554e495155452849442c2046524f5a454e5f464f525f504152454e5453293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f494446525a5f434f4d505f554b20554e495155452849442c2046524f5a454e5f464f525f434f4d5053293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f494446525a5f434f4e545f554b20554e495155452849442c2046524f5a454e5f464f525f434f4e5453293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f424b5f554b20554e4951554528444154415f49445f4348494c442c444154415f49445f504152454e542c52454c4154494f4e534849505f4944293b0a414c544552205441424c4520444154415f53544f52455f53455256494345532041444420434f4e53545241494e5420445353455f424b5f554b20554e49515545284b45592c20444154415f53544f52455f4944293b0a414c544552205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532041444420434f4e53545241494e54204453534453545f424b5f554b20554e4951554528444154415f53544f52455f534552564943455f49442c20444154415f5345545f545950455f4944293b0a414c544552205441424c4520444154415f53544f5245532041444420434f4e53545241494e5420444153545f424b5f554b20554e4951554528434f44452c55554944293b0a414c544552205441424c4520444154415f54595045532041444420434f4e53545241494e5420444154595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f494446525a5f554b20554e495155452849442c2046524f5a454e293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f494446525a5f535f554b20554e495155452849442c2046524f5a454e5f464f525f53414d50293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f494446525a5f445f554b20554e495155452849442c2046524f5a454e5f464f525f44415441293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f424b5f554b20554e4951554528434f44452c50524f4a5f4944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f50495f554b20554e49515545285045524d5f4944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f424b5f554b20554e4951554528455850455f49442c455450545f4944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f424b5f554b20554e4951554528455854595f49442c505254595f4944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f424b5f554b20554e49515545284c4f434154494f4e2c4c4f54595f4944293b0a414c544552205441424c452046494c455f464f524d41545f54595045532041444420434f4e53545241494e5420464654595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204c4f4341544f525f54595045532041444420434f4e53545241494e54204c4f54595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f424b5f554b20554e4951554528434f44452c4d4154595f4944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f424b5f554b20554e49515545284d4154455f49442c4d5450545f4944293b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f424b5f554b20554e49515545284d4154595f49442c505254595f4944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f424b5f554b20554e4951554528555345525f4944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f494446525a5f554b20554e495155452849442c2046524f5a454e293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f494446525a5f455f554b20554e495155452849442c2046524f5a454e5f464f525f455850293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f494446525a5f535f554b20554e495155452849442c2046524f5a454e5f464f525f53414d50293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f424b5f554b20554e4951554528434f44452c53504143455f4944293b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f50495f554b20554e49515545285045524d5f4944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f424b5f554b20554e4951554528434f44452c49535f494e5445524e414c5f4e414d455350414345293b0a43524541544520554e4951554520494e44455820524f41535f50455f53504143455f50524f4a4543545f424b5f554b204f4e20524f4c455f41535349474e4d454e54532028504552535f49445f4752414e5445452c20524f4c455f434f44452c20636f616c657363652853504143455f49442c2d31292c20636f616c657363652850524f4a4543545f49442c2d3129293b200a43524541544520554e4951554520494e44455820524f41535f41475f53504143455f50524f4a4543545f424b5f554b204f4e20524f4c455f41535349474e4d454e5453202841475f49445f4752414e5445452c20524f4c455f434f44452c20636f616c657363652853504143455f49442c2d31292c20636f616c657363652850524f4a4543545f49442c2d3129293b200a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f494446525a5f554b20554e495155452849442c2046524f5a454e293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f494446525a5f435f554b20554e495155452849442c2046524f5a454e5f464f525f434f4d50293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f494446525a5f43485f554b20554e495155452849442c2046524f5a454e5f464f525f4348494c4452454e293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f494446525a5f505f554b20554e495155452849442c2046524f5a454e5f464f525f504152454e5453293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f494446525a5f445f554b20554e495155452849442c2046524f5a454e5f464f525f44415441293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f50495f554b20554e49515545285045524d5f4944293b0a414c544552205441424c452073616d706c65735f616c6c2041444420434f4e53545241494e542073616d705f636f64655f756e697175655f636865636b5f756b20554e4951554528636f64655f756e697175655f636865636b293b0a414c544552205441424c452073616d706c65735f616c6c2041444420434f4e53545241494e542073616d705f737562636f64655f756e697175655f636865636b5f756b20554e4951554528737562636f64655f756e697175655f636865636b293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f424b5f554b20554e495155452853414d505f49442c535450545f4944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f424b5f554b20554e4951554528534154595f49442c505254595f4944293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f494446525a5f554b20554e495155452849442c2046524f5a454e293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f494446525a5f505f554b20554e495155452849442c2046524f5a454e5f464f525f50524f4a293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f494446525a5f535f554b20554e495155452849442c2046524f5a454e5f464f525f53414d50293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f424b5f554b20554e4951554528445354595f49442c505254595f4944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f424b5f554b20554e495155452844535f49442c44535450545f4944293b0a2d2d204e4f54453a20666f6c6c6f77696e6720756e697175656e65737320636f6e73747261696e747320666f72206174746163686d656e747320776f726b2c206265636175736520286e756c6c20213d206e756c6c2920696e20506f737467726573200a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f455850455f424b5f554b20554e4951554528455850455f49442c46494c455f4e414d452c56455253494f4e293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f50524f4a5f424b5f554b20554e495155452850524f4a5f49442c46494c455f4e414d452c56455253494f4e293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f53414d505f424b5f554b20554e495155452853414d505f49442c46494c455f4e414d452c56455253494f4e293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f5550532041444420434f4e53545241494e542041475f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c452046494c544552532041444420434f4e53545241494e542046494c545f424b5f554b20554e49515545284e414d452c20475249445f4944293b0a414c544552205441424c4520475249445f435553544f4d5f434f4c554d4e532041444420434f4e53545241494e5420475249445f435553544f4d5f434f4c554d4e535f424b5f554b20554e4951554528434f44452c20475249445f4944293b0a414c544552205441424c4520515545524945532041444420434f4e53545241494e5420515545525f424b5f554b20554e49515545284e414d45293b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f626b5f756b20554e495155452873616d706c655f69645f6368696c642c73616d706c655f69645f706172656e742c72656c6174696f6e736869705f6964293b0a414c544552205441424c452072656c6174696f6e736869705f74797065732041444420434f4e53545241494e5420726574795f756b20554e4951554528636f6465293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f554b20554e49515545284e414d45293b0a414c544552205441424c4520434f52455f504c5547494e532041444420434f4e53545241494e5420434f504c5f4e414d455f5645525f554b20554e49515545284e414d452c56455253494f4e293b0a414c544552205441424c4520454e544954595f4f5045524154494f4e535f4c4f472041444420434f4e53545241494e5420454f4c5f5245475f49445f554b20554e4951554528524547495354524154494f4e5f4944293b0a414c544552205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d532041444420434f4e53545241494e542045444d535f434f44455f554b20554e4951554528434f4445293b0a2d2d204e4f54453a20666f6c6c6f77696e6720756e697175656e65737320636f6e73747261696e747320666f72206d65746170726f6a6563742061737369676e6d656e747320776f726b2c206265636175736520286e756c6c20213d206e756c6c2920696e20506f737467726573200a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f455850455f49445f554b20554e4951554520284d4550525f49442c20455850455f4944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f53414d505f49445f554b20554e4951554520284d4550525f49442c2053414d505f4944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f444154415f49445f554b20554e4951554520284d4550525f49442c20444154415f4944293b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f4d4154455f49445f554b20554e4951554520284d4550525f49442c204d4154455f4944293b0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f434f44455f554b20554e495155452028434f4445293b0a414c544552205441424c4520434f4e54454e545f434f504945532041444420434f4e53545241494e5420636f6e74656e745f636f706965735f756e697175655f636865636b5f756b20554e49515545286c6f636174696f6e5f756e697175655f636865636b293b0a414c544552205441424c452053454d414e5449435f414e4e4f544154494f4e532041444420434f4e53545241494e542053454d414e5449435f414e4e4f544154494f4e535f5045524d5f49445f554b20554e4951554520285045524d5f4944293b0a0a2d2d204372656174696e6720666f726569676e206b657920636f6e73747261696e74730a0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c41524945532041444420434f4e53545241494e5420434f564f5f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f434f564f5f464b20464f524549474e204b45592028434f564f5f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152494553284944293b0a414c544552205441424c4520434f4e54524f4c4c45445f564f434142554c4152595f5445524d532041444420434f4e53545241494e5420435654455f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f445354595f464b20464f524549474e204b45592028445354595f494429205245464552454e43455320444154415f5345545f5459504553284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f455850455f464b20464f524549474e204b45592028455850455f49442c20455850455f46524f5a454e29205245464552454e434553204558504552494d454e54535f414c4c2849442c2046524f5a454e5f464f525f4441544129204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f53414d505f464b20464f524549474e204b4559202853414d505f49442c2053414d505f46524f5a454e29205245464552454e4345532053414d504c45535f414c4c2849442c2046524f5a454e5f464f525f4441544129204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f444153545f464b20464f524549474e204b45592028444153545f494429205245464552454e43455320444154415f53544f524553284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f4348494c4420464f524549474e204b45592028444154415f49445f4348494c442c204348494c445f46524f5a454e29205245464552454e43455320444154415f414c4c2849442c2046524f5a454e5f464f525f504152454e545329204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f504152454e5420464f524549474e204b45592028444154415f49445f504152454e542c20504152454e545f46524f5a454e29205245464552454e43455320444154415f414c4c2849442c2046524f5a454e5f464f525f4348494c4452454e29204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f434f4d5020464f524549474e204b45592028444154415f49445f4348494c442c20434f4d505f46524f5a454e29205245464552454e43455320444154415f414c4c2849442c2046524f5a454e5f464f525f434f4e545329204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f434f4e5420464f524549474e204b45592028444154415f49445f504152454e542c20434f4e545f46524f5a454e29205245464552454e43455320444154415f414c4c2849442c2046524f5a454e5f464f525f434f4d505329204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f444154415f464b5f52454c4154494f4e5348495020464f524549474e204b4559202852454c4154494f4e534849505f494429205245464552454e4345532052454c4154494f4e534849505f5459504553284944293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420445352455f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e5420444154415f5345545f52454c4154494f4e53484950535f504552535f464b20464f524549474e204b45592028504552535f49445f415554484f5229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f646174615f666b5f6368696c6420464f524549474e204b4559202873616d706c655f69645f6368696c642c204348494c445f46524f5a454e29205245464552454e4345532053414d504c45535f414c4c2869642c2046524f5a454e5f464f525f504152454e545329204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f646174615f666b5f706172656e7420464f524549474e204b4559202873616d706c655f69645f706172656e742c20504152454e545f46524f5a454e29205245464552454e4345532053414d504c45535f414c4c2869642c2046524f5a454e5f464f525f4348494c4452454e29204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f646174615f666b5f72656c6174696f6e7368697020464f524549474e204b4559202872656c6174696f6e736869705f696429205245464552454e4345532072656c6174696f6e736869705f7479706573286964293b0a414c544552205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c2041444420434f4e53545241494e5420736172655f64656c5f666b20464f524549474e204b4559202864656c5f696429205245464552454e4345532064656c6574696f6e73286964293b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f414c4c2041444420434f4e53545241494e542053414d504c455f52454c4154494f4e53484950535f504552535f464b20464f524549474e204b45592028504552535f49445f415554484f5229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f53544f52455f53455256494345532041444420434f4e53545241494e5420445353455f44535f464b20464f524549474e204b45592028444154415f53544f52455f494429205245464552454e43455320444154415f53544f52455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532041444420434f4e53545241494e54204453534453545f44535f464b20464f524549474e204b45592028444154415f53544f52455f534552564943455f494429205245464552454e43455320444154415f53544f52455f534552564943455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f54595045532041444420434f4e53545241494e54204453534453545f4453545f464b20464f524549474e204b45592028444154415f5345545f545950455f494429205245464552454e43455320444154415f5345545f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204556454e54532041444420434f4e53545241494e542045564e545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204556454e54532041444420434f4e53545241494e542045564e545f455841435f464b20464f524549474e204b45592028455841435f494429205245464552454e434553204154544143484d454e545f434f4e54454e5453284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f455854595f464b20464f524549474e204b45592028455854595f494429205245464552454e434553204558504552494d454e545f5459504553284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f50524f4a5f464b20464f524549474e204b4559202850524f4a5f49442c2050524f4a5f46524f5a454e29205245464552454e4345532050524f4a454354532849442c2046524f5a454e5f464f525f45585029204f4e2055504441544520434153434144453b0a414c544552205441424c45204558504552494d454e54535f414c4c2041444420434f4e53545241494e5420455850455f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f455850455f464b20464f524549474e204b45592028455850455f49442c20455850455f46524f5a454e29205245464552454e434553204558504552494d454e54535f414c4c2849442c2046524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f50524f4a5f464b20464f524549474e204b4559202850524f4a5f49442c2050524f4a5f46524f5a454e29205245464552454e4345532050524f4a454354532849442c2046524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f53414d505f464b20464f524549474e204b4559202853414d505f49442c2053414d505f46524f5a454e29205245464552454e4345532053414d504c45535f414c4c2849442c2046524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f434f4e545f464b20464f524549474e204b45592028455841435f494429205245464552454e434553204154544143484d454e545f434f4e54454e5453284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f455450545f464b20464f524549474e204b45592028455450545f494429205245464552454e434553204558504552494d454e545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f455850455f464b20464f524549474e204b45592028455850455f49442c20455850455f46524f5a454e29205245464552454e434553204558504552494d454e54535f414c4c2849442c2046524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f455450545f464b20464f524549474e204b45592028455450545f494429205245464552454e434553204558504552494d454e545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f54595045532041444420434f4e53545241494e5420455854595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f455854595f464b20464f524549474e204b45592028455854595f494429205245464552454e434553204558504552494d454e545f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f435654455f464b20464f524549474e204b45592028435654455f49445f53544f525f464d5429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f435654455f53544f5245445f4f4e5f464b20464f524549474e204b45592028435654455f49445f53544f524529205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f444154415f464b20464f524549474e204b45592028494429205245464552454e43455320444154415f414c4c284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f464654595f464b20464f524549474e204b45592028464654595f494429205245464552454e4345532046494c455f464f524d41545f5459504553284944293b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4e53545241494e5420455844415f4c4f54595f464b20464f524549474e204b455920284c4f54595f494429205245464552454e434553204c4f4341544f525f5459504553284944293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f504552535f464b5f5245474953544552455220464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452044454c4554494f4e532041444420434f4e53545241494e542044454c5f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f4d4154595f464b20464f524549474e204b455920284d4154595f494429205245464552454e434553204d4154455249414c5f5459504553284944293b0a414c544552205441424c45204d4154455249414c532041444420434f4e53545241494e54204d4154455f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f4d4154455f464b20464f524549474e204b455920284d4154455f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f4d5450545f464b20464f524549474e204b455920284d5450545f494429205245464552454e434553204d4154455249414c5f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f4d4154455f464b20464f524549474e204b455920284d4154455f494429205245464552454e434553204d4154455249414c5328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f4d5450545f464b20464f524549474e204b455920284d5450545f494429205245464552454e434553204d4154455249414c5f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f54595045532041444420434f4e53545241494e54204d4154595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f4d4154595f464b20464f524549474e204b455920284d4154595f494429205245464552454e434553204d4154455249414c5f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f54595045532041444420434f4e53545241494e5420445354595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e43455320535041434553284944293b0a414c544552205441424c4520504552534f4e532041444420434f4e53545241494e5420504552535f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f53504143455f464b20464f524549474e204b4559202853504143455f49442c2053504143455f46524f5a454e29205245464552454e434553205350414345532849442c2046524f5a454e5f464f525f50524f4a29204f4e2055504441544520434153434144453b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504552535f464b5f4c454144455220464f524549474e204b45592028504552535f49445f4c454144455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504552535f464b5f5245474953544552455220464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f4a454354532041444420434f4e53545241494e542050524f4a5f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f434f564f5f464b20464f524549474e204b45592028434f564f5f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152494553284944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f444154595f464b20464f524549474e204b45592028444154595f494429205245464552454e43455320444154415f5459504553284944293b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452050524f50455254595f54595045532041444420434f4e53545241494e5420505254595f4d4154595f464b20464f524549474e204b455920284d4154595f50524f505f494429205245464552454e434553204d4154455249414c5f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e4345532053504143455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f50524f4a4543545f464b20464f524549474e204b4559202850524f4a4543545f494429205245464552454e4345532050524f4a4543545328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f504552535f464b5f4752414e54454520464f524549474e204b45592028504552535f49445f4752414e54454529205245464552454e43455320504552534f4e5328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f41475f464b5f4752414e54454520464f524549474e204b4559202841475f49445f4752414e54454529205245464552454e43455320415554484f52495a4154494f4e5f47524f55505328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f504552535f464b5f5245474953544552455220464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f53504143455f464b20464f524549474e204b4559202853504143455f49442c2053504143455f46524f5a454e29205245464552454e434553205350414345532849442c2046524f5a454e5f464f525f53414d5029204f4e2055504441544520434153434144453b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f50524f4a5f464b20464f524549474e204b4559202850524f4a5f49442c2050524f4a5f46524f5a454e29205245464552454e4345532050524f4a454354532849442c2046524f5a454e5f464f525f53414d5029204f4e2055504441544520434153434144453b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f44454c5f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f53414d505f464b5f504152545f4f4620464f524549474e204b4559202853414d505f49445f504152545f4f462c20434f4e545f46524f5a454e29205245464552454e4345532053414d504c45535f414c4c2849442c2046524f5a454e5f464f525f434f4d5029204f4e2055504441544520434153434144453b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f455850455f464b20464f524549474e204b45592028455850455f49442c20455850455f46524f5a454e29205245464552454e434553204558504552494d454e54535f414c4c2849442c2046524f5a454e5f464f525f53414d5029204f4e2055504441544520434153434144453b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f534154595f464b20464f524549474e204b45592028534154595f494429205245464552454e4345532053414d504c455f5459504553284944293b0a414c544552205441424c452053414d504c45535f414c4c2041444420434f4e53545241494e542053414d505f504552535f464b5f4d4f4420464f524549474e204b45592028504552535f49445f4d4f44494649455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f53414d505f464b20464f524549474e204b4559202853414d505f49442c2053414d505f46524f5a454e29205245464552454e4345532053414d504c45535f414c4c2849442c2046524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f535450545f464b20464f524549474e204b45592028535450545f494429205245464552454e4345532053414d504c455f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f535450545f464b20464f524549474e204b45592028535450545f494429205245464552454e4345532053414d504c455f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f54595045532041444420434f4e53545241494e5420534154595f5343524950545f464b20464f524549474e204b4559202856414c49444154494f4e5f5343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f534154595f464b20464f524549474e204b45592028534154595f494429205245464552454e4345532053414d504c455f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f445354595f464b20464f524549474e204b45592028445354595f494429205245464552454e43455320444154415f5345545f54595045532849442920204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f505254595f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f435654455f464b20464f524549474e204b45592028435654455f494429205245464552454e43455320434f4e54524f4c4c45445f564f434142554c4152595f5445524d53284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f44535450545f464b20464f524549474e204b4559202844535450545f494429205245464552454e43455320444154415f5345545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f44535f464b20464f524549474e204b4559202844535f49442c20444153455f46524f5a454e29205245464552454e43455320444154415f414c4c2849442c2046524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f4d4150525f464b20464f524549474e204b455920284d4154455f50524f505f494429205245464552454e434553204d4154455249414c53284944293b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f44535450545f464b20464f524549474e204b4559202844535450545f494429205245464552454e43455320444154415f5345545f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f44535f464b20464f524549474e204b4559202844535f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e532041444420434f4e53545241494e54204147505f41475f464b20464f524549474e204b4559202841475f494429205245464552454e43455320415554484f52495a4154494f4e5f47524f555053284944293b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f55505f504552534f4e532041444420434f4e53545241494e54204147505f504552535f464b20464f524549474e204b45592028504552535f494429205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520415554484f52495a4154494f4e5f47524f5550532041444420434f4e53545241494e542041475f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a0a414c544552205441424c452046494c544552532041444420434f4e53545241494e542046494c545f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520475249445f435553544f4d5f434f4c554d4e532041444420434f4e53545241494e5420475249445f435553544f4d5f434f4c554d4e535f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c4520515545524945532041444420434f4e53545241494e5420515545525f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944292044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204f4e4c5920504f53545f524547495354524154494f4e5f444154415345545f51554555452041444420434f4e53545241494e5420707264715f64735f666b20464f524549474e204b4559202864735f696429205245464552454e43455320646174615f616c6c28696429204f4e2044454c45544520434153434144453b0a0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f4d41494e5f455850455f464b20464f524549474e204b455920284d41494e5f455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420455852454c485f50524f4a5f464b20464f524549474e204b4559202850524f4a5f494429205245464552454e4345532050524f4a4543545328494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f4d41494e5f53414d505f464b20464f524549474e204b455920284d41494e5f53414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e4345532053504143455328494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e542053414d5052454c485f50524f4a4543545f464b20464f524549474e204b4559202850524f4a5f494429205245464552454e4345532050524f4a4543545328494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f4d41494e5f444154415f464b20464f524549474e204b455920284d41494e5f444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f53414d505f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e54204441544152454c485f444154415f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f4d41494e5f50524f4a5f464b20464f524549474e204b455920284d41494e5f50524f4a5f494429205245464552454e4345532050524f4a4543545328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f455850455f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520534554204e554c4c3b0a414c544552205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f52592041444420434f4e53545241494e5420505252454c485f53504143455f464b20464f524549474e204b4559202853504143455f494429205245464552454e4345532053504143455328494429204f4e2044454c45544520534554204e554c4c3b0a0a414c544552205441424c45204c494e4b5f444154412041444420434f4e53545241494e54206c6e64615f646174615f666b20464f524549474e204b4559202849442c20444154415f46524f5a454e29205245464552454e43455320646174615f616c6c2849442c2046524f5a454e29204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144453b0a414c544552205441424c45204c494e4b5f444154412041444420434f4e53545241494e54204c494e4b5f444154415f494446525a5f554b20554e495155452849442c20444154415f46524f5a454e293b0a0a414c544552205441424c4520434f4e54454e545f434f504945532041444420434f4e53545241494e5420434f434f5f444154415f464b20464f524549474e204b45592028444154415f49442c20444154415f46524f5a454e29205245464552454e434553204c494e4b5f444154412849442c20444154415f46524f5a454e29204f4e2055504441544520434153434144453b0a414c544552205441424c4520434f4e54454e545f434f504945532041444420434f4e53545241494e5420434f434f5f45444d535f464b20464f524549474e204b4559202845444d535f494429205245464552454e4345532045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d53284944293b0a0a414c544552205441424c45204d45544150524f4a454354532041444420434f4e53545241494e54204d45544150524f4a454354535f4f574e45525f464b20464f524549474e204b455920284f574e455229205245464552454e43455320504552534f4e5328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f49445f464b20464f524549474e204b455920284d4550525f494429205245464552454e434553204d45544150524f4a4543545328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f455850455f49445f464b20464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f53414d505f49445f464b20464f524549474e204b4559202853414d505f494429205245464552454e4345532053414d504c45535f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f444154415f49445f464b20464f524549474e204b45592028444154415f494429205245464552454e43455320444154415f414c4c28494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4154455f49445f464b20464f524549474e204b455920284d4154455f494429205245464552454e434553204d4154455249414c5328494429204f4e2044454c45544520434153434144453b0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f44454c5f49445f464b20464f524549474e204b4559202844454c5f494429205245464552454e4345532044454c4554494f4e53284944293b0a0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f4f574e45525f464b20464f524549474e204b455920284f574e455229205245464552454e43455320504552534f4e5328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a0a414c544552205441424c452053454d414e5449435f414e4e4f544154494f4e532041444420434f4e53545241494e542053454d414e5449435f414e4e4f544154494f4e535f534154595f49445f464b20464f524549474e204b45592028534154595f494429205245464552454e4345532053414d504c455f545950455328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053454d414e5449435f414e4e4f544154494f4e532041444420434f4e53545241494e542053454d414e5449435f414e4e4f544154494f4e535f535450545f49445f464b20464f524549474e204b45592028535450545f494429205245464552454e4345532053414d504c455f545950455f50524f50455254595f545950455328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a414c544552205441424c452053454d414e5449435f414e4e4f544154494f4e532041444420434f4e53545241494e542053454d414e5449435f414e4e4f544154494f4e535f505254595f49445f464b20464f524549474e204b45592028505254595f494429205245464552454e4345532050524f50455254595f545950455328494429204f4e2044454c45544520434153434144452044454645525241424c4520494e495449414c4c592044454645525245443b0a0a2d2d204372656174696e6720636865636b20636f6e73747261696e74730a0a414c544552205441424c4520444154415f414c4c2041444420434f4e53545241494e5420444154415f434b20434845434b2028455850455f4944204953204e4f54204e554c4c204f522053414d505f4944204953204e4f54204e554c4c293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945532041444420434f4e53545241494e5420455850525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204558504552494d454e545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542045585052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a0a414c544552205441424c452053414d504c455f50524f504552544945532041444420434f4e53545241494e5420534150525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c452053414d504c455f50524f504552544945535f484953544f52592041444420434f4e53545241494e542053415052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945532041444420434f4e53545241494e54204d4150525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204d4154455249414c5f50524f504552544945535f484953544f52592041444420434f4e53545241494e54204d415052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a414c544552205441424c4520444154415f5345545f50524f504552544945532041444420434f4e53545241494e5420445350525f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e4f54204e554c4c20414e44204d4154455f50524f505f4944204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420435654455f4944204953204e554c4c20414e44204d4154455f50524f505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c4520444154415f5345545f50524f504552544945535f484953544f52592041444420434f4e53545241494e542044535052485f434b20434845434b200a09282856414c5545204953204e4f54204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e554c4c29204f52200a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e4f54204e554c4c20414e44204d4154455249414c204953204e554c4c29204f520a09202856414c5545204953204e554c4c20414e4420564f434142554c4152595f5445524d204953204e554c4c20414e44204d4154455249414c204953204e4f54204e554c4c290a09293b0a414c544552205441424c45204154544143484d454e54532041444420434f4e53545241494e5420415454415f4152435f434b20434845434b200a092828455850455f4944204953204e4f54204e554c4c20414e442050524f4a5f4944204953204e554c4c20414e442053414d505f4944204953204e554c4c29204f52200a092028455850455f4944204953204e554c4c20414e442050524f4a5f4944204953204e4f54204e554c4c20414e442053414d505f4944204953204e554c4c29204f520a092028455850455f4944204953204e554c4c20414e442050524f4a5f4944204953204e554c4c20414e442053414d505f4944204953204e4f54204e554c4c290a09293b0a414c544552205441424c45206576656e74732041444420434f4e53545241494e542065766e745f65745f656e756d5f636b20434845434b200a0928656e746974795f7479706520494e2028274154544143484d454e54272c202744415441534554272c20274558504552494d454e54272c20275350414345272c20274d4154455249414c272c202750524f4a454354272c202750524f50455254595f54595045272c202753414d504c45272c2027564f434142554c415259272c2027415554484f52495a4154494f4e5f47524f5550272c20274d45544150524f4a4543542729293b200a414c544552205441424c4520636f6e74726f6c6c65645f766f636162756c6172795f7465726d732041444420434f4e53545241494e5420637674655f636b20434845434b20286f7264696e616c203e2030293b0a0a414c544552205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2041444420434f4e53545241494e54204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f434845434b5f4e4e20434845434b20280a0928455850455f4944204953204e4f54204e554c4c20414e442053414d505f4944204953204e554c4c20414e4420444154415f4944204953204e554c4c20414e44204d4154455f4944204953204e554c4c29204f520a0928455850455f4944204953204e554c4c20414e442053414d505f4944204953204e4f54204e554c4c20414e4420444154415f4944204953204e554c4c20414e44204d4154455f4944204953204e554c4c29204f520a0928455850455f4944204953204e554c4c20414e442053414d505f4944204953204e554c4c20414e4420444154415f4944204953204e4f54204e554c4c20414e44204d4154455f4944204953204e554c4c29204f520a0928455850455f4944204953204e554c4c20414e442053414d505f4944204953204e554c4c20414e4420444154415f4944204953204e554c4c20414e44204d4154455f4944204953204e4f54204e554c4c29293b0a0a414c544552205441424c4520534352495054532041444420434f4e53545241494e54205343524950545f4e4e5f434b20434845434b0a202028504c5547494e5f54595045203d20275052454445504c4f59454427204f5220534352495054204953204e4f54204e554c4c293b0a0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f53544154455f53544152545f444154455f434845434b20434845434b20280a0928535441544520494e2028274e4557272c275343484544554c4544272920414e442053544152545f44415445204953204e554c4c29204f52200a0928535441544520494e20282752554e4e494e47272c2746494e4953484544272c274641494c4544272920414e442053544152545f44415445204953204e4f54204e554c4c290a293b0a0a414c544552205441424c45204f5045524154494f4e5f455845435554494f4e532041444420434f4e53545241494e54204f5045524154494f4e5f455845435554494f4e535f53544154455f46494e4953485f444154455f434845434b20434845434b20280a0928535441544520494e2028274e4557272c275343484544554c4544272c2752554e4e494e47272920414e442046494e4953485f44415445204953204e554c4c29204f52200a0928535441544520494e20282746494e4953484544272c274641494c4544272920414e442046494e4953485f44415445204953204e4f54204e554c4c290a293b0a0a414c544552205441424c452053454d414e5449435f414e4e4f544154494f4e532041444420434f4e53545241494e542053454d414e5449435f414e4e4f544154494f4e535f5353505f434b20434845434b200a092828534154595f4944204953204e4f54204e554c4c20414e4420535450545f4944204953204e554c4c20414e4420505254595f4944204953204e554c4c29204f52200a092028534154595f4944204953204e554c4c20414e4420535450545f4944204953204e4f54204e554c4c20414e4420505254595f4944204953204e554c4c29204f520a092028534154595f4944204953204e554c4c20414e4420535450545f4944204953204e554c4c20414e4420505254595f4944204953204e4f54204e554c4c290a09293b0a20200a2d2d204372656174696e6720696e64696365730a0a43524541544520494e44455820434f564f5f504552535f464b5f49204f4e20434f4e54524f4c4c45445f564f434142554c41524945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820435654455f434f564f5f464b5f49204f4e20434f4e54524f4c4c45445f564f434142554c4152595f5445524d532028434f564f5f4944293b0a43524541544520494e44455820435654455f504552535f464b5f49204f4e20434f4e54524f4c4c45445f564f434142554c4152595f5445524d532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820444154415f494446525a5f504b5f49204f4e20444154415f414c4c202869642c2066726f7a656e293b0a43524541544520494e44455820444154415f494446525a5f43485f504b5f49204f4e20444154415f414c4c202869642c2066726f7a656e5f666f725f6368696c6472656e293b0a43524541544520494e44455820444154415f494446525a5f505f504b5f49204f4e20444154415f414c4c202869642c2066726f7a656e5f666f725f706172656e7473293b0a43524541544520494e44455820444154415f494446525a5f434f4d505f504b5f49204f4e20444154415f414c4c202869642c2066726f7a656e5f666f725f636f6d7073293b0a43524541544520494e44455820444154415f494446525a5f434f4e545f504b5f49204f4e20444154415f414c4c202869642c2066726f7a656e5f666f725f636f6e7473293b0a43524541544520494e44455820444154415f445354595f464b5f49204f4e20444154415f414c4c2028445354595f4944293b0a43524541544520494e44455820444154415f53414d505f464b5f49204f4e20444154415f414c4c202853414d505f4944293b0a43524541544520494e44455820444154415f455850455f464b5f49204f4e20444154415f414c4c2028455850455f4944293b0a43524541544520494e44455820444154415f44454c5f464b5f49204f4e20444154415f414c4c202844454c5f4944293b0a43524541544520494e44455820444154415f414343545f49204f4e20444154415f414c4c20284143434553535f54494d455354414d50293b0a43524541544520494e444558204c494e4b5f444154415f494446525a5f504b5f49204f4e204c494e4b5f44415441202849442c20444154415f46524f5a454e293b0a43524541544520494e44455820445352455f444154415f464b5f495f4348494c44204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c2028444154415f49445f4348494c44293b0a43524541544520494e44455820445352455f444154415f464b5f495f504152454e54204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c2028444154415f49445f504152454e54293b0a43524541544520494e44455820445352455f44454c5f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c202844454c5f4944293b0a43524541544520494e44455820736172655f646174615f666b5f695f6368696c64204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202873616d706c655f69645f6368696c64293b0a43524541544520494e44455820736172655f646174615f666b5f695f706172656e74204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202873616d706c655f69645f706172656e74293b0a43524541544520494e44455820736172655f646174615f666b5f695f72656c6174696f6e73686970204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202872656c6174696f6e736869705f6964293b0a43524541544520494e44455820736172655f64656c5f666b5f69204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c202864656c5f6964293b0a43524541544520494e44455820445353455f44535f464b5f49204f4e20444154415f53544f52455f53455256494345532028444154415f53544f52455f4944293b0a43524541544520494e444558204453534453545f44535f464b5f49204f4e20444154415f53544f52455f534552564943455f444154415f5345545f54595045532028444154415f53544f52455f534552564943455f4944293b0a43524541544520494e444558204453534453545f4453545f464b5f49204f4e20444154415f53544f52455f534552564943455f444154415f5345545f54595045532028444154415f5345545f545950455f4944293b0a43524541544520494e44455820455450545f455854595f464b5f49204f4e204558504552494d454e545f545950455f50524f50455254595f54595045532028455854595f4944293b0a43524541544520494e44455820455450545f504552535f464b5f49204f4e204558504552494d454e545f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820455450545f505254595f464b5f49204f4e204558504552494d454e545f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e4445582045564e545f504552535f464b5f49204f4e204556454e54532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582045564e545f46525f49445f464b5f49204f4e204556454e545320284556454e545f545950452c204944454e5449464945525329205748455245204556454e545f54595045203d2027465245455a494e47273b0a43524541544520494e4445582045564e545f455841435f464b5f49204f4e204556454e54532028455841435f4944293b0a43524541544520494e44455820415454415f455850455f464b5f49204f4e204154544143484d454e54532028455850455f4944293b0a43524541544520494e44455820415454415f53414d505f464b5f49204f4e204154544143484d454e5453202853414d505f4944293b0a43524541544520494e44455820415454415f50524f4a5f464b5f49204f4e204154544143484d454e5453202850524f4a5f4944293b0a43524541544520494e44455820415454415f504552535f464b5f49204f4e204154544143484d454e54532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820415454415f455841435f464b5f49204f4e204154544143484d454e54532028455841435f4944293b0a43524541544520494e44455820455844415f435654455f464b5f49204f4e2045585445524e414c5f444154412028435654455f49445f53544f525f464d54293b0a43524541544520494e44455820455844415f435654455f53544f5245445f4f4e5f464b5f49204f4e2045585445524e414c5f444154412028435654455f49445f53544f5245293b0a43524541544520494e44455820455844415f464654595f464b5f49204f4e2045585445524e414c5f444154412028464654595f4944293b0a43524541544520494e44455820455844415f4c4f54595f464b5f49204f4e2045585445524e414c5f4441544120284c4f54595f4944293b0a43524541544520494e44455820455850455f494446525a5f504b5f49204f4e204558504552494d454e54535f414c4c202869642c2066726f7a656e293b0a43524541544520494e44455820455850455f494446525a5f535f504b5f49204f4e204558504552494d454e54535f414c4c202869642c2066726f7a656e5f666f725f73616d70293b0a43524541544520494e44455820455850455f494446525a5f445f504b5f49204f4e204558504552494d454e54535f414c4c202869642c2066726f7a656e5f666f725f64617461293b0a43524541544520494e44455820455850455f455854595f464b5f49204f4e204558504552494d454e54535f414c4c2028455854595f4944293b0a43524541544520494e44455820455850455f44454c5f464b5f49204f4e204558504552494d454e54535f414c4c202844454c5f4944293b0a43524541544520494e44455820455850455f504552535f464b5f49204f4e204558504552494d454e54535f414c4c2028504552535f49445f52454749535445524552293b0a43524541544520494e44455820455850455f50524f4a5f464b5f49204f4e204558504552494d454e54535f414c4c202850524f4a5f4944293b0a43524541544520494e44455820455850525f435654455f464b5f49204f4e204558504552494d454e545f50524f504552544945532028435654455f4944293b0a43524541544520494e44455820455850525f455450545f464b5f49204f4e204558504552494d454e545f50524f504552544945532028455450545f4944293b0a43524541544520494e44455820455850525f455850455f464b5f49204f4e204558504552494d454e545f50524f504552544945532028455850455f4944293b0a43524541544520494e44455820455850525f504552535f464b5f49204f4e204558504552494d454e545f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820455850525f4d4150525f464b5f49204f4e204558504552494d454e545f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e4445582045585052485f455450545f464b5f49204f4e204558504552494d454e545f50524f504552544945535f484953544f52592028455450545f4944293b0a43524541544520494e4445582045585052485f455850455f464b5f49204f4e204558504552494d454e545f50524f504552544945535f484953544f52592028455850455f4944293b0a43524541544520494e4445582045585052485f565554535f464b5f49204f4e204558504552494d454e545f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e4445582053504143455f504552535f524547495354455245445f42595f464b5f49204f4e205350414345532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582044454c5f504552535f464b5f49204f4e2044454c4554494f4e532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d4150525f435654455f464b5f49204f4e204d4154455249414c5f50524f504552544945532028435654455f4944293b0a43524541544520494e444558204d4150525f4d4154455f464b5f49204f4e204d4154455249414c5f50524f5045525449455320284d4154455f4944293b0a43524541544520494e444558204d4150525f4d5450545f464b5f49204f4e204d4154455249414c5f50524f5045525449455320284d5450545f4944293b0a43524541544520494e444558204d4150525f504552535f464b5f49204f4e204d4154455249414c5f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d4150525f4d4150525f464b5f49204f4e204d4154455249414c5f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e444558204d415052485f455450545f464b5f49204f4e204d4154455249414c5f50524f504552544945535f484953544f525920284d5450545f4944293b0a43524541544520494e444558204d415052485f455850455f464b5f49204f4e204d4154455249414c5f50524f504552544945535f484953544f525920284d4154455f4944293b0a43524541544520494e444558204d415052485f565554535f464b5f49204f4e204d4154455249414c5f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e444558204d4154455f4d4154595f464b5f49204f4e204d4154455249414c5320284d4154595f4944293b0a43524541544520494e444558204d4154455f504552535f464b5f49204f4e204d4154455249414c532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d5450545f4d4154595f464b5f49204f4e204d4154455249414c5f545950455f50524f50455254595f545950455320284d4154595f4944293b0a43524541544520494e444558204d5450545f504552535f464b5f49204f4e204d4154455249414c5f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e444558204d5450545f505254595f464b5f49204f4e204d4154455249414c5f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e44455820504552535f53504143455f464b5f49204f4e20504552534f4e53202853504143455f4944293b0a43524541544520494e44455820504552535f49535f4143544956455f49204f4e20504552534f4e53202849535f414354495645293b0a43524541544520494e4445582050524f4a5f494446525a5f504b5f49204f4e2050524f4a45435453202849442c2046524f5a454e293b0a43524541544520494e4445582050524f4a5f494446525a5f455f504b5f49204f4e2050524f4a45435453202849442c2046524f5a454e5f464f525f455850293b0a43524541544520494e4445582050524f4a5f494446525a5f535f504b5f49204f4e2050524f4a45435453202849442c2046524f5a454e5f464f525f53414d50293b0a43524541544520494e4445582050524f4a5f53504143455f464b5f49204f4e2050524f4a45435453202853504143455f4944293b0a43524541544520494e4445582050524f4a5f504552535f464b5f495f4c4541444552204f4e2050524f4a454354532028504552535f49445f4c4541444552293b0a43524541544520494e4445582050524f4a5f504552535f464b5f495f52454749535445524552204f4e2050524f4a454354532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820505254595f434f564f5f464b5f49204f4e2050524f50455254595f54595045532028434f564f5f4944293b0a43524541544520494e44455820505254595f444154595f464b5f49204f4e2050524f50455254595f54595045532028444154595f4944293b0a43524541544520494e44455820505254595f504552535f464b5f49204f4e2050524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820524f41535f53504143455f464b5f49204f4e20524f4c455f41535349474e4d454e5453202853504143455f4944293b0a43524541544520494e44455820524f41535f50524f4a4543545f464b5f49204f4e20524f4c455f41535349474e4d454e5453202850524f4a4543545f4944293b0a43524541544520494e44455820524f41535f504552535f464b5f495f4752414e544545204f4e20524f4c455f41535349474e4d454e54532028504552535f49445f4752414e544545293b0a43524541544520494e44455820524f41535f41475f464b5f495f4752414e544545204f4e20524f4c455f41535349474e4d454e5453202841475f49445f4752414e544545293b0a43524541544520494e44455820524f41535f504552535f464b5f495f52454749535445524552204f4e20524f4c455f41535349474e4d454e54532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582053504143455f494446525a5f504b5f49204f4e20535041434553202869642c2066726f7a656e293b0a43524541544520494e4445582053504143455f494446525a5f505f504b5f49204f4e20535041434553202869642c2066726f7a656e5f666f725f70726f6a293b0a43524541544520494e4445582053504143455f494446525a5f535f504b5f49204f4e20535041434553202869642c2066726f7a656e5f666f725f73616d70293b0a43524541544520494e4445582053414d505f494446525a5f504b5f49204f4e2053414d504c45535f414c4c202869642c2066726f7a656e293b0a43524541544520494e4445582053414d505f494446525a5f435f504b5f49204f4e2053414d504c45535f414c4c202869642c2066726f7a656e5f666f725f636f6d70293b0a43524541544520494e4445582053414d505f494446525a5f43485f504b5f49204f4e2053414d504c45535f414c4c202869642c2066726f7a656e5f666f725f6368696c6472656e293b0a43524541544520494e4445582053414d505f494446525a5f505f504b5f49204f4e2053414d504c45535f414c4c202869642c2066726f7a656e5f666f725f706172656e7473293b0a43524541544520494e4445582053414d505f494446525a5f445f504b5f49204f4e2053414d504c45535f414c4c202869642c2066726f7a656e5f666f725f64617461293b0a43524541544520494e4445582053414d505f44454c5f464b5f49204f4e2053414d504c45535f414c4c202844454c5f4944293b0a43524541544520494e4445582053414d505f504552535f464b5f49204f4e2053414d504c45535f414c4c2028504552535f49445f52454749535445524552293b0a43524541544520494e4445582053414d505f53414d505f464b5f495f504152545f4f46204f4e2053414d504c45535f414c4c202853414d505f49445f504152545f4f46293b0a43524541544520494e4445582053414d505f455850455f464b5f49204f4e2053414d504c45535f414c4c2028455850455f4944293b0a43524541544520494e4445582053414d505f50524f4a5f464b5f49204f4e2053414d504c45535f414c4c202850524f4a5f4944293b0a43524541544520494e4445582053414d505f434f44455f49204f4e2053414d504c45535f414c4c2028434f4445293b0a43524541544520494e4445582053414d505f534154595f464b5f49204f4e2053414d504c45535f414c4c2028534154595f4944293b0a43524541544520494e44455820534150525f435654455f464b5f49204f4e2053414d504c455f50524f504552544945532028435654455f4944293b0a43524541544520494e44455820534150525f504552535f464b5f49204f4e2053414d504c455f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820534150525f53414d505f464b5f49204f4e2053414d504c455f50524f50455254494553202853414d505f4944293b0a43524541544520494e44455820534150525f535450545f464b5f49204f4e2053414d504c455f50524f504552544945532028535450545f4944293b0a43524541544520494e44455820534150525f4d4150525f464b5f49204f4e2053414d504c455f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e4445582053415052485f455450545f464b5f49204f4e2053414d504c455f50524f504552544945535f484953544f52592028535450545f4944293b0a43524541544520494e4445582053415052485f455850455f464b5f49204f4e2053414d504c455f50524f504552544945535f484953544f5259202853414d505f4944293b0a43524541544520494e4445582053415052485f565554535f464b5f49204f4e2053414d504c455f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e44455820535450545f504552535f464b5f49204f4e2053414d504c455f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820535450545f505254595f464b5f49204f4e2053414d504c455f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e44455820535450545f534154595f464b5f49204f4e2053414d504c455f545950455f50524f50455254595f54595045532028534154595f4944293b0a43524541544520494e44455820445350525f435654455f464b5f49204f4e20444154415f5345545f50524f504552544945532028435654455f4944293b0a43524541544520494e44455820445350525f44535450545f464b5f49204f4e20444154415f5345545f50524f50455254494553202844535450545f4944293b0a43524541544520494e44455820445350525f44535f464b5f49204f4e20444154415f5345545f50524f50455254494553202844535f4944293b0a43524541544520494e44455820445350525f504552535f464b5f49204f4e20444154415f5345545f50524f504552544945532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820445350525f4d4150525f464b5f49204f4e20444154415f5345545f50524f5045525449455320284d4154455f50524f505f4944293b0a43524541544520494e4445582044535052485f455450545f464b5f49204f4e20444154415f5345545f50524f504552544945535f484953544f5259202844535450545f4944293b0a43524541544520494e4445582044535052485f455850455f464b5f49204f4e20444154415f5345545f50524f504552544945535f484953544f5259202844535f4944293b0a43524541544520494e4445582044535052485f565554535f464b5f49204f4e20444154415f5345545f50524f504552544945535f484953544f5259202856414c49445f554e54494c5f54494d455354414d50293b0a43524541544520494e4445582044535450545f445354595f464b5f49204f4e20444154415f5345545f545950455f50524f50455254595f54595045532028445354595f4944293b0a43524541544520494e4445582044535450545f504552535f464b5f49204f4e20444154415f5345545f545950455f50524f50455254595f54595045532028504552535f49445f52454749535445524552293b0a43524541544520494e4445582044535450545f505254595f464b5f49204f4e20444154415f5345545f545950455f50524f50455254595f54595045532028505254595f4944293b0a43524541544520494e4445582046494c545f504552535f464b5f49204f4e2046494c544552532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820475249445f435553544f4d5f434f4c554d4e535f504552535f464b5f49204f4e20475249445f435553544f4d5f434f4c554d4e532028504552535f49445f52454749535445524552293b0a43524541544520494e444558205343524950545f504552535f464b5f49204f4e20534352495054532028504552535f49445f52454749535445524552293b0a43524541544520494e44455820454e544954595f4f5045524154494f4e535f4c4f475f5249445f49204f4e20454e544954595f4f5045524154494f4e535f4c4f4728524547495354524154494f4e5f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f53414d505f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f49442c2053414d505f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f444154415f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f49442c20444154415f4944293b0a43524541544520494e44455820455852454c485f4d41494e5f455850455f464b5f50524f4a5f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920284d41494e5f455850455f49442c2050524f4a5f4944293b0a43524541544520494e44455820455852454c485f53414d505f49445f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259202853414d505f4944293b0a43524541544520494e44455820455852454c485f444154415f49445f464b5f49204f4e204558504552494d454e545f52454c4154494f4e53484950535f484953544f52592028444154415f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f455850455f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c20455850455f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f53414d505f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c2053414d505f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f444154415f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c20444154415f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f53504143455f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c2053504143455f4944293b0a43524541544520494e4445582053414d5052454c485f4d41494e5f53414d505f464b5f50524f4a5f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f525920284d41494e5f53414d505f49442c2050524f4a5f4944293b0a43524541544520494e4445582053414d5052454c485f53414d505f49445f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f5259202853414d505f4944293b0a43524541544520494e4445582053414d5052454c485f444154415f49445f464b5f49204f4e2053414d504c455f52454c4154494f4e53484950535f484953544f52592028444154415f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f455850455f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f49442c20455850455f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f53414d505f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f49442c2053414d505f4944293b0a43524541544520494e444558204441544152454c485f4d41494e5f444154415f464b5f444154415f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f525920284d41494e5f444154415f49442c20444154415f4944293b0a43524541544520494e444558204441544152454c485f444154415f464b5f49204f4e20444154415f5345545f52454c4154494f4e53484950535f484953544f52592028444154415f4944293b0a43524541544520494e44455820505252454c485f4d41494e5f50524f4a5f464b5f49204f4e2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920284d41494e5f50524f4a5f4944293b0a43524541544520494e44455820505252454c485f4d41494e5f50524f4a5f464b5f455850455f464b5f49204f4e2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920284d41494e5f50524f4a5f49442c20455850455f4944293b0a43524541544520494e44455820505252454c485f4d41494e5f50524f4a5f464b5f53504143455f464b5f49204f4e2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920284d41494e5f50524f4a5f49442c2053504143455f4944293b0a43524541544520494e444558204d45544150524f4a454354535f4f574e45525f464b5f49204f4e204d45544150524f4a4543545320284f574e4552293b0a43524541544520494e444558204d45544150524f4a454354535f4e414d455f49204f4e204d45544150524f4a4543545320284e414d45293b0a43524541544520554e4951554520494e444558204d45544150524f4a454354535f4e414d455f4f574e45525f554b204f4e204d45544150524f4a4543545320286c6f776572284e414d45292c204f574e4552293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4550525f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20284d4550525f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f455850455f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2028455850455f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f53414d505f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c202853414d505f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f444154415f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c2028444154415f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f4d4154455f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20284d4154455f4944293b0a43524541544520494e444558204d45544150524f4a4543545f41535349474e4d454e54535f414c4c5f44454c5f464b5f49204f4e204d45544150524f4a4543545f41535349474e4d454e54535f414c4c202844454c5f4944293b0a43524541544520494e444558204f5045524154494f4e5f455845435554494f4e535f434f44455f49204f4e204f5045524154494f4e5f455845435554494f4e532028434f4445293b0a43524541544520494e444558204f5045524154494f4e5f455845435554494f4e535f4f574e45525f49204f4e204f5045524154494f4e5f455845435554494f4e5320284f574e4552293b0a43524541544520494e444558204f5045524154494f4e5f455845435554494f4e535f415641494c4142494c4954595f49204f4e204f5045524154494f4e5f455845435554494f4e532028415641494c4142494c495459293b0a43524541544520494e444558204f5045524154494f4e5f455845435554494f4e535f53554d4d4152595f415641494c4142494c4954595f49204f4e204f5045524154494f4e5f455845435554494f4e53202853554d4d4152595f415641494c4142494c495459293b0a43524541544520494e444558204f5045524154494f4e5f455845435554494f4e535f44455441494c535f415641494c4142494c4954595f49204f4e204f5045524154494f4e5f455845435554494f4e53202844455441494c535f415641494c4142494c495459293b0a43524541544520494e4445582053454d414e5449435f414e4e4f544154494f4e535f534154595f49445f49204f4e2053454d414e5449435f414e4e4f544154494f4e532028534154595f4944293b0a43524541544520494e4445582053454d414e5449435f414e4e4f544154494f4e535f535450545f49445f49204f4e2053454d414e5449435f414e4e4f544154494f4e532028535450545f4944293b0a43524541544520494e4445582053454d414e5449435f414e4e4f544154494f4e535f505254595f49445f49204f4e2053454d414e5449435f414e4e4f544154494f4e532028505254595f4944293b0a0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f41475f504552535f4152435f434b20434845434b20282841475f49445f4752414e544545204953204e4f54204e554c4c20414e4420504552535f49445f4752414e544545204953204e554c4c29204f52202841475f49445f4752414e544545204953204e554c4c20414e4420504552535f49445f4752414e544545204953204e4f54204e554c4c29293b0a414c544552205441424c4520524f4c455f41535349474e4d454e54532041444420434f4e53545241494e5420524f41535f53504143455f50524f4a4543545f434b20434845434b202853504143455f4944204953204e554c4c204f522050524f4a4543545f4944204953204e554c4c293b0a414c544552205441424c45204d4154455249414c5f54595045532041444420434f4e53545241494e54204d4154595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45204558504552494d454e545f54595045532041444420434f4e53545241494e5420455854595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c452053414d504c455f54595045532041444420434f4e53545241494e5420534154595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c4520444154415f5345545f54595045532041444420434f4e53545241494e5420445354595f424b5f554b20554e4951554528434f4445293b0a414c544552205441424c45205350414345532041444420434f4e53545241494e542053504143455f424b5f554b20554e4951554528434f4445293b0a	\N
178	./sql/postgresql/178/function-178.sql	SUCCESS	2020-10-01 08:03:14.186	\\x2d2d204372656174696e672046756e6374696f6e730a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652066756e6374696f6e2052454e414d455f53455155454e43452829207468617420697320726571756972656420666f722072656e616d696e67207468652073657175656e6365732062656c6f6e67696e6720746f207461626c65730a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a4352454154452046554e4354494f4e2052454e414d455f53455155454e4345284f4c445f4e414d4520564152434841522c204e45575f4e414d452056415243484152292052455455524e5320494e54454745522041532024240a4445434c4152450a2020435552525f5345515f56414c202020494e54454745523b0a424547494e0a202053454c45435420494e544f20435552525f5345515f56414c204e45585456414c284f4c445f4e414d45293b0a20204558454355544520274352454154452053455155454e43452027207c7c204e45575f4e414d45207c7c202720535441525420574954482027207c7c20435552525f5345515f56414c3b0a202045584543555445202744524f502053455155454e43452027207c7c204f4c445f4e414d453b0a202052455455524e20435552525f5345515f56414c3b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020437265617465207472696767657220434f4e54524f4c4c45445f564f434142554c4152595f434845434b200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e20434f4e54524f4c4c45445f564f434142554c4152595f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f636f64652020434f44453b0a424547494e0a0a20202073656c65637420636f646520696e746f20765f636f64652066726f6d20646174615f7479706573207768657265206964203d204e45572e646174795f69643b0a0a2020202d2d20436865636b206966207468652064617461206973206f6620747970652022434f4e54524f4c4c4544564f434142554c415259220a202020696620765f636f6465203d2027434f4e54524f4c4c4544564f434142554c41525927207468656e0a2020202020206966204e45572e636f766f5f6964204953204e554c4c207468656e0a202020202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f662050726f706572747920547970652028436f64653a202529206661696c65642c206173206974732044617461205479706520697320434f4e54524f4c4c4544564f434142554c4152592c20627574206974206973206e6f74206c696e6b656420746f206120436f6e74726f6c6c656420566f636162756c6172792e272c204e45572e636f64653b0a202020202020656e642069663b0a202020656e642069663b0a0a20202052455455524e204e45573b0a0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220434f4e54524f4c4c45445f564f434142554c4152595f434845434b204245464f524520494e53455254204f5220555044415445204f4e2050524f50455254595f54595045530a20202020464f52204541434820524f5720455845435554452050524f43454455524520434f4e54524f4c4c45445f564f434142554c4152595f434845434b28293b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520747269676765722045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e2045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f636f766f5f636f64652020434f44453b0a202020646174615f636f646520434f44453b0a424547494e0a0a20202073656c65637420636f646520696e746f20765f636f766f5f636f64652066726f6d20636f6e74726f6c6c65645f766f636162756c61726965730a20202020202077686572652069735f696e7465726e616c5f6e616d657370616365203d207472756520616e64200a2020202020202020206964203d202873656c65637420636f766f5f69642066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d73207768657265206964203d204e45572e637674655f69645f73746f725f666d74293b0a2020202d2d20436865636b2069662074686520646174612073746f7261676520666f726d61742069732061207465726d206f662074686520636f6e74726f6c6c656420766f636162756c617279202253544f524147455f464f524d4154220a202020696620765f636f766f5f636f646520213d202753544f524147455f464f524d415427207468656e0a20202020202073656c65637420636f646520696e746f20646174615f636f64652066726f6d20646174615f616c6c207768657265206964203d204e45572e69643b200a202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f6620446174612028436f64653a202529206661696c65642c206173206974732053746f7261676520466f726d617420697320252c2062757420697320726571756972656420746f2062652053544f524147455f464f524d41542e272c20646174615f636f64652c20765f636f766f5f636f64653b0a202020656e642069663b0a0a20202052455455524e204e45573b0a0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b204245464f524520494e53455254204f5220555044415445204f4e2045585445524e414c5f444154410a20202020464f52204541434820524f5720455845435554452050524f4345445552452045585445524e414c5f444154415f53544f524147455f464f524d41545f434845434b28293b0a0a2020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520747269676765727320666f7220636865636b696e672073616d706c6520636f646520756e697175656e657373200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a202020200a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f66696c6c5f636f64655f756e697175655f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20204e45572e636f64655f756e697175655f636865636b203d204e45572e636f6465207c7c20272c27207c7c20636f616c65736365284e45572e73616d705f69645f706172745f6f662c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e70726f6a5f69642c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e73706163655f69642c202d31293b0a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a20200a20200a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f66696c6c5f737562636f64655f756e697175655f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a4445434c4152450a20202020756e697175655f737562636f64652020424f4f4c45414e5f434841523b0a424547494e0a2020202053454c4543542069735f737562636f64655f756e6971756520696e746f20756e697175655f737562636f64652046524f4d2073616d706c655f7479706573205748455245206964203d204e45572e736174795f69643b0a202020200a2020202049462028756e697175655f737562636f646529205448454e0a202020204e45572e737562636f64655f756e697175655f636865636b203d204e45572e636f6465207c7c20272c27207c7c20636f616c65736365284e45572e736174795f69642c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e70726f6a5f69642c202d3129207c7c20272c27207c7c20636f616c65736365284e45572e73706163655f69642c202d31293b0a20202020454c53450a202020204e45572e737562636f64655f756e697175655f636865636b203d204e554c4c3b0a2020454e442049463b0a20200a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2064697361626c655f70726f6a6563745f6c6576656c5f73616d706c657328290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20202020494620284e45572e70726f6a5f6964204953204e4f54204e554c4c29205448454e0a20202020524149534520455843455054494f4e202750726f6a656374206c6576656c2073616d706c6573206172652064697361626c6564273b0a2020454e442049463b0a20200a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a20200a20200a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f747970655f66696c6c5f737562636f64655f756e697175655f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20202020494620284e45572e69735f737562636f64655f756e697175653a3a626f6f6c65616e203c3e204f4c442e69735f737562636f64655f756e697175653a3a626f6f6c65616e29205448454e0a2020202020205550444154452073616d706c65735f616c6c2053455420737562636f64655f756e697175655f636865636b203d20737562636f64655f756e697175655f636865636b20574845524520736174795f6964203d204e45572e69643b0a2020454e442049463b0a2020202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a20200a43524541544520545249474745522073616d706c655f66696c6c5f636f64655f756e697175655f636865636b0a20204245464f524520494e53455254204f52205550444154450a20204f4e2073616d706c65735f616c6c0a2020464f52204541434820524f570a2020455845435554452050524f4345445552452073616d706c655f66696c6c5f636f64655f756e697175655f636865636b28293b0a0a43524541544520545249474745522064697361626c655f70726f6a6563745f6c6576656c5f73616d706c65730a20204245464f524520494e53455254204f52205550444154450a20204f4e2073616d706c65735f616c6c0a2020464f52204541434820524f570a2020455845435554452050524f4345445552452064697361626c655f70726f6a6563745f6c6576656c5f73616d706c657328293b0a0a0a43524541544520545249474745522073616d706c655f66696c6c5f737562636f64655f756e697175655f636865636b0a20204245464f524520494e53455254204f52205550444154450a20204f4e2073616d706c65735f616c6c0a2020464f52204541434820524f570a2020455845435554452050524f4345445552452073616d706c655f66696c6c5f737562636f64655f756e697175655f636865636b28293b0a20200a43524541544520545249474745522073616d706c655f747970655f66696c6c5f737562636f64655f756e697175655f636865636b0a20204146544552205550444154450a20204f4e2073616d706c655f74797065730a2020464f52204541434820524f570a2020455845435554452050524f4345445552452073616d706c655f747970655f66696c6c5f737562636f64655f756e697175655f636865636b28293b0a202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a207472696767657220666f72206461746120736574733a20546865792073686f756c64206265206c696e6b656420746f20616e206578706572696d656e74206f7220612073616d706c6520776974682073706163650a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e20646174615f6578705f6f725f73616d706c655f6c696e6b5f636865636b28292052455455524e5320747269676765722041532024240a4445434c4152450a202073706163655f696420434f44453b0a202073616d706c655f636f646520434f44453b0a424547494e0a20206966204e45572e657870655f6964204953204e4f54204e554c4c207468656e0a2020202052455455524e204e45573b0a2020656e642069663b0a20206966204e45572e73616d705f6964204953204e554c4c207468656e0a20202020524149534520455843455054494f4e20274e656974686572206578706572696d656e74206e6f722073616d706c652069732073706563696669656420666f722064617461207365742025272c204e45572e636f64653b0a2020656e642069663b0a202073656c65637420732e69642c20732e636f646520696e746f2073706163655f69642c2073616d706c655f636f64652066726f6d2073616d706c65735f616c6c207320776865726520732e6964203d204e45572e73616d705f69643b0a202069662073706163655f6964206973204e554c4c207468656e0a20202020524149534520455843455054494f4e202753616d706c6520252069732061207368617265642073616d706c652e272c2073616d706c655f636f64653b0a2020656e642069663b0a202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220646174615f6578705f6f725f73616d706c655f6c696e6b5f636865636b204245464f524520494e53455254204f5220555044415445204f4e20646174615f616c6c0a464f52204541434820524f5720455845435554452050524f43454455524520646174615f6578705f6f725f73616d706c655f6c696e6b5f636865636b28293b0a20200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652074726967676572204d4154455249414c2f53414d504c452f4558504552494d454e542f444154415f534554205f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b0a2d2d202020202020202020202020497420636865636b732074686174206966206d6174657269616c2070726f70657274792076616c75652069732061737369676e656420746f2074686520656e746974792c0a2d2d0909090909097468656e20746865206d6174657269616c207479706520697320657175616c20746f20746865206f6e65206465736372696265642062792070726f706572747920747970652e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e204d4154455249414c5f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d206d6174657269616c5f747970655f70726f70657274795f747970657320657470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e6d7470745f6964203d20657470742e696420414e4420657470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a09090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a4352454154452054524947474552204d4154455249414c5f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e206d6174657269616c5f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f434544555245204d4154455249414c5f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a202020200a435245415445204f52205245504c4143452046554e4354494f4e2053414d504c455f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d2073616d706c655f747970655f70726f70657274795f747970657320657470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e737470745f6964203d20657470742e696420414e4420657470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a090909090909090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522053414d504c455f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e2073616d706c655f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f4345445552452053414d504c455f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a202020200a435245415445204f52205245504c4143452046554e4354494f4e204558504552494d454e545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d206578706572696d656e745f747970655f70726f70657274795f747970657320657470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e657470745f6964203d20657470742e696420414e4420657470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a090909090909090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a4352454154452054524947474552204558504552494d454e545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e206578706572696d656e745f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f434544555245204558504552494d454e545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a200a202d2d2064617461207365740a435245415445204f52205245504c4143452046554e4354494f4e20444154415f5345545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f747970655f69642020434f44453b0a202020765f747970655f69645f70726f702020434f44453b0a424547494e0a2020206966204e45572e6d6174655f70726f705f6964204953204e4f54204e554c4c207468656e0a0909092d2d2066696e64206d6174657269616c2074797065206964206f66207468652070726f70657274792074797065200a09090973656c6563742070742e6d6174795f70726f705f696420696e746f20765f747970655f69645f70726f70200a090909202066726f6d20646174615f7365745f747970655f70726f70657274795f74797065732064737470742c2070726f70657274795f7479706573207074200a090909207768657265204e45572e64737470745f6964203d2064737470742e696420414e442064737470742e707274795f6964203d2070742e69643b0a09090a090909696620765f747970655f69645f70726f70204953204e4f54204e554c4c207468656e0a090909092d2d2066696e64206d6174657269616c2074797065206964206f6620746865206d6174657269616c20776869636820636f6e73697374732074686520656e7469747927732070726f70657274792076616c75650a0909090973656c65637420656e746974792e6d6174795f696420696e746f20765f747970655f6964200a09090909202066726f6d206d6174657269616c7320656e746974790a09090909207768657265204e45572e6d6174655f70726f705f6964203d20656e746974792e69643b0a09090909696620765f747970655f696420213d20765f747970655f69645f70726f70207468656e0a0909090909524149534520455843455054494f4e2027496e736572742f557064617465206f662070726f70657274792076616c7565207265666572656e63696e67206d6174657269616c202869643a202529206661696c65642c206173207265666572656e636564206d6174657269616c207479706520697320646966666572656e74207468616e2065787065637465642028696420252c2065787065637465642069643a2025292e272c200a090909090909090909090909204e45572e6d6174655f70726f705f69642c20765f747970655f69642c20765f747970655f69645f70726f703b0a09090909656e642069663b0a090909656e642069663b0a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220444154415f5345545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b204245464f524520494e53455254204f5220555044415445204f4e20646174615f7365745f70726f706572746965730a20202020464f52204541434820524f5720455845435554452050524f43454455524520444154415f5345545f50524f50455254595f574954485f4d4154455249414c5f444154415f545950455f434845434b28293b0a202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20507572706f73653a2043726561746520444546455252454420747269676765727320666f7220636865636b696e6720636f6e73697374656e6379206f662064656c6574696f6e2073746174652e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d207574696c6974792066756e6374696f6e2064657363726962696e6720612064656c6574696f6e0a0a435245415445204f52205245504c4143452046554e4354494f4e2064656c6574696f6e5f6465736372697074696f6e2864656c5f696420544543485f4944292052455455524e5320564152434841522041532024240a4445434c4152450a202064656c5f706572736f6e20564152434841523b0a202064656c5f6461746520564152434841523b0a202064656c5f726561736f6e20564152434841523b0a424547494e0a202053454c45435420702e6c6173745f6e616d65207c7c20272027207c7c20702e66697273745f6e616d65207c7c2027202827207c7c20702e656d61696c207c7c202729272c200a202020202020202020746f5f6368617228642e726567697374726174696f6e5f74696d657374616d702c2027595959592d4d4d2d44442048483a4d4d3a535327292c20642e726561736f6e200a20202020494e544f2064656c5f706572736f6e2c2064656c5f646174652c2064656c5f726561736f6e2046524f4d2064656c6574696f6e7320642c20706572736f6e732070200a20202020574845524520642e706572735f69645f72656769737465726572203d20702e696420414e4420642e6964203d2064656c5f69643b0a202052455455524e202764656c657465642062792027207c7c2064656c5f706572736f6e207c7c2027206f6e2027207c7c2064656c5f64617465207c7c2027207769746820726561736f6e3a202227207c7c2064656c5f726561736f6e207c7c202722273b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20312e2064617461207365740a2d2d2d206f6e20696e736572742f757064617465202d2064656c65746564206578706572696d656e74206f722073616d706c652063616e277420626520636f6e6e65637465640a2d2d2d2020202020202020202020202020202020202d20706172656e74732f6368696c6472656e2072656c6174696f6e7368697020737461797320756e6368616e676564200a0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f637265617465645f6f725f6d6f6469666965645f646174615f7365745f6f776e65725f69735f616c69766528292052455455524e5320747269676765722041532024240a4445434c4152450a096f776e65725f636f646509434f44453b0a096f776e65725f64656c5f696409544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0a20202d2d20636865636b2073616d706c650a2020494620284e45572e73616d705f6964204953204e4f54204e554c4c29205448454e0a20200953454c4543542064656c5f69642c20636f646520494e544f206f776e65725f64656c5f69642c206f776e65725f636f64650a202009202046524f4d2073616d706c6573200a20200920205748455245206964203d204e45572e73616d705f69643b0a202009494620286f776e65725f64656c5f6964204953204e4f54204e554c4c29205448454e200a090909524149534520455843455054494f4e202744617461205365742028436f64653a2025292063616e6e6f7420626520636f6e6e656374656420746f20612053616d706c652028436f64653a20252920252e272c200a090909202020202020202020202020202020204e45572e636f64652c206f776e65725f636f64652c2064656c6574696f6e5f6465736372697074696f6e286f776e65725f64656c5f6964293b0a0909454e442049463b0a09454e442049463b0a092d2d20636865636b206578706572696d656e740a2020494620284e45572e657870655f6964204953204e4f54204e554c4c29205448454e0a090953454c4543542064656c5f69642c20636f646520494e544f206f776e65725f64656c5f69642c206f776e65725f636f64650a092020202046524f4d206578706572696d656e7473200a09202020205748455245206964203d204e45572e657870655f69643b0a092020494620286f776e65725f64656c5f6964204953204e4f54204e554c4c29205448454e200a090909524149534520455843455054494f4e202744617461205365742028436f64653a2025292063616e6e6f7420626520636f6e6e656374656420746f20616e204578706572696d656e742028436f64653a20252920252e272c200a090909202020202020202020202020202020204e45572e636f64652c206f776e65725f636f64652c2064656c6574696f6e5f6465736372697074696f6e286f776e65725f64656c5f6964293b0a0909454e442049463b090a09454e442049463b090a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a20200a43524541544520434f4e53545241494e54205452494747455220636865636b5f637265617465645f6f725f6d6f6469666965645f646174615f7365745f6f776e65725f69735f616c697665200a09414654455220494e53455254204f5220555044415445204f4e20646174615f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f570a09455845435554452050524f43454455524520636865636b5f637265617465645f6f725f6d6f6469666965645f646174615f7365745f6f776e65725f69735f616c69766528293b0a090a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20322e2073616d706c650a2d2d2d206f6e20696e736572742f757064617465202d3e206578706572696d656e742063616e27742062652064656c6574656420756e6c657373207468652073616d706c652069732064656c657465640a2d2d2d2064656c6574696f6e200a2d2d2d2d3e20616c6c206469726563746c7920636f6e6e656374656420646174612073657473206e65656420746f2062652064656c657465640a2d2d2d2d3e20616c6c20636f6d706f6e656e747320616e64206368696c6472656e206e65656420746f2062652064656c657465640a0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f637265617465645f6f725f6d6f6469666965645f73616d706c655f6f776e65725f69735f616c69766528292052455455524e5320747269676765722041532024240a4445434c4152450a096f776e65725f636f646509434f44453b0a096f776e65725f64656c5f696409544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0a20202d2d20636865636b206578706572696d656e74202863616e27742062652064656c65746564290a2020494620284e45572e657870655f6964204953204e4f54204e554c4c29205448454e0a20200953454c4543542064656c5f69642c20636f646520494e544f206f776e65725f64656c5f69642c206f776e65725f636f64650a202009202046524f4d206578706572696d656e7473200a20200920205748455245206964203d204e45572e657870655f69643b0a202009494620286f776e65725f64656c5f6964204953204e4f54204e554c4c29205448454e200a090909524149534520455843455054494f4e202753616d706c652028436f64653a2025292063616e6e6f7420626520636f6e6e656374656420746f20616e204578706572696d656e742028436f64653a20252920252e272c200a2020200909202020202020202020202020202020204e45572e636f64652c206f776e65725f636f64652c2064656c6574696f6e5f6465736372697074696f6e286f776e65725f64656c5f6964293b0a0909454e442049463b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a20200a43524541544520434f4e53545241494e54205452494747455220636865636b5f637265617465645f6f725f6d6f6469666965645f73616d706c655f6f776e65725f69735f616c697665200a2020414654455220494e53455254204f5220555044415445204f4e2073616d706c65735f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f570a09455845435554452050524f43454455524520636865636b5f637265617465645f6f725f6d6f6469666965645f73616d706c655f6f776e65725f69735f616c69766528293b0a090a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f64656c6574696f6e28292052455455524e5320747269676765722041532024240a4445434c4152450a2020636f756e7465722020494e54454745523b0a424547494e0a09494620284f4c442e64656c5f6964204953204e4f54204e554c4c204f52204e45572e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0a20202d2d20616c6c206469726563746c7920636f6e6e656374656420646174612073657473206e65656420746f2062652064656c657465640a20202d2d20636865636b2064617461736574730a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d20646174610a092020574845524520646174612e73616d705f6964203d204e45572e696420414e4420646174612e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e202753616d706c652028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f66206974732064617461207365747320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a20202d2d20616c6c20636f6d706f6e656e7473206e65656420746f2062652064656c657465640a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d2073616d706c6573200a09202057484552452073616d706c65732e73616d705f69645f706172745f6f66203d204e45572e696420414e442073616d706c65732e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e202753616d706c652028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f662069747320636f6d706f6e656e742073616d706c657320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a20200a43524541544520434f4e53545241494e54205452494747455220636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f64656c6574696f6e200a2020414654455220555044415445204f4e2073616d706c65735f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f57200a09455845435554452050524f43454455524520636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f64656c6574696f6e28293b090a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d207570646174652073616d706c652072656c6174696f6e7368697073206f6e20726576657274200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e2070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f72656c6174696f6e736869707328292052455455524e5320747269676765722041532024240a4445434c4152450a202064656c69642020544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c204f52204f4c442e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d2053414d504c45535f414c4c207768657265206964203d204e45572e73616d706c655f69645f706172656e743b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d2053414d504c45535f414c4c207768657265206964203d204e45572e73616d706c655f69645f6368696c643b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f72656c6174696f6e7368697073200a20204245464f524520555044415445204f4e2073616d706c655f72656c6174696f6e73686970735f616c6c0a09464f52204541434820524f57200a09455845435554452050524f4345445552452070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f73616d706c655f72656c6174696f6e736869707328293b0a090a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2075706461746520646174617365742072656c6174696f6e7368697073206f6e20726576657274200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f646174615f7365745f72656c6174696f6e736869707328292052455455524e5320747269676765722041532024240a4445434c4152450a202064656c69642020544543485f49443b0a424547494e0a09494620284e45572e64656c5f6964204953204e4f54204e554c4c204f52204f4c442e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d20444154415f414c4c207768657265206964203d204e45572e646174615f69645f706172656e743b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0953454c4543542064656c5f696420494e544f2064656c69640a090946524f4d20444154415f414c4c207768657265206964203d204e45572e646174615f69645f6368696c643b0a094946202864656c6964204953204e4f54204e554c4c29205448454e0a09094e45572e64656c5f6964203d2064656c69643b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520545249474745522070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f646174615f7365745f72656c6174696f6e7368697073200a20204245464f524520555044415445204f4e20646174615f7365745f72656c6174696f6e73686970735f616c6c0a09464f52204541434820524f57200a09455845435554452050524f4345445552452070726573657276655f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f646174615f7365745f72656c6174696f6e736869707328293b0a090a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20332e206578706572696d656e740a2d2d2d2064656c6574696f6e202d3e20616c6c206469726563746c7920636f6e6e65637465642073616d706c657320616e6420646174612073657473206e65656420746f2062652064656c657465640a0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f6578706572696d656e745f64656c6574696f6e28292052455455524e5320747269676765722041532024240a4445434c4152450a2020636f756e7465722020494e54454745523b0a424547494e0a09494620284f4c442e64656c5f6964204953204e4f54204e554c4c204f52204e45572e64656c5f6964204953204e554c4c29205448454e0a090952455455524e204e45573b0a09454e442049463b0a090a20202d2d20636865636b2064617461736574730a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d20646174610a092020574845524520646174612e657870655f6964203d204e45572e696420414e4420646174612e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e20274578706572696d656e742028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f66206974732064617461207365747320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a092d2d20636865636b2073616d706c65730a0953454c45435420636f756e74282a2920494e544f20636f756e746572200a09202046524f4d2073616d706c6573200a09202057484552452073616d706c65732e657870655f6964203d204e45572e696420414e442073616d706c65732e64656c5f6964204953204e554c4c3b0a0949462028636f756e746572203e203029205448454e0a092020524149534520455843455054494f4e20274578706572696d656e742028436f64653a2025292064656c6574696f6e206661696c65642062656361757365206174206c65617374206f6e65206f66206974732073616d706c657320776173206e6f742064656c657465642e272c204e45572e636f64653b0a09454e442049463b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520434f4e53545241494e54205452494747455220636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f6578706572696d656e745f64656c6574696f6e200a2020414654455220555044415445204f4e206578706572696d656e74735f616c6c0a0944454645525241424c4520494e495449414c4c592044454645525245440a09464f52204541434820524f57200a09455845435554452050524f43454455524520636865636b5f64656c6574696f6e5f636f6e73697374656e63795f6f6e5f6578706572696d656e745f64656c6574696f6e28293b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a204372656174652066756e6374696f6e732f747269676765727320746f2076616c6964617465206461746120696e207461626c6520636f6e74656e745f636f706965732e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e20636f6e74656e745f636f706965735f756e697175656e6573735f636865636b28290a202052455455524e5320747269676765722041530a24424f4459240a424547494e0a20204e45572e6c6f636174696f6e5f756e697175655f636865636b203d204e45572e646174615f6964207c7c20272c27207c7c200a2020202020202020202020202020202020202020202020202020202020204e45572e65646d735f6964207c7c20272c27207c7c0a202020202020202020202020202020202020202020202020202020202020636f616c65736365284e45572e706174682c20272729207c7c20272c27207c7c200a202020202020202020202020202020202020202020202020202020202020636f616c65736365284e45572e6769745f636f6d6d69745f686173682c20272729207c7c20272c27207c7c200a202020202020202020202020202020202020202020202020202020202020636f616c65736365284e45572e6769745f7265706f7369746f72795f69642c20272729207c7c20272c27207c7c200a202020202020202020202020202020202020202020202020202020202020636f616c65736365284e45572e65787465726e616c5f636f64652c202727293b0a202052455455524e204e45573b0a454e443b0a24424f4459240a20204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220636f6e74656e745f636f706965735f756e697175656e6573735f636865636b0a20204245464f524520494e53455254204f52205550444154450a20204f4e20636f6e74656e745f636f706965730a2020464f52204541434820524f570a2020455845435554452050524f43454455524520636f6e74656e745f636f706965735f756e697175656e6573735f636865636b28293b0a0a20200a435245415445204f52205245504c4143452046554e4354494f4e20636f6e74656e745f636f706965735f6c6f636174696f6e5f747970655f636865636b28292052455455524e5320747269676765722041532024240a4445434c4152450a20202065646d735f616464726573735f747970652045444d535f414444524553535f545950453b0a202020696e64657820696e74656765723b0a424547494e0a0a20202073656c65637420706f736974696f6e28616464726573735f7479706520696e204e45572e6c6f636174696f6e5f74797065292c20616464726573735f7479706520696e746f20696e6465782c2065646d735f616464726573735f747970652066726f6d2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d730a2020202020207768657265206964203d204e45572e65646d735f69643b0a0a202020696620696e64657820213d2031207468656e0a202020202020524149534520455843455054494f4e2027496e736572742f55706461746520746f20636f6e74656e745f636f70696573206661696c65642e204c6f636174696f6e207479706520252c206275742065646d732e616464726573735f747970652025272c204e45572e6c6f636174696f6e5f747970652c2065646d735f616464726573735f747970653b0a202020656e642069663b0a0a20202052455455524e204e45573b0a0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220636f6e74656e745f636f706965735f6c6f636174696f6e5f747970655f636865636b200a20204245464f524520494e53455254204f5220555044415445200a20204f4e20636f6e74656e745f636f706965730a2020464f52204541434820524f57200a2020455845435554452050524f43454455524520636f6e74656e745f636f706965735f6c6f636174696f6e5f747970655f636865636b28293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2052756c657320666f722076696577730a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452052554c452073616d706c655f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c657320444f20494e5354454144200a20202020202020494e5345525420494e544f2073616d706c65735f616c6c20280a20202020202020202069642c0a20202020202020202066726f7a656e2c0a20202020202020202066726f7a656e5f666f725f636f6d702c200a20202020202020202066726f7a656e5f666f725f6368696c6472656e2c200a20202020202020202066726f7a656e5f666f725f706172656e74732c200a20202020202020202066726f7a656e5f666f725f646174612c0a202020202020202020636f64652c200a20202020202020202064656c5f69642c0a2020202020202020206f7269675f64656c2c0a202020202020202020657870655f69642c0a202020202020202020657870655f66726f7a656e2c0a20202020202020202070726f6a5f69642c0a20202020202020202070726f6a5f66726f7a656e2c0a2020202020202020206d6f64696669636174696f6e5f74696d657374616d702c0a2020202020202020207065726d5f69642c0a202020202020202020706572735f69645f726567697374657265722c200a202020202020202020706572735f69645f6d6f6469666965722c200a202020202020202020726567697374726174696f6e5f74696d657374616d702c200a20202020202020202073616d705f69645f706172745f6f662c0a202020202020202020636f6e745f66726f7a656e2c0a202020202020202020736174795f69642c200a20202020202020202073706163655f69642c0a20202020202020202073706163655f66726f7a656e2c0a20202020202020202076657273696f6e0a20202020202020292056414c55455320280a2020202020202020204e45572e69642c0a2020202020202020204e45572e66726f7a656e2c0a2020202020202020204e45572e66726f7a656e5f666f725f636f6d702c200a2020202020202020204e45572e66726f7a656e5f666f725f6368696c6472656e2c200a2020202020202020204e45572e66726f7a656e5f666f725f706172656e74732c200a2020202020202020204e45572e66726f7a656e5f666f725f646174612c0a2020202020202020204e45572e636f64652c200a2020202020202020204e45572e64656c5f69642c0a2020202020202020204e45572e6f7269675f64656c2c0a2020202020202020204e45572e657870655f69642c0a2020202020202020204e45572e657870655f66726f7a656e2c0a2020202020202020204e45572e70726f6a5f69642c0a2020202020202020204e45572e70726f6a5f66726f7a656e2c0a2020202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a2020202020202020204e45572e7065726d5f69642c0a2020202020202020204e45572e706572735f69645f726567697374657265722c200a2020202020202020204e45572e706572735f69645f6d6f6469666965722c200a2020202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c200a2020202020202020204e45572e73616d705f69645f706172745f6f662c0a2020202020202020204e45572e636f6e745f66726f7a656e2c0a2020202020202020204e45572e736174795f69642c200a2020202020202020204e45572e73706163655f69642c0a2020202020202020204e45572e73706163655f66726f7a656e2c0a2020202020202020204e45572e76657273696f6e0a20202020202020293b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c657320444f20494e5354454144200a202020202020205550444154452073616d706c65735f616c6c0a2020202020202020202053455420636f6465203d204e45572e636f64652c0a202020202020202020202020202066726f7a656e203d204e45572e66726f7a656e2c0a202020202020202020202020202066726f7a656e5f666f725f636f6d70203d204e45572e66726f7a656e5f666f725f636f6d702c200a202020202020202020202020202066726f7a656e5f666f725f6368696c6472656e203d204e45572e66726f7a656e5f666f725f6368696c6472656e2c200a202020202020202020202020202066726f7a656e5f666f725f706172656e7473203d204e45572e66726f7a656e5f666f725f706172656e74732c200a202020202020202020202020202066726f7a656e5f666f725f64617461203d204e45572e66726f7a656e5f666f725f646174612c0a202020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a2020202020202020202020202020657870655f6964203d204e45572e657870655f69642c0a2020202020202020202020202020657870655f66726f7a656e203d204e45572e657870655f66726f7a656e2c0a202020202020202020202020202070726f6a5f6964203d204e45572e70726f6a5f69642c0a202020202020202020202020202070726f6a5f66726f7a656e203d204e45572e70726f6a5f66726f7a656e2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a20202020202020202020202020207065726d5f6964203d204e45572e7065726d5f69642c0a2020202020202020202020202020706572735f69645f72656769737465726572203d204e45572e706572735f69645f726567697374657265722c0a2020202020202020202020202020706572735f69645f6d6f646966696572203d204e45572e706572735f69645f6d6f6469666965722c0a2020202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020202073616d705f69645f706172745f6f66203d204e45572e73616d705f69645f706172745f6f662c0a2020202020202020202020202020636f6e745f66726f7a656e203d204e45572e636f6e745f66726f7a656e2c0a2020202020202020202020202020736174795f6964203d204e45572e736174795f69642c0a202020202020202020202020202073706163655f6964203d204e45572e73706163655f69642c0a202020202020202020202020202073706163655f66726f7a656e203d204e45572e73706163655f66726f7a656e2c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c657320444f20494e53544541440a2020202020202044454c4554452046524f4d2073616d706c65735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f64656c657465645f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f64656c6574656420444f20494e53544541440a202020202020205550444154452073616d706c65735f616c6c0a202020202020202020205345542064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f64656c657465645f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c65735f64656c6574656420444f20494e53544541440a2020202020202044454c4554452046524f4d2073616d706c65735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a20202020202020202020202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d206578706572696d656e74202d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452052554c45206578706572696d656e745f696e736572742041530a20204f4e20494e5345525420544f206578706572696d656e747320444f20494e5354454144200a2020202020494e5345525420494e544f206578706572696d656e74735f616c6c20280a2020202020202069642c0a2020202020202066726f7a656e2c0a2020202020202066726f7a656e5f666f725f73616d702c0a2020202020202066726f7a656e5f666f725f646174612c0a20202020202020636f64652c200a2020202020202064656c5f69642c0a202020202020206f7269675f64656c2c0a20202020202020657874795f69642c200a2020202020202069735f7075626c69632c0a202020202020206d6f64696669636174696f6e5f74696d657374616d702c0a202020202020207065726d5f69642c0a20202020202020706572735f69645f726567697374657265722c200a20202020202020706572735f69645f6d6f6469666965722c200a2020202020202070726f6a5f69642c0a2020202020202070726f6a5f66726f7a656e2c0a20202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202076657273696f6e0a2020202020292056414c55455320280a202020202020204e45572e69642c0a202020202020204e45572e66726f7a656e2c0a202020202020204e45572e66726f7a656e5f666f725f73616d702c0a202020202020204e45572e66726f7a656e5f666f725f646174612c0a202020202020204e45572e636f64652c200a202020202020204e45572e64656c5f69642c0a202020202020204e45572e6f7269675f64656c2c0a202020202020204e45572e657874795f69642c200a202020202020204e45572e69735f7075626c69632c0a202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020204e45572e7065726d5f69642c0a202020202020204e45572e706572735f69645f726567697374657265722c200a202020202020204e45572e706572735f69645f6d6f6469666965722c200a202020202020204e45572e70726f6a5f69642c0a202020202020204e45572e70726f6a5f66726f7a656e2c0a202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020204e45572e76657273696f6e0a2020202020293b0a20202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e747320444f20494e5354454144200a20202020202020555044415445206578706572696d656e74735f616c6c0a2020202020202020202053455420636f6465203d204e45572e636f64652c0a202020202020202020202020202066726f7a656e203d204e45572e66726f7a656e2c0a202020202020202020202020202066726f7a656e5f666f725f73616d70203d204e45572e66726f7a656e5f666f725f73616d702c0a202020202020202020202020202066726f7a656e5f666f725f64617461203d204e45572e66726f7a656e5f666f725f646174612c0a202020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a2020202020202020202020202020657874795f6964203d204e45572e657874795f69642c0a202020202020202020202020202069735f7075626c6963203d204e45572e69735f7075626c69632c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a20202020202020202020202020207065726d5f6964203d204e45572e7065726d5f69642c0a2020202020202020202020202020706572735f69645f72656769737465726572203d204e45572e706572735f69645f726567697374657265722c0a2020202020202020202020202020706572735f69645f6d6f646966696572203d204e45572e706572735f69645f6d6f6469666965722c0a202020202020202020202020202070726f6a5f6964203d204e45572e70726f6a5f69642c0a202020202020202020202020202070726f6a5f66726f7a656e203d204e45572e70726f6a5f66726f7a656e2c0a2020202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e747320444f20494e53544541440a2020202020202044454c4554452046524f4d206578706572696d656e74735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a2020202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e74735f64656c657465645f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e74735f64656c6574656420444f20494e5354454144200a20202020202020555044415445206578706572696d656e74735f616c6c0a202020202020202020205345542064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e74735f64656c657465645f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e74735f64656c6574656420444f20494e53544541440a2020202020202044454c4554452046524f4d206578706572696d656e74735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a2020202020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202020200a2d2d2d2d2d2d2d2d2d2d0a2d2d2064617461202d2d0a2d2d2d2d2d2d2d2d2d2d0a2020202020200a0a435245415445204f52205245504c4143452052554c4520646174615f696e736572742041530a20204f4e20494e5345525420544f206461746120444f20494e5354454144200a2020202020494e5345525420494e544f20646174615f616c6c20280a2020202020202069642c0a2020202020202066726f7a656e2c0a2020202020202066726f7a656e5f666f725f6368696c6472656e2c200a2020202020202066726f7a656e5f666f725f706172656e74732c200a2020202020202066726f7a656e5f666f725f636f6d70732c200a2020202020202066726f7a656e5f666f725f636f6e74732c0a20202020202020636f64652c200a2020202020202064656c5f69642c0a202020202020206f7269675f64656c2c0a20202020202020657870655f69642c0a20202020202020657870655f66726f7a656e2c0a20202020202020646173745f69642c0a20202020202020646174615f70726f64756365725f636f64652c0a20202020202020647374795f69642c0a2020202020202069735f646572697665642c0a2020202020202069735f76616c69642c0a202020202020206d6f64696669636174696f6e5f74696d657374616d702c0a202020202020206163636573735f74696d657374616d702c0a20202020202020706572735f69645f726567697374657265722c0a20202020202020706572735f69645f6d6f6469666965722c0a2020202020202070726f64756374696f6e5f74696d657374616d702c0a20202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202073616d705f69642c0a2020202020202073616d705f66726f7a656e2c0a2020202020202076657273696f6e2c0a20202020202020646174615f7365745f6b696e640a2020202020292056414c55455320280a202020202020204e45572e69642c0a202020202020204e45572e66726f7a656e2c0a202020202020204e45572e66726f7a656e5f666f725f6368696c6472656e2c200a202020202020204e45572e66726f7a656e5f666f725f706172656e74732c200a202020202020204e45572e66726f7a656e5f666f725f636f6d70732c200a202020202020204e45572e66726f7a656e5f666f725f636f6e74732c0a202020202020204e45572e636f64652c200a202020202020204e45572e64656c5f69642c200a202020202020204e45572e6f7269675f64656c2c0a202020202020204e45572e657870655f69642c0a202020202020204e45572e657870655f66726f7a656e2c0a202020202020204e45572e646173745f69642c0a202020202020204e45572e646174615f70726f64756365725f636f64652c0a202020202020204e45572e647374795f69642c0a202020202020204e45572e69735f646572697665642c200a202020202020204e45572e69735f76616c69642c0a202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020204e45572e6163636573735f74696d657374616d702c0a202020202020204e45572e706572735f69645f726567697374657265722c0a202020202020204e45572e706572735f69645f6d6f6469666965722c0a202020202020204e45572e70726f64756374696f6e5f74696d657374616d702c0a202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020204e45572e73616d705f69642c0a202020202020204e45572e73616d705f66726f7a656e2c0a202020202020204e45572e76657273696f6e2c0a202020202020204e45572e646174615f7365745f6b696e640a2020202020293b0a20202020200a435245415445204f52205245504c4143452052554c4520646174615f7570646174652041530a202020204f4e2055504441544520544f206461746120444f20494e5354454144200a2020202020202055504441544520646174615f616c6c0a2020202020202020202053455420636f6465203d204e45572e636f64652c0a202020202020202020202020202066726f7a656e203d204e45572e66726f7a656e2c0a202020202020202020202020202066726f7a656e5f666f725f6368696c6472656e203d204e45572e66726f7a656e5f666f725f6368696c6472656e2c200a202020202020202020202020202066726f7a656e5f666f725f706172656e7473203d204e45572e66726f7a656e5f666f725f706172656e74732c200a202020202020202020202020202066726f7a656e5f666f725f636f6d7073203d204e45572e66726f7a656e5f666f725f636f6d70732c200a202020202020202020202020202066726f7a656e5f666f725f636f6e7473203d204e45572e66726f7a656e5f666f725f636f6e74732c0a202020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a2020202020202020202020202020657870655f6964203d204e45572e657870655f69642c0a2020202020202020202020202020657870655f66726f7a656e203d204e45572e657870655f66726f7a656e2c0a2020202020202020202020202020646173745f6964203d204e45572e646173745f69642c0a2020202020202020202020202020646174615f70726f64756365725f636f6465203d204e45572e646174615f70726f64756365725f636f64652c0a2020202020202020202020202020647374795f6964203d204e45572e647374795f69642c0a202020202020202020202020202069735f64657269766564203d204e45572e69735f646572697665642c0a202020202020202020202020202069735f76616c6964203d204e45572e69735f76616c69642c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a20202020202020202020202020206163636573735f74696d657374616d70203d204e45572e6163636573735f74696d657374616d702c0a2020202020202020202020202020706572735f69645f72656769737465726572203d204e45572e706572735f69645f726567697374657265722c0a2020202020202020202020202020706572735f69645f6d6f646966696572203d204e45572e706572735f69645f6d6f6469666965722c0a202020202020202020202020202070726f64756374696f6e5f74696d657374616d70203d204e45572e70726f64756374696f6e5f74696d657374616d702c0a2020202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020202073616d705f6964203d204e45572e73616d705f69642c0a202020202020202020202020202073616d705f66726f7a656e203d204e45572e73616d705f66726f7a656e2c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e2c0a2020202020202020202020202020646174615f7365745f6b696e64203d204e45572e646174615f7365745f6b696e640a202020202020205748455245206964203d204e45572e69643b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c4520646174615f616c6c2041530a202020204f4e2044454c45544520544f206461746120444f20494e53544541440a2020202020202044454c4554452046524f4d20646174615f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c4520646174615f64656c657465645f7570646174652041530a202020204f4e2055504441544520544f20646174615f64656c6574656420444f20494e5354454144200a2020202020202055504441544520646174615f616c6c0a202020202020202020205345542064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020206f7269675f64656c203d204e45572e6f7269675f64656c2c0a20202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d702c0a202020202020202020202020202076657273696f6e203d204e45572e76657273696f6e0a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c4520646174615f64656c657465645f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f64656c6574656420444f20494e53544541440a2020202020202044454c4554452046524f4d20646174615f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b2020202020202020202020202020200a0a0a2d2d206c696e6b5f64617461206d75737420726566657220746f2061206461746120736574206f66206b696e64204c494e4b0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f646174615f7365745f6b696e645f6c696e6b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020206b696e6420444154415f5345545f4b494e443b0a424547494e0a2020202053454c45435420646174615f7365745f6b696e6420494e544f206b696e640a202020202020202046524f4d20646174615f616c6c200a20202020202020205748455245206964203d204e45572e69643b0a2020202020202020494620286b696e64203c3e20274c494e4b2729205448454e200a202020202020202020202020524149534520455843455054494f4e20274c696e6b20646174612028446174612053657420436f64653a202529206d757374207265666572656e63652061206461746120736574206f66206b696e64204c494e4b202869732025292e272c200a202020202020202020202020202020202020202020202020202020204e45572e69642c206b696e643b0a2020202020202020454e442049463b0a2020202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520434f4e53545241494e54205452494747455220636865636b5f646174615f7365745f6b696e645f6c696e6b200a20202020414654455220494e53455254204f5220555044415445204f4e206c696e6b5f646174610a2020202044454645525241424c4520494e495449414c4c592044454645525245440a20202020464f52204541434820524f570a20202020455845435554452050524f43454455524520636865636b5f646174615f7365745f6b696e645f6c696e6b28293b0a0a2d2d2065787465726e616c5f64617461206d75737420726566657220746f2061206461746120736574206f66206b696e6420504859534943414c0a435245415445204f52205245504c4143452046554e4354494f4e20636865636b5f646174615f7365745f6b696e645f706879736963616c28292052455455524e5320747269676765722041532024240a4445434c4152450a202020206b696e6420444154415f5345545f4b494e443b0a424547494e0a2020202053454c45435420646174615f7365745f6b696e6420494e544f206b696e640a202020202020202046524f4d20646174615f616c6c200a20202020202020205748455245206964203d204e45572e69643b0a2020202020202020494620286b696e64203c3e2027504859534943414c2729205448454e200a202020202020202020202020524149534520455843455054494f4e202745787465726e616c20646174612028446174612053657420436f64653a202529206d757374207265666572656e63652061206461746120736574206f66206b696e6420504859534943414c202869732025292e272c200a202020202020202020202020202020202020202020202020202020204e45572e69642c206b696e643b0a2020202020202020454e442049463b0a2020202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a43524541544520434f4e53545241494e54205452494747455220636865636b5f646174615f7365745f6b696e645f706879736963616c200a20202020414654455220494e53455254204f5220555044415445204f4e2065787465726e616c5f646174610a2020202044454645525241424c4520494e495449414c4c592044454645525245440a20202020464f52204541434820524f570a20202020455845435554452050524f43454455524520636865636b5f646174615f7365745f6b696e645f706879736963616c28293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2052756c657320666f722070726f7065727469657320686973746f72790a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a2d2d204d6174657269616c2050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c45206d6174657269616c5f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f206d6174657269616c5f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f200a20202020202020494e5345525420494e544f206d6174657269616c5f70726f706572746965735f686973746f727920280a20202020202020202049442c200a2020202020202020204d4154455f49442c200a2020202020202020204d5450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274d4154455249414c5f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e4d4154455f49442c200a2020202020202020204f4c442e4d5450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c45206d6174657269616c5f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f206d6174657269616c5f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c0a20202020444f20414c534f200a20202020202020494e5345525420494e544f206d6174657269616c5f70726f706572746965735f686973746f727920280a20202020202020202049442c200a2020202020202020204d4154455f49442c200a2020202020202020204d5450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274d4154455249414c5f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e4d4154455f49442c200a2020202020202020204f4c442e4d5450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d204578706572696d656e742050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e745f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f200a20202020202020494e5345525420494e544f206578706572696d656e745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a202020202020202020455850455f49442c0a202020202020202020455450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e455850455f49442c200a2020202020202020204f4c442e455450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e745f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c0a20202020444f20414c534f200a20202020202020494e5345525420494e544f206578706572696d656e745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a202020202020202020455850455f49442c0a202020202020202020455450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e455850455f49442c200a2020202020202020204f4c442e455450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d2053616d706c652050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c452073616d706c655f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f70726f706572746965730a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f0a20202020202020494e5345525420494e544f2073616d706c655f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202053414d505f49442c0a202020202020202020535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e53414d505f49442c200a2020202020202020204f4c442e535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c655f70726f70657274696573200a2020202057484552452028284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c290a09202020414e44202853454c4543542044454c5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204f4c442e53414d505f494429204953204e554c4c0a2020202020444f20414c534f0a20202020202020494e5345525420494e544f2073616d706c655f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202053414d505f49442c0a202020202020202020535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e53414d505f49442c200a2020202020202020204f4c442e535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d2044617461205365742050726f70657274696573202d2d0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f70726f706572746965735f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f70726f70657274696573200a20202020574845524520284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c786566626662642720414e44204f4c442e56414c554520213d204e45572e56414c554529200a20202020202020204f5220284f4c442e435654455f4944204953204e4f54204e554c4c20414e44204f4c442e435654455f494420213d204e45572e435654455f494429200a20202020202020204f5220284f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c20414e44204f4c442e4d4154455f50524f505f494420213d204e45572e4d4154455f50524f505f4944290a20202020444f20414c534f0a20202020202020494e5345525420494e544f20646174615f7365745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202044535f49442c0a20202020202020202044535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e44535f49442c200a2020202020202020204f4c442e44535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f70726f706572746965735f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f7365745f70726f70657274696573200a2020202057484552452028284f4c442e56414c5545204953204e4f54204e554c4c20414e44206465636f6465287265706c61636528737562737472696e67284f4c442e76616c75652066726f6d203120666f722031292c20275c272c20275c5c27292c2027657363617065272920213d2045275c5c7865666266626427290a20202020202020204f52204f4c442e435654455f4944204953204e4f54204e554c4c200a20202020202020204f52204f4c442e4d4154455f50524f505f4944204953204e4f54204e554c4c290a09202020414e44202853454c4543542044454c5f49442046524f4d20444154415f414c4c205748455245204944203d204f4c442e44535f494429204953204e554c4c0a20202020444f20414c534f0a20202020202020494e5345525420494e544f20646174615f7365745f70726f706572746965735f686973746f727920280a20202020202020202049442c200a20202020202020202044535f49442c0a20202020202020202044535450545f49442c200a20202020202020202056414c55452c200a202020202020202020564f434142554c4152595f5445524d2c0a2020202020202020204d4154455249414c2c200a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202056414c49445f554e54494c5f54494d455354414d50200a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f50524f50455254595f49445f53455127292c200a2020202020202020204f4c442e44535f49442c200a2020202020202020204f4c442e44535450545f49442c200a2020202020202020204f4c442e56414c55452c200a2020202020202020202873656c6563742028742e636f6465207c7c2027205b27207c7c20762e636f6465207c7c20275d27292066726f6d20636f6e74726f6c6c65645f766f636162756c6172795f7465726d732061732074206a6f696e20636f6e74726f6c6c65645f766f636162756c61726965732061732076206f6e20742e636f766f5f6964203d20762e696420776865726520742e6964203d204f4c442e435654455f4944292c0a2020202020202020202873656c65637420286d2e636f6465207c7c2027205b27207c7c206d742e636f6465207c7c20275d27292066726f6d206d6174657269616c73206173206d206a6f696e206d6174657269616c5f7479706573206173206d74206f6e206d2e6d6174795f6964203d206d742e6964207768657265206d2e6964203d204f4c442e4d4154455f50524f505f4944292c0a2020202020202020204f4c442e504552535f49445f415554484f522c0a2020202020202020204f4c442e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202063757272656e745f74696d657374616d700a20202020202020293b0a202020202020200a2d2d20456e64206f662072756c657320666f722070726f7065727469657320686973746f72790a435245415445204f52205245504c4143452052554c4520646174615f7365745f72656c6174696f6e73686970735f696e736572742041530a202020204f4e20494e5345525420544f20646174615f7365745f72656c6174696f6e736869707320444f20494e5354454144200a20202020202020494e5345525420494e544f20646174615f7365745f72656c6174696f6e73686970735f616c6c20280a202020202020202020646174615f69645f706172656e742c0a202020202020202020706172656e745f66726f7a656e2c0a202020202020202020636f6e745f66726f7a656e2c0a202020202020202020646174615f69645f6368696c642c0a2020202020202020206368696c645f66726f7a656e2c0a202020202020202020636f6d705f66726f7a656e2c0a202020202020202020706572735f69645f617574686f722c0a20202020202020202072656c6174696f6e736869705f69642c0a2020202020202020206f7264696e616c2c0a202020202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202020206d6f64696669636174696f6e5f74696d657374616d700a20202020202020292056414c55455320280a2020202020202020204e45572e646174615f69645f706172656e742c0a2020202020202020204e45572e706172656e745f66726f7a656e2c0a2020202020202020204e45572e636f6e745f66726f7a656e2c0a2020202020202020204e45572e646174615f69645f6368696c642c0a2020202020202020204e45572e6368696c645f66726f7a656e2c0a2020202020202020204e45572e636f6d705f66726f7a656e2c2020200a2020202020202020204e45572e706572735f69645f617574686f722c0a2020202020202020204e45572e72656c6174696f6e736869705f69642c0a2020202020202020204e45572e6f7264696e616c2c0a2020202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a2020202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d700a20202020202020293b0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f72656c6174696f6e73686970735f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e736869707320444f20494e5354454144200a2020202020202055504441544520646174615f7365745f72656c6174696f6e73686970735f616c6c0a20202020202020202020534554200a202020202020202020202020646174615f69645f706172656e74203d204e45572e646174615f69645f706172656e742c0a202020202020202020202020706172656e745f66726f7a656e203d204e45572e706172656e745f66726f7a656e2c0a202020202020202020202020636f6e745f66726f7a656e203d204e45572e636f6e745f66726f7a656e2c0a202020202020202020202020646174615f69645f6368696c64203d204e45572e646174615f69645f6368696c642c0a2020202020202020202020206368696c645f66726f7a656e203d204e45572e6368696c645f66726f7a656e2c0a202020202020202020202020636f6d705f66726f7a656e203d204e45572e636f6d705f66726f7a656e2c0a20202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69642c0a2020202020202020202020206f7264696e616c203d204e45572e6f7264696e616c2c0a202020202020202020202020706572735f69645f617574686f72203d204e45572e706572735f69645f617574686f722c0a202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a2020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d700a20202020202020202020574845524520646174615f69645f706172656e74203d204e45572e646174615f69645f706172656e7420616e6420646174615f69645f6368696c64203d204e45572e646174615f69645f6368696c64200a20202020202020202020202020202020616e642072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69643b0a0a435245415445204f52205245504c4143452052554c4520646174615f7365745f72656c6174696f6e73686970735f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f7365745f72656c6174696f6e736869707320444f20494e53544541440a2020202020202044454c4554452046524f4d20646174615f7365745f72656c6174696f6e73686970735f616c6c0a2020202020202020202020202020574845524520646174615f69645f706172656e74203d204f4c442e646174615f69645f706172656e7420616e6420646174615f69645f6368696c64203d204f4c442e646174615f69645f6368696c640a2020202020202020202020202020202020202020616e642072656c6174696f6e736869705f6964203d204f4c442e72656c6174696f6e736869705f69643b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f72656c6174696f6e73686970735f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c655f72656c6174696f6e736869707320444f20494e5354454144200a20202020202020494e5345525420494e544f2073616d706c655f72656c6174696f6e73686970735f616c6c20280a20202020202020202069642c200a20202020202020202073616d706c655f69645f706172656e742c0a202020202020202020706172656e745f66726f7a656e2c0a20202020202020202072656c6174696f6e736869705f69642c0a20202020202020202073616d706c655f69645f6368696c642c0a2020202020202020206368696c645f66726f7a656e2c0a202020202020202020706572735f69645f617574686f722c0a202020202020202020726567697374726174696f6e5f74696d657374616d702c0a2020202020202020206d6f64696669636174696f6e5f74696d657374616d700a20202020202020292056414c55455320280a2020202020202020204e45572e69642c200a2020202020202020204e45572e73616d706c655f69645f706172656e742c0a2020202020202020204e45572e706172656e745f66726f7a656e2c0a2020202020202020204e45572e72656c6174696f6e736869705f69642c0a2020202020202020204e45572e73616d706c655f69645f6368696c642c0a2020202020202020204e45572e6368696c645f66726f7a656e2c0a2020202020202020204e45572e706572735f69645f617574686f722c0a2020202020202020204e45572e726567697374726174696f6e5f74696d657374616d702c0a2020202020202020204e45572e6d6f64696669636174696f6e5f74696d657374616d700a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f72656c6174696f6e73686970735f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f72656c6174696f6e736869707320444f20494e5354454144200a202020202020205550444154452073616d706c655f72656c6174696f6e73686970735f616c6c0a20202020202020202020534554200a2020202020202020202020202073616d706c655f69645f706172656e74203d204e45572e73616d706c655f69645f706172656e742c0a20202020202020202020202020706172656e745f66726f7a656e203d204e45572e706172656e745f66726f7a656e2c0a2020202020202020202020202072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69642c0a2020202020202020202020202073616d706c655f69645f6368696c64203d204e45572e73616d706c655f69645f6368696c642c0a202020202020202020202020206368696c645f66726f7a656e203d204e45572e6368696c645f66726f7a656e2c0a2020202020202020202020202064656c5f6964203d204e45572e64656c5f69642c0a20202020202020202020202020706572735f69645f617574686f72203d204e45572e706572735f69645f617574686f722c0a20202020202020202020202020726567697374726174696f6e5f74696d657374616d70203d204e45572e726567697374726174696f6e5f74696d657374616d702c0a202020202020202020202020206d6f64696669636174696f6e5f74696d657374616d70203d204e45572e6d6f64696669636174696f6e5f74696d657374616d700a202020202020202020205748455245206964203d204e45572e69643b0a20202020200a435245415445204f52205245504c4143452052554c452073616d706c655f72656c6174696f6e73686970735f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c655f72656c6174696f6e736869707320444f20494e53544541440a2020202020202044454c4554452046524f4d2073616d706c655f72656c6174696f6e73686970735f616c6c0a20202020202020202020202020205748455245206964203d204f4c442e69643b0a0a435245415445204f52205245504c4143452052554c45204d45544150524f4a4543545f41535349474e4d454e54535f494e534552542041530a202020204f4e20494e5345525420544f204d45544150524f4a4543545f41535349474e4d454e545320444f20494e5354454144200a20202020202020494e5345525420494e544f204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20280a20202020202020202049442c200a2020202020202020204d4550525f49442c0a202020202020202020455850455f49442c0a09090920202053414d505f49442c0a090909202020444154415f49442c0a0909092020204d4154455f49442c0a09090920202044454c5f49442c0a0909092020204352454154494f4e5f444154450a20202020202020292056414c55455320280a2020202020202020204e45572e49442c200a2020202020202020204e45572e4d4550525f49442c0a2020202020202020204e45572e455850455f49442c0a0909092020204e45572e53414d505f49442c0a0909092020204e45572e444154415f49442c0a0909092020204e45572e4d4154455f49442c0a0909092020204e45572e44454c5f49442c0a0909092020204e45572e4352454154494f4e5f444154450a20202020202020293b0a0a435245415445204f52205245504c4143452052554c45204d45544150524f4a4543545f41535349474e4d454e54535f5550444154452041530a202020204f4e2055504441544520544f204d45544150524f4a4543545f41535349474e4d454e545320444f20494e5354454144200a20202020202020555044415445204d45544150524f4a4543545f41535349474e4d454e54535f414c4c0a20202020202020202020534554200a0909092020202020204944203d204e45572e49442c200a20202020202020202009094d4550525f4944203d204e45572e4d4550525f49442c0a2020202020202020200909455850455f4944203d204e45572e455850455f49442c0a090909202020090953414d505f4944203d204e45572e53414d505f49442c0a0909092020200909444154415f4944203d204e45572e444154415f49442c0a09090920202009094d4154455f4944203d204e45572e4d4154455f49442c0a090909202020090944454c5f4944203d204e45572e44454c5f49442c0a09090920202009094352454154494f4e5f44415445203d204e45572e4352454154494f4e5f444154450a202020202020202020205748455245204944203d204e45572e49443b0a202020202020202020200a435245415445204f52205245504c4143452052554c45204d45544150524f4a4543545f41535349474e4d454e54535f44454c4554452041530a202020204f4e2044454c45544520544f204d45544150524f4a4543545f41535349474e4d454e545320444f20494e53544541440a2020202020202044454c4554452046524f4d204d45544150524f4a4543545f41535349474e4d454e54535f414c4c0a202020202020202020205748455245204944203d204f4c442e49443b0a20202020202020202020202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2052756c657320666f722072656c6174696f6e736869707320686973746f72790a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a2d2d2073616d706c65202d3e206578706572696d656e740a0a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e455850455f494420213d204e45572e455850455f4944204f52204f4c442e455850455f4944204953204e554c4c2920414e44204e45572e455850455f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e455850455f4944204953204e4f54204e554c4c20414e44204e45572e455850455f4944204953204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e455850455f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c452073616d706c655f6578706572696d656e745f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e455850455f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f200a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020200a2d2d20636f6e7461696e65722073616d706c65730a202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e53414d505f49445f504152545f4f4620213d204e45572e53414d505f49445f504152545f4f46204f52204f4c442e53414d505f49445f504152545f4f46204953204e554c4c2920414e44204e45572e53414d505f49445f504152545f4f46204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e455227290a20202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e454427293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a20202020202020202027434f4e5441494e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a20202020202020202027434f4e5441494e4544272c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f49445f504152545f4f46292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e53414d505f49445f504152545f4f46204953204e4f54204e554c4c20414e44204e45572e53414d505f49445f504152545f4f46204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e455227290a20202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e454427293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e53414d505f49445f504152545f4f46204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a20202020202020202027434f4e5441494e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a20202020202020202027434f4e5441494e4544272c200a2020202020202020204e45572e53414d505f49445f504152545f4f462c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f49445f504152545f4f46292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c452073616d706c655f636f6e7461696e65725f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e53414d505f49445f504152545f4f46204953204e4f54204e554c4c0a20202020202020444f20414c534f200a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f49445f504152545f4f4620414e442053414d505f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c20414e442052454c4154494f4e5f54595045203d2027434f4e5441494e4552273b0a0a2d2d2064617461736574202d3e2065706572696d656e740a0a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a20202020574845524520284f4c442e455850455f494420213d204e45572e455850455f4944204f52204f4c442e53414d505f4944204953204e4f54204e554c4c2920414e44204e45572e53414d505f4944204953204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a202020205748455245204f4c442e53414d505f4944204953204e554c4c20414e44204e45572e53414d505f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f444154415f4944203d204f4c442e494420414e4420455850455f4944203d204f4c442e455850455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f696e736572742041530a202020204f4e20494e5345525420544f20646174615f616c6c200a202020205748455245204e45572e455850455f4944204953204e4f54204e554c4c20414e44204e45572e53414d505f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e455850455f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e455850455f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d204558504552494d454e54535f414c4c205748455245204944203d204e45572e455850455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c4520646174617365745f6578706572696d656e745f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f616c6c200a202020205748455245204f4c442e455850455f4944204953204e4f54204e554c4c20414e44204f4c442e53414d505f4944204953204e554c4c0a20202020202020444f20414c534f200a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e455850455f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a0a2d2d2064617461736574202d3e2073616d706c650a0a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a20202020574845524520284f4c442e53414d505f494420213d204e45572e53414d505f4944204f52204f4c442e53414d505f4944204953204e554c4c2920414e44204e45572e53414d505f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53414d505f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f20646174615f616c6c200a202020205748455245204f4c442e53414d505f4944204953204e4f54204e554c4c20414e44204e45572e53414d505f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a2020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f444154415f4944203d204f4c442e494420414e442053414d505f4944203d204f4c442e53414d505f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f696e736572742041530a202020204f4e20494e5345525420544f20646174615f616c6c200a202020205748455245204e45572e53414d505f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020444154415f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e53414d505f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e434f44452c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f444154415f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053414d505f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53414d505f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d2053414d504c45535f414c4c205748455245204944203d204e45572e53414d505f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c4520646174617365745f73616d706c655f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f616c6c200a202020205748455245204f4c442e53414d505f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f200a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e53414d505f494420414e4420444154415f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a0a2d2d2064617461207365742072656c6174696f6e736869700a0a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f696e736572742041530a202020204f4e20494e5345525420544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a20202020202020293b0a0a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f64656c6574652041530a202020204f4e2044454c45544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e444154415f49445f504152454e54200a202020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f4348494c440a202020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a202020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f444154415f4944203d204f4c442e444154415f49445f4348494c44200a2020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f504152454e54200a2020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a2020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c20414e44204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e444154415f49445f504152454e54200a202020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f4348494c440a202020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a202020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f444154415f4944203d204f4c442e444154415f49445f4348494c44200a2020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f504152454e54200a2020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a2020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f74726173685f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e4f54204e554c4c20414e44204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a20202020202020202055504441544520444154415f5345545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f444154415f4944203d204f4c442e444154415f49445f504152454e54200a202020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f4348494c440a202020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a202020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f444154415f4944203d204f4c442e444154415f49445f4348494c44200a2020202020202020202020202020202020414e4420444154415f4944203d204f4c442e444154415f49445f504152454e54200a2020202020202020202020202020202020414e442052454c4154494f4e5f54595045203d202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204f4c442e52454c4154494f4e534849505f4944290a2020202020202020202020202020202020414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c4520646174615f72656c6174696f6e736869705f74726173685f7265766572745f7570646174652041530a202020204f4e2055504441544520544f20646174615f7365745f72656c6174696f6e73686970735f616c6c0a202020205748455245204f4c442e44454c5f4944204953204e4f54204e554c4c20414e44204e45572e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420555050455228504152454e545f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a202020202020202020494e5345525420494e544f20444154415f5345545f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f444154415f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a2020202020202020202020444154415f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d502c0a20202020202020202020204f5244494e414c0a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c2827444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e444154415f49445f4348494c442c200a20202020202020202020202853454c454354205550504552284348494c445f4c4142454c292046524f4d2052454c4154494f4e534849505f5459504553205748455245204944203d204e45572e52454c4154494f4e534849505f4944292c200a20202020202020202020204e45572e444154415f49445f504152454e542c200a20202020202020202020202853454c45435420434f44452046524f4d20646174615f616c6c205748455245204944203d204e45572e444154415f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d502c0a20202020202020202020204e45572e4f5244494e414c0a202020202020202020293b0a20202020202020293b0a0a0a2d2d20646174612073657420636f6e74656e7420636f706965732072656c6174696f6e73686970730a0a435245415445204f52205245504c4143452052554c4520636f6e74656e745f636f706965735f686973746f72795f696e736572742041530a20204f4e20494e5345525420544f20636f6e74656e745f636f706965730a2020444f20414c534f20280a20202020494e5345525420494e544f20646174615f7365745f636f706965735f686973746f727920280a20202020202069642c0a20202020202063635f69642c0a202020202020646174615f69642c0a20202020202065787465726e616c5f636f64652c0a202020202020706174682c0a2020202020206769745f636f6d6d69745f686173682c0a2020202020206769745f7265706f7369746f72795f69642c0a20202020202065646d735f69642c0a20202020202065646d735f636f64652c0a20202020202065646d735f6c6162656c2c0a20202020202065646d735f616464726573732c0a202020202020706572735f69645f617574686f722c0a20202020202076616c69645f66726f6d5f74696d657374616d700a20202020292056414c55455320280a2020202020206e65787476616c2827646174615f7365745f636f706965735f686973746f72795f69645f73657127292c200a2020202020204e45572e69642c0a2020202020204e45572e646174615f69642c200a2020202020204e45572e65787465726e616c5f636f64652c0a2020202020204e45572e706174682c0a2020202020204e45572e6769745f636f6d6d69745f686173682c0a2020202020204e45572e6769745f7265706f7369746f72795f69642c0a2020202020204e45572e65646d735f69642c0a2020202020202853454c45435420636f64652046524f4d2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d73205748455245206964203d204e45572e65646d735f6964292c0a2020202020202853454c454354206c6162656c2046524f4d2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d73205748455245206964203d204e45572e65646d735f6964292c0a2020202020202853454c45435420616464726573732046524f4d2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d73205748455245206964203d204e45572e65646d735f6964292c0a2020202020204e45572e706572735f69645f726567697374657265722c0a2020202020204e45572e726567697374726174696f6e5f74696d657374616d70293b0a2020293b0a202020200a435245415445204f52205245504c4143452052554c4520636f6e74656e745f636f706965735f686973746f72795f64656c6574652041530a20204f4e2044454c45544520544f20636f6e74656e745f636f706965730a2020444f20414c534f20280a2020202055504441544520646174615f7365745f636f706965735f686973746f7279205345542076616c69645f756e74696c5f74696d657374616d70203d2043555252454e545f54494d455354414d500a2020202057484552452063635f6964203d204f4c442e69643b0a2020293b202020200a202020200a2d2d2063726561746520636f6e74656e7420636f707920686973746f727920656e747279206f6e2065787465726e616c20646d73206368616e67650a435245415445204f52205245504c4143452052554c452065646d735f615f696e736572745f636f6e74656e745f636f70795f686973746f72792041530a20204f4e2055504441544520544f2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d730a2020444f20414c534f20280a20202020494e5345525420494e544f20646174615f7365745f636f706965735f686973746f727920280a20202020202069642c0a20202020202063635f69642c0a202020202020646174615f69642c0a20202020202065787465726e616c5f636f64652c0a202020202020706174682c0a2020202020206769745f636f6d6d69745f686173682c0a2020202020206769745f7265706f7369746f72795f69642c0a20202020202065646d735f69642c0a20202020202065646d735f636f64652c0a20202020202065646d735f6c6162656c2c0a20202020202065646d735f616464726573732c0a202020202020706572735f69645f617574686f722c0a20202020202076616c69645f66726f6d5f74696d657374616d700920200a09290a2020202053454c4543540a202020206e65787476616c2827646174615f7365745f636f706965735f686973746f72795f69645f73657127292c200a20202020647363682e63635f69642c0a20202020647363682e646174615f69642c0a20202020647363682e65787465726e616c5f636f64652c0a20202020647363682e706174682c0a20202020647363682e6769745f636f6d6d69745f686173682c0a20202020647363682e6769745f7265706f7369746f72795f69642c0a20202020647363682e65646d735f69642c0a202020204e45572e636f64652c0a202020204e45572e6c6162656c2c0a202020204e45572e616464726573732c0a20202020647363682e706572735f69645f617574686f722c0a2020202043555252454e545f54494d455354414d500a2020202046524f4d20646174615f7365745f636f706965735f686973746f727920647363680a202020204a4f494e2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d732065646d730a202020204f4e2065646d732e6964203d20647363682e65646d735f69640a202020205748455245204e45572e6964203d20647363682e65646d735f696420414e4420647363682e76616c69645f756e74696c5f74696d657374616d70204953204e554c4c3b0a2020293b0a0a2d2d2065787069726520636f6e74656e7420636f707920686973746f727920656e747279206f6e2065787465726e616c20646d73206368616e67650a435245415445204f52205245504c4143452052554c452065646d735f625f6578706972655f636f6e74656e745f636f70795f686973746f72792041530a20204f4e2055504441544520544f2065787465726e616c5f646174615f6d616e6167656d656e745f73797374656d730a2020444f20414c534f20280a202020205550444154450a20202020646174615f7365745f636f706965735f686973746f7279205345542076616c69645f756e74696c5f74696d657374616d70203d2043555252454e545f54494d455354414d500a2020202057484552452076616c69645f756e74696c5f74696d657374616d70204953204e554c4c0a20202020414e442065646d735f6964203d204e45572e69640a20202020414e442076616c69645f66726f6d5f74696d657374616d70203c3e2043555252454e545f54494d455354414d503b0a293b0a0a0a2d2d2073616d706c657320706172656e742d6368696c642072656c6174696f6e736869700a0a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a202020202020202020202027504152454e54272c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a2020202020202020202020274348494c44272c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a20202020202020293b0a202020202020200a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f64656c6574652041530a202020204f4e2044454c45544520544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a2020202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442053414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442053414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e4f54204e554c4c20414e44204f4c442e44454c5f4944204953204e554c4c0a20202020202020444f20414c534f20280a2020202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d700a2020202020202020202020574845524520284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442053414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c290a202020202020202020202020204f5220284d41494e5f53414d505f4944203d204f4c442e53414d504c455f49445f4348494c4420414e442053414d505f4944203d204f4c442e53414d504c455f49445f504152454e5420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c293b0a20202020202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f706172656e745f6368696c645f7265766572745f7570646174652041530a202020204f4e2055504441544520544f2073616d706c655f72656c6174696f6e73686970735f616c6c0a202020205748455245204e45572e44454c5f4944204953204e554c4c20414e44204f4c442e44454c5f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a202020202020202020202027504152454e54272c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f4348494c44292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a202020202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a202020202020202020202049442c200a20202020202020202020204d41494e5f53414d505f49442c0a202020202020202020202052454c4154494f4e5f545950452c200a202020202020202020202053414d505f49442c200a2020202020202020202020454e544954595f5045524d5f49442c0a2020202020202020202020504552535f49445f415554484f522c0a202020202020202020202056414c49445f46524f4d5f54494d455354414d500a202020202020202020292056414c55455320280a20202020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a20202020202020202020204e45572e53414d504c455f49445f4348494c442c200a2020202020202020202020274348494c44272c200a20202020202020202020204e45572e53414d504c455f49445f504152454e542c200a20202020202020202020202853454c454354205045524d5f49442046524f4d2073616d706c65735f616c6c205748455245204944203d204e45572e53414d504c455f49445f504152454e54292c0a20202020202020202020204e45572e504552535f49445f415554484f522c0a20202020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a202020202020202020293b0a20202020202020293b0a0a2d2d206578706572696d656e74202d3e2070726f6a6563740a0a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e74735f616c6c200a20202020574845524520284f4c442e50524f4a5f494420213d204e45572e50524f4a5f4944204f52204f4c442e50524f4a5f4944204953204e554c4c2920414e44204e45572e50524f4a5f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e50524f4a5f494420414e4420455850455f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e50524f4a5f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e494420414e442050524f4a5f4944203d204f4c442e50524f4a5f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202050524f4a5f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e50524f4a5f49442c200a2020202020202020202853454c454354207065726d5f69642046524f4d2050524f4a45435453205748455245204944203d204e45572e50524f4a5f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f206578706572696d656e74735f616c6c200a202020205748455245204f4c442e50524f4a5f4944204953204e4f54204e554c4c20414e44204e45572e50524f4a5f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e50524f4a5f494420414e4420455850455f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020555044415445204558504552494d454e545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f455850455f4944203d204f4c442e494420414e442050524f4a5f4944203d204f4c442e50524f4a5f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f696e736572742041530a202020204f4e20494e5345525420544f206578706572696d656e74735f616c6c200a202020205748455245204e45572e50524f4a5f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a202020202020202020455850455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e50524f4a5f49442c200a202020202020202020274f574e4552272c200a2020202020202020204e45572e49442c200a2020202020202020204e45572e5045524d5f49442c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020202020494e5345525420494e544f204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f455850455f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202050524f4a5f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c28274558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e50524f4a5f49442c200a2020202020202020202853454c454354207065726d5f69642046524f4d2050524f4a45435453205748455245204944203d204e45572e50524f4a5f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2020200a435245415445204f52205245504c4143452052554c45206578706572696d656e745f70726f6a6563745f64656c6574652041530a202020204f4e2044454c45544520544f206578706572696d656e74735f616c6c200a202020205748455245204f4c442e50524f4a5f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f200a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d2063757272656e745f74696d657374616d70200a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e50524f4a5f494420414e4420455850455f4944203d204f4c442e494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a0a2d2d2070726f6a656374202d3e2073706163650a0a435245415445204f52205245504c4143452052554c452070726f6a6563745f73706163655f7570646174652041530a202020204f4e2055504441544520544f2070726f6a65637473200a20202020574845524520284f4c442e53504143455f494420213d204e45572e53504143455f4944204f52204f4c442e53504143455f4944204953204e554c4c2920414e44204e45572e53504143455f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452070726f6a6563745f73706163655f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2070726f6a65637473200a202020205748455245204f4c442e53504143455f4944204953204e4f54204e554c4c20414e44204e45572e53504143455f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452050524f4a4543545f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d500a2020202020202020205748455245204d41494e5f50524f4a5f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a202020200a435245415445204f52205245504c4143452052554c452070726f6a6563745f73706163655f696e736572742041530a202020204f4e20494e5345525420544f2070726f6a65637473200a202020205748455245204e45572e53504143455f4944204953204e4f54204e554c4c0a20202020202020444f20414c534f20280a20202020202020494e5345525420494e544f2050524f4a4543545f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f50524f4a5f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282750524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a0a2d2d2073616d706c65202d3e2070726f6a6563740a0a435245415445204f52205245504c4143452052554c452073616d706c655f70726f6a6563745f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e50524f4a5f494420213d204e45572e50524f4a5f4944204f52204f4c442e50524f4a5f4944204953204e554c4c204f52204f4c442e455850455f4944204953204e4f54204e554c4c2920414e44204e45572e50524f4a5f4944204953204e4f54204e554c4c20414e44204e45572e455850455f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e442050524f4a5f4944203d204f4c442e50524f4a5f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202050524f4a5f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e50524f4a5f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d2050524f4a45435453205748455245204944203d204e45572e50524f4a5f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f70726f6a6563745f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e50524f4a5f4944204953204e4f54204e554c4c20414e4420284e45572e50524f4a5f4944204953204e554c4c204f5220284f4c442e455850455f4944204953204e554c4c20414e44204e45572e455850455f4944204953204e4f54204e554c4c29290a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e442050524f4a5f4944203d204f4c442e50524f4a5f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f70726f6a6563745f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e455850455f4944204953204e554c4c20414e44204e45572e50524f4a5f4944204953204e4f54204e554c4c0a20202020444f20414c534f20280a202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202050524f4a5f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e50524f4a5f49442c200a2020202020202020202853454c454354205045524d5f49442046524f4d2050524f4a45435453205748455245204944203d204e45572e50524f4a5f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a0a2d2d2073616d706c65202d3e2070726f6a6563740a0a435245415445204f52205245504c4143452052554c452073616d706c655f73706163655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a20202020574845524520284f4c442e53504143455f494420213d204e45572e53504143455f4944204f52204f4c442e53504143455f4944204953204e554c4c204f52204f4c442e455850455f4944204953204e4f54204e554c4c204f52204f4c442e50524f4a5f4944204953204e4f54204e554c4c2920414e44204e45572e53504143455f4944204953204e4f54204e554c4c20414e44204e45572e455850455f4944204953204e554c4c20414e44204e45572e50524f4a5f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a20202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f73706163655f72656d6f76655f7570646174652041530a202020204f4e2055504441544520544f2073616d706c65735f616c6c200a202020205748455245204f4c442e53504143455f4944204953204e4f54204e554c4c20414e4420284e45572e53504143455f4944204953204e554c4c204f5220284f4c442e455850455f4944204953204e554c4c20414e44204e45572e455850455f4944204953204e4f54204e554c4c29204f5220284f4c442e50524f4a5f4944204953204e554c4c20414e44204e45572e50524f4a5f4944204953204e4f54204e554c4c29290a20202020444f20414c534f20280a202020202020205550444154452053414d504c455f52454c4154494f4e53484950535f484953544f5259205345542056414c49445f554e54494c5f54494d455354414d50203d204e45572e4d4f44494649434154494f4e5f54494d455354414d50200a2020202020202020205748455245204d41494e5f53414d505f4944203d204f4c442e494420414e442053504143455f4944203d204f4c442e53504143455f494420414e442056414c49445f554e54494c5f54494d455354414d50204953204e554c4c3b0a20202020293b0a0a435245415445204f52205245504c4143452052554c452073616d706c655f73706163655f696e736572742041530a202020204f4e20494e5345525420544f2073616d706c65735f616c6c200a202020205748455245204e45572e455850455f4944204953204e554c4c20414e44204e45572e53504143455f4944204953204e4f54204e554c4c20414e44204e45572e50524f4a5f4944204953204e554c4c0a20202020444f20414c534f20280a202020202020494e5345525420494e544f2053414d504c455f52454c4154494f4e53484950535f484953544f525920280a20202020202020202049442c200a2020202020202020204d41494e5f53414d505f49442c0a20202020202020202052454c4154494f4e5f545950452c200a20202020202020202053504143455f49442c200a202020202020202020454e544954595f5045524d5f49442c0a202020202020202020504552535f49445f415554484f522c0a20202020202020202056414c49445f46524f4d5f54494d455354414d500a20202020202020292056414c55455320280a2020202020202020206e65787476616c282753414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455127292c200a2020202020202020204e45572e49442c200a202020202020202020274f574e4544272c200a2020202020202020204e45572e53504143455f49442c200a2020202020202020202853454c45435420434f44452046524f4d20535041434553205748455245204944203d204e45572e53504143455f4944292c0a2020202020202020204e45572e504552535f49445f4d4f4449464945522c0a2020202020202020204e45572e4d4f44494649434154494f4e5f54494d455354414d500a20202020202020293b0a202020293b0a2d2d20656e64206f662072756c657320666f722072656c6174696f6e736869707320686973746f72790a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20547269676765727320666f7220667265657a696e670a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f444528292052455455524e5320747269676765722041532024240a424547494e0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f7765642062656361757365202520252069732066726f7a656e2e272c2054475f415247565b305d2c2054475f415247565b315d2c204f4c442e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f53504143455f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a2020202073706163655f6964202020544543485f49443b0a202020206f7065726174696f6e2020544558543b0a424547494e0a20202020494620284e45572e73706163655f6964204953204e4f54204e554c4c20414e44204e45572e73706163655f66726f7a656e29205448454e0a202020202020202073706163655f6964203d204e45572e73706163655f69643b0a20202020202020206f7065726174696f6e203d2027534554205350414345273b0a20202020454c5345494620284f4c442e73706163655f6964204953204e4f54204e554c4c20414e44204f4c442e73706163655f66726f7a656e29205448454e0a202020202020202073706163655f6964203d204f4c442e73706163655f69643b0a20202020202020206f7065726174696f6e203d202752454d4f5645205350414345273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652025202520616e642073706163652025206172652066726f7a656e2e272c206f7065726174696f6e2c2054475f415247565b305d2c204e45572e636f64652c0a20202020202020202873656c65637420636f64652066726f6d20737061636573207768657265206964203d2073706163655f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f50524f4a4543545f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a2020202070726f6a6563745f6964202020544543485f49443b0a202020206f7065726174696f6e20202020544558543b0a424547494e0a20202020494620284e45572e70726f6a5f6964204953204e4f54204e554c4c20414e44204e45572e70726f6a5f66726f7a656e29205448454e0a202020202020202070726f6a6563745f6964203d204e45572e70726f6a5f69643b0a20202020202020206f7065726174696f6e203d20275345542050524f4a454354273b0a20202020454c5345494620284f4c442e70726f6a5f6964204953204e4f54204e554c4c20414e44204f4c442e70726f6a5f66726f7a656e29205448454e0a202020202020202070726f6a6563745f6964203d204f4c442e70726f6a5f69643b0a20202020202020206f7065726174696f6e203d202752454d4f56452050524f4a454354273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652025202520616e642070726f6a6563742025206172652066726f7a656e2e272c206f7065726174696f6e2c2054475f415247565b305d2c204e45572e636f64652c0a20202020202020202873656c65637420636f64652066726f6d2070726f6a65637473207768657265206964203d2070726f6a6563745f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e545f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a202020206578706572696d656e745f6964202020544543485f49443b0a202020206f7065726174696f6e20202020202020544558543b0a424547494e0a20202020494620284e45572e657870655f6964204953204e4f54204e554c4c20414e44204e45572e657870655f66726f7a656e29205448454e0a20202020202020206578706572696d656e745f6964203d204e45572e657870655f69643b0a20202020202020206f7065726174696f6e203d2027534554204558504552494d454e54273b0a20202020454c5345494620284f4c442e657870655f6964204953204e4f54204e554c4c20414e44204f4c442e657870655f66726f7a656e29205448454e0a20202020202020206578706572696d656e745f6964203d204f4c442e657870655f69643b0a20202020202020206f7065726174696f6e203d202752454d4f5645204558504552494d454e54273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652025202520616e64206578706572696d656e742025206172652066726f7a656e2e272c206f7065726174696f6e2c2054475f415247565b305d2c204e45572e636f64652c0a20202020202020202873656c65637420636f64652066726f6d206578706572696d656e74735f616c6c207768657265206964203d206578706572696d656e745f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20547269676765727320666f7220667265657a696e670a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f444528292052455455524e5320747269676765722041532024240a424547494e0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f7765642062656361757365202520252069732066726f7a656e2e272c2054475f415247565b305d2c2054475f415247565b315d2c204f4c442e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f53504143455f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a2020202073706163655f6964202020544543485f49443b0a202020206f7065726174696f6e2020544558543b0a424547494e0a20202020494620284e45572e73706163655f6964204953204e4f54204e554c4c20414e44204e45572e73706163655f66726f7a656e29205448454e0a202020202020202073706163655f6964203d204e45572e73706163655f69643b0a20202020202020206f7065726174696f6e203d2027534554205350414345273b0a20202020454c5345494620284f4c442e73706163655f6964204953204e4f54204e554c4c20414e44204f4c442e73706163655f66726f7a656e29205448454e0a202020202020202073706163655f6964203d204f4c442e73706163655f69643b0a20202020202020206f7065726174696f6e203d202752454d4f5645205350414345273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f776564206265636175736520737061636520252069732066726f7a656e20666f72202520252e272c206f7065726174696f6e2c0a20202020202020202873656c65637420636f64652066726f6d20737061636573207768657265206964203d2073706163655f6964292c2054475f415247565b305d2c204e45572e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f50524f4a4543545f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a2020202070726f6a6563745f6964202020544543485f49443b0a202020206f7065726174696f6e20202020544558543b0a424547494e0a20202020494620284e45572e70726f6a5f6964204953204e4f54204e554c4c20414e44204e45572e70726f6a5f66726f7a656e29205448454e0a202020202020202070726f6a6563745f6964203d204e45572e70726f6a5f69643b0a20202020202020206f7065726174696f6e203d20275345542050524f4a454354273b0a20202020454c5345494620284f4c442e70726f6a5f6964204953204e4f54204e554c4c20414e44204f4c442e70726f6a5f66726f7a656e29205448454e0a202020202020202070726f6a6563745f6964203d204f4c442e70726f6a5f69643b0a20202020202020206f7065726174696f6e203d202752454d4f56452050524f4a454354273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652070726f6a65637420252069732066726f7a656e20666f72202520252e272c206f7065726174696f6e2c0a20202020202020202873656c65637420636f64652066726f6d2070726f6a65637473207768657265206964203d2070726f6a6563745f6964292c2054475f415247565b305d2c204e45572e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e545f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a202020206578706572696d656e745f6964202020544543485f49443b0a202020206f7065726174696f6e20202020202020544558543b0a424547494e0a20202020494620284e45572e657870655f6964204953204e4f54204e554c4c20414e44204e45572e657870655f66726f7a656e29205448454e0a20202020202020206578706572696d656e745f6964203d204e45572e657870655f69643b0a20202020202020206f7065726174696f6e203d2027534554204558504552494d454e54273b0a20202020454c5345494620284f4c442e657870655f6964204953204e4f54204e554c4c20414e44204f4c442e657870655f66726f7a656e29205448454e0a20202020202020206578706572696d656e745f6964203d204f4c442e657870655f69643b0a20202020202020206f7065726174696f6e203d202752454d4f5645204558504552494d454e54273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f7765642062656361757365206578706572696d656e7420252069732066726f7a656e20666f72202520252e272c206f7065726174696f6e2c0a20202020202020202873656c65637420636f64652066726f6d206578706572696d656e74735f616c6c207768657265206964203d206578706572696d656e745f6964292c2054475f415247565b305d2c204e45572e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d20537061636573202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20537061636573206d656c74696e670a0a435245415445204f52205245504c4143452046554e4354494f4e204d454c545f53504143455f464f5228292052455455524e5320747269676765722061732024240a424547494e0a202020204e45572e46524f5a454e5f464f525f50524f4a203d202766273b0a202020204e45572e46524f5a454e5f464f525f53414d50203d202766273b0a2020202072657475726e204e45573b0a656e643b0a2424206c616e677561676520706c706773716c3b0a0a44524f50205452494747455220494620455849535453204d454c545f53504143455f464f52204f4e205350414345533b0a4352454154452054524947474552204d454c545f53504143455f464f52204245464f524520555044415445204f4e205350414345530a20202020464f52204541434820524f57205748454e2028284e45572e46524f5a454e5f464f525f50524f4a204f52204e45572e46524f5a454e5f464f525f53414d502920414e44204e4f54204e45572e46524f5a454e290a20202020455845435554452050524f434544555245204d454c545f53504143455f464f5228293b0a0a2d2d205370616365732064656c6574696e670a0a44524f502054524947474552204946204558495354532053504143455f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e205350414345533b0a43524541544520545249474745522053504143455f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e205350414345530a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f4445282744454c455445272c2027737061636527293b0a0a2d2d205370616365207570646174650a0a44524f502054524947474552204946204558495354532053504143455f46524f5a454e5f434845434b5f4f4e5f555044415445204f4e205350414345533b0a43524541544520545249474745522053504143455f46524f5a454e5f434845434b5f4f4e5f555044415445204245464f524520555044415445204f4e205350414345530a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e20414e44204e45572e66726f7a656e20414e44200a2020202020202020284f4c442e6465736372697074696f6e203c3e204e45572e6465736372697074696f6e200a2020202020202020204f5220284f4c442e6465736372697074696f6e204953204e554c4c20414e44204e45572e6465736372697074696f6e204953204e4f54204e554c4c290a2020202020202020204f5220284f4c442e6465736372697074696f6e204953204e4f54204e554c4c20414e44204e45572e6465736372697074696f6e204953204e554c4c292929200a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f44452827555044415445272c2027737061636527293b0a0a2d2d2050726f6a65637473202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2050726f6a65637473206d656c74696e670a0a435245415445204f52205245504c4143452046554e4354494f4e204d454c545f50524f4a4543545f464f5228292052455455524e5320747269676765722061732024240a424547494e0a202020204e45572e46524f5a454e5f464f525f455850203d202766273b0a202020204e45572e46524f5a454e5f464f525f53414d50203d202766273b0a2020202072657475726e204e45573b0a656e643b0a2424206c616e677561676520706c706773716c3b0a0a44524f50205452494747455220494620455849535453204d454c545f50524f4a4543545f464f52204f4e2050524f4a454354533b0a4352454154452054524947474552204d454c545f50524f4a4543545f464f52204245464f524520555044415445204f4e2050524f4a454354530a20202020464f52204541434820524f57205748454e2028284e45572e46524f5a454e5f464f525f455850204f52204e45572e46524f5a454e5f464f525f53414d502920414e44204e4f54204e45572e46524f5a454e290a20202020455845435554452050524f434544555245204d454c545f50524f4a4543545f464f5228293b0a0a2d2d2050726f6a6563742064656c6574696e670a0a44524f502054524947474552204946204558495354532050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e2050524f4a454354533b0a43524541544520545249474745522050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e2050524f4a454354530a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f4445282744454c455445272c202770726f6a65637427293b0a0a2d2d2050726f6a656374207570646174650a44524f502054524947474552204946204558495354532050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f555044415445204f4e2050524f4a454354533b0a43524541544520545249474745522050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f555044415445204245464f524520555044415445204f4e2050524f4a454354530a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e20414e44204e45572e66726f7a656e20414e44200a2020202020202020284f4c442e6465736372697074696f6e203c3e204e45572e6465736372697074696f6e200a2020202020202020204f5220284f4c442e6465736372697074696f6e204953204e554c4c20414e44204e45572e6465736372697074696f6e204953204e4f54204e554c4c290a2020202020202020204f5220284f4c442e6465736372697074696f6e204953204e4f54204e554c4c20414e44204e45572e6465736372697074696f6e204953204e554c4c292929200a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f44452827555044415445272c202770726f6a65637427293b0a0a2d2d2050726f6a656374206174746163686d656e7420696e73657274696e672c207570646174696e6720616e642064656c6574696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f50524f4a45435428292052455455524e5320747269676765722041532024240a4445434c4152450a2020202070726f6a6563745f6964202020544543485f49443b0a424547494e0a202020204946202854475f4f50203d202744454c4554452729205448454e0a202020202020202070726f6a6563745f6964203d204f4c442e70726f6a5f69643b0a20202020454c53454946202854475f4f50203d2027494e5345525427204f522054475f4f50203d20275550444154452729205448454e0a202020202020202070726f6a6563745f6964203d204e45572e70726f6a5f69643b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e20252025206973206e6f7420616c6c6f77656420626563617573652070726f6a65637420252069732066726f7a656e2e272c2054475f4f502c2054475f415247565b305d2c0a20202020202020202873656c65637420636f64652066726f6d2070726f6a65637473207768657265206964203d2070726f6a6563745f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a44524f502054524947474552204946204558495354532050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f494e534552545f4154544143484d454e54204f4e204154544143484d454e54533b0a43524541544520545249474745522050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f494e534552545f4154544143484d454e54204245464f524520494e53455254204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284e45572e50524f4a5f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a45435428274154544143484d454e5427293b0a0a44524f502054524947474552204946204558495354532050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f5550444154455f4154544143484d454e54204f4e204154544143484d454e54533b0a43524541544520545249474745522050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f5550444154455f4154544143484d454e54204245464f524520555044415445204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284f4c442e50524f4a5f46524f5a454e20414e44204e45572e50524f4a5f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a45435428274154544143484d454e5427293b0a0a44524f502054524947474552204946204558495354532050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f4154544143484d454e54204f4e204154544143484d454e54533b0a43524541544520545249474745522050524f4a4543545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f4154544143484d454e54204245464f52452044454c455445204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284f4c442e50524f4a5f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a45435428274154544143484d454e5427293b0a0a2d2d2050726f6a6563742073706163652072656c6174696f6e736869700a44524f50205452494747455220494620455849535453204144445f50524f4a4543545f544f5f53504143455f434845434b204f4e2050524f4a454354533b0a4352454154452054524947474552204144445f50524f4a4543545f544f5f53504143455f434845434b20414654455220494e53455254204f4e2050524f4a454354530a20202020464f52204541434820524f57205748454e20284e45572e53504143455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53504143455f52454c4154494f4e53484950282770726f6a65637427293b0a0a44524f502054524947474552204946204558495354532050524f4a4543545f53504143455f52454c4154494f4e534849505f46524f5a454e5f434845434b204f4e2050524f4a454354533b0a43524541544520545249474745522050524f4a4543545f53504143455f52454c4154494f4e534849505f46524f5a454e5f434845434b204245464f524520555044415445204f4e2050524f4a454354530a20202020464f52204541434820524f57205748454e20284e45572e73706163655f6964203c3e204f4c442e73706163655f696420414e4420284e45572e53504143455f46524f5a454e204f52204f4c442e53504143455f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53504143455f52454c4154494f4e53484950282770726f6a65637427293b0a0a2d2d204578706572696d656e7473202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d204578706572696d656e7473206d656c74696e670a0a435245415445204f52205245504c4143452046554e4354494f4e204d454c545f4558504552494d454e545f464f5228292052455455524e5320747269676765722061732024240a424547494e0a202020204e45572e46524f5a454e5f464f525f53414d50203d202766273b0a202020204e45572e46524f5a454e5f464f525f44415441203d202766273b0a2020202072657475726e204e45573b0a656e643b0a2424206c616e677561676520706c706773716c3b0a0a44524f50205452494747455220494620455849535453204d454c545f4558504552494d454e545f464f52204f4e204558504552494d454e54535f414c4c3b0a4352454154452054524947474552204d454c545f4558504552494d454e545f464f52204245464f524520555044415445204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e2028284e45572e46524f5a454e5f464f525f53414d50204f52204e45572e46524f5a454e5f464f525f444154412920414e44204e4f54204e45572e46524f5a454e290a20202020455845435554452050524f434544555245204d454c545f4558504552494d454e545f464f5228293b0a0a2d2d204578706572696d656e74207472617368696e6720616e642064656c6574696e670a0a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f5452415348204f4e204558504552494d454e54535f414c4c3b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f5452415348204245464f524520555044415445204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e66726f7a656e29200a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f444528275452415348272c20276578706572696d656e7427293b0a202020200a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e204558504552494d454e54535f414c4c3b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f4445282744454c455445272c20276578706572696d656e7427293b0a0a2d2d204578706572696d656e742070726f706572747920696e73657274696e672c207570646174696e6720616e642064656c6574696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e5428292052455455524e5320747269676765722041532024240a4445434c4152450a202020206578706572696d656e745f6964202020544543485f49443b0a424547494e0a202020204946202854475f4f50203d202744454c4554452729205448454e0a20202020202020206578706572696d656e745f6964203d204f4c442e657870655f69643b0a20202020454c53454946202854475f4f50203d2027494e5345525427204f522054475f4f50203d20275550444154452729205448454e0a20202020202020206578706572696d656e745f6964203d204e45572e657870655f69643b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e20252025206973206e6f7420616c6c6f7765642062656361757365206578706572696d656e7420252069732066726f7a656e2e272c2054475f4f502c2054475f415247565b305d2c0a20202020202020202873656c65637420636f64652066726f6d206578706572696d656e74735f616c6c207768657265206964203d206578706572696d656e745f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f494e534552545f50524f5045525459204f4e204558504552494d454e545f50524f504552544945533b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f494e534552545f50524f5045525459204245464f524520494e53455254204f4e204558504552494d454e545f50524f504552544945530a20202020464f52204541434820524f57205748454e20284e45572e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e54282750524f504552545927293b0a0a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f4348414e47455f50524f5045525459204f4e204558504552494d454e545f50524f504552544945533b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f4348414e47455f50524f5045525459204245464f524520555044415445204f4e204558504552494d454e545f50524f504552544945530a20202020464f52204541434820524f57205748454e20284f4c442e455850455f46524f5a454e20414e44204e45572e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e54282750524f504552545927293b0a0a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f50524f5045525459204f4e204558504552494d454e545f50524f504552544945533b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f50524f5045525459204245464f52452044454c455445204f4e204558504552494d454e545f50524f504552544945530a20202020464f52204541434820524f57205748454e20284f4c442e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e54282750524f504552545927293b0a0a2d2d204578706572696d656e74206174746163686d656e7420696e73657274696e672c207570646174696e6720616e642064656c6574696e670a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f494e534552545f4154544143484d454e54204f4e204154544143484d454e54533b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f494e534552545f4154544143484d454e54204245464f524520494e53455254204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284e45572e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e5428274154544143484d454e5427293b0a0a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f5550444154455f4154544143484d454e54204f4e204154544143484d454e54533b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f5550444154455f4154544143484d454e54204245464f524520555044415445204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284f4c442e455850455f46524f5a454e20414e44204e45572e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e5428274154544143484d454e5427293b0a0a44524f50205452494747455220494620455849535453204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f4154544143484d454e54204f4e204154544143484d454e54533b0a4352454154452054524947474552204558504552494d454e545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f4154544143484d454e54204245464f52452044454c455445204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284f4c442e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e5428274154544143484d454e5427293b0a0a2d2d204578706572696d656e742070726f6a6563742072656c6174696f6e736869700a44524f50205452494747455220494620455849535453204144445f4558504552494d454e545f544f5f50524f4a4543545f434845434b204f4e204558504552494d454e54535f414c4c3b0a4352454154452054524947474552204144445f4558504552494d454e545f544f5f50524f4a4543545f434845434b20414654455220494e53455254204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e50524f4a5f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a4543545f52454c4154494f4e5348495028276578706572696d656e7427293b0a0a44524f50205452494747455220494620455849535453204558504552494d454e545f50524f4a4543545f52454c4154494f4e534849505f46524f5a454e5f434845434b204f4e204558504552494d454e54535f414c4c3b0a4352454154452054524947474552204558504552494d454e545f50524f4a4543545f52454c4154494f4e534849505f46524f5a454e5f434845434b204245464f524520555044415445204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e70726f6a5f6964203c3e204f4c442e70726f6a5f696420414e4420284e45572e50524f4a5f46524f5a454e204f52204f4c442e50524f4a5f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a4543545f52454c4154494f4e5348495028276578706572696d656e7427293b0a0a2d2d2053616d706c6573202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2053616d706c6573206d656c74696e670a0a435245415445204f52205245504c4143452046554e4354494f4e204d454c545f53414d504c455f464f5228292052455455524e5320747269676765722061732024240a424547494e0a202020204e45572e46524f5a454e5f464f525f434f4d50203d202766273b0a202020204e45572e46524f5a454e5f464f525f4348494c4452454e203d202766273b0a202020204e45572e46524f5a454e5f464f525f504152454e5453203d202766273b0a202020204e45572e46524f5a454e5f464f525f44415441203d202766273b0a2020202072657475726e204e45573b0a656e643b0a2424206c616e677561676520706c706773716c3b0a0a44524f50205452494747455220494620455849535453204d454c545f53414d504c455f464f52204f4e2053414d504c45535f414c4c3b0a4352454154452054524947474552204d454c545f53414d504c455f464f52204245464f524520555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e2028284e45572e46524f5a454e5f464f525f434f4d50204f52204e45572e46524f5a454e5f464f525f4348494c4452454e204f52204e45572e46524f5a454e5f464f525f504152454e5453204f52204e45572e46524f5a454e5f464f525f444154412920414e44204e4f54204e45572e46524f5a454e290a20202020455845435554452050524f434544555245204d454c545f53414d504c455f464f5228293b0a0a2d2d2053616d706c65207472617368696e6720616e642064656c6574696e670a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f5452415348204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f5452415348204245464f524520555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e66726f7a656e29200a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f444528275452415348272c202773616d706c6527293b0a202020200a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f4445282744454c455445272c202773616d706c6527293b0a0a2d2d2053616d706c652070726f706572747920696e73657274696e672c207570646174696e6720616e642064656c6574696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f53414d504c4528292052455455524e5320747269676765722041532024240a4445434c4152450a2020202073616d706c655f6964202020544543485f49443b0a424547494e0a202020204946202854475f4f50203d202744454c4554452729205448454e0a202020202020202073616d706c655f6964203d204f4c442e73616d705f69643b0a20202020454c53454946202854475f4f50203d2027494e5345525427204f522054475f4f50203d20275550444154452729205448454e0a202020202020202073616d706c655f6964203d204e45572e73616d705f69643b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e20252025206973206e6f7420616c6c6f77656420626563617573652073616d706c6520252069732066726f7a656e2e272c2054475f4f502c2054475f415247565b305d2c0a20202020202020202873656c65637420636f64652066726f6d2073616d706c65735f616c6c207768657265206964203d2073616d706c655f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f494e534552545f50524f5045525459204f4e2053414d504c455f50524f504552544945533b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f494e534552545f50524f5045525459204245464f524520494e53455254204f4e2053414d504c455f50524f504552544945530a20202020464f52204541434820524f57205748454e20284e45572e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c45282750524f504552545927293b0a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f4348414e47455f50524f5045525459204f4e2053414d504c455f50524f504552544945533b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f4348414e47455f50524f5045525459204245464f524520555044415445204f4e2053414d504c455f50524f504552544945530a20202020464f52204541434820524f57205748454e20284f4c442e53414d505f46524f5a454e20414e44204e45572e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c45282750524f504552545927293b0a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f44454c4554455f50524f5045525459204f4e2053414d504c455f50524f504552544945533b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f44454c4554455f50524f5045525459204245464f52452044454c455445204f4e2053414d504c455f50524f504552544945530a20202020464f52204541434820524f57205748454e20284f4c442e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c45282750524f504552545927293b0a0a2d2d2053616d706c65206174746163686d656e7420696e73657274696e672c207570646174696e6720616e642064656c6574696e670a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f494e534552545f4154544143484d454e54204f4e204154544143484d454e54533b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f494e534552545f4154544143484d454e54204245464f524520494e53455254204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284e45572e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c4528274154544143484d454e5427293b0a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f5550444154455f4154544143484d454e54204f4e204154544143484d454e54533b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f5550444154455f4154544143484d454e54204245464f524520555044415445204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284f4c442e53414d505f46524f5a454e20414e44204e45572e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c4528274154544143484d454e5427293b0a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f44454c4554455f4154544143484d454e54204f4e204154544143484d454e54533b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f44454c4554455f4154544143484d454e54204245464f52452044454c455445204f4e204154544143484d454e54530a20202020464f52204541434820524f57205748454e20284f4c442e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c4528274154544143484d454e5427293b0a0a2d2d2053616d706c6520636f6e7461696e65722073657474696e6720616e642072656d6f76696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f53414d504c455f434f4e5441494e45525f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a2020202073616d706c655f6964202020544543485f49443b0a202020206f7065726174696f6e202020544558543b0a424547494e0a20202020494620284e45572e73616d705f69645f706172745f6f66204953204e4f54204e554c4c20414e44204e45572e434f4e545f46524f5a454e29205448454e0a202020202020202073616d706c655f6964203d204e45572e73616d705f69645f706172745f6f663b0a20202020202020206f7065726174696f6e203d202753455420434f4e5441494e4552273b0a20202020454c5345494620284f4c442e73616d705f69645f706172745f6f66204953204e4f54204e554c4c20414e44204f4c442e434f4e545f46524f5a454e29205448454e0a202020202020202073616d706c655f6964203d204f4c442e73616d705f69645f706172745f6f663b0a20202020202020206f7065726174696f6e203d202752454d4f564520434f4e5441494e4552273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652073616d706c6520252069732066726f7a656e20666f722073616d706c6520252e272c206f7065726174696f6e2c0a20202020202020202873656c65637420636f64652066726f6d2073616d706c65735f616c6c207768657265206964203d2073616d706c655f6964292c204e45572e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f50205452494747455220494620455849535453204144445f53414d504c455f544f5f434f4e5441494e45525f434845434b204f4e2053414d504c45535f414c4c3b0a4352454154452054524947474552204144445f53414d504c455f544f5f434f4e5441494e45525f434845434b20414654455220494e53455254204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e434f4e545f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c455f434f4e5441494e45525f52454c4154494f4e5348495028293b0a0a44524f502054524947474552204946204558495354532053414d504c455f46524f5a454e5f434845434b5f4f4e5f5345545f434f4e5441494e4552204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522053414d504c455f46524f5a454e5f434845434b5f4f4e5f5345545f434f4e5441494e4552204245464f524520555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20280a2020202020202020284e45572e73616d705f69645f706172745f6f66203c3e204f4c442e73616d705f69645f706172745f6f660a2020202020202020204f5220284e45572e73616d705f69645f706172745f6f66204953204e4f54204e554c4c20414e44204f4c442e73616d705f69645f706172745f6f66204953204e554c4c290a2020202020202020204f5220284e45572e73616d705f69645f706172745f6f66204953204e554c4c20414e44204f4c442e73616d705f69645f706172745f6f66204953204e4f54204e554c4c29290a2020202020202020414e4420284e45572e434f4e545f46524f5a454e204f52204f4c442e434f4e545f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c455f434f4e5441494e45525f52454c4154494f4e5348495028293b0a0a2d2d2053616d706c6520706172656e742d6368696c642072656c6174696f6e7368697020696e73657274696e6720616e642064656c6574696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f53414d504c455f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a20202020706172656e745f6964202020544543485f49443b0a202020206368696c645f696420202020544543485f49443b0a424547494e0a202020204946202854475f4f50203d202744454c4554452729205448454e0a2020202020202020706172656e745f6964203d204f4c442e73616d706c655f69645f706172656e743b0a20202020202020206368696c645f6964203d204f4c442e73616d706c655f69645f6368696c643b0a20202020454c53454946202854475f4f50203d2027494e534552542729205448454e0a2020202020202020706172656e745f6964203d204e45572e73616d706c655f69645f706172656e743b0a20202020202020206368696c645f6964203d204e45572e73616d706c655f69645f6368696c643b0a20202020454e442049463b0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652073616d706c652025206f7220252069732066726f7a656e2e272c2054475f4f502c200a20202020202020202873656c65637420636f64652066726f6d2073616d706c65735f616c6c207768657265206964203d20706172656e745f6964292c0a20202020202020202873656c65637420636f64652066726f6d2073616d706c65735f616c6c207768657265206964203d206368696c645f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f502054524947474552204946204558495354532053414d504c455f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f494e53455254204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522053414d504c455f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f494e53455254204245464f524520494e53455254204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e504152454e545f46524f5a454e204f52204e45572e4348494c445f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c455f52454c4154494f4e5348495028293b0a0a44524f502054524947474552204946204558495354532053414d504c455f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522053414d504c455f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e504152454e545f46524f5a454e204f52204f4c442e4348494c445f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53414d504c455f52454c4154494f4e5348495028293b0a0a2d2d2053616d706c65206578706572696d656e742072656c6174696f6e736869700a44524f50205452494747455220494620455849535453204144445f53414d504c455f544f5f4558504552494d454e545f434845434b204f4e2053414d504c45535f414c4c3b0a4352454154452054524947474552204144445f53414d504c455f544f5f4558504552494d454e545f434845434b20414654455220494e53455254204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e545f52454c4154494f4e53484950282773616d706c6527293b0a0a44524f502054524947474552204946204558495354532053414d504c455f4558504552494d454e545f52454c4154494f4e534849505f46524f5a454e5f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522053414d504c455f4558504552494d454e545f52454c4154494f4e534849505f46524f5a454e5f434845434b204245464f524520555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20280a2020202020202020284e45572e657870655f6964203c3e204f4c442e657870655f69640a2020202020202020204f5220284e45572e657870655f6964204953204e4f54204e554c4c20414e44204f4c442e657870655f6964204953204e554c4c290a2020202020202020204f5220284e45572e657870655f6964204953204e554c4c20414e44204f4c442e657870655f6964204953204e4f54204e554c4c29290a2020202020202020414e4420284e45572e455850455f46524f5a454e204f52204f4c442e455850455f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e545f52454c4154494f4e53484950282773616d706c6527293b0a0a2d2d2053616d706c652070726f6a6563742072656c6174696f6e736869700a44524f50205452494747455220494620455849535453204144445f53414d504c455f544f5f50524f4a4543545f434845434b204f4e2053414d504c45535f414c4c3b0a4352454154452054524947474552204144445f53414d504c455f544f5f50524f4a4543545f434845434b20414654455220494e53455254204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e50524f4a5f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a4543545f52454c4154494f4e53484950282773616d706c6527293b0a0a44524f502054524947474552204946204558495354532053414d504c455f50524f4a4543545f52454c4154494f4e534849505f46524f5a454e5f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522053414d504c455f50524f4a4543545f52454c4154494f4e534849505f46524f5a454e5f434845434b204245464f524520555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20280a2020202020202020284e45572e70726f6a5f6964203c3e204f4c442e70726f6a5f69640a2020202020202020204f5220284e45572e70726f6a5f6964204953204e4f54204e554c4c20414e44204f4c442e70726f6a5f6964204953204e554c4c290a2020202020202020204f5220284e45572e70726f6a5f6964204953204e554c4c20414e44204f4c442e70726f6a5f6964204953204e4f54204e554c4c29290a2020202020202020414e4420284e45572e50524f4a5f46524f5a454e204f52204f4c442e50524f4a5f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f50524f4a4543545f52454c4154494f4e53484950282773616d706c6527293b0a0a2d2d2053616d706c652073706163652072656c6174696f6e736869700a44524f50205452494747455220494620455849535453204144445f53414d504c455f544f5f53504143455f434845434b204f4e2053414d504c45535f414c4c3b0a4352454154452054524947474552204144445f53414d504c455f544f5f53504143455f434845434b20414654455220494e53455254204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e53504143455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53504143455f52454c4154494f4e53484950282773616d706c6527293b0a0a44524f502054524947474552204946204558495354532053414d504c455f53504143455f52454c4154494f4e534849505f46524f5a454e5f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522053414d504c455f53504143455f52454c4154494f4e534849505f46524f5a454e5f434845434b204245464f524520555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20280a2020202020202020284e45572e73706163655f6964203c3e204f4c442e73706163655f69640a2020202020202020204f5220284e45572e73706163655f6964204953204e4f54204e554c4c20414e44204f4c442e73706163655f6964204953204e554c4c290a2020202020202020204f5220284e45572e73706163655f6964204953204e554c4c20414e44204f4c442e73706163655f6964204953204e4f54204e554c4c29290a2020202020202020414e4420284e45572e53504143455f46524f5a454e204f52204f4c442e53504143455f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f53504143455f52454c4154494f4e53484950282773616d706c6527293b0a0a2d2d204461746120536574202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d20536574206d656c74696e670a0a435245415445204f52205245504c4143452046554e4354494f4e204d454c545f444154415f5345545f464f5228292052455455524e5320747269676765722061732024240a424547494e0a202020204e45572e46524f5a454e5f464f525f4348494c4452454e203d202766273b0a202020204e45572e46524f5a454e5f464f525f504152454e5453203d202766273b0a202020204e45572e46524f5a454e5f464f525f434f4d5053203d202766273b0a202020204e45572e46524f5a454e5f464f525f434f4e5453203d202766273b0a2020202072657475726e204e45573b0a656e643b0a2424206c616e677561676520706c706773716c3b0a0a44524f50205452494747455220494620455849535453204d454c545f444154415f5345545f464f52204f4e20444154415f414c4c3b0a4352454154452054524947474552204d454c545f444154415f5345545f464f52204245464f524520555044415445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e2028284e45572e46524f5a454e5f464f525f4348494c4452454e204f52204e45572e46524f5a454e5f464f525f504152454e5453204f52204e45572e46524f5a454e5f464f525f434f4d5053204f52204e45572e46524f5a454e5f464f525f434f4e54532920414e44204e4f54204e45572e46524f5a454e290a20202020455845435554452050524f434544555245204d454c545f444154415f5345545f464f5228293b0a0a2d2d204461746120736574207472617368696e6720616e642064656c6574696e670a0a44524f5020545249474745522049462045584953545320444154415f5345545f46524f5a454e5f434845434b5f4f4e5f5452415348204f4e20444154415f414c4c3b0a435245415445205452494747455220444154415f5345545f46524f5a454e5f434845434b5f4f4e5f5452415348204245464f524520555044415445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e66726f7a656e29200a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f444528275452415348272c2027646174612073657427293b0a202020200a44524f5020545249474745522049462045584953545320444154415f5345545f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e20444154415f414c4c3b0a435245415445205452494747455220444154415f5345545f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f454e544954595f42595f434f4445282744454c455445272c2027646174612073657427293b0a0a2d2d2044617461207365742070726f706572747920696e73657274696e672c207570646174696e6720616e642064656c6574696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f444154415f53455428292052455455524e5320747269676765722041532024240a4445434c4152450a2020202064735f6964202020544543485f49443b0a424547494e0a202020204946202854475f4f50203d202744454c4554452729205448454e0a202020202020202064735f6964203d204f4c442e64735f69643b0a20202020454c53454946202854475f4f50203d2027494e5345525427204f522054475f4f50203d20275550444154452729205448454e0a202020202020202064735f6964203d204e45572e64735f69643b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e20252025206973206e6f7420616c6c6f776564206265636175736520646174612073657420252069732066726f7a656e2e272c2054475f4f502c2054475f415247565b305d2c0a20202020202020202873656c65637420636f64652066726f6d20646174615f616c6c207768657265206964203d2064735f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f46524f5a454e5f434845434b5f4f4e5f494e534552545f50524f5045525459204f4e20444154415f5345545f50524f504552544945533b0a435245415445205452494747455220444154415f5345545f46524f5a454e5f434845434b5f4f4e5f494e534552545f50524f5045525459204245464f524520494e53455254204f4e20444154415f5345545f50524f504552544945530a20202020464f52204541434820524f57205748454e20284e45572e444153455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f534554282750524f504552545927293b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f46524f5a454e5f434845434b5f4f4e5f4348414e47455f50524f5045525459204f4e20444154415f5345545f50524f504552544945533b0a435245415445205452494747455220444154415f5345545f46524f5a454e5f434845434b5f4f4e5f4348414e47455f50524f5045525459204245464f524520555044415445204f4e20444154415f5345545f50524f504552544945530a20202020464f52204541434820524f57205748454e20284f4c442e444153455f46524f5a454e20414e44204e45572e444153455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f534554282750524f504552545927293b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f50524f5045525459204f4e20444154415f5345545f50524f504552544945533b0a435245415445205452494747455220444154415f5345545f46524f5a454e5f434845434b5f4f4e5f44454c4554455f50524f5045525459204245464f52452044454c455445204f4e20444154415f5345545f50524f504552544945530a20202020464f52204541434820524f57205748454e20284f4c442e444153455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f534554282750524f504552545927293b0a0a2d2d20446174612073657420706172656e742d6368696c642f636f6e7461696e65722d636f6d706f6e656e742072656c6174696f6e7368697020696e73657274696e6720616e642064656c6574696e670a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f444154415f5345545f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a20202020706172656e745f69642020202020202020202020544543485f49443b0a202020206368696c645f6964202020202020202020202020544543485f49443b0a2020202072656c6174696f6e736869705f69642020202020544543485f49443b0a2020202072656c6174696f6e736869702020202020202020434f44453b0a20202020706172656e745f6368696c645f66726f7a656e20424f4f4c45414e5f434841523b0a20202020636f6e745f636f6d705f66726f7a656e20202020424f4f4c45414e5f434841523b0a424547494e0a202020204946202854475f4f50203d202744454c4554452729205448454e0a2020202020202020706172656e745f6964203d204f4c442e646174615f69645f706172656e743b0a20202020202020206368696c645f6964203d204f4c442e646174615f69645f6368696c643b0a202020202020202072656c6174696f6e736869705f6964203d204f4c442e72656c6174696f6e736869705f69643b0a2020202020202020706172656e745f6368696c645f66726f7a656e203d204f4c442e706172656e745f66726f7a656e204f52204f4c442e6368696c645f66726f7a656e3b0a2020202020202020636f6e745f636f6d705f66726f7a656e203d204f4c442e636f6e745f66726f7a656e204f52204f4c442e636f6d705f66726f7a656e3b0a20202020454c53454946202854475f4f50203d2027494e534552542729205448454e0a2020202020202020706172656e745f6964203d204e45572e646174615f69645f706172656e743b0a20202020202020206368696c645f6964203d204e45572e646174615f69645f6368696c643b0a202020202020202072656c6174696f6e736869705f6964203d204e45572e72656c6174696f6e736869705f69643b0a2020202020202020706172656e745f6368696c645f66726f7a656e203d204e45572e706172656e745f66726f7a656e204f52204e45572e6368696c645f66726f7a656e3b0a2020202020202020636f6e745f636f6d705f66726f7a656e203d204e45572e636f6e745f66726f7a656e204f52204e45572e636f6d705f66726f7a656e3b0a20202020454e442049463b0a2020202053454c45435420636f646520494e544f2072656c6174696f6e736869702046524f4d2072656c6174696f6e736869705f7479706573205748455245206964203d2072656c6174696f6e736869705f69643b0a202020204946202872656c6174696f6e73686970203d2027504152454e545f4348494c442720414e4420706172656e745f6368696c645f66726f7a656e29204f52202872656c6174696f6e73686970203d2027434f4e5441494e45525f434f4d504f4e454e542720414e4420636f6e745f636f6d705f66726f7a656e29205448454e0a20202020202020524149534520455843455054494f4e20274f7065726174696f6e20252025206973206e6f7420616c6c6f77656420626563617573652064617461207365742025206f7220252069732066726f7a656e2e272c2054475f4f502c2072656c6174696f6e736869702c0a2020202020202020202020202873656c65637420636f64652066726f6d20646174615f616c6c207768657265206964203d20706172656e745f6964292c0a2020202020202020202020202873656c65637420636f64652066726f6d20646174615f616c6c207768657265206964203d206368696c645f6964293b0a20202020454e442049463b0a202020204946202854475f4f50203d202744454c4554452729205448454e0a202020202020202052455455524e204f4c443b0a20202020454c53454946202854475f4f50203d2027494e534552542729205448454e0a202020202020202052455455524e204e45573b0a20202020454e442049463b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f494e53455254204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c3b0a435245415445205452494747455220444154415f5345545f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f494e53455254204245464f524520494e53455254204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e504152454e545f46524f5a454e204f52204e45572e4348494c445f46524f5a454e204f52204e45572e434f4e545f46524f5a454e204f52204e45572e434f4d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f5345545f52454c4154494f4e5348495028293b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f44454c455445204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c3b0a435245415445205452494747455220444154415f5345545f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f44454c455445204245464f52452044454c455445204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e504152454e545f46524f5a454e204f52204f4c442e4348494c445f46524f5a454e204f52204f4c442e434f4e545f46524f5a454e204f52204f4c442e434f4d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f5345545f52454c4154494f4e5348495028293b0a0a2d2d204461746120736574206578706572696d656e742072656c6174696f6e736869700a44524f50205452494747455220494620455849535453204144445f444154415f5345545f544f5f4558504552494d454e545f434845434b204f4e20444154415f414c4c3b0a4352454154452054524947474552204144445f444154415f5345545f544f5f4558504552494d454e545f434845434b20414654455220494e53455254204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e455850455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e545f52454c4154494f4e534849502827646174612073657427293b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f4558504552494d454e545f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f555044415445204f4e20444154415f414c4c3b0a435245415445205452494747455220444154415f5345545f4558504552494d454e545f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f555044415445204245464f524520555044415445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20280a2020202020202020284e45572e455850455f4944203c3e204f4c442e455850455f49440a2020202020202020204f5220284e45572e455850455f4944204953204e4f54204e554c4c20414e44204f4c442e455850455f4944204953204e554c4c290a2020202020202020204f5220284e45572e455850455f4944204953204e554c4c20414e44204f4c442e455850455f4944204953204e4f54204e554c4c29290a2020202020202020414e4420284e45572e455850455f46524f5a454e204f52204f4c442e455850455f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f4558504552494d454e545f52454c4154494f4e534849502827646174612073657427293b0a0a2d2d2044617461207365742073616d706c652072656c6174696f6e736869700a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f455843455054494f4e5f46524f5a454e5f444154415f5345545f53414d504c455f52454c4154494f4e5348495028292052455455524e5320747269676765722041532024240a4445434c4152450a2020202073616d706c655f6964202020544543485f49443b0a202020206f7065726174696f6e202020544558543b0a424547494e0a20202020494620284e45572e73616d705f6964204953204e4f54204e554c4c20414e44204e45572e73616d705f66726f7a656e29205448454e0a202020202020202073616d706c655f6964203d204e45572e73616d705f69643b0a20202020202020206f7065726174696f6e203d20275345542053414d504c45273b0a20202020454c5345494620284f4c442e73616d705f6964204953204e4f54204e554c4c20414e44204f4c442e73616d705f66726f7a656e29205448454e0a202020202020202073616d706c655f6964203d204f4c442e73616d705f69643b0a20202020202020206f7065726174696f6e203d202752454d4f56452053414d504c45273b0a20202020454e442049463b0a0a20202020524149534520455843455054494f4e20274f7065726174696f6e2025206973206e6f7420616c6c6f77656420626563617573652073616d706c6520252069732066726f7a656e20666f7220646174612073657420252e272c206f7065726174696f6e2c0a20202020202020202873656c65637420636f64652066726f6d2073616d706c65735f616c6c207768657265206964203d2073616d706c655f6964292c204e45572e636f64653b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a44524f50205452494747455220494620455849535453204144445f444154415f5345545f544f5f53414d504c455f434845434b204f4e20444154415f414c4c3b0a4352454154452054524947474552204144445f444154415f5345545f544f5f53414d504c455f434845434b20414654455220494e53455254204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e53414d505f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f5345545f53414d504c455f52454c4154494f4e5348495028293b0a0a44524f5020545249474745522049462045584953545320444154415f5345545f53414d504c455f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f555044415445204f4e20444154415f414c4c3b0a435245415445205452494747455220444154415f5345545f53414d504c455f52454c4154494f4e534849505f46524f5a454e5f434845434b5f4f4e5f555044415445204245464f524520555044415445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20280a2020202020202020284e45572e53414d505f4944203c3e204f4c442e53414d505f49440a2020202020202020204f5220284e45572e53414d505f4944204953204e4f54204e554c4c20414e44204f4c442e53414d505f4944204953204e554c4c290a2020202020202020204f5220284e45572e53414d505f4944204953204e554c4c20414e44204f4c442e53414d505f4944204953204e4f54204e554c4c29290a2020202020202020414e4420284e45572e53414d505f46524f5a454e204f52204f4c442e53414d505f46524f5a454e29290a20202020455845435554452050524f4345445552452052414953455f455843455054494f4e5f46524f5a454e5f444154415f5345545f53414d504c455f52454c4154494f4e5348495028293b0a0a2d2d20467265657a696e6720636865636b7320666f722064656c6574696f6e0a2d2d2066726f6d207370616365202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f44454c4554455f46524f4d5f53504143455f455843455054494f4e28292052455455524e5320747269676765722041532024240a424547494e0a20202020524149534520455843455054494f4e20274f7065726174696f6e2044454c4554452025206973206e6f7420616c6c6f776564206265636175736520737061636520252069732066726f7a656e2e272c2054475f415247565b305d2c200a20202020202020202873656c65637420636f64652066726f6d20737061636573207768657265206964203d206f6c642e73706163655f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2050726f6a6563742066726f6d2073706163652064656c6574696f6e0a44524f502054524947474552204946204558495354532044454c4554455f50524f4a4543545f46524f4d5f53504143455f434845434b204f4e2050524f4a454354533b0a43524541544520545249474745522044454c4554455f50524f4a4543545f46524f4d5f53504143455f434845434b2041465445522044454c455445204f4e2050524f4a454354530a20202020464f52204541434820524f57205748454e20284f4c442e53504143455f46524f5a454e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53504143455f455843455054494f4e282750524f4a45435427293b0a0a2d2d2053616d706c652066726f6d2073706163652064656c6574696e670a44524f502054524947474552204946204558495354532054524153485f53414d504c455f46524f4d5f53504143455f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522054524153485f53414d504c455f46524f4d5f53504143455f434845434b20414654455220555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e73706163655f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53504143455f455843455054494f4e282753414d504c4527293b0a0a44524f502054524947474552204946204558495354532044454c4554455f53414d504c455f46524f4d5f53504143455f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522044454c4554455f53414d504c455f46524f4d5f53504143455f434845434b2041465445522044454c455445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e73706163655f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53504143455f455843455054494f4e282753414d504c4527293b0a0a2d2d2066726f6d2070726f6a656374202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f44454c4554455f46524f4d5f50524f4a4543545f455843455054494f4e28292052455455524e5320747269676765722041532024240a424547494e0a20202020524149534520455843455054494f4e20274f7065726174696f6e2044454c4554452025206973206e6f7420616c6c6f77656420626563617573652070726f6a65637420252069732066726f7a656e2e272c2054475f415247565b305d2c200a20202020202020202873656c65637420636f64652066726f6d2070726f6a65637473207768657265206964203d206f6c642e70726f6a5f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d204578706572696d656e742066726f6d2070726f6a6563742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f4558504552494d454e545f46524f4d5f50524f4a4543545f434845434b204f4e204558504552494d454e54535f414c4c3b0a43524541544520545249474745522054524153485f4558504552494d454e545f46524f4d5f50524f4a4543545f434845434b20414654455220555044415445204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e70726f6a5f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f50524f4a4543545f455843455054494f4e28274558504552494d454e5427293b0a0a44524f502054524947474552204946204558495354532044454c4554455f4558504552494d454e545f46524f4d5f50524f4a4543545f434845434b204f4e204558504552494d454e54535f414c4c3b0a43524541544520545249474745522044454c4554455f4558504552494d454e545f46524f4d5f50524f4a4543545f434845434b2041465445522044454c455445204f4e204558504552494d454e54535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e70726f6a5f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f50524f4a4543545f455843455054494f4e28274558504552494d454e5427293b0a0a2d2d2053616d706c652066726f6d2070726f6a6563742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f53414d504c455f46524f4d5f50524f4a4543545f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522054524153485f53414d504c455f46524f4d5f50524f4a4543545f434845434b20414654455220555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e70726f6a5f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f50524f4a4543545f455843455054494f4e282753414d504c4527293b0a0a44524f502054524947474552204946204558495354532044454c4554455f53414d504c455f46524f4d5f50524f4a4543545f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522044454c4554455f53414d504c455f46524f4d5f50524f4a4543545f434845434b2041465445522044454c455445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e70726f6a5f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f50524f4a4543545f455843455054494f4e282753414d504c4527293b0a0a2d2d2066726f6d206578706572696d656e74202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f44454c4554455f46524f4d5f4558504552494d454e545f455843455054494f4e28292052455455524e5320747269676765722041532024240a424547494e0a20202020524149534520455843455054494f4e20274f7065726174696f6e2044454c4554452025206973206e6f7420616c6c6f7765642062656361757365206578706572696d656e7420252069732066726f7a656e2e272c2054475f415247565b305d2c200a20202020202020202873656c65637420636f64652066726f6d206578706572696d656e74735f616c6c207768657265206964203d206f6c642e657870655f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2053616d706c652066726f6d206578706572696d656e742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f53414d504c455f46524f4d5f4558504552494d454e545f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522054524153485f53414d504c455f46524f4d5f4558504552494d454e545f434845434b20414654455220555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e657870655f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f4558504552494d454e545f455843455054494f4e282753414d504c4527293b0a0a44524f502054524947474552204946204558495354532044454c4554455f53414d504c455f46524f4d5f4558504552494d454e545f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522044454c4554455f53414d504c455f46524f4d5f4558504552494d454e545f434845434b2041465445522044454c455445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e657870655f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f4558504552494d454e545f455843455054494f4e282753414d504c4527293b0a0a2d2d2044617461207365742066726f6d206578706572696d656e742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f444154415f5345545f46524f4d5f4558504552494d454e545f434845434b204f4e20444154415f414c4c3b0a43524541544520545249474745522054524153485f444154415f5345545f46524f4d5f4558504552494d454e545f434845434b20414654455220555044415445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e657870655f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f4558504552494d454e545f455843455054494f4e2827444154412053455427293b0a0a44524f502054524947474552204946204558495354532044454c4554455f444154415f5345545f46524f4d5f4558504552494d454e545f434845434b204f4e20444154415f414c4c3b0a43524541544520545249474745522044454c4554455f444154415f5345545f46524f4d5f4558504552494d454e545f434845434b2041465445522044454c455445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e657870655f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f4558504552494d454e545f455843455054494f4e2827444154412053455427293b0a0a2d2d2066726f6d2073616d706c65202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e28292052455455524e5320747269676765722041532024240a4445434c4152450a2020202073616d705f696420544543485f49443b0a424547494e0a202020204946202854475f415247565b305d203d202753414d504c45204348494c442729205448454e0a202020202020202073616d705f6964203d206f6c642e73616d706c655f69645f706172656e743b0a20202020454c53454946202854475f415247565b305d203d202753414d504c4520504152454e542729205448454e0a202020202020202073616d705f6964203d206f6c642e73616d706c655f69645f6368696c643b0a20202020454c53454946202854475f415247565b305d203d202753414d504c4520434f4d504f4e454e542729205448454e0a202020202020202073616d705f6964203d206f6c642e73616d705f69645f706172745f6f663b0a20202020454c53450a202020202020202073616d705f6964203d206f6c642e73616d705f69643b0a20202020454e442049463b0a20202020524149534520455843455054494f4e20274f7065726174696f6e2044454c4554452025206973206e6f7420616c6c6f77656420626563617573652073616d706c6520252069732066726f7a656e2e272c2054475f415247565b305d2c200a20202020202020202873656c65637420636f64652066726f6d2073616d706c65735f616c6c207768657265206964203d2073616d705f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2053616d706c652066726f6d20636f6e7461696e65722064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f53414d504c455f46524f4d5f434f4e5441494e45525f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522054524153485f53414d504c455f46524f4d5f434f4e5441494e45525f434845434b20414654455220555044415445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e636f6e745f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e282753414d504c4520434f4d504f4e454e5427293b0a0a44524f502054524947474552204946204558495354532044454c4554455f53414d504c455f46524f4d5f434f4e5441494e45525f434845434b204f4e2053414d504c45535f414c4c3b0a43524541544520545249474745522044454c4554455f53414d504c455f46524f4d5f434f4e5441494e45525f434845434b2041465445522044454c455445204f4e2053414d504c45535f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e636f6e745f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e282753414d504c4520434f4d504f4e454e5427293b0a0a2d2d2053616d706c652066726f6d20706172656e742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f53414d504c455f46524f4d5f504152454e545f434845434b204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522054524153485f53414d504c455f46524f4d5f504152454e545f434845434b20414654455220555044415445204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e706172656e745f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e282753414d504c45204348494c4427293b0a0a2d2d2053616d706c652066726f6d206368696c642064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f53414d504c455f46524f4d5f4348494c445f434845434b204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522054524153485f53414d504c455f46524f4d5f4348494c445f434845434b20414654455220555044415445204f4e2053414d504c455f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e6368696c645f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e282753414d504c4520504152454e5427293b0a0a2d2d2044617461207365742066726f6d2073616d706c652064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f444154415f5345545f46524f4d5f53414d504c455f434845434b204f4e20444154415f414c4c3b0a43524541544520545249474745522054524153485f444154415f5345545f46524f4d5f53414d504c455f434845434b20414654455220555044415445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e73616d705f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e2827444154412053455427293b0a0a44524f502054524947474552204946204558495354532044454c4554455f444154415f5345545f46524f4d5f53414d504c455f434845434b204f4e20444154415f414c4c3b0a43524541544520545249474745522044454c4554455f444154415f5345545f46524f4d5f53414d504c455f434845434b2041465445522044454c455445204f4e20444154415f414c4c0a20202020464f52204541434820524f57205748454e20284f4c442e73616d705f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f53414d504c455f455843455054494f4e2827444154412053455427293b0a0a2d2d2066726f6d206461746120736574202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a435245415445204f52205245504c4143452046554e4354494f4e2052414953455f44454c4554455f46524f4d5f444154415f5345545f455843455054494f4e28292052455455524e5320747269676765722041532024240a4445434c4152450a20202020646174615f696420544543485f49443b0a424547494e0a202020204946202854475f415247565b305d203d20274441544120534554204348494c442729205448454e0a2020202020202020646174615f6964203d206f6c642e646174615f69645f706172656e743b0a20202020454c53454946202854475f415247565b305d203d2027444154412053455420504152454e542729205448454e0a2020202020202020646174615f6964203d206f6c642e646174615f69645f6368696c643b0a20202020454c53454946202854475f415247565b305d203d2027444154412053455420434f4d504f4e454e542729205448454e0a2020202020202020646174615f6964203d206f6c642e646174615f69645f706172656e743b0a20202020454c53454946202854475f415247565b305d203d2027444154412053455420434f4e5441494e45522729205448454e0a2020202020202020646174615f6964203d206f6c642e646174615f69645f6368696c643b0a20202020454e442049463b0a20202020524149534520455843455054494f4e20274f7065726174696f6e2044454c4554452025206973206e6f7420616c6c6f776564206265636175736520646174612073657420252069732066726f7a656e2e272c2054475f415247565b305d2c200a20202020202020202873656c65637420636f64652066726f6d20646174615f616c6c207768657265206964203d20646174615f6964293b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a2d2d2044617461207365742066726f6d20706172656e742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f444154415f5345545f46524f4d5f504152454e545f434845434b204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522054524153485f444154415f5345545f46524f4d5f504152454e545f434845434b20414654455220555044415445204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e706172656e745f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f444154415f5345545f455843455054494f4e28274441544120534554204348494c4427293b0a0a2d2d2044617461207365742066726f6d206368696c642064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f444154415f5345545f46524f4d5f4348494c445f434845434b204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522054524153485f444154415f5345545f46524f4d5f4348494c445f434845434b20414654455220555044415445204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e6368696c645f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f444154415f5345545f455843455054494f4e2827444154412053455420504152454e5427293b0a0a2d2d2044617461207365742066726f6d20636f6e7461696e65722064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f444154415f5345545f46524f4d5f434f4e5441494e45525f434845434b204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522054524153485f444154415f5345545f46524f4d5f434f4e5441494e45525f434845434b20414654455220555044415445204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e636f6e745f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f444154415f5345545f455843455054494f4e2827444154412053455420434f4d504f4e454e5427293b0a0a2d2d2044617461207365742066726f6d20636f6d706f6e656e742064656c6574696f6e0a44524f502054524947474552204946204558495354532054524153485f444154415f5345545f46524f4d5f434f4d504f4e454e545f434845434b204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c3b0a43524541544520545249474745522054524153485f444154415f5345545f46524f4d5f434f4d504f4e454e545f434845434b20414654455220555044415445204f4e20444154415f5345545f52454c4154494f4e53484950535f414c4c0a20202020464f52204541434820524f57205748454e20284e45572e64656c5f6964204953204e4f54204e554c4c20414e44204f4c442e64656c5f6964204953204e554c4c20414e44204f4c442e636f6d705f66726f7a656e290a20202020455845435554452050524f4345445552452052414953455f44454c4554455f46524f4d5f444154415f5345545f455843455054494f4e2827444154412053455420434f4e5441494e455227293b0a0a2d2d20656e64206f6620747269676765727320666f7220667265657a696e670a	\N
178	./sql/postgresql/178/grants-178.sql	SUCCESS	2020-10-01 08:03:14.217	\\x2d2d204772616e74696e672053454c4543542070726976696c65676520746f2067726f7570204f50454e4249535f524541444f4e4c590a0a4752414e542053454c454354204f4e2053455155454e4345206174746163686d656e745f636f6e74656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206174746163686d656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520636f64655f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f636f64655f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f636f64655f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520636f6e74726f6c6c65645f766f636162756c6172795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520636f72655f706c7567696e5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520637674655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f7365745f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f7365745f72656c6174696f6e736869705f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f7365745f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f73746f72655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520444154415f53544f52455f53455256494345535f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520646174615f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452064617461626173655f696e7374616e63655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452064737470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520657470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206576656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206578706572696d656e745f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452066696c655f666f726d61745f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073706163655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452064656c6574696f6e5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206c6f6361746f725f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d6174657269616c5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d6174657269616c5f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d6174657269616c5f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345206d7470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345207065726d5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520706572736f6e5f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452070726f6a6563745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452070726f70657274795f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520726f6c655f61737369676e6d656e745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f70726f70657274795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520737470745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520617574686f72697a6174696f6e5f67726f75705f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452066696c7465725f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452071756572795f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520504f53545f524547495354524154494f4e5f444154415345545f51554555455f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520454e544954595f4f5045524154494f4e535f4c4f475f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345204558504552494d454e545f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452053414d504c455f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e434520444154415f5345545f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452050524f4a4543545f52454c4154494f4e53484950535f484953544f52595f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d5f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345204d45544150524f4a4543545f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b200a4752414e542053454c454354204f4e2053455155454e4345204d45544150524f4a4543545f41535349474e4d454e545f49445f53455120544f2047524f5550204f50454e4249535f524541444f4e4c593b200a4752414e542053454c454354204f4e2053455155454e434520677269645f637573746f6d5f636f6c756d6e735f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452073616d706c655f72656c6174696f6e736869705f69645f7365712020544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e4345207363726970745f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2053455155454e43452072656c6174696f6e736869705f747970655f69645f73657120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206174746163686d656e745f636f6e74656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206174746163686d656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520636f6e74726f6c6c65645f766f636162756c617269657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520636f6e74726f6c6c65645f766f636162756c6172795f7465726d7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520636f72655f706c7567696e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206461746120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e20646174615f64656c6574656420544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e20646174615f7365745f72656c6174696f6e736869707320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f72656c6174696f6e73686970735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f7365745f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f73746f72657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520646174615f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520444154415f53544f52455f534552564943455320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520444154415f53544f52455f534552564943455f444154415f5345545f545950455320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452064617461626173655f76657273696f6e5f6c6f677320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206576656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e745f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206578706572696d656e74735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206578706572696d656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206578706572696d656e74735f64656c6574656420544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452065787465726e616c5f6461746120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452066696c655f666f726d61745f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073706163657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452064656c6574696f6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206c6f6361746f725f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c5f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45206d6174657269616c7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520706572736f6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452070726f6a6563747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452070726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520726f6c655f61737369676e6d656e747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f70726f7065727469657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f70726f706572746965735f686973746f727920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f747970655f70726f70657274795f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c65735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c65735f64656c6574656420544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c655f72656c6174696f6e736869707320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452073616d706c655f72656c6174696f6e73686970735f616c6c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520617574686f72697a6174696f6e5f67726f75707320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520617574686f72697a6174696f6e5f67726f75705f706572736f6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452066696c7465727320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45207175657269657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45207363726970747320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520504f53545f524547495354524154494f4e5f444154415345545f515545554520544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520454e544954595f4f5045524154494f4e535f4c4f4720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204558504552494d454e545f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452053414d504c455f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520444154415f5345545f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452050524f4a4543545f52454c4154494f4e53484950535f484953544f525920544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e2073616d706c655f686973746f72795f7669657720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e20646174615f7365745f686973746f72795f7669657720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e206578706572696d656e745f686973746f72795f7669657720544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452045585445524e414c5f444154415f4d414e4147454d454e545f53595354454d5320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204c494e4b5f4441544120544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c4520677269645f637573746f6d5f636f6c756d6e7320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c452072656c6174696f6e736869705f747970657320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204d45544150524f4a4543545320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204d45544150524f4a4543545f41535349474e4d454e54535f414c4c20544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e204d45544150524f4a4543545f41535349474e4d454e545320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a4752414e542053454c454354204f4e205441424c45204f5045524154494f4e5f455845435554494f4e5320544f2047524f5550204f50454e4249535f524541444f4e4c593b0a	\N
178	./sql/generic/178/data-178.sql	SUCCESS	2020-10-01 08:03:14.471	\\x2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c6520504552534f4e530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f20706572736f6e730a2869640a2c66697273745f6e616d650a2c6c6173745f6e616d650a2c757365725f69640a2c656d61696c290a76616c7565730a286e65787476616c2827504552534f4e5f49445f53455127290a2c27270a2c2753797374656d2055736572270a2c2773797374656d270a2c2727293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520436f6e74726f6c6c656420566f636162756c6172792053544f524147455f464f524d41540a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a696e7365727420696e746f20636f6e74726f6c6c65645f766f636162756c6172696573200a20202020202020282069640a202020202020202c20636f64650a202020202020202c2069735f696e7465726e616c5f6e616d6573706163652020202020200a202020202020202c206465736372697074696f6e0a202020202020202c20706572735f69645f726567697374657265720a202020202020202c2069735f6d616e616765645f696e7465726e616c6c79290a76616c7565732020286e65787476616c2827434f4e54524f4c4c45445f564f434142554c4152595f49445f53455127290a202020202020202c202753544f524147455f464f524d4154270a202020202020202c20747275650a202020202020202c2027546865206f6e2d6469736b2073746f7261676520666f726d6174206f662061206461746120736574270a202020202020202c202873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a202020202020202c2074727565293b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a202043726561746520436f6e74726f6c6c656420566f636162756c617279205465726d7320666f722053544f524147455f464f524d41540a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a696e7365727420696e746f20636f6e74726f6c6c65645f766f636162756c6172795f7465726d73200a20202020202020282069640a202020202020202c20636f64650a202020202020202c20636f766f5f6964200a202020202020202c20706572735f69645f726567697374657265720a202020202020202c206f7264696e616c20290a76616c7565732020286e65787476616c2827435654455f49445f53455127290a202020202020202c202750524f5052494554415259270a202020202020202c202873656c6563742069642066726f6d20636f6e74726f6c6c65645f766f636162756c617269657320776865726520636f6465203d202753544f524147455f464f524d41542720616e642069735f696e7465726e616c5f6e616d657370616365203d2074727565290a202020202020202c202873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a202020202020202c2031293b0a0a696e7365727420696e746f20636f6e74726f6c6c65645f766f636162756c6172795f7465726d73200a20202020202020282069640a202020202020202c20636f64650a202020202020202c20636f766f5f6964200a202020202020202c20706572735f69645f726567697374657265720a202020202020202c206f7264696e616c290a76616c7565732020286e65787476616c2827435654455f49445f53455127290a202020202020202c20274244535f4449524543544f5259270a202020202020202c202873656c6563742069642066726f6d20636f6e74726f6c6c65645f766f636162756c617269657320776865726520636f6465203d202753544f524147455f464f524d41542720616e642069735f696e7465726e616c5f6e616d657370616365203d2074727565290a202020202020202c202873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a202020202020202c2032293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c6520444154415f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c2756415243484152270a2c2753686f72742074657874270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c274d554c54494c494e455f56415243484152270a202c274c6f6e672074657874270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c27494e5445474552270a2c27496e7465676572206e756d626572270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c275245414c270a2c275265616c206e756d6265722c20692e652e20616e20696e65786163742c207661726961626c652d707265636973696f6e206e756d657269632074797065270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c27424f4f4c45414e270a2c2754727565206f722046616c7365270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f545950455f49445f53455127290a2c2754494d455354414d50270a2c27426f7468206461746520616e642074696d652e20466f726d61743a20797979792d6d6d2d64642068683a6d6d3a7373270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c27434f4e54524f4c4c4544564f434142554c415259270a202c27436f6e74726f6c6c656420566f636162756c617279270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c274d4154455249414c270a202c275265666572656e636520746f2061206d6174657269616c270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c2748595045524c494e4b270a202c2741646472657373206f662061207765622070616765270a293b0a0a696e7365727420696e746f20646174615f74797065730a2869640a202c636f64650a202c6465736372697074696f6e290a2076616c756573200a20286e65787476616c2827444154415f545950455f49445f53455127290a202c27584d4c270a202c27584d4c20646f63756d656e74270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c652050524f50455254595f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2070726f70657274795f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a2c6c6162656c0a2c646174795f69640a2c706572735f69645f72656769737465726572290a76616c756573200a286e65787476616c282750524f50455254595f545950455f49445f53455127290a2c274445534352495054494f4e270a2c2741204465736372697074696f6e270a2c274465736372697074696f6e270a2c2873656c6563742069642066726f6d20646174615f747970657320776865726520636f6465203d275641524348415227290a2c2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c65204558504552494d454e545f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f206578706572696d656e745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c28274558504552494d454e545f545950455f49445f53455127290a2c27554e4b4e4f574e270a2c27556e6b6e6f776e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c652053414d504c455f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2073616d706c655f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c282753414d504c455f545950455f49445f53455127290a2c27554e4b4e4f574e270a2c27556e6b6e6f776e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c6520444154415f5345545f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f20646174615f7365745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c2827444154415f5345545f545950455f49445f53455127290a2c27554e4b4e4f574e270a2c27556e6b6e6f776e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c652046494c455f464f524d41545f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c2748444635270a2c2748696572617263686963616c204461746120466f726d61742046696c652c2076657273696f6e2035270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c2750524f5052494554415259270a2c2750726f707269657461727920466f726d61742046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c27535246270a2c2753657175656e6365205265616420466f726d61742046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c2754494646270a2c27544946462046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c27545356270a2c27546162205365706172617465642056616c7565732046696c65270a293b0a0a696e7365727420696e746f2066696c655f666f726d61745f74797065730a2869640a2c636f64650a2c6465736372697074696f6e0a290a76616c756573200a286e65787476616c282746494c455f464f524d41545f545950455f49445f53455127290a2c27584d4c270a2c27584d4c2046696c65270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c20646174612073657420696e746f20746865207461626c65204c4f4341544f525f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f206c6f6361746f725f74797065730a2869640a2c636f64650a2c6465736372697074696f6e290a76616c756573200a286e65787476616c28274c4f4341544f525f545950455f49445f53455127290a2c2752454c41544956455f4c4f434154494f4e270a2c2752656c6174697665204c6f636174696f6e270a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020496e7365727420616e20696e697469616c206461746120696e746f207461626c652052454c4154494f4e534849505f54595045530a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f2072656c6174696f6e736869705f74797065730a2869642c200a636f64652c200a6c6162656c2c200a706172656e745f6c6162656c2c200a6368696c645f6c6162656c2c200a6465736372697074696f6e2c200a706572735f69645f726567697374657265722c200a69735f6d616e616765645f696e7465726e616c6c792c200a69735f696e7465726e616c5f6e616d6573706163650a29200a76616c7565730a280a6e65787476616c282752454c4154494f4e534849505f545950455f49445f53455127292c0a27504152454e545f4348494c44272c0a27506172656e74202d204368696c64272c200a27506172656e74272c200a274368696c64272c200a27506172656e74202d204368696c642072656c6174696f6e73686970272c200a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27292c200a2754272c200a2754270a293b0a0a696e7365727420696e746f2072656c6174696f6e736869705f74797065730a2869642c200a636f64652c200a6c6162656c2c200a706172656e745f6c6162656c2c200a6368696c645f6c6162656c2c200a6465736372697074696f6e2c200a706572735f69645f726567697374657265722c200a69735f6d616e616765645f696e7465726e616c6c792c200a69735f696e7465726e616c5f6e616d65737061636529200a76616c7565730a280a6e65787476616c282752454c4154494f4e534849505f545950455f49445f53455127292c0a27504c4154455f434f4e54524f4c5f4c41594f5554272c0a27506c617465202d20436f6e74726f6c204c61796f7574272c200a27506c617465272c200a27436f6e74726f6c204c61796f7574272c200a27506c617465202d20436f6e74726f6c204c61796f75742072656c6174696f6e73686970272c200a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27292c200a2754272c200a2754270a293b0a0a696e7365727420696e746f2072656c6174696f6e736869705f74797065730a2869642c200a636f64652c200a6c6162656c2c200a706172656e745f6c6162656c2c200a6368696c645f6c6162656c2c200a6465736372697074696f6e2c200a706572735f69645f726567697374657265722c200a69735f6d616e616765645f696e7465726e616c6c792c200a69735f696e7465726e616c5f6e616d65737061636529200a76616c7565730a280a6e65787476616c282752454c4154494f4e534849505f545950455f49445f53455127292c0a27434f4e5441494e45525f434f4d504f4e454e54272c0a27436f6e7461696e6572202d20436f6d706f6e656e74272c200a27436f6e7461696e6572272c200a27436f6d706f6e656e74272c200a27436f6e7461696e6572202d20436f6d706f6e656e742072656c6174696f6e73686970272c200a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27292c200a2754272c200a275427293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c742073706163650a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a696e7365727420696e746f207370616365730a2869642c0a636f64652c0a706572735f69645f72656769737465726572290a76616c7565730a280a6e65787476616c282753504143455f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c742070726f6a6563740a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a73656c656374206e65787476616c282750524f4a4543545f49445f53455127293b0a696e7365727420696e746f2070726f6a656374730a2869642c0a7065726d5f69642c0a636f64652c0a73706163655f69642c0a706572735f69645f72656769737465726572290a76616c7565730a280a6375727276616c282750524f4a4543545f49445f53455127292c0a746f5f63686172286e6f7728292c2027595959594d4d4444484832344d4953534d5327297c7c272d277c7c6375727276616c282750524f4a4543545f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d2073706163657320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c74206578706572696d656e740a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a73656c656374206e65787476616c28274558504552494d454e545f49445f53455127293b0a696e7365727420696e746f206578706572696d656e74735f616c6c0a2869642c0a7065726d5f69642c0a636f64652c0a70726f6a5f69642c0a657874795f69642c0a706572735f69645f72656769737465726572290a76616c7565730a280a6375727276616c28274558504552494d454e545f49445f53455127292c0a746f5f63686172286e6f7728292c2027595959594d4d4444484832344d4953534d5327297c7c272d277c7c6375727276616c28274558504552494d454e545f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d2070726f6a6563747320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d206578706572696d656e745f747970657320776865726520636f6465203d2027554e4b4e4f574e27292c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652064656661756c742073616d706c650a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a73656c656374206e65787476616c282753414d504c455f49445f53455127293b0a696e7365727420696e746f2073616d706c65735f616c6c0a2869642c0a7065726d5f69642c0a636f64652c0a657870655f69642c0a73706163655f69642c0a736174795f69642c0a706572735f69645f72656769737465726572290a76616c7565730a280a6375727276616c282753414d504c455f49445f53455127292c0a746f5f63686172286e6f7728292c2027595959594d4d4444484832344d4953534d5327297c7c272d277c7c6375727276616c282753414d504c455f49445f53455127292c0a2744454641554c54272c0a2873656c6563742069642066726f6d206578706572696d656e747320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d2073706163657320776865726520636f6465203d202744454641554c5427292c0a2873656c6563742069642066726f6d2073616d706c655f747970657320776865726520636f6465203d2027554e4b4e4f574e27292c0a2873656c6563742069642066726f6d20706572736f6e7320776865726520757365725f6964203d2773797374656d27290a293b0a0a	\N
\.


--
-- Data for Name: deletions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.deletions (id, pers_id_registerer, registration_timestamp, reason) FROM stdin;
\.


--
-- Data for Name: entity_operations_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.entity_operations_log (id, registration_id) FROM stdin;
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.events (id, event_type, description, reason, pers_id_registerer, registration_timestamp, entity_type, identifiers, content, exac_id) FROM stdin;
\.


--
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.experiment_properties (id, expe_id, etpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, pers_id_author, modification_timestamp, expe_frozen) FROM stdin;
\.


--
-- Data for Name: experiment_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Data for Name: experiment_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.experiment_relationships_history (id, main_expe_id, relation_type, samp_id, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp, proj_id) FROM stdin;
1	1	OWNED	\N	\N	20201001080314224-1	\N	2020-10-01 08:03:14.224058+02	\N	1
2	1	OWNER	1	\N	20201001080314224-1	\N	2020-10-01 08:03:14.224058+02	\N	\N
\.


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.experiment_types (id, code, description, modification_timestamp, validation_script_id) FROM stdin;
1	UNKNOWN	Unknown	2020-10-01 08:03:14.224058+02	\N
\.


--
-- Data for Name: experiments_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.experiments_all (id, perm_id, code, exty_id, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, pers_id_modifier, version, frozen, frozen_for_samp, frozen_for_data, proj_frozen) FROM stdin;
1	20201001080314224-1	DEFAULT	1	1	2020-10-01 08:03:14.224058+02	2020-10-01 08:03:14.224058+02	1	\N	\N	f	\N	0	f	f	f	f
\.


--
-- Data for Name: external_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.external_data (id, share_id, size, location, ffty_id, loty_id, cvte_id_stor_fmt, is_complete, cvte_id_store, status, present_in_archive, speed_hint, storage_confirmation, h5_folders, h5ar_folders, archiving_requested) FROM stdin;
\.


--
-- Data for Name: external_data_management_systems; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.external_data_management_systems (id, code, label, address, address_type) FROM stdin;
\.


--
-- Data for Name: file_format_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.file_format_types (id, code, description) FROM stdin;
1	HDF5	Hierarchical Data Format File, version 5
2	PROPRIETARY	Proprietary Format File
3	SRF	Sequence Read Format File
4	TIFF	TIFF File
5	TSV	Tab Separated Values File
6	XML	XML File
\.


--
-- Data for Name: filters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.filters (id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
\.


--
-- Data for Name: grid_custom_columns; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.grid_custom_columns (id, code, label, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
\.


--
-- Data for Name: link_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.link_data (id, data_frozen) FROM stdin;
\.


--
-- Data for Name: locator_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.locator_types (id, code, description) FROM stdin;
1	RELATIVE_LOCATION	Relative Location
\.


--
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.material_properties (id, mate_id, mtpt_id, value, registration_timestamp, pers_id_author, modification_timestamp, pers_id_registerer, cvte_id, mate_prop_id) FROM stdin;
\.


--
-- Data for Name: material_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.material_types (id, code, description, modification_timestamp, validation_script_id) FROM stdin;
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.materials (id, code, maty_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: metaproject_assignments_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.metaproject_assignments_all (id, mepr_id, expe_id, samp_id, data_id, mate_id, del_id, creation_date) FROM stdin;
\.


--
-- Data for Name: metaprojects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.metaprojects (id, name, description, owner, private, creation_date) FROM stdin;
\.


--
-- Data for Name: operation_executions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.operation_executions (id, code, state, owner, description, notification, availability, availability_time, summary_operations, summary_progress, summary_error, summary_results, summary_availability, summary_availability_time, details_path, details_availability, details_availability_time, creation_date, start_date, finish_date) FROM stdin;
\.


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.persons (id, first_name, last_name, user_id, email, space_id, registration_timestamp, pers_id_registerer, display_settings, is_active) FROM stdin;
1		System User	system		\N	2020-10-01 08:03:14.224058+02	\N	\N	t
2			etlserver		\N	2020-10-01 08:03:47.520408+02	1	\\xaced00057372004163682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e446973706c617953657474696e6773000000000000000102000f5a0009646562756767696e675a001669676e6f72654c617374486973746f7279546f6b656e5a00176c65676163794d656461646174615549456e61626c65644c000e636f6c756d6e53657474696e677374000f4c6a6176612f7574696c2f4d61703b4c001b637573746f6d576562417070446973706c617953657474696e677371007e00014c000e64656661756c7450726f6a6563747400124c6a6176612f6c616e672f537472696e673b4c001064726f70446f776e53657474696e677371007e00014c00166c617374486973746f7279546f6b656e4f724e756c6c71007e00024c001670616e656c436f6c6c617073656453657474696e677371007e00014c001170616e656c53697a6553657474696e677371007e00014c0015706f72746c6574436f6e66696775726174696f6e7371007e00014c001d7265616c4e756d626572466f726d6174696e67506172616d65746572737400514c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f5265616c4e756d626572466f726d6174696e67506172616d65746572733b4c000b74616253657474696e677371007e00014c001a746563686e6f6c6f6779537065636966696353657474696e677371007e00014c00067669736974737400104c6a6176612f7574696c2f4c6973743b7870000000737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f40000000000000770800000010000000007870707371007e00063f400000000000007708000000100000000078707371007e00063f4000000000000077080000001000000000787371007e00063f400000000000007708000000100000000078707372004f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e5265616c4e756d626572466f726d6174696e67506172616d657465727300000000000000010200035a0010666f726d6174696e67456e61626c6564490009707265636973696f6e5a000a736369656e746966696378700100000004007371007e00063f4000000000000077080000001000000000787371007e00063f40000000000000770800000010000000007870	t
3			admin		\N	2020-10-01 08:03:54.892794+02	1	\\xaced00057372004163682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e446973706c617953657474696e6773000000000000000102000f5a0009646562756767696e675a001669676e6f72654c617374486973746f7279546f6b656e5a00176c65676163794d656461646174615549456e61626c65644c000e636f6c756d6e53657474696e677374000f4c6a6176612f7574696c2f4d61703b4c001b637573746f6d576562417070446973706c617953657474696e677371007e00014c000e64656661756c7450726f6a6563747400124c6a6176612f6c616e672f537472696e673b4c001064726f70446f776e53657474696e677371007e00014c00166c617374486973746f7279546f6b656e4f724e756c6c71007e00024c001670616e656c436f6c6c617073656453657474696e677371007e00014c001170616e656c53697a6553657474696e677371007e00014c0015706f72746c6574436f6e66696775726174696f6e7371007e00014c001d7265616c4e756d626572466f726d6174696e67506172616d65746572737400514c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f5265616c4e756d626572466f726d6174696e67506172616d65746572733b4c000b74616253657474696e677371007e00014c001a746563686e6f6c6f6779537065636966696353657474696e677371007e00014c00067669736974737400104c6a6176612f7574696c2f4c6973743b7870000000737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000174001c726f6c652d61737369676e6d656e742d62726f777365722d67726964737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a657870000000057704000000057372003f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e436f6c756d6e53657474696e6700000000000000010200055a000968617346696c7465725a000668696464656e49000577696474684c0008636f6c756d6e494471007e00024c0007736f72744469727400444c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f536f7274496e666f24536f72744469723b7870010000000096740006504552534f4e707371007e000b010000000096740013415554484f52495a4154494f4e5f47524f5550707371007e000b0100000000967400055350414345707371007e000b01000000009674000750524f4a454354707371007e000b010000000096740004524f4c4570787870707371007e00063f400000000000007708000000100000000078707371007e00063f4000000000000077080000001000000000787371007e00063f400000000000007708000000100000000078707372004f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e5265616c4e756d626572466f726d6174696e67506172616d657465727300000000000000010200035a0010666f726d6174696e67456e61626c6564490009707265636973696f6e5a000a736369656e746966696378700100000004007371007e00063f4000000000000077080000001000000000787371007e00063f4000000000000077080000001000000000787371007e00090000000077040000000078	t
\.


--
-- Data for Name: post_registration_dataset_queue; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.post_registration_dataset_queue (id, ds_id) FROM stdin;
\.


--
-- Data for Name: project_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.project_relationships_history (id, main_proj_id, relation_type, expe_id, space_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
1	1	OWNED	\N	1	DEFAULT	\N	2020-10-01 08:03:14.224058+02	\N
2	1	OWNER	1	\N	20201001080314224-1	\N	2020-10-01 08:03:14.224058+02	\N
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.projects (id, perm_id, code, space_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp, pers_id_modifier, version, frozen, frozen_for_exp, frozen_for_samp, space_frozen) FROM stdin;
1	20201001080314224-1	DEFAULT	1	\N	\N	1	2020-10-01 08:03:14.224058+02	2020-10-01 08:03:14.224058+02	\N	0	f	f	f	f
\.


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, maty_prop_id, schema, transformation, meta_data) FROM stdin;
1	DESCRIPTION	A Description	Description	1	2020-10-01 08:03:14.224058+02	1	\N	f	f	\N	\N	\N	\N
2	SEARCH_QUERY.CUSTOM_DATA	Additional data in custom format	Custom data	10	2020-10-01 08:03:30.196+02	1	\N	f	t	\N	\N	\N	\N
3	NAME	Name	Name	1	2020-10-01 08:03:30.216+02	1	\N	f	t	\N	\N	\N	\N
4	SEARCH_QUERY.FETCH_OPTIONS	V3 API fetch options	Fetch options	10	2020-10-01 08:03:30.222+02	1	\N	f	t	\N	\N	\N	\N
5	SEARCH_QUERY.SEARCH_CRITERIA	V3 API search criteria	Search criteria	10	2020-10-01 08:03:30.227+02	1	\N	f	t	\N	\N	\N	\N
\.


--
-- Data for Name: queries; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.queries (id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, query_type, entity_type_code, db_key) FROM stdin;
\.


--
-- Data for Name: relationship_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.relationship_types (id, code, label, parent_label, child_label, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace) FROM stdin;
1	PARENT_CHILD	Parent - Child	Parent	Child	Parent - Child relationship	2020-10-01 08:03:14.224058+02	1	t	t
2	PLATE_CONTROL_LAYOUT	Plate - Control Layout	Plate	Control Layout	Plate - Control Layout relationship	2020-10-01 08:03:14.224058+02	1	t	t
3	CONTAINER_COMPONENT	Container - Component	Container	Component	Container - Component relationship	2020-10-01 08:03:14.224058+02	1	t	t
\.


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.role_assignments (id, role_code, space_id, project_id, pers_id_grantee, ag_id_grantee, pers_id_registerer, registration_timestamp) FROM stdin;
1	ETL_SERVER	\N	\N	2	\N	1	2020-10-01 08:03:47.520408+02
2	ADMIN	\N	\N	3	\N	1	2020-10-01 08:03:54.892794+02
3	ADMIN	\N	\N	1	\N	3	2020-10-01 08:04:07.579701+02
\.


--
-- Data for Name: sample_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sample_properties (id, samp_id, stpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, pers_id_author, modification_timestamp, samp_frozen) FROM stdin;
\.


--
-- Data for Name: sample_properties_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp) FROM stdin;
\.


--
-- Data for Name: sample_relationships_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sample_relationships_all (id, sample_id_parent, relationship_id, sample_id_child, del_id, pers_id_author, registration_timestamp, modification_timestamp, parent_frozen, child_frozen) FROM stdin;
\.


--
-- Data for Name: sample_relationships_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sample_relationships_history (id, main_samp_id, relation_type, expe_id, samp_id, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, valid_until_timestamp, space_id, proj_id) FROM stdin;
1	1	OWNED	1	\N	\N	20201001080314224-1	\N	2020-10-01 08:03:14.224058+02	\N	\N	\N
\.


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed, ordinal, section, script_id, is_shown_edit, show_raw_value) FROM stdin;
1	2	3	t	f	1	2020-10-01 08:03:30.314+02	t	1	General	\N	t	f
2	2	5	t	f	1	2020-10-01 08:03:30.337+02	t	2	General	\N	f	f
3	2	4	f	f	1	2020-10-01 08:03:30.359+02	t	3	General	\N	f	f
4	2	2	f	f	1	2020-10-01 08:03:30.379+02	t	4	General	\N	f	f
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sample_types (id, code, description, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique, inherit_properties, validation_script_id, show_parent_metadata) FROM stdin;
1	UNKNOWN	Unknown	t	0	0	2020-10-01 08:03:14.224058+02	f	S	f	f	\N	f
2	SEARCH_QUERY	\N	t	1	0	2020-10-01 08:03:30.256+02	t	SEARCH_QUERY.	f	f	\N	f
\.


--
-- Data for Name: samples_all; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.samples_all (id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, del_id, orig_del, space_id, samp_id_part_of, pers_id_modifier, code_unique_check, subcode_unique_check, version, proj_id, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents, frozen_for_data, space_frozen, proj_frozen, expe_frozen, cont_frozen) FROM stdin;
1	20201001080314224-1	DEFAULT	1	1	2020-10-01 08:03:14.224058+02	2020-10-01 08:03:14.224058+02	1	\N	\N	1	\N	\N	DEFAULT,-1,-1,1	\N	0	\N	f	f	f	f	f	f	f	f	f
\.


--
-- Data for Name: scripts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.scripts (id, name, script_type, description, script, registration_timestamp, pers_id_registerer, entity_kind, plugin_type, is_available) FROM stdin;
\.


--
-- Data for Name: semantic_annotations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.semantic_annotations (id, perm_id, saty_id, stpt_id, prty_id, predicate_ontology_id, predicate_ontology_version, predicate_accession_id, descriptor_ontology_id, descriptor_ontology_version, descriptor_accession_id, creation_date) FROM stdin;
\.


--
-- Data for Name: spaces; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.spaces (id, code, description, registration_timestamp, pers_id_registerer, frozen, frozen_for_proj, frozen_for_samp) FROM stdin;
1	DEFAULT	\N	2020-10-01 08:03:14.224058+02	1	f	f	f
\.


--
-- Name: attachment_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.attachment_content_id_seq', 1, false);


--
-- Name: attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.attachment_id_seq', 1, false);


--
-- Name: authorization_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.authorization_group_id_seq', 1, false);


--
-- Name: code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.code_seq', 1, false);


--
-- Name: content_copies_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.content_copies_id_seq', 1, false);


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.controlled_vocabulary_id_seq', 1, true);


--
-- Name: core_plugin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.core_plugin_id_seq', 1, true);


--
-- Name: cvte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cvte_id_seq', 2, true);


--
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_id_seq', 1, false);


--
-- Name: data_set_copies_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_set_copies_history_id_seq', 1, false);


--
-- Name: data_set_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_set_property_id_seq', 1, false);


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_set_relationship_id_seq', 1, false);


--
-- Name: data_set_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_set_relationships_history_id_seq', 1, false);


--
-- Name: data_set_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_set_type_id_seq', 1, true);


--
-- Name: data_store_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_store_id_seq', 1, true);


--
-- Name: data_store_services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_store_services_id_seq', 4, true);


--
-- Name: data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.data_type_id_seq', 10, true);


--
-- Name: database_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.database_instance_id_seq', 1, false);


--
-- Name: deletion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.deletion_id_seq', 1, false);


--
-- Name: dstpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.dstpt_id_seq', 1, false);


--
-- Name: entity_operations_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.entity_operations_log_id_seq', 1, false);


--
-- Name: etpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.etpt_id_seq', 1, false);


--
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.event_id_seq', 1, false);


--
-- Name: experiment_code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.experiment_code_seq', 1, false);


--
-- Name: experiment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.experiment_id_seq', 1, true);


--
-- Name: experiment_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.experiment_property_id_seq', 1, false);


--
-- Name: experiment_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.experiment_relationships_history_id_seq', 2, true);


--
-- Name: experiment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.experiment_type_id_seq', 1, true);


--
-- Name: external_data_management_system_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.external_data_management_system_id_seq', 1, false);


--
-- Name: file_format_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.file_format_type_id_seq', 6, true);


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.filter_id_seq', 1, false);


--
-- Name: grid_custom_columns_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.grid_custom_columns_id_seq', 1, false);


--
-- Name: locator_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.locator_type_id_seq', 1, true);


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.material_id_seq', 1, false);


--
-- Name: material_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.material_property_id_seq', 1, false);


--
-- Name: material_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.material_type_id_seq', 1, false);


--
-- Name: metaproject_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.metaproject_assignment_id_seq', 1, false);


--
-- Name: metaproject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.metaproject_id_seq', 1, false);


--
-- Name: mtpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.mtpt_id_seq', 1, false);


--
-- Name: operation_executions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.operation_executions_id_seq', 1, false);


--
-- Name: perm_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.perm_id_seq', 1, false);


--
-- Name: person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.person_id_seq', 3, true);


--
-- Name: post_registration_dataset_queue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.post_registration_dataset_queue_id_seq', 1, false);


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.project_id_seq', 1, true);


--
-- Name: project_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.project_relationships_history_id_seq', 2, true);


--
-- Name: property_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.property_type_id_seq', 5, true);


--
-- Name: query_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.query_id_seq', 1, false);


--
-- Name: relationship_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.relationship_type_id_seq', 3, true);


--
-- Name: role_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_assignment_id_seq', 3, true);


--
-- Name: sample_code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sample_code_seq', 1, false);


--
-- Name: sample_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sample_id_seq', 1, true);


--
-- Name: sample_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sample_property_id_seq', 1, false);


--
-- Name: sample_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sample_relationship_id_seq', 1, false);


--
-- Name: sample_relationships_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sample_relationships_history_id_seq', 1, true);


--
-- Name: sample_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sample_type_id_seq', 2, true);


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.script_id_seq', 1, false);


--
-- Name: semantic_annotation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.semantic_annotation_id_seq', 1, false);


--
-- Name: space_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.space_id_seq', 1, true);


--
-- Name: stpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.stpt_id_seq', 4, true);


--
-- Name: authorization_groups ag_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorization_groups
    ADD CONSTRAINT ag_bk_uk UNIQUE (code);


--
-- Name: authorization_groups ag_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorization_groups
    ADD CONSTRAINT ag_pk PRIMARY KEY (id);


--
-- Name: authorization_group_persons agp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorization_group_persons
    ADD CONSTRAINT agp_pk PRIMARY KEY (pers_id, ag_id);


--
-- Name: attachments atta_expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);


--
-- Name: attachments atta_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_pk PRIMARY KEY (id);


--
-- Name: attachments atta_proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);


--
-- Name: attachments atta_samp_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);


--
-- Name: content_copies coco_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_copies
    ADD CONSTRAINT coco_pk PRIMARY KEY (id);


--
-- Name: content_copies content_copies_unique_check_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_copies
    ADD CONSTRAINT content_copies_unique_check_uk UNIQUE (location_unique_check);


--
-- Name: core_plugins copl_name_ver_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.core_plugins
    ADD CONSTRAINT copl_name_ver_uk UNIQUE (name, version);


--
-- Name: controlled_vocabularies covo_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace);


--
-- Name: controlled_vocabularies covo_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);


--
-- Name: controlled_vocabulary_terms cvte_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);


--
-- Name: controlled_vocabulary_terms cvte_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);


--
-- Name: data_stores dast_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_stores
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, uuid);


--
-- Name: data_stores dast_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);


--
-- Name: data_all data_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_bk_uk UNIQUE (code);


--
-- Name: data_all data_idfrz_ch_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_idfrz_ch_uk UNIQUE (id, frozen_for_children);


--
-- Name: data_all data_idfrz_comp_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_idfrz_comp_uk UNIQUE (id, frozen_for_comps);


--
-- Name: data_all data_idfrz_cont_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_idfrz_cont_uk UNIQUE (id, frozen_for_conts);


--
-- Name: data_all data_idfrz_p_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_idfrz_p_uk UNIQUE (id, frozen_for_parents);


--
-- Name: data_all data_idfrz_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_idfrz_uk UNIQUE (id, frozen);


--
-- Name: data_all data_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_pk PRIMARY KEY (id);


--
-- Name: data_set_relationships_history datarelh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_history
    ADD CONSTRAINT datarelh_pk PRIMARY KEY (id);


--
-- Name: data_types daty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);


--
-- Name: data_types daty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);


--
-- Name: deletions del_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deletions
    ADD CONSTRAINT del_pk PRIMARY KEY (id);


--
-- Name: data_set_copies_history dsch_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_copies_history
    ADD CONSTRAINT dsch_pk PRIMARY KEY (id);


--
-- Name: data_set_properties dspr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_bk_uk UNIQUE (ds_id, dstpt_id);


--
-- Name: data_set_properties dspr_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_pk PRIMARY KEY (id);


--
-- Name: data_set_properties_history dsprh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties_history
    ADD CONSTRAINT dsprh_pk PRIMARY KEY (id);


--
-- Name: data_set_relationships_all dsre_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent, relationship_id);


--
-- Name: data_store_service_data_set_types dssdst_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_store_service_data_set_types
    ADD CONSTRAINT dssdst_bk_uk UNIQUE (data_store_service_id, data_set_type_id);


--
-- Name: data_store_services dsse_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_store_services
    ADD CONSTRAINT dsse_bk_uk UNIQUE (key, data_store_id);


--
-- Name: data_store_services dsse_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_store_services
    ADD CONSTRAINT dsse_pk PRIMARY KEY (id);


--
-- Name: data_set_type_property_types dstpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_type_property_types
    ADD CONSTRAINT dstpt_bk_uk UNIQUE (dsty_id, prty_id);


--
-- Name: data_set_type_property_types dstpt_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_type_property_types
    ADD CONSTRAINT dstpt_pk PRIMARY KEY (id);


--
-- Name: data_set_types dsty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_types
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code);


--
-- Name: data_set_types dsty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);


--
-- Name: external_data_management_systems edms_code_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data_management_systems
    ADD CONSTRAINT edms_code_uk UNIQUE (code);


--
-- Name: external_data_management_systems edms_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data_management_systems
    ADD CONSTRAINT edms_pk PRIMARY KEY (id);


--
-- Name: entity_operations_log eol_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entity_operations_log
    ADD CONSTRAINT eol_pk PRIMARY KEY (id);


--
-- Name: entity_operations_log eol_reg_id_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entity_operations_log
    ADD CONSTRAINT eol_reg_id_uk UNIQUE (registration_id);


--
-- Name: experiment_type_property_types etpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);


--
-- Name: experiment_type_property_types etpt_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);


--
-- Name: events evnt_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);


--
-- Name: attachment_contents exac_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);


--
-- Name: external_data exda_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);


--
-- Name: external_data exda_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (id);


--
-- Name: experiments_all expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);


--
-- Name: experiments_all expe_idfrz_d_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_idfrz_d_uk UNIQUE (id, frozen_for_data);


--
-- Name: experiments_all expe_idfrz_s_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_idfrz_s_uk UNIQUE (id, frozen_for_samp);


--
-- Name: experiments_all expe_idfrz_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_idfrz_uk UNIQUE (id, frozen);


--
-- Name: experiments_all expe_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);


--
-- Name: experiments_all expe_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);


--
-- Name: experiment_properties expr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);


--
-- Name: experiment_properties expr_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);


--
-- Name: experiment_properties_history exprh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties_history
    ADD CONSTRAINT exprh_pk PRIMARY KEY (id);


--
-- Name: experiment_relationships_history exrelh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_relationships_history
    ADD CONSTRAINT exrelh_pk PRIMARY KEY (id);


--
-- Name: experiment_types exty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code);


--
-- Name: experiment_types exty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);


--
-- Name: file_format_types ffty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code);


--
-- Name: file_format_types ffty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);


--
-- Name: filters filt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.filters
    ADD CONSTRAINT filt_bk_uk UNIQUE (name, grid_id);


--
-- Name: filters filt_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.filters
    ADD CONSTRAINT filt_pk PRIMARY KEY (id);


--
-- Name: grid_custom_columns grid_custom_columns_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_bk_uk UNIQUE (code, grid_id);


--
-- Name: grid_custom_columns grid_custom_columns_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pk PRIMARY KEY (id);


--
-- Name: link_data link_data_idfrz_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_data
    ADD CONSTRAINT link_data_idfrz_uk UNIQUE (id, data_frozen);


--
-- Name: link_data lnda_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_data
    ADD CONSTRAINT lnda_pk PRIMARY KEY (id);


--
-- Name: locator_types loty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);


--
-- Name: locator_types loty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);


--
-- Name: material_properties mapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);


--
-- Name: material_properties mapr_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);


--
-- Name: material_properties_history maprh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties_history
    ADD CONSTRAINT maprh_pk PRIMARY KEY (id);


--
-- Name: materials mate_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id);


--
-- Name: materials mate_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);


--
-- Name: material_types maty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code);


--
-- Name: material_types maty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_mepr_id_data_id_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_data_id_uk UNIQUE (mepr_id, data_id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_mepr_id_expe_id_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_expe_id_uk UNIQUE (mepr_id, expe_id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_mepr_id_mate_id_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_mate_id_uk UNIQUE (mepr_id, mate_id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_mepr_id_samp_id_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_samp_id_uk UNIQUE (mepr_id, samp_id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_pk PRIMARY KEY (id);


--
-- Name: metaprojects metaprojects_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaprojects
    ADD CONSTRAINT metaprojects_pk PRIMARY KEY (id);


--
-- Name: material_type_property_types mtpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);


--
-- Name: material_type_property_types mtpt_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);


--
-- Name: operation_executions operation_executions_code_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_executions
    ADD CONSTRAINT operation_executions_code_uk UNIQUE (code);


--
-- Name: operation_executions operation_executions_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_executions
    ADD CONSTRAINT operation_executions_pk PRIMARY KEY (id);


--
-- Name: persons pers_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (user_id);


--
-- Name: persons pers_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);


--
-- Name: post_registration_dataset_queue prdq_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_registration_dataset_queue
    ADD CONSTRAINT prdq_pk PRIMARY KEY (id);


--
-- Name: projects proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, space_id);


--
-- Name: projects proj_idfrz_e_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_idfrz_e_uk UNIQUE (id, frozen_for_exp);


--
-- Name: projects proj_idfrz_s_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_idfrz_s_uk UNIQUE (id, frozen_for_samp);


--
-- Name: projects proj_idfrz_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_idfrz_uk UNIQUE (id, frozen);


--
-- Name: projects proj_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_pi_uk UNIQUE (perm_id);


--
-- Name: projects proj_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);


--
-- Name: project_relationships_history prrelh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_relationships_history
    ADD CONSTRAINT prrelh_pk PRIMARY KEY (id);


--
-- Name: property_types prty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace);


--
-- Name: property_types prty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);


--
-- Name: queries quer_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.queries
    ADD CONSTRAINT quer_bk_uk UNIQUE (name);


--
-- Name: queries quer_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.queries
    ADD CONSTRAINT quer_pk PRIMARY KEY (id);


--
-- Name: relationship_types rety_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relationship_types
    ADD CONSTRAINT rety_pk PRIMARY KEY (id);


--
-- Name: relationship_types rety_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relationship_types
    ADD CONSTRAINT rety_uk UNIQUE (code);


--
-- Name: role_assignments roas_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);


--
-- Name: samples_all samp_code_unique_check_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_code_unique_check_uk UNIQUE (code_unique_check);


--
-- Name: samples_all samp_idfrz_c_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_idfrz_c_uk UNIQUE (id, frozen_for_comp);


--
-- Name: samples_all samp_idfrz_ch_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_idfrz_ch_uk UNIQUE (id, frozen_for_children);


--
-- Name: samples_all samp_idfrz_d_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_idfrz_d_uk UNIQUE (id, frozen_for_data);


--
-- Name: samples_all samp_idfrz_p_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_idfrz_p_uk UNIQUE (id, frozen_for_parents);


--
-- Name: samples_all samp_idfrz_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_idfrz_uk UNIQUE (id, frozen);


--
-- Name: samples_all samp_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);


--
-- Name: samples_all samp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);


--
-- Name: samples_all samp_subcode_unique_check_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_subcode_unique_check_uk UNIQUE (subcode_unique_check);


--
-- Name: sample_relationships_history samprelh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_pk PRIMARY KEY (id);


--
-- Name: sample_properties sapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);


--
-- Name: sample_properties sapr_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);


--
-- Name: sample_properties_history saprh_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties_history
    ADD CONSTRAINT saprh_pk PRIMARY KEY (id);


--
-- Name: sample_relationships_all sare_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sare_bk_uk UNIQUE (sample_id_child, sample_id_parent, relationship_id);


--
-- Name: sample_relationships_all sare_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sare_pk PRIMARY KEY (id);


--
-- Name: sample_types saty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code);


--
-- Name: sample_types saty_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);


--
-- Name: scripts scri_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.scripts
    ADD CONSTRAINT scri_pk PRIMARY KEY (id);


--
-- Name: scripts scri_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.scripts
    ADD CONSTRAINT scri_uk UNIQUE (name);


--
-- Name: semantic_annotations semantic_annotations_perm_id_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semantic_annotations
    ADD CONSTRAINT semantic_annotations_perm_id_uk UNIQUE (perm_id);


--
-- Name: semantic_annotations semantic_annotations_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semantic_annotations
    ADD CONSTRAINT semantic_annotations_pk PRIMARY KEY (id);


--
-- Name: spaces space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spaces
    ADD CONSTRAINT space_bk_uk UNIQUE (code);


--
-- Name: spaces space_idfrz_p_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spaces
    ADD CONSTRAINT space_idfrz_p_uk UNIQUE (id, frozen_for_proj);


--
-- Name: spaces space_idfrz_s_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spaces
    ADD CONSTRAINT space_idfrz_s_uk UNIQUE (id, frozen_for_samp);


--
-- Name: spaces space_idfrz_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spaces
    ADD CONSTRAINT space_idfrz_uk UNIQUE (id, frozen);


--
-- Name: spaces space_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spaces
    ADD CONSTRAINT space_pk PRIMARY KEY (id);


--
-- Name: sample_type_property_types stpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);


--
-- Name: sample_type_property_types stpt_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);


--
-- Name: atta_exac_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX atta_exac_fk_i ON public.attachments USING btree (exac_id);


--
-- Name: atta_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX atta_expe_fk_i ON public.attachments USING btree (expe_id);


--
-- Name: atta_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX atta_pers_fk_i ON public.attachments USING btree (pers_id_registerer);


--
-- Name: atta_proj_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX atta_proj_fk_i ON public.attachments USING btree (proj_id);


--
-- Name: atta_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX atta_samp_fk_i ON public.attachments USING btree (samp_id);


--
-- Name: covo_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX covo_pers_fk_i ON public.controlled_vocabularies USING btree (pers_id_registerer);


--
-- Name: cvte_covo_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cvte_covo_fk_i ON public.controlled_vocabulary_terms USING btree (covo_id);


--
-- Name: cvte_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cvte_pers_fk_i ON public.controlled_vocabulary_terms USING btree (pers_id_registerer);


--
-- Name: data_acct_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_acct_i ON public.data_all USING btree (access_timestamp);


--
-- Name: data_del_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_del_fk_i ON public.data_all USING btree (del_id);


--
-- Name: data_dsty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_dsty_fk_i ON public.data_all USING btree (dsty_id);


--
-- Name: data_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_expe_fk_i ON public.data_all USING btree (expe_id);


--
-- Name: data_idfrz_ch_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_idfrz_ch_pk_i ON public.data_all USING btree (id, frozen_for_children);


--
-- Name: data_idfrz_comp_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_idfrz_comp_pk_i ON public.data_all USING btree (id, frozen_for_comps);


--
-- Name: data_idfrz_cont_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_idfrz_cont_pk_i ON public.data_all USING btree (id, frozen_for_conts);


--
-- Name: data_idfrz_p_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_idfrz_p_pk_i ON public.data_all USING btree (id, frozen_for_parents);


--
-- Name: data_idfrz_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_idfrz_pk_i ON public.data_all USING btree (id, frozen);


--
-- Name: data_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_samp_fk_i ON public.data_all USING btree (samp_id);


--
-- Name: datarelh_data_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX datarelh_data_fk_i ON public.data_set_relationships_history USING btree (data_id);


--
-- Name: datarelh_main_data_fk_data_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX datarelh_main_data_fk_data_fk_i ON public.data_set_relationships_history USING btree (main_data_id, data_id);


--
-- Name: datarelh_main_data_fk_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX datarelh_main_data_fk_expe_fk_i ON public.data_set_relationships_history USING btree (main_data_id, expe_id);


--
-- Name: datarelh_main_data_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX datarelh_main_data_fk_i ON public.data_set_relationships_history USING btree (main_data_id);


--
-- Name: datarelh_main_data_fk_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX datarelh_main_data_fk_samp_fk_i ON public.data_set_relationships_history USING btree (main_data_id, samp_id);


--
-- Name: del_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX del_pers_fk_i ON public.deletions USING btree (pers_id_registerer);


--
-- Name: dspr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dspr_cvte_fk_i ON public.data_set_properties USING btree (cvte_id);


--
-- Name: dspr_ds_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dspr_ds_fk_i ON public.data_set_properties USING btree (ds_id);


--
-- Name: dspr_dstpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dspr_dstpt_fk_i ON public.data_set_properties USING btree (dstpt_id);


--
-- Name: dspr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dspr_mapr_fk_i ON public.data_set_properties USING btree (mate_prop_id);


--
-- Name: dspr_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dspr_pers_fk_i ON public.data_set_properties USING btree (pers_id_registerer);


--
-- Name: dsprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsprh_etpt_fk_i ON public.data_set_properties_history USING btree (dstpt_id);


--
-- Name: dsprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsprh_expe_fk_i ON public.data_set_properties_history USING btree (ds_id);


--
-- Name: dsprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsprh_vuts_fk_i ON public.data_set_properties_history USING btree (valid_until_timestamp);


--
-- Name: dsre_data_fk_i_child; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsre_data_fk_i_child ON public.data_set_relationships_all USING btree (data_id_child);


--
-- Name: dsre_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsre_data_fk_i_parent ON public.data_set_relationships_all USING btree (data_id_parent);


--
-- Name: dsre_del_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsre_del_fk_i ON public.data_set_relationships_all USING btree (del_id);


--
-- Name: dssdst_ds_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dssdst_ds_fk_i ON public.data_store_service_data_set_types USING btree (data_store_service_id);


--
-- Name: dssdst_dst_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dssdst_dst_fk_i ON public.data_store_service_data_set_types USING btree (data_set_type_id);


--
-- Name: dsse_ds_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dsse_ds_fk_i ON public.data_store_services USING btree (data_store_id);


--
-- Name: dstpt_dsty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dstpt_dsty_fk_i ON public.data_set_type_property_types USING btree (dsty_id);


--
-- Name: dstpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dstpt_pers_fk_i ON public.data_set_type_property_types USING btree (pers_id_registerer);


--
-- Name: dstpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dstpt_prty_fk_i ON public.data_set_type_property_types USING btree (prty_id);


--
-- Name: entity_operations_log_rid_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX entity_operations_log_rid_i ON public.entity_operations_log USING btree (registration_id);


--
-- Name: etpt_exty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX etpt_exty_fk_i ON public.experiment_type_property_types USING btree (exty_id);


--
-- Name: etpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX etpt_pers_fk_i ON public.experiment_type_property_types USING btree (pers_id_registerer);


--
-- Name: etpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX etpt_prty_fk_i ON public.experiment_type_property_types USING btree (prty_id);


--
-- Name: evnt_exac_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX evnt_exac_fk_i ON public.events USING btree (exac_id);


--
-- Name: evnt_fr_id_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX evnt_fr_id_fk_i ON public.events USING btree (event_type, identifiers) WHERE ((event_type)::text = 'FREEZING'::text);


--
-- Name: evnt_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX evnt_pers_fk_i ON public.events USING btree (pers_id_registerer);


--
-- Name: exda_cvte_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exda_cvte_fk_i ON public.external_data USING btree (cvte_id_stor_fmt);


--
-- Name: exda_cvte_stored_on_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exda_cvte_stored_on_fk_i ON public.external_data USING btree (cvte_id_store);


--
-- Name: exda_ffty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exda_ffty_fk_i ON public.external_data USING btree (ffty_id);


--
-- Name: exda_loty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exda_loty_fk_i ON public.external_data USING btree (loty_id);


--
-- Name: expe_del_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_del_fk_i ON public.experiments_all USING btree (del_id);


--
-- Name: expe_exty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_exty_fk_i ON public.experiments_all USING btree (exty_id);


--
-- Name: expe_idfrz_d_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_idfrz_d_pk_i ON public.experiments_all USING btree (id, frozen_for_data);


--
-- Name: expe_idfrz_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_idfrz_pk_i ON public.experiments_all USING btree (id, frozen);


--
-- Name: expe_idfrz_s_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_idfrz_s_pk_i ON public.experiments_all USING btree (id, frozen_for_samp);


--
-- Name: expe_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_pers_fk_i ON public.experiments_all USING btree (pers_id_registerer);


--
-- Name: expe_proj_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expe_proj_fk_i ON public.experiments_all USING btree (proj_id);


--
-- Name: expr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expr_cvte_fk_i ON public.experiment_properties USING btree (cvte_id);


--
-- Name: expr_etpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expr_etpt_fk_i ON public.experiment_properties USING btree (etpt_id);


--
-- Name: expr_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expr_expe_fk_i ON public.experiment_properties USING btree (expe_id);


--
-- Name: expr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expr_mapr_fk_i ON public.experiment_properties USING btree (mate_prop_id);


--
-- Name: expr_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX expr_pers_fk_i ON public.experiment_properties USING btree (pers_id_registerer);


--
-- Name: exprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exprh_etpt_fk_i ON public.experiment_properties_history USING btree (etpt_id);


--
-- Name: exprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exprh_expe_fk_i ON public.experiment_properties_history USING btree (expe_id);


--
-- Name: exprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exprh_vuts_fk_i ON public.experiment_properties_history USING btree (valid_until_timestamp);


--
-- Name: exrelh_data_id_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exrelh_data_id_fk_i ON public.experiment_relationships_history USING btree (data_id);


--
-- Name: exrelh_main_expe_fk_data_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exrelh_main_expe_fk_data_fk_i ON public.experiment_relationships_history USING btree (main_expe_id, data_id);


--
-- Name: exrelh_main_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exrelh_main_expe_fk_i ON public.experiment_relationships_history USING btree (main_expe_id);


--
-- Name: exrelh_main_expe_fk_proj_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exrelh_main_expe_fk_proj_fk_i ON public.experiment_relationships_history USING btree (main_expe_id, proj_id);


--
-- Name: exrelh_main_expe_fk_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exrelh_main_expe_fk_samp_fk_i ON public.experiment_relationships_history USING btree (main_expe_id, samp_id);


--
-- Name: exrelh_samp_id_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX exrelh_samp_id_fk_i ON public.experiment_relationships_history USING btree (samp_id);


--
-- Name: filt_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX filt_pers_fk_i ON public.filters USING btree (pers_id_registerer);


--
-- Name: grid_custom_columns_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX grid_custom_columns_pers_fk_i ON public.grid_custom_columns USING btree (pers_id_registerer);


--
-- Name: link_data_idfrz_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX link_data_idfrz_pk_i ON public.link_data USING btree (id, data_frozen);


--
-- Name: mapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mapr_cvte_fk_i ON public.material_properties USING btree (cvte_id);


--
-- Name: mapr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mapr_mapr_fk_i ON public.material_properties USING btree (mate_prop_id);


--
-- Name: mapr_mate_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mapr_mate_fk_i ON public.material_properties USING btree (mate_id);


--
-- Name: mapr_mtpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mapr_mtpt_fk_i ON public.material_properties USING btree (mtpt_id);


--
-- Name: mapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mapr_pers_fk_i ON public.material_properties USING btree (pers_id_registerer);


--
-- Name: maprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX maprh_etpt_fk_i ON public.material_properties_history USING btree (mtpt_id);


--
-- Name: maprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX maprh_expe_fk_i ON public.material_properties_history USING btree (mate_id);


--
-- Name: maprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX maprh_vuts_fk_i ON public.material_properties_history USING btree (valid_until_timestamp);


--
-- Name: mate_maty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mate_maty_fk_i ON public.materials USING btree (maty_id);


--
-- Name: mate_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mate_pers_fk_i ON public.materials USING btree (pers_id_registerer);


--
-- Name: metaproject_assignments_all_data_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaproject_assignments_all_data_fk_i ON public.metaproject_assignments_all USING btree (data_id);


--
-- Name: metaproject_assignments_all_del_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaproject_assignments_all_del_fk_i ON public.metaproject_assignments_all USING btree (del_id);


--
-- Name: metaproject_assignments_all_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaproject_assignments_all_expe_fk_i ON public.metaproject_assignments_all USING btree (expe_id);


--
-- Name: metaproject_assignments_all_mate_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaproject_assignments_all_mate_fk_i ON public.metaproject_assignments_all USING btree (mate_id);


--
-- Name: metaproject_assignments_all_mepr_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaproject_assignments_all_mepr_fk_i ON public.metaproject_assignments_all USING btree (mepr_id);


--
-- Name: metaproject_assignments_all_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaproject_assignments_all_samp_fk_i ON public.metaproject_assignments_all USING btree (samp_id);


--
-- Name: metaprojects_name_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaprojects_name_i ON public.metaprojects USING btree (name);


--
-- Name: metaprojects_name_owner_uk; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX metaprojects_name_owner_uk ON public.metaprojects USING btree (lower((name)::text), owner);


--
-- Name: metaprojects_owner_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX metaprojects_owner_fk_i ON public.metaprojects USING btree (owner);


--
-- Name: mtpt_maty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mtpt_maty_fk_i ON public.material_type_property_types USING btree (maty_id);


--
-- Name: mtpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mtpt_pers_fk_i ON public.material_type_property_types USING btree (pers_id_registerer);


--
-- Name: mtpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mtpt_prty_fk_i ON public.material_type_property_types USING btree (prty_id);


--
-- Name: operation_executions_availability_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX operation_executions_availability_i ON public.operation_executions USING btree (availability);


--
-- Name: operation_executions_code_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX operation_executions_code_i ON public.operation_executions USING btree (code);


--
-- Name: operation_executions_details_availability_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX operation_executions_details_availability_i ON public.operation_executions USING btree (details_availability);


--
-- Name: operation_executions_owner_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX operation_executions_owner_i ON public.operation_executions USING btree (owner);


--
-- Name: operation_executions_summary_availability_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX operation_executions_summary_availability_i ON public.operation_executions USING btree (summary_availability);


--
-- Name: pers_is_active_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pers_is_active_i ON public.persons USING btree (is_active);


--
-- Name: pers_space_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pers_space_fk_i ON public.persons USING btree (space_id);


--
-- Name: proj_idfrz_e_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX proj_idfrz_e_pk_i ON public.projects USING btree (id, frozen_for_exp);


--
-- Name: proj_idfrz_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX proj_idfrz_pk_i ON public.projects USING btree (id, frozen);


--
-- Name: proj_idfrz_s_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX proj_idfrz_s_pk_i ON public.projects USING btree (id, frozen_for_samp);


--
-- Name: proj_pers_fk_i_leader; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX proj_pers_fk_i_leader ON public.projects USING btree (pers_id_leader);


--
-- Name: proj_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX proj_pers_fk_i_registerer ON public.projects USING btree (pers_id_registerer);


--
-- Name: proj_space_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX proj_space_fk_i ON public.projects USING btree (space_id);


--
-- Name: prrelh_main_proj_fk_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX prrelh_main_proj_fk_expe_fk_i ON public.project_relationships_history USING btree (main_proj_id, expe_id);


--
-- Name: prrelh_main_proj_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX prrelh_main_proj_fk_i ON public.project_relationships_history USING btree (main_proj_id);


--
-- Name: prrelh_main_proj_fk_space_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX prrelh_main_proj_fk_space_fk_i ON public.project_relationships_history USING btree (main_proj_id, space_id);


--
-- Name: prty_covo_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX prty_covo_fk_i ON public.property_types USING btree (covo_id);


--
-- Name: prty_daty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX prty_daty_fk_i ON public.property_types USING btree (daty_id);


--
-- Name: prty_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX prty_pers_fk_i ON public.property_types USING btree (pers_id_registerer);


--
-- Name: roas_ag_fk_i_grantee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX roas_ag_fk_i_grantee ON public.role_assignments USING btree (ag_id_grantee);


--
-- Name: roas_ag_space_project_bk_uk; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX roas_ag_space_project_bk_uk ON public.role_assignments USING btree (ag_id_grantee, role_code, COALESCE((space_id)::bigint, ('-1'::integer)::bigint), COALESCE((project_id)::bigint, ('-1'::integer)::bigint));


--
-- Name: roas_pe_space_project_bk_uk; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX roas_pe_space_project_bk_uk ON public.role_assignments USING btree (pers_id_grantee, role_code, COALESCE((space_id)::bigint, ('-1'::integer)::bigint), COALESCE((project_id)::bigint, ('-1'::integer)::bigint));


--
-- Name: roas_pers_fk_i_grantee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX roas_pers_fk_i_grantee ON public.role_assignments USING btree (pers_id_grantee);


--
-- Name: roas_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX roas_pers_fk_i_registerer ON public.role_assignments USING btree (pers_id_registerer);


--
-- Name: roas_project_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX roas_project_fk_i ON public.role_assignments USING btree (project_id);


--
-- Name: roas_space_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX roas_space_fk_i ON public.role_assignments USING btree (space_id);


--
-- Name: samp_code_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_code_i ON public.samples_all USING btree (code);


--
-- Name: samp_del_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_del_fk_i ON public.samples_all USING btree (del_id);


--
-- Name: samp_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_expe_fk_i ON public.samples_all USING btree (expe_id);


--
-- Name: samp_idfrz_c_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_idfrz_c_pk_i ON public.samples_all USING btree (id, frozen_for_comp);


--
-- Name: samp_idfrz_ch_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_idfrz_ch_pk_i ON public.samples_all USING btree (id, frozen_for_children);


--
-- Name: samp_idfrz_d_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_idfrz_d_pk_i ON public.samples_all USING btree (id, frozen_for_data);


--
-- Name: samp_idfrz_p_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_idfrz_p_pk_i ON public.samples_all USING btree (id, frozen_for_parents);


--
-- Name: samp_idfrz_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_idfrz_pk_i ON public.samples_all USING btree (id, frozen);


--
-- Name: samp_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_pers_fk_i ON public.samples_all USING btree (pers_id_registerer);


--
-- Name: samp_proj_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_proj_fk_i ON public.samples_all USING btree (proj_id);


--
-- Name: samp_samp_fk_i_part_of; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_samp_fk_i_part_of ON public.samples_all USING btree (samp_id_part_of);


--
-- Name: samp_saty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samp_saty_fk_i ON public.samples_all USING btree (saty_id);


--
-- Name: samprelh_data_id_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_data_id_fk_i ON public.sample_relationships_history USING btree (data_id);


--
-- Name: samprelh_main_samp_fk_data_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_main_samp_fk_data_fk_i ON public.sample_relationships_history USING btree (main_samp_id, data_id);


--
-- Name: samprelh_main_samp_fk_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_main_samp_fk_expe_fk_i ON public.sample_relationships_history USING btree (main_samp_id, expe_id);


--
-- Name: samprelh_main_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_main_samp_fk_i ON public.sample_relationships_history USING btree (main_samp_id);


--
-- Name: samprelh_main_samp_fk_proj_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_main_samp_fk_proj_fk_i ON public.sample_relationships_history USING btree (main_samp_id, proj_id);


--
-- Name: samprelh_main_samp_fk_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_main_samp_fk_samp_fk_i ON public.sample_relationships_history USING btree (main_samp_id, samp_id);


--
-- Name: samprelh_main_samp_fk_space_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_main_samp_fk_space_fk_i ON public.sample_relationships_history USING btree (main_samp_id, space_id);


--
-- Name: samprelh_samp_id_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX samprelh_samp_id_fk_i ON public.sample_relationships_history USING btree (samp_id);


--
-- Name: sapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sapr_cvte_fk_i ON public.sample_properties USING btree (cvte_id);


--
-- Name: sapr_mapr_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sapr_mapr_fk_i ON public.sample_properties USING btree (mate_prop_id);


--
-- Name: sapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sapr_pers_fk_i ON public.sample_properties USING btree (pers_id_registerer);


--
-- Name: sapr_samp_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sapr_samp_fk_i ON public.sample_properties USING btree (samp_id);


--
-- Name: sapr_stpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sapr_stpt_fk_i ON public.sample_properties USING btree (stpt_id);


--
-- Name: saprh_etpt_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saprh_etpt_fk_i ON public.sample_properties_history USING btree (stpt_id);


--
-- Name: saprh_expe_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saprh_expe_fk_i ON public.sample_properties_history USING btree (samp_id);


--
-- Name: saprh_vuts_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saprh_vuts_fk_i ON public.sample_properties_history USING btree (valid_until_timestamp);


--
-- Name: sare_data_fk_i_child; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sare_data_fk_i_child ON public.sample_relationships_all USING btree (sample_id_child);


--
-- Name: sare_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sare_data_fk_i_parent ON public.sample_relationships_all USING btree (sample_id_parent);


--
-- Name: sare_data_fk_i_relationship; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sare_data_fk_i_relationship ON public.sample_relationships_all USING btree (relationship_id);


--
-- Name: sare_del_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sare_del_fk_i ON public.sample_relationships_all USING btree (del_id);


--
-- Name: script_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX script_pers_fk_i ON public.scripts USING btree (pers_id_registerer);


--
-- Name: semantic_annotations_prty_id_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX semantic_annotations_prty_id_i ON public.semantic_annotations USING btree (prty_id);


--
-- Name: semantic_annotations_saty_id_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX semantic_annotations_saty_id_i ON public.semantic_annotations USING btree (saty_id);


--
-- Name: semantic_annotations_stpt_id_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX semantic_annotations_stpt_id_i ON public.semantic_annotations USING btree (stpt_id);


--
-- Name: space_idfrz_p_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX space_idfrz_p_pk_i ON public.spaces USING btree (id, frozen_for_proj);


--
-- Name: space_idfrz_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX space_idfrz_pk_i ON public.spaces USING btree (id, frozen);


--
-- Name: space_idfrz_s_pk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX space_idfrz_s_pk_i ON public.spaces USING btree (id, frozen_for_samp);


--
-- Name: space_pers_registered_by_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX space_pers_registered_by_fk_i ON public.spaces USING btree (pers_id_registerer);


--
-- Name: stpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX stpt_pers_fk_i ON public.sample_type_property_types USING btree (pers_id_registerer);


--
-- Name: stpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX stpt_prty_fk_i ON public.sample_type_property_types USING btree (prty_id);


--
-- Name: stpt_saty_fk_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX stpt_saty_fk_i ON public.sample_type_property_types USING btree (saty_id);


--
-- Name: content_copies content_copies_history_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE content_copies_history_delete AS
    ON DELETE TO public.content_copies DO  UPDATE public.data_set_copies_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((data_set_copies_history.cc_id)::bigint = (old.id)::bigint);


--
-- Name: content_copies content_copies_history_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE content_copies_history_insert AS
    ON INSERT TO public.content_copies DO  INSERT INTO public.data_set_copies_history (id, cc_id, data_id, external_code, path, git_commit_hash, git_repository_id, edms_id, edms_code, edms_label, edms_address, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.data_set_copies_history_id_seq'::regclass), new.id, new.data_id, new.external_code, new.path, new.git_commit_hash, new.git_repository_id, new.edms_id, ( SELECT external_data_management_systems.code
           FROM public.external_data_management_systems
          WHERE ((external_data_management_systems.id)::bigint = (new.edms_id)::bigint)), ( SELECT external_data_management_systems.label
           FROM public.external_data_management_systems
          WHERE ((external_data_management_systems.id)::bigint = (new.edms_id)::bigint)), ( SELECT external_data_management_systems.address
           FROM public.external_data_management_systems
          WHERE ((external_data_management_systems.id)::bigint = (new.edms_id)::bigint)), new.pers_id_registerer, new.registration_timestamp);


--
-- Name: data data_all; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_all AS
    ON DELETE TO public.data DO INSTEAD  DELETE FROM public.data_all
  WHERE ((data_all.id)::bigint = (old.id)::bigint);


--
-- Name: data_deleted data_deleted_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_deleted_delete AS
    ON DELETE TO public.data_deleted DO INSTEAD  DELETE FROM public.data_all
  WHERE ((data_all.id)::bigint = (old.id)::bigint);


--
-- Name: data_deleted data_deleted_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_deleted_update AS
    ON UPDATE TO public.data_deleted DO INSTEAD  UPDATE public.data_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((data_all.id)::bigint = (new.id)::bigint);


--
-- Name: data data_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_insert AS
    ON INSERT TO public.data DO INSTEAD  INSERT INTO public.data_all (id, frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, code, del_id, orig_del, expe_id, expe_frozen, dast_id, data_producer_code, dsty_id, is_derived, is_valid, modification_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, production_timestamp, registration_timestamp, samp_id, samp_frozen, version, data_set_kind)
  VALUES (new.id, new.frozen, new.frozen_for_children, new.frozen_for_parents, new.frozen_for_comps, new.frozen_for_conts, new.code, new.del_id, new.orig_del, new.expe_id, new.expe_frozen, new.dast_id, new.data_producer_code, new.dsty_id, new.is_derived, new.is_valid, new.modification_timestamp, new.access_timestamp, new.pers_id_registerer, new.pers_id_modifier, new.production_timestamp, new.registration_timestamp, new.samp_id, new.samp_frozen, new.version, new.data_set_kind);


--
-- Name: data_set_relationships_all data_relationship_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_delete AS
    ON DELETE TO public.data_set_relationships_all
   WHERE (old.del_id IS NULL) DO  UPDATE public.data_set_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: data_set_relationships_all data_relationship_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_insert AS
    ON INSERT TO public.data_set_relationships_all
   WHERE (new.del_id IS NULL) DO ( INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);


--
-- Name: data_set_relationships_all data_relationship_trash_revert_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_trash_revert_update AS
    ON UPDATE TO public.data_set_relationships_all
   WHERE ((old.del_id IS NOT NULL) AND (new.del_id IS NULL)) DO ( INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);


--
-- Name: data_set_relationships_all data_relationship_trash_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_trash_update AS
    ON UPDATE TO public.data_set_relationships_all
   WHERE ((new.del_id IS NOT NULL) AND (old.del_id IS NULL)) DO  UPDATE public.data_set_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: data_set_relationships_all data_relationship_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_relationship_update AS
    ON UPDATE TO public.data_set_relationships_all
   WHERE ((new.del_id IS NULL) AND (old.del_id IS NULL)) DO ( UPDATE public.data_set_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM public.relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);


--
-- Name: data_set_properties data_set_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_properties_delete AS
    ON DELETE TO public.data_set_properties
   WHERE ((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL) OR (old.mate_prop_id IS NOT NULL)) AND (( SELECT data_all.del_id
           FROM public.data_all
          WHERE ((data_all.id)::bigint = (old.ds_id)::bigint)) IS NULL)) DO  INSERT INTO public.data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.data_set_property_id_seq'::regclass), old.ds_id, old.dstpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, CURRENT_TIMESTAMP);


--
-- Name: data_set_properties data_set_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_properties_update AS
    ON UPDATE TO public.data_set_properties
   WHERE (((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint)) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO public.data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.data_set_property_id_seq'::regclass), old.ds_id, old.dstpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: data_set_relationships data_set_relationships_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_relationships_delete AS
    ON DELETE TO public.data_set_relationships DO INSTEAD  DELETE FROM public.data_set_relationships_all
  WHERE (((data_set_relationships_all.data_id_parent)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_all.data_id_child)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_all.relationship_id)::bigint = (old.relationship_id)::bigint));


--
-- Name: data_set_relationships data_set_relationships_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_relationships_insert AS
    ON INSERT TO public.data_set_relationships DO INSTEAD  INSERT INTO public.data_set_relationships_all (data_id_parent, parent_frozen, cont_frozen, data_id_child, child_frozen, comp_frozen, pers_id_author, relationship_id, ordinal, registration_timestamp, modification_timestamp)
  VALUES (new.data_id_parent, new.parent_frozen, new.cont_frozen, new.data_id_child, new.child_frozen, new.comp_frozen, new.pers_id_author, new.relationship_id, new.ordinal, new.registration_timestamp, new.modification_timestamp);


--
-- Name: data_set_relationships data_set_relationships_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_set_relationships_update AS
    ON UPDATE TO public.data_set_relationships DO INSTEAD  UPDATE public.data_set_relationships_all SET data_id_parent = new.data_id_parent, parent_frozen = new.parent_frozen, cont_frozen = new.cont_frozen, data_id_child = new.data_id_child, child_frozen = new.child_frozen, comp_frozen = new.comp_frozen, del_id = new.del_id, relationship_id = new.relationship_id, ordinal = new.ordinal, pers_id_author = new.pers_id_author, registration_timestamp = new.registration_timestamp, modification_timestamp = new.modification_timestamp
  WHERE (((data_set_relationships_all.data_id_parent)::bigint = (new.data_id_parent)::bigint) AND ((data_set_relationships_all.data_id_child)::bigint = (new.data_id_child)::bigint) AND ((data_set_relationships_all.relationship_id)::bigint = (new.relationship_id)::bigint));


--
-- Name: data data_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE data_update AS
    ON UPDATE TO public.data DO INSTEAD  UPDATE public.data_all SET code = new.code, frozen = new.frozen, frozen_for_children = new.frozen_for_children, frozen_for_parents = new.frozen_for_parents, frozen_for_comps = new.frozen_for_comps, frozen_for_conts = new.frozen_for_conts, del_id = new.del_id, orig_del = new.orig_del, expe_id = new.expe_id, expe_frozen = new.expe_frozen, dast_id = new.dast_id, data_producer_code = new.data_producer_code, dsty_id = new.dsty_id, is_derived = new.is_derived, is_valid = new.is_valid, modification_timestamp = new.modification_timestamp, access_timestamp = new.access_timestamp, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, production_timestamp = new.production_timestamp, registration_timestamp = new.registration_timestamp, samp_id = new.samp_id, samp_frozen = new.samp_frozen, version = new.version, data_set_kind = new.data_set_kind
  WHERE ((data_all.id)::bigint = (new.id)::bigint);


--
-- Name: data_all dataset_experiment_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_delete AS
    ON DELETE TO public.data_all
   WHERE ((old.expe_id IS NOT NULL) AND (old.samp_id IS NULL)) DO  UPDATE public.experiment_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: data_all dataset_experiment_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_insert AS
    ON INSERT TO public.data_all
   WHERE ((new.expe_id IS NOT NULL) AND (new.samp_id IS NULL)) DO ( INSERT INTO public.experiment_relationships_history (id, main_expe_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM public.experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: data_all dataset_experiment_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_remove_update AS
    ON UPDATE TO public.data_all
   WHERE ((old.samp_id IS NULL) AND (new.samp_id IS NOT NULL)) DO ( UPDATE public.experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 UPDATE public.data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.expe_id)::bigint = (old.expe_id)::bigint) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: data_all dataset_experiment_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_experiment_update AS
    ON UPDATE TO public.data_all
   WHERE ((((old.expe_id)::bigint <> (new.expe_id)::bigint) OR (old.samp_id IS NOT NULL)) AND (new.samp_id IS NULL)) DO ( UPDATE public.experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.experiment_relationships_history (id, main_expe_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 UPDATE public.data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.expe_id)::bigint = (old.expe_id)::bigint) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM public.experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: data_all dataset_sample_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_delete AS
    ON DELETE TO public.data_all
   WHERE (old.samp_id IS NOT NULL) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: data_all dataset_sample_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_insert AS
    ON INSERT TO public.data_all
   WHERE (new.samp_id IS NOT NULL) DO ( INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.samp_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.samp_id, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: data_all dataset_sample_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_remove_update AS
    ON UPDATE TO public.data_all
   WHERE ((old.samp_id IS NOT NULL) AND (new.samp_id IS NULL)) DO ( UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 UPDATE public.data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.samp_id)::bigint = (old.samp_id)::bigint) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: data_all dataset_sample_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE dataset_sample_update AS
    ON UPDATE TO public.data_all
   WHERE ((((old.samp_id)::bigint <> (new.samp_id)::bigint) OR (old.samp_id IS NULL)) AND (new.samp_id IS NOT NULL)) DO ( UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.samp_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 UPDATE public.data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.samp_id)::bigint = (old.samp_id)::bigint) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.data_set_relationships_history (id, main_data_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.samp_id, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: external_data_management_systems edms_a_insert_content_copy_history; Type: RULE; Schema: public; Owner: -
--

CREATE RULE edms_a_insert_content_copy_history AS
    ON UPDATE TO public.external_data_management_systems DO  INSERT INTO public.data_set_copies_history (id, cc_id, data_id, external_code, path, git_commit_hash, git_repository_id, edms_id, edms_code, edms_label, edms_address, pers_id_author, valid_from_timestamp)  SELECT nextval('public.data_set_copies_history_id_seq'::regclass) AS nextval,
            dsch.cc_id,
            dsch.data_id,
            dsch.external_code,
            dsch.path,
            dsch.git_commit_hash,
            dsch.git_repository_id,
            dsch.edms_id,
            new.code,
            new.label,
            new.address,
            dsch.pers_id_author,
            CURRENT_TIMESTAMP AS "current_timestamp"
           FROM (public.data_set_copies_history dsch
             JOIN public.external_data_management_systems edms ON (((edms.id)::bigint = (dsch.edms_id)::bigint)))
          WHERE (((new.id)::bigint = (dsch.edms_id)::bigint) AND (dsch.valid_until_timestamp IS NULL));


--
-- Name: external_data_management_systems edms_b_expire_content_copy_history; Type: RULE; Schema: public; Owner: -
--

CREATE RULE edms_b_expire_content_copy_history AS
    ON UPDATE TO public.external_data_management_systems DO  UPDATE public.data_set_copies_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((data_set_copies_history.valid_until_timestamp IS NULL) AND ((data_set_copies_history.edms_id)::bigint = (new.id)::bigint) AND ((data_set_copies_history.valid_from_timestamp)::timestamp with time zone <> CURRENT_TIMESTAMP));


--
-- Name: experiments experiment_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_delete AS
    ON DELETE TO public.experiments DO INSTEAD  DELETE FROM public.experiments_all
  WHERE ((experiments_all.id)::bigint = (old.id)::bigint);


--
-- Name: experiments experiment_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_insert AS
    ON INSERT TO public.experiments DO INSTEAD  INSERT INTO public.experiments_all (id, frozen, frozen_for_samp, frozen_for_data, code, del_id, orig_del, exty_id, is_public, modification_timestamp, perm_id, pers_id_registerer, pers_id_modifier, proj_id, proj_frozen, registration_timestamp, version)
  VALUES (new.id, new.frozen, new.frozen_for_samp, new.frozen_for_data, new.code, new.del_id, new.orig_del, new.exty_id, new.is_public, new.modification_timestamp, new.perm_id, new.pers_id_registerer, new.pers_id_modifier, new.proj_id, new.proj_frozen, new.registration_timestamp, new.version);


--
-- Name: experiments_all experiment_project_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_delete AS
    ON DELETE TO public.experiments_all
   WHERE (old.proj_id IS NOT NULL) DO  UPDATE public.project_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE (((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint) AND (project_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: experiments_all experiment_project_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_insert AS
    ON INSERT TO public.experiments_all
   WHERE (new.proj_id IS NOT NULL) DO ( INSERT INTO public.project_relationships_history (id, main_proj_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.project_relationships_history_id_seq'::regclass), new.proj_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO public.experiment_relationships_history (id, main_expe_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.experiment_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.perm_id
           FROM public.projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: experiments_all experiment_project_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_remove_update AS
    ON UPDATE TO public.experiments_all
   WHERE ((old.proj_id IS NOT NULL) AND (new.proj_id IS NULL)) DO ( UPDATE public.project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint) AND (project_relationships_history.valid_until_timestamp IS NULL));
 UPDATE public.experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.id)::bigint) AND ((experiment_relationships_history.proj_id)::bigint = (old.proj_id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: experiments_all experiment_project_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_project_update AS
    ON UPDATE TO public.experiments_all
   WHERE ((((old.proj_id)::bigint <> (new.proj_id)::bigint) OR (old.proj_id IS NULL)) AND (new.proj_id IS NOT NULL)) DO ( UPDATE public.project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint) AND (project_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.project_relationships_history (id, main_proj_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.project_relationships_history_id_seq'::regclass), new.proj_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 UPDATE public.experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.id)::bigint) AND ((experiment_relationships_history.proj_id)::bigint = (old.proj_id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.experiment_relationships_history (id, main_expe_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.experiment_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.perm_id
           FROM public.projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: experiment_properties experiment_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_properties_delete AS
    ON DELETE TO public.experiment_properties
   WHERE (((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL) OR (old.mate_prop_id IS NOT NULL)) DO  INSERT INTO public.experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.experiment_property_id_seq'::regclass), old.expe_id, old.etpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, CURRENT_TIMESTAMP);


--
-- Name: experiment_properties experiment_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_properties_update AS
    ON UPDATE TO public.experiment_properties
   WHERE (((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint)) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO public.experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.experiment_property_id_seq'::regclass), old.expe_id, old.etpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: experiments experiment_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiment_update AS
    ON UPDATE TO public.experiments DO INSTEAD  UPDATE public.experiments_all SET code = new.code, frozen = new.frozen, frozen_for_samp = new.frozen_for_samp, frozen_for_data = new.frozen_for_data, del_id = new.del_id, orig_del = new.orig_del, exty_id = new.exty_id, is_public = new.is_public, modification_timestamp = new.modification_timestamp, perm_id = new.perm_id, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, proj_id = new.proj_id, proj_frozen = new.proj_frozen, registration_timestamp = new.registration_timestamp, version = new.version
  WHERE ((experiments_all.id)::bigint = (new.id)::bigint);


--
-- Name: experiments_deleted experiments_deleted_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiments_deleted_delete AS
    ON DELETE TO public.experiments_deleted DO INSTEAD  DELETE FROM public.experiments_all
  WHERE ((experiments_all.id)::bigint = (old.id)::bigint);


--
-- Name: experiments_deleted experiments_deleted_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE experiments_deleted_update AS
    ON UPDATE TO public.experiments_deleted DO INSTEAD  UPDATE public.experiments_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((experiments_all.id)::bigint = (new.id)::bigint);


--
-- Name: material_properties material_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE material_properties_delete AS
    ON DELETE TO public.material_properties
   WHERE (((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL) OR (old.mate_prop_id IS NOT NULL)) DO  INSERT INTO public.material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.material_property_id_seq'::regclass), old.mate_id, old.mtpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, CURRENT_TIMESTAMP);


--
-- Name: material_properties material_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE material_properties_update AS
    ON UPDATE TO public.material_properties
   WHERE (((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint)) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO public.material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.material_property_id_seq'::regclass), old.mate_id, old.mtpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: metaproject_assignments metaproject_assignments_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE metaproject_assignments_delete AS
    ON DELETE TO public.metaproject_assignments DO INSTEAD  DELETE FROM public.metaproject_assignments_all
  WHERE ((metaproject_assignments_all.id)::bigint = (old.id)::bigint);


--
-- Name: metaproject_assignments metaproject_assignments_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE metaproject_assignments_insert AS
    ON INSERT TO public.metaproject_assignments DO INSTEAD  INSERT INTO public.metaproject_assignments_all (id, mepr_id, expe_id, samp_id, data_id, mate_id, del_id, creation_date)
  VALUES (new.id, new.mepr_id, new.expe_id, new.samp_id, new.data_id, new.mate_id, new.del_id, new.creation_date);


--
-- Name: metaproject_assignments metaproject_assignments_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE metaproject_assignments_update AS
    ON UPDATE TO public.metaproject_assignments DO INSTEAD  UPDATE public.metaproject_assignments_all SET id = new.id, mepr_id = new.mepr_id, expe_id = new.expe_id, samp_id = new.samp_id, data_id = new.data_id, mate_id = new.mate_id, del_id = new.del_id, creation_date = new.creation_date
  WHERE ((metaproject_assignments_all.id)::bigint = (new.id)::bigint);


--
-- Name: projects project_space_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE project_space_insert AS
    ON INSERT TO public.projects
   WHERE (new.space_id IS NOT NULL) DO  INSERT INTO public.project_relationships_history (id, main_proj_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.project_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM public.spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);


--
-- Name: projects project_space_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE project_space_remove_update AS
    ON UPDATE TO public.projects
   WHERE ((old.space_id IS NOT NULL) AND (new.space_id IS NULL)) DO  UPDATE public.project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((project_relationships_history.main_proj_id)::bigint = (old.id)::bigint) AND ((project_relationships_history.space_id)::bigint = (old.space_id)::bigint) AND (project_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: projects project_space_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE project_space_update AS
    ON UPDATE TO public.projects
   WHERE ((((old.space_id)::bigint <> (new.space_id)::bigint) OR (old.space_id IS NULL)) AND (new.space_id IS NOT NULL)) DO ( UPDATE public.project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((project_relationships_history.main_proj_id)::bigint = (old.id)::bigint) AND ((project_relationships_history.space_id)::bigint = (old.space_id)::bigint) AND (project_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.project_relationships_history (id, main_proj_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.project_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM public.spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: samples_all sample_container_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_delete AS
    ON DELETE TO public.samples_all
   WHERE (old.samp_id_part_of IS NOT NULL) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text));


--
-- Name: samples_all sample_container_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_insert AS
    ON INSERT TO public.samples_all
   WHERE (new.samp_id_part_of IS NOT NULL) DO ( INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.samp_id_part_of, 'CONTAINER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'CONTAINED'::text, new.samp_id_part_of, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id_part_of)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: samples_all sample_container_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_remove_update AS
    ON UPDATE TO public.samples_all
   WHERE ((old.samp_id_part_of IS NOT NULL) AND (new.samp_id_part_of IS NULL)) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text)) OR (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.samp_id_part_of)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL) AND ((sample_relationships_history.relation_type)::text = 'CONTAINED'::text)));


--
-- Name: samples_all sample_container_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_container_update AS
    ON UPDATE TO public.samples_all
   WHERE ((((old.samp_id_part_of)::bigint <> (new.samp_id_part_of)::bigint) OR (old.samp_id_part_of IS NULL)) AND (new.samp_id_part_of IS NOT NULL)) DO ( UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text)) OR (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.samp_id_part_of)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL) AND ((sample_relationships_history.relation_type)::text = 'CONTAINED'::text)));
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.samp_id_part_of, 'CONTAINER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'CONTAINED'::text, new.samp_id_part_of, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id_part_of)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: samples sample_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_delete AS
    ON DELETE TO public.samples DO INSTEAD  DELETE FROM public.samples_all
  WHERE ((samples_all.id)::bigint = (old.id)::bigint);


--
-- Name: samples_deleted sample_deleted_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_deleted_delete AS
    ON DELETE TO public.samples_deleted DO INSTEAD  DELETE FROM public.samples_all
  WHERE ((samples_all.id)::bigint = (old.id)::bigint);


--
-- Name: samples_deleted sample_deleted_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_deleted_update AS
    ON UPDATE TO public.samples_deleted DO INSTEAD  UPDATE public.samples_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((samples_all.id)::bigint = (new.id)::bigint);


--
-- Name: samples_all sample_experiment_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_delete AS
    ON DELETE TO public.samples_all
   WHERE (old.expe_id IS NOT NULL) DO  UPDATE public.experiment_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: samples_all sample_experiment_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_insert AS
    ON INSERT TO public.samples_all
   WHERE (new.expe_id IS NOT NULL) DO ( INSERT INTO public.experiment_relationships_history (id, main_expe_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM public.experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: samples_all sample_experiment_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_remove_update AS
    ON UPDATE TO public.samples_all
   WHERE ((old.expe_id IS NOT NULL) AND (new.expe_id IS NULL)) DO ( UPDATE public.experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.expe_id)::bigint = (old.expe_id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));
);


--
-- Name: samples_all sample_experiment_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_experiment_update AS
    ON UPDATE TO public.samples_all
   WHERE ((((old.expe_id)::bigint <> (new.expe_id)::bigint) OR (old.expe_id IS NULL)) AND (new.expe_id IS NOT NULL)) DO ( UPDATE public.experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.experiment_relationships_history (id, main_expe_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.expe_id)::bigint = (old.expe_id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM public.experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: samples sample_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_insert AS
    ON INSERT TO public.samples DO INSTEAD  INSERT INTO public.samples_all (id, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents, frozen_for_data, code, del_id, orig_del, expe_id, expe_frozen, proj_id, proj_frozen, modification_timestamp, perm_id, pers_id_registerer, pers_id_modifier, registration_timestamp, samp_id_part_of, cont_frozen, saty_id, space_id, space_frozen, version)
  VALUES (new.id, new.frozen, new.frozen_for_comp, new.frozen_for_children, new.frozen_for_parents, new.frozen_for_data, new.code, new.del_id, new.orig_del, new.expe_id, new.expe_frozen, new.proj_id, new.proj_frozen, new.modification_timestamp, new.perm_id, new.pers_id_registerer, new.pers_id_modifier, new.registration_timestamp, new.samp_id_part_of, new.cont_frozen, new.saty_id, new.space_id, new.space_frozen, new.version);


--
-- Name: sample_relationships_all sample_parent_child_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_delete AS
    ON DELETE TO public.sample_relationships_all
   WHERE (old.del_id IS NULL) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_parent)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_child)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL)) OR (((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_child)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_parent)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: sample_relationships_all sample_parent_child_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_insert AS
    ON INSERT TO public.sample_relationships_all
   WHERE (new.del_id IS NULL) DO ( INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.sample_id_parent, 'PARENT'::text, new.sample_id_child, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_child)::bigint)), new.pers_id_author, new.modification_timestamp);
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.sample_id_child, 'CHILD'::text, new.sample_id_parent, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp);
);


--
-- Name: sample_relationships_all sample_parent_child_revert_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_revert_update AS
    ON UPDATE TO public.sample_relationships_all
   WHERE ((new.del_id IS NULL) AND (old.del_id IS NOT NULL)) DO ( INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.sample_id_parent, 'PARENT'::text, new.sample_id_child, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_child)::bigint)), new.pers_id_author, new.modification_timestamp);
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.sample_id_child, 'CHILD'::text, new.sample_id_parent, ( SELECT samples_all.perm_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp);
);


--
-- Name: sample_relationships_all sample_parent_child_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_parent_child_update AS
    ON UPDATE TO public.sample_relationships_all
   WHERE ((new.del_id IS NOT NULL) AND (old.del_id IS NULL)) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = CURRENT_TIMESTAMP
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_parent)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_child)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL)) OR (((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_child)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_parent)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL)));


--
-- Name: samples_all sample_project_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_project_insert AS
    ON INSERT TO public.samples_all
   WHERE ((new.expe_id IS NULL) AND (new.proj_id IS NOT NULL)) DO  INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.perm_id
           FROM public.projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);


--
-- Name: samples_all sample_project_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_project_remove_update AS
    ON UPDATE TO public.samples_all
   WHERE ((old.proj_id IS NOT NULL) AND ((new.proj_id IS NULL) OR ((old.expe_id IS NULL) AND (new.expe_id IS NOT NULL)))) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.proj_id)::bigint = (old.proj_id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: samples_all sample_project_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_project_update AS
    ON UPDATE TO public.samples_all
   WHERE ((((old.proj_id)::bigint <> (new.proj_id)::bigint) OR (old.proj_id IS NULL) OR (old.expe_id IS NOT NULL)) AND (new.proj_id IS NOT NULL) AND (new.expe_id IS NULL)) DO ( UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.proj_id)::bigint = (old.proj_id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.perm_id
           FROM public.projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: sample_properties sample_properties_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_properties_delete AS
    ON DELETE TO public.sample_properties
   WHERE ((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL) OR (old.mate_prop_id IS NOT NULL)) AND (( SELECT samples_all.del_id
           FROM public.samples_all
          WHERE ((samples_all.id)::bigint = (old.samp_id)::bigint)) IS NULL)) DO  INSERT INTO public.sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.sample_property_id_seq'::regclass), old.samp_id, old.stpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, CURRENT_TIMESTAMP);


--
-- Name: sample_properties sample_properties_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_properties_update AS
    ON UPDATE TO public.sample_properties
   WHERE (((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint)) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO public.sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('public.sample_property_id_seq'::regclass), old.samp_id, old.stpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (public.controlled_vocabulary_terms t
             JOIN public.controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
          WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (public.materials m
             JOIN public.material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
          WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, new.modification_timestamp);


--
-- Name: sample_relationships sample_relationships_delete; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_relationships_delete AS
    ON DELETE TO public.sample_relationships DO INSTEAD  DELETE FROM public.sample_relationships_all
  WHERE ((sample_relationships_all.id)::bigint = (old.id)::bigint);


--
-- Name: sample_relationships sample_relationships_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_relationships_insert AS
    ON INSERT TO public.sample_relationships DO INSTEAD  INSERT INTO public.sample_relationships_all (id, sample_id_parent, parent_frozen, relationship_id, sample_id_child, child_frozen, pers_id_author, registration_timestamp, modification_timestamp)
  VALUES (new.id, new.sample_id_parent, new.parent_frozen, new.relationship_id, new.sample_id_child, new.child_frozen, new.pers_id_author, new.registration_timestamp, new.modification_timestamp);


--
-- Name: sample_relationships sample_relationships_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_relationships_update AS
    ON UPDATE TO public.sample_relationships DO INSTEAD  UPDATE public.sample_relationships_all SET sample_id_parent = new.sample_id_parent, parent_frozen = new.parent_frozen, relationship_id = new.relationship_id, sample_id_child = new.sample_id_child, child_frozen = new.child_frozen, del_id = new.del_id, pers_id_author = new.pers_id_author, registration_timestamp = new.registration_timestamp, modification_timestamp = new.modification_timestamp
  WHERE ((sample_relationships_all.id)::bigint = (new.id)::bigint);


--
-- Name: samples_all sample_space_insert; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_space_insert AS
    ON INSERT TO public.samples_all
   WHERE ((new.expe_id IS NULL) AND (new.space_id IS NOT NULL) AND (new.proj_id IS NULL)) DO  INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM public.spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);


--
-- Name: samples_all sample_space_remove_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_space_remove_update AS
    ON UPDATE TO public.samples_all
   WHERE ((old.space_id IS NOT NULL) AND ((new.space_id IS NULL) OR ((old.expe_id IS NULL) AND (new.expe_id IS NOT NULL)) OR ((old.proj_id IS NULL) AND (new.proj_id IS NOT NULL)))) DO  UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.space_id)::bigint = (old.space_id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));


--
-- Name: samples_all sample_space_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_space_update AS
    ON UPDATE TO public.samples_all
   WHERE ((((old.space_id)::bigint <> (new.space_id)::bigint) OR (old.space_id IS NULL) OR (old.expe_id IS NOT NULL) OR (old.proj_id IS NOT NULL)) AND (new.space_id IS NOT NULL) AND (new.expe_id IS NULL) AND (new.proj_id IS NULL)) DO ( UPDATE public.sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE (((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.space_id)::bigint = (old.space_id)::bigint) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO public.sample_relationships_history (id, main_samp_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('public.sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM public.spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);


--
-- Name: samples sample_update; Type: RULE; Schema: public; Owner: -
--

CREATE RULE sample_update AS
    ON UPDATE TO public.samples DO INSTEAD  UPDATE public.samples_all SET code = new.code, frozen = new.frozen, frozen_for_comp = new.frozen_for_comp, frozen_for_children = new.frozen_for_children, frozen_for_parents = new.frozen_for_parents, frozen_for_data = new.frozen_for_data, del_id = new.del_id, orig_del = new.orig_del, expe_id = new.expe_id, expe_frozen = new.expe_frozen, proj_id = new.proj_id, proj_frozen = new.proj_frozen, modification_timestamp = new.modification_timestamp, perm_id = new.perm_id, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, registration_timestamp = new.registration_timestamp, samp_id_part_of = new.samp_id_part_of, cont_frozen = new.cont_frozen, saty_id = new.saty_id, space_id = new.space_id, space_frozen = new.space_frozen, version = new.version
  WHERE ((samples_all.id)::bigint = (new.id)::bigint);


--
-- Name: data_all add_data_set_to_experiment_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_data_set_to_experiment_check AFTER INSERT ON public.data_all FOR EACH ROW WHEN (new.expe_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_experiment_relationship('data set');


--
-- Name: data_all add_data_set_to_sample_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_data_set_to_sample_check AFTER INSERT ON public.data_all FOR EACH ROW WHEN (new.samp_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_data_set_sample_relationship();


--
-- Name: experiments_all add_experiment_to_project_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_experiment_to_project_check AFTER INSERT ON public.experiments_all FOR EACH ROW WHEN (new.proj_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_project_relationship('experiment');


--
-- Name: projects add_project_to_space_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_project_to_space_check AFTER INSERT ON public.projects FOR EACH ROW WHEN (new.space_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_space_relationship('project');


--
-- Name: samples_all add_sample_to_container_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_sample_to_container_check AFTER INSERT ON public.samples_all FOR EACH ROW WHEN (new.cont_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_sample_container_relationship();


--
-- Name: samples_all add_sample_to_experiment_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_sample_to_experiment_check AFTER INSERT ON public.samples_all FOR EACH ROW WHEN (new.expe_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_experiment_relationship('sample');


--
-- Name: samples_all add_sample_to_project_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_sample_to_project_check AFTER INSERT ON public.samples_all FOR EACH ROW WHEN (new.proj_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_project_relationship('sample');


--
-- Name: samples_all add_sample_to_space_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER add_sample_to_space_check AFTER INSERT ON public.samples_all FOR EACH ROW WHEN (new.space_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_space_relationship('sample');


--
-- Name: data_all check_created_or_modified_data_set_owner_is_alive; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_created_or_modified_data_set_owner_is_alive AFTER INSERT OR UPDATE ON public.data_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE public.check_created_or_modified_data_set_owner_is_alive();


--
-- Name: samples_all check_created_or_modified_sample_owner_is_alive; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_created_or_modified_sample_owner_is_alive AFTER INSERT OR UPDATE ON public.samples_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE public.check_created_or_modified_sample_owner_is_alive();


--
-- Name: link_data check_data_set_kind_link; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_data_set_kind_link AFTER INSERT OR UPDATE ON public.link_data DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE public.check_data_set_kind_link();


--
-- Name: external_data check_data_set_kind_physical; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_data_set_kind_physical AFTER INSERT OR UPDATE ON public.external_data DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE public.check_data_set_kind_physical();


--
-- Name: experiments_all check_deletion_consistency_on_experiment_deletion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_experiment_deletion AFTER UPDATE ON public.experiments_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE public.check_deletion_consistency_on_experiment_deletion();


--
-- Name: samples_all check_deletion_consistency_on_sample_deletion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_sample_deletion AFTER UPDATE ON public.samples_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE public.check_deletion_consistency_on_sample_deletion();


--
-- Name: content_copies content_copies_location_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER content_copies_location_type_check BEFORE INSERT OR UPDATE ON public.content_copies FOR EACH ROW EXECUTE PROCEDURE public.content_copies_location_type_check();


--
-- Name: content_copies content_copies_uniqueness_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER content_copies_uniqueness_check BEFORE INSERT OR UPDATE ON public.content_copies FOR EACH ROW EXECUTE PROCEDURE public.content_copies_uniqueness_check();


--
-- Name: property_types controlled_vocabulary_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER controlled_vocabulary_check BEFORE INSERT OR UPDATE ON public.property_types FOR EACH ROW EXECUTE PROCEDURE public.controlled_vocabulary_check();


--
-- Name: data_all data_exp_or_sample_link_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_exp_or_sample_link_check BEFORE INSERT OR UPDATE ON public.data_all FOR EACH ROW EXECUTE PROCEDURE public.data_exp_or_sample_link_check();


--
-- Name: data_all data_set_experiment_relationship_frozen_check_on_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_experiment_relationship_frozen_check_on_update BEFORE UPDATE ON public.data_all FOR EACH ROW WHEN (((((new.expe_id)::bigint <> (old.expe_id)::bigint) OR ((new.expe_id IS NOT NULL) AND (old.expe_id IS NULL)) OR ((new.expe_id IS NULL) AND (old.expe_id IS NOT NULL))) AND (new.expe_frozen OR old.expe_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_experiment_relationship('data set');


--
-- Name: data_set_properties data_set_frozen_check_on_change_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_frozen_check_on_change_property BEFORE UPDATE ON public.data_set_properties FOR EACH ROW WHEN ((old.dase_frozen AND new.dase_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_data_set('PROPERTY');


--
-- Name: data_all data_set_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_frozen_check_on_delete BEFORE DELETE ON public.data_all FOR EACH ROW WHEN (old.frozen) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('DELETE', 'data set');


--
-- Name: data_set_properties data_set_frozen_check_on_delete_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_frozen_check_on_delete_property BEFORE DELETE ON public.data_set_properties FOR EACH ROW WHEN (old.dase_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_data_set('PROPERTY');


--
-- Name: data_set_properties data_set_frozen_check_on_insert_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_frozen_check_on_insert_property BEFORE INSERT ON public.data_set_properties FOR EACH ROW WHEN (new.dase_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_data_set('PROPERTY');


--
-- Name: data_all data_set_frozen_check_on_trash; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_frozen_check_on_trash BEFORE UPDATE ON public.data_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('TRASH', 'data set');


--
-- Name: data_set_properties data_set_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON public.data_set_properties FOR EACH ROW EXECUTE PROCEDURE public.data_set_property_with_material_data_type_check();


--
-- Name: data_set_relationships_all data_set_relationship_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_relationship_frozen_check_on_delete BEFORE DELETE ON public.data_set_relationships_all FOR EACH ROW WHEN ((old.parent_frozen OR old.child_frozen OR old.cont_frozen OR old.comp_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_data_set_relationship();


--
-- Name: data_set_relationships_all data_set_relationship_frozen_check_on_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_relationship_frozen_check_on_insert BEFORE INSERT ON public.data_set_relationships_all FOR EACH ROW WHEN ((new.parent_frozen OR new.child_frozen OR new.cont_frozen OR new.comp_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_data_set_relationship();


--
-- Name: data_all data_set_sample_relationship_frozen_check_on_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_sample_relationship_frozen_check_on_update BEFORE UPDATE ON public.data_all FOR EACH ROW WHEN (((((new.samp_id)::bigint <> (old.samp_id)::bigint) OR ((new.samp_id IS NOT NULL) AND (old.samp_id IS NULL)) OR ((new.samp_id IS NULL) AND (old.samp_id IS NOT NULL))) AND (new.samp_frozen OR old.samp_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_data_set_sample_relationship();


--
-- Name: data_all delete_data_set_from_experiment_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_data_set_from_experiment_check AFTER DELETE ON public.data_all FOR EACH ROW WHEN (old.expe_frozen) EXECUTE PROCEDURE public.raise_delete_from_experiment_exception('DATA SET');


--
-- Name: data_all delete_data_set_from_sample_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_data_set_from_sample_check AFTER DELETE ON public.data_all FOR EACH ROW WHEN (old.samp_frozen) EXECUTE PROCEDURE public.raise_delete_from_sample_exception('DATA SET');


--
-- Name: experiments_all delete_experiment_from_project_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_experiment_from_project_check AFTER DELETE ON public.experiments_all FOR EACH ROW WHEN (old.proj_frozen) EXECUTE PROCEDURE public.raise_delete_from_project_exception('EXPERIMENT');


--
-- Name: projects delete_project_from_space_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_project_from_space_check AFTER DELETE ON public.projects FOR EACH ROW WHEN (old.space_frozen) EXECUTE PROCEDURE public.raise_delete_from_space_exception('PROJECT');


--
-- Name: samples_all delete_sample_from_container_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_sample_from_container_check AFTER DELETE ON public.samples_all FOR EACH ROW WHEN (old.cont_frozen) EXECUTE PROCEDURE public.raise_delete_from_sample_exception('SAMPLE COMPONENT');


--
-- Name: samples_all delete_sample_from_experiment_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_sample_from_experiment_check AFTER DELETE ON public.samples_all FOR EACH ROW WHEN (old.expe_frozen) EXECUTE PROCEDURE public.raise_delete_from_experiment_exception('SAMPLE');


--
-- Name: samples_all delete_sample_from_project_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_sample_from_project_check AFTER DELETE ON public.samples_all FOR EACH ROW WHEN (old.proj_frozen) EXECUTE PROCEDURE public.raise_delete_from_project_exception('SAMPLE');


--
-- Name: samples_all delete_sample_from_space_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER delete_sample_from_space_check AFTER DELETE ON public.samples_all FOR EACH ROW WHEN (old.space_frozen) EXECUTE PROCEDURE public.raise_delete_from_space_exception('SAMPLE');


--
-- Name: samples_all disable_project_level_samples; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER disable_project_level_samples BEFORE INSERT OR UPDATE ON public.samples_all FOR EACH ROW EXECUTE PROCEDURE public.disable_project_level_samples();


--
-- Name: experiment_properties experiment_frozen_check_on_change_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_change_property BEFORE UPDATE ON public.experiment_properties FOR EACH ROW WHEN ((old.expe_frozen AND new.expe_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_experiment('PROPERTY');


--
-- Name: experiments_all experiment_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_delete BEFORE DELETE ON public.experiments_all FOR EACH ROW WHEN (old.frozen) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('DELETE', 'experiment');


--
-- Name: attachments experiment_frozen_check_on_delete_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_delete_attachment BEFORE DELETE ON public.attachments FOR EACH ROW WHEN (old.expe_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_experiment('ATTACHMENT');


--
-- Name: experiment_properties experiment_frozen_check_on_delete_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_delete_property BEFORE DELETE ON public.experiment_properties FOR EACH ROW WHEN (old.expe_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_experiment('PROPERTY');


--
-- Name: attachments experiment_frozen_check_on_insert_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_insert_attachment BEFORE INSERT ON public.attachments FOR EACH ROW WHEN (new.expe_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_experiment('ATTACHMENT');


--
-- Name: experiment_properties experiment_frozen_check_on_insert_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_insert_property BEFORE INSERT ON public.experiment_properties FOR EACH ROW WHEN (new.expe_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_experiment('PROPERTY');


--
-- Name: experiments_all experiment_frozen_check_on_trash; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_trash BEFORE UPDATE ON public.experiments_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('TRASH', 'experiment');


--
-- Name: attachments experiment_frozen_check_on_update_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_frozen_check_on_update_attachment BEFORE UPDATE ON public.attachments FOR EACH ROW WHEN ((old.expe_frozen AND new.expe_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_experiment('ATTACHMENT');


--
-- Name: experiments_all experiment_project_relationship_frozen_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_project_relationship_frozen_check BEFORE UPDATE ON public.experiments_all FOR EACH ROW WHEN ((((new.proj_id)::bigint <> (old.proj_id)::bigint) AND (new.proj_frozen OR old.proj_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_project_relationship('experiment');


--
-- Name: experiment_properties experiment_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON public.experiment_properties FOR EACH ROW EXECUTE PROCEDURE public.experiment_property_with_material_data_type_check();


--
-- Name: external_data external_data_storage_format_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER external_data_storage_format_check BEFORE INSERT OR UPDATE ON public.external_data FOR EACH ROW EXECUTE PROCEDURE public.external_data_storage_format_check();


--
-- Name: material_properties material_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER material_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON public.material_properties FOR EACH ROW EXECUTE PROCEDURE public.material_property_with_material_data_type_check();


--
-- Name: data_all melt_data_set_for; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER melt_data_set_for BEFORE UPDATE ON public.data_all FOR EACH ROW WHEN (((new.frozen_for_children OR new.frozen_for_parents OR new.frozen_for_comps OR new.frozen_for_conts) AND (NOT new.frozen))) EXECUTE PROCEDURE public.melt_data_set_for();


--
-- Name: experiments_all melt_experiment_for; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER melt_experiment_for BEFORE UPDATE ON public.experiments_all FOR EACH ROW WHEN (((new.frozen_for_samp OR new.frozen_for_data) AND (NOT new.frozen))) EXECUTE PROCEDURE public.melt_experiment_for();


--
-- Name: projects melt_project_for; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER melt_project_for BEFORE UPDATE ON public.projects FOR EACH ROW WHEN (((new.frozen_for_exp OR new.frozen_for_samp) AND (NOT new.frozen))) EXECUTE PROCEDURE public.melt_project_for();


--
-- Name: samples_all melt_sample_for; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER melt_sample_for BEFORE UPDATE ON public.samples_all FOR EACH ROW WHEN (((new.frozen_for_comp OR new.frozen_for_children OR new.frozen_for_parents OR new.frozen_for_data) AND (NOT new.frozen))) EXECUTE PROCEDURE public.melt_sample_for();


--
-- Name: spaces melt_space_for; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER melt_space_for BEFORE UPDATE ON public.spaces FOR EACH ROW WHEN (((new.frozen_for_proj OR new.frozen_for_samp) AND (NOT new.frozen))) EXECUTE PROCEDURE public.melt_space_for();


--
-- Name: data_set_relationships_all preserve_deletion_consistency_on_data_set_relationships; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER preserve_deletion_consistency_on_data_set_relationships BEFORE UPDATE ON public.data_set_relationships_all FOR EACH ROW EXECUTE PROCEDURE public.preserve_deletion_consistency_on_data_set_relationships();


--
-- Name: sample_relationships_all preserve_deletion_consistency_on_sample_relationships; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER preserve_deletion_consistency_on_sample_relationships BEFORE UPDATE ON public.sample_relationships_all FOR EACH ROW EXECUTE PROCEDURE public.preserve_deletion_consistency_on_sample_relationships();


--
-- Name: projects project_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER project_frozen_check_on_delete BEFORE DELETE ON public.projects FOR EACH ROW WHEN (old.frozen) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('DELETE', 'project');


--
-- Name: attachments project_frozen_check_on_delete_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER project_frozen_check_on_delete_attachment BEFORE DELETE ON public.attachments FOR EACH ROW WHEN (old.proj_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_project('ATTACHMENT');


--
-- Name: attachments project_frozen_check_on_insert_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER project_frozen_check_on_insert_attachment BEFORE INSERT ON public.attachments FOR EACH ROW WHEN (new.proj_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_project('ATTACHMENT');


--
-- Name: projects project_frozen_check_on_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER project_frozen_check_on_update BEFORE UPDATE ON public.projects FOR EACH ROW WHEN ((old.frozen AND new.frozen AND (((old.description)::text <> (new.description)::text) OR ((old.description IS NULL) AND (new.description IS NOT NULL)) OR ((old.description IS NOT NULL) AND (new.description IS NULL))))) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('UPDATE', 'project');


--
-- Name: attachments project_frozen_check_on_update_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER project_frozen_check_on_update_attachment BEFORE UPDATE ON public.attachments FOR EACH ROW WHEN ((old.proj_frozen AND new.proj_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_project('ATTACHMENT');


--
-- Name: projects project_space_relationship_frozen_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER project_space_relationship_frozen_check BEFORE UPDATE ON public.projects FOR EACH ROW WHEN ((((new.space_id)::bigint <> (old.space_id)::bigint) AND (new.space_frozen OR old.space_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_space_relationship('project');


--
-- Name: samples_all sample_experiment_relationship_frozen_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_experiment_relationship_frozen_check BEFORE UPDATE ON public.samples_all FOR EACH ROW WHEN (((((new.expe_id)::bigint <> (old.expe_id)::bigint) OR ((new.expe_id IS NOT NULL) AND (old.expe_id IS NULL)) OR ((new.expe_id IS NULL) AND (old.expe_id IS NOT NULL))) AND (new.expe_frozen OR old.expe_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_experiment_relationship('sample');


--
-- Name: samples_all sample_fill_code_unique_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_fill_code_unique_check BEFORE INSERT OR UPDATE ON public.samples_all FOR EACH ROW EXECUTE PROCEDURE public.sample_fill_code_unique_check();


--
-- Name: samples_all sample_fill_subcode_unique_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_fill_subcode_unique_check BEFORE INSERT OR UPDATE ON public.samples_all FOR EACH ROW EXECUTE PROCEDURE public.sample_fill_subcode_unique_check();


--
-- Name: sample_properties sample_frozen_check_on_change_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_change_property BEFORE UPDATE ON public.sample_properties FOR EACH ROW WHEN ((old.samp_frozen AND new.samp_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_sample('PROPERTY');


--
-- Name: samples_all sample_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_delete BEFORE DELETE ON public.samples_all FOR EACH ROW WHEN (old.frozen) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('DELETE', 'sample');


--
-- Name: attachments sample_frozen_check_on_delete_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_delete_attachment BEFORE DELETE ON public.attachments FOR EACH ROW WHEN (old.samp_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_sample('ATTACHMENT');


--
-- Name: sample_properties sample_frozen_check_on_delete_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_delete_property BEFORE DELETE ON public.sample_properties FOR EACH ROW WHEN (old.samp_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_sample('PROPERTY');


--
-- Name: attachments sample_frozen_check_on_insert_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_insert_attachment BEFORE INSERT ON public.attachments FOR EACH ROW WHEN (new.samp_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_sample('ATTACHMENT');


--
-- Name: sample_properties sample_frozen_check_on_insert_property; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_insert_property BEFORE INSERT ON public.sample_properties FOR EACH ROW WHEN (new.samp_frozen) EXECUTE PROCEDURE public.raise_exception_frozen_sample('PROPERTY');


--
-- Name: samples_all sample_frozen_check_on_set_container; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_set_container BEFORE UPDATE ON public.samples_all FOR EACH ROW WHEN (((((new.samp_id_part_of)::bigint <> (old.samp_id_part_of)::bigint) OR ((new.samp_id_part_of IS NOT NULL) AND (old.samp_id_part_of IS NULL)) OR ((new.samp_id_part_of IS NULL) AND (old.samp_id_part_of IS NOT NULL))) AND (new.cont_frozen OR old.cont_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_sample_container_relationship();


--
-- Name: samples_all sample_frozen_check_on_trash; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_trash BEFORE UPDATE ON public.samples_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('TRASH', 'sample');


--
-- Name: attachments sample_frozen_check_on_update_attachment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_frozen_check_on_update_attachment BEFORE UPDATE ON public.attachments FOR EACH ROW WHEN ((old.samp_frozen AND new.samp_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_sample('ATTACHMENT');


--
-- Name: samples_all sample_project_relationship_frozen_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_project_relationship_frozen_check BEFORE UPDATE ON public.samples_all FOR EACH ROW WHEN (((((new.proj_id)::bigint <> (old.proj_id)::bigint) OR ((new.proj_id IS NOT NULL) AND (old.proj_id IS NULL)) OR ((new.proj_id IS NULL) AND (old.proj_id IS NOT NULL))) AND (new.proj_frozen OR old.proj_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_project_relationship('sample');


--
-- Name: sample_properties sample_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON public.sample_properties FOR EACH ROW EXECUTE PROCEDURE public.sample_property_with_material_data_type_check();


--
-- Name: sample_relationships_all sample_relationship_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_relationship_frozen_check_on_delete BEFORE DELETE ON public.sample_relationships_all FOR EACH ROW WHEN ((old.parent_frozen OR old.child_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_sample_relationship();


--
-- Name: sample_relationships_all sample_relationship_frozen_check_on_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_relationship_frozen_check_on_insert BEFORE INSERT ON public.sample_relationships_all FOR EACH ROW WHEN ((new.parent_frozen OR new.child_frozen)) EXECUTE PROCEDURE public.raise_exception_frozen_sample_relationship();


--
-- Name: samples_all sample_space_relationship_frozen_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_space_relationship_frozen_check BEFORE UPDATE ON public.samples_all FOR EACH ROW WHEN (((((new.space_id)::bigint <> (old.space_id)::bigint) OR ((new.space_id IS NOT NULL) AND (old.space_id IS NULL)) OR ((new.space_id IS NULL) AND (old.space_id IS NOT NULL))) AND (new.space_frozen OR old.space_frozen))) EXECUTE PROCEDURE public.raise_exception_frozen_space_relationship('sample');


--
-- Name: sample_types sample_type_fill_subcode_unique_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_type_fill_subcode_unique_check AFTER UPDATE ON public.sample_types FOR EACH ROW EXECUTE PROCEDURE public.sample_type_fill_subcode_unique_check();


--
-- Name: spaces space_frozen_check_on_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER space_frozen_check_on_delete BEFORE DELETE ON public.spaces FOR EACH ROW WHEN (old.frozen) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('DELETE', 'space');


--
-- Name: spaces space_frozen_check_on_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER space_frozen_check_on_update BEFORE UPDATE ON public.spaces FOR EACH ROW WHEN ((old.frozen AND new.frozen AND (((old.description)::text <> (new.description)::text) OR ((old.description IS NULL) AND (new.description IS NOT NULL)) OR ((old.description IS NOT NULL) AND (new.description IS NULL))))) EXECUTE PROCEDURE public.raise_exception_frozen_entity_by_code('UPDATE', 'space');


--
-- Name: data_set_relationships_all trash_data_set_from_child_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_data_set_from_child_check AFTER UPDATE ON public.data_set_relationships_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.child_frozen)) EXECUTE PROCEDURE public.raise_delete_from_data_set_exception('DATA SET PARENT');


--
-- Name: data_set_relationships_all trash_data_set_from_component_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_data_set_from_component_check AFTER UPDATE ON public.data_set_relationships_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.comp_frozen)) EXECUTE PROCEDURE public.raise_delete_from_data_set_exception('DATA SET CONTAINER');


--
-- Name: data_set_relationships_all trash_data_set_from_container_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_data_set_from_container_check AFTER UPDATE ON public.data_set_relationships_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.cont_frozen)) EXECUTE PROCEDURE public.raise_delete_from_data_set_exception('DATA SET COMPONENT');


--
-- Name: data_all trash_data_set_from_experiment_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_data_set_from_experiment_check AFTER UPDATE ON public.data_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.expe_frozen)) EXECUTE PROCEDURE public.raise_delete_from_experiment_exception('DATA SET');


--
-- Name: data_set_relationships_all trash_data_set_from_parent_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_data_set_from_parent_check AFTER UPDATE ON public.data_set_relationships_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.parent_frozen)) EXECUTE PROCEDURE public.raise_delete_from_data_set_exception('DATA SET CHILD');


--
-- Name: data_all trash_data_set_from_sample_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_data_set_from_sample_check AFTER UPDATE ON public.data_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.samp_frozen)) EXECUTE PROCEDURE public.raise_delete_from_sample_exception('DATA SET');


--
-- Name: experiments_all trash_experiment_from_project_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_experiment_from_project_check AFTER UPDATE ON public.experiments_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.proj_frozen)) EXECUTE PROCEDURE public.raise_delete_from_project_exception('EXPERIMENT');


--
-- Name: sample_relationships_all trash_sample_from_child_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_sample_from_child_check AFTER UPDATE ON public.sample_relationships_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.child_frozen)) EXECUTE PROCEDURE public.raise_delete_from_sample_exception('SAMPLE PARENT');


--
-- Name: samples_all trash_sample_from_container_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_sample_from_container_check AFTER UPDATE ON public.samples_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.cont_frozen)) EXECUTE PROCEDURE public.raise_delete_from_sample_exception('SAMPLE COMPONENT');


--
-- Name: samples_all trash_sample_from_experiment_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_sample_from_experiment_check AFTER UPDATE ON public.samples_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.expe_frozen)) EXECUTE PROCEDURE public.raise_delete_from_experiment_exception('SAMPLE');


--
-- Name: sample_relationships_all trash_sample_from_parent_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_sample_from_parent_check AFTER UPDATE ON public.sample_relationships_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.parent_frozen)) EXECUTE PROCEDURE public.raise_delete_from_sample_exception('SAMPLE CHILD');


--
-- Name: samples_all trash_sample_from_project_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_sample_from_project_check AFTER UPDATE ON public.samples_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.proj_frozen)) EXECUTE PROCEDURE public.raise_delete_from_project_exception('SAMPLE');


--
-- Name: samples_all trash_sample_from_space_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trash_sample_from_space_check AFTER UPDATE ON public.samples_all FOR EACH ROW WHEN (((new.del_id IS NOT NULL) AND (old.del_id IS NULL) AND old.space_frozen)) EXECUTE PROCEDURE public.raise_delete_from_space_exception('SAMPLE');


--
-- Name: authorization_groups ag_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorization_groups
    ADD CONSTRAINT ag_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: authorization_group_persons agp_ag_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorization_group_persons
    ADD CONSTRAINT agp_ag_fk FOREIGN KEY (ag_id) REFERENCES public.authorization_groups(id);


--
-- Name: authorization_group_persons agp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorization_group_persons
    ADD CONSTRAINT agp_pers_fk FOREIGN KEY (pers_id) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: attachments atta_cont_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES public.attachment_contents(id);


--
-- Name: attachments atta_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id, expe_frozen) REFERENCES public.experiments_all(id, frozen) ON UPDATE CASCADE;


--
-- Name: attachments atta_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: attachments atta_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id, proj_frozen) REFERENCES public.projects(id, frozen) ON UPDATE CASCADE;


--
-- Name: attachments atta_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id, samp_frozen) REFERENCES public.samples_all(id, frozen) ON UPDATE CASCADE;


--
-- Name: content_copies coco_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_copies
    ADD CONSTRAINT coco_data_fk FOREIGN KEY (data_id, data_frozen) REFERENCES public.link_data(id, data_frozen) ON UPDATE CASCADE;


--
-- Name: content_copies coco_edms_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_copies
    ADD CONSTRAINT coco_edms_fk FOREIGN KEY (edms_id) REFERENCES public.external_data_management_systems(id);


--
-- Name: controlled_vocabularies covo_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: controlled_vocabulary_terms cvte_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES public.controlled_vocabularies(id);


--
-- Name: controlled_vocabulary_terms cvte_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_all data_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES public.data_stores(id);


--
-- Name: data_all data_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_del_fk FOREIGN KEY (del_id) REFERENCES public.deletions(id);


--
-- Name: data_all data_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES public.data_set_types(id);


--
-- Name: data_all data_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id, expe_frozen) REFERENCES public.experiments_all(id, frozen_for_data) ON UPDATE CASCADE;


--
-- Name: data_all data_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_all data_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_all data_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_all
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id, samp_frozen) REFERENCES public.samples_all(id, frozen_for_data) ON UPDATE CASCADE;


--
-- Name: data_set_relationships_all data_set_relationships_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT data_set_relationships_pers_fk FOREIGN KEY (pers_id_author) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_set_relationships_history datarelh_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_history
    ADD CONSTRAINT datarelh_data_fk FOREIGN KEY (data_id) REFERENCES public.data_all(id) ON DELETE SET NULL;


--
-- Name: data_set_relationships_history datarelh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_history
    ADD CONSTRAINT datarelh_expe_fk FOREIGN KEY (expe_id) REFERENCES public.experiments_all(id) ON DELETE SET NULL;


--
-- Name: data_set_relationships_history datarelh_main_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_history
    ADD CONSTRAINT datarelh_main_data_fk FOREIGN KEY (main_data_id) REFERENCES public.data_all(id) ON DELETE CASCADE;


--
-- Name: data_set_relationships_history datarelh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_history
    ADD CONSTRAINT datarelh_samp_fk FOREIGN KEY (samp_id) REFERENCES public.samples_all(id) ON DELETE SET NULL;


--
-- Name: deletions del_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deletions
    ADD CONSTRAINT del_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_set_properties dspr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES public.controlled_vocabulary_terms(id);


--
-- Name: data_set_properties dspr_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id, dase_frozen) REFERENCES public.data_all(id, frozen) ON UPDATE CASCADE;


--
-- Name: data_set_properties dspr_dstpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES public.data_set_type_property_types(id) ON DELETE CASCADE;


--
-- Name: data_set_properties dspr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES public.materials(id);


--
-- Name: data_set_properties dspr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_set_properties_history dsprh_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties_history
    ADD CONSTRAINT dsprh_ds_fk FOREIGN KEY (ds_id) REFERENCES public.data_all(id) ON DELETE CASCADE;


--
-- Name: data_set_properties_history dsprh_dstpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_properties_history
    ADD CONSTRAINT dsprh_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES public.data_set_type_property_types(id) ON DELETE CASCADE;


--
-- Name: data_set_relationships_all dsre_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child, child_frozen) REFERENCES public.data_all(id, frozen_for_parents) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: data_set_relationships_all dsre_data_fk_comp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_comp FOREIGN KEY (data_id_child, comp_frozen) REFERENCES public.data_all(id, frozen_for_conts) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: data_set_relationships_all dsre_data_fk_cont; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_cont FOREIGN KEY (data_id_parent, cont_frozen) REFERENCES public.data_all(id, frozen_for_comps) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: data_set_relationships_all dsre_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent, parent_frozen) REFERENCES public.data_all(id, frozen_for_children) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: data_set_relationships_all dsre_data_fk_relationship; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES public.relationship_types(id);


--
-- Name: data_set_relationships_all dsre_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_relationships_all
    ADD CONSTRAINT dsre_del_fk FOREIGN KEY (del_id) REFERENCES public.deletions(id);


--
-- Name: data_store_service_data_set_types dssdst_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_store_service_data_set_types
    ADD CONSTRAINT dssdst_ds_fk FOREIGN KEY (data_store_service_id) REFERENCES public.data_store_services(id) ON DELETE CASCADE;


--
-- Name: data_store_service_data_set_types dssdst_dst_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_store_service_data_set_types
    ADD CONSTRAINT dssdst_dst_fk FOREIGN KEY (data_set_type_id) REFERENCES public.data_set_types(id) ON DELETE CASCADE;


--
-- Name: data_store_services dsse_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_store_services
    ADD CONSTRAINT dsse_ds_fk FOREIGN KEY (data_store_id) REFERENCES public.data_stores(id) ON DELETE CASCADE;


--
-- Name: data_set_type_property_types dstpt_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_type_property_types
    ADD CONSTRAINT dstpt_dsty_fk FOREIGN KEY (dsty_id) REFERENCES public.data_set_types(id) ON DELETE CASCADE;


--
-- Name: data_set_type_property_types dstpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_type_property_types
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: data_set_type_property_types dstpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES public.property_types(id) ON DELETE CASCADE;


--
-- Name: data_set_type_property_types dstpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_type_property_types
    ADD CONSTRAINT dstpt_script_fk FOREIGN KEY (script_id) REFERENCES public.scripts(id);


--
-- Name: data_set_types dsty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_set_types
    ADD CONSTRAINT dsty_script_fk FOREIGN KEY (validation_script_id) REFERENCES public.scripts(id);


--
-- Name: experiment_type_property_types etpt_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES public.experiment_types(id) ON DELETE CASCADE;


--
-- Name: experiment_type_property_types etpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: experiment_type_property_types etpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES public.property_types(id) ON DELETE CASCADE;


--
-- Name: experiment_type_property_types etpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_type_property_types
    ADD CONSTRAINT etpt_script_fk FOREIGN KEY (script_id) REFERENCES public.scripts(id);


--
-- Name: events evnt_exac_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT evnt_exac_fk FOREIGN KEY (exac_id) REFERENCES public.attachment_contents(id);


--
-- Name: events evnt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: external_data exda_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_cvte_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES public.controlled_vocabulary_terms(id);


--
-- Name: external_data exda_cvte_stored_on_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_cvte_stored_on_fk FOREIGN KEY (cvte_id_store) REFERENCES public.controlled_vocabulary_terms(id);


--
-- Name: external_data exda_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (id) REFERENCES public.data_all(id);


--
-- Name: external_data exda_ffty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES public.file_format_types(id);


--
-- Name: external_data exda_loty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES public.locator_types(id);


--
-- Name: experiments_all expe_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_del_fk FOREIGN KEY (del_id) REFERENCES public.deletions(id);


--
-- Name: experiments_all expe_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES public.experiment_types(id);


--
-- Name: experiments_all expe_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: experiments_all expe_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: experiments_all expe_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiments_all
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id, proj_frozen) REFERENCES public.projects(id, frozen_for_exp) ON UPDATE CASCADE;


--
-- Name: experiment_properties expr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES public.controlled_vocabulary_terms(id);


--
-- Name: experiment_properties expr_etpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_etpt_fk FOREIGN KEY (etpt_id) REFERENCES public.experiment_type_property_types(id) ON DELETE CASCADE;


--
-- Name: experiment_properties expr_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id, expe_frozen) REFERENCES public.experiments_all(id, frozen) ON UPDATE CASCADE;


--
-- Name: experiment_properties expr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES public.materials(id);


--
-- Name: experiment_properties expr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: experiment_properties_history exprh_etpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties_history
    ADD CONSTRAINT exprh_etpt_fk FOREIGN KEY (etpt_id) REFERENCES public.experiment_type_property_types(id) ON DELETE CASCADE;


--
-- Name: experiment_properties_history exprh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_properties_history
    ADD CONSTRAINT exprh_expe_fk FOREIGN KEY (expe_id) REFERENCES public.experiments_all(id) ON DELETE CASCADE;


--
-- Name: experiment_relationships_history exrelh_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_relationships_history
    ADD CONSTRAINT exrelh_data_fk FOREIGN KEY (data_id) REFERENCES public.data_all(id) ON DELETE SET NULL;


--
-- Name: experiment_relationships_history exrelh_main_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_relationships_history
    ADD CONSTRAINT exrelh_main_expe_fk FOREIGN KEY (main_expe_id) REFERENCES public.experiments_all(id) ON DELETE CASCADE;


--
-- Name: experiment_relationships_history exrelh_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_relationships_history
    ADD CONSTRAINT exrelh_proj_fk FOREIGN KEY (proj_id) REFERENCES public.projects(id) ON DELETE SET NULL;


--
-- Name: experiment_relationships_history exrelh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_relationships_history
    ADD CONSTRAINT exrelh_samp_fk FOREIGN KEY (samp_id) REFERENCES public.samples_all(id) ON DELETE SET NULL;


--
-- Name: experiment_types exty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.experiment_types
    ADD CONSTRAINT exty_script_fk FOREIGN KEY (validation_script_id) REFERENCES public.scripts(id);


--
-- Name: filters filt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.filters
    ADD CONSTRAINT filt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: grid_custom_columns grid_custom_columns_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: link_data lnda_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_data
    ADD CONSTRAINT lnda_data_fk FOREIGN KEY (id, data_frozen) REFERENCES public.data_all(id, frozen) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: material_properties mapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES public.controlled_vocabulary_terms(id);


--
-- Name: material_properties mapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES public.materials(id);


--
-- Name: material_properties mapr_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES public.materials(id);


--
-- Name: material_properties mapr_mtpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES public.material_type_property_types(id) ON DELETE CASCADE;


--
-- Name: material_properties mapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: material_properties_history maprh_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties_history
    ADD CONSTRAINT maprh_mate_fk FOREIGN KEY (mate_id) REFERENCES public.materials(id) ON DELETE CASCADE;


--
-- Name: material_properties_history maprh_mtpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_properties_history
    ADD CONSTRAINT maprh_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES public.material_type_property_types(id) ON DELETE CASCADE;


--
-- Name: materials mate_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES public.material_types(id);


--
-- Name: materials mate_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: material_types maty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_types
    ADD CONSTRAINT maty_script_fk FOREIGN KEY (validation_script_id) REFERENCES public.scripts(id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_data_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_data_id_fk FOREIGN KEY (data_id) REFERENCES public.data_all(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all metaproject_assignments_all_del_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_del_id_fk FOREIGN KEY (del_id) REFERENCES public.deletions(id);


--
-- Name: metaproject_assignments_all metaproject_assignments_all_expe_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_expe_id_fk FOREIGN KEY (expe_id) REFERENCES public.experiments_all(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all metaproject_assignments_all_mate_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mate_id_fk FOREIGN KEY (mate_id) REFERENCES public.materials(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all metaproject_assignments_all_mepr_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_fk FOREIGN KEY (mepr_id) REFERENCES public.metaprojects(id) ON DELETE CASCADE;


--
-- Name: metaproject_assignments_all metaproject_assignments_all_samp_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_samp_id_fk FOREIGN KEY (samp_id) REFERENCES public.samples_all(id) ON DELETE CASCADE;


--
-- Name: metaprojects metaprojects_owner_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metaprojects
    ADD CONSTRAINT metaprojects_owner_fk FOREIGN KEY (owner) REFERENCES public.persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: material_type_property_types mtpt_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES public.material_types(id) ON DELETE CASCADE;


--
-- Name: material_type_property_types mtpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: material_type_property_types mtpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES public.property_types(id) ON DELETE CASCADE;


--
-- Name: material_type_property_types mtpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material_type_property_types
    ADD CONSTRAINT mtpt_script_fk FOREIGN KEY (script_id) REFERENCES public.scripts(id);


--
-- Name: operation_executions operation_executions_owner_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_executions
    ADD CONSTRAINT operation_executions_owner_fk FOREIGN KEY (owner) REFERENCES public.persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: persons pers_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: persons pers_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persons
    ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES public.spaces(id);


--
-- Name: post_registration_dataset_queue prdq_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.post_registration_dataset_queue
    ADD CONSTRAINT prdq_ds_fk FOREIGN KEY (ds_id) REFERENCES public.data_all(id) ON DELETE CASCADE;


--
-- Name: projects proj_pers_fk_leader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: projects proj_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: projects proj_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: projects proj_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT proj_space_fk FOREIGN KEY (space_id, space_frozen) REFERENCES public.spaces(id, frozen_for_proj) ON UPDATE CASCADE;


--
-- Name: project_relationships_history prrelh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_relationships_history
    ADD CONSTRAINT prrelh_expe_fk FOREIGN KEY (expe_id) REFERENCES public.experiments_all(id) ON DELETE SET NULL;


--
-- Name: project_relationships_history prrelh_main_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_relationships_history
    ADD CONSTRAINT prrelh_main_proj_fk FOREIGN KEY (main_proj_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: project_relationships_history prrelh_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_relationships_history
    ADD CONSTRAINT prrelh_space_fk FOREIGN KEY (space_id) REFERENCES public.spaces(id) ON DELETE SET NULL;


--
-- Name: property_types prty_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES public.controlled_vocabularies(id);


--
-- Name: property_types prty_daty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES public.data_types(id);


--
-- Name: property_types prty_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES public.material_types(id) ON DELETE CASCADE;


--
-- Name: property_types prty_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: queries quer_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.queries
    ADD CONSTRAINT quer_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: role_assignments roas_ag_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_assignments
    ADD CONSTRAINT roas_ag_fk_grantee FOREIGN KEY (ag_id_grantee) REFERENCES public.authorization_groups(id) ON DELETE CASCADE;


--
-- Name: role_assignments roas_pers_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES public.persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: role_assignments roas_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: role_assignments roas_project_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_assignments
    ADD CONSTRAINT roas_project_fk FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: role_assignments roas_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_assignments
    ADD CONSTRAINT roas_space_fk FOREIGN KEY (space_id) REFERENCES public.spaces(id) ON DELETE CASCADE;


--
-- Name: samples_all samp_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_del_fk FOREIGN KEY (del_id) REFERENCES public.deletions(id);


--
-- Name: samples_all samp_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id, expe_frozen) REFERENCES public.experiments_all(id, frozen_for_samp) ON UPDATE CASCADE;


--
-- Name: samples_all samp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: samples_all samp_pers_fk_mod; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: samples_all samp_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_proj_fk FOREIGN KEY (proj_id, proj_frozen) REFERENCES public.projects(id, frozen_for_samp) ON UPDATE CASCADE;


--
-- Name: samples_all samp_samp_fk_part_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of, cont_frozen) REFERENCES public.samples_all(id, frozen_for_comp) ON UPDATE CASCADE;


--
-- Name: samples_all samp_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES public.sample_types(id);


--
-- Name: samples_all samp_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.samples_all
    ADD CONSTRAINT samp_space_fk FOREIGN KEY (space_id, space_frozen) REFERENCES public.spaces(id, frozen_for_samp) ON UPDATE CASCADE;


--
-- Name: sample_relationships_all sample_relationships_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sample_relationships_pers_fk FOREIGN KEY (pers_id_author) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sample_relationships_history samprelh_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_data_fk FOREIGN KEY (data_id) REFERENCES public.data_all(id) ON DELETE SET NULL;


--
-- Name: sample_relationships_history samprelh_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_expe_fk FOREIGN KEY (expe_id) REFERENCES public.experiments_all(id) ON DELETE SET NULL;


--
-- Name: sample_relationships_history samprelh_main_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_main_samp_fk FOREIGN KEY (main_samp_id) REFERENCES public.samples_all(id) ON DELETE CASCADE;


--
-- Name: sample_relationships_history samprelh_project_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_project_fk FOREIGN KEY (proj_id) REFERENCES public.projects(id) ON DELETE SET NULL;


--
-- Name: sample_relationships_history samprelh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_samp_fk FOREIGN KEY (samp_id) REFERENCES public.samples_all(id) ON DELETE SET NULL;


--
-- Name: sample_relationships_history samprelh_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_history
    ADD CONSTRAINT samprelh_space_fk FOREIGN KEY (space_id) REFERENCES public.spaces(id) ON DELETE SET NULL;


--
-- Name: sample_properties sapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES public.controlled_vocabulary_terms(id);


--
-- Name: sample_properties sapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES public.materials(id);


--
-- Name: sample_properties sapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sample_properties sapr_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id, samp_frozen) REFERENCES public.samples_all(id, frozen) ON UPDATE CASCADE;


--
-- Name: sample_properties sapr_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES public.sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: sample_properties_history saprh_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties_history
    ADD CONSTRAINT saprh_samp_fk FOREIGN KEY (samp_id) REFERENCES public.samples_all(id) ON DELETE CASCADE;


--
-- Name: sample_properties_history saprh_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_properties_history
    ADD CONSTRAINT saprh_stpt_fk FOREIGN KEY (stpt_id) REFERENCES public.sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: sample_relationships_all sare_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child, child_frozen) REFERENCES public.samples_all(id, frozen_for_parents) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: sample_relationships_all sare_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent, parent_frozen) REFERENCES public.samples_all(id, frozen_for_children) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: sample_relationships_all sare_data_fk_relationship; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sare_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES public.relationship_types(id);


--
-- Name: sample_relationships_all sare_del_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_relationships_all
    ADD CONSTRAINT sare_del_fk FOREIGN KEY (del_id) REFERENCES public.deletions(id);


--
-- Name: sample_types saty_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_types
    ADD CONSTRAINT saty_script_fk FOREIGN KEY (validation_script_id) REFERENCES public.scripts(id);


--
-- Name: scripts scri_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.scripts
    ADD CONSTRAINT scri_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: semantic_annotations semantic_annotations_prty_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semantic_annotations
    ADD CONSTRAINT semantic_annotations_prty_id_fk FOREIGN KEY (prty_id) REFERENCES public.property_types(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: semantic_annotations semantic_annotations_saty_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semantic_annotations
    ADD CONSTRAINT semantic_annotations_saty_id_fk FOREIGN KEY (saty_id) REFERENCES public.sample_types(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: semantic_annotations semantic_annotations_stpt_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semantic_annotations
    ADD CONSTRAINT semantic_annotations_stpt_id_fk FOREIGN KEY (stpt_id) REFERENCES public.sample_type_property_types(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: spaces space_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spaces
    ADD CONSTRAINT space_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sample_type_property_types stpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES public.persons(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sample_type_property_types stpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES public.property_types(id) ON DELETE CASCADE;


--
-- Name: sample_type_property_types stpt_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES public.sample_types(id) ON DELETE CASCADE;


--
-- Name: sample_type_property_types stpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sample_type_property_types
    ADD CONSTRAINT stpt_script_fk FOREIGN KEY (script_id) REFERENCES public.scripts(id);


--
-- Name: SEQUENCE attachment_content_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.attachment_content_id_seq TO openbis_readonly;


--
-- Name: TABLE attachment_contents; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.attachment_contents TO openbis_readonly;


--
-- Name: SEQUENCE attachment_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.attachment_id_seq TO openbis_readonly;


--
-- Name: TABLE attachments; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.attachments TO openbis_readonly;


--
-- Name: SEQUENCE authorization_group_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.authorization_group_id_seq TO openbis_readonly;


--
-- Name: TABLE authorization_group_persons; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.authorization_group_persons TO openbis_readonly;


--
-- Name: TABLE authorization_groups; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.authorization_groups TO openbis_readonly;


--
-- Name: SEQUENCE code_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.code_seq TO openbis_readonly;


--
-- Name: TABLE controlled_vocabularies; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.controlled_vocabularies TO openbis_readonly;


--
-- Name: SEQUENCE controlled_vocabulary_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.controlled_vocabulary_id_seq TO openbis_readonly;


--
-- Name: TABLE controlled_vocabulary_terms; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.controlled_vocabulary_terms TO openbis_readonly;


--
-- Name: SEQUENCE core_plugin_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.core_plugin_id_seq TO openbis_readonly;


--
-- Name: TABLE core_plugins; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.core_plugins TO openbis_readonly;


--
-- Name: SEQUENCE cvte_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.cvte_id_seq TO openbis_readonly;


--
-- Name: TABLE data_all; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_all TO openbis_readonly;


--
-- Name: TABLE data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data TO openbis_readonly;


--
-- Name: TABLE data_deleted; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_deleted TO openbis_readonly;


--
-- Name: SEQUENCE data_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_id_seq TO openbis_readonly;


--
-- Name: TABLE data_set_properties_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_properties_history TO openbis_readonly;


--
-- Name: TABLE data_set_relationships_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_relationships_history TO openbis_readonly;


--
-- Name: TABLE data_set_history_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_history_view TO openbis_readonly;


--
-- Name: TABLE data_set_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_properties TO openbis_readonly;


--
-- Name: SEQUENCE data_set_property_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_set_property_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE data_set_relationship_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_set_relationship_id_seq TO openbis_readonly;


--
-- Name: TABLE data_set_relationships_all; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_relationships_all TO openbis_readonly;


--
-- Name: TABLE data_set_relationships; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_relationships TO openbis_readonly;


--
-- Name: SEQUENCE data_set_relationships_history_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_set_relationships_history_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE data_set_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_set_type_id_seq TO openbis_readonly;


--
-- Name: TABLE data_set_type_property_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_type_property_types TO openbis_readonly;


--
-- Name: TABLE data_set_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_set_types TO openbis_readonly;


--
-- Name: SEQUENCE data_store_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_store_id_seq TO openbis_readonly;


--
-- Name: TABLE data_store_service_data_set_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_store_service_data_set_types TO openbis_readonly;


--
-- Name: TABLE data_store_services; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_store_services TO openbis_readonly;


--
-- Name: SEQUENCE data_store_services_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_store_services_id_seq TO openbis_readonly;


--
-- Name: TABLE data_stores; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_stores TO openbis_readonly;


--
-- Name: SEQUENCE data_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.data_type_id_seq TO openbis_readonly;


--
-- Name: TABLE data_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.data_types TO openbis_readonly;


--
-- Name: SEQUENCE database_instance_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.database_instance_id_seq TO openbis_readonly;


--
-- Name: TABLE database_version_logs; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.database_version_logs TO openbis_readonly;


--
-- Name: SEQUENCE deletion_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.deletion_id_seq TO openbis_readonly;


--
-- Name: TABLE deletions; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.deletions TO openbis_readonly;


--
-- Name: SEQUENCE dstpt_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.dstpt_id_seq TO openbis_readonly;


--
-- Name: TABLE entity_operations_log; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.entity_operations_log TO openbis_readonly;


--
-- Name: SEQUENCE entity_operations_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.entity_operations_log_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE etpt_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.etpt_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE event_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.event_id_seq TO openbis_readonly;


--
-- Name: TABLE events; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.events TO openbis_readonly;


--
-- Name: SEQUENCE experiment_code_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.experiment_code_seq TO openbis_readonly;


--
-- Name: TABLE experiment_properties_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiment_properties_history TO openbis_readonly;


--
-- Name: TABLE experiment_relationships_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiment_relationships_history TO openbis_readonly;


--
-- Name: TABLE experiment_history_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiment_history_view TO openbis_readonly;


--
-- Name: SEQUENCE experiment_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.experiment_id_seq TO openbis_readonly;


--
-- Name: TABLE experiment_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiment_properties TO openbis_readonly;


--
-- Name: SEQUENCE experiment_property_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.experiment_property_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE experiment_relationships_history_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.experiment_relationships_history_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE experiment_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.experiment_type_id_seq TO openbis_readonly;


--
-- Name: TABLE experiment_type_property_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiment_type_property_types TO openbis_readonly;


--
-- Name: TABLE experiment_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiment_types TO openbis_readonly;


--
-- Name: TABLE experiments_all; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiments_all TO openbis_readonly;


--
-- Name: TABLE experiments; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiments TO openbis_readonly;


--
-- Name: TABLE experiments_deleted; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.experiments_deleted TO openbis_readonly;


--
-- Name: TABLE external_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.external_data TO openbis_readonly;


--
-- Name: SEQUENCE external_data_management_system_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.external_data_management_system_id_seq TO openbis_readonly;


--
-- Name: TABLE external_data_management_systems; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.external_data_management_systems TO openbis_readonly;


--
-- Name: SEQUENCE file_format_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.file_format_type_id_seq TO openbis_readonly;


--
-- Name: TABLE file_format_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.file_format_types TO openbis_readonly;


--
-- Name: SEQUENCE filter_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.filter_id_seq TO openbis_readonly;


--
-- Name: TABLE filters; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.filters TO openbis_readonly;


--
-- Name: TABLE grid_custom_columns; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.grid_custom_columns TO openbis_readonly;


--
-- Name: SEQUENCE grid_custom_columns_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.grid_custom_columns_id_seq TO openbis_readonly;


--
-- Name: TABLE link_data; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.link_data TO openbis_readonly;


--
-- Name: SEQUENCE locator_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.locator_type_id_seq TO openbis_readonly;


--
-- Name: TABLE locator_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.locator_types TO openbis_readonly;


--
-- Name: SEQUENCE material_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.material_id_seq TO openbis_readonly;


--
-- Name: TABLE material_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.material_properties TO openbis_readonly;


--
-- Name: TABLE material_properties_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.material_properties_history TO openbis_readonly;


--
-- Name: SEQUENCE material_property_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.material_property_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE material_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.material_type_id_seq TO openbis_readonly;


--
-- Name: TABLE material_type_property_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.material_type_property_types TO openbis_readonly;


--
-- Name: TABLE material_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.material_types TO openbis_readonly;


--
-- Name: TABLE materials; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.materials TO openbis_readonly;


--
-- Name: SEQUENCE metaproject_assignment_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.metaproject_assignment_id_seq TO openbis_readonly;


--
-- Name: TABLE metaproject_assignments_all; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.metaproject_assignments_all TO openbis_readonly;


--
-- Name: TABLE metaproject_assignments; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.metaproject_assignments TO openbis_readonly;


--
-- Name: SEQUENCE metaproject_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.metaproject_id_seq TO openbis_readonly;


--
-- Name: TABLE metaprojects; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.metaprojects TO openbis_readonly;


--
-- Name: SEQUENCE mtpt_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.mtpt_id_seq TO openbis_readonly;


--
-- Name: TABLE operation_executions; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.operation_executions TO openbis_readonly;


--
-- Name: SEQUENCE perm_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.perm_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE person_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.person_id_seq TO openbis_readonly;


--
-- Name: TABLE persons; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.persons TO openbis_readonly;


--
-- Name: TABLE post_registration_dataset_queue; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.post_registration_dataset_queue TO openbis_readonly;


--
-- Name: SEQUENCE post_registration_dataset_queue_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.post_registration_dataset_queue_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE project_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.project_id_seq TO openbis_readonly;


--
-- Name: TABLE project_relationships_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.project_relationships_history TO openbis_readonly;


--
-- Name: SEQUENCE project_relationships_history_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.project_relationships_history_id_seq TO openbis_readonly;


--
-- Name: TABLE projects; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.projects TO openbis_readonly;


--
-- Name: SEQUENCE property_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.property_type_id_seq TO openbis_readonly;


--
-- Name: TABLE property_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.property_types TO openbis_readonly;


--
-- Name: TABLE queries; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.queries TO openbis_readonly;


--
-- Name: SEQUENCE query_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.query_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE relationship_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.relationship_type_id_seq TO openbis_readonly;


--
-- Name: TABLE relationship_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.relationship_types TO openbis_readonly;


--
-- Name: SEQUENCE role_assignment_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.role_assignment_id_seq TO openbis_readonly;


--
-- Name: TABLE role_assignments; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.role_assignments TO openbis_readonly;


--
-- Name: SEQUENCE sample_code_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.sample_code_seq TO openbis_readonly;


--
-- Name: TABLE sample_properties_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_properties_history TO openbis_readonly;


--
-- Name: TABLE sample_relationships_history; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_relationships_history TO openbis_readonly;


--
-- Name: TABLE sample_history_view; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_history_view TO openbis_readonly;


--
-- Name: SEQUENCE sample_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.sample_id_seq TO openbis_readonly;


--
-- Name: TABLE sample_properties; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_properties TO openbis_readonly;


--
-- Name: SEQUENCE sample_property_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.sample_property_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE sample_relationship_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.sample_relationship_id_seq TO openbis_readonly;


--
-- Name: TABLE sample_relationships_all; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_relationships_all TO openbis_readonly;


--
-- Name: TABLE sample_relationships; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_relationships TO openbis_readonly;


--
-- Name: SEQUENCE sample_relationships_history_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.sample_relationships_history_id_seq TO openbis_readonly;


--
-- Name: SEQUENCE sample_type_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.sample_type_id_seq TO openbis_readonly;


--
-- Name: TABLE sample_type_property_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_type_property_types TO openbis_readonly;


--
-- Name: TABLE sample_types; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.sample_types TO openbis_readonly;


--
-- Name: TABLE samples_all; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.samples_all TO openbis_readonly;


--
-- Name: TABLE samples; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.samples TO openbis_readonly;


--
-- Name: TABLE samples_deleted; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.samples_deleted TO openbis_readonly;


--
-- Name: SEQUENCE script_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.script_id_seq TO openbis_readonly;


--
-- Name: TABLE scripts; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.scripts TO openbis_readonly;


--
-- Name: SEQUENCE space_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.space_id_seq TO openbis_readonly;


--
-- Name: TABLE spaces; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.spaces TO openbis_readonly;


--
-- Name: SEQUENCE stpt_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON SEQUENCE public.stpt_id_seq TO openbis_readonly;


--
-- PostgreSQL database dump complete
--

