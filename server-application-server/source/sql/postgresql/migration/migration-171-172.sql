-- Migration from 171 to 172

------------------------------------------------------------------------------------
--  Purpose:  SSDM-6070: Add archiving request flag to physical datasets
------------------------------------------------------------------------------------

ALTER TABLE external_data ADD COLUMN archiving_requested BOOLEAN_CHAR NOT NULL DEFAULT 'F'
