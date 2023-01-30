def validate(dataset, isNew):
    try:
        assertEntities(dataset.parents(), ["20081105092159111-1", "20081105092159222-2", "20081105092159333-3"], 'parents')
        assertEntities(dataset.parentsOfType('HCS_IMAGE'), ["20081105092159111-1", "20081105092159222-2", "20081105092159333-3"], 'parents of type HCS_IMAGE')
        assertEntities(dataset.parentsOfType('NOT_EXISTING_TYPE'), [], 'parents of type NOT_EXISTING_TYPE')
    except Exception, ex:
        return ex.args[0]
