function UnarchivingHelperController(mainController) {
	this._mainController = mainController;
	this._unarchivingHelperModel = new UnarchivingHelperModel();
	this._unarchivingHelperView = new UnarchivingHelperView(this, this._unarchivingHelperModel);
	
	this.init = function(views) {
		var _this = this;
		_this._unarchivingHelperView.repaint(views);
	}

	this.getInfo = function(ids, callback) {
		mainController.serverFacade.getArchivingInfo(ids, function(info) {
			callback(info);
		});
	}

	this.unarchive = function(ids, callback) {
		mainController.serverFacade.unarchiveDataSets(ids, callback);
	}
}