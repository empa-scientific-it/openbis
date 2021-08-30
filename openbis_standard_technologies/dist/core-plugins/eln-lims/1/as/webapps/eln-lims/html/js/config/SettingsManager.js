function SettingsManager(serverFacade) {

    this._serverFacade = serverFacade;

	this.validateAndsave = function(settingsSample, settings, doneCallback) {
		var _this = this;
		var errors = this._validateSettings(settings);
		if (errors.length > 0) {
			Util.showError(FormUtil._getSanitizedErrorString("Settings validation errors:", errors));
		} else {
			settingsSample.properties = { "$ELN_SETTINGS" : JSON.stringify(settings) };
			this._serverFacade.updateSample(settingsSample, function(ok) {
				if(ok) {
					_this.applySettingsToProfile(settings, profile);
					doneCallback();
				}
			});
		}
	}

	this.loadSettings = function(callback) {
		this._serverFacade.searchSamples({ 	"sampleTypeCode" : "GENERAL_ELN_SETTINGS",
											"withProperties" : true }, (function(settingsObjects) {
			if(settingsObjects && settingsObjects.length > 0) {
				settingsObjects.sort(function(a, b) {
				    if(a.identifier === "/ELN_SETTINGS/GENERAL_ELN_SETTINGS") { // Global settings are applied first to be overriden by others
				    		return -1;
				    } else {
				    		return 1;
				    }
				});
				callback(settingsObjects);
			} else {
				callback();
			}
		}).bind(this))
	}


	// Loads settings and logs validation errors to console.
	// Applies the settings to the profile even if they are invalid.
    this.loadSettingsAndApplyToProfile = function(doneCallback, profileToEditOrNull) {
		this.loadSettings((function(settingsObjects) {
			if(settingsObjects) {
			    // Store settings objects globally to use latter
			    profile.settingsObjects = settingsObjects;
				for(var sIdx = 0; sIdx < settingsObjects.length; sIdx++) {
					var settingsObject = settingsObjects[sIdx];
					if (settingsObject && settingsObject.properties && (settingsObject.properties["ELN_SETTINGS"] || settingsObject.properties["$ELN_SETTINGS"])) {
						var settings = settingsObject.properties["$ELN_SETTINGS"];
						if(!settings) {
							settings = settingsObject.properties["ELN_SETTINGS"];
						}
						settings = JSON.parse(settings);
						if (settings) {
							var errors = this._validateSettings(settings);
							if (errors.length > 0) {
								console.log("The settings contain the following errors:");
								console.log(errors);
							}
							this.applySettingsToProfile(settings, (profileToEditOrNull)?profileToEditOrNull:profile, (profile.isMultiGroup && sIdx > 0));
						}
					}
				}
			}
			doneCallback();
		}).bind(this));
    }

	this.getAllDatasetTypeCodeOptions = function() {
		return profile.allDatasetTypeCodes;
	}

	this.getForcedDisableRTFOptions = function() {
		return profile.allPropertyTypes.map(function(_) { return _.code; });
	}

	this.getForcedMonospaceFontOptions = function() {
		return profile.allPropertyTypes.map(function(_) { return _.code; });
	}

	this.getInventorySpacesOptions = function() {
		return profile.allSpaces;
	}

    this.getInventorySpacesReadOnlyOptions = function() {
		return profile.allSpaces;
	}

	this.getSampleTypeOptions = function() {
		return profile.getAllSampleTypes().map( function(_) { return _.code; } )
	}

	this.getAnnotationPropertyTypeOptions = function() {
		return profile.allPropertyTypes.map( function(_) { return _.code; } );
	}

    /*
     * Overall settings should work following these use cases:
     *  Single Instance: Only the "General Settings" are applied
     *  User belongs to one group: "General Settings" are applied first, the only group second, both with isMergeGroup = false
     *  User belongs to more than one group: "General Settings" are applied first, any additional group with isMergeGroup = true
     */
   this.applySettingsToProfile = function(settings, targetProfile, isMergeGroup) {

     	    // Main menu Items
     	    for(var menuItem in settings.mainMenu) {
     	        if(isMergeGroup && targetProfile.mainMenu[menuItem] !== undefined) { // Merge found values
     	            targetProfile.mainMenu[menuItem] = targetProfile.mainMenu[menuItem] || settings.mainMenu[menuItem];
     	        } else { // Replaces or sets value
     	            targetProfile.mainMenu[menuItem] = settings.mainMenu[menuItem];
     	        }

     	    }

             // Miscellaneous
             var miscellaneousFields = ["hideSectionsByDefault", "showDatasetArchivingButton"]
             for(var miscellaneousField of miscellaneousFields) {
                if(isMergeGroup && targetProfile[miscellaneousField] != undefined) { // Merge found values
                    targetProfile[miscellaneousField] = targetProfile[miscellaneousField] || settings[miscellaneousField];
                } else { // Replaces or sets value
     	            targetProfile[miscellaneousField] = settings[miscellaneousField];
     	        }
     	    }

             // Forced Disable RTF
             if(isMergeGroup) { // Merge found values
                targetProfile["forcedDisableRTF"] = targetProfile["forcedDisableRTF"].concat(settings["forcedDisableRTF"]).unique();
             } else { // Replaces or sets values
                targetProfile["forcedDisableRTF"] = settings["forcedDisableRTF"];
             }

             // Forced Monospace Font
             if(isMergeGroup) { // Merge found values
                targetProfile["forceMonospaceFont"] = targetProfile["forceMonospaceFont"].concat(settings["forceMonospaceFont"]).unique();
             } else { // Replaces or sets values
                targetProfile["forceMonospaceFont"] = settings["forceMonospaceFont"];
             }



             // Inventory Spaces
             if(isMergeGroup) { // Merge found values
                targetProfile["inventorySpaces"] = targetProfile["inventorySpaces"].concat(settings["inventorySpaces"]).unique();
                targetProfile["inventorySpacesReadOnly"] = targetProfile["inventorySpacesReadOnly"].concat(settings["inventorySpacesReadOnly"]).unique();
             } else { // Replaces or sets values
                targetProfile["inventorySpaces"] = settings["inventorySpaces"];
                targetProfile["inventorySpacesReadOnly"] = settings["inventorySpacesReadOnly"];
             }


             // Dataset Types from File Extension
             if(isMergeGroup) { // Merge found values
                for(var idxTp = 0; idxTp < settings.dataSetTypeForFileNameMap.length; idxTp++) {
                     targetProfile.dataSetTypeForFileNameMap.push(settings.dataSetTypeForFileNameMap[idxTp]);
                 }
             } else { // Replaces or sets values
                targetProfile.dataSetTypeForFileNameMap = settings.dataSetTypeForFileNameMap;
             }


            // Object Type definitions Extension : Gets values merged and exiting sampleTypeDefinitionsExtension values replaced with last settings found and found boolean values merged with OR

                 // For every key on the settings to apply, merge with logic OR
                 var sampleTypeDefinitionBooleans = {
                     "ENABLE_STORAGE" : true,
                     "SAMPLE_CHILDREN_ANY_TYPE_DISABLED" : true,
                     "SAMPLE_CHILDREN_DISABLED" : true,
                     "SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
                     "SAMPLE_PARENTS_DISABLED" : true,
                     "SHOW" : true,
                     "SHOW_ON_NAV" : true,
                     "USE_AS_PROTOCOL" : true,
                 }

     		for (var sampleTypeCode of Object.keys(settings.sampleTypeDefinitionsExtension)) {

     			// If doesn't exist, it is created
     			if(!targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode]) {
     			    targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode] = {};
     			}

     			for(var key in settings.sampleTypeDefinitionsExtension[sampleTypeCode]) { // Key by Key, in case there is new Keys not available in the old config
                     // Known boolean values are merged, others are replaced

                     // Missing values are just replaced
                     if(targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode][key] === undefined) {
                         targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode][key] = settings.sampleTypeDefinitionsExtension[sampleTypeCode][key];
                     } else { // Existing values are merged or replaced
                        if(isMergeGroup) {
                            if(key in sampleTypeDefinitionBooleans) { // Merge with logic OR
                                targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode][key] = targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode][key] || settings.sampleTypeDefinitionsExtension[sampleTypeCode][key]
                            } else { // Random to keep or to use new one
                                var keep = Math.random() < 0.5;
                                if(keep) {
                                    // Keep current
                                } else { // Replace
                                    targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode][key] = settings.sampleTypeDefinitionsExtension[sampleTypeCode][key];
                                }
                            }
                        } else { // Replace
                            targetProfile.sampleTypeDefinitionsExtension[sampleTypeCode][key] = settings.sampleTypeDefinitionsExtension[sampleTypeCode][key];
                        }
                     }

                 }

     			// Remove current type from hideTypes, if present
     			if($.inArray(sampleTypeCode, targetProfile.hideTypes["sampleTypeCodes"]) !== -1) {
     				var indexToRemove = $.inArray(sampleTypeCode, targetProfile.hideTypes["sampleTypeCodes"]);
     				targetProfile.hideTypes["sampleTypeCodes"].splice(indexToRemove, 1);
     			}

     			// Add current type to hideTypes, if not SHOW
     			if(!settings.sampleTypeDefinitionsExtension[sampleTypeCode].SHOW) {
     				targetProfile.hideTypes["sampleTypeCodes"].push(sampleTypeCode);
     			}
     		}
     }

    this._validateSettings = function(settings) {
		var errors = [];
		this._validateSpaces(settings, errors);
		this._validateForcedDisableRTF(settings, errors);
		this._validateForcedMonospaceFont(settings, errors);
		this._validateDataSetTypeForFileNameMap(settings, errors);
		this._validateSampleTypeDefinitionsExtension(settings, errors);
		return errors;
	}

    this._validateSpaces = function(settings, errors) {
        if(settings.inventorySpaces) {
            for(var idx = 0; idx < settings.inventorySpaces.length; idx++) {
                if(!settings.inventorySpaces[idx]) {
                    errors.push("Empty value found instead of a space, please delete it before save.");
                }
            }
        }
        if(settings.inventorySpacesReadOnly) {
            for(var idx = 0; idx < settings.inventorySpaces.length; idx++) {
                if(!settings.inventorySpaces[idx]) {
                    errors.push("Empty value found instead of a space, please delete it before save.");
                }
            }
        }
    }

	this._validateSampleTypeDefinitionsExtension = function(settings, errors) {
		if (settings.sampleTypeDefinitionsExtension) {
			for (var sampleType of Object.keys(settings.sampleTypeDefinitionsExtension)) {
				// not checking for valid sample type
				// config can contain entries which are not yet defined
				// but if sample type exists, more strict validation is used
				var sampleTypeExists = this.getSampleTypeOptions().indexOf(sampleType) !== -1;
				var hints = settings.sampleTypeDefinitionsExtension[sampleType];
				var errorPrefix = "Error in definitions extension for sample type " + sampleType + ": ";
				for (var hintType of Object.keys(hints)) {
					if (hintType == "undefined") {
						errors.push(errorPrefix + "Hint type (children/parents) is undefined");
					}
					if (hintType === "SAMPLE_CHILDREN_HINT" || hintType === "SAMPLE_PARENTS_HINT") {
						var hintArray = hints[hintType];
						for (var hint of hintArray) {
							if (hint.LABEL == null || hint.LABEL.length === 0) {
								errors.push(errorPrefix + "Sample type definitions hint labels can't be empty.");
							}
							if (typeof(hint.TYPE) !== "string") {
								errors.push(errorPrefix + "Sample type definitions hint type (" +
											hint.TYPE +
											 ") must be a string.");
							} else if (sampleTypeExists && this.getSampleTypeOptions().indexOf(hint.TYPE) === -1) {
								errors.push(errorPrefix + "Sample type definitions hint type (" +
											hint.TYPE +
											 ") must be an existing property type.");
							}
							if (hint.MIN_COUNT != null && (typeof(hint.MIN_COUNT) !== "number" || isNaN(hint.MIN_COUNT))) {
								errors.push(errorPrefix + "Sample type definitions hint MIN_COUNT must be a number.");
							}
							if (hint.MAX_COUNT != null && (typeof(hint.MAX_COUNT) !== "number" || isNaN(hint.MAX_COUNT))) {
								errors.push(errorPrefix + "Sample type definitions hint MAX_COUNT must be a number.");
							}
							if (hint.ANNOTATION_PROPERTIES) {
								var propertyTypeOptions = this.getAnnotationPropertyTypeOptions();
								for (var annotationProperty of hint.ANNOTATION_PROPERTIES) {
									if (sampleTypeExists && (annotationProperty.TYPE == null || propertyTypeOptions.indexOf(annotationProperty.TYPE) === -1)) {
										errors.push(errorPrefix + "Annotation properties must have an existing property type but is: " +
													annotationProperty.TYPE);
									}
									if (annotationProperty.MANDATORY == null || typeof(annotationProperty.MANDATORY) !== "boolean") {
										errors.push(errorPrefix + "Annotation properties must have a boolean MANDATORY field but is: "
													+ annotationProperty.MANDATORY);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	this._validateForcedDisableRTF = function(settings, errors) {
		if (settings.forcedDisableRTF) {
			for (var item of settings.forcedDisableRTF) {
				if (this.getForcedDisableRTFOptions().indexOf(item) === -1) {
					errors.push(item + " is not a property type.");
				}
			}
		}
	}

	this._validateForcedMonospaceFont = function(settings, errors) {
		if (settings.forceMonospaceFont) {
			for (var item of settings.forceMonospaceFont) {
				if (this.getForcedMonospaceFontOptions().indexOf(item) === -1) {
					errors.push(item + " is not a property type.");
				}
			}
		}
	}

	this._validateDataSetTypeForFileNameMap = function(settings, errors) {
		if (settings.dataSetTypeForFileNameMap) {
			for (var dataSetTypeForFileName of settings.dataSetTypeForFileNameMap) {
				if (dataSetTypeForFileName.dataSetType == null) {
					errors.push("dataSetTypeForFileNameMap must contain a field named dataSetType.");
				} else if (this.getAllDatasetTypeCodeOptions().indexOf(dataSetTypeForFileName.dataSetType) === -1) {
					errors.push("Dataset type " + 
								dataSetTypeForFileName.dataSetType + 
								" is not allowed. Available types: " +
								this.getAllDatasetTypeCodeOptions().join(", ") +
								".");
				}
				if (dataSetTypeForFileName.fileNameExtension == null) {
					errors.push("dataSetTypeForFileNameMap must contain a field named fileNameExtension.");
				} else if (dataSetTypeForFileName.fileNameExtension.length == 0) {
					errors.push("Filename extension can't be empty.");
				}
			}
		}
	}

}
