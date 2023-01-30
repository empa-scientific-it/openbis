-- Migration from 045 to 046

------------------------------------------------------------------------------------
--  Purpose:  allow longer codes 
------------------------------------------------------------------------------------

-- Switch all uses of the domain code (there are a lot of them!) to use varchar(40)
alter table CONTROLLED_VOCABULARIES alter column CODE type varchar(40);
alter table DATA alter column CODE type varchar(40), alter column DATA_PRODUCER_CODE type varchar(40);
alter table DATABASE_INSTANCES alter column CODE type varchar(40), alter column UUID type varchar(40);
alter table DATA_STORES alter column CODE type varchar(40);
alter table DATA_TYPES alter column CODE type varchar(40);
alter table EXPERIMENTS alter column PERM_ID type varchar(40), alter column CODE type varchar(40);
alter table EXPERIMENT_TYPES alter column CODE type varchar(40);
alter table FILE_FORMAT_TYPES alter column CODE type varchar(40);
alter table GROUPS alter column CODE type varchar(40);
alter table LOCATOR_TYPES alter column CODE type varchar(40);
alter table MATERIALS alter column CODE type varchar(40);
alter table MATERIAL_TYPES alter column CODE type varchar(40);
alter table DATA_SET_TYPES alter column CODE type varchar(40);
alter table PROJECTS alter column CODE type varchar(40);
alter table PROPERTY_TYPES alter column CODE type varchar(40);
alter table SAMPLES alter column PERM_ID type varchar(40), alter column CODE type varchar(40);
alter table SAMPLE_TYPES alter column CODE type varchar(40), alter column generated_code_prefix type varchar(40);
alter table AUTHORIZATION_GROUPS alter column CODE type varchar(40);

-- Convert CODE to VARCHAR(60)
drop DOMAIN CODE;
create DOMAIN CODE as varchar(60);

-- Switch all columns back to using the domain code
alter table CONTROLLED_VOCABULARIES alter column CODE type CODE;
alter table DATA alter column CODE type CODE, alter column DATA_PRODUCER_CODE type CODE;
alter table DATABASE_INSTANCES alter column CODE type CODE, alter column UUID type CODE;
alter table DATA_STORES alter column CODE type CODE;
alter table DATA_TYPES alter column CODE type CODE;
alter table EXPERIMENTS alter column PERM_ID type CODE, alter column CODE type CODE;
alter table EXPERIMENT_TYPES alter column CODE type CODE;
alter table FILE_FORMAT_TYPES alter column CODE type CODE;
alter table GROUPS alter column CODE type CODE;
alter table LOCATOR_TYPES alter column CODE type CODE;
alter table MATERIALS alter column CODE type CODE;
alter table MATERIAL_TYPES alter column CODE type CODE;
alter table DATA_SET_TYPES alter column CODE type CODE;
alter table PROJECTS alter column CODE type CODE;
alter table PROPERTY_TYPES alter column CODE type CODE;
alter table SAMPLES alter column PERM_ID type CODE, alter column CODE type CODE;
alter table SAMPLE_TYPES alter column CODE type CODE, alter column generated_code_prefix type CODE;
alter table AUTHORIZATION_GROUPS alter column CODE type CODE;
