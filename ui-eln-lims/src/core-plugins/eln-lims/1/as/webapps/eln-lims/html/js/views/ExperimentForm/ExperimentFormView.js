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

function ExperimentFormView(experimentFormController, experimentFormModel) {
	this._experimentFormController = experimentFormController;
	this._experimentFormModel = experimentFormModel;

	this.repaint = function(views) {
		var $container = views.content;
		mainController.profile.beforeViewPaint(ViewType.EXPERIMENT_FORM, this._experimentFormModel, $container);
		var _this = this;
	    var experimentTypeDefinitionsExtension = profile.experimentTypeDefinitionsExtension[_this._experimentFormModel.experiment.experimentTypeCode];

		var experimentTypeCode = this._experimentFormModel.experiment.experimentTypeCode;

		var $form = $("<span>");

		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
		});

		var $rightPanel = null;
		if(this._experimentFormModel.mode === FormMode.VIEW) {
			$rightPanel = views.auxContent;
		}

		$form.append($formColumn);

		//
		// Title
		//
		var $formTitle = $("<div>");
		var typeTitle = Util.getDisplayNameFromCode(this._experimentFormModel.experiment.experimentTypeCode);
		var title = "";
		if (this._experimentFormModel.mode === FormMode.CREATE) {
			title = "Create " + typeTitle;
		} else {
			var nameLabel = FormUtil.getExperimentName(this._experimentFormModel.experiment.code, this._experimentFormModel.experiment.properties)
			title = typeTitle + ": " + nameLabel;
			if (this._experimentFormModel.mode === FormMode.EDIT) {
				title = "Update " + title;
			}
		}
		$formTitle.append($("<h2>").append(title));

		//
		// Toolbar
		//
		var toolbarModel = [];
		var dropdownOptionsModel = [];
		if(this._experimentFormModel.mode === FormMode.VIEW) {
		    var toolbarConfig = profile.getExperimentTypeToolbarConfiguration(_this._experimentFormModel.experiment.experimentTypeCode);
			if (_this._allowedToCreateSample() && toolbarConfig.CREATE) {
				//Create Experiment Step
				var sampleTypes = FormUtil.getSampleTypesOnDropdowns(IdentifierUtil.getSpaceCodeFromIdentifier(_this._experimentFormModel.experiment.identifier));
				FormUtil.addCreationDropdown(toolbarModel, sampleTypes, ["ENTRY", "EXPERIMENTAL_STEP"], function(typeCode) {
					return function() {
						Util.blockUI();
						setTimeout(function() {
							var argsMap = {
								"sampleTypeCode" : typeCode,
								"experimentIdentifier" : _this._experimentFormModel.experiment.identifier
							};
							mainController.changeView("showCreateSubExperimentPage", JSON.stringify(argsMap));
						}, 100);
					}
				});
			}
			if (_this._allowedToEdit() && toolbarConfig.EDIT) {
				//Edit
				var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
                    Util.blockUI();
                    var exp = _this._experimentFormModel.experiment;
                    var args = encodeURIComponent('["' + exp.identifier + '","' + exp.experimentTypeCode + '"]');
                    mainController.changeView("showEditExperimentPageFromIdentifier", args);
				}, "Edit", null, "edit-btn");
				toolbarModel.push({ component : $editBtn });
			}
			if (_this._allowedToMove() && toolbarConfig.MOVE) {
				//Move
				dropdownOptionsModel.push({
                    label : "Move",
                    action : function() {
                        var moveEntityController = new MoveEntityController("EXPERIMENT", experimentFormModel.experiment.permId);
                        moveEntityController.init();
                    }
                });
			}
			if (_this._allowedToDelete() && toolbarConfig.DELETE) {
				//Delete
                var maxNumToShow = 10;
                var $component = $("<div>");
                var experiment = this._experimentFormModel.v3_experiment;
                var experimentKindName = ELNDictionary.getExperimentKindName(experiment.getType().getCode()).toLowerCase();
                var samples = experiment.samples;
                if (samples.length > 0) {
                    var warningText = "The " + experimentKindName + " has " + samples.length + " " 
                            + ELNDictionary.sample + "s, which will also be deleted:";
                    for (var cIdx = 0; cIdx < Math.min(maxNumToShow, samples.length); cIdx++) {
                        warningText += "\n  " + Util.getDisplayNameForEntity(samples[cIdx]);
                    }
                    if (maxNumToShow < samples.length) {
                        warningText += "\n  ...";
                    }
                    var $warning = FormUtil.getFieldForLabelWithText(null, warningText);
                    $warning.css('color', FormUtil.warningColor);
                    $component.append($warning);
                }
                var dataSets = experiment.dataSets;
                if (dataSets.length > 0) {
                    var warningText = "The " + experimentKindName + " has " + dataSets.length + " data sets " 
                            + "which will also be deleted:";
                    for (var cIdx = 0; cIdx < Math.min(maxNumToShow, dataSets.length); cIdx++) {
                        warningText += "\n  " + Util.getDisplayNameForEntity(dataSets[cIdx]);
                    }
                    if (maxNumToShow < dataSets.length) {
                        warningText += "\n  ...";
                    }
                    var $warning = FormUtil.getFieldForLabelWithText(null, warningText);
                    $warning.css('color', FormUtil.warningColor);
                    $component.append($warning);
                }
                dropdownOptionsModel.push({
                    label : "Delete",
                    action : function() {
                        var modalView = new DeleteEntityController(function(reason) {
                            _this._experimentFormController.deleteExperiment(reason);
                        }, true, null, $component);
                        modalView.init();
                    }
                });
			}

			//Print
			dropdownOptionsModel.push(FormUtil.getPrintPDFButtonModel("EXPERIMENT",  _this._experimentFormModel.experiment.permId));

			if(_this._allowedToRegisterDataSet()) {
			    if(toolbarConfig.UPLOAD_DATASET) {
                    //Create Dataset
                    var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-upload", function () {
                        Util.blockUI();
                        mainController.changeView('showCreateDataSetPageFromExpPermId',_this._experimentFormModel.experiment.permId);
                    }, "Upload", null, "upload-btn");
                    toolbarModel.push({ component : $uploadBtn });
	            }

	            if(toolbarConfig.UPLOAD_DATASET_HELPER) {
                    //Get dropbox folder name
                    dropdownOptionsModel.push({
                        label : "Dataset upload helper tool for eln-lims dropbox",
                        action : function() {
                            var space = IdentifierUtil.getSpaceCodeFromIdentifier(_this._experimentFormModel.experiment.identifier);
                            var project = IdentifierUtil.getProjectCodeFromExperimentIdentifier(_this._experimentFormModel.experiment.identifier);
                            var nameElements = [
                                "E",
                                space,
                                project,
                                _this._experimentFormModel.experiment.code,
                            ];
                            FormUtil.showDropboxFolderNameDialog(nameElements);
                        }
                    });

					dropdownOptionsModel.push({
						label : "Template for metadata import",
						action : function() {
							var templateLink = mainController.serverFacade.getTemplateLink("EXPERIMENT",
									experimentTypeCode, "REGISTRATION", "json");
							window.open(templateLink, "_blank").focus();
						}
					});
                }
			}

			//Export
			if(toolbarConfig.EXPORT_METADATA) {
                dropdownOptionsModel.push({
                    label : "Export Metadata",
                    action : FormUtil.getExportAction([{ type: "EXPERIMENT", permId : _this._experimentFormModel.experiment.permId, expand : true }], true)
                });
            }

            if(toolbarConfig.EXPORT_ALL) {
                dropdownOptionsModel.push({
                    label : "Export Metadata & Data",
                    action : FormUtil.getExportAction([{ type: "EXPERIMENT", permId : _this._experimentFormModel.experiment.permId, expand : true }], false)
                });
            }

			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
				dropdownOptionsModel.push({
                    label : "New Jupyter notebook",
                    action : function () {
                        var jupyterNotebook = new JupyterNotebookController(_this._experimentFormModel.experiment);
                        jupyterNotebook.init();
                    }
                });
			}

            //Freeze
            if(_this._experimentFormModel.v3_experiment && _this._experimentFormModel.v3_experiment.frozen !== undefined && toolbarConfig.FREEZE) { //Freezing available on the API
                var isEntityFrozen = _this._experimentFormModel.v3_experiment.frozen;
                if(isEntityFrozen) {
                    var $freezeButton = FormUtil.getFreezeButton("EXPERIMENT", this._experimentFormModel.v3_experiment.permId.permId, isEntityFrozen);
                    toolbarModel.push({ component : $freezeButton, tooltip: "Entity Frozen" });
                } else {
                    dropdownOptionsModel.push({
                        label : "Freeze Entity (Disable further modifications)",
                        action : function () {
                            FormUtil.showFreezeForm("EXPERIMENT", _this._experimentFormModel.v3_experiment.permId.permId);
                        }
                    });
                }
            }

            //History
            if(toolbarConfig.HISTORY) {
                dropdownOptionsModel.push({
                    label : "History",
                    action : function() {
                        mainController.changeView('showExperimentHistoryPage', _this._experimentFormModel.experiment.permId);
                    }
                });
            }
		} else { //Create and Edit
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._experimentFormController.updateExperiment();
			}, "Save", null, "save-btn");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn });
		}

		var $header = views.header;
		$header.append($formTitle);

		var hideShowOptionsModel = [];

		// Preview
        var $previewImageContainer = new $('<div>', { id : "previewImageContainer" });
        $previewImageContainer.append($("<legend>").append("Preview"));
        $previewImageContainer.hide();
        $formColumn.append($previewImageContainer);

		//
		// Identification Info on Create
		//
		if(this._experimentFormModel.mode === FormMode.CREATE) {
			$formColumn.append(this._createIdentificationInfoSection(hideShowOptionsModel));
		}

        // Plugin Hook
        var $experimentFormTop = new $('<div>');
        $formColumn.append($experimentFormTop);
        profile.experimentFormTop($experimentFormTop, this._experimentFormModel);

		//
		// Form Defined Properties from General Section
		//
		var experimentType = mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		if(experimentType.propertyTypeGroups) {
			for(var i = 0; i < experimentType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = experimentType.propertyTypeGroups[i];
				this._paintPropertiesForSection($formColumn, propertyTypeGroup, i);
			}
		}

		//Sample List Container
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			$formColumn.append(this._createSamplesSection(hideShowOptionsModel));
		}

        // Data sets section
        $formColumn.append($("<div>", {'id':'data-sets-section'}))

		//
		// Identification Info on not Create
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			$formColumn.append(this._createIdentificationInfoSection(hideShowOptionsModel));
		}

		//
		// PREVIEW IMAGE
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {

            var maxWidth = Math.floor(LayoutManager.getExpectedContentWidth() / 3);
            var maxHeight = Math.floor(LayoutManager.getExpectedContentHeight() / 3);

            var previewStyle = null;
            if (maxHeight < maxWidth) {
                previewStyle = "max-height:" + maxHeight + "px; display:none;";
            } else {
                previewStyle = "max-width:" + maxWidth + "px; display:none;";
            }

			var $previewImage = $("<img>", { 'data-preview-loaded' : 'false',
											 'class' : 'zoomableImage',
											 'id' : 'preview-image',
											 'src' : './img/image_loading.gif',
											 'style' : previewStyle
											});
			$previewImage.click(function() {
				Util.showImage($("#preview-image").attr("src"));
			});

			$previewImageContainer.append($previewImage);
		}

		// Plugin Hook
		var $experimentFormBottom = new $('<div>');
		$formColumn.append($experimentFormBottom);
		profile.experimentFormBottom($experimentFormBottom, this._experimentFormModel);

		//
		// DATASETS
		//

	    if(this._experimentFormModel.mode !== FormMode.CREATE &&
	        this._experimentFormModel.v3_experiment.dataSets.length > 0) {
            var belongToExperiment = false;
            for(var dIdx = 0; dIdx < this._experimentFormModel.v3_experiment.dataSets.length; dIdx++) {
                if(this._experimentFormModel.v3_experiment.dataSets[dIdx].sample === null) {
                    belongToExperiment = true;
                    break;
                }
            }
            if(belongToExperiment) {
                //Preview image
                this._reloadPreviewImage();

                // Dataset Viewer
                var $dataSetViewerContainer = new $('<div>', { id : "dataSetViewerContainer", style: "overflow: scroll; margin-top: 5px; padding-top: 5px; border-top: 1px dashed #ddd; " });
                mainController.sideMenu.addSubSideMenu($dataSetViewerContainer);
                this._experimentFormModel.dataSetViewer = new DataSetViewerController("dataSetViewerContainer", profile, this._experimentFormModel.v3_experiment, mainController.serverFacade, 
                        profile.getDefaultDataStoreURL(), null, false, true, this._experimentFormModel.mode);
                this._experimentFormModel.dataSetViewer.init();
            }
        }

		//
		// INIT
		//

		// Toolbar extension
		if(experimentTypeDefinitionsExtension && experimentTypeDefinitionsExtension.extraToolbar) {
		    toolbarModel = toolbarModel.concat(experimentTypeDefinitionsExtension.extraToolbar(_this._experimentFormModel.mode, _this._experimentFormModel.experiment));
		}
		if(experimentTypeDefinitionsExtension && experimentTypeDefinitionsExtension.extraToolbarDropdown) {
		    dropdownOptionsModel = dropdownOptionsModel.concat(experimentTypeDefinitionsExtension.extraToolbarDropdown(_this._experimentFormModel.mode, _this._experimentFormModel.experiment));
		}

		FormUtil.addOptionsToToolbar(toolbarModel, dropdownOptionsModel, hideShowOptionsModel,
				"EXPERIMENT-VIEW-" + this._experimentFormModel.experiment.experimentTypeCode);
		$header.append(FormUtil.getToolbar(toolbarModel));
		$container.append($form);

        mainController.profile.afterViewPaint(ViewType.EXPERIMENT_FORM, this._experimentFormModel, $container);
		Util.unblockUI();
	}

	this._createIdentificationInfoSection = function(hideShowOptionsModel) {
		hideShowOptionsModel.push({
		    forceToShow : this._experimentFormModel.mode === FormMode.CREATE,
			label : "Identification Info",
			section : "#experiment-identification-info"
		});

		var _this = this;
		var $identificationInfo = $("<div>", { id : "experiment-identification-info" });
		$identificationInfo.append($('<legend>').text("Identification Info"));
        if (this._experimentFormModel.mode !== FormMode.CREATE) {
            $identificationInfo.append(FormUtil.getFieldForLabelWithText("PermId", this._experimentFormModel.experiment.permId));
            $identificationInfo.append(FormUtil.getFieldForLabelWithText("Identifier", this._experimentFormModel.experiment.identifier));
		}
		if (this._experimentFormModel.mode !== FormMode.CREATE) {
			var spaceCode = IdentifierUtil.getSpaceCodeFromIdentifier(this._experimentFormModel.experiment.identifier);
			var projectCode = IdentifierUtil.getProjectCodeFromExperimentIdentifier(this._experimentFormModel.experiment.identifier);
			var experimentCode = this._experimentFormModel.experiment.code;
			var entityPath = FormUtil.getFormPath(spaceCode, projectCode, experimentCode);
			$identificationInfo.append(FormUtil.getFieldForComponentWithLabel(entityPath, "Path"));
		}

		var projectIdentifier = IdentifierUtil.getProjectIdentifierFromExperimentIdentifier(this._experimentFormModel.experiment.identifier);
		if(!this._experimentFormModel.mode === FormMode.CREATE) {
		    $identificationInfo.append(FormUtil.getFieldForLabelWithText("Type", this._experimentFormModel.experiment.experimentTypeCode));
		    $identificationInfo.append(FormUtil.getFieldForLabelWithText("Project", projectIdentifier));
		}

		var $projectField = FormUtil._getInputField("text", null, "project", null, true);
		$projectField.val(projectIdentifier);
		$projectField.hide();
		$identificationInfo.append($projectField);

		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			$identificationInfo.append(FormUtil.getFieldForLabelWithText("Code", this._experimentFormModel.experiment.code));

			var $codeField = FormUtil._getInputField("text", "codeId", "code", null, true);
			$codeField.val(IdentifierUtil.getCodeFromIdentifier(this._experimentFormModel.experiment.identifier));
			$codeField.hide();
			$identificationInfo.append($codeField);
		} else if(this._experimentFormModel.mode === FormMode.CREATE) {
			var $codeField = FormUtil._getInputField("text", "codeId", "code", null, true);
			$codeField.keyup(function() {
				_this._experimentFormModel.isFormDirty = true;
				var caretPosition = this.selectionStart;
				$(this).val($(this).val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._experimentFormModel.experiment.code = $(this).val();

				//Full Identifier
				var currentIdentifierSpace = IdentifierUtil.getSpaceCodeFromIdentifier(_this._experimentFormModel.experiment.identifier);
				var currentIdentifierProject = IdentifierUtil.getProjectCodeFromExperimentIdentifier(_this._experimentFormModel.experiment.identifier);
				var experimentIdentifier = IdentifierUtil.getExperimentIdentifier(currentIdentifierSpace, currentIdentifierProject, _this._experimentFormModel.experiment.code);
				_this._experimentFormModel.experiment.identifier = experimentIdentifier;
			})
			var $codeFieldRow = FormUtil.getFieldForComponentWithLabel($codeField, "Code");
			$identificationInfo.append($codeFieldRow);

			mainController.serverFacade.getProjectFromIdentifier($projectField.val(), function(project) {
				delete project["@id"];
				delete project["@type"];
				mainController.serverFacade.generateExperimentCode(project.id, function(autoGeneratedCode) {
					$codeField.val(autoGeneratedCode);
					_this._experimentFormModel.experiment.code = autoGeneratedCode;

					//Full Identifier
					var currentIdentifierSpace = IdentifierUtil.getSpaceCodeFromIdentifier(_this._experimentFormModel.experiment.identifier);
					var currentIdentifierProject = IdentifierUtil.getProjectCodeFromExperimentIdentifier(_this._experimentFormModel.experiment.identifier);
					var experimentIdentifier = IdentifierUtil.getExperimentIdentifier(currentIdentifierSpace, currentIdentifierProject, _this._experimentFormModel.experiment.code);
					_this._experimentFormModel.experiment.identifier = experimentIdentifier;

					_this._experimentFormModel.isFormDirty = true;
				});
			});
		}

		//
		// Registration and modification info
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._experimentFormModel.experiment.registrationDetails;

			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$identificationInfo.append($registrator);

			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)))
			$identificationInfo.append($registationDate);

			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$identificationInfo.append($modifier);

			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$identificationInfo.append($modificationDate);
		}
		$identificationInfo.hide();
		return $identificationInfo;
	}

	this._createSamplesSection = function(hideShowOptionsModel) {
		var _this = this;
		var $samples = $("<div>", { id : "experiment-samples" });
		$samples.append($('<legend>').text(ELNDictionary.Samples));
		var sampleListHeader = $("<p>");
		var sampleListContainer = $("<div>");
		$samples.append(sampleListHeader);
		$samples.append(sampleListContainer);
		var views = {
				header : sampleListHeader,
				content : sampleListContainer
		}
		var sampleList = new SampleTableController(this._experimentFormController, null, this._experimentFormModel.experiment.identifier, null, null, this._experimentFormModel.experiment);
		sampleList.init(views);
		$samples.hide();
		hideShowOptionsModel.push({
			label : ELNDictionary.Samples,
			section : "#experiment-samples",
			beforeShowingAction : function() {
				sampleList.refresh();
			}
		});
		return $samples;
	}

	this._paintPropertiesForSection = function($formColumn, propertyTypeGroup, i) {
		var _this = this;
		var experimentType = mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);

		var $fieldset = $('<div>');
		var $legend = $('<legend>');
		$fieldset.append($legend);

		if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
			$legend.text(propertyTypeGroup.name);
		} else if((i === 0) || ((i !== 0) && (experimentType.propertyTypeGroups[i-1].name !== null) && (experimentType.propertyTypeGroups[i-1].name !== ""))) {
			$legend.text("Metadata");
		} else {
			$legend.remove();
		}

		var propertyGroupPropertiesOnForm = 0;
		for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
			var propertyType = propertyTypeGroup.propertyTypes[j];
			var propertyTypeV3 = profile.getPropertyTypeFromSampleTypeV3(this._experimentFormModel.experimentType, propertyType.code);
			var isMultiValue = false;
            if(propertyTypeV3.isMultiValue) {
                isMultiValue = propertyTypeV3.isMultiValue();
            }

			profile.fixV1PropertyTypeVocabulary(propertyType);
			FormUtil.fixStringPropertiesForForm(propertyTypeV3, this._experimentFormModel.experiment);

			if(!propertyType.showInEditViews && (this._experimentFormController.mode === FormMode.EDIT || this._experimentFormController.mode === FormMode.CREATE) && propertyType.code !== "$XMLCOMMENTS") { //Skip
				continue;
			} else if(propertyType.dinamic && this._experimentFormController.mode === FormMode.CREATE) { //Skip
				continue;
			} else if(this._experimentFormModel.isSimpleFolder && this._experimentFormModel.mode === FormMode.CREATE &&
			        propertyType.code !== "$NAME" &&
			        !propertyType.mandatory) {
			    continue;
			}

            if(propertyType.code === "$XMLCOMMENTS") {
				var $commentsContainer = $("<div>");
				$fieldset.append($commentsContainer);
				var isAvailable = this._experimentFormController._addCommentsWidget($commentsContainer);
				if(!isAvailable) {
					continue;
				}
			} else {
				if(propertyType.code === "$SHOW_IN_PROJECT_OVERVIEW") {
					if(!(profile.inventorySpaces.length > 0 && $.inArray(IdentifierUtil.getSpaceCodeFromIdentifier(this._experimentFormModel.experiment.identifier), profile.inventorySpaces) === -1)) {
						continue;
					}
				}
				var $controlGroup =  null;

				var value = this._experimentFormModel.experiment.properties[propertyType.code];
				if(!value && propertyType.code.charAt(0) === '$') {
					value = this._experimentFormModel.experiment.properties[propertyType.code.substr(1)];
					this._experimentFormModel.experiment.properties[propertyType.code] = value;
					delete this._experimentFormModel.experiment.properties[propertyType.code.substr(1)];
				}

				if(this._experimentFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
		            if(Util.getEmptyIfNull(value) !== "") { //Don't show empty fields, whole empty sections will show the title
                        var customWidget = profile.customWidgetSettings[propertyType.code];
						var forceDisableRTF = profile.isForcedDisableRTF(propertyType);
                        if(customWidget && !forceDisableRTF) {
                            if (customWidget === 'Spreadsheet') {
                                var $jexcelContainer = $("<div>");
                                JExcelEditorManager.createField($jexcelContainer, this._experimentFormModel.mode, propertyType.code, this._experimentFormModel.experiment);
                                $controlGroup = FormUtil.getFieldForComponentWithLabel($jexcelContainer, propertyType.label);
                            } else if (customWidget === 'Word Processor') {
                                var $component = FormUtil.getFieldForPropertyType(propertyType, value);
                                $component = FormUtil.activateRichTextProperties($component, undefined, propertyType, value, true);
                                $controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
                            }
                        } else if(propertyType.dataType === "SAMPLE") {
                            var $component = new SampleField(false, '', false, value, true, isMultiValue);
                            $controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
                        } else {
                    	    $controlGroup = FormUtil.createPropertyField(propertyType, value);
                        }
                    } else {
                        continue;
                    }
				} else {
					var $component = null;
					if(propertyType.code === "$DEFAULT_OBJECT_TYPE") {
						$component = FormUtil.getSampleTypeDropdown(propertyType.code, false, null, null, IdentifierUtil.getSpaceCodeFromIdentifier(this._experimentFormModel.experiment.identifier), true);
					} else {
						$component = FormUtil.getFieldForPropertyType(propertyType, value, isMultiValue);
					}

					if(this._experimentFormModel.mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							FormUtil.setFieldValue(propertyType, $component, value);
						} else if(propertyType.dataType === "TIMESTAMP" || propertyType.dataType === "DATE") {
						} else if(isMultiValue) {
						    var valueV3 = this._experimentFormModel.v3_experiment.properties[propertyType.code];
						    if(valueV3) {
                                var valueArray;
                                if(Array.isArray(valueV3)) {
                                    valueArray = valueV3.sort();
                                } else {
                                    valueArray = valueV3.split(',');
                                    valueArray = valueArray.map(x => x.trim()).sort();
                                }
                                $component.val(valueArray);
                            }
						} else {
							$component.val(value);
						}
					} else {
						$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
					}

					var changeEvent = function(propertyType, isMultiValueProperty) {
                        return function(jsEvent, newValue) {
                            var propertyTypeCode = null;
                            propertyTypeCode = propertyType.code;
                            _this._experimentFormModel.isFormDirty = true;
                            var field = $(this);
                            if(propertyType.dataType === "BOOLEAN") {
                                _this._experimentFormModel.experiment.properties[propertyTypeCode] = FormUtil.getBooleanValue(field);
                            } else if (propertyType.dataType === "TIMESTAMP" || propertyType.dataType === "DATE") {
                                var timeValue = $($(field.children()[0]).children()[0]).val();
                                var isValidValue = Util.isDateValid(timeValue, propertyType.dataType === "DATE");
                                if(!isValidValue.isValid) {
                                    Util.showUserError(isValidValue.error);
                                } else {
                                    _this._experimentFormModel.experiment.properties[propertyTypeCode] = timeValue;
                                }
                            } else {
                                if(newValue !== undefined && newValue !== null) {
                                    _this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(newValue);
                                } else {
                                    var lastSelected = Util.getEmptyIfNull($('option', this).filter(':selected:last').val());
                                    var dataLast = field.data('last');
                                     if(propertyType.dataType === "CONTROLLEDVOCABULARY" && isMultiValueProperty) {
                                         var props = _this._experimentFormModel.experiment.properties[propertyTypeCode];
                                         if (field.val()) {
                                        if(props !== undefined) {
                                            if(props != '' && field.val().includes('')) {
                                                _this._experimentFormModel.experiment.properties[propertyTypeCode] = '';
                                                field.val([]);
                                            } else {
                                                if(props == '' && field.val().includes('')) {
                                                    var removedEmpty = field.val().filter(x => x != '');
                                                    _this._experimentFormModel.experiment.properties[propertyTypeCode] = removedEmpty;
                                                    field.val(removedEmpty);
                                                } else {
                                                    _this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
                                                }
                                            }
                                        } else {
                                            if(field.val().includes('')) {
                                                if(dataLast == undefined) {
                                                    var val = field.val().filter(x => x != '');
                                                    _this._experimentFormModel.experiment.properties[propertyTypeCode] = val;
                                                    field.val(val);
                                                } else {
                                                    _this._experimentFormModel.experiment.properties[propertyTypeCode] = '';
                                                    field.val([]);
                                                }
                                            } else {
                                                _this._experimentFormModel.experiment.properties[propertyTypeCode] = field.val();
                                            }
                                        }
                                         } else {
                                              _this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
                                         }



                                    } else {
                                        _this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
                                    }
                                    field.data('last', field.val());
                                }
                            }
                        }
                    }

					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}

					var customWidget = profile.customWidgetSettings[propertyType.code];
					var forceDisableRTF = profile.isForcedDisableRTF(propertyType);

                    if(customWidget && !forceDisableRTF) {
                        switch(customWidget) {
                            case 'Word Processor':
                                if(propertyType.dataType === "MULTILINE_VARCHAR") {
                    		        $component = FormUtil.activateRichTextProperties($component, changeEvent(propertyType), propertyType, value, false);
                    	        } else {
                    		        alert("Word Processor only works with MULTILINE_VARCHAR data type.");
                    		        $component.change(changeEvent(propertyType));
                    		    }
                                break;
                    	    case 'Spreadsheet':
                    	        if(propertyType.dataType === "XML") {
                                    var $jexcelContainer = $("<div>");
                                    JExcelEditorManager.createField($jexcelContainer, this._experimentFormModel.mode, propertyType.code, this._experimentFormModel.experiment);
                                    $component = $jexcelContainer;
                    		    } else {
                    		        alert("Spreadsheet only works with XML data type.");
                    		        $component.change(changeEvent(propertyType));
                    		    }
                    		    break;
                        }
                    } else if(propertyType.dataType === "TIMESTAMP" || propertyType.dataType === "DATE") {
						$component.on("dp.change", changeEvent(propertyType));
					} else {
						$component.change(changeEvent(propertyType, isMultiValue));
					}

					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}

				$fieldset.append($controlGroup);
			}
			propertyGroupPropertiesOnForm++;
		}

		if(propertyGroupPropertiesOnForm === 0) {
			$legend.remove();
		}

		$formColumn.append($fieldset);
	}

	//
	// Preview Image
	//
	this._reloadPreviewImage = function() {
		var _this = this;
		var previewCallback =  function(data) {
				if (data.objects.length == 0) {
					_this._updateLoadingToNotAvailableImage();
				} else {
					var listFilesForDataSetCallback = function(dataFiles) {
							var found = false;
							if(!dataFiles.result) {
								//DSS Is not running probably
							} else {
								for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
									if(!dataFiles.result[pathIdx].isDirectory) {
										var elementId = 'preview-image';
										var downloadUrl = profile.getDefaultDataStoreURL() + '/' + data.objects[0].code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();

										var img = $("#" + elementId);
										img.attr('src', downloadUrl);
										img.attr('data-preview-loaded', 'true');
										img.show();
										$("#previewImageContainer").show();
										break;
									}
								}
							}
					};
					mainController.serverFacade.listFilesForDataSet(data.objects[0].code, "/", true, listFilesForDataSetCallback);
				}
		};

		var datasetRules = { "UUIDv4.1" : { type : "Experiment", name : "ATTR.PERM_ID", value : this._experimentFormModel.experiment.permId },
							 "UUIDv4.2" : { type : "Attribute", name : "DATA_SET_TYPE", value : "ELN_PREVIEW" },
							 "UUIDv4.3" : { type : "Sample", name : "NULL.NULL", value : "NULL" }
							 };

    	mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, previewCallback);
	}

	this._updateLoadingToNotAvailableImage = function() {
		var notLoadedImages = $("[data-preview-loaded='false']");
		notLoadedImages.attr('src', "./img/image_unavailable.png");
	}

	this._allowedToCreateSample = function() {
		var experiment = this._experimentFormModel.v3_experiment;
		var project = experiment.project;
		var space = project.space;

		return experiment.frozenForSamples == false && project.frozenForSamples == false && space.frozenForSamples == false
			&& this._experimentFormModel.sampleRights.rights.indexOf("CREATE") >= 0;
	}

	this._allowedToEdit = function() {
		var experiment = this._experimentFormModel.v3_experiment;
		var updateAllowed = this._allowedToUpdate(this._experimentFormModel.rights);
		return updateAllowed && experiment.frozen == false;
	}

	this._allowedToUpdate = function(rights) {
		return rights && rights.rights.indexOf("UPDATE") >= 0;
	}

	this._allowedToMove = function() {
		var experiment = this._experimentFormModel.v3_experiment;
		if (experiment.project.frozenForExperiments) {
			return false;
		}
		return this._allowedToUpdate(this._experimentFormModel.rights);
	}

	this._allowedToDelete = function() {
		var experiment = this._experimentFormModel.v3_experiment;
        var numberOfUndeletableDataSets = 0;
        experiment.dataSets.forEach(function(dataSet) {
            if (dataSet.frozen || dataSet.type.disallowDeletion) {
                numberOfUndeletableDataSets++;
            }
        });
        return (experiment.frozen == false && experiment.project.frozenForExperiments == false)
                && numberOfUndeletableDataSets == 0
                && this._experimentFormModel.rights.rights.indexOf("DELETE") >= 0;
	}

	this._allowedToRegisterDataSet = function() {
		var experiment = this._experimentFormModel.v3_experiment;
		return experiment.frozenForDataSets == false && this._experimentFormModel.dataSetRights.rights.indexOf("CREATE") >= 0;
	}
}