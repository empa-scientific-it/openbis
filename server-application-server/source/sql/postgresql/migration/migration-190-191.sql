-- Migration from 190 to 191
insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'JSON'
,'Json value'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_INTEGER'
,'Array of integer values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_REAL'
,'Array of floating point values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_STRING'
,'Array of string values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_TIMESTAMP'
,'Array of timestamp values'
);

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN INTEGER_ARRAY_VALUE text_value;

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN REAL_ARRAY_VALUE text_value;

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN TIMESTAMP_ARRAY_VALUE text_value;

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN STRING_ARRAY_VALUE text_value;

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_CK;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );


