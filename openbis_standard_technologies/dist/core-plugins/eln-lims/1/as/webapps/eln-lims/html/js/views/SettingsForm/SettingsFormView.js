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

function SettingsFormView(settingsFormController, settingsFormModel) {
	this._settingsFormController = settingsFormController;
	this._settingsFormModel = settingsFormModel;

	this._datasetTypesTableModel = null;

	this._mainMenuItemsTableModel = null;
	this._forcedDisableRTFTableModel = null;
	this._customWidgetsTableModel = null;
	this._forcedMonospaceTableModel = null;
	this._inventorySpacesTableModel = null;
	this._inventorySpacesReadOnlyTableModel = null;
	this._sampleTypeDefinitionsMiscellaneousSettingsTableModels = {}; // key: sample type; value: table model
	this._sampleTypeDefinitionsSettingsTableModels = {}; // key: sample type; value: table model
	this._sampleTypeDefinitionsHintsTableModels = {}; // key: sample type; value: table model
	this._miscellaneousTableModel = null;

	this.repaint = function(views, profileToEdit) {
		var _this = this;
		this._profileToEdit = profileToEdit;
		var $container = views.content;

		Util.blockUI(null, null, true);
		// delay painting just a bit so blockUI can be shown
		window.setTimeout((function($container) {

			var $form = $("<div>");
			var $formColumn = $("<div>");
			$form.append($formColumn);

			var typeTitle = "Settings";

			var $formTitle = $("<h2>").append(typeTitle);

			//
			// Toolbar
			//
			var toolbarModel = [];

			if(this._settingsFormModel.mode === FormMode.VIEW) {
				if (this._settingsFormModel.sampleRights.rights.indexOf("UPDATE") >= 0) {
					//Edit
					var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
						mainController.changeView("showEditSettingsPage", _this._settingsFormModel.settingsSample.identifier);
					}, "Edit", null, "edit-btn");
					toolbarModel.push({ component : $editButton });
				}
			} else { //Create and Edit
				//Save
				var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", (function() {
				    var widgetSettings = null;
				    if(this._settingsFormModel.settingsSample.code === "GENERAL_ELN_SETTINGS") {
				        widgetSettings = this._customWidgetsTableModel.getValues();
				    }
					this._settingsFormController.save(this._getSettings(), widgetSettings);
				}).bind(this), "Save", null, "save-btn");
				$saveBtn.removeClass("btn-default");
				$saveBtn.addClass("btn-primary");
				toolbarModel.push({ component : $saveBtn });
			}

			var $header = views.header;
			$header.append($formTitle);
			$header.append(FormUtil.getToolbar(toolbarModel));

			var texts = ELNDictionary.settingsView.sections;

            if(this._settingsFormModel.settingsSample.code === "GENERAL_ELN_SETTINGS") {
                $formColumn.append($("<h2>").append("Instance Settings"));
	            this._paintCustomWidgetsSection($formColumn, texts.customWidgets);
	            this._paintForcedMonospaceSection($formColumn, texts.forceMonospaceFont);
	            this._paintDataSetTypesForFileNamesSection($formColumn, texts.dataSetTypeForFileName);
			}

            $formColumn.append($("<h2>").append("Group Settings"));

            if(profile.isMultiGroup()) {
                $formColumn.append(FormUtil.getWarningText("Storages and Templates shown are the ones belonging to the group settings selected."));
            }

            this._paintStoragesSection($formColumn, texts.storages);
			this._paintTemplateSection($formColumn, texts.templates);
            this._paintSampleTypesDefinition($formColumn,texts.sampleTypeDefinitionsExtension);
            this._paintInventorySpacesSection($formColumn, texts.inventorySpaces);
			this._paintMainMenuSection($formColumn, texts.mainMenu);
			this._paintMiscellaneous($formColumn, texts.miscellaneous)

            if(this._settingsFormModel.settingsSample.code === "GENERAL_ELN_SETTINGS") {
                $formColumn.append($("<h2>").append("Deprecated"));
                this._paintForcedDisableRtfSection($formColumn, texts.forcedDisableRTF);
            }

			$container.append($form);

			Util.unblockUI();

		}).bind(this, $container), 20);

	}

	this._getSettings = function() {
	    var settings = {
            mainMenu : this._mainMenuItemsTableModel.getValues(),
            sampleTypeDefinitionsExtension : this._getSampleTypeDefinitionsExtension(),
            showDatasetArchivingButton : this._miscellaneousTableModel.getValues()["Show Dataset archiving button"],
            hideSectionsByDefault : this._miscellaneousTableModel.getValues()["Hide sections by default"],
            inventorySpaces : this._inventorySpacesTableModel.getValues(),
            inventorySpacesReadOnly : this._inventorySpacesReadOnlyTableModel.getValues()
        };

        if(this._settingsFormModel.settingsSample.code === "GENERAL_ELN_SETTINGS") {
            settings.dataSetTypeForFileNameMap = this._datasetTypesTableModel.getValues();
            settings.forceMonospaceFont = this._forcedMonospaceTableModel.getValues();
            settings.forcedDisableRTF = this._forcedDisableRTFTableModel.getValues();
        }

		return settings;
	}

	this._getSampleTypeDefinitionsExtension = function() {
		var sampleTypeDefinitionsSettings = {};
		var sampleTypes = Object.keys(this._sampleTypeDefinitionsHintsTableModels);
		for (sampleType of sampleTypes) {

			var sampleTypeSection = {};

			var hintsTableModel = this._sampleTypeDefinitionsHintsTableModels[sampleType];
			sampleTypeSection = hintsTableModel.getValues();

			var settingsTableModel = this._sampleTypeDefinitionsSettingsTableModels[sampleType];
			var settingsValues = settingsTableModel.getValues();
			for (var key in settingsValues) {
				sampleTypeSection[key] = settingsValues[key];
			}
			
			var miscellaneousSettingsTableModel = this._sampleTypeDefinitionsMiscellaneousSettingsTableModels[sampleType];
			var miscellaneousSettingsValues = miscellaneousSettingsTableModel.getValues();
			for (var key in miscellaneousSettingsValues) {
				sampleTypeSection[key] = miscellaneousSettingsValues[key];
			}
			
			sampleTypeDefinitionsSettings[sampleType] = sampleTypeSection;
		}
		return sampleTypeDefinitionsSettings;
	}

	this._paintStoragesSection = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-storages");
		$fieldset.append(FormUtil.getInfoText(text.info));

		var experimentIdentifier = profile.getStorageConfigCollectionForConfigSample(this._settingsFormModel.settingsSample); //"/ELN_SETTINGS/STORAGES/STORAGES_COLLECTION";

		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
			var argsMap = {
					"sampleTypeCode" : "STORAGE",
					"experimentIdentifier" : experimentIdentifier
			}
			var argsMapStr = JSON.stringify(argsMap);
			Util.unblockUI();
			mainController.changeView("showCreateSubExperimentPage", argsMapStr);
		}, "New Storage");

		$fieldset.append($("<p>").append($addBtn));

		var $gridContainer = $("<div>");
		$fieldset.append($gridContainer);

        var advancedSampleSearchCriteria = {
            entityKind : "SAMPLE",
            logicalOperator : "AND",
            rules : {
                "1" : { type : "Experiment",  name : "ATTR.IDENTIFIER", value : experimentIdentifier }
            }
        }
        var dataGrid = SampleDataGridUtil.getSampleDataGrid(experimentIdentifier, advancedSampleSearchCriteria, 
                null, null, null, null, true, null, false, false, false, 40);
		var extraOptions = [];
		dataGrid.init($gridContainer, extraOptions);
	}
	
	this._paintTemplateSection = function($container, text) {
		var _this = this;
		var $fieldset = this._getFieldset($container, text.title, "settings-section-templates");
		$fieldset.append(FormUtil.getInfoText(text.info));

		var experimentIdentifier = profile.getTemplateConfigCollectionForConfigSample(this._settingsFormModel.settingsSample); //"/ELN_SETTINGS/TEMPLATES/TEMPLATES_COLLECTION";

        var $addBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
                FormUtil.createNewSample(experimentIdentifier);
        }, "New Template");

        $fieldset.append($("<p>").append($addBtn));
        var $gridContainer = $("<div>");
		$fieldset.append($gridContainer);

        var advancedSampleSearchCriteria = {
            entityKind : "SAMPLE",
            logicalOperator : "AND",
            rules : {
                "1" : { type : "Experiment",  name : "ATTR.IDENTIFIER", value : experimentIdentifier }
            }
        }

        var dataGrid = SampleDataGridUtil.getSampleDataGrid(null, advancedSampleSearchCriteria, null, null, null, 
                null, true, null, false, false, false, 40);
		var extraOptions = [];
		dataGrid.init($gridContainer, extraOptions);
	}
	

	this._paintMainMenuSection = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-main-menu");
		$fieldset.append(FormUtil.getInfoText(text.info));
		this._mainMenuItemsTableModel = this._getMainMenuItemsTableModel();
		$fieldset.append(this._getTable(this._mainMenuItemsTableModel));
	}

	this._paintForcedDisableRtfSection = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-disable-rtf");
		$fieldset.append(FormUtil.getInfoText(text.info));
		this._forcedDisableRTFTableModel = this._getForcedDisableRTFTableModel();
		$fieldset.append(this._getTable(this._forcedDisableRTFTableModel));
	}

    this._paintCustomWidgetsSection = function($container, text) {
    		var $fieldset = this._getFieldset($container, text.title, "settings-section-custom-widgets", true);
    		$fieldset.append(FormUtil.getInfoText(text.info));
    		this._customWidgetsTableModel = this._getCustomWidgetsTableModel();
    		$fieldset.append(this._getTable(this._customWidgetsTableModel));
    }


	this._paintForcedMonospaceSection = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-monospace", true);
		$fieldset.append(FormUtil.getInfoText(text.info));
		this._forcedMonospaceTableModel = this._getForcedMonospaceTableModel();
		$fieldset.append(this._getTable(this._forcedMonospaceTableModel));
	}

	this._paintInventorySpacesSection = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-inventory-spaces", true);
		$fieldset.append(FormUtil.getInfoText(text.info));
		this._inventorySpacesTableModel = this._getInventorySpacesTableModel();
		$fieldset.append(this._getTable(this._inventorySpacesTableModel));
		this._inventorySpacesReadOnlyTableModel = this._getInventorySpacesReadOnlyTableModel();
        $fieldset.append(this._getTable(this._inventorySpacesReadOnlyTableModel));
	}

	this._getMainMenuItemsTableModel = function() {
		var tableModel = this._getTableModel();
		tableModel.fullWidth = false;
		// define columns
		tableModel.columns = [{ label : "Main Menu Item"}, { label : "enabled"}];
		tableModel.rowBuilders = {
			"Main Menu Item" : function(rowData) {
				return $("<span>").text(rowData.menuItemName);
			},
			"enabled" : function(rowData) {
				var $checkbox = $("<input>", { type : "checkbox", name : "cb" });
				if (rowData.enabled) {
					$checkbox.attr("checked", true);
				}
				return $checkbox;
			}
		};
		// add data
		for (var menuItemName of Object.keys(this._profileToEdit.mainMenu)) {
			if (menuItemName !== "showSettings") {
				tableModel.addRow({
					menuItemName : menuItemName,
					enabled : this._profileToEdit.mainMenu[menuItemName]
				});
			}
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			var mainMenu = {};
			for (var value of values) {
				mainMenu[value["Main Menu Item"]] = value["enabled"];
			}
			return mainMenu;
		};
		return tableModel;
	}

	this._getForcedDisableRTFTableModel = function() {
		return this._getSingleColumnDropdownTableModel({
			columnName : "Property Type",
			placeholder : "select property type",
			options : this._settingsFormController.getForcedDisableRTFOptions(),
			initialValues : this._profileToEdit.forcedDisableRTF,
		});
	}

	this._getCustomWidgetsTableModel = function() {
        return this._getDoubleColumnDropdownTableModel({
            columnName : "Property Type",
    	    placeholder : "select property type",
    	    options : profile.allPropertyTypes.map(function(_) { return _.code; })
        }, {
    	    columnName : "Widget",
            placeholder : "select widget",
            options : ["Word Processor", "Spreadsheet"]
        }, this._settingsFormModel.customWidgetSettings);
    }

	this._getDoubleColumnDropdownTableModel = function(params, paramsB, initialValues) {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [{ label : params.columnName }, { label : paramsB.columnName }];
		tableModel.rowBuilders[params.columnName] = function(rowData) {
			return FormUtil.getDropdown(params.options.map(function(option) {
				return {
					label : Util.getDisplayNameFromCode(option),
					value : option,
					selected : option === rowData[params.columnName],
				};
			}), params.placeholder);
		}
		tableModel.rowBuilders[paramsB.columnName] = function(rowData) {
            return FormUtil.getDropdown(paramsB.options.map(function(option) {
        	    return {
        		    label : option,
        		    value : option,
        		    selected : option === rowData[paramsB.columnName],
                };
            }), paramsB.placeholder);
        }
		// add data
		for (var item of initialValues) {
			tableModel.addRow(item);
		}

		return tableModel;
	}

	this._getForcedMonospaceTableModel = function() {
		return this._getSingleColumnDropdownTableModel({
			columnName : "Property Type",
			placeholder : "select property type",
			options : this._settingsFormController.getForcedMonospaceFontOptions(),
			initialValues : this._profileToEdit.forceMonospaceFont,
		});
	}

	this._getInventorySpacesTableModel = function() {
		// Not accessible spaces are known on the config anyway, hiding them now will not increase security
		// Removing them can bring more issues so better keep them
		
		var spacesOptions = this._settingsFormController.getInventorySpacesOptions();
			spacesOptions = JSON.parse(JSON.stringify(spacesOptions));
		var initialValues = [];
		if(this._profileToEdit.inventorySpaces) {
			initialValues = this._profileToEdit.inventorySpaces.filter(space => space != null);
		}
		
		for(var i = 0; i < initialValues.length; i++) {
			if($.inArray(initialValues[i], spacesOptions) === -1) {
				spacesOptions.push(initialValues[i]);
			}
		}
		
		return this._getSingleColumnDropdownTableModel({
			columnName : "Space",
			placeholder : "select space",
			options : spacesOptions,
			initialValues : initialValues,
		});
	}

    this._getInventorySpacesReadOnlyTableModel = function() {
		// Not accessible spaces are known on the config anyway, hiding them now will not increase security
		// Removing them can bring more issues so better keep them

		var spacesOptions = this._settingsFormController.getInventorySpacesReadOnlyOptions();
			spacesOptions = JSON.parse(JSON.stringify(spacesOptions));
		var initialValues = [];
		if(this._profileToEdit.inventorySpacesReadOnly) {
			initialValues = this._profileToEdit.inventorySpacesReadOnly.filter(space => space != null);
		}
		for(var i = 0; i < initialValues.length; i++) {
			if($.inArray(initialValues[i], spacesOptions) === -1) {
				spacesOptions.push(initialValues[i]);
			}
		}

		return this._getSingleColumnDropdownTableModel({
			columnName : "Space Read only",
			placeholder : "select space",
			options : spacesOptions,
			initialValues : initialValues,
		});
	}

	//
	// dataset types for filenames
	//
	this._paintDataSetTypesForFileNamesSection = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-dataset-types", true);
		$fieldset.append(FormUtil.getInfoText(text.info));
		this._datasetTypesTableModel = this._getDatasetTypesTableModel();
		$fieldset.append(this._getTable(this._datasetTypesTableModel));
	}

	this._getDatasetTypesTableModel = function() {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [{ label : "Filename extension" }, { label : "Dataset type" }];
		tableModel.rowBuilders = {
			"Filename extension" : function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.fileNameExtension);
			},
			"Dataset type" : (function(rowData) {
				var allDatasetTypeCodes = this._settingsFormController.getAllDatasetTypeCodeOptions();
				return FormUtil.getDropdown(allDatasetTypeCodes.map(function(option) {
					return {
						label : Util.getDisplayNameFromCode(option),
						value : option,
						selected : option === rowData.dataSetType,
					};
				}), "select Dataset type");
			}).bind(this),
		};
		// add data
		for (var dataSetTypeForFileName of this._profileToEdit.dataSetTypeForFileNameMap) {
			tableModel.addRow(dataSetTypeForFileName);
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			return values.map(function(value) {
				return {
					fileNameExtension : value["Filename extension"],
					dataSetType : value["Dataset type"],
				};
			});
		}
		return tableModel;
	}

	//
	// sample type definitions
	//
	this._paintSampleTypesDefinition = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-sampletype-definitions");
		$fieldset.append(FormUtil.getInfoText(text.info));
		for (var sampleType of profile.getAllSampleTypes()) {
			// layout
			var $div = $("<div>").css("padding-left", "15px");
			var displayName = Util.getDisplayNameFromCode(sampleType.code);
			var $sampleTypeFieldset = this._getFieldset($div, displayName, "settings-section-sampletype-" + sampleType.code, true);
			$fieldset.append($div);

            var defaultSampleTypeSettings = SettingsManagerUtils.getDefaultProfile().sampleTypeDefinitionsExtension[sampleType.code];
			var sampleTypeSettings = this._profileToEdit.sampleTypeDefinitionsExtension[sampleType.code];
			if(!sampleTypeSettings && defaultSampleTypeSettings) { // Sets the default profile configuration given by plugins
			    sampleTypeSettings = defaultSampleTypeSettings;
			}

			// Checkboxes for miscellaneous options
			// isProtocol
			// isStorage
			var miscellaneousSettingsTableModel = this._getSampleTypesDefinitionMiscellaneousSettingsTableModel(sampleType.code, sampleTypeSettings);
			var miscellaneousSettingsTable = this._getTable(miscellaneousSettingsTableModel);
			miscellaneousSettingsTable.css( { "margin-left" : "30px" } );
			$sampleTypeFieldset.append(miscellaneousSettingsTable);
			this._sampleTypeDefinitionsMiscellaneousSettingsTableModels[sampleType.code] = miscellaneousSettingsTableModel;
			
			// table for parents / children settings:
			// SAMPLE_PARENTS_TITLE, SAMPLE_PARENTS_DISABLED, SAMPLE_PARENTS_ANY_TYPE_DISABLED, 
			// SAMPLE_CHILDREN_TITLE, SAMPLE_CHILDREN_DISABLED, SAMPLE_CHILDREN_ANY_TYPE_DISABLED
			var settingsTableModel = this._getSampleTypesDefinitionSettingsTableModel(sampleTypeSettings);
			var settingsTable = this._getTable(settingsTableModel);
			settingsTable.css( { "margin-left" : "30px" } );
			$sampleTypeFieldset.append(settingsTable);
			this._sampleTypeDefinitionsSettingsTableModels[sampleType.code] = settingsTableModel;

			// table for parents / children hints for SAMPLE_PARENTS_HINT, SAMPLE_CHILDREN_HINT
			var hintsTableModel = this._getSampleTypeDefinitionTableModel(sampleTypeSettings);
			$sampleTypeFieldset.append(this._getTable(hintsTableModel));
			this._sampleTypeDefinitionsHintsTableModels[sampleType.code] = hintsTableModel;
		}
	}

	this._paintMiscellaneous = function($container, text) {
		var $fieldset = this._getFieldset($container, text.title, "settings-section-miscellaneous");
		$fieldset.append(FormUtil.getInfoText(text.info));
		this._miscellaneousTableModel = this._getMiscellaneousTableModel();
		$fieldset.append(this._getTable(this._miscellaneousTableModel));
	}

	this._getMiscellaneousTableModel = function() {
		var tableModel = this._getTableModel();
		tableModel.fullWidth = false;
		// define columns
		tableModel.columns = [{ label : "Setting"}, { label : "enabled"}];
		tableModel.rowBuilders = {
			"Setting" : function(rowData) {
				return $("<span>").text(rowData.label);
			},
			"enabled" : function(rowData) {
				var $checkbox = $("<input>", { type : "checkbox", name : "cb" });
				if (rowData.enabled) {
					$checkbox.attr("checked", true);
				}
				return $checkbox;
			}
		};
		// add data
		tableModel.addRow({
			label : "Show Dataset archiving button",
			enabled : this._profileToEdit.showDatasetArchivingButton
		});
		tableModel.addRow({
			label : "Hide sections by default",
			enabled : this._profileToEdit.hideSectionsByDefault
			
		});
		// transform output
		tableModel.valuesTransformer = function(values) {
			var settings = {};
			for (var value of values) {
				settings[value["Setting"]] = value["enabled"];
			}
			return settings;
		};
		return tableModel;
	}

	this._getSampleTypesDefinitionMiscellaneousSettingsTableModel = function(code, sampleTypeSettings) {
		var tableModel = this._getTableModel();
		tableModel.fullWidth = false;
		// define columns
		tableModel.columns = [
			{ label : "Options" },
			{ label : "enabled" }
		];
		tableModel.rowBuilders = {
			"Options" : function(rowData) {
				return $("<span>").text(rowData.name);
			},
			"enabled" : function(rowData) {
				var $checkbox = $("<input>", { type : "checkbox" });
				if (rowData.enabled) {
					$checkbox.attr("checked", true);
				}
				if (rowData.id) {
                    $checkbox.attr("id", rowData.id);
                }
				return $checkbox;
			}
		};
		// add data
		if (sampleTypeSettings) { // values from profile
			tableModel.addRow({
				name : "Use as Protocol",
				enabled : sampleTypeSettings["USE_AS_PROTOCOL"]
			});
			tableModel.addRow({
				name : "Enable Storage",
				enabled : sampleTypeSettings["ENABLE_STORAGE"]
			});
			var showInDropDowns = sampleTypeSettings["SHOW"];
            if(showInDropDowns === undefined) {
                showInDropDowns = false;
            }
			tableModel.addRow({
				name : "Show in drop downs",
				enabled : showInDropDowns,
				id : code + "_show_in_drop_downs"
			});
			var showOnNav = sampleTypeSettings["SHOW_ON_NAV"];
			if(showOnNav === undefined) {
			    showOnNav = false;
			}
			tableModel.addRow({
                name : "Show in main menu",
                enabled : showOnNav
            });
		} else { // default values
			tableModel.addRow({
				name : "Use as Protocol",
				enabled : false
			});
			tableModel.addRow({
				name : "Enable Storage",
				enabled : false
			});
			tableModel.addRow({
                name : "Show in drop downs",
                enabled : false
            });
            tableModel.addRow({
                name : "Show in main menu",
                enabled : false
            });
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			var settings = {};
			for (var rowValues of values) {
				if (rowValues["Options"] === "Use as Protocol") {
					settings["USE_AS_PROTOCOL"] = rowValues["enabled"];
				} else if (rowValues["Options"] === "Enable Storage") {
					settings["ENABLE_STORAGE"] = rowValues["enabled"];
				} else if (rowValues["Options"] === "Show in drop downs") {
					settings["SHOW"] = rowValues["enabled"];
				} else if (rowValues["Options"] === "Show in main menu") {
                    settings["SHOW_ON_NAV"] = rowValues["enabled"];
                }
			}
			return settings;
		}
		return tableModel;
	}
	
	this._getSampleTypesDefinitionSettingsTableModel = function(sampleTypeSettings) {
		var tableModel = this._getTableModel();
		// define columns
		tableModel.columns = [
			{ label : "Settings for" },
			{ label : "Section name" },
			{ label : "Disable section" },
			{ label : "Disable addition of any " + ELNDictionary.sample + " type" }
		];
		tableModel.rowBuilders = {}
		tableModel.rowBuilders["Settings for"] = function(rowData) {
				return $("<span>").text(rowData.parentsOrChildren);
		};
		tableModel.rowBuilders["Section name"] = function(rowData) {
			return $("<input>", { type : "text", class : "form-control" }).val(rowData.title);
		};
		tableModel.rowBuilders["Disable section"] = function(rowData) {
			var $checkbox = $("<input>", { type : "checkbox" });
			if (rowData.disabled) {
				$checkbox.attr("checked", true);
			}
			return $checkbox;
		};
		tableModel.rowBuilders["Disable addition of any " + ELNDictionary.sample + " type"] = function(rowData) {
			var $checkbox = $("<input>", { type : "checkbox" });
			if (rowData.anyTypeDisabled) {
				$checkbox.attr("checked", true);
			}
			return $checkbox;
		};
		// add data
		if (sampleTypeSettings) { // values from profile
			tableModel.addRow({
				parentsOrChildren : "Parents",
				title : sampleTypeSettings["SAMPLE_PARENTS_TITLE"] ? sampleTypeSettings["SAMPLE_PARENTS_TITLE"] : "",
				disabled : sampleTypeSettings["SAMPLE_PARENTS_DISABLED"],
				anyTypeDisabled : sampleTypeSettings["SAMPLE_PARENTS_ANY_TYPE_DISABLED"],
			});
			tableModel.addRow({
				parentsOrChildren : "Children",
				title : sampleTypeSettings["SAMPLE_CHILDREN_TITLE"] ? sampleTypeSettings["SAMPLE_CHILDREN_TITLE"] : "",
				disabled : sampleTypeSettings["SAMPLE_CHILDREN_DISABLED"],
				anyTypeDisabled : sampleTypeSettings["SAMPLE_CHILDREN_ANY_TYPE_DISABLED"],
			});
		} else { // default values
			tableModel.addRow({
				parentsOrChildren : "Parents",
				title : "",
				disabled : false,
				anyTypeDisabled : false,
			});
			tableModel.addRow({
				parentsOrChildren : "Children",
				title : "",
				disabled : false,
				anyTypeDisabled : false,
			});
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			var settings = {};
			for (var rowValues of values) {
				if (rowValues["Settings for"] === "Parents") {
					if (rowValues["Section name"] && rowValues["Section name"].length > 0) {
						settings["SAMPLE_PARENTS_TITLE"] = rowValues["Section name"];
					}
					if (rowValues["Disable section"]) {
						settings["SAMPLE_PARENTS_DISABLED"] = true;
					} else {
					    settings["SAMPLE_PARENTS_DISABLED"] = false;
					}
					if (rowValues["Disable addition of any " + ELNDictionary.sample + " type"]) {
						settings["SAMPLE_PARENTS_ANY_TYPE_DISABLED"] = true;
					} else {
					    settings["SAMPLE_PARENTS_ANY_TYPE_DISABLED"] = false;
					}
				} else if (rowValues["Settings for"] === "Children") {
					if (rowValues["Section name"] && rowValues["Section name"].length > 0) {
						settings["SAMPLE_CHILDREN_TITLE"] = rowValues["Section name"];
					}
					if (rowValues["Disable section"]) {
						settings["SAMPLE_CHILDREN_DISABLED"] = true;
					} else {
					    settings["SAMPLE_CHILDREN_DISABLED"] = false;
					}
					if (rowValues["Disable addition of any " + ELNDictionary.sample + " type"]) {
						settings["SAMPLE_CHILDREN_ANY_TYPE_DISABLED"] = true;
					} else {
					    settings["SAMPLE_CHILDREN_ANY_TYPE_DISABLED"] = false;
					}
				}
			}
			return settings;
		}
		return tableModel;
	}

	this._getSampleTypeDefinitionTableModel = function(sampleTypeSettings) {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [
			{ label : "Hints for", width : "140px" },
			{ label : "Label" },
			{ label : "Type" },
			{ label : "Min", width : "80px" },
			{ label : "Max", width : "80px" }
		];
		tableModel.rowBuilders = {
			"Hints for" : (function(rowData) {
				var options = ["Children", "Parents"];
				return FormUtil.getDropdown(options.map(function(option) {
					return {
						label : option,
						value : option,
						selected : rowData.hintType && option === rowData.hintType,
					};
				}), "select children/parents");
			}).bind(this),
			"Label" :  function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.LABEL);
			},
			"Type" : (function(rowData) {
				var options = this._settingsFormController.getSampleTypeOptions();
				return FormUtil.getDropdown(options.map(function(option) {
					return {
						label : Util.getDisplayNameFromCode(option),
						value : option,
						selected : option === rowData.TYPE,
					};
				}), "select sample type");
			}).bind(this),
			"Min" : function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.MIN_COUNT);
			},
			"Max" : function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.MAX_COUNT);
			},
		};
		// extra
		tableModel.rowExtraBuilder = (function(rowData) {
			var annotationPropertiesTableModel = this._getAnnotationPropertiesTableModel();
			if (rowData.ANNOTATION_PROPERTIES) {
				for (var annotationProperty of rowData.ANNOTATION_PROPERTIES) {
					annotationPropertiesTableModel.addRow(annotationProperty);
				}
			}
			tableModel.rowExtraModels.push(annotationPropertiesTableModel);
			return this._getTable(annotationPropertiesTableModel);
		}).bind(this);
		// add data
		var hintTypesMap = new Map();
		hintTypesMap.set("Children", "SAMPLE_CHILDREN_HINT");
		hintTypesMap.set("Parents", "SAMPLE_PARENTS_HINT");
		for (var [hintTypeLabel, hintTypeField] of hintTypesMap) {
			if (sampleTypeSettings && sampleTypeSettings[hintTypeField]) {
				for (var hintTypeSettings of sampleTypeSettings[hintTypeField]) {
					var hintTypeSettingsCopy = $.extend(true, {}, hintTypeSettings);
					hintTypeSettingsCopy.hintType = hintTypeLabel;
					tableModel.addRow(hintTypeSettingsCopy);
				}
			}
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			var definitionsExtension = {};
			for (var rowValues of values) {
				var hintTypeField = hintTypesMap.get(rowValues["Hints for"]);
				if ( ! definitionsExtension[hintTypeField]) {
					definitionsExtension[hintTypeField] = [];
				}
				var hints = {};
				hints.LABEL = rowValues["Label"];
				hints.TYPE = rowValues["Type"];
				if (rowValues["Min"]) { hints.MIN_COUNT = Number(rowValues["Min"]); };
				if (rowValues["Max"]) { hints.MAX_COUNT = Number(rowValues["Max"]); };
				hints.ANNOTATION_PROPERTIES = rowValues.extraValues;
				definitionsExtension[hintTypeField].push(hints);				
			}
			return definitionsExtension;
		}
		return tableModel;
	}

	this._getAnnotationPropertiesTableModel = function() {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [{ label : "Annotation property type" }, { label : "Mandatory", width : "160px" }];
		tableModel.rowBuilders = {
			"Annotation property type" : (function(rowData) {
				var options = this._settingsFormController.getAnnotationPropertyTypeOptions();
				return FormUtil.getDropdown(options.map(function(option) {
					return {
						label : Util.getDisplayNameFromCode(option),
						value : option,
						selected : option === rowData.TYPE,
					};
				}), "select property type");
			}).bind(this),
			"Mandatory" : function(rowData) {
				var $checkbox = $("<input>", { type : "checkbox" });
				if (rowData.MANDATORY) {
					$checkbox.attr("checked", true);
				}
				return $checkbox;
			},
		}
		// add data - done by caller
		// transform output
		tableModel.valuesTransformer = function(values) {
			return values.map(function(rowValues) {
				return {
					TYPE : rowValues["Annotation property type"],
					MANDATORY : rowValues["Mandatory"],
				};
			});
		}
		return tableModel;
	}

	//
	// general
	//

	this._getSingleColumnDropdownTableModel = function(params) {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [{ label : params.columnName }];
		tableModel.rowBuilders[params.columnName] = function(rowData) {
			return FormUtil.getDropdown(params.options.map(function(option) {
				return {
					label : Util.getDisplayNameFromCode(option),
					value : option,
					selected : option === rowData,
				};
			}), params.placeholder);
		}
		// add data
		for (var item of params.initialValues) {
			tableModel.addRow(item);
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			return values.map(function(value) {
				return value[params.columnName];
			});
		}
		return tableModel;
	}

	this._getTableModel = function() {
		var tableModel = {};
		tableModel.columns = []; // array of elements with label and optional width
		tableModel.rowBuilders = {}; // key (column name); value (function to build widget)
		tableModel.rows = []; // array of maps with key (column name); value (widget)
		tableModel.rowExtraBuilder = null; // optional builder for expandable component per row
		tableModel.rowExtras = []; // array of extras corresponding to the rows
		tableModel.rowExtraModels = [] // row extra models can be placed here. models need getValues() function
		tableModel.dynamicRows = false; // allows adding / removing rows
		tableModel.fullWidth = true; // table is drawn using the full width if true
		tableModel.valuesTransformer = function(values) { return values }; // optional transformer
		tableModel.getValues = (function() {
			var values = [];
			for (var i of Object.keys(tableModel.rows)) {
				var row = tableModel.rows[i];
				var rowValues = {};
				for (var column of tableModel.columns) {
					var $widget = row[column.label];
					var value = this._getWidgetValue($widget);
					rowValues[column.label] = value;
				}
				if (tableModel.rowExtraModels.length === tableModel.rows.length) {
					rowValues.extraValues = tableModel.rowExtraModels[i].getValues();
				}
				values.push(rowValues);
			}
			return tableModel.valuesTransformer(values);
		}).bind(this);
		tableModel.addRow = function(rowData) {
			var rowWidgets = {};
			for (var column of tableModel.columns) {
				var rowBuilder = tableModel.rowBuilders[column.label];
				rowWidgets[column.label] = rowBuilder(rowData);
			}
			tableModel.rows.push(rowWidgets);
			if (tableModel.rowExtraBuilder) {
				tableModel.rowExtras.push(tableModel.rowExtraBuilder(rowData));
			}
			return rowWidgets;
		};
		return tableModel;
	}

	this._getWidgetValue = function($widget) {
		if ($widget.is("span")) {
			return $widget.text();
		} else if ($widget.is("input") && $widget.attr("type") === "checkbox") {
			return $widget.is(":checked");
		} else {
			return $widget.val();
		}
	}

	this._getTable = function(tableModel) {
		var $table = $("<table>", { class : "table borderless table-compact" });
		if (tableModel.fullWidth != true) {
			$table.css("width", "initial");
		}
		// head
		var $thead = $("<thead>");
        var $trHead = $("<tr>");
		if (tableModel.rowExtraBuilder) {
			$trHead.append($("<th>").css("width", "30px"));
		}
        for (var column of tableModel.columns) {
			var $th = $("<th>").css("vertical-align", "middle").text(column.label);
			if (column.width) {
				$th.css("width", column.width);
			}
            $trHead.append($th);
        }
		// add row button
		if (tableModel.dynamicRows) {
			var $addButton = $("<a>", { class : "btn btn-default" })
						.append($("<span>", { class : "glyphicon glyphicon-plus" } ));
			if (this._settingsFormModel.mode === FormMode.VIEW) {
				$addButton.addClass("disabled");
			} else {
				$addButton.on("click", (function() {
					var rowWidgets = tableModel.addRow({});
					if (tableModel.rowExtraBuilder) {
						var $extra = tableModel.rowExtras[tableModel.rowExtras.length-1];
						this._addRow($tbody, tableModel, rowWidgets, $extra);
					} else {
						this._addRow($tbody, tableModel, rowWidgets);
					}
				}).bind(this))
			}
			$trHead.append($("<th>").css("width", "80px").append($addButton));
		}
		$thead.append($trHead);
		$table.append($thead);
		// body
        var $tbody = $("<tbody>");
		for (var i of Object.keys(tableModel.rows)) {
			var row = tableModel.rows[i];

			if (tableModel.rowExtraBuilder) {
				// add extra as row after actual row
				var $extra = tableModel.rowExtras[i];
				this._addRow($tbody, tableModel, row, $extra);
			} else {
				this._addRow($tbody, tableModel, row);				
			}
		}
		$table.append($tbody);
		return $table
	}

	this._addRow = function($tbody, tableModel, tableModelRow, $extra) {
		var $tr = $("<tr>");
		$tbody.append($tr);
		var $extraRow = null;

		// add expand / collapse for extra
		if ($extra) {
			// create extra row
			var colspan = tableModel.columns.length + 1;
			if (tableModel.dynamicRows) {
				colspan++;
			}
			$extraRow = $("<tr>")
				.append($("<td>").css({"padding-left" : "50px", "padding-right" : "50px"}).attr("colspan", colspan)
					.append($extra));
			// hiding / showing extra row
			$extraRow.hide();
			var $td = $("<td>");
			var $expandCollapse = $("<div>", { class : "glyphicon glyphicon-plus-sign" }).css("vertical-align", "middle");
			$expandCollapse.on("click", (function($extraRow, $expandCollapse) {
				$extraRow.toggle();
				if ($extraRow.is(":visible")) {
					$expandCollapse.removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
				} else {
					$expandCollapse.removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
				}
			}).bind(this, $extraRow, $expandCollapse));
			$tr.append($td);
			$td.append($expandCollapse);
		}

		for (var column of tableModel.columns) {
			var $td = $("<td>");
			$tr.append($td);
			var $widget = tableModelRow[column.label];
			$td.append($widget);
			// disbale widget if in view mode
			if (this._settingsFormModel.mode === FormMode.VIEW) {
				$widget.prop("disabled", true);
			}
		}
		// remove row button if in edit mode
		if (tableModel.dynamicRows) {
			$removeButton = $("<a>", { class : "btn btn-default" })
						.append($("<span>", { class : "glyphicon glyphicon-minus" }));
			if (this._settingsFormModel.mode === FormMode.VIEW) {
				$removeButton.addClass("disabled");
			} else {
				$removeButton.on("click", function() {
					$tr.remove();
					if ($extraRow) {
						$extraRow.remove();
					}
					var rowIndex = tableModel.rows.indexOf(tableModelRow);
					tableModel.rows.splice(rowIndex, 1);
					if (tableModel.rowExtraModels) {
						tableModel.rowExtraModels.splice(rowIndex, 1);
					}
				});
			}
			$tr.append($("<td>").append($removeButton));
		}
		// add extra row
		if ($extraRow) {
			$tbody.append($extraRow);
		}
	}

	this._getFieldset = function($container, legendText, key, dontRestoreState) {
		var $fieldsetOwner = $("<div>");
		if ( ! dontRestoreState) {
			$fieldsetOwner.hide();
		}
		var $fieldset = $("<div>");
		var $legend = $("<legend>").text(legendText);
		$legend.prepend(FormUtil.getShowHideButton($fieldset, key, dontRestoreState, (function($fieldsetOwner) {
			$fieldsetOwner.show();
		}).bind(this, $fieldsetOwner)));
		$fieldsetOwner.append($legend).append($fieldset);
		$container.append($fieldsetOwner);
		return $fieldset;
	}

}
