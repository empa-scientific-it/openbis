transaction = service.transaction(incoming, factory)
dataSet = transaction.createNewDataSet()
dataSet.setPropertyValue('dataSetProp', 'dataSetPropValue')
transaction.moveFile(incoming.getPath() + '/sub_data_set_1', dataSet)
dataSet.setDataSetType('O1')

experiment = transaction.getExperiment('/SPACE/PROJECT/EXP')
dataSet.setExperiment(experiment)

material = transaction.createNewMaterial("new-material", "new-material-type")
material.setPropertyValue("material-prop", "material-prop-value")

#
# TODO KE: dataSet.setMaterialProperty("AAAA", material)
#

