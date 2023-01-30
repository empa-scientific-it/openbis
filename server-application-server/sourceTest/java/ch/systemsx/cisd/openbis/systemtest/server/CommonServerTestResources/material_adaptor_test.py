def validate(material, isNew):
    try:
        assertEqual(material.code(), 'C-NO-TIME', 'code')
        assertEqual(len(material.properties()), 4, 'number of properties')
        assertEqual(material.propertyValue('DESCRIPTION'), 'neutral control', 'description property')
        assertEqual(material.propertyValue('IS_VALID'), 'true', 'is_valid property')
    except Exception, ex:
        return ex.args[0]
