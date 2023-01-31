KEY = "Key"
VALUE = "Value"

def aggregate(parameters, tableBuilder):
    tableBuilder.addHeader(KEY)
    tableBuilder.addHeader(VALUE)
    for entry in parameters.entrySet():
        row = tableBuilder.addRow()
        row.setCell(KEY, entry.key)
        row.setCell(VALUE, entry.value)
    mailService.createEmailSender().withSubject("test").send()
