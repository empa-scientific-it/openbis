def configureUI():
    factory = inputWidgetFactory()

    widgets = [
        factory.createTextInputField('t1'),
        factory.createTextInputField('t2')\
               .setValue('default 2'),
        factory.createTextInputField('t3')\
               .setDescription('description 3'),
        factory.createMultilineTextInputField('multi')\
               .setValue('default m')\
               .setDescription('multiline'),
        factory.createComboBoxInputField('combo', ['v1', 'v2', 'v3'])\
               .setMandatory(True)\
               .setDescription('select from list')
    ]
    
    uiAction = property.getUiDescription().addAction('Create')
    uiAction.addInputWidgets(widgets) 