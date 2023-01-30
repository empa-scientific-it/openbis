-- Migration from 055 to 056

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
-- Screening specific migration. Nothing will be performed on openBIS databases 
-- which are not screening specific.
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION merge_words(text, text) RETURNS text AS $$
DECLARE
  BEGIN
    IF  character_length($1) > 0 THEN
      RETURN $1 || ' ' || $2;
    ELSE
      RETURN $2;
    END IF;
  END;
$$ LANGUAGE plpgsql;

CREATE AGGREGATE merge_words(text)
(
  SFUNC = merge_words,
  STYPE = text
);


CREATE OR REPLACE FUNCTION REPLACE_GENE_SYMBOL_BY_GENE_ID() RETURNS void AS $$
DECLARE
	counter  int;
	library_id_assigned_to_gene bool;
BEGIN
	--------------------------------------------------
	-- create a GENE_SYMBOL property and assign it to GENE
	--------------------------------------------------
	
	select true  
	into library_id_assigned_to_gene 
	from material_type_property_types mtpt, material_types mt, property_types pt  
	where mtpt.maty_id = mt.id and mtpt.prty_id = pt.id 
		and mt.code = 'GENE' and pt.code = 'LIBRARY_ID';
	
	if library_id_assigned_to_gene IS NULL then 
		-- skip migration if gene has no library_id property
		return;
	end if; 
	
	
	select count(*)
	into counter 
	from  
		(select m.id, count(mp.id) as c 
			from materials m, material_types mt, material_properties mp, material_type_property_types mtpt, property_types pt 
			where m.maty_id = mt.id and mp.mate_id = m.id and mp.mtpt_id = mtpt.id and mtpt.maty_id = mt.id and pt.id = mtpt.prty_id
				and mt.code = 'GENE' and pt.code = 'LIBRARY_ID' 
			group by m.id) as counter_table 
	where c < 1; 
	
	if counter > 0 then 
		-- skip migration if there is at least one gene without library_id
		return;
	end if;
	
	
	insert into property_types
		(id
		,code
		,description
		,label
		,daty_id
		,pers_id_registerer
		,dbin_id)
	values 
		(nextval('PROPERTY_TYPE_ID_SEQ')
		,'GENE_SYMBOLS'
		,'Gene symbols'
		,'Gene symbols'
		,(select id from data_types where code ='VARCHAR')
		,(select id from persons where user_id ='system')
		,(select id from database_instances where is_original_source = 'T')
	);
		
	insert into material_type_property_types( 
	  id,
	  maty_id,
	  prty_id,
	  is_mandatory,
	  pers_id_registerer,
	  ordinal
	) values(
			nextval('mtpt_id_seq'), 
			(select id from material_types where code = 'GENE'),
			(select id from property_types where code = 'GENE_SYMBOLS'),
			false,
			(select id from persons where user_id ='system'),
			(select max(ordinal)+1 from material_type_property_types 
				where maty_id = (select id from material_types where code = 'GENE'))
		);

	--------------------------------------------------
	-- create temporary table with all gene migration information
	--------------------------------------------------

	create temp table genes 
	(
		id tech_id,
		code code,
		library_id generic_value,
		gene_codes generic_value,
		new_id tech_id,
		library_tech_id tech_id		
	);
	
	insert into genes (id, code, library_id, library_tech_id)
		(select m.id, m.code, mp.value as library_id, mp.id as library_tech_id from materials m, material_properties mp, material_type_property_types mtpt, property_types pt, material_types mt
		where mp.mate_id = m.id and  mp.mtpt_id = mtpt.id and pt.id = mtpt.prty_id and pt.code = 'LIBRARY_ID' and mt.id = m.maty_id and mt.id = mtpt.maty_id and mt.code = 'GENE');
	
	update genes set new_id = map.to_id from (select g_from.id as from_id, min(g_to.id) as to_id  from genes g_from, genes g_to where g_from.library_id = g_to.library_id group by g_from.id) as map where map.from_id = id;	
	
	update genes set gene_codes = map.gene_codes from (select g_from.library_id as lib_id, merge_words(distinct g_to.code) as gene_codes from genes g_from, genes g_to where g_from.library_id = g_to.library_id group by g_from.library_id) as map where map.lib_id = library_id;	

	--------------------------------------------------
	-- update gene references
	--------------------------------------------------

	update EXPERIMENTS set MATE_ID_STUDY_OBJECT = genes.new_id from genes where genes.id = MATE_ID_STUDY_OBJECT and not genes.id = genes.new_id;
	update EXPERIMENT_PROPERTIES set  MATE_PROP_ID = genes.new_id from genes where genes.id = MATE_PROP_ID and not genes.id = genes.new_id;
	update MATERIAL_PROPERTIES set  MATE_PROP_ID = genes.new_id from genes where genes.id = MATE_PROP_ID and not genes.id = genes.new_id;
	update SAMPLE_PROPERTIES set  MATE_PROP_ID = genes.new_id from genes where genes.id = MATE_PROP_ID and not genes.id = genes.new_id;
	update DATA_SET_PROPERTIES set  MATE_PROP_ID = genes.new_id from genes where genes.id = MATE_PROP_ID and not genes.id = genes.new_id;
	
	delete from  material_properties where mate_id in (select id from genes where not genes.id = genes.new_id);
	delete from MATERIALS where id in (select id from genes where not genes.id = genes.new_id);

	delete from genes where id in (select id from genes where not genes.id = genes.new_id);


	--------------------------------------------------
	-- replace gene code with library_id
	--------------------------------------------------
	
	update materials set code = genes.library_id from genes where materials.id = genes.id ;
	delete from material_properties where id in (select library_tech_id from genes);
	
	--------------------------------------------------
	-- create a gene symbols property for each gene
	--------------------------------------------------
	
	insert into material_properties
		(id
	  , mate_id
	  , mtpt_id
	  , "value"
	  , pers_id_registerer)
		(select nextval('material_property_id_seq') id
	  	, genes.id  mate_id
	    , (select mtpt.id
	    		from   material_type_property_types mtpt, material_types mt, property_types pt
	    		where  pt.code = 'GENE_SYMBOLS' and mt.code = 'GENE' and mtpt.prty_id = pt.id
	        	and mtpt.maty_id = mt.id) mtpt_id
	    , genes.gene_codes "value"
	    , (select id from persons where user_id = 'system') pers_id_registerer
	 	from genes); 

	--------------------------------------------------
	-- delete temporary table
	--------------------------------------------------
	
	drop table genes;
	
	delete from material_type_property_types where id = 
		(select mtpt.id from material_type_property_types mtpt, material_types mt, property_types pt 
			where pt.id = mtpt.prty_id and mtpt.maty_id = mt.id 
			and pt.code = 'LIBRARY_ID' and mt.code = 'GENE');
	
END;
$$ LANGUAGE 'plpgsql';

select REPLACE_GENE_SYMBOL_BY_GENE_ID();
drop function REPLACE_GENE_SYMBOL_BY_GENE_ID();
DROP AGGREGATE merge_words(text);
drop function merge_words(text,text);


--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------

UPDATE sample_types SET code = 'SIRNA_WELL' WHERE code = 'OLIGO_WELL';
UPDATE material_types SET code = 'SIRNA' WHERE code = 'OLIGO';
UPDATE property_types SET code = 'SIRNA', label = 'siRNA' WHERE code = 'OLIGO';



















