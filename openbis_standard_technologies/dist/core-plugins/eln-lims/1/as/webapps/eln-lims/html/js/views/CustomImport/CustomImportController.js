function CustomImportController(parentController) {
    this._parentController = parentController;
    this._model = new CustomImportModel();
    this._view = new CustomImportView(this, this._model);

    this.init = function(views) {
        this._view.repaint(views);
    }

    this.importFile = function(dropbox, callback) {
        Util.blockUI();
        var parameters = {
                "method": "dropDataSet",
                "dropBoxName": dropbox,
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

    this.getAttachment = function(entityKind, entityPermid, attachmentName, callback) {
        Util.blockUI();
        var handleAttachment = function(entity) {
            if (entity) {
                attachments = entity.getAttachments().filter(a => a.getFileName() === attachmentName);
                if (attachments.length > 0) {
                    Util.unblockUI();
                    callback(attachments[0]);
                } else {
                    Util.showError("There is no attachment file with name '" + attachmentName + " in " + entityKind 
                            + " with perm ID " + entityPermid + ".", Util.unblockUI);
                }
            } else {
                Util.showError("There is no " + entityKind + " with perm ID " + entityPermid + ".", Util.unblockUI);
            }
        };
        if (entityKind === "PROJECT") {
            mainController.serverFacade.getProjectWithAttachments(entityPermid, handleAttachment);
        } else if (entityKind === "EXPERIMENT") {
            mainController.serverFacade.getExperimentWithAttachments(entityPermid, handleAttachment);
        } else if (entityKind === "SAMPLE") {
            mainController.serverFacade.getSampleWithAttachments(entityPermid, handleAttachment);
        } else {
            Util.showError("Unknwon entity kind " +  entityKind 
                    + ". Allowed values are PROJECT, EXPERIMENT, and SAMPLE.", Util.unblockUI);
        } 
    }

}
