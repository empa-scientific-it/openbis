from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import FeatureListDataConfig

def process(transaction):

  featureContainerDataSet = transaction.getDataSetForUpdate("20130412153659994-391")
  
  config = FeatureListDataConfig()
  config.setName("NUMBER_FEATURE_LIST");
  config.setFeatureList(["row number", "column number"])
  config.setContainerDataSet(featureContainerDataSet)
  
  transaction.createNewFeatureListDataSet(config)
  
  config = FeatureListDataConfig()
  config.setName("BARCODE_AND_STATE_FEATURE_LIST");
  config.setFeatureList(["barcode", "STATE"])
  config.setContainerDataSet(featureContainerDataSet)
  
  transaction.createNewFeatureListDataSet(config)
