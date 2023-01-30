DATA_SET = "Data Set"
DATA_SET_TYPE = "Data Set Type"

def describe(dataSets, tableBuilder):
    tableBuilder.addHeader(DATA_SET)
    tableBuilder.addHeader(DATA_SET_TYPE)
    for dataSet in dataSets:
        row = tableBuilder.addRow()
        row.setCell(DATA_SET, dataSet.dataSetCode)
        row.setCell(DATA_SET_TYPE, dataSet.dataSetTypeCode)
    mailService.createEmailSender().withSubject("test").send()
