def validate(sample, isNew):
    try:
        assertEntity(sample.container(), 'CL1', 'container')
    except Exception, ex:
        return ex.args[0]
