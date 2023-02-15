function UserManagementConfigController(mainController, mode) {
    this._mainController = mainController;
    this._userManagementConfigModel = new UserManagementConfigModel(mode);
    this._userManagementConfigView = new UserManagementConfigView(this, this._userManagementConfigModel);

    this.init = function(views) {
        this._userManagementConfigView.repaint(views);
    }

    this.save = function(config, callback) {
        try {
            JSON.parse(config);
        } catch (error) {
            Util.showError(error);
            return;
        }
        profile.userManagementMaintenanceTaskConfig = config;
        Util.blockUI();
        this._mainController.serverFacade.saveUserManagementMaintenanceTaskConfig(config, function() {
            Util.showSuccess("Successfully saved configuration of the User Management Maintenance Task.", function() {
                Util.unblockUI();
                callback();
            });
        });
    }

    this.saveAndExecute = function(config, callback) {
        var _this = this;
        var serverFacade = this._mainController.serverFacade;
        this.save(config, function() {
            Util.blockUI();
            serverFacade.executeUserManagementMaintenanceTask(function(executionId) {
                var executionView = new UserManagementExecutionView(callback);
                executionView.init();
                var _polling = function() {
                    serverFacade.getUserManagementMaintenanceTaskReport(executionId, function(report) {
                        var log = report[0];
                        var finishedMarker = "UserManagementMaintenanceTask - finished";
                        var index = log.indexOf(finishedMarker);
                        if (index > 0) {
                            executionView.updateLog(log.substring(0, index + finishedMarker.length));
                            executionView.finished(report[1], report[2]);
                            serverFacade.removeUserManagementMaintenanceTaskReport(executionId);
                        } else {
                            executionView.updateLog(log);
                            setTimeout(_polling, 1000);
                        }
                    });
                };
                _polling();
            });
        });
    }

    this.isDirty = function() {
        return this._userManagementConfigModel.isFormDirty;
    }
}