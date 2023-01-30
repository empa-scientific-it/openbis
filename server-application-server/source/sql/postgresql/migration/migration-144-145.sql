-- Fixing samples with non-unique codes by appending the perm_id
update samples_all set code=code||'_'||perm_id 
where id in (select s.id from samples_all s join samples_all s2 on (s.code=s2.code and s.space_id = s2.space_id)
                              join sample_types st on s.saty_id=st.id 
             where (coalesce(s.samp_id_part_of,-1) = coalesce(s2.samp_id_part_of,-1) 
                      or (st.is_subcode_unique and s.saty_id=s2.saty_id))
                   and s.id > s2.id);

-- run trigger for recalculating 'code_unique_check' and 'subcode_unique_check' for samples with old values
update samples_all set code=code where code_unique_check like '%,%,%,%' or subcode_unique_check like '%,%,%,%';

