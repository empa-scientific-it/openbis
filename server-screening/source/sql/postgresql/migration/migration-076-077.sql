-- Migration from 076 to 077

---------------------------------------------
-- the property "DESCRIPTION" should be optional for materials "VIRUS", "BACTERIUM", "COMPOUND"
-- (screening-specific requirement)
---------------------------------------------
UPDATE material_type_property_types 
    SET is_mandatory=FALSE 
    WHERE 
        maty_id IN (SELECT id FROM material_types WHERE code IN ('VIRUS', 'BACTERIUM', 'COMPOUND'))
    AND
        prty_id = (SELECT id FROM property_types WHERE code='DESCRIPTION');
