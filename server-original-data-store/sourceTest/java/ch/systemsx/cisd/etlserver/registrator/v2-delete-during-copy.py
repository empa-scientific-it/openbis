from java.lang import IllegalArgumentException
from java.io import File
from ch.systemsx.cisd.common.filesystem import FileUtilities

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def process(tr):
  # This should not be called
  jythonHookTestTool.log("process")
  dataSet = transaction.createNewDataSet()
  incoming = transaction.getIncoming()
  transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(transaction.getExperiment('/SPACE/PROJECT/EXP'))

