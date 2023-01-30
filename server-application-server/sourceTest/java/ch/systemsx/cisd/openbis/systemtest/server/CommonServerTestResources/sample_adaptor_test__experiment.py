def validate(sample, isNew):
    try:
        assertEntity(sample.experiment(), 'EXP11', 'experiment')
    except Exception, ex:
        return ex.args[0]
