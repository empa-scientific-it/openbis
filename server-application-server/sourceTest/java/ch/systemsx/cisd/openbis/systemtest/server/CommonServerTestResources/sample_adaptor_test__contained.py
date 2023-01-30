def validate(sample, isNew):
    try:
        assertEntities(sample.contained(), ['A01','A03'], 'contained')
        assertEntities(sample.containedOfType('WELL'), ['A01','A03'], 'contained of type WELL')
        assertEntities(sample.containedOfType('NOT_EXISTING_TYPE'), [], 'contained of type NOT_EXISTING_TYPE')
    except Exception, ex:
        return ex.args[0]
