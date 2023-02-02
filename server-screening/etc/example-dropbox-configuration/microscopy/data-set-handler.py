import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
 
class MicroscopyConfig(SimpleImageContainerDataConfig):
    def getChannelColor(self, channelCode):
        dict = { "CHANNEL-1" : ChannelColor.GREEN, "CHANNEL-2" : ChannelColor.RED }
        if channelCode in dict:
            return dict[channelCode]
        else:
            return None
 
imageDataset = MicroscopyConfig()
imageDataset.setRecognizedImageExtensions(['lif', 'dv', 'tif', 'tiff', 'nd2', 'lsm'])
imageDataset.setDataSetType("IMG_MICROSCOPY")
imageDataset.setMicroscopyData(True)
imageDataset.setGenerateThumbnails(True)
imageDataset.setMaxThumbnailWidthAndHeight(512)
imageDataset.setUseImageMagicToGenerateThumbnails(False)
imageDataset.setPlate("TEST", "MY_SAMPLE")
imageDataset.setImageLibrary("BioFormats")
factory.registerImageDataset(imageDataset, incoming, service)