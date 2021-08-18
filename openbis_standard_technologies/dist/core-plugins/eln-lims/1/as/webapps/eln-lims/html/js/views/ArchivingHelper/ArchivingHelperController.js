function ArchivingHelperController(mainController) {
    this._mainController = mainController;
    this._archivingHelperModel = new ArchivingHelperModel();
    this._archivingHelperView = new ArchivingHelperView(this, this._archivingHelperModel);
    
    this.init = function(views) {
        var _this = this;
        _this._archivingHelperView.repaint(views);
    }

    this.archive = function(dataSets, callback) {
        Util.requestArchiving(dataSets, callback);
    }
}
