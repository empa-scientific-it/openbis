-- Migration from 074 to 075

----------------------------------------------
-- Purpose: Rename invalidation to deletion --
----------------------------------------------
-- renamings:
-- table INVALIDATIONS -> DELETIONS  
ALTER TABLE invalidations RENAME TO deletions;
-- sequence INVALIDATION_ID_SEQ -> DELETION_ID_SEQ
SELECT RENAME_SEQUENCE('INVALIDATION_ID_SEQ', 'DELETION_ID_SEQ');
-- columns INVA_ID -> DEL_ID
ALTER TABLE data RENAME inva_id TO del_id;
ALTER TABLE samples RENAME inva_id TO del_id;
ALTER TABLE experiments RENAME inva_id TO del_id;
-- indexes
ALTER INDEX data_inva_fk_i RENAME TO data_del_fk_i;
ALTER INDEX expe_inva_fk_i RENAME TO expe_del_fk_i;
ALTER INDEX inva_pers_fk_i RENAME TO del_pers_fk_i;
ALTER INDEX samp_inva_fk_i RENAME TO samp_del_fk_i;
--
-- recreate constraints with new names (renaming is not possible)
--
ALTER TABLE data 
  DROP CONSTRAINT data_inva_fk;
ALTER TABLE experiments
  DROP CONSTRAINT expe_inva_fk;
ALTER TABLE samples
  DROP CONSTRAINT samp_inva_fk;
ALTER TABLE deletions 
  DROP CONSTRAINT inva_pk;
  
ALTER TABLE deletions 
  ADD CONSTRAINT del_pk PRIMARY KEY(id);
ALTER TABLE data 
  ADD CONSTRAINT data_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE experiments 
  ADD CONSTRAINT expe_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE samples 
  ADD CONSTRAINT samp_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
  
ALTER TABLE deletions 
  DROP CONSTRAINT inva_pers_fk;
ALTER TABLE deletions 
  ADD CONSTRAINT del_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
--
-- remove 'INVALIDATION' from EVENT_TYPE domain (we don't store or handle such events)
--
DELETE FROM events WHERE event_type = 'INVALIDATION';
ALTER DOMAIN event_type DROP CONSTRAINT event_type_check;
ALTER DOMAIN event_type ADD CONSTRAINT event_type_check CHECK (VALUE IN ('DELETION', 'MOVEMENT'));

----------------------------------------------------------------------------------------------------
-- Purpose: Remove all deletions and make reason not null.
-- Reasoning:
--- they were only test deletions and probably current DB state doesn't satisfy consistency rules 
--  introduced with triggers in next migration,
--- it is easier to delete them than to fix inconsistencies.
----------------------------------------------------------------------------------------------------

UPDATE data SET del_id = NULL;
UPDATE samples SET del_id = NULL;
UPDATE experiments SET del_id = NULL;
DELETE FROM deletions;
ALTER TABLE deletions ALTER COLUMN reason SET NOT NULL;