-- Migration from 192 to 193

ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_BK_UK;
ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT SAPR_BK_UK;
ALTER TABLE DATA_SET_PROPERTIES DROP CONSTRAINT DSPR_BK_UK;


ALTER TABLE IF EXISTS PROPERTY_TYPES
    ADD COLUMN IS_MULTI_VALUE BOOLEAN_CHAR NOT NULL DEFAULT 'F';
