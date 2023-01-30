def validate(sample, isNew):
    try:
        assertEntities(sample.parents(), ['A02','3V-125'], 'parents')
        assertEntities(sample.parentsOfType('DILUTION_PLATE'), ['3V-125'], 'parents of type DILUTION_PLATE')
        assertEntities(sample.parentsOfType('WELL'), ['A02'], 'parents of type WELL')
        assertEntities(sample.parentsOfType('NOT_EXISTING_TYPE'), [], 'parents of type NOT_EXISTING_TYPE')
    except Exception, ex:
        return ex.args[0]
