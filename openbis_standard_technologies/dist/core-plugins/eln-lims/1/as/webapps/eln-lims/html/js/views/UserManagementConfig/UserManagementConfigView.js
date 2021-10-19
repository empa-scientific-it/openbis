function UserManagementConfigView(userManagementConfigController, userManagementConfigModel) {
    this._userManagementConfigController = userManagementConfigController;
    this._userManagementConfigModel = userManagementConfigModel;

    this.repaint = function(views) {
        var _this = this;
        var $header = views.header;
        var $container = views.content;
        $container.empty();
        
        $header.append($("<h1>").append("User Management Config"));
        var $textarea = $('<textarea>', {'id' : 'userManagementConfigTest', 
            'style' : 'height: 100%; width: 100%; resize: none', 'class' : 'form-control'});
        $textarea.val(profile.userManagementMaintenanceTaskConfig);
        $textarea.change(function(event) {
            _this._userManagementConfigModel.isFormDirty = true;
        });
        if (this._userManagementConfigModel.mode !== FormMode.EDIT) {
            $textarea.attr('readonly','readonly');
        }

        //
        // Toolbar
        //
        var toolbarModel = [];
        if (this._userManagementConfigModel.mode === FormMode.EDIT) {
            //Save
            var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
                _this._userManagementConfigController.save($textarea.val(), function() {
                    _this._userManagementConfigModel.isFormDirty = false;
                    mainController.changeView("showUserManagementConfigPage", FormMode.VIEW);
                });
            }, "Save", null, "save-btn");
            $saveBtn.removeClass("btn-default");
            $saveBtn.addClass("btn-primary");
            toolbarModel.push({ component : $saveBtn });
        } else {
            //Edit
            var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
                mainController.changeView("showUserManagementConfigPage", FormMode.EDIT);
            }, "Edit", null, "edit-btn");
            toolbarModel.push({ component : $editButton });
        }
        $header.append(FormUtil.getToolbar(toolbarModel));
        $container.append(FormUtil.getInfoBox("This is an expert feature which allows to change the configuration file "
                + "of the User Management Maintenance Task. For more details see " 
                + "<a href='" + profile.docuBaseUrl + "/User+Group+Management+for+Multi-groups+openBIS+Instances'>"
                + profile.docuBaseUrl + "/User+Group+Management+for+Multi-groups+openBIS+Instances</a>",
                []));
        $container.append($("<br>"));
        $container.append($textarea);
    }
}