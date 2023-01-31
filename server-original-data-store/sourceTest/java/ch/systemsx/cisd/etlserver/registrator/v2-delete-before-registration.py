from java.lang import IllegalArgumentException
from java.io import File
from ch.systemsx.cisd.common.filesystem import FileUtilities

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

def pre_metadata_registration(context):
  path_to_delete = context.getPersistentMap().get("path")
  file_to_delete = File(path_to_delete)
  FileUtilities.deleteRecursively(file_to_delete)
  jythonHookTestTool.log("pre_metadata_registration")

def process(tr):
  # Remember the original file so we can delete it before we do metadata registration
  incomingDir = tr.getGlobalState().getThreadParameters().getIncomingDataDirectory()
  original = File(incomingDir, tr.getIncoming().getName())
  context = tr.getRegistrationContext()
  context.getPersistentMap().put("path", original.getPath())
  
  dataSet = tr.createNewDataSet()
  incoming = tr.getIncoming()
  tr.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
  dataSet.setDataSetType('O1')
  dataSet.setExperiment(tr.getExperiment('/SPACE/PROJECT/EXP'))

