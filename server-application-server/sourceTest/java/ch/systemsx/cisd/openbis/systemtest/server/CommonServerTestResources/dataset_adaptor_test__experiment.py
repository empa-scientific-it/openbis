def validate(dataset, isNew):
    try:
        assertEntity(dataset.experiment(), 'EXP1', 'experiment')
    except Exception, ex:
        return ex.args[0]
