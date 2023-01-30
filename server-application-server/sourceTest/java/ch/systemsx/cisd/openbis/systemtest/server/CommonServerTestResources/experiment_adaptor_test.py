def validate(experiment, isNew):
    try:
        assertEqual(experiment.code(), 'EXP-REUSE', 'code')
        assertEqual(len(experiment.properties()), 1, 'number of properties')
        assertEqual(experiment.propertyValue('DESCRIPTION'), 'Test of sample reusage from invalidated experiments', 'description property')
    except Exception, ex:
        return ex.args[0]
