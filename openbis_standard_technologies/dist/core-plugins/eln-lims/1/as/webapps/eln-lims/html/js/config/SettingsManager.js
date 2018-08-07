function SettingsManager(serverFacade) {

    this._serverFacade = serverFacade;

	this.validateAndsave = function(settingsSample, settings, doneCallback) {
		var _this = this;
		var errors = this._validateSettings(settings);
		if (errors.length > 0) {
			Util.showError(FormUtil._getSanitizedErrorString("Settings validation errors:", errors));
		} else {
			settingsSample.properties = { "ELN_SETTINGS" : JSON.stringify(settings) };
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
				    		return 1;
				    } else {
				    		return -1;
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
				for(var sIdx = 0; sIdx < settingsObjects.length; sIdx++) {
					var settingsObject = settingsObjects[sIdx];
					if (settingsObject && settingsObject.properties && settingsObject.properties.ELN_SETTINGS) {
						var settings = JSON.parse(settingsObject.properties.ELN_SETTINGS);
						if (settings) {
							var errors = this._validateSettings(settings);
							if (errors.length > 0) {
								console.log("The settings contain the following errors:");
								console.log(errors);
							}
							this.applySettingsToProfile(settings, (profileToEditOrNull)?profileToEditOrNull:profile);
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

	this.getSampleTypeOptions = function() {
		return profile.getAllSampleTypes().map( function(_) { return _.code; } )
	}

	this.getAnnotationPropertyTypeOptions = function() {
		return profile.allPropertyTypes.map( function(_) { return _.code; } );
	}

	this.applySettingsToProfile = function(settings, targetProfile) {
		// fields that get overwritten with settings if found
		var fieldsToOverride = [
			"dataSetTypeForFileNameMap",
			"forcedDisableRTF",
			"forceMonospaceFont"
		];
		for (var field of fieldsToOverride) {
			if (settings[field]) {
				targetProfile[field] = settings[field];
			}
		}
		
		// array fields to add values to defaults
		var fieldsToAdd = [
			"inventorySpaces"
		];
		for (var field of fieldsToAdd) {
			if (settings[field]) {
				for(var fIdx = 0; fIdx < settings[field].length; fIdx++) {
					var settingsValue = settings[field][fIdx];
					if(($.inArray(settingsValue, targetProfile[field]) === -1)) {
						targetProfile[field].push(settingsValue);
					}
				}
			}
		}
		
		// main menu, checks menu items one by one to keep new ones
		for (var menuItem of Object.keys(targetProfile.mainMenu)) {
			if (settings.mainMenu[menuItem] != undefined) {
				targetProfile.mainMenu[menuItem] = settings.mainMenu[menuItem];
			}
		}
		
		// sampleTypeDefinitionsExtension gets overwritten with settings if found
		for (var sampleType of Object.keys(settings.sampleTypeDefinitionsExtension)) {
			profile.sampleTypeDefinitionsExtension[sampleType] = settings.sampleTypeDefinitionsExtension[sampleType];
			// Add the types to hide == not show
			if(!settings.sampleTypeDefinitionsExtension[sampleType].SHOW) {
				targetProfile.hideTypes["sampleTypeCodes"].push(sampleType);
			} else if($.inArray(sampleType, targetProfile.hideTypes["sampleTypeCodes"]) !== -1) {
				var indexToRemove = $.inArray(sampleType, targetProfile.hideTypes);
				targetProfile.hideTypes["sampleTypeCodes"] = targetProfile.hideTypes["sampleTypeCodes"].splice(indexToRemove, 1);
			}
		}
	}

    this._validateSettings = function(settings) {
		var errors = [];
		this._validateForcedDisableRTF(settings, errors);
		this._validateForcedMonospaceFont(settings, errors);
		this._validateInventorySpaces(settings, errors);
		this._validateDataSetTypeForFileNameMap(settings, errors);
		this._validateSampleTypeDefinitionsExtension(settings, errors);
		return errors;
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

	this._validateInventorySpaces = function(settings, errors) {
		if (settings.inventorySpaces) {
			for (var item of settings.inventorySpaces) {
				if (this.getInventorySpacesOptions().indexOf(item) === -1) {
					errors.push(item + " is not space.");
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
