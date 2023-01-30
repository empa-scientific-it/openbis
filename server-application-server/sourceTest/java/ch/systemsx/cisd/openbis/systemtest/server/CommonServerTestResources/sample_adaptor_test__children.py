def validate(sample, isNew):
    try:
        assertEntities(sample.children(), ['CP1-A1','CP1-A2'], 'children')
        assertEntities(sample.childrenOfType('CELL_PLATE'), ['CP1-A1','CP1-A2'], 'children of type CELL_PLATE')
        assertEntities(sample.childrenOfType('NOT_EXISTING_TYPE'), [], 'children of type NOT_EXISTING_TYPE')
    except Exception, ex:
        return ex.args[0]
