-- Migration from 128 to 129

CREATE INDEX EXRELH_SAMP_ID_FK_I ON EXPERIMENT_RELATIONSHIPS_HISTORY (SAMP_ID);
CREATE INDEX EXRELH_DATA_ID_FK_I ON EXPERIMENT_RELATIONSHIPS_HISTORY (DATA_ID);
CREATE INDEX SAMPRELH_SAMP_ID_FK_I ON SAMPLE_RELATIONSHIPS_HISTORY (SAMP_ID);
CREATE INDEX SAMPRELH_DATA_ID_FK_I ON SAMPLE_RELATIONSHIPS_HISTORY (DATA_ID);
CREATE INDEX DATARELH_DATA_FK_I ON DATA_SET_RELATIONSHIPS_HISTORY (DATA_ID);

CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
	   AND (SELECT DEL_ID FROM SAMPLES_ALL WHERE ID = OLD.SAMP_ID) IS NULL
     DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
	   AND (SELECT DEL_ID FROM DATA_ALL WHERE ID = OLD.DS_ID) IS NULL
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
