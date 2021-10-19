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

    this.isDirty = function() {
        return this._userManagementConfigModel.isFormDirty;
    }
}