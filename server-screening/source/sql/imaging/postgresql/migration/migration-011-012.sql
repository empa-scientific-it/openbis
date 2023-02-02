-- Migration from 011 to 012
CREATE DOMAIN CHANNEL_COLOR AS VARCHAR(20) CHECK (VALUE IN ('BLUE', 'GREEN', 'RED', 'RED_GREEN', 'RED_BLUE', 'GREEN_BLUE'));

ALTER TABLE channels ADD COLUMN color CHANNEL_COLOR;
  
update channels upd_ch
set color = 
  (  select 
  			CASE 
  			  WHEN (upd_ch.id - min(id)) % 6 = 0 THEN 'BLUE'
		 			WHEN (upd_ch.id - min(id)) % 6 = 1 THEN 'GREEN'
		 			WHEN (upd_ch.id - min(id)) % 6 = 2 THEN 'RED'
		 			WHEN (upd_ch.id - min(id)) % 6 = 3 THEN 'RED_GREEN'
		 			WHEN (upd_ch.id - min(id)) % 6 = 4 THEN 'RED_BLUE'
				  WHEN (upd_ch.id - min(id)) % 6 = 5 THEN 'GREEN_BLUE'
		    END color
         from channels cc
         where upd_ch.exp_id = cc.exp_id or upd_ch.ds_id = cc.ds_id
         group by cc.exp_id, cc.ds_id
  );
           
ALTER TABLE channels ALTER COLUMN color SET NOT NULL;
