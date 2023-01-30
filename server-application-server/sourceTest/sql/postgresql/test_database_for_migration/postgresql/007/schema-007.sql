SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET search_path = public, pg_catalog;
CREATE DOMAIN code AS character varying(20);
CREATE DOMAIN description_250 AS character varying(250);
CREATE DOMAIN description_80 AS character varying(80);
CREATE DOMAIN double_precision_value AS double precision;
CREATE DOMAIN file AS bytea;
CREATE DOMAIN file_name AS character varying(100);
CREATE DOMAIN object_name AS character varying(50);
CREATE DOMAIN real_value AS real;
CREATE DOMAIN tech_id AS bigint;
CREATE DOMAIN time_stamp AS timestamp without time zone DEFAULT now();
CREATE DOMAIN user_id AS character varying(20);
CREATE FUNCTION br001_crtl_plate_chk() RETURNS "trigger"
    AS $$
DECLARE
   cnt           INTEGER;
   v_cell_plate  CODE;
BEGIN
   select code into v_cell_plate from sample_types where id  = NEW.saty_id;
   -- Check if the sample is of type "Cell Plate"
   if v_cell_plate = 'CELL PLATE' then
      -- Check that a Control Plate exists before registering the Cell Plate
      select count(*) into cnt from samples s, sample_types st where s.saty_id = st.id and st.code = 'CONTROL LAYOUT';	 
      IF cnt = 0 THEN
		 -- Doens't make sense, so comment it out
         -- RAISE EXCEPTION 'A Control Plate must exist before you can register Cell Plate: %', NEW.code;
      END IF;
   end if;
   RETURN NEW;
END;
$$
    LANGUAGE plpgsql;
SET default_tablespace = '';
SET default_with_oids = false;
CREATE TABLE data (
    id tech_id NOT NULL,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    obty_id tech_id NOT NULL,
    proc_id_acquired_by tech_id NOT NULL,
    samp_id_acquired_from tech_id,
    samp_id_derived_from tech_id,
    CONSTRAINT data_samp_arc_ck CHECK ((((samp_id_acquired_from IS NOT NULL) AND (samp_id_derived_from IS NULL)) OR ((samp_id_acquired_from IS NULL) AND (samp_id_derived_from IS NOT NULL))))
);
CREATE SEQUENCE data_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_id_seq', 4, true);
CREATE SEQUENCE data_value_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('data_value_id_seq', 1, false);
CREATE TABLE data_values (
    id tech_id NOT NULL,
    data_id tech_id NOT NULL,
    saco_id tech_id NOT NULL,
    value double_precision_value NOT NULL
);
CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);
CREATE SEQUENCE experiment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_id_seq', 4, true);
CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    value file NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE SEQUENCE experiment_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_property_id_seq', 4, true);
CREATE SEQUENCE experiment_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('experiment_type_id_seq', 1, true);
CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE TABLE experiments (
    id tech_id NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    mate_id_study_object tech_id,
    pers_id_registerer tech_id,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    description description_250,
    proj_id tech_id NOT NULL
);
CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    "location" character varying(200) NOT NULL,
    loty_id tech_id NOT NULL,
    ffty_id tech_id NOT NULL
);
CREATE SEQUENCE file_format_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('file_format_type_id_seq', 3, true);
CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE SEQUENCE locator_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('locator_type_id_seq', 1, true);
CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE SEQUENCE material_batch_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_batch_id_seq', 21204, true);
CREATE TABLE material_batches (
    id tech_id NOT NULL,
    code code NOT NULL,
    amount real_value,
    mate_id tech_id NOT NULL,
    cont_id tech_id,
    proc_id tech_id,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE SEQUENCE material_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_id_seq', 27824, true);
CREATE SEQUENCE material_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('material_type_id_seq', 6, true);
CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_250,
    maty_id tech_id NOT NULL,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE TABLE molecules (
    mate_id tech_id NOT NULL,
    "sequence" character varying(100),
    accession_number object_name,
    off_set character varying(20),
    mole_id_inhibitor_of tech_id,
    gene_symbol code
);
CREATE SEQUENCE observable_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('observable_type_id_seq', 2, true);
CREATE TABLE observable_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE SEQUENCE organization_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('organization_id_seq', 3, true);
CREATE TABLE organizations (
    id tech_id NOT NULL,
    code code NOT NULL
);
CREATE SEQUENCE person_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('person_id_seq', 2, true);
CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id user_id,
    email object_name
);
CREATE SEQUENCE procedure_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('procedure_id_seq', 5, true);
CREATE SEQUENCE procedure_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('procedure_type_id_seq', 2, true);
CREATE TABLE procedure_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL
);
CREATE TABLE procedures (
    id tech_id NOT NULL,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    expe_id tech_id NOT NULL,
    pcty_id tech_id NOT NULL,
    pers_id tech_id
);
CREATE SEQUENCE project_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('project_id_seq', 4, true);
CREATE TABLE projects (
    id tech_id NOT NULL,
    code code NOT NULL,
    orga_id tech_id NOT NULL
);
CREATE SEQUENCE sample_component_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_component_id_seq', 27263, true);
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
CREATE SEQUENCE sample_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_id_seq', 92, true);
CREATE SEQUENCE sample_input_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_input_id_seq', 4, true);
CREATE TABLE sample_inputs (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    proc_id tech_id NOT NULL
);
CREATE SEQUENCE sample_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('sample_type_id_seq', 5, true);
CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);
CREATE TABLE samples (
    id tech_id NOT NULL,
    code code NOT NULL,
    cont_id tech_id,
    proc_id tech_id,
    samp_id_top tech_id,
    samp_id_generated_from tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);
CREATE SEQUENCE scma_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
SELECT pg_catalog.setval('scma_id_seq', 21318, true);

