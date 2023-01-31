def aggregate(parameters, tableBuilder):
  dataSetCode = parameters.get('dataset-code')
  content = contentProviderUnfiltered.getContent(dataSetCode)
  
  tableBuilder.addHeader("name")
  row = tableBuilder.addRow()
  row.setCell("name", content.rootNode.getName())