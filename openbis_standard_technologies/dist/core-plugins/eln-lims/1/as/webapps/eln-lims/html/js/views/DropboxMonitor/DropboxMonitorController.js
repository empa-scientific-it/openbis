function DropboxMonitorController(mainController) {
    this._mainController = mainController;
    this._dropboxMonitorModel = new DropboxMonitorModel();
    this._dropboxMonitorView = new DropboxMonitorView(this, this._dropboxMonitorModel);

    this.init = function(views) {
        this.loadOverview(views);
    }

    this.loadOverview = function(views) {
        var _this = this;
        _this._mainController.serverFacade.getDropboxMonitorOverview(function(result) {
            _this._dropboxMonitorModel.dropboxes = result;
            _this._dropboxMonitorView.repaint(views);
        });
        
    }
    
    this.showLogsModal = function(dropboxName) {
        var _this = this;
        new DropboxLogsController(this._mainController, dropboxName).init();
    }
}