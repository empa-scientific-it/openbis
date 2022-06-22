function CustomImportView(customImportController, customImportModel) {
    this._controller = customImportController;
    this._model = customImportModel;
    
    this.repaint = function(views) {
        var _this = this;
        var $header = views.header;
        $header.append($("<h1>").append("Custom Import"));

        var $contents = views.content;
        var definitionsById = {};
        var dropdownList = [];
        profile.customImportDefinitions.forEach(function(definition) {
            definitionsById[definition.code] = definition;
            var item = {value:definition.code, label:definition.properties["name"]};
            if (definition.properties["description"]) {
                item.tooltip = definition.properties["description"];
            }
            dropdownList.push(item);
        });
        var $customImports = FormUtil.getDropdown(dropdownList, "Select a service");
        $customImports.on("change", function(event) {
            $("#template-link").empty();
            var serviceId = $customImports.val();
            var definition = definitionsById[serviceId];
            var templateEntityKind = definition.properties["template-entity-kind"];
            var templateEntityPermid = definition.properties["template-entity-permid"];
            var templateAttachmentName = definition.properties["template-attachment-name"];
            if (templateEntityKind && templateEntityPermid && templateAttachmentName) {
                var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent;'});
                var $templateLink = $("<a>").text("Download");
                $templateLink.on("click", function() {
                    _this._controller.getAttachment(templateEntityKind, templateEntityPermid, templateAttachmentName,
                            function(attachment) {
                                Util.download(attachment.getContent(), "application/octet-stream", true, attachment.getFileName());
                            });
                });
                $component.append($templateLink);
                var $linkGroup = FormUtil.getFieldForComponentWithLabel($component, 'Template');
                $("#template-link").append($linkGroup);
            }
        });
        $customImports.attr('required', '');
        $contents.append(FormUtil.getFieldForComponentWithLabel($customImports, "Custom Import Service"));
        $contents.append($('<div>', { 'id' : 'template-link' } ));
        $contents.append($('<div>', { 'id' : 'APIUploader' } ));
        mainController.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), function(data) {
                _this._model.file = data.name;
                $("#filedrop").hide();
            }, {
            main_title: $('<legend>').text('Files Uploader'),
            uploads_title: $('<legend>').text('File list'),
            singleFile: true,
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
            var definition = definitionsById[serviceId];
            var dropbox = definition.properties["dropbox-name"];
            _this._controller.importFile(dropbox, function() {
                Util.showInfo("Custom import of file '" + _this._model.file 
                        + "' successfully submitted for service '" + serviceId + "' (dropbox '" + dropbox + "').");
                mainController._showCustomImportPage();
            });
        }, "Import", null, "save-btn");
        $saveBtn.removeClass("btn-default");
        $saveBtn.addClass("btn-primary");
        $header.append($saveBtn);
    }

    this.showError = function(message) {
        Util.blockUI();
        Util.showUserError(message, Util.unblockUI);

    }
    
}
