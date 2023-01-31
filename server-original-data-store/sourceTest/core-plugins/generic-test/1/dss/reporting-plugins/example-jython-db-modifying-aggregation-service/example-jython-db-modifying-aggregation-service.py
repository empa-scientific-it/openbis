def process(tr, parameters, tableBuilder):
  sampleCode = parameters.get("code")
  sampleId = "/CISD/" + sampleCode
  sample = tr.createNewSample(sampleId, "DYNAMIC_PLATE")
  exp = tr.getExperiment("/CISD/NEMO/EXP-TEST-1")
  sample.setExperiment(exp)
  
  tableBuilder.addHeader("CODE")  
  tableBuilder.addHeader("IDENTIFIER")
  row = tableBuilder.addRow()
  row.setCell("CODE", sampleCode)
  row.setCell("IDENTIFIER", sampleId)
