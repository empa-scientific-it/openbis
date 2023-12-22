SET statement_timeout = 0;
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;
SET search_path = public, pg_catalog;
CREATE DOMAIN archiving_status AS character varying(100)
	CONSTRAINT archiving_status_check CHECK (((VALUE)::text = ANY (ARRAY[('LOCKED'::character varying)::text, ('AVAILABLE'::character varying)::text, ('ARCHIVED'::character varying)::text, ('ARCHIVE_PENDING'::character varying)::text, ('UNARCHIVE_PENDING'::character varying)::text, ('BACKUP_PENDING'::character varying)::text])));
CREATE DOMAIN authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY (ARRAY[('ADMIN'::character varying)::text, ('POWER_USER'::character varying)::text, ('USER'::character varying)::text, ('OBSERVER'::character varying)::text, ('ETL_SERVER'::character varying)::text])));
CREATE DOMAIN boolean_char AS boolean DEFAULT false;
CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));
CREATE DOMAIN code AS character varying(100);
CREATE DOMAIN column_label AS character varying(128);
CREATE DOMAIN data_set_kind AS character varying(40)
	CONSTRAINT data_set_kind_check CHECK (((VALUE)::text = ANY (ARRAY[('PHYSICAL'::character varying)::text, ('LINK'::character varying)::text, ('CONTAINER'::character varying)::text])));
CREATE DOMAIN data_store_service_kind AS character varying(40)
	CONSTRAINT data_store_service_kind_check CHECK (((VALUE)::text = ANY (ARRAY[('PROCESSING'::character varying)::text, ('QUERIES'::character varying)::text])));
CREATE DOMAIN data_store_service_reporting_plugin_type AS character varying(40)
	CONSTRAINT data_store_service_reporting_plugin_type_check CHECK (((VALUE)::text = ANY (ARRAY[('TABLE_MODEL'::character varying)::text, ('DSS_LINK'::character varying)::text, ('AGGREGATION_TABLE_MODEL'::character varying)::text])));
CREATE DOMAIN description_2000 AS character varying(2000);
CREATE DOMAIN edms_address_type AS text
	CONSTRAINT edms_address_type_check CHECK ((VALUE = ANY (ARRAY['OPENBIS'::text, 'URL'::text, 'FILE_SYSTEM'::text])));
CREATE DOMAIN entity_kind AS character varying(40)
	CONSTRAINT entity_kind_check CHECK (((VALUE)::text = ANY (ARRAY[('SAMPLE'::character varying)::text, ('EXPERIMENT'::character varying)::text, ('DATA_SET'::character varying)::text, ('MATERIAL'::character varying)::text])));
CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY (ARRAY[('DELETION'::character varying)::text, ('MOVEMENT'::character varying)::text, ('FREEZING'::character varying)::text])));
CREATE DOMAIN file AS bytea;
CREATE DOMAIN file_name AS character varying(255);
CREATE DOMAIN grid_expression AS character varying(2000);
CREATE DOMAIN grid_id AS character varying(200);
CREATE DOMAIN identifier AS character varying(200);
CREATE DOMAIN location_type AS text
	CONSTRAINT location_type_check CHECK ((VALUE = ANY (ARRAY['OPENBIS'::text, 'URL'::text, 'FILE_SYSTEM_PLAIN'::text, 'FILE_SYSTEM_GIT'::text])));
CREATE DOMAIN object_name AS character varying(50);
CREATE DOMAIN operation_execution_availability AS character varying(40)
	CONSTRAINT operation_execution_availability_check CHECK (((VALUE)::text = ANY (ARRAY[('AVAILABLE'::character varying)::text, ('DELETE_PENDING'::character varying)::text, ('DELETED'::character varying)::text, ('TIME_OUT_PENDING'::character varying)::text, ('TIMED_OUT'::character varying)::text])));
CREATE DOMAIN operation_execution_state AS character varying(40)
	CONSTRAINT operation_execution_state_check CHECK (((VALUE)::text = ANY (ARRAY[('NEW'::character varying)::text, ('SCHEDULED'::character varying)::text, ('RUNNING'::character varying)::text, ('FINISHED'::character varying)::text, ('FAILED'::character varying)::text])));
CREATE DOMAIN ordinal_int AS bigint
	CONSTRAINT ordinal_int_check CHECK ((VALUE > 0));
CREATE DOMAIN plugin_type AS character varying(40)
	CONSTRAINT plugin_type_check CHECK (((VALUE)::text = ANY (ARRAY[('JYTHON'::character varying)::text, ('PREDEPLOYED'::character varying)::text])));
CREATE DOMAIN query_type AS character varying(40)
	CONSTRAINT query_type_check CHECK (((VALUE)::text = ANY (ARRAY[('GENERIC'::character varying)::text, ('EXPERIMENT'::character varying)::text, ('SAMPLE'::character varying)::text, ('DATA_SET'::character varying)::text, ('MATERIAL'::character varying)::text])));
CREATE DOMAIN real_value AS real;
CREATE DOMAIN sample_identifier AS character varying(404);
CREATE DOMAIN script_type AS character varying(40)
	CONSTRAINT script_type_check CHECK (((VALUE)::text = ANY (ARRAY[('DYNAMIC_PROPERTY'::character varying)::text, ('MANAGED_PROPERTY'::character varying)::text, ('ENTITY_VALIDATION'::character varying)::text])));
CREATE DOMAIN tech_id AS bigint;
CREATE DOMAIN text_value AS text;
CREATE DOMAIN time_stamp AS timestamp with time zone;
CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();
CREATE DOMAIN title_100 AS character varying(100);
CREATE DOMAIN user_id AS character varying(256);
CREATE DOMAIN email_value AS character varying(320);
CREATE DOMAIN long_value AS bigint;
CREATE DOMAIN double_value AS double precision;
CREATE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  owner_code  CODE;
  owner_del_id  TECH_ID;
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
CREATE FUNCTION check_data_set_kind_link() RETURNS trigger
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
CREATE FUNCTION check_data_set_kind_physical() RETURNS trigger
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
CREATE FUNCTION content_copies_location_type_check() RETURNS trigger
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
CREATE FUNCTION content_copies_uniqueness_check() RETURNS trigger
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
CREATE FUNCTION data_all_tsvector_document_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.tsvector_document := setweight(('/' || escape_tsvector_string(NEW.code) || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$;
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
CREATE FUNCTION escape_tsvector_string(value character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN REPLACE(
            REPLACE(
                    REPLACE(
                            REPLACE(
                                    REPLACE(
                                            REPLACE(
                                                    REPLACE(
                                                            REPLACE(
                                                                    REPLACE(LOWER(value), '<', '\<'),
                                                                    '!', '\!'),
                                                            '*', '\*'),
                                                    '&', '\&'),
                                            '|', '\|'),
                                    ')', '\)'),
                            '(', '\('),
                    ':', '\:'),
            ' ', '\ ');
END
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
CREATE FUNCTION experiments_all_in_project_tsvector_document_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE new_space_code VARCHAR;
        tsv tsvector;
        exp RECORD;
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.space_id IS DISTINCT FROM OLD.space_id THEN
        SELECT code
        INTO new_space_code
        FROM spaces
        WHERE id = NEW.space_id;
        FOR exp IN
            SELECT id, code, perm_id
            FROM experiments_all
            WHERE proj_id = NEW.id
            LOOP
                tsv := setweight((escape_tsvector_string(exp.perm_id) || ':1')::tsvector, 'A') ||
                       setweight((escape_tsvector_string('/' || new_space_code || '/' || NEW.code || '/' || exp.code)
                           || ':1')::tsvector, 'A') ||
                       setweight((escape_tsvector_string(exp.code) || ':1')::tsvector, 'B');
                UPDATE experiments_all
                SET tsvector_document = tsv
                WHERE id = exp.id;
            END LOOP;
    END IF;
    RETURN NEW;
END
$$;
CREATE FUNCTION experiments_all_tsvector_document_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
BEGIN
    SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
            INNER JOIN spaces s ON p.space_id = s.id WHERE p.id = NEW.proj_id;
    NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string('/' || space_code || '/' || proj_code || '/' || NEW.code)
                    || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$;
CREATE FUNCTION external_data_storage_format_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN
   select code into v_covo_code from controlled_vocabularies
      where is_managed_internally = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data_all where id = NEW.id; 
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
CREATE FUNCTION materials_tsvector_document_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE material_type_code VARCHAR;
BEGIN
    SELECT code INTO STRICT material_type_code FROM material_types WHERE id = NEW.maty_id;
    NEW.tsvector_document := setweight((escape_tsvector_string(
            NEW.code || ' (' || material_type_code || ')') || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$;
CREATE FUNCTION melt_data_set_for() RETURNS trigger
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
CREATE FUNCTION melt_experiment_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_SAMP = 'f';
    NEW.FROZEN_FOR_DATA = 'f';
    return NEW;
end;
$$;
CREATE FUNCTION melt_project_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_EXP = 'f';
    NEW.FROZEN_FOR_SAMP = 'f';
    return NEW;
end;
$$;
CREATE FUNCTION melt_sample_for() RETURNS trigger
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
CREATE FUNCTION melt_space_for() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.FROZEN_FOR_PROJ = 'f';
    NEW.FROZEN_FOR_SAMP = 'f';
    return NEW;
end;
$$;
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
CREATE FUNCTION properties_tsvector_document_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE cvt RECORD;
BEGIN
    IF NEW.cvte_id IS NOT NULL THEN
        SELECT code, label INTO STRICT cvt FROM controlled_vocabulary_terms WHERE id = NEW.cvte_id;
        NEW.tsvector_document := text_to_ts_vector(cvt.code, 'C') || text_to_ts_vector(cvt.label, 'C');
    ELSE
        NEW.tsvector_document := text_to_ts_vector(NEW.value, 'D');
    END IF;
    RETURN NEW;
END $$;
CREATE FUNCTION raise_delete_from_data_set_exception() RETURNS trigger
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
CREATE FUNCTION raise_delete_from_experiment_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because experiment % is frozen.', TG_ARGV[0], 
        (select code from experiments_all where id = old.expe_id);
END;
$$;
CREATE FUNCTION raise_delete_from_project_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because project % is frozen.', TG_ARGV[0], 
        (select code from projects where id = old.proj_id);
END;
$$;
CREATE FUNCTION raise_delete_from_sample_exception() RETURNS trigger
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
CREATE FUNCTION raise_delete_from_space_exception() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because space % is frozen.', TG_ARGV[0], 
        (select code from spaces where id = old.space_id);
END;
$$;
CREATE FUNCTION raise_exception_frozen_data_set() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_data_set_relationship() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_data_set_sample_relationship() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_entity_by_code() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'Operation % is not allowed because % % is frozen.', TG_ARGV[0], TG_ARGV[1], OLD.code;
END;
$$;
CREATE FUNCTION raise_exception_frozen_experiment() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_experiment_relationship() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_project() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_project_relationship() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_sample() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_sample_container_relationship() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_sample_relationship() RETURNS trigger
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
CREATE FUNCTION raise_exception_frozen_space_relationship() RETURNS trigger
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
CREATE FUNCTION safe_double(s text) RETURNS double precision
    LANGUAGE plpgsql STRICT
    AS $$
BEGIN
    RETURN s::double precision;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
END; $$;
CREATE FUNCTION safe_timestamp(s text) RETURNS timestamp with time zone
    LANGUAGE plpgsql STRICT
    AS $$
BEGIN
    RETURN s::timestamp with time zone;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
END; $$;
CREATE FUNCTION sample_fill_code_unique_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.code_unique_check = NEW.code || ',' || coalesce(NEW.samp_id_part_of, -1) || ',' || coalesce(NEW.proj_id, -1) || ',' || coalesce(NEW.space_id, -1);
  RETURN NEW;
END;
$$;
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
CREATE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
        container_code VARCHAR;
        sample_code VARCHAR;
        identifier VARCHAR := '/';
BEGIN
    IF TG_OP != 'DELETE' THEN
        IF NEW.space_id IS NOT NULL THEN
            SELECT code INTO STRICT space_code FROM spaces WHERE id = NEW.space_id;
            identifier := identifier || space_code || '/';
        END IF;
        IF NEW.proj_id IS NOT NULL THEN
            SELECT code INTO STRICT proj_code FROM projects WHERE id = NEW.proj_id;
            identifier := identifier || proj_code || '/';
        END IF;
        IF NEW.samp_id_part_of IS NOT NULL THEN
            SELECT code INTO STRICT container_code FROM samples_all WHERE id = NEW.samp_id_part_of;
            sample_code := container_code || ':' || NEW.code;
            NEW.sample_identifier := identifier || sample_code;
            NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
                                     setweight((escape_tsvector_string(NEW.sample_identifier) || ':1')::tsvector,
                                         'A') ||
                                     setweight((escape_tsvector_string(sample_code) || ':1')::tsvector, 'B') ||
                                     setweight((escape_tsvector_string(container_code) || ':1')::tsvector, 'B') ||
                                     setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
        ELSE
            NEW.sample_identifier := identifier || NEW.code;
            NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
                                     setweight((escape_tsvector_string(NEW.sample_identifier) || ':1')::tsvector,
                                         'A') ||
                                     setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
        END IF;
    END IF;
    RETURN NEW;
END
$$;
CREATE FUNCTION text_to_ts_vector(text_to_index text, weight "char") RETURNS tsvector
    LANGUAGE plpgsql
    AS $$
DECLARE indexed BOOLEAN;
    DECLARE result tsvector;
BEGIN
    indexed := FALSE;
    text_to_index := regexp_replace(coalesce(text_to_index, ''), E'<[^>]+>', '', 'gi'); -- Remove XML Tags
    text_to_index := escape_tsvector_string(text_to_index); -- Escape characters used by ts_vector
    WHILE NOT INDEXED LOOP
            BEGIN
                result = setweight(to_tsvector('english', text_to_index), weight)::TEXT;
                indexed := TRUE;
            EXCEPTION WHEN sqlstate '54000' THEN
                text_to_index := left(text_to_index, LENGTH(text_to_index) / 2); -- If the index is too big reduce the size of the text to half
            END;
        END LOOP;
    RETURN result;
END $$;
CREATE SEQUENCE attachment_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
SET default_tablespace = '';
CREATE TABLE attachment_contents (
    id tech_id NOT NULL,
    value file NOT NULL
);
CREATE SEQUENCE attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
    proj_frozen boolean_char DEFAULT false NOT NULL,
    expe_frozen boolean_char DEFAULT false NOT NULL,
    samp_frozen boolean_char DEFAULT false NOT NULL,
    CONSTRAINT atta_arc_ck CHECK ((((expe_id IS NOT NULL) AND (proj_id IS NULL) AND (samp_id IS NULL)) OR ((expe_id IS NULL) AND (proj_id IS NOT NULL) AND (samp_id IS NULL)) OR ((expe_id IS NULL) AND (proj_id IS NULL) AND (samp_id IS NOT NULL))))
);
CREATE SEQUENCE authorization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE authorization_group_persons (
    ag_id tech_id NOT NULL,
    pers_id tech_id NOT NULL
);
CREATE TABLE authorization_groups (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);
CREATE SEQUENCE code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE content_copies (
    data_id tech_id NOT NULL,
    edms_id tech_id NOT NULL,
    external_code text_value,
    id tech_id NOT NULL,
    location_type location_type NOT NULL,
    path text_value,
    git_commit_hash text_value,
    git_repository_id text_value,
    location_unique_check text_value NOT NULL,
    pers_id_registerer tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    data_frozen boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE content_copies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_chosen_from_list boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);
CREATE SEQUENCE controlled_vocabulary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
CREATE SEQUENCE core_plugin_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE core_plugins (
    id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    version integer NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    master_reg_script text_value
);
CREATE SEQUENCE cvte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_all (
    id tech_id NOT NULL,
    code code,
    dsty_id tech_id NOT NULL,
    data_producer_code code,
    production_timestamp time_stamp,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_valid boolean_char DEFAULT true,
    modification_timestamp time_stamp DEFAULT now(),
    expe_id tech_id,
    dast_id tech_id NOT NULL,
    is_derived boolean_char NOT NULL,
    samp_id tech_id,
    pers_id_registerer tech_id,
    del_id tech_id,
    pers_id_modifier tech_id,
    version integer DEFAULT 0,
    orig_del tech_id,
    access_timestamp time_stamp DEFAULT now(),
    data_set_kind data_set_kind DEFAULT 'PHYSICAL'::character varying NOT NULL,
    frozen boolean_char DEFAULT false NOT NULL,
    expe_frozen boolean_char DEFAULT false NOT NULL,
    samp_frozen boolean_char DEFAULT false NOT NULL,
    frozen_for_children boolean_char DEFAULT false NOT NULL,
    frozen_for_parents boolean_char DEFAULT false NOT NULL,
    frozen_for_comps boolean_char DEFAULT false NOT NULL,
    frozen_for_conts boolean_char DEFAULT false NOT NULL,
    tsvector_document tsvector NOT NULL,
    meta_data jsonb,
    CONSTRAINT data_ck CHECK (((expe_id IS NOT NULL) OR (samp_id IS NOT NULL)))
);
CREATE VIEW data AS
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
    data_all.frozen_for_conts,
    data_all.tsvector_document,
    data_all.meta_data
   FROM data_all
  WHERE (data_all.del_id IS NULL);
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
    data_all.version,
    data_all.data_set_kind
   FROM data_all
  WHERE (data_all.del_id IS NOT NULL);
CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_set_copies_history (
    id tech_id NOT NULL,
    cc_id tech_id NOT NULL,
    data_id tech_id NOT NULL,
    external_code text_value,
    path text_value,
    git_commit_hash text_value,
    git_repository_id text_value,
    edms_id tech_id NOT NULL,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp,
    edms_code code,
    edms_label text_value,
    edms_address text_value
);
CREATE SEQUENCE data_set_copies_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_set_properties_history (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value text_value,
    valid_until_timestamp time_stamp DEFAULT now(),
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    vocabulary_term identifier,
    material identifier,
    sample identifier,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT dsprh_ck CHECK ((
        ((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
CREATE TABLE data_set_relationships_history (
    id tech_id NOT NULL,
    main_data_id tech_id NOT NULL,
    relation_type text_value,
    expe_id tech_id,
    samp_id tech_id,
    data_id tech_id,
    entity_perm_id text_value,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp,
    ordinal integer,
    entity_kind text_value
);
CREATE VIEW data_set_history_view AS
 SELECT (3 * (data_set_relationships_history.id)::bigint) AS id,
    data_set_relationships_history.main_data_id,
    data_set_relationships_history.relation_type,
    data_set_relationships_history.ordinal,
    data_set_relationships_history.expe_id,
    data_set_relationships_history.samp_id,
    data_set_relationships_history.data_id,
    data_set_relationships_history.entity_kind,
    data_set_relationships_history.entity_perm_id,
    NULL::bigint AS dstpt_id,
    NULL::text AS value,
    NULL::character varying AS vocabulary_term,
    NULL::character varying AS material,
    NULL::character varying AS sample,
    NULL::text AS external_code,
    NULL::text AS path,
    NULL::text AS git_commit_hash,
    NULL::text AS git_repository_id,
    (NULL::bigint)::tech_id AS edms_id,
    NULL::text AS edms_code,
    NULL::text AS edms_label,
    NULL::text AS edms_address,
    data_set_relationships_history.pers_id_author,
    data_set_relationships_history.valid_from_timestamp,
    data_set_relationships_history.valid_until_timestamp,
    NULL::long_value[] AS integer_array_value,
    NULL::double_value[] AS real_array_value,
    NULL::text_value[] AS string_array_value,
    NULL::time_stamp[] AS timestamp_array_value,
    NULL::jsonb AS json_value
   FROM data_set_relationships_history
  WHERE (data_set_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
 SELECT ((3 * (data_set_properties_history.id)::bigint) + 1) AS id,
    data_set_properties_history.ds_id AS main_data_id,
    NULL::text AS relation_type,
    NULL::integer AS ordinal,
    NULL::bigint AS expe_id,
    NULL::bigint AS samp_id,
    NULL::bigint AS data_id,
    NULL::text AS entity_kind,
    NULL::text AS entity_perm_id,
    data_set_properties_history.dstpt_id,
    data_set_properties_history.value,
    data_set_properties_history.vocabulary_term,
    data_set_properties_history.material,
    data_set_properties_history.sample,
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
    data_set_properties_history.valid_until_timestamp,
    data_set_properties_history.integer_array_value,
    data_set_properties_history.real_array_value,
    data_set_properties_history.string_array_value,
    data_set_properties_history.timestamp_array_value,
    data_set_properties_history.json_value
   FROM data_set_properties_history
UNION
 SELECT ((3 * (data_set_copies_history.id)::bigint) + 2) AS id,
    data_set_copies_history.data_id AS main_data_id,
    NULL::text AS relation_type,
    NULL::integer AS ordinal,
    NULL::bigint AS expe_id,
    NULL::bigint AS samp_id,
    NULL::bigint AS data_id,
    NULL::text AS entity_kind,
    NULL::text AS entity_perm_id,
    NULL::bigint AS dstpt_id,
    NULL::text AS value,
    NULL::character varying AS vocabulary_term,
    NULL::character varying AS material,
    NULL::character varying AS sample,
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
    data_set_copies_history.valid_until_timestamp,
    NULL::long_value[] AS integer_array_value,
    NULL::double_value[] AS real_array_value,
    NULL::text_value[] AS string_array_value,
    NULL::time_stamp[] AS timestamp_array_value,
    NULL::jsonb AS json_value
   FROM data_set_copies_history
  WHERE (data_set_copies_history.valid_until_timestamp IS NOT NULL);
CREATE TABLE data_set_properties (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_author tech_id NOT NULL,
    dase_frozen boolean_char DEFAULT false NOT NULL,
    samp_prop_id tech_id,
    tsvector_document tsvector NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT dspr_ck CHECK ((
        ((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
CREATE SEQUENCE data_set_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_set_relationships_all (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL,
    del_id tech_id,
    pers_id_author tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    relationship_id tech_id NOT NULL,
    ordinal integer,
    parent_frozen boolean_char DEFAULT false NOT NULL,
    child_frozen boolean_char DEFAULT false NOT NULL,
    comp_frozen boolean_char DEFAULT false NOT NULL,
    cont_frozen boolean_char DEFAULT false NOT NULL
);
CREATE VIEW data_set_relationships AS
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
   FROM data_set_relationships_all
  WHERE (data_set_relationships_all.del_id IS NULL);
CREATE SEQUENCE data_set_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE data_set_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_set_type_property_types (
    id tech_id NOT NULL,
    dsty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL
);
CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    modification_timestamp time_stamp DEFAULT now(),
    main_ds_pattern character varying(300),
    main_ds_path character varying(1000),
    deletion_disallow boolean_char DEFAULT false,
    validation_script_id tech_id,
    meta_data jsonb
);
CREATE SEQUENCE data_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_store_service_data_set_types (
    data_store_service_id tech_id NOT NULL,
    data_set_type_id tech_id NOT NULL
);
CREATE TABLE data_store_services (
    id tech_id NOT NULL,
    key character varying(256) NOT NULL,
    label character varying(256) NOT NULL,
    kind data_store_service_kind NOT NULL,
    data_store_id tech_id NOT NULL,
    reporting_plugin_type data_store_service_reporting_plugin_type
);
CREATE SEQUENCE data_store_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_stores (
    id tech_id NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_archiver_configured boolean_char DEFAULT false NOT NULL,
    data_source_definitions text_value,
    uuid code NOT NULL
);
CREATE SEQUENCE data_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000 NOT NULL
);
CREATE SEQUENCE database_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);
CREATE SEQUENCE deletion_id_seq
    START WITH 5
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE deletions (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_2000 NOT NULL
);
CREATE SEQUENCE dstpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE entity_operations_log (
    id tech_id NOT NULL,
    registration_id tech_id NOT NULL
);
CREATE SEQUENCE entity_operations_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE etpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY (ARRAY[('ATTACHMENT'::character varying)::text, ('DATASET'::character varying)::text, ('EXPERIMENT'::character varying)::text, ('SPACE'::character varying)::text, ('MATERIAL'::character varying)::text, ('PROJECT'::character varying)::text, ('PROPERTY_TYPE'::character varying)::text, ('SAMPLE'::character varying)::text, ('VOCABULARY'::character varying)::text, ('AUTHORIZATION_GROUP'::character varying)::text, ('METAPROJECT'::character varying)::text])))
);
CREATE TABLE events_search (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    entity_type text_value NOT NULL,
    entity_space text_value,
    entity_space_perm_id text_value,
    entity_project text_value,
    entity_project_perm_id text_value,
    entity_registerer text_value,
    entity_registration_timestamp time_stamp,
    identifier text_value NOT NULL,
    description text_value,
    reason text_value,
    content text_value,
    exac_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp NOT NULL,
    CONSTRAINT events_search_entity_type_ck CHECK (((entity_type)::text = ANY (ARRAY['ATTACHMENT'::text, 'DATASET'::text, 'EXPERIMENT'::text, 'SPACE'::text, 'MATERIAL'::text, 'PROJECT'::text, 'PROPERTY_TYPE'::text, 'SAMPLE'::text, 'VOCABULARY'::text, 'AUTHORIZATION_GROUP'::text, 'METAPROJECT'::text])))
);
CREATE SEQUENCE events_search_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE experiment_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE experiment_properties_history (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value text_value,
    valid_until_timestamp time_stamp DEFAULT now(),
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    vocabulary_term identifier,
    material identifier,
    sample identifier,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT exprh_ck CHECK ((
        ((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
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
    proj_id tech_id,
    entity_kind text_value
);
CREATE VIEW experiment_history_view AS
 SELECT (2 * (experiment_relationships_history.id)::bigint) AS id,
    experiment_relationships_history.main_expe_id,
    experiment_relationships_history.relation_type,
    experiment_relationships_history.proj_id,
    experiment_relationships_history.samp_id,
    experiment_relationships_history.data_id,
    experiment_relationships_history.entity_kind,
    experiment_relationships_history.entity_perm_id,
    NULL::bigint AS etpt_id,
    NULL::text AS value,
    NULL::character varying AS vocabulary_term,
    NULL::character varying AS material,
    NULL::character varying AS sample,
    experiment_relationships_history.pers_id_author,
    experiment_relationships_history.valid_from_timestamp,
    experiment_relationships_history.valid_until_timestamp,
    NULL::long_value[] AS integer_array_value,
    NULL::double_value[] AS real_array_value,
    NULL::text_value[] AS string_array_value,
    NULL::time_stamp[] AS timestamp_array_value,
    NULL::jsonb AS json_value
   FROM experiment_relationships_history
  WHERE (experiment_relationships_history.valid_until_timestamp IS NOT NULL)
UNION
 SELECT ((2 * (experiment_properties_history.id)::bigint) + 1) AS id,
    experiment_properties_history.expe_id AS main_expe_id,
    NULL::text AS relation_type,
    NULL::bigint AS proj_id,
    NULL::bigint AS samp_id,
    NULL::bigint AS data_id,
    NULL::text AS entity_kind,
    NULL::text AS entity_perm_id,
    experiment_properties_history.etpt_id,
    experiment_properties_history.value,
    experiment_properties_history.vocabulary_term,
    experiment_properties_history.material,
    experiment_properties_history.sample,
    experiment_properties_history.pers_id_author,
    experiment_properties_history.valid_from_timestamp,
    experiment_properties_history.valid_until_timestamp,
    experiment_properties_history.integer_array_value,
    experiment_properties_history.real_array_value,
    experiment_properties_history.string_array_value,
    experiment_properties_history.timestamp_array_value,
    experiment_properties_history.json_value
   FROM experiment_properties_history;
CREATE SEQUENCE experiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    mate_prop_id tech_id,
    pers_id_author tech_id NOT NULL,
    expe_frozen boolean_char DEFAULT false NOT NULL,
    samp_prop_id tech_id,
    tsvector_document tsvector NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT expr_ck CHECK ((
        ((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
CREATE SEQUENCE experiment_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE experiment_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE experiment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE experiment_type_property_types (
    id tech_id NOT NULL,
    exty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL
);
CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    modification_timestamp time_stamp DEFAULT now(),
    validation_script_id tech_id,
    meta_data jsonb
);
CREATE TABLE experiments_all (
    id tech_id NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    proj_id tech_id NOT NULL,
    del_id tech_id,
    is_public boolean_char DEFAULT false NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    perm_id code NOT NULL,
    pers_id_modifier tech_id,
    version integer DEFAULT 0,
    orig_del tech_id,
    frozen boolean_char DEFAULT false NOT NULL,
    proj_frozen boolean_char DEFAULT false NOT NULL,
    frozen_for_samp boolean_char DEFAULT false NOT NULL,
    frozen_for_data boolean_char DEFAULT false NOT NULL,
    tsvector_document tsvector NOT NULL,
    meta_data jsonb
);
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
    experiments_all.proj_frozen,
    experiments_all.del_id,
    experiments_all.orig_del,
    experiments_all.is_public,
    experiments_all.version,
    experiments_all.frozen,
    experiments_all.frozen_for_samp,
    experiments_all.frozen_for_data,
    experiments_all.tsvector_document,
    experiments_all.meta_data
   FROM experiments_all
  WHERE (experiments_all.del_id IS NULL);
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
CREATE TABLE external_data (
    id tech_id NOT NULL,
    location character varying(1024) NOT NULL,
    ffty_id tech_id NOT NULL,
    loty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store tech_id,
    status archiving_status DEFAULT 'AVAILABLE'::character varying NOT NULL,
    share_id code,
    size ordinal_int,
    present_in_archive boolean_char DEFAULT false,
    speed_hint integer DEFAULT '-50'::integer NOT NULL,
    storage_confirmation boolean_char DEFAULT false NOT NULL,
    h5_folders boolean_char NOT NULL,
    h5ar_folders boolean_char NOT NULL,
    archiving_requested boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE external_data_management_system_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE external_data_management_systems (
    id tech_id NOT NULL,
    code code NOT NULL,
    label text_value,
    address text_value NOT NULL,
    address_type edms_address_type NOT NULL
);
CREATE SEQUENCE file_format_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000
);
CREATE SEQUENCE filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
CREATE SEQUENCE grid_custom_columns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE link_data (
    id tech_id NOT NULL,
    data_frozen boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE locator_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000
);
CREATE SEQUENCE material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value text_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    modification_timestamp time_stamp DEFAULT now(),
    mate_prop_id tech_id,
    pers_id_author tech_id NOT NULL,
    tsvector_document tsvector NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT mapr_ck CHECK ((
            ((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
CREATE TABLE material_properties_history (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value text_value,
    valid_until_timestamp time_stamp DEFAULT now(),
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    vocabulary_term identifier,
    material identifier,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT maprh_ck CHECK ((
            ((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
                OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
CREATE SEQUENCE material_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE material_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE material_type_property_types (
    id tech_id NOT NULL,
    maty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    section description_2000,
    ordinal ordinal_int NOT NULL,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL
);
CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    modification_timestamp time_stamp DEFAULT now(),
    validation_script_id tech_id
);
CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    tsvector_document tsvector NOT NULL
);
CREATE SEQUENCE metaproject_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE metaproject_assignments_all (
    id tech_id NOT NULL,
    mepr_id tech_id NOT NULL,
    expe_id tech_id,
    samp_id tech_id,
    data_id tech_id,
    mate_id tech_id,
    del_id tech_id,
    creation_date time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT metaproject_assignments_all_check_nn CHECK ((((expe_id IS NOT NULL) AND (samp_id IS NULL) AND (data_id IS NULL) AND (mate_id IS NULL)) OR ((expe_id IS NULL) AND (samp_id IS NOT NULL) AND (data_id IS NULL) AND (mate_id IS NULL)) OR ((expe_id IS NULL) AND (samp_id IS NULL) AND (data_id IS NOT NULL) AND (mate_id IS NULL)) OR ((expe_id IS NULL) AND (samp_id IS NULL) AND (data_id IS NULL) AND (mate_id IS NOT NULL))))
);
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
CREATE SEQUENCE metaproject_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE metaprojects (
    id tech_id NOT NULL,
    name code NOT NULL,
    description description_2000,
    owner tech_id NOT NULL,
    private boolean_char DEFAULT true NOT NULL,
    creation_date time_stamp_dfl DEFAULT now() NOT NULL
);
CREATE SEQUENCE mtpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE operation_executions (
    id tech_id NOT NULL,
    code code NOT NULL,
    state operation_execution_state DEFAULT 'NEW'::character varying NOT NULL,
    owner tech_id NOT NULL,
    description text_value,
    notification text_value,
    availability operation_execution_availability DEFAULT 'AVAILABLE'::character varying NOT NULL,
    availability_time bigint DEFAULT 1 NOT NULL,
    summary_operations text_value,
    summary_progress text_value,
    summary_error text_value,
    summary_results text_value,
    summary_availability operation_execution_availability DEFAULT 'AVAILABLE'::character varying NOT NULL,
    summary_availability_time bigint DEFAULT 1 NOT NULL,
    details_path character varying(1000),
    details_availability operation_execution_availability DEFAULT 'AVAILABLE'::character varying NOT NULL,
    details_availability_time bigint DEFAULT 1 NOT NULL,
    creation_date time_stamp_dfl NOT NULL,
    start_date time_stamp,
    finish_date time_stamp,
    CONSTRAINT operation_executions_state_finish_date_check CHECK (((((state)::text = ANY (ARRAY[('NEW'::character varying)::text, ('SCHEDULED'::character varying)::text, ('RUNNING'::character varying)::text])) AND (finish_date IS NULL)) OR (((state)::text = ANY (ARRAY[('FINISHED'::character varying)::text, ('FAILED'::character varying)::text])) AND (finish_date IS NOT NULL)))),
    CONSTRAINT operation_executions_state_start_date_check CHECK (((((state)::text = ANY (ARRAY[('NEW'::character varying)::text, ('SCHEDULED'::character varying)::text])) AND (start_date IS NULL)) OR (((state)::text = ANY (ARRAY[('RUNNING'::character varying)::text, ('FINISHED'::character varying)::text, ('FAILED'::character varying)::text])) AND (start_date IS NOT NULL))))
);
CREATE SEQUENCE operation_executions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE perm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name text_value,
    last_name text_value,
    user_id user_id NOT NULL,
    email email_value,
    space_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id,
    display_settings file,
    is_active boolean DEFAULT true
);
CREATE TABLE post_registration_dataset_queue (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL
);
CREATE SEQUENCE post_registration_dataset_queue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE project_relationships_history (
    id tech_id NOT NULL,
    main_proj_id tech_id NOT NULL,
    relation_type text_value,
    expe_id tech_id,
    space_id tech_id,
    entity_perm_id text_value,
    pers_id_author tech_id,
    valid_from_timestamp time_stamp NOT NULL,
    valid_until_timestamp time_stamp,
    entity_kind text_value,
    samp_id tech_id
);
CREATE SEQUENCE project_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
    version integer DEFAULT 0,
    frozen boolean_char DEFAULT false NOT NULL,
    frozen_for_exp boolean_char DEFAULT false NOT NULL,
    frozen_for_samp boolean_char DEFAULT false NOT NULL,
    space_frozen boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE property_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
    maty_prop_id tech_id,
    schema text_value,
    transformation text_value,
    meta_data jsonb,
    saty_prop_id tech_id,
    is_multi_value boolean_char DEFAULT false NOT NULL
);
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
    db_key code DEFAULT '1'::character varying NOT NULL,
    entity_type_code code
);
CREATE SEQUENCE query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE relationship_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE relationship_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    label column_label,
    parent_label column_label,
    child_label column_label,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE role_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE role_assignments (
    id tech_id NOT NULL,
    role_code authorization_role NOT NULL,
    space_id tech_id,
    pers_id_grantee tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    ag_id_grantee tech_id,
    project_id tech_id,
    CONSTRAINT roas_ag_pers_arc_ck CHECK ((((ag_id_grantee IS NOT NULL) AND (pers_id_grantee IS NULL)) OR ((ag_id_grantee IS NULL) AND (pers_id_grantee IS NOT NULL)))),
    CONSTRAINT roas_space_project_ck CHECK (((space_id IS NULL) OR (project_id IS NULL)))
);
CREATE SEQUENCE sample_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE sample_properties_history (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value text_value,
    valid_until_timestamp time_stamp DEFAULT now(),
    pers_id_author tech_id NOT NULL,
    valid_from_timestamp time_stamp NOT NULL,
    vocabulary_term identifier,
    material identifier,
    sample identifier,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT saprh_ck CHECK ((
        ((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
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
    space_id tech_id,
    proj_id tech_id,
    annotations jsonb,
    entity_kind text_value
);
CREATE VIEW sample_history_view AS
 SELECT (2 * (sample_relationships_history.id)::bigint) AS id,
    sample_relationships_history.main_samp_id,
    sample_relationships_history.relation_type,
    sample_relationships_history.space_id,
    sample_relationships_history.expe_id,
    sample_relationships_history.samp_id,
    sample_relationships_history.proj_id,
    sample_relationships_history.data_id,
    sample_relationships_history.entity_kind,
    sample_relationships_history.entity_perm_id,
    sample_relationships_history.annotations,
    NULL::bigint AS stpt_id,
    NULL::text AS value,
    NULL::character varying AS vocabulary_term,
    NULL::character varying AS material,
    NULL::character varying AS sample,
    sample_relationships_history.pers_id_author,
    sample_relationships_history.valid_from_timestamp,
    sample_relationships_history.valid_until_timestamp,
    NULL::long_value[] AS integer_array_value,
    NULL::double_value[] AS real_array_value,
    NULL::text_value[] AS string_array_value,
    NULL::time_stamp[] AS timestamp_array_value,
    NULL::jsonb AS json_value
   FROM sample_relationships_history
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
    NULL::text AS entity_kind,
    NULL::text AS entity_perm_id,
    NULL::jsonb AS annotations,
    sample_properties_history.stpt_id,
    sample_properties_history.value,
    sample_properties_history.vocabulary_term,
    sample_properties_history.material,
    sample_properties_history.sample,
    sample_properties_history.pers_id_author,
    sample_properties_history.valid_from_timestamp,
    sample_properties_history.valid_until_timestamp,
    sample_properties_history.integer_array_value,
    sample_properties_history.real_array_value,
    sample_properties_history.string_array_value,
    sample_properties_history.timestamp_array_value,
    sample_properties_history.json_value
   FROM sample_properties_history;
CREATE SEQUENCE sample_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    mate_prop_id tech_id,
    pers_id_author tech_id NOT NULL,
    samp_frozen boolean_char DEFAULT false NOT NULL,
    samp_prop_id tech_id,
    tsvector_document tsvector NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL,
    integer_array_value long_value[],
    real_array_value double_value[],
    string_array_value text_value[],
    timestamp_array_value time_stamp[],
    json_value jsonb,
    CONSTRAINT sapr_ck CHECK ((
        ((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NOT NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NOT NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NOT NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NOT NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NOT NULL) AND (json_value IS NULL))
            OR ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL) AND (integer_array_value IS NULL) AND (real_array_value IS NULL) AND (string_array_value IS NULL) AND (timestamp_array_value IS NULL) AND (json_value IS NOT NULL))
        ))
);
CREATE SEQUENCE sample_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE sample_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE sample_relationships_all (
    id tech_id NOT NULL,
    sample_id_parent tech_id NOT NULL,
    relationship_id tech_id NOT NULL,
    sample_id_child tech_id NOT NULL,
    del_id tech_id,
    pers_id_author tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    parent_frozen boolean_char DEFAULT false NOT NULL,
    child_frozen boolean_char DEFAULT false NOT NULL,
    child_annotations jsonb,
    parent_annotations jsonb
);
CREATE VIEW sample_relationships AS
 SELECT sample_relationships_all.id,
    sample_relationships_all.sample_id_parent,
    sample_relationships_all.parent_frozen,
    sample_relationships_all.relationship_id,
    sample_relationships_all.sample_id_child,
    sample_relationships_all.child_frozen,
    sample_relationships_all.del_id,
    sample_relationships_all.pers_id_author,
    sample_relationships_all.registration_timestamp,
    sample_relationships_all.modification_timestamp,
    sample_relationships_all.child_annotations,
    sample_relationships_all.parent_annotations
   FROM sample_relationships_all
  WHERE (sample_relationships_all.del_id IS NULL);
CREATE SEQUENCE sample_relationships_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE SEQUENCE sample_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
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
    ordinal ordinal_int NOT NULL,
    script_id tech_id,
    is_shown_edit boolean_char DEFAULT true NOT NULL,
    show_raw_value boolean_char DEFAULT false NOT NULL,
    is_unique boolean_char DEFAULT false NOT NULL
);
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
    show_parent_metadata boolean_char DEFAULT false NOT NULL,
    meta_data jsonb
);
CREATE TABLE samples_all (
    id tech_id NOT NULL,
    code code NOT NULL,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    del_id tech_id,
    space_id tech_id,
    samp_id_part_of tech_id,
    modification_timestamp time_stamp DEFAULT now(),
    expe_id tech_id,
    perm_id code NOT NULL,
    pers_id_modifier tech_id,
    code_unique_check character varying(300),
    subcode_unique_check character varying(300),
    version integer DEFAULT 0,
    orig_del tech_id,
    proj_id tech_id,
    frozen boolean_char DEFAULT false NOT NULL,
    space_frozen boolean_char DEFAULT false NOT NULL,
    proj_frozen boolean_char DEFAULT false NOT NULL,
    expe_frozen boolean_char DEFAULT false NOT NULL,
    cont_frozen boolean_char DEFAULT false NOT NULL,
    frozen_for_comp boolean_char DEFAULT false NOT NULL,
    frozen_for_children boolean_char DEFAULT false NOT NULL,
    frozen_for_parents boolean_char DEFAULT false NOT NULL,
    frozen_for_data boolean_char DEFAULT false NOT NULL,
    sample_identifier sample_identifier,
    tsvector_document tsvector NOT NULL,
    meta_data jsonb
);
CREATE VIEW samples AS
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
    samples_all.frozen_for_data,
    samples_all.tsvector_document,
    samples_all.sample_identifier,
    samples_all.meta_data
   FROM samples_all
  WHERE (samples_all.del_id IS NULL);
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
    samples_all.proj_id,
    samples_all.samp_id_part_of,
    samples_all.version
   FROM samples_all
  WHERE (samples_all.del_id IS NOT NULL);
CREATE SEQUENCE script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE scripts (
    id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    script text_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    entity_kind entity_kind,
    script_type script_type NOT NULL,
    plugin_type plugin_type DEFAULT 'JYTHON'::character varying NOT NULL,
    is_available boolean_char DEFAULT true NOT NULL,
    CONSTRAINT script_nn_ck CHECK ((((plugin_type)::text = 'PREDEPLOYED'::text) OR (script IS NOT NULL)))
);
CREATE SEQUENCE semantic_annotation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE semantic_annotations (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    saty_id tech_id,
    stpt_id tech_id,
    prty_id tech_id,
    predicate_ontology_id text,
    predicate_ontology_version text,
    predicate_accession_id text,
    descriptor_ontology_id text,
    descriptor_ontology_version text,
    descriptor_accession_id text,
    creation_date time_stamp_dfl NOT NULL,
    CONSTRAINT semantic_annotations_ssp_ck CHECK ((((saty_id IS NOT NULL) AND (stpt_id IS NULL) AND (prty_id IS NULL)) OR ((saty_id IS NULL) AND (stpt_id IS NOT NULL) AND (prty_id IS NULL)) OR ((saty_id IS NULL) AND (stpt_id IS NULL) AND (prty_id IS NOT NULL))))
);
CREATE SEQUENCE space_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
CREATE TABLE spaces (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    frozen boolean_char DEFAULT false NOT NULL,
    frozen_for_proj boolean_char DEFAULT false NOT NULL,
    frozen_for_samp boolean_char DEFAULT false NOT NULL
);
CREATE SEQUENCE stpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
SELECT pg_catalog.setval('attachment_content_id_seq', 12, true);
SELECT pg_catalog.setval('attachment_id_seq', 12, true);
SELECT pg_catalog.setval('authorization_group_id_seq', 1, true);
SELECT pg_catalog.setval('code_seq', 8, true);
SELECT pg_catalog.setval('content_copies_id_seq', 4, true);
SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 6, true);
SELECT pg_catalog.setval('core_plugin_id_seq', 1, false);
SELECT pg_catalog.setval('cvte_id_seq', 17, true);
SELECT pg_catalog.setval('data_id_seq', 41, true);
SELECT pg_catalog.setval('data_set_copies_history_id_seq', 1, false);
SELECT pg_catalog.setval('data_set_property_id_seq', 29, true);
SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);
SELECT pg_catalog.setval('data_set_relationships_history_id_seq', 16, true);
SELECT pg_catalog.setval('data_set_type_id_seq', 11, true);
SELECT pg_catalog.setval('data_store_id_seq', 1, true);
SELECT pg_catalog.setval('data_store_services_id_seq', 1, true);
SELECT pg_catalog.setval('data_type_id_seq', 12, true);
SELECT pg_catalog.setval('database_instance_id_seq', 1, true);
SELECT pg_catalog.setval('deletion_id_seq', 4, true);
SELECT pg_catalog.setval('dstpt_id_seq', 10, true);
SELECT pg_catalog.setval('entity_operations_log_id_seq', 10, false);
SELECT pg_catalog.setval('etpt_id_seq', 11, true);
SELECT pg_catalog.setval('event_id_seq', 1, false);
SELECT pg_catalog.setval('events_search_id_seq', 1, false);
SELECT pg_catalog.setval('experiment_code_seq', 7, true);
SELECT pg_catalog.setval('experiment_id_seq', 25, true);
SELECT pg_catalog.setval('experiment_property_id_seq', 23, true);
SELECT pg_catalog.setval('experiment_relationships_history_id_seq', 1, false);
SELECT pg_catalog.setval('experiment_type_id_seq', 3, true);
SELECT pg_catalog.setval('external_data_management_system_id_seq', 3, true);
SELECT pg_catalog.setval('file_format_type_id_seq', 8, true);
SELECT pg_catalog.setval('filter_id_seq', 1, true);
SELECT pg_catalog.setval('grid_custom_columns_id_seq', 1, false);
SELECT pg_catalog.setval('locator_type_id_seq', 1, true);
SELECT pg_catalog.setval('material_id_seq', 3736, true);
SELECT pg_catalog.setval('material_property_id_seq', 9324, true);
SELECT pg_catalog.setval('material_type_id_seq', 11, true);
SELECT pg_catalog.setval('metaproject_assignment_id_seq', 18, true);
SELECT pg_catalog.setval('metaproject_id_seq', 5, true);
SELECT pg_catalog.setval('mtpt_id_seq', 31, true);
SELECT pg_catalog.setval('operation_executions_id_seq', 1, false);
SELECT pg_catalog.setval('perm_id_seq', 1035, true);
SELECT pg_catalog.setval('person_id_seq', 21, true);
SELECT pg_catalog.setval('post_registration_dataset_queue_id_seq', 1, true);
SELECT pg_catalog.setval('project_id_seq', 7, true);
SELECT pg_catalog.setval('project_relationships_history_id_seq', 1, false);
SELECT pg_catalog.setval('property_type_id_seq', 28, true);
SELECT pg_catalog.setval('query_id_seq', 1, false);
SELECT pg_catalog.setval('relationship_type_id_seq', 3, true);
SELECT pg_catalog.setval('role_assignment_id_seq', 29, true);
SELECT pg_catalog.setval('sample_code_seq', 8, true);
SELECT pg_catalog.setval('sample_id_seq', 1061, true);
SELECT pg_catalog.setval('sample_property_id_seq', 55, true);
SELECT pg_catalog.setval('sample_relationship_id_seq', 48, true);
SELECT pg_catalog.setval('sample_relationships_history_id_seq', 1, false);
SELECT pg_catalog.setval('sample_type_id_seq', 12, true);
SELECT pg_catalog.setval('script_id_seq', 15, true);
SELECT pg_catalog.setval('semantic_annotation_id_seq', 8, true);
SELECT pg_catalog.setval('space_id_seq', 3, true);
SELECT pg_catalog.setval('stpt_id_seq', 21, true);

