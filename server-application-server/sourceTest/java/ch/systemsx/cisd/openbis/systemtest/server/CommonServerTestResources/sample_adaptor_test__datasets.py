def validate(sample, isNew):
    try:
        assertEntities(sample.dataSets(), ["20081105092159333-3", "20110805092359990-17"], 'data sets')
        assertEntities(sample.dataSetsOfType('HCS_IMAGE'), ["20081105092159333-3", "20110805092359990-17"], 'data sets of type HCS_IMAGE')
        assertEntities(sample.dataSetsOfType('NOT_EXISTING_TYPE'), [], 'data sets of type NOT_EXISTING_TYPE')
    except Exception, ex:
        return ex.args[0]
