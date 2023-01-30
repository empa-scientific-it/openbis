SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET search_path = public, pg_catalog;
CREATE DOMAIN boolean_char AS boolean DEFAULT false;
CREATE DOMAIN code AS character varying(40);
CREATE DOMAIN description_80 AS character varying(80);
CREATE DOMAIN tech_id AS bigint;
CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);
CREATE DOMAIN object_name AS character varying(50);
CREATE TABLE controlled_vocabulary_terms (
    id tech_id NOT NULL,
    code object_name NOT NULL,
    registration_timestamp time_stamp_dfl,
    covo_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE DOMAIN time_stamp AS timestamp with time zone;
CREATE TABLE data (
    id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    obty_id tech_id NOT NULL,
    proc_id_produced_by tech_id NOT NULL,
    samp_id_acquired_from tech_id,
    samp_id_derived_from tech_id,
    is_placeholder boolean_char NOT NULL,
    code code NOT NULL,
    data_producer_code code,
    production_timestamp time_stamp,
    CONSTRAINT data_samp_arc_ck CHECK ((((samp_id_acquired_from IS NOT NULL) AND (samp_id_derived_from IS NULL)) OR ((samp_id_acquired_from IS NULL) AND (samp_id_derived_from IS NOT NULL))))
);
CREATE TABLE data_set_relationships (
    id tech_id NOT NULL,
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL,
    registration_timestamp time_stamp_dfl
);
CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL
);
CREATE TABLE database_instances (
    id tech_id NOT NULL,
    code code NOT NULL,
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
CREATE DOMAIN file AS bytea;
CREATE DOMAIN file_name AS character varying(100);
CREATE TABLE experiment_attachments (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl,
    value file NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE DOMAIN generic_value AS character varying(1024);
CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    value generic_value,
    registration_timestamp time_stamp_dfl,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    CONSTRAINT expr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL))))
);
CREATE TABLE experiment_type_property_types (
    id tech_id NOT NULL,
    exty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl
);
CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);
CREATE TABLE experiments (
    id tech_id NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    mate_id_study_object tech_id,
    pers_id_registerer tech_id,
    registration_timestamp time_stamp_dfl,
    proj_id tech_id NOT NULL,
    inva_id tech_id
);
CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));
CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    location character varying(1024) NOT NULL,
    loty_id tech_id NOT NULL,
    ffty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL
);
CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);
CREATE DOMAIN description_250 AS character varying(250);
CREATE TABLE groups (
    id tech_id NOT NULL,
    code code NOT NULL,
    dbin_id tech_id NOT NULL,
    grou_id_parent tech_id,
    pers_id_leader tech_id,
    description description_250,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE TABLE invalidations (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    reason description_250
);
CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE DOMAIN real_value AS real;
CREATE TABLE material_batches (
    id tech_id NOT NULL,
    code code NOT NULL,
    amount real_value,
    mate_id tech_id NOT NULL,
    proc_id tech_id,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL
);
CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    value generic_value,
    mtpt_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    CONSTRAINT mapr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL))))
);
CREATE TABLE material_type_property_types (
    id tech_id NOT NULL,
    maty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL
);
CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);
CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL,
    mate_id_inhibitor_of tech_id,
    dbin_id tech_id NOT NULL
);
CREATE TABLE observable_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);
CREATE DOMAIN user_id AS character varying(20);
CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id user_id NOT NULL,
    email object_name,
    dbin_id tech_id NOT NULL,
    pers_id_registerer tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);
CREATE TABLE procedure_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL,
    is_data_acquisition boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);
CREATE TABLE procedures (
    id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    expe_id tech_id NOT NULL,
    pcty_id tech_id NOT NULL
);
CREATE DOMAIN description_1000 AS character varying(1000);
CREATE TABLE projects (
    id tech_id NOT NULL,
    code code NOT NULL,
    grou_id tech_id NOT NULL,
    pers_id_leader tech_id,
    pers_id_registerer tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    description description_1000
);
CREATE DOMAIN column_label AS character varying(40);
CREATE TABLE property_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL,
    label column_label NOT NULL,
    daty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL,
    covo_id tech_id,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);
CREATE DOMAIN authorization_role AS code NOT NULL
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying, 'OBSERVER'::character varying, 'ETL_SERVER'::character varying])::text[])));
CREATE TABLE role_assignments (
    id tech_id NOT NULL,
    role_code authorization_role,
    grou_id tech_id,
    dbin_id tech_id,
    pers_id_grantee tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT roas_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);
CREATE TABLE sample_component_materials (
    id tech_id NOT NULL,
    saco_id tech_id NOT NULL,
    maba_id tech_id NOT NULL
);
CREATE TABLE sample_components (
    id tech_id NOT NULL,
    code code NOT NULL,
    samp_id tech_id NOT NULL
);
CREATE TABLE sample_inputs (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    proc_id tech_id NOT NULL
);
CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    CONSTRAINT sapr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL))))
);
CREATE TABLE sample_type_property_types (
    id tech_id NOT NULL,
    saty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl
);
CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);
CREATE TABLE samples (
    id tech_id NOT NULL,
    code code NOT NULL,
    samp_id_top tech_id,
    samp_id_generated_from tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl,
    pers_id_registerer tech_id NOT NULL,
    inva_id tech_id,
    samp_id_control_layout tech_id,
    dbin_id tech_id,
    grou_id tech_id,
    CONSTRAINT samp_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);
CREATE FUNCTION controlled_vocabulary_check() RETURNS trigger
    AS $$
DECLARE
   cnt     INTEGER;
   v_code  CODE;
BEGIN
   select code into v_code from data_types where id  = NEW.daty_id;
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
CREATE FUNCTION external_data_storage_format_check() RETURNS trigger
    AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN
   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;
   RETURN NEW;
END;
$$
    LANGUAGE plpgsql;
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
CREATE SEQUENCE controlled_vocabulary_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 2, true);
CREATE SEQUENCE cvte_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('cvte_id_seq', 5, true);
CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_id_seq', 1, false);
CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);
CREATE SEQUENCE data_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_type_id_seq', 6, true);
CREATE SEQUENCE database_instance_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('database_instance_id_seq', 1, true);
CREATE SEQUENCE etpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('etpt_id_seq', 2, true);
CREATE SEQUENCE experiment_attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_attachment_id_seq', 1, false);
CREATE SEQUENCE experiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_id_seq', 1, false);
CREATE SEQUENCE experiment_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_property_id_seq', 1, false);
CREATE SEQUENCE experiment_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_type_id_seq', 2, true);
CREATE SEQUENCE file_format_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('file_format_type_id_seq', 3, true);
CREATE SEQUENCE group_id_seq
    START WITH 4
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('group_id_seq', 4, false);
CREATE SEQUENCE invalidation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('invalidation_id_seq', 1, false);
CREATE SEQUENCE locator_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('locator_type_id_seq', 1, true);
CREATE SEQUENCE material_batch_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_batch_id_seq', 20821, true);
CREATE SEQUENCE material_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_id_seq', 27819, true);
CREATE SEQUENCE material_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_property_id_seq', 27817, true);
CREATE SEQUENCE material_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_type_id_seq', 8, true);
CREATE SEQUENCE mtpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('mtpt_id_seq', 10, true);
CREATE SEQUENCE observable_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('observable_type_id_seq', 3, true);
CREATE SEQUENCE person_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('person_id_seq', 1, true);
CREATE SEQUENCE procedure_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('procedure_id_seq', 1, false);
CREATE SEQUENCE procedure_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('procedure_type_id_seq', 3, true);
CREATE SEQUENCE project_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('project_id_seq', 1, true);
CREATE SEQUENCE property_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('property_type_id_seq', 6, true);
CREATE SEQUENCE role_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('role_assignment_id_seq', 1, false);
CREATE SEQUENCE sample_component_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_component_id_seq', 26880, true);
CREATE SEQUENCE sample_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_id_seq', 70, true);
CREATE SEQUENCE sample_input_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_input_id_seq', 1, false);
CREATE SEQUENCE sample_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_property_id_seq', 70, true);
CREATE SEQUENCE sample_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_type_id_seq', 5, true);
CREATE SEQUENCE scma_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('scma_id_seq', 20935, true);
CREATE SEQUENCE stpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('stpt_id_seq', 2, true);

