"""
Created on Feb 20, 2014

@author: Aaron Ponti
"""

import java.io.File
import os
import re
import xml.etree.ElementTree as xml
from datetime import datetime
from BioFormatsProcessor import BioFormatsProcessor
from MicroscopySingleDatasetConfig import MicroscopySingleDatasetConfig

class Processor:
    """The Processor class performs all steps required for registering datasets
    from the assigned dropbox folder."""

    # A transaction object passed by openBIS
    _transaction = None

    # The incoming folder to process (a java.io.File object)
    _incoming = ""

    # Constructor
    def __init__(self, transaction):

        # Store arguments
        self._transaction = transaction
        self._incoming = transaction.getIncoming()

    def getCustomTimeStamp(self):
        """Create an univocal time stamp based on the current date and time
        (works around incomplete API of Jython 2.5)."""

        t = datetime.now()
        return t.strftime("%y%d%m%H%M%S") + unicode(t)[20:]

    def getSubFolders(self):
        """Return a list of subfolders of the passed incoming directory.

        @return list of subfolders (String)
        """

        incomingStr = self._incoming.getAbsolutePath()
        return [name for name in os.listdir(incomingStr)
                if os.path.isdir(os.path.join(incomingStr, name))]

    def createExperiment(self, expId, expType):
        # Create the experiment
        exp = self._transaction.createNewExperiment(expId, expType)
        if not exp:
            raise Exception("Could not create experiment " + expId + "!")

        return exp

    def processExperiment(self, experimentNode, openBISExpType):
        """Register an IExperimentUpdatable based on the Experiment XML node.

        @param experimentNode An XML node corresponding to an Experiment
        @param openBISExpType The experiment type
        @return IExperimentUpdatable experiment
        """

        # Get the openBIS identifier
        openBISIdentifier = experimentNode.attrib.get("openBISIdentifier")

        openBISExperiment = self.createExperiment(openBISIdentifier, openBISExpType)
        if not openBISExperiment:
            raise Exception("Could not create experiment " + openBISIdentifier)
        return openBISExperiment


    def processMicroscopyFile(self, microscopyFileNode, openBISExperiment, propertiesFile):
        """Register the Microscopy File using the parsed properties file.

        @param microscopyFileNode An XML node corresponding to a microscopy
        file (dataset)
        @param openBISExperiment An ISample object representing an Experiment
        """

        # Assign the file to the dataset (we will use the absolute path)
        relativeFileName = microscopyFileNode.attrib.get("relativeFileName")
        fileName = os.path.join(self._incoming.getAbsolutePath(), relativeFileName)

        # Instantiate a BioFormatsProcessor
        bioFormatsProcessor = BioFormatsProcessor(fileName)

        # Extract and store metadata
        bioFormatsProcessor.extractMetadata()

        sample = self._transaction.createNewProjectSampleWithGeneratedCode("/TEST/TEST-PROJECT", "MICROSCOPY_SAMPLE")
        sample.setExperiment(openBISExperiment)
        image_data_set = None
        num_series = bioFormatsProcessor.getNumSeries()
        for i in range(num_series):
            singleDatasetConfig = MicroscopySingleDatasetConfig(bioFormatsProcessor, i)
            if image_data_set is None:
                dataset = self._transaction.createNewImageDataSet(singleDatasetConfig, java.io.File(fileName))
                image_data_set = dataset
                self._transaction.moveFile(fileName, dataset)
            else:
                dataset = self._transaction.createNewImageDataSetFromDataSet(singleDatasetConfig, image_data_set)
            dataset.establishSampleLinkForContainedDataSets()
            dataset.setSample(sample)

    def register(self, tree, propertiesFile):
        """Register the Experiment using the parsed properties file.

        @param tree ElementTree parsed from the properties XML file
        """

        # Get the root node (obitXML)
        root = tree.getroot()

        # Iterate over the children (Experiments)
        for experimentNode in root:
            # The tag of the immediate children of the root experimentNode
            # must be Experiment
            if experimentNode.tag != "Experiment":
                raise Exception("Expected Experiment node, found " + experimentNode.tag)

            # Process an Experiment XML node and get/create an IExperimentUpdatable
            openBISExperiment = self.processExperiment(experimentNode, "MICROSCOPY_EXPERIMENT")

            # Process children of the Experiment
            for microscopyFileNode in experimentNode:
                if microscopyFileNode.tag != "MicroscopyFile":
                    raise Exception("Expected MicroscopyFile node (found " + microscopyFileNode.tag + "!")

                # Process the MicroscopyFile node
                self.processMicroscopyFile(microscopyFileNode, openBISExperiment, propertiesFile)


    def run(self):
        """Run the registration."""

        # Make sure that incoming is a folder
        if not self._incoming.isDirectory():
            raise Exception("Incoming MUST be a folder!")

        # There must be just one subfolder: the user subfolder
        subFolders = self.getSubFolders()
        if len(subFolders) != 1:
            raise Exception("Expected user subfolder!")

        # Set the user folder
        userFolder = os.path.join(self._incoming.getAbsolutePath(), subFolders[0])

        # In the user subfolder we must find the data_structure.ois file
        dataFileName = os.path.join(userFolder, "data_structure.ois")
        if not os.path.exists(dataFileName):
            raise Exception("File data_structure.ois not found!")

        # Now read the data structure file and store all the pointers to
        # the properties files. The paths are stored relative to self._incoming,
        # so we can easily build the full file paths.
        propertiesFileList = []
        f = open(dataFileName)
        try:
            for line in f:
                line = re.sub('[\r\n]', '', line)
                propertiesFile = os.path.join(self._incoming.getAbsolutePath(), line)
                propertiesFileList.append(propertiesFile)
        finally:
            f.close()

        # Process (and ultimately register) all experiments
        for propertiesFile in propertiesFileList:
            # Read the properties file into an ElementTree
            tree = xml.parse(propertiesFile)

            # Now register the experiment
            self.register(tree, propertiesFile)
