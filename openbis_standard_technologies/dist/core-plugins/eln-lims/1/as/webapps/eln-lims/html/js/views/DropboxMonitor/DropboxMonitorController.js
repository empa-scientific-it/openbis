function DropboxMonitorController(mainController) {
    this._mainController = mainController;
    this._dropboxMonitorModel = new DropboxMonitorModel();
    this._dropboxMonitorView = new DropboxMonitorView(this, this._dropboxMonitorModel);

    this.init = function(views) {
        this._loadOverview(views);
    }

    this._loadOverview = function(views) {
        var _this = this;
        _this._mainController.serverFacade.getDropboxMonitorOverview(function(result) {
            _this._dropboxMonitorModel.dropboxes = result.result.rows;
            _this._dropboxMonitorView.repaint(views);
        });
        
    }
}