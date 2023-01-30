def validate(sample, isNew):
    try:
        assertEqual(sample.code(), 'CP-TEST-1', 'code')
        assertEqual(len(sample.properties()), 5, 'number of properties')
        assertEqual(sample.propertyValue('SIZE'), '123', 'size property')
        assertEqual(sample.propertyValue('ORGANISM'), 'HUMAN', 'organism property')
        assertEqual(sample.propertyValue('BACTERIUM'), 'BACTERIUM-X (BACTERIUM)', 'bacterium property')
    except Exception, ex:
        return ex.args[0]
