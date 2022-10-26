def getRenderedProperty(entity, property):
    value = entity.property(property)
    if value is not None:
        return value.renderedValue()

def validate(entity, isNew):
    start_date = getRenderedProperty(entity, "START_DATE")
    end_date = getRenderedProperty(entity, "END_DATE")
    if start_date is not None and end_date is not None and start_date > end_date:
        return "End date cannot be before start date!"
