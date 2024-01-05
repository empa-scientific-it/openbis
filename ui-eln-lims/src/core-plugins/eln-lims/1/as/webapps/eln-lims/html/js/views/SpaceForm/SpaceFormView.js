/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function SpaceFormView(spaceFormController, spaceFormModel) {
	this._spaceFormController = spaceFormController;
	this._spaceFormModel = spaceFormModel;
	
	this.repaint = function(views) {
		var _this = this;
		var $container = views.content;
        mainController.profile.beforeViewPaint(ViewType.SPACE_FORM, this._spaceFormModel, $container);
		var $form = $("<div>");
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var typeTitle = (spaceFormModel.isInventory ? "Inventory " : "") + "Space: ";
		
        if (this._spaceFormModel.mode === FormMode.CREATE) {
            title = "Create " + typeTitle;
        } else if (this._spaceFormModel.mode === FormMode.EDIT) {
            title = "Update " + typeTitle + Util.getDisplayNameFromCode(this._spaceFormModel.space);
        } else {
            title = typeTitle + Util.getDisplayNameFromCode(this._spaceFormModel.space);
        }
        var $formTitle = $("<h2>").append(title);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var dropdownOptionsModel = [];
        if (this._spaceFormModel.mode === FormMode.VIEW) {
            
            if (_this._allowedToCreateProject()) {
                var $createProj = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
                    _this._spaceFormController.createProject();
                }, "New Project", null, "create-btn");
                toolbarModel.push({ component : $createProj});
            }
            
            if (this._allowedToEditSpace()) {
                // edit
                var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
                    _this._spaceFormController.enableEditing();
                }, "Edit", null, "edit-btn");
                toolbarModel.push({ component : $editBtn });
            }
            
            if (this._allowedToDeleteSpace()) {
                // deletion
                dropdownOptionsModel.push({
                    label : "Delete",
                    action : function() {
                        var modalView = new DeleteEntityController(function(reason) {
                            _this._spaceFormController.deleteSpace(reason);
                        }, true);
                        modalView.init();
                    }
                });
            }

            //Print
            dropdownOptionsModel.push(FormUtil.getPrintPDFButtonModel("SPACE", _this._spaceFormModel.v3_space.permId.permId));

            //Export
            dropdownOptionsModel.push({
                label : "Export Metadata",
                action : FormUtil.getExportAction([{ type: "SPACE", permId : _this._spaceFormModel.space, expand : true }], true)
            });
            
            dropdownOptionsModel.push({
                label : "Export Metadata & Data",
                action : FormUtil.getExportAction([{ type: "SPACE", permId : _this._spaceFormModel.space, expand : true }], false)
            });
            
            //Jupyter Button
            if(profile.jupyterIntegrationServerEndpoint) {
                dropdownOptionsModel.push({
                    label : "New Jupyter notebook",
                    action : function () {
                        var jupyterNotebook = new JupyterNotebookController(_this._spaceFormModel.space);
                        jupyterNotebook.init();
                    }
                });
            }
            
            // authorization
            if (this._spaceFormModel.roles.indexOf("ADMIN") > -1 ) {
                dropdownOptionsModel.push({
                    label : "Manage access",
                    action : function () {
                        FormUtil.showAuthorizationDialog({
                            space: _this._spaceFormModel.space,
                        });
                    }
                });
            }
            
            //Freeze
            if(_this._spaceFormModel.v3_space && _this._spaceFormModel.v3_space.frozen !== undefined) { //Freezing available on the API
                var isEntityFrozen = _this._spaceFormModel.v3_space.frozen;
                if(isEntityFrozen) {
                    var $freezeButton = FormUtil.getFreezeButton("SPACE", _this._spaceFormModel.v3_space.permId.permId, isEntityFrozen);
                    toolbarModel.push({ component : $freezeButton, tooltip: "Entity Frozen" });
                } else {
                    dropdownOptionsModel.push({
                        label : "Freeze Entity (Disable further modifications)",
                        action : function() {
                            FormUtil.showFreezeForm("SPACE", _this._spaceFormModel.v3_space.permId.permId);
                        }
                    });
                }
            }
        } else {
            var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
                _this._spaceFormController.updateSpace();
            }, "Save", null, "save-btn");
            $saveBtn.removeClass("btn-default");
            $saveBtn.addClass("btn-primary");
            toolbarModel.push({ component : $saveBtn });
        }

		var $header = views.header;
		$header.append($formTitle);

        var hideShowOptionsModel = [];
        $formColumn.append(this._createIdentificationInfoSection(hideShowOptionsModel));
        $formColumn.append(this._createDescriptionSection(hideShowOptionsModel));
        FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, hideShowOptionsModel, "SPACE-VIEW");
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		$container.append($form);
        mainController.profile.afterViewPaint(ViewType.SPACE_FORM, this._spaceFormModel, $container);
	}
	
    this._createIdentificationInfoSection = function(hideShowOptionsModel) {
        hideShowOptionsModel.push({
            forceToShow : this._spaceFormModel.mode === FormMode.CREATE,
            label : "Identification Info",
            section : "#space-identification-info"
        });
        var _this = this;
        var $identificationInfo = $("<div>", { id : "space-identification-info" });
        $identificationInfo.append($("<legend>").append("Identification Info"));
        if (this._spaceFormModel.mode !== FormMode.CREATE) {
            var space = this._spaceFormModel.v3_space;
            $identificationInfo.append(FormUtil.getFieldForLabelWithText("PermId / Code", space.getCode()));
            var $registrator = FormUtil.getFieldForLabelWithText("Registrator", space.getRegistrator().userId);
            $identificationInfo.append($registrator);

            var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", 
                   Util.getFormatedDate(new Date(space.registrationDate)));
            $identificationInfo.append($registationDate);
        } else { // FormMode.CREATE
            var groupPrefixes = this._spaceFormController.getAllGroupPrefixes();
            if (groupPrefixes.length > 0) {
                var values = [{label:"(no group)", value:"", selected:true}];
                groupPrefixes.forEach(p => values.push({label:p, value:p}));
                var $prefixDropdown = FormUtil.getDropdown(values, "Select group");
                $prefixDropdown.on("change", function() {
                    _this._spaceFormController.setPrefix(this.value);
                    if ($fullCodeField) {
                        $fullCodeField.val(_this._spaceFormModel.space);
                    }
                });
                $identificationInfo.append(FormUtil.getInfoText("When assigning a group to a space, the group settings are applied to it."));
                $identificationInfo.append(FormUtil.getFieldForComponentWithLabel($prefixDropdown, "Group"));
            }
            var $textField = FormUtil._getInputField('text', "space-code-id", "Space Code", null, true);
            $textField.keyup(function(event){
                var textField = $(this);
                var caretPosition = this.selectionStart;
                textField.val(textField.val().toUpperCase());
                this.selectionStart = caretPosition;
                this.selectionEnd = caretPosition;
                _this._spaceFormController.setPostfix(textField.val());
                if ($fullCodeField) {
                    $fullCodeField.val(_this._spaceFormModel.space);
                }
            });
            $identificationInfo.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
            if (groupPrefixes.length > 0) {
                var $fullCodeField = FormUtil._getInputField('text');
                $fullCodeField.attr('disabled','disabled');
                $identificationInfo.append(FormUtil.getFieldForComponentWithLabel($fullCodeField, "Full Code"));
            }
            if (_this._spaceFormModel.isInventory) {
                var $readOnlyField = FormUtil._getBooleanField("readOnlyInventory", "Indicates if the space should be read-only for non admin users.");
                $readOnlyField.change(function() {
                    _this._spaceFormModel.isReadOnly = $($(this).children()[0]).children()[0].checked;
                });
                $identificationInfo.append(FormUtil.getFieldForComponentWithLabel($readOnlyField, "Read only"));
            }
        }
        $identificationInfo.hide();
        return $identificationInfo;
    }
    this._createDescriptionSection = function(hideShowOptionsModel) {
        hideShowOptionsModel.push({
            forceToShow : this._spaceFormModel.mode === FormMode.CREATE,
            label : "Description",
            section : "#space-description"
        });
        
        var _this = this;
        var $description = $("<div>", { id : "space-description" });
        $description.append($("<legend>").append("General"));
        var space = this._spaceFormModel.v3_space;
        var description = space ? space.getDescription() : "";
        if (this._spaceFormModel.mode !== FormMode.VIEW) {
            var $textBox = FormUtil._getTextBox("description-id", "Description", false);
            var textBoxEvent = function(jsEvent, newValue) {
                var valueToUse = null;
                if (newValue !== undefined && newValue !== null) {
                    valueToUse = newValue;
                } else {
                    valueToUse = $(this).val();
                }
                _this._spaceFormModel.description = valueToUse;
                _this._spaceFormModel.isFormDirty = true;
            };
            $textBox.val(description);
            $textBox = FormUtil.activateRichTextProperties($textBox, textBoxEvent, null, description, false);
            $description.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
        } else {
            var $textBox = FormUtil._getTextBox(null, "Description", false);
            $textBox = FormUtil.activateRichTextProperties($textBox, undefined, null, description, true);
            $description.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
        }
        $description.hide();
        return $description;
    }
    
	this._allowedToCreateProject = function() {
		var space = this._spaceFormModel.v3_space;
		return space.frozenForProjects == false && this._spaceFormModel.projectRights.rights.indexOf("CREATE") >= 0;
	};
	
    this._allowedToEditSpace = function() {
        var space = this._spaceFormModel.v3_space;
        return space.frozen == false && this._spaceFormModel.projectRights.rights.indexOf("CREATE") >= 0;
    };
    this._allowedToDeleteSpace = function() {
        var space = this._spaceFormModel.v3_space;
        return (space.frozen == false && profile.isAdmin && profile.inventorySpacesReadOnly.indexOf(space.code) < 0) && this._allowedToEditSpace();
    };
}
