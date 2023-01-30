def configureUI():
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader('column1')
    tableBuilder.addHeader('column2')
    tableBuilder.addHeader('column3') 
    
    row1 = tableBuilder.addRow()
    row1.setCell('column1','v1') 
    row1.setCell('column2', 1)
    row1.setCell('column3', 1.5) 
    
    row2 = tableBuilder.addRow()
    row2.setCell('column1','v2') 
    row2.setCell('column2', 2)
    row2.setCell('column3', 2.5)
    
    row3 = tableBuilder.addRow()
    row3.setCell('column1','v3')
    
    property.setOwnTab(True)
    uiDesc = property.getUiDescription()
    uiDesc.useTableOutput(tableBuilder.getTableModel())