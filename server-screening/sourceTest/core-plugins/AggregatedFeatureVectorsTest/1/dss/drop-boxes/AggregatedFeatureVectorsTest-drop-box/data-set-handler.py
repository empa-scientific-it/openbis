PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"

def create_experiment(tr):
    space = tr.getSpace("TEST")
    if space == None:
        space = tr.createNewSpace("TEST", "etlserver")
    project = tr.getProject("/TEST/TEST-PROJECT")
    if project == None:
        project = tr.createNewProject("/TEST/TEST-PROJECT")
    expid = "/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"

    exp = tr.createNewExperiment(expid, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "Test experiment")
        
    return exp

def create_plate(tr, experiment, plateCode, gene):
    plateId = "/TEST/TEST-PROJECT/" + plateCode
    plate = tr.createNewSample(plateId, 'PLATE')
    plate.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
    plate.setExperiment(experiment)
    
    wellA1 = tr.createNewSample(plate.getSampleIdentifier() + ":A1", "SIRNA_WELL")
    wellA1.setPropertyValue("GENE", gene.getMaterialIdentifier())
    wellA1.setContainer(plate)

    wellA2 = tr.createNewSample(plate.getSampleIdentifier() + ":A2", "SIRNA_WELL")
    wellA2.setPropertyValue("GENE", gene.getMaterialIdentifier())
    wellA2.setContainer(plate)
    
    return plate


def create_analysis_data_set(tr, plate, builder, analysis_procedure, ds_file):    
    analysis_registration_details = factory.createFeatureVectorDatasetDetails(builder)    
    analysis_data_set = tr.createNewDataSet(analysis_registration_details)
    analysis_data_set.setSample(plate)
    analysis_data_set.setPropertyValue("$ANALYSIS_PROCEDURE", analysis_procedure)
    analysis_data_set_file = tr.moveFile(incoming.getPath() + "/" + ds_file, analysis_data_set)

def create_dataset_with_features1(tr, experiment, gene):
    plate1 = create_plate(tr, experiment, "PLATE1", gene)
    builder = factory.createFeaturesBuilder()
    
    featureX = builder.defineFeature("X")
    featureX.addValue(1, 1, "1")
    featureX.addValue(1, 2, "2")
    
    featureY = builder.defineFeature("Y")
    featureY.addValue(1, 1, "3")
    featureY.addValue(1, 2, "2")

    create_analysis_data_set(tr, plate1, builder, "p1", "data-set-1.csv")    

def create_dataset_with_features2(tr, experiment, gene):
    plate2 = create_plate(tr, experiment, "PLATE2", gene)
    builder = factory.createFeaturesBuilder()
    
    featureA = builder.defineFeature("A")
    featureA.addValue(1, 1, "10")
    featureA.addValue(1, 2, "20")
    
    featureB = builder.defineFeature("B")
    featureB.addValue(1, 1, "2")
    featureB.addValue(1, 2, "NaN")
    
    featureX = builder.defineFeature("X")
    featureX.addValue(1, 1, "5")
    featureX.addValue(1, 2, "6")

    create_analysis_data_set(tr, plate2, builder, "p2", "data-set-2.file")    


tr = service.transaction()
experiment = create_experiment(tr)

geneCode = "G"
geneG = tr.createNewMaterial(geneCode, "GENE") 

create_dataset_with_features1(tr, experiment, geneG)
create_dataset_with_features2(tr, experiment, geneG)

# delete the empty incoming, its 
# contents have been moved to the data sets 
incoming.delete()