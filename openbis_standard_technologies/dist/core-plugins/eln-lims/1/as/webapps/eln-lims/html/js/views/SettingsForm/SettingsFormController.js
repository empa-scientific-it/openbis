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

function SettingsFormController(mainController, settingsSample, mode) {
	this._mainController = mainController;
	this._settingsFormModel = new SettingsFormModel(settingsSample, mode);
	this._settingsFormView = new SettingsFormView(this, this._settingsFormModel);
	this._settingsManager = new SettingsManager(this._mainController.serverFacade);

	this.init = function(views) {
	    var _this = this;
	    mainController.serverFacade.getCustomWidgetSettings(function(customWidgetSettings) {
	        var profileToEdit = null;
            if(settingsSample.properties["$ELN_SETTINGS"]) {
                profileToEdit = JSON.parse(settingsSample.properties["$ELN_SETTINGS"]);
            }
            var initialGroupSettings = profileToEdit && Object.keys(profileToEdit).length == 2 && profileToEdit["inventorySpaces"] && profileToEdit["inventorySpacesReadOnly"];
            if(initialGroupSettings) { // Special initialisation for group settings
                var newProfile = jQuery.extend(true, {}, profile);
                newProfile["inventorySpaces"] = profileToEdit["inventorySpaces"];
                newProfile["inventorySpacesReadOnly"] = profileToEdit["inventorySpacesReadOnly"];
                profileToEdit = newProfile;
            } else if(!settingsSample.properties["$ELN_SETTINGS"]) { // Special initialisation for general settings
                var newProfile = jQuery.extend(true, {}, profile);
                newProfile["inventorySpaces"] = profile.inventorySpacesPostFixes;
                newProfile["inventorySpacesReadOnly"] = profile.inventorySpacesReadOnlyPostFixes;
                profileToEdit = newProfile;
            }

            require(["as/dto/sample/id/SampleIdentifier"], function(SampleIdentifier) {
				var sampId = new SampleIdentifier(settingsSample.identifier);
				mainController.openbisV3.getRights([sampId], null).done(function(rightsByIds) {
					_this._settingsFormModel.sampleRights = rightsByIds[sampId];
					_this._settingsFormModel.customWidgetSettings = customWidgetSettings;
					_this._settingsFormView.repaint(views, profileToEdit);
				});
			});
	    });
	}

	this.save = function(settings, widgetSettings) {
	    if(widgetSettings) { // Validate Widget Settings
            for(var idx = 0; idx < widgetSettings.length; idx++) {
                var widget = widgetSettings[idx];
                var property = profile.getPropertyType(widget["Property Type"]);
                switch(widget.Widget) {
                    case "Word Processor":
                        if(property.dataType !== "MULTILINE_VARCHAR") {
                            Util.showUserError("Word Processor only works with MULTILINE_VARCHAR data type, " + property.code + " is " + property.dataType + ".", function() {}, true);
                            return;
                        }
                        break;
                    case "Spreadsheet":
                        if(property.dataType !== "XML") {
                            Util.showUserError("Spreadsheet only works with XML data type, " + property.code + " is " + property.dataType + ".", function() {}, true);
                            return;
                        }
                        break;
                }
            }
	    }

	    var _this = this;
	    var onSave = function() {
	        _this._settingsManager.validateAndsave(_this._settingsFormModel.settingsSample, settings, (function() {
                Util.reloadApplication("Application will be reloaded to apply the new settings.");
            }));
	    }

        Util.blockUI();
	    if(widgetSettings) {
            this._mainController.serverFacade.setCustomWidgetSettings(widgetSettings, onSave);
        } else {
            onSave();
        }

	}

	this.getAllDatasetTypeCodeOptions = this._settingsManager.getAllDatasetTypeCodeOptions;
	this.getForcedDisableRTFOptions = this._settingsManager.getForcedDisableRTFOptions;
	this.getForcedMonospaceFontOptions = this._settingsManager.getForcedMonospaceFontOptions;
	this.getInventorySpacesOptions = this._settingsManager.getInventorySpacesOptions;
    this.getInventorySpacesReadOnlyOptions = this._settingsManager.getInventorySpacesReadOnlyOptions;
	this.getSampleTypeOptions = this._settingsManager.getSampleTypeOptions;
	this.getAnnotationPropertyTypeOptions = this._settingsManager.getAnnotationPropertyTypeOptions;

}
