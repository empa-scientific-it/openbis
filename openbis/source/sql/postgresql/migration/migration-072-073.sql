-- Migration from 072 to 073

ALTER TABLE DATA ADD COLUMN INVA_ID TECH_ID;
ALTER TABLE DATA ADD CONSTRAINT DATA_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);
CREATE INDEX DATA_INVA_FK_I ON DATA (INVA_ID);
