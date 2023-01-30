-- Migration from 081 to 082

delete from data_stores where code = 'STANDARD' and id not in (select distinct dast_id from data)