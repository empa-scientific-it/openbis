def validate(dataset, isNew):
    try:
        assertEntities(dataset.children(), ["20081105092259900-0", "20081105092259900-1"], 'children')
        assertEntities(dataset.childrenOfType('HCS_IMAGE'), ["20081105092259900-0", "20081105092259900-1"], 'children of type HCS_IMAGE')
        assertEntities(dataset.childrenOfType('NOT_EXISTING_TYPE'), [], 'children of type NOT_EXISTING_TYPE')
        assertEntities(dataset.contained(), [], 'components')
        assertEntities(dataset.containers(), [], 'containers')
    except Exception, ex:
        return ex.args[0]
