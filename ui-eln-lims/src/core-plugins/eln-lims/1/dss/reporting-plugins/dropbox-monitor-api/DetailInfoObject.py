import Util
import os


class DetailInfoObject:

    def __init__(self, logN, dropboxName, dropboxInProcess,
                 dropboxFailed, dropboxSucceeded, logDirectory):
        self.logN = logN
        self.dropboxName = dropboxName
        self.dropboxInProcess = dropboxInProcess
        self.dropboxFailed = dropboxFailed
        self.dropboxSucceeded = dropboxSucceeded
        self.logDirectory = logDirectory

    def getDetailInfoMap(self):
        (processFiles, succededFiles, failedFiles) = Util.returnNLatestResultsAll(
            self.dropboxName, self.dropboxInProcess, self.dropboxSucceeded,
            self.dropboxFailed, self.logN)

        filteredNFiles = self.filterNlatest(
            processFiles, succededFiles, failedFiles)

        (processFilesDict, succededFilesDict, failedFilesDict) = self.readFilteredFilesAll(
            processFiles, succededFiles, failedFiles, filteredNFiles)

        dropboxInformationDict = self.prepareInformationDict(
            processFilesDict, succededFilesDict, failedFilesDict)

        return dropboxInformationDict

    def prepareInformationDict(self, processDict, succededDict, failedDict):
        if (len(processDict) > 0 or len(succededDict) > 0 or
                len(failedDict) > 0):
            tmpDict = {}
            tmpDict["process"] = processDict
            tmpDict["failed"] = failedDict
            tmpDict["succeded"] = succededDict
            return tmpDict
        else:
            return None

    def filterNlatest(self, processFiles, succededFiles, failedFiles):
        filteredNFiles = []
        filteredNFiles.extend(processFiles)
        filteredNFiles.extend(failedFiles)
        filteredNFiles.extend(succededFiles)
        filteredNFiles.sort(reverse=True)
        filteredNFiles = filteredNFiles[0:self.logN]
        return filteredNFiles

    def readFilteredFilesAll(self, process, succeded, failed, filteredNFiles):
        (dropboxInProcess, dropboxSucceeded, dropboxFailed) = self.getLogsDir()
        return (self.readFilteredFiles(process, filteredNFiles, dropboxInProcess),
                self.readFilteredFiles(
                    succeded, filteredNFiles, dropboxSucceeded),
                self.readFilteredFiles(failed, filteredNFiles, dropboxFailed))

    def readFilteredFiles(self, list, container, logDir):
        foundFilesDict = {}
        for item in list:
            if item in container:
                openFile = open(os.path.join(logDir, item), 'r')
                foundFilesDict[item] = openFile.read()
                openFile.close()
        return foundFilesDict

    def getLogsDir(self):
        return (os.path.join(self.logDirectory, "in-process"),
                os.path.join(self.logDirectory, "succeeded"),
                os.path.join(self.logDirectory, "failed"))
