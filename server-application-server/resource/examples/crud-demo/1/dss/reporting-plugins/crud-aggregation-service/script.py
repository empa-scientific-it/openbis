ID = "Id"
INFO = "Info"

def aggregate(parameters, tableBuilder):
  result = queryService.select("crud-db", "SELECT id, info FROM exampledata ORDER BY id")
  tableBuilder.addHeader(ID)
  tableBuilder.addHeader(INFO)  
  for resultRow in result:
    rowid = resultRow.get('id')
    info = resultRow.get('info')
    row = tableBuilder.addRow()
    row.setCell(ID, rowid)
    row.setCell(INFO, info)

