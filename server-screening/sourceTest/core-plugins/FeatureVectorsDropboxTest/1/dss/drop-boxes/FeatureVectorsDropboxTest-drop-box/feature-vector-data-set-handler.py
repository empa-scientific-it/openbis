#!/usr/bin/env python

PROJECT_CODE = "TEST-FEATURE-PROJECT"
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"

"""
An Jython dropbox for importing HCS image datasets for use by the FeatureVectorsDropboxTest
"""

import os
from java.io import File
from java.util import Properties
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import SimpleFeatureVectorDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import FeatureListDataConfig

def create_experiment(tr):
    space = tr.getSpace("TEST")
    if space == None:
        space = tr.createNewSpace("TEST", "etlserver")
    project = tr.getProject("/TEST/" + PROJECT_CODE)
    if project == None:
        project = tr.createNewProject("/TEST/" + PROJECT_CODE)
    expid = "/TEST/" + PROJECT_CODE +"/TRANSFORMED_THUMBNAILS_EXP"

    exp = tr.createNewExperiment(expid, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "Test experiment")
        
    return exp

def create_plate(tr, experiment, plateCode):
    plateId = "/TEST/TEST-PROJECT/" + plateCode
    plate = tr.createNewSample(plateId, 'PLATE')
    plate.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
    plate.setExperiment(experiment)
    
    wellA1 = tr.createNewSample(plate.getSampleIdentifier() + ":A1", "SIRNA_WELL")
    wellA1.setContainer(plate)

    wellA2 = tr.createNewSample(plate.getSampleIdentifier() + ":A2", "SIRNA_WELL")
    wellA2.setContainer(plate)
    
    return plate
    
def config_props():
  config = Properties()
  config.setProperty("separator", ",")
  config.setProperty("well-name-row", "Well")
  config.setProperty("well-name-col", "Well")
  config.setProperty("well-name-col-is-alphanum", "true")
  return config

def process(transaction):
  experiment = create_experiment(transaction)
  plate = create_plate(transaction, experiment, 'PLATE-FEATURE-VECTOR-TEST')
  
  configProps = config_props()
  config = SimpleFeatureVectorDataConfig(configProps)
  
  featuresPath = os.path.join(transaction.getIncoming().getAbsolutePath(), "features.csv")
  featureDataSet = transaction.createNewFeatureVectorDataSet(config, File(featuresPath))
  featureDataSet.setFileFormatType('CSV')

  featureDataSet.setSample(plate)
  transaction.moveFile(featuresPath, featureDataSet)
  
  config = FeatureListDataConfig()
  config.setName("shortListOfFeatures");
  config.setFeatureList(["feature1", "feature2"])
  config.setContainerDataSet(featureDataSet)
  
  transaction.createNewFeatureListDataSet(config)
  