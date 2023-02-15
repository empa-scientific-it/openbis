try:
    import json
except:
    from com.xhaus.jyson import JysonCodec as json
from datetime import datetime


class SimpleInfoBuilder:

    def __init__(self, tableBuilder):
        self.tableBuilder = tableBuilder
        self.now = datetime.now()

    def buildRow(self, simpleInfoMap):
        row = self.tableBuilder.addRow()
        row.setCell("dropbox name", simpleInfoMap["dropboxName"])
        row.setCell("last status", simpleInfoMap["lastStatus"])
        row.setCell("current status", simpleInfoMap["currentStatus"])
        row.setCell("current status time", self.formatDate(
            simpleInfoMap["currentStatusTime"]))
        row.setCell("last success", self.formatDate(
            simpleInfoMap["lastSuccess"]))
        row.setCell("last failure", self.formatDate(
            simpleInfoMap["lastFailure"]))

    def createHeader(self):
        self.tableBuilder.addHeader("dropbox name")
        self.tableBuilder.addHeader("last status")
        self.tableBuilder.addHeader("current status")
        self.tableBuilder.addHeader("current status time")
        self.tableBuilder.addHeader("last success")
        self.tableBuilder.addHeader("last failure")

    def formatDate(self, actionTime):
        if actionTime != None:
            deltatime = self.now - actionTime
            days = deltatime.days
            hours = deltatime.seconds / 60 / 60
            minutes = deltatime.seconds / 60 % 60
            seconds = deltatime.seconds % 60 % 60
            dateJson = {}
            dateJson["days"] = days
            dateJson["hours"] = hours
            dateJson["minutes"] = minutes
            dateJson["seconds"] = seconds
            return json.dumps(dateJson)
        else:
            return None
