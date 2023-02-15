import Util

class SimpleInfoObject:

    def __init__(self, dropboxName, dropboxInProcess, dropboxFailed,
                 dropboxSucceeded):
        self.dropboxName = dropboxName
        self.dropboxInProcess = dropboxInProcess
        self.dropboxFailed = dropboxFailed
        self.dropboxSucceeded = dropboxSucceeded

    def getSimpleInfoMap(self):
        informationDict = {}

        runStartTime = self.evalRunStartTime()
        currentStatus = self.evalCurrentStatus(runStartTime)
        lastSuccess = self.evalLastSuccess()
        lastFailure = self.evalLastFailure()
        lastStatus = self.evalLastStatus(lastSuccess, lastFailure)
        currentStatusTime = self.evalCurrentStatusTime(
            runStartTime, lastSuccess, lastFailure)

        informationDict["dropboxName"] = self.dropboxName # string
        informationDict["lastStatus"] = lastStatus # 0, 1 or None
        informationDict["currentStatus"] = currentStatus # string or None
        informationDict["currentStatusTime"] = currentStatusTime # datetime or None
        informationDict["lastSuccess"] = lastSuccess # datetime or None
        informationDict["lastFailure"] = lastFailure # datetime or None

        return informationDict

    def evalRunStartTime(self):
        if Util.isInList(self.dropboxName, self.dropboxInProcess):
            return Util.returnLatestResultDate(self.dropboxName, self.dropboxInProcess)
        else:
            return None

    def evalCurrentStatus(self, runStartTime):
        if runStartTime != None:
            return "running"
        else:
            return "idle"

    def evalLastSuccess(self):
        if Util.isInList(self.dropboxName, self.dropboxSucceeded):
            return Util.returnLatestResultDate(self.dropboxName, self.dropboxSucceeded)
        else:
            return None

    def evalLastFailure(self):
        if Util.isInList(self.dropboxName, self.dropboxFailed):
            return Util.returnLatestResultDate(self.dropboxName, self.dropboxFailed)
        else:
            return None

    def evalLastStatus(self, lastSuccess, lastFailure):
        states = {"success": 0, "failure": 1, "noInfo": None}
        if lastSuccess != None and lastFailure != None:
            if lastSuccess > lastFailure:
                return states["success"]
            else:
                return states["failure"]
        elif lastSuccess != None:
            return states["success"]
        elif lastFailure != None:
            return states["failure"]
        else:
            return states["noInfo"]

    def evalCurrentStatusTime(self, runStartTime, lastSuccess, lastFailure):
        if runStartTime != None:
            return runStartTime
        elif lastSuccess != None and lastFailure != None:
            return lastSuccess if lastSuccess > lastFailure else lastFailure
        elif lastSuccess != None:
            return lastSuccess
        else:
            return lastFailure
