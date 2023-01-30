----------------------------------------------------------------------------------------------
--  File: migration-008-009.sql
--
-- 
--  This script enables the migration of the database schema from 008 to 009.
-- 
--  Update History
--
--
--	Who							When				What
--	---							----				----
--	Tomasz Pylak		09-11-2007	Initial Version - updated property types labels and codes 
--  
----------------------------------------------------------------------------------------------
----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table PROPERTY_TYPES
--
--  Authors: Tomasz Pylak
--
--  Change History:
--
--  Date          Author       Description
--  09-11-2007   Tomasz Pylak  First Draft
--
-----------------------------------------------------------------------

update PROPERTY_TYPES
set code = 'NUCLEOTIDE_SEQUENCE',
		label = 'Nucleotide Sequence'
where code = 'NUCLEOTIDE SEQUENCE';

update PROPERTY_TYPES
set code = 'GENE_SYMBOL',
		label = 'Gene Symbol'
where code = 'GENE SYMBOL';

update PROPERTY_TYPES
set code = 'ACCESSION_NUMBER',
		label = 'Accession Number'
where code = 'ACCESSION NUMBER';

update PROPERTY_TYPES
set label = 'Description'
where code = 'DESCRIPTION';

update PROPERTY_TYPES
set label = 'Offset'
where code = 'OFFSET';
