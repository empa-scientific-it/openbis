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
		
		var $form = $("<div>");
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var typeTitle = "Space: ";
		
		var $formTitle = $("<h2>").append(typeTitle + Util.getDisplayNameFromCode(this._spaceFormModel.space));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var dropdownOptionsModel = [];

		if (_this._allowedToCreateProject()) {
			var $createProj = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
				_this._spaceFormController.createProject();
			}, "New Project", null, "create-btn");
			toolbarModel.push({ component : $createProj});
		}

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

        // deletion
        if (this._allowedToDeleteSpace()) {
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

		var $header = views.header;
		$header.append($formTitle);
        FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, [], "SPACE-VIEW");
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		$container.append($form);
	}
	
	this._allowedToCreateProject = function() {
		var space = this._spaceFormModel.v3_space;
		return space.frozenForProjects == false && this._spaceFormModel.projectRights.rights.indexOf("CREATE") >= 0;
	};
	
    this._allowedToDeleteSpace = function() {
        var space = this._spaceFormModel.v3_space;
        return space.frozen == false && profile.isAdmin;
    };
}
