-- move screenable plates to a group, because it's no longer possible
-- to have such plates on the instance level

CREATE OR REPLACE FUNCTION move_exp_samples_to_group() RETURNS integer AS $$
DECLARE
    sample RECORD;
    groupId Integer;
BEGIN
  FOR sample IN 
    (SELECT * FROM samples s inner join sample_types t on s.saty_id = t.id 
	where t.code in ('CELL_PLATE', 'REINFECT_PLATE'))
  LOOP
	if (sample.dbin_id is not NULL) THEN
		select proj.grou_id into groupId from
			samples sa
			left outer join sample_inputs si on sa.id = si.samp_id 
			left outer join procedures proc on proc.id = si.proc_id  
			left outer join experiments ex on ex.id = proc.expe_id
			left outer join projects proj on proj.id = ex.proj_id
		where sa.id = sample.id;
		if (groupId is NULL) THEN
			select id into groupId from groups limit 1;
		END IF;
		if (groupId is NULL) THEN
			RAISE EXCEPTION 'There is no group in the database - internal error!';
		END IF;
		update samples set grou_id = groupId, dbin_id = NULL
		  where id = sample.id;
	END IF;
  END LOOP;
  RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

select move_exp_samples_to_group();

-- Create new table DATA_STORES.

CREATE TABLE DATA_STORES (ID TECH_ID NOT NULL,DBIN_ID TECH_ID NOT NULL,CODE CODE NOT NULL,DOWNLOAD_URL VARCHAR(1024) NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE SEQUENCE DATA_STORE_ID_SEQ;
ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_PK PRIMARY KEY(ID);
ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
CREATE INDEX DAST_DBIN_FK_I ON DATA_STORES (DBIN_ID);

-- Add new column DAST_ID (connection to DATA_STORES table) to tables DATABASE_INSTANCES, GROUPS, PROJECTS, EXPERIMENTS.

ALTER TABLE DATABASE_INSTANCES ADD COLUMN DAST_ID TECH_ID;
ALTER TABLE GROUPS ADD COLUMN DAST_ID TECH_ID;
ALTER TABLE PROJECTS ADD COLUMN DAST_ID TECH_ID;
ALTER TABLE EXPERIMENTS ADD COLUMN DAST_ID TECH_ID;

-- Create the STANDARD data store and assign it to the home database instance.
 
insert into data_stores
(id
,code
,download_url
,dbin_id)
values 
(nextval('DATA_STORE_ID_SEQ')
,'STANDARD'
,''
,(select id from database_instances where is_original_source = true)
);

update database_instances set dast_id = (select id from data_stores where code = 'STANDARD') where is_original_source = true;

-- Add foreign key constratints.

ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);