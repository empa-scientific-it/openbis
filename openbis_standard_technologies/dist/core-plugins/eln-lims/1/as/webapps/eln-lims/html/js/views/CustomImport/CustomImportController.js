function CustomImportController(parentController) {
    this._parentController = parentController;
    this._model = new CustomImportModel();
    this._view = new CustomImportView(this, this._model);
    
    this.init = function(views) {
        this._view.repaint(views);
    }
    
    this.importFile = function(customServiceId, callback) {
        Util.blockUI();
        var parameters = {
                "method": "dropDataSet",
                "dropBoxName": customServiceId,
                "fileName": this._model.file
        };
        mainController.serverFacade.createReportFromAggregationService(profile.allDataStores[0].code, parameters, function(response) {
            if(response.error) { //Error Case 1
                Util.showError(response.error.message, Util.unblockUI);
            } else if (response.result.columns[1].title === "Error") { //Error Case 2
                var stacktrace = response.result.rows[0][1].value;
                Util.showStacktraceAsError(stacktrace);
            } else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
                callback();
            } else { //This should never happen
                Util.showError("Unknown Error.", Util.unblockUI);
            }
        });

    }
}
