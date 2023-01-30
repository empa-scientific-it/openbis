#!/usr/bin/env python

"""
An Jython dropbox for importing a CSV file with feature vectors.
"""

import os
from java.util import Properties
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v2 import SimpleFeatureVectorDataConfig 

def getConfigurationProperties():
  config = Properties()
  config.setProperty("separator", ",")
  config.setProperty("well-name-row", "Well")
  config.setProperty("well-name-col", "Well")
  config.setProperty("well-name-col-is-alphanum", "true")
  return config

def process(transaction):
  space="PLATONIC"
  incoming = transaction.incoming
  splittedFileName = incoming.getName().split('.')
  plateCode = splittedFileName[0]
  sample = transaction.getSample("/" + space + "/" + plateCode)
  configProps = getConfigurationProperties()
  config = SimpleFeatureVectorDataConfig(configProps)
  dataset = transaction.createNewFeatureVectorDataSet(config, incoming)
  dataset.setFileFormatType('CSV')
  if len(splittedFileName) > 1:
    dataset.setPropertyValue("$ANALYSIS_PROCEDURE", splittedFileName[1])
  dataset.setSample(sample)
  transaction.moveFile(incoming.getPath(), dataset)

