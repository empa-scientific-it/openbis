from ch.systemsx.cisd.openbis.generic.shared.basic.dto import DataSetKind

SPACE_CODE = "LDSS"
PROJECT_ID = "/LDSS/LDSP"
EXPERIMENT_ID = "/LDSS/LDSP/LDSPE"

def create_project_and_experiment(transaction):
    space = transaction.createNewSpace(SPACE_CODE, None)
    space.setDescription("A demo space")
    project = transaction.createNewProject(PROJECT_ID)
    project.setDescription("A demo project")
    exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "A sample experiment")
    return exp

def process(transaction):
    experiment = create_project_and_experiment(transaction)
    linkds = transaction.createNewDataSet("LINK_TYPE")
    linkds.setExperiment(experiment)
    linkds.setExternalCode("EX_CODE_1")
    linkds.setDataSetKind(DataSetKind.LINK)
    externalDMS = transaction.getExternalDataManagementSystem("DMS_1")
    if (externalDMS is None):
        raise "External data management system with code DMS_1, has not been found in the database"
    linkds.setExternalDataManagementSystem(externalDMS)

