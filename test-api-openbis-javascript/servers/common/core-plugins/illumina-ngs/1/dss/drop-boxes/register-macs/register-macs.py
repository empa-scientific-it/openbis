
def process(transaction):
  # Create a data set and set type
  dataSet = transaction.createNewDataSet("MACS_OUTPUT")
  dataSet.setPropertyValue("MACS_VERSION", "1.4.0RC2")
  dataSet.setMeasuredData(False)

  incomingPath = incoming.getAbsolutePath()
   
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)

  # rsplit not in Python 2.2
  #sampleName = ":".join(incomingPath.split("/")[-1].rsplit("_",1))
  flowCell =  "_".join((incomingPath.split("/")[-1].split("_")[2:-1]))
  lane = (incomingPath.split("/")[-1]).split("_")[-1]
  sampleName = flowCell + ":" + lane
   
  # Set the owner of the data set -- the specified sample
  sample = transaction.getSample("/BSSE_BEISEL/" + sampleName)

  dataSet.setSample(sample)
