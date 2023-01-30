def validate(dataset, isNew):
    try:
        assertEntity(dataset.container(), '20110509092359990-10', 'container')
    except Exception, ex:
        return ex.args[0]
