def validate(dataset, isNew):
    try:
        assertEntity(dataset.sample(), 'CP-TEST-2', 'sample')
    except Exception, ex:
        return ex.args[0]
