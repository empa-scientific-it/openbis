update images as img set path = (select images_directory
                                 from data_sets as d 
                                   join channel_stacks as cs on cs.ds_id = d.id
                                   join acquired_images as ai on ai.channel_stack_id = cs.id
                                   join images as i on ai.img_id = i.id 
                                 where i.id = img.id)
                                || '/' || path where path not like '%:%';
                                
alter table data_sets drop column images_directory;

/* ---------------------------------------------------------------------- */
/* FEATURE VECTORS                                                        */
/* ---------------------------------------------------------------------- */ 

CREATE TABLE FEATURE_DEFS (
    ID BIGSERIAL  NOT NULL,
    
    NAME NAME NOT NULL,
    DESCRIPTION DESCRIPTION,
    
    DS_ID  TECH_ID NOT NULL,
    
    PRIMARY KEY (ID),
    CONSTRAINT FK_FEATURE_DEFS_1 FOREIGN KEY (DS_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FEATURE_DEFS_UK_1 UNIQUE(NAME, DS_ID)
);

CREATE INDEX FEATURE_DEFS_DS_IDX ON FEATURE_DEFS(DS_ID);

CREATE TABLE FEATURE_VALUES (
    ID BIGSERIAL  NOT NULL,
		
		-- we use the fixed dimension meters here
		Z_in_M REAL,
		-- we use the fixed dimension seconds here
		T_in_SEC REAL,
		-- serialized 2D matrix with values for each spot
		VALUES BYTEA NOT NULL,
		
		FD_ID  TECH_ID NOT NULL,
		
		PRIMARY KEY (ID),
		CONSTRAINT FK_FEATURE_VALUES_1 FOREIGN KEY (FD_ID) REFERENCES FEATURE_DEFS (ID) ON DELETE CASCADE ON UPDATE CASCADE
    -- This constaint does not make any sense. Leave it out for now.
    -- CONSTRAINT FEATURE_VALUES_UK_1 UNIQUE(Z_in_M, T_in_SEC)
);

CREATE INDEX FEATURE_VALUES_FD_IDX ON FEATURE_VALUES(FD_ID);
CREATE INDEX FEATURE_VALUES_Z_AND_T_IDX ON FEATURE_VALUES(Z_in_M, T_in_SEC);
