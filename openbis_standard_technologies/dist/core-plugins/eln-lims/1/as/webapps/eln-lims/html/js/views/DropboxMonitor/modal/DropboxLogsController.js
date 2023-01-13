function DropboxLogsController(mainController, dropboxName) {
    this._mainController = mainController;
    this._model = new DropboxLogsModel(dropboxName);
    this._view = new DropboxLogsView(this, this._model);

    this.init = function() {
        this.loadLogFiles();
    }
    
    this.loadLogFiles = function() {
        var _this = this;
        var dropboxName = this._model.dropboxName;
        var maxNumberOfLogs = this._model.maxNumberOfLogs;
        this._mainController.serverFacade.getDropboxMonitorLogs(dropboxName, maxNumberOfLogs, function(rows) {
            if (rows[0][1].value !== "null") {
                _this._model.logFiles = [];
                Object.entries(JSON.parse(rows[0][1].value)).forEach(function(entry) {
                    var status = entry[0];
                    var files = entry[1];
                    Object.entries(files).forEach(function(logFile) {
                        _this._model.logFiles.push({"logFile": logFile[0],
                            "status": status,
                            "content": logFile[1]});
                    });
                });
                _this._model.logFiles.sort(function(e1, e2) {
                    var f1 = e1.logFile;
                    var f2 = e2.logFile;
                    return f1 < f2 ? 1 : (f1 > f2 ? -1 : 0);
                });
            }
            _this._view.repaint();
        });
    }
}