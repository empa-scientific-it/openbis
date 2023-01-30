def validate(dataset, isNew):
    try:
        assertEqual(dataset.code(), '20081105092159111-1', 'code')
        assertEqual(len(dataset.properties()), 4, 'number of properties')
        assertEqual(dataset.propertyValue('COMMENT'), 'no comment', 'comment property')
        assertEqual(dataset.propertyValue('GENDER'), 'FEMALE', 'gender property')
        assertEqual(dataset.propertyValue('BACTERIUM'), 'BACTERIUM1 (BACTERIUM)', 'bacterium property')
    except Exception, ex:
        return ex.args[0]
