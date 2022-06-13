function CustomImportView(customImportController, customImportModel) {
    this._controller = customImportController;
    this._model = customImportModel;
    
    this.repaint = function(views) {
        var _this = this;
        var $header = views.header;
        $header.append($("<h1>").append("Custom Import"));

        var $contents = views.content;
        var dropdownList = [];
        profile.customImportDefinitions.forEach(function(definition) {
            dropdownList.push({value:definition.code, label:definition.properties["name"]});
        });
        var $customImports = FormUtil.getDropdown(dropdownList, "Select a service");
        $customImports.attr('required', '');
        $contents.append(FormUtil.getFieldForComponentWithLabel($customImports, "Custom Import Service"));
        $contents.append($('<div>', { 'id' : 'APIUploader' } ));
        mainController.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), function(data) {
                _this._model.file = data.name;
                $("#filedrop").hide();
            }, {
            main_title: $('<legend>').text('Files Uploader'),
            uploads_title: $('<legend>').text('File list'),
            ondelete: function(file) {
                _this._model.file = null;
                $("#filedrop").show();
//                Uploader.reset();
            }
        });

        var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
            var serviceId = $customImports.val();
            if (!serviceId) {
                _this.showError("Choose a Custom Import Service.");
                return;
            }
            if (!_this._model.file) {
                _this.showError("Drop or choose a file.");
                return;
            }
            _this._controller.importFile(serviceId, function() {
                Util.showInfo("Custom import of file '" + _this._model.file 
                        + "' successfully submitted for service '" + serviceId + "'.");
                mainController._showCustomImportPage();
            });
        }, "Save", null, "save-btn");
        $saveBtn.removeClass("btn-default");
        $saveBtn.addClass("btn-primary");
        $header.append($saveBtn);
    }

    this.showError = function(message) {
        Util.blockUI();
        Util.showUserError(message, Util.unblockUI);

    }
    
}
