def updateFromUI(action):
    if action.getName() == 'a1':
        value = 'a1|'
        for input in action.getInputWidgetDescriptions():
            inputValue = input.getValue()
            if inputValue is None:
                inputValue = 'null'
            value = value + input.getLabel() + '=' + inputValue + '|'
        property.setValue(value)
    elif action.getName() == 'a2':
        value = 'a2!'
        for input in action.getInputWidgetDescriptions():
            inputValue = input.getValue()
            if inputValue is None:
                inputValue = 'null'
            value = value + input.getLabel() + '=' + inputValue + '!'
        property.setValue(value)
    else:
        raise ValidationException('action ' + action.getName() + ' is not supported')