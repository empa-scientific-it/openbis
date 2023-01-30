-- Make some labels wider
alter table CONTROLLED_VOCABULARY_TERMS alter column LABEL type varchar(128);
alter table PROPERTY_TYPES alter column LABEL type varchar(128);
drop domain COLUMN_LABEL;
create domain COLUMN_LABEL as varchar(128);
alter table CONTROLLED_VOCABULARY_TERMS alter column LABEL type COLUMN_LABEL;
alter table PROPERTY_TYPES alter column LABEL type COLUMN_LABEL;