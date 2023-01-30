#! /usr/bin/env python
# Jython dropbox which is not used by iBrain2.
# It is suitable to upload any uninterpreted datasets with the API.

import commonDropbox
#reload(commonDropbox)

transaction = service.transaction()
dataset = transaction.createNewDataSet()
transaction.moveFile(incoming.getPath(), dataset)

type = dataset.getDataSetType()
exp = dataset.getExperiment()
if (type == "HCS_MODULESETTINGS_OBJECTCLASSIFICATION_MAT" and exp is not None):
	expCode = exp.getExperimentIdentifier().split("/")[-1]
	datasetCode = dataset.getDataSetCode()
	commonDropbox.RegistrationConfirmationUtils().createExternalDatasetConfirmation(datasetCode, expCode, type, incoming)