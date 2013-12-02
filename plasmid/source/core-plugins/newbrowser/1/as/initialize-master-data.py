import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

# The default ELN Experiment
elnExperimentTypeE = tr.getOrCreateNewExperimentType("SYSTEM_EXPERIMENT")
elnExperimentTypeE.setDescription("Lab Experiment")
elnExperimentTypeS = tr.getOrCreateNewSampleType("SYSTEM_EXPERIMENT")
elnExperimentTypeS.setDescription("Lab Experiment")
elnExperimentTypeS.setListable(True)
elnExperimentTypeS.setSubcodeUnique(False)
elnExperimentTypeS.setAutoGeneratedCode(False)
elnExperimentTypeS.setGeneratedCodePrefix('C')

# The default experiment used by the UI, assigned automatically to new samples
folderType = tr.getOrCreateNewExperimentType("ELN_FOLDER")
folderType.setDescription("Folder")