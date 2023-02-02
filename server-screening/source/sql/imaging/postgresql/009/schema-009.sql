
/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN CODE AS VARCHAR(40);

CREATE DOMAIN NAME AS VARCHAR(80);

CREATE DOMAIN DESCRIPTION AS VARCHAR(200);

CREATE DOMAIN FILE_PATH as VARCHAR(1000);

CREATE DOMAIN COLOR_COMPONENT AS VARCHAR(40) CHECK (VALUE IN ('RED', 'GREEN', 'BLUE'));

CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;

/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

CREATE TABLE EXPERIMENTS (
  ID BIGSERIAL NOT NULL,
  PERM_ID CODE NOT NULL,
  IMAGE_TRANSFORMER_FACTORY BYTEA,

  PRIMARY KEY (ID),
  UNIQUE (PERM_ID)
);

CREATE TABLE CONTAINERS (
  ID BIGSERIAL NOT NULL,
  PERM_ID CODE NOT NULL,

  SPOTS_WIDTH INTEGER,
  SPOTS_HEIGHT INTEGER,
  
  EXPE_ID TECH_ID NOT NULL,

  PRIMARY KEY (ID),
  UNIQUE (PERM_ID),
  CONSTRAINT FK_SAMPLE_1 FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX CONTAINERS_EXPE_IDX ON CONTAINERS(EXPE_ID);

CREATE TABLE SPOTS (
  ID BIGSERIAL NOT NULL,
	
	-- position in the container, one-based
  X INTEGER, 
  Y INTEGER, 
  CONT_ID TECH_ID NOT NULL,
  
  PRIMARY KEY (ID),
  CONSTRAINT FK_SPOT_1 FOREIGN KEY (CONT_ID) REFERENCES CONTAINERS (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX SPOTS_CONT_IDX ON SPOTS(CONT_ID);
-- allows to select one spot of the container quicker
CREATE INDEX SPOTS_COORDS_IDX ON SPOTS(CONT_ID, X, Y);

CREATE TABLE DATA_SETS (
  ID BIGSERIAL NOT NULL,
  PERM_ID CODE NOT NULL,
  
	FIELDS_WIDTH INTEGER,
	FIELDS_HEIGHT INTEGER,	
  
  CONT_ID TECH_ID,
  -- a redundant information if there are timepoint or depth stack data for any spots in this dataset
  IS_MULTIDIMENSIONAL BOOLEAN_CHAR NOT NULL,
  -- transformation for merged channels on the dataset level, overrides experiment level transformation
  IMAGE_TRANSFORMER_FACTORY BYTEA,
  
  PRIMARY KEY (ID),
  UNIQUE (PERM_ID),
  CONSTRAINT FK_DATA_SET_1 FOREIGN KEY (CONT_ID) REFERENCES CONTAINERS (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX DATA_SETS_CONT_IDX ON DATA_SETS(CONT_ID);

CREATE TABLE CHANNELS (
    ID BIGSERIAL  NOT NULL,
    
    CODE NAME NOT NULL,
    LABEL NAME NOT NULL,
    DESCRIPTION DESCRIPTION,
    WAVELENGTH INTEGER,
    IMAGE_TRANSFORMER_FACTORY BYTEA,

    DS_ID TECH_ID,
    EXP_ID TECH_ID,
    
    PRIMARY KEY (ID),
    CONSTRAINT FK_CHANNELS_1 FOREIGN KEY (DS_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_CHANNELS_2 FOREIGN KEY (EXP_ID) REFERENCES EXPERIMENTS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT CHANNELS_DS_EXP_ARC_CK CHECK ((DS_ID IS NOT NULL AND EXP_ID IS NULL) OR (DS_ID IS NULL AND EXP_ID IS NOT NULL)),
    
    CONSTRAINT CHANNELS_UK_1 UNIQUE(CODE, DS_ID),
    CONSTRAINT CHANNELS_UK_2 UNIQUE(CODE, EXP_ID)
);

CREATE INDEX CHANNELS_DS_IDX ON CHANNELS(DS_ID);

CREATE TABLE CHANNEL_STACKS (
    ID BIGSERIAL  NOT NULL,
		
		-- x and y are kind of a two dimensional sequence number, some use case may only use x and leave y alone
		X INTEGER,
		Y INTEGER,
		-- We use the fixed dimension meters here.
		Z_in_M REAL,
		-- We use the fixed dimension seconds here.
		T_in_SEC REAL,
		SERIES_NUMBER INTEGER,
		
		-- For all channel stacks of a well (HCS) or image dataset (microscopy) there should be exactly 
		-- one record with is_representative = true
		is_representative BOOLEAN_CHAR NOT NULL DEFAULT 'F',
    
    DS_ID TECH_ID	NOT NULL,
		SPOT_ID TECH_ID,

    PRIMARY KEY (ID),
    CONSTRAINT FK_CHANNEL_STACKS_1 FOREIGN KEY (SPOT_ID) REFERENCES SPOTS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_CHANNEL_STACKS_2 FOREIGN KEY (DS_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX CHANNEL_STACKS_DS_IDX ON CHANNEL_STACKS(DS_ID);
CREATE INDEX CHANNEL_STACKS_SPOT_IDX ON CHANNEL_STACKS(SPOT_ID);
CREATE INDEX CHANNEL_STACKS_DIM_IDX ON CHANNEL_STACKS(X, Y, Z_in_M, T_in_SEC);

CREATE TABLE IMAGES (
    ID BIGSERIAL  NOT NULL,
   
    PATH	FILE_PATH NOT NULL,
    PAGE	INTEGER,
    COLOR	COLOR_COMPONENT,
    
    PRIMARY KEY (ID)
);

CREATE TABLE ACQUIRED_IMAGES (
    ID BIGSERIAL  NOT NULL,
   
		IMG_ID   TECH_ID NOT NULL,
		THUMBNAIL_ID  TECH_ID,

    CHANNEL_STACK_ID  TECH_ID NOT NULL,
    CHANNEL_ID  TECH_ID NOT NULL,

    PRIMARY KEY (ID),
    CONSTRAINT FK_IMAGES_1 FOREIGN KEY (CHANNEL_STACK_ID) REFERENCES CHANNEL_STACKS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_IMAGES_2 FOREIGN KEY (CHANNEL_ID) REFERENCES CHANNELS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_IMAGES_3 FOREIGN KEY (IMG_ID) REFERENCES IMAGES (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_IMAGES_4 FOREIGN KEY (THUMBNAIL_ID) REFERENCES IMAGES (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IMAGES_CHANNEL_STACK_IDX ON ACQUIRED_IMAGES(CHANNEL_STACK_ID);
CREATE INDEX IMAGES_CHANNEL_IDX ON ACQUIRED_IMAGES(CHANNEL_ID);
CREATE INDEX IMAGES_IMG_IDX ON ACQUIRED_IMAGES(IMG_ID);
CREATE INDEX IMAGES_THUMBNAIL_IDX ON ACQUIRED_IMAGES(THUMBNAIL_ID);

CREATE TABLE EVENTS (
  LAST_SEEN_DELETION_EVENT_ID TECH_ID NOT NULL
);

/* ---------------------------------------------------------------------- */
/* FEATURE VECTORS                                                        */
/* ---------------------------------------------------------------------- */ 

CREATE TABLE FEATURE_DEFS (
    ID BIGSERIAL  NOT NULL,
    
    CODE NAME NOT NULL,
    LABEL NAME NOT NULL,
    DESCRIPTION DESCRIPTION,
    
    DS_ID  TECH_ID NOT NULL,
    
    PRIMARY KEY (ID),
    CONSTRAINT FK_FEATURE_DEFS_1 FOREIGN KEY (DS_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FEATURE_DEFS_UK_1 UNIQUE(CODE, DS_ID)
);

CREATE INDEX FEATURE_DEFS_DS_IDX ON FEATURE_DEFS(DS_ID);

CREATE TABLE FEATURE_VOCABULARY_TERMS (
	ID BIGSERIAL  NOT NULL,

  CODE NAME NOT NULL,
  SEQUENCE_NUMBER INTEGER NOT NULL,
	FD_ID  TECH_ID NOT NULL,

	PRIMARY KEY (ID),
	CONSTRAINT FK_FEATURE_VOCABULARY_TERMS_1 FOREIGN KEY (FD_ID) REFERENCES FEATURE_DEFS (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX FEATURE_VOCABULARY_TERMS_FD_IDX ON FEATURE_VOCABULARY_TERMS(FD_ID);

CREATE TABLE FEATURE_VALUES (
    ID BIGSERIAL  NOT NULL,
		
		-- we use the fixed dimension meters here
		Z_in_M REAL,
		-- we use the fixed dimension seconds here
		T_in_SEC REAL,
		-- Serialized 2D matrix with values for each spot.
		-- Contains floats which can be NaN. 
		-- It is never a case that the whole matrix contains NaN - in such a case we save nothing.
		-- If feature definition has some connected vocabulary terms then the matrix 
		-- stores FEATURE_VOCABULARY_TERMS.SEQUENCE_NUMBER of the terms (should be casted from float to int).
		-- If the term is null the Float.NaN is stored.
		VALUES BYTEA NOT NULL,
		
		FD_ID  TECH_ID NOT NULL,
		
		PRIMARY KEY (ID),
		CONSTRAINT FK_FEATURE_VALUES_1 FOREIGN KEY (FD_ID) REFERENCES FEATURE_DEFS (ID) ON DELETE CASCADE ON UPDATE CASCADE
    -- This constaint does not make any sense. Leave it out for now.
    -- CONSTRAINT FEATURE_VALUES_UK_1 UNIQUE(Z_in_M, T_in_SEC)
);

CREATE INDEX FEATURE_VALUES_FD_IDX ON FEATURE_VALUES(FD_ID);
CREATE INDEX FEATURE_VALUES_Z_AND_T_IDX ON FEATURE_VALUES(Z_in_M, T_in_SEC);


/* ---------------------------------------------------------------------- */
/* FUNCTIONS AND TRIGGERS                                                 */
/* ---------------------------------------------------------------------- */ 

CREATE OR REPLACE FUNCTION DELETE_UNUSED_IMAGES() RETURNS trigger AS $$
BEGIN
   delete from images where id = OLD.img_id or id = OLD.thumbnail_id;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UNUSED_IMAGES AFTER DELETE ON ACQUIRED_IMAGES
    FOR EACH ROW EXECUTE PROCEDURE DELETE_UNUSED_IMAGES();
    
------------------------------------------------------------------------------------
--  Purpose:  Create trigger CHANNEL_STACKS_CHECK which checks if both spot_id and dataset.cont_id 
--            are both null or not null.
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION CHANNEL_STACKS_CHECK() RETURNS trigger AS $$
DECLARE
   v_cont_id  CODE;
BEGIN

   select cont_id into v_cont_id from data_sets where id = NEW.ds_id;

   -- Check that if there is no spot than there is no dataset container as well
   if v_cont_id IS NULL then
      if NEW.spot_id IS NOT NULL then
         RAISE EXCEPTION 'Insert/Update of CHANNEL_STACKS failed, as the dataset container is not set, but spot is (spot id = %).',NEW.spot_id;
      end if;
	 else
      if NEW.spot_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of CHANNEL_STACKS failed, as the dataset container is set (id = %), but spot is not set.',v_cont_id;
      end if; 
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER CHANNEL_STACKS_CHECK BEFORE INSERT OR UPDATE ON CHANNEL_STACKS
    FOR EACH ROW EXECUTE PROCEDURE CHANNEL_STACKS_CHECK();