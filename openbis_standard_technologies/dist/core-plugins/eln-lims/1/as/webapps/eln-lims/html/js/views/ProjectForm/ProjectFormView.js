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

function ProjectFormView(projectFormController, projectFormModel) {
	this._projectFormController = projectFormController;
	this._projectFormModel = projectFormModel;
	
	this.repaint = function(views) {
		var $container = views.content;
		var _this = this;
		var projectIdentifier = IdentifierUtil.getProjectIdentifier(_this._projectFormModel.project.spaceCode, _this._projectFormModel.project.code);
		var $form = $("<div>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		
		$form.append($formColumn);

		//
		// Title
		//
		var title = null;
		var isInventoryProject = this._projectFormModel.project && profile.isInventorySpace(this._projectFormModel.project.spaceCode);
		var typeTitle = "Project: ";
		
		if(this._projectFormModel.mode === FormMode.CREATE) {
			title = "Create " + typeTitle;
		} else if (this._projectFormModel.mode === FormMode.EDIT) {
			title = "Update " + typeTitle + Util.getDisplayNameFromCode(this._projectFormModel.project.code);
		} else {
			title = typeTitle + Util.getDisplayNameFromCode(this._projectFormModel.project.code);
		}
		
		var $formTitle = $("<div>");
		$formTitle.append($("<h2>").append(title));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var dropdownOptionsModel = [];
		if(this._projectFormModel.mode === FormMode.VIEW) {
			var experimentKindName = ELNDictionary.getExperimentKindName(projectIdentifier);
			if (_this._allowedToCreateExperiments()) {
				//Create Experiment
				var experimentTypes = mainController.profile.getExperimentTypes();
				FormUtil.addCreationDropdown(toolbarModel, experimentTypes, ["DEFAULT_EXPERIMENT", "COLLECTION"], function(typeCode) {
					return function() {
						Util.blockUI();
						setTimeout(function() {
							_this._projectFormController.createNewExperiment(typeCode);
						}, 100);
					}
				});
			}
			if (_this._allowedToMove()) {
                //Move
				dropdownOptionsModel.push({
                    label : "Move",
                    action : function() {
                        var moveEntityController = new MoveEntityController("PROJECT", _this._projectFormModel.project.permId);
                        moveEntityController.init();
                    }
                });
            }
			if(_this._allowedToEdit()) {
				//Edit
				var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
					_this._projectFormController.enableEditing();
				}, "Edit", null, "edit-btn");
				toolbarModel.push({ component : $editBtn });
			}
			if(_this._allowedToDelete()) {
				//Delete
				dropdownOptionsModel.push({
                    label : "Delete",
                    action : function() {
                        var modalView = new DeleteEntityController(function(reason) {
					        _this._projectFormController.deleteProject(reason);
                        }, true);
                        modalView.init();
                    }
                });
			}
			
			//Export
			dropdownOptionsModel.push({
                label : "Export Metadata",
                action : FormUtil.getExportAction([{ type: "PROJECT", permId : _this._projectFormModel.project.permId, expand : true }], true)
            });

            dropdownOptionsModel.push({
                label : "Export Metadata & Data",
                action : FormUtil.getExportAction([{ type: "PROJECT", permId : _this._projectFormModel.project.permId, expand : true }], false)
            });

			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
			    dropdownOptionsModel.push({
                    label : "New Jupyter notebook",
                    action : function () {
                        var jupyterNotebook = new JupyterNotebookController(_this._projectFormModel.project);
                        jupyterNotebook.init();
                    }
                });
			}

			// authorization
			if (this._projectFormModel.roles.indexOf("ADMIN") > -1 ) {
				dropdownOptionsModel.push({
                    label : "Manage access",
                    action : function () {
                        FormUtil.showAuthorizationDialog({
                            project: _this._projectFormModel.project,
                        });
                    }
                });
			}

            //Freeze
            if(_this._projectFormModel.v3_project && _this._projectFormModel.v3_project.frozen !== undefined) { //Freezing available on the API
                var isEntityFrozen = _this._projectFormModel.v3_project.frozen;
                if(isEntityFrozen) {
                    var $freezeButton = FormUtil.getFreezeButton("PROJECT", this._projectFormModel.v3_project.permId.permId, "Entity Frozen");
                    toolbarModel.push({ component : $freezeButton, tooltip: "Entity Frozen" });
                } else {
                    dropdownOptionsModel.push({
                        label : "Freeze Entity (Disable further modifications)",
                        action : function() {
                            FormUtil.showFreezeForm("PROJECT", _this._projectFormModel.v3_project.permId.permId);
                        }
                    });
                }

            }
		} else {
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._projectFormController.updateProject();
			}, "Save", null, "save-btn");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn });
		}
		
		var $header = views.header;
		$header.append($formTitle);

		var hideShowOptionsModel = [];

		$formColumn.append(this._createIdentificationInfoSection(hideShowOptionsModel));

		if(this._projectFormModel.isSimpleFolder && this._projectFormModel.mode === FormMode.CREATE) {
		    //
		} else {
            $formColumn.append(this._createDescriptionSection(hideShowOptionsModel));
		}

		if (this._projectFormModel.mode !== FormMode.CREATE && !isInventoryProject) {
			$formColumn.append(this._createExperimentsSection(projectIdentifier, hideShowOptionsModel));
			$formColumn.append(this._createSamplesSection(hideShowOptionsModel));
		}

		FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, hideShowOptionsModel, "PROJECT-VIEW");
		$header.append(FormUtil.getToolbar(toolbarModel));

		$container.append($form);
	};
	
	this._createIdentificationInfoSection = function(hideShowOptionsModel) {
		hideShowOptionsModel.push({
			forceToShow : this._projectFormModel.mode === FormMode.CREATE,
			label : "Identification Info",
			section : "#project-identification-info"
		});
		
		var _this = this;
		var $identificationInfo = $("<div>", { id : "project-identification-info" });

        $identificationInfo.append($("<legend>").append("Identification Info"));
        if (this._projectFormModel.mode !== FormMode.CREATE) {
            $identificationInfo.append(FormUtil.getFieldForLabelWithText("PermId", this._projectFormModel.project.permId));
		}

		var spaceCode = this._projectFormModel.project.spaceCode;
		if (this._projectFormModel.mode !== FormMode.CREATE) {
			var entityPath = FormUtil.getFormPath(spaceCode, this._projectFormModel.project.code);
			$identificationInfo.append(FormUtil.getFieldForComponentWithLabel(entityPath, "Path"));
		}

		if(this._projectFormModel.mode !== FormMode.CREATE) {
		    $identificationInfo.append(FormUtil.getFieldForLabelWithText("Space", spaceCode));
        }

		if (this._projectFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', "project-code-id", "Project Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				var caretPosition = this.selectionStart;
				textField.val(textField.val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._projectFormModel.project.code = textField.val();
				_this._projectFormModel.isFormDirty = true;
			});
			$identificationInfo.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
		} else {
			$identificationInfo.append(FormUtil.getFieldForLabelWithText("Code", this._projectFormModel.project.code));
		}

		if(this._projectFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._projectFormModel.project.registrationDetails;

			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$identificationInfo.append($registrator);

			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)));
			$identificationInfo.append($registationDate);

			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$identificationInfo.append($modifier);

			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$identificationInfo.append($modificationDate);
		}
		$identificationInfo.hide();
		return $identificationInfo;
	}
	
	this._createDescriptionSection = function(hideShowOptionsModel) {
		hideShowOptionsModel.push({
			forceToShow : this._projectFormModel.mode === FormMode.CREATE,
			label : "Description",
			section : "#project-description"
		});
		
		var _this = this;
		var $description = $("<div>", { id : "project-description" });
		$description.append($("<legend>").append("General"));
		var description = Util.getEmptyIfNull(this._projectFormModel.project.description);
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var $textBox = FormUtil._getTextBox("description-id", "Description", false);
			var textBoxEvent = function(jsEvent, newValue) {
				var valueToUse = null;
				if (newValue !== undefined && newValue !== null) {
					valueToUse = newValue;
				} else {
					valueToUse = $(this).val();
				}
				_this._projectFormModel.project.description = valueToUse;
				_this._projectFormModel.isFormDirty = true;
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
	
	this._createExperimentsSection = function(projectIdentifier, hideShowOptionsModel) {
		var entityKindName = ELNDictionary.getExperimentKindName(projectIdentifier, true);
		var $experiments = $("<div>", { id : "project-experiments" });
		var $experimentsContainer = $("<div>");
		$experiments.append($("<legend>").append(entityKindName));
		$experiments.append($experimentsContainer);
		
		var experimentTableController = new ExperimentTableController(this._projectFormController, null, jQuery.extend(true, {}, this._projectFormModel.project), true);
		experimentTableController.init($experimentsContainer);
		$experiments.hide();
		hideShowOptionsModel.push({
			label : entityKindName,
			section : "#project-experiments",
			beforeShowingAction : function() {
				experimentTableController.refreshHeight();
			}
		});
		return $experiments;
	}
	
	this._createSamplesSection = function(hideShowOptionsModel) {
		var entityKindName = "" + ELNDictionary.Samples + "";
		
		var $samples = $("<div>", { id : "project-samples" });
		var $experimentsContainer = $("<div>");
		$samples.append($("<legend>").append(entityKindName));
		var $samplesContainerHeader = $("<div>");
		$samples.append($samplesContainerHeader);
		var $samplesContainer = $("<div>");
		$samples.append($samplesContainer);
		
		var views = {
				header : $samplesContainerHeader,
				content : $samplesContainer
		}
		var sampleTableController = new SampleTableController(this._projectFormController, null, null, this._projectFormModel.project.permId, true, null, 40);
		sampleTableController.init(views);
		$samples.hide();
		hideShowOptionsModel.push({
			label : entityKindName,
			section : "#project-samples",
			beforeShowingAction : function() {
				sampleTableController.refreshHeight();
			}
		});
		return $samples;
	}
	
	this._allowedToCreateExperiments = function() {
		var project = this._projectFormModel.v3_project;
		return project.frozenForExperiments == false && this._projectFormModel.experimentRights.rights.indexOf("CREATE") >= 0;
	};
	
	this._allowedToEdit = function() {
		var project = this._projectFormModel.v3_project;
		return project.frozen == false && this._allowedToUpdate(this._projectFormModel.rights);
	};
	
	this._allowedToUpdate = function(rights) {
		return rights && rights.rights.indexOf("UPDATE") >= 0;
	}

	this._allowedToMove = function() {
		var project = this._projectFormModel.v3_project;
		if (project.frozen || project.space.frozenForProjects) {
			return false;
		}
		return this._allowedToUpdate(this._projectFormModel.rights);
	};
	
	this._allowedToDelete = function() {
		var project = this._projectFormModel.v3_project;
        return (project.frozen == false && project.space.frozenForProjects == false)
                && this._projectFormModel.rights.rights.indexOf("DELETE") >= 0;
	};
}
