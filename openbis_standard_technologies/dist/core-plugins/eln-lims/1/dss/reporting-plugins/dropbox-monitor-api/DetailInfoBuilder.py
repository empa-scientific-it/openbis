try:
    import json
except:
    from com.xhaus.jyson import JysonCodec as json

class DetailInfoBuilder:

    def __init__(self, tableBuilder, dropboxName):
        self.tableBuilder = tableBuilder
        self.dropboxName = dropboxName

    def createHeader(self):
        self.tableBuilder.addHeader("dropboxName")
        self.tableBuilder.addHeader("Information")

    def buildRow(self, detailInfoMap):
        row = self.tableBuilder.addRow()
        row.setCell("dropboxName", self.dropboxName)
        row.setCell("Information", json.dumps(detailInfoMap))
