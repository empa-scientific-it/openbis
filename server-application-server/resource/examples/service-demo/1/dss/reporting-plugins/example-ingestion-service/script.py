def process(tr, parameters, tableBuilder):
  """Create a new sample with the code specified in the parameters

  This script assumes that the experiment '/TEST/TEST-PROJECT/DEMO-EXP-HCS' 
  and sample type 'LIBRARY' exist. These are included by default when openBIS
  is installed by the installer.
  """
  sampleCode = parameters.get("code")
  sampleId = "/TEST/" + sampleCode
  sample = tr.createNewSample(sampleId, "LIBRARY")
  exp = tr.getExperiment("/TEST/TEST-PROJECT/DEMO-EXP-HCS")
  sample.setExperiment(exp)
  
  tableBuilder.addHeader("CODE")  
  tableBuilder.addHeader("IDENTIFIER")
  row = tableBuilder.addRow()
  row.setCell("CODE", sampleCode)
  row.setCell("IDENTIFIER", sampleId)
