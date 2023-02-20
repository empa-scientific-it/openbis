#! /usr/bin/env python

#   Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#        http://www.apache.org/licenses/LICENSE-2.0
#   
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# This is a dropbox for importing HCS segmentation image datasets

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import OriginalDataStorageFormat
import commonImageDropbox
import commonDropbox

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
        
def register(incomingPath):
    global datasetMetadataParser
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = datasetMetadataParser.getIBrain2DatasetId()
    openbisDatasetParentPermIds = datasetMetadataParser.getParentDatasetPermId()
    
    for openbisDatasetParentPermId in openbisDatasetParentPermIds:
        (space, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParentPermId, iBrain2DatasetId, incomingPath)
        if plate != None:
            break
    if plate == None:
        return
    
    imageDataset = commonImageDropbox.IBrain2SegmentationImageDataSetConfig()
    imageDataset.setSegmentationImageDatasetType()
    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("TIFF")
    imageDataset.setRecognizedImageExtensions(["tif", "tiff"])
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)
    imageDataset.setGenerateHighQualityThumbnails(True)
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setOriginalDataStorageFormat(OriginalDataStorageFormat.HDF5)
    imageDataset.setConvertTransformationCliArguments("-contrast-stretch 0 -edge 1 -threshold 1 -transparent black")
    imageDataset.setUseImageMagicToGenerateThumbnails(False)
    imageDataset.setAllowedMachineLoadDuringThumbnailsGeneration(1/2.0)
    imageDataset.setImageLibrary("BioFormats", "TiffDelegateReader")

    commonDropbox.setImageDatasetPropertiesAndRegister(imageDataset, datasetMetadataParser, incoming, service, factory, None, True)
    

if incoming.isDirectory():
    register(incoming.getPath())
