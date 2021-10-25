function UserManagementConfigView(userManagementConfigController, userManagementConfigModel) {
    this._userManagementConfigController = userManagementConfigController;
    this._userManagementConfigModel = userManagementConfigModel;

    this.repaint = function(views) {
        var _this = this;
        var $header = views.header;
        var $container = views.content;
        $container.empty();
        
        $header.append($("<h1>").append("User Management Config"));
        var $textarea = $('<textarea>', {'id' : 'userManagementConfigText', 
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
            //Save & Execute
            var $saveAndExecuteBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
                _this._userManagementConfigController.saveAndExecute($textarea.val(), function() {
                    _this._userManagementConfigModel.isFormDirty = false;
                    mainController.changeView("showUserManagementConfigPage", FormMode.VIEW);
                });
            }, "Save & Execute", null, "save-and-execute-btn");
            toolbarModel.push({ component : $saveAndExecuteBtn });
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

function UserManagementExecutionView(callback) {
    this.$title;
    this.$textarea;
    this.$wait;
    this.$logButton;
    this.log;
    this.$auditButton;
    this.auditLog;
    this.$errorReportButton;
    this.errorReport;
    this.callback = callback;

    this.init = function() {
        var _this = this;
        var $dialog = $("<div>")
        $dialog.append($('<legend>').append('User Management Task Execution'));
        this.$title = $("<div>").text('Excerpt from the AS log file:')
        $dialog.append(this.$title);
        this.$textarea = $("<textarea>", {'id' : 'userManagementMaintenanceTaskLog', 'rows' : 16,
            'readonly' : 'readonly', 'style' : 'white-space: pre; font-size: small; width: 100%; resize: none'});
        $dialog.append(this.$textarea);

        $buttons = $("<div>", {'style' : 'margin-top: 10px'});
        $dialog.append($buttons);
        this.$wait = $("<span>", {'style' : 'color: red; text-align: center'});
        this.$wait.append("Please, wait until task has been finished.");
        $buttons.append(this.$wait);
        this.$logButton = FormUtil.getButtonWithText("Show AS Log", function() {
            _this.$title.text("AS log:");
            _this.$textarea.val(_this.log);
        });
        this.$logButton.hide();
        $buttons.append(this.$logButton);
        this.$auditButton = FormUtil.getButtonWithText("Show Audit Log", function() {
            _this.$title.text("Audit log:");
            _this.$textarea.val(_this.auditLog);
        });
        this.$auditButton.hide();
        $buttons.append('&nbsp;').append(this.$auditButton);
        this.$errorReportButton = FormUtil.getButtonWithText("Show Error Report", function() {
            _this.$title.text("Error report:");
            _this.$textarea.val(_this.errorReport);
        });
        this.$errorReportButton.hide();
        $buttons.append('&nbsp;').append(this.$errorReportButton);
        $closeButton = FormUtil.getButtonWithText("Close", function() {
            Util.unblockUI();
            _this.callback();
        });
        $closeButton.css("float", "right");
        $buttons.append($closeButton);

        Util.blockUI($dialog, {'text-align': 'left', 'left': '10%', 'top': '10%', 'width': '80%'});
    }

    this.updateLog = function(newLog) {
        this.log = newLog;
        this.$textarea.val(newLog);
    }
    
    this.finished = function(auditLog, errorReport) {
        this.$wait.hide();
        this.auditLog = auditLog;
        this.$logButton.show();
        this.$auditButton.show();
        if (errorReport !== "") {
            this.errorReport = errorReport;
            this.$errorReportButton.show();
            alert("Execution finished with an error.");
        }
    }
}
