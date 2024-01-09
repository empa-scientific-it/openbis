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
        mainController.profile.beforeViewPaint(ViewType.PROJECT_FORM, this._projectFormModel, $container);
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
			title = "Update " + typeTitle + FormUtil.getProjectName(this._projectFormModel.project.code);
		} else {
			title = typeTitle + FormUtil.getProjectName(this._projectFormModel.project.code);
		}
		
		var $formTitle = $("<div>");
		$formTitle.append($("<h2>").append(title));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var dropdownOptionsModel = [];
		if(this._projectFormModel.mode === FormMode.VIEW) {
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
				    Util.blockUI();
					_this._projectFormController.enableEditing();
				}, "Edit", null, "edit-btn");
				toolbarModel.push({ component : $editBtn });
			}
			if(_this._allowedToDelete()) {
				//Delete
				dropdownOptionsModel.push({
                    label : "Delete",
                    action : function() {
                        _this._projectDeletionAction();
                    }
                });
			}

			//Print
			dropdownOptionsModel.push(FormUtil.getPrintPDFButtonModel("PROJECT",  _this._projectFormModel.project.permId));

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

            //History
            dropdownOptionsModel.push({
                label : "History",
                action : function() {
                    mainController.changeView('showProjectHistoryPage', _this._projectFormModel.project.permId);
                }
            });
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
            $formColumn.append(this._createOverviewSection(projectIdentifier, hideShowOptionsModel));
			$formColumn.append(this._createExperimentsSection(projectIdentifier, hideShowOptionsModel));
		}

		FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, hideShowOptionsModel, "PROJECT-VIEW");
		$header.append(FormUtil.getToolbar(toolbarModel));

		$container.append($form);
        mainController.profile.afterViewPaint(ViewType.PROJECT_FORM, this._projectFormModel, $container);
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
            $identificationInfo.append(FormUtil.getFieldForLabelWithText("Identifier", this._projectFormModel.v3_project.identifier.identifier));
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
			showByDefault : true,
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

    this._createOverviewSection = function(projectIdentifier, hideShowOptionsModel) {
        var $overview = $("<div>", { id : "project-overview" });
        $overview.append($("<legend>").append("Overview"));
        var $overviewContainer = $("<div>");
        $overview.append($overviewContainer);

        $experimentsOverview = $("<div>");
        $overviewContainer.append($("<h4>").append(ELNDictionary.ExperimentsELN));
        $overviewContainer.append($experimentsOverview);
        
        $samplesOverview = $("<div>");
        $header = $("<h4>").append(ELNDictionary.Samples);
        $overviewContainer.append($header);
        $overviewContainer.append($samplesOverview);
        
        var experimentTableController = new ExperimentTableController(this._projectFormController, null, jQuery.extend(true, {}, this._projectFormModel.project), true);
        experimentTableController.init($experimentsOverview);
        var sampleTableController = new SampleTableController(this._projectFormController, null, null, this._projectFormModel.project.permId, true, null, 40);
        var views = {
            header : $header,
            content : $samplesOverview
        }
        sampleTableController.init(views);

        $overview.hide();
        hideShowOptionsModel.push({
            label : "Overview",
            section : "#project-overview",
            beforeShowingAction : function() {
                experimentTableController.refresh();
                sampleTableController.refresh();
            }
        });
        return $overview;
    }

	this._createExperimentsSection = function(projectIdentifier, hideShowOptionsModel) {
		var entityKindName = ELNDictionary.getExperimentsDualName();
		var $experiments = $("<div>", { id : "project-experiments" });
		var $experimentsContainer = $("<div>");
		$experiments.append($("<legend>").append(entityKindName));
		$experiments.append($experimentsContainer);
		
        var _this = this;
        var extraOptions = [];
        extraOptions.push({ name : "Delete", action : function(selected) {
            if(selected != undefined && selected.length == 0){
                Util.showUserError("Please select at least one " + ELNDictionary.experimentELN + " to delete!");
            } else {
                _this._deleteExperiments(selected.map(e => e.permId));
            }
        }});
        extraOptions.push({ name : "Move", action : function(selected) {
            if(selected != undefined && selected.length == 0){
                Util.showUserError("Please select at least one " + ELNDictionary.experimentELN + " to move!");
            } else {
                _this._moveExperiments(selected.map(s => s.permId));
            }
        }});
        var experimentTableController = new ExperimentTableController(this._projectFormController, null, jQuery.extend(true, {}, this._projectFormModel.project), 
                false, extraOptions);
		experimentTableController.init($experimentsContainer);
		$experiments.hide();
		hideShowOptionsModel.push({
			label : entityKindName,
			section : "#project-experiments",
			beforeShowingAction : function() {
				experimentTableController.refresh();
			}
		});
		return $experiments;
	}

    this._deleteExperiments = function(permIds) {
        var _this = this;
        var $component = $("<div>");
        var warningText = "Also all " + ELNDictionary.samples + " and data sets of the selected " 
                + permIds.length + " " + ELNDictionary.getExperimentsDualName() + " will be deleted.";
        var $warning = FormUtil.getFieldForLabelWithText(null, warningText);
        $warning.css('color', FormUtil.warningColor);
        $component.append($warning);
        var modalView = new DeleteEntityController(function(reason) {
            require(["as/dto/experiment/id/ExperimentPermId","as/dto/experiment/delete/ExperimentDeletionOptions"],
                function(ExperimentPermId, ExperimentDeletionOptions) {
                    var experimentIds = permIds.map(permId => new ExperimentPermId(permId));
                    var deletionOptions = new ExperimentDeletionOptions();
                    deletionOptions.setReason(reason);
                    mainController.openbisV3.deleteExperiments(experimentIds, deletionOptions).done(function() {
                        Util.showSuccess("All " + permIds.length + " " + ELNDictionary.getExperimentsDualName() 
                                + " are moved to trashcan", function() {
                            permIds.forEach(function(permId) {
                                mainController.sideMenu.deleteNodeByEntityPermId("EXPERIMENT", permId, false);
                            });
                            mainController.refreshView();
                        });
                    }).fail(function(error) {
                        Util.showFailedServerCallError(error);
                        Util.unblockUI();
                    });
                })
            }, true, null, $component);
        modalView.init();
    }

    this._moveExperiments = function(permIds) {
        var _this = this;
        var $window = $('<form>', { 'action' : 'javascript:void(0);' });
        var project = null;
        $window.submit(function() {
            Util.unblockUI();
            require(["as/dto/experiment/id/ExperimentPermId", "as/dto/experiment/update/ExperimentUpdate"], 
                    function(ExperimentPermId, ExperimentUpdate) {
                        var projectIdentifier = project.getIdentifier();
                        var updates = [];
                        permIds.forEach(function(permId) {
                            var update = new ExperimentUpdate();
                            update.setExperimentId(new ExperimentPermId(permId));
                            update.setProjectId(projectIdentifier);
                            updates.push(update);
                        });
                        mainController.openbisV3.updateExperiments(updates).done(function() {
                            Util.showSuccess("Moved successfully", function() {
                                var projectPermId = project.getPermId().getPermId();
                                mainController.sideMenu.refreshNodeParentByPermId("PROJECT", projectPermId, true);
                                permIds.forEach(function(permId) {
                                    mainController.sideMenu.deleteNodeByEntityPermId("EXPERIMENT", permId, false);
                                });
                                mainController.refreshView();
                            });
                        }).fail(function(error) {
                            Util.showFailedServerCallError(error);
                            Util.unblockUI();
                        });
                    });
        });

        $window.append($('<legend>').append("Moving " + permIds.length + " selected " 
                + ELNDictionary.getExperimentsDualName() + " to:"));
        var $searchBox = $('<div>');
        $window.append($searchBox);
        var searchDropdown = new AdvancedEntitySearchDropdown(false, true, "search project to move to",
                false, false, false, true, false);
        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });
        var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
        $btnCancel.click(function() {
            Util.unblockUI();
        });

        $window.append('<br>').append($btnAccept).append('&nbsp;').append($btnCancel);
        searchDropdown.onChange(function(selected) {
            project = selected[0];
        });

        searchDropdown.init($searchBox);

        var css = {
                'text-align' : 'left',
                'top' : '15%',
                'width' : '70%',
                'left' : '15%',
                'right' : '20%',
                'overflow' : 'hidden'
        };
        Util.blockUI($window, css);

    }

    this._projectDeletionAction = function() {
        var _this = this;
        this._projectFormController.getDependentEntities(function(experiments, samples) {
            var numberOfExperiments = experiments.length;
            var numberOfSamples = samples.length;
            if (numberOfExperiments > 0 || numberOfSamples > 0) {
                var $component = $("<div>");
                var warningText = "This project can not be deleted because it has ";
                if (numberOfExperiments == 1) {
                    warningText += "one " + ELNDictionary.ExperimentELN;
                } else if (numberOfExperiments > 1) {
                    warningText += numberOfExperiments + " " + ELNDictionary.ExperimentsELN;
                }
                if (numberOfExperiments > 0 && numberOfSamples > 0) {
                    warningText += " and ";
                }
                if (numberOfSamples == 1) {
                    warningText += " one " + ELNDictionary.Sample;
                } else if (numberOfSamples > 1) {
                    warningText += numberOfSamples + " " + ELNDictionary.Samples;
                }
                var $warning = FormUtil.getFieldForLabelWithText(null, warningText + ".");
                $warning.css('color', FormUtil.warningColor);
                $component.append($warning);
                var deleteEntityController = new DeleteEntityController(function(reason) {
                    _this._projectFormController.deleteDependentEntities(reason, experiments, samples);
                }, true, null, $component);
                deleteEntityController.setNumberOfEntities(numberOfExperiments + numberOfSamples);
                deleteEntityController.setAdditionalTest("After removal of the entities from the Trashcan, you will be able to delete this Project.");
                deleteEntityController.init();
            } else {
                var modalView = new DeleteEntityController(function(reason) {
                    _this._projectFormController.deleteProject(reason);
                }, true, null, $component);
                modalView.init();
            }
        });
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
