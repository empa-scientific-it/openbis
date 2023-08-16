var FormMode = {
    CREATE : 0,
    EDIT : 1,
    VIEW : 2
}

var FormUtil = new function() {
	this.profile = null;
	
	//
	// Form css classes
	//
	this.shortformColumClass = 'col-md-9'
	this.formColumClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.shortControlColumnClass = 'col-md-5';
	this.controlColumnClass = 'col-md-9';
	this.controlColumnClassBig = 'col-md-9';

    this.warningColor = '#e71616'

	//
	// Sample Relationship Annotations 19.X & 20.X
	//

	this.getAnnotationsFromSample = function(sample, type) {
        var typeAnnotations = {};
        if(profile.enableNewAnnotationsBackend) { // Used by openBIS 20.X
            typeAnnotations = this.getAnnotationsFromSampleV3(sample, type);
        } else { // Used by openBIS 19.X
            typeAnnotations = this.getAnnotationsFromSampleV1(sample, type);
        }
        return typeAnnotations;
	}

	//
	// Sample Relationship Annotations 19.X
	//

	this.addAnnotationSlotForSample = function(stateObj, sample) {
		var sampleAnnotations = stateObj[sample.permId];
		if(!sampleAnnotations) {
			sampleAnnotations = {};
			stateObj[sample.permId] = sampleAnnotations;
		}
		
		sampleAnnotations["identifier"] =  sample.identifier; //Adds code to the annotations if not present
		sampleAnnotations["sampleType"] =  sample.sampleTypeCode; //Adds sampleType code to the annotations if not present
		return sampleAnnotations;
	}
	
	this.writeAnnotationForSample = function(stateObj, sample, propertyTypeCode, propertyValue) {
		var sampleAnnotations = this.addAnnotationSlotForSample(stateObj, sample);
		
		if(propertyTypeCode && propertyValue !== null && propertyValue !== undefined) {
			sampleAnnotations[propertyTypeCode] = propertyValue;
		}
	}
	
	this.deleteAnnotationsFromPermId = function(stateObj, permId) {
		delete stateObj[permId];
	}
	
	this.getXMLFromAnnotations = function(stateObj) {
		var rootNode = document.createElementNS("http://www.w3.org/1999/xhtml", "root"); //The namespace should be ignored by both ELN and openBIS parsers
		
		for(var permId in stateObj) {
			var sampleNode	= document.createElementNS("http://www.w3.org/1999/xhtml", "Sample"); //Should not add the namespace since is the same as the root
			sampleNode.setAttributeNS(null, "permId", permId); //Should not add the namespace
			
			for(var propertyTypeCode in stateObj[permId]) {
				var propertyTypeValue = stateObj[permId][propertyTypeCode];
				sampleNode.setAttributeNS(null, propertyTypeCode, propertyTypeValue); //Should not add the namespace
			}
			
			rootNode.appendChild(sampleNode);
		}
		
		var serializer = new XMLSerializer();
		var xmlDoc = serializer.serializeToString(rootNode);
		return xmlDoc;
	}
	
	this.getAnnotationsFromSampleV1 = function(sample, type) {
		var field = sample.properties["$ANNOTATIONS_STATE"];
		var stateFieldValue = Util.getEmptyIfNull(field);
		if(stateFieldValue === "") {
			stateFieldValue = undefined;
			sample.properties["$ANNOTATIONS_STATE"] = undefined;
		}
		var allAnnotations = this.getAnnotationsFromField(stateFieldValue);
		var typeAnnotations = {};
        if(sample.parents && (type === 'PARENTS' || !type)) {
            for(var pIdx = 0; pIdx < sample.parents.length; pIdx++) {
                var parentPermId =  sample.parents[pIdx].permId;
                if(parentPermId in allAnnotations) {
                    typeAnnotations[parentPermId] = allAnnotations[parentPermId];
                }
            }
        }
        if(sample.children && (type === 'CHILDREN' || !type)) {
            for(var cIdx = 0; cIdx < sample.children.length; cIdx++) {
                var childPermId =  sample.children[cIdx].permId;
                if(childPermId in allAnnotations) {
                    typeAnnotations[childPermId] = allAnnotations[childPermId];
                }
            }
        }
        return typeAnnotations;
	}
	
	this.getAnnotationsFromField = function(field) {
		var stateObj = {};
		var stateFieldValue = Util.getEmptyIfNull(field);

		if(stateFieldValue === "") {
			return stateObj;
		}
		var xmlDoc = new DOMParser().parseFromString(stateFieldValue, 'text/xml');
		var samples = xmlDoc.getElementsByTagName("Sample");
		for(var i = 0; i < samples.length; i++) {
			var sample = samples[i];
			var permId = sample.attributes["permId"].value;
			for(var j = 0; j < sample.attributes.length; j++) {
				var attribute = sample.attributes[j];
				if(attribute.name !== "permId") {
					if(!stateObj[permId]) {
						stateObj[permId] = {};
					}
					stateObj[permId][attribute.name] = attribute.value;
				}
			}
		}
		return stateObj;
	}

	//
	// Sample Relationship Annotations 20.X
	//

    this.getAnnotationsFromSampleV3 = function(sample, type) {
        var typeAnnotations = {};
        if(sample.parents && (type === 'PARENTS' || !type)) {
            for(var parentPermId in sample.parentsRelationships) {
                var parentAnnotations = {};
                for(var parentAnnotationKey in sample.parentsRelationships[parentPermId].parentAnnotations) {
                    parentAnnotations[parentAnnotationKey] = sample.parentsRelationships[parentPermId].parentAnnotations[parentAnnotationKey];
                }
                typeAnnotations[parentPermId] = parentAnnotations;
            }
        }
        if(sample.children && (type === 'CHILDREN' || !type)) {
            for(var childPermId in sample.childrenRelationships) {
                var childAnnotations = {};
                for(var childAnnotationKey in sample.childrenRelationships[childPermId].childAnnotations) {
                    childAnnotations[childAnnotationKey] = sample.childrenRelationships[childPermId].childAnnotations[childAnnotationKey];
                }
                typeAnnotations[childPermId] = childAnnotations;
            }
        }
        return typeAnnotations;
	}

	//
	// Standard Form Fields
	//
	
	this.getDropDownToogleWithSelectedFeedback = function(prefixElement, labelWithEvents, isSelectedFeedback, clickCallback) {
		var $dropDownToogle = $('<span>', { class : 'dropdown' });
		if(prefixElement) {
			$dropDownToogle.append(prefixElement);
		}
		$dropDownToogle.append($('<button>', { 'href' : '#', 'data-toggle' : 'dropdown', 'class' : 'dropdown-toggle btn btn-default'}).append($('<b>', { 'class' : 'caret' })));
		
		var $dropDownToogleOptions = $('<ul>', { class : 'dropdown-menu', 'role' : 'menu' });
		$dropDownToogle.append($dropDownToogleOptions);
		
		for(var i = 0; i < labelWithEvents.length; i++) {
			
			var selectedFeedback = $('<span>', { 'id' : 'dropdown-' + labelWithEvents[i].id });
			
			if(isSelectedFeedback && i === 0) {
				selectedFeedback.append("<span class='glyphicon glyphicon-ok'></span>");
			}
			
			var $a = $('<a>', { class : '', 'title' : labelWithEvents[i].title }).append(selectedFeedback).append('&nbsp;').append(labelWithEvents[i].title);
			
			var clickFunction = function(labelWithEvents, selectedIndex, isSelectedFeedback) {
				return function() {
					if(isSelectedFeedback) {
						for(var j = 0; j < labelWithEvents.length; j++) {
							$("#" + 'dropdown-' + labelWithEvents[j].id).empty();
							if(j === selectedIndex) {
								$("#" + 'dropdown-' + labelWithEvents[j].id).append("<span class='glyphicon glyphicon-ok'></span>");
							}
						}
					}
					
					labelWithEvents[selectedIndex].href();
					
					if(clickCallback) {
						clickCallback();
					}
				};
			}
			
			$a.click(clickFunction(labelWithEvents, i, isSelectedFeedback));
			$dropDownToogleOptions.append($('<li>', { 'role' : 'presentation' }).append($a));
		}	
		return $dropDownToogle;
	}
	
	this.getDefaultBenchDropDown = function(id, isRequired, callbackFunction) {
		this.getDefaultStoragesDropDown(id, isRequired, function($storageDropDown) {
			if(!$storageDropDown) {
				return null;
			}
			for(var i = $storageDropDown.children().length -1; i >= 0 ; i--) {
				var isEmpty = $storageDropDown.children()[i].value === "";
				var isBench = $storageDropDown.children()[i].value.indexOf("BENCH") > -1;
				if(!isEmpty && !isBench){
					$storageDropDown.children()[i].remove();
			    }
			}
			callbackFunction($storageDropDown);
		});
		
	}
	
	this.getDefaultStorageBoxSizesDropDown = function(id, isRequired) {
		if(!this.profile.storagesConfiguration["isEnabled"]) {
			return null;
		}
		var storageBoxesVocabularyProp = this.profile.getPropertyType(this.profile.getStoragePropertyGroup().boxSizeProperty);
		if(!storageBoxesVocabularyProp) {
			return null;
		}
		var $storageBoxesDropDown = this.getFieldForPropertyType(storageBoxesVocabularyProp);
		$storageBoxesDropDown.attr('id', id);
		if (isRequired) {
			$storageBoxesDropDown.attr('required', '');
		}
		return $storageBoxesDropDown;
	}
	
	this.getDefaultStoragesDropDown = function(spaceCode, id, isRequired, callbackFunction) {
	    var spaceGroupPrefix = null;
	    if(spaceCode !== null) {
	        spaceGroupPrefix = SettingsManagerUtils.getSpaceGroupPrefix(spaceCode);
	    }

		if(!this.profile.storagesConfiguration["isEnabled"]) {
			return null;
		}
		
		profile.getStoragesConfiguation(function(storageConfigurations) {
			var $component = $("<select>", {"id" : id, class : 'form-control'});
			if (isRequired) {
				$component.attr('required', '');
			}
			
			$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text("Select a Storage"));
			for(var idx = 0; idx < storageConfigurations.length; idx++) {
			    var storageConfiguration = storageConfigurations[idx];
			    var storageGroupPrefix = SettingsManagerUtils.getSpaceGroupPrefix(storageConfiguration.spaceCode);
			    if(spaceGroupPrefix !== null && spaceGroupPrefix !== storageGroupPrefix) {
			        continue;
			    }
				var label = null;
				if(storageConfiguration.label) {
					label = storageConfiguration.label;
				} else {
					label = storageConfiguration.code;
				}
				var $option = $("<option>");
				$option.attr('spaceCode',storageConfiguration.spaceCode);
				$option.attr('value',storageConfiguration.code);
				$option.text(label)
				$component.append($option);
			}
			callbackFunction($component);
			Select2Manager.add($component);
		});
	}
	
	this.getBoxPositionsDropdown = function(id, isRequired, code) {
		var numRows = null;
		var numCols = null;
		
		if(code) {
			var rowsAndCols = code.split("X");
			numRows = parseInt(rowsAndCols[0]);
			numCols = parseInt(rowsAndCols[1]);
		} else {
			numRows = 0;
			numCols = 0;
		}
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		
		for(var i = 1; i <= numRows; i++) {
			var rowLetter = Util.getLetterForNumber(i);
			for(var j = 1; j <= numCols; j++) {
				$component.append($("<option>").attr('value',rowLetter+j).text(rowLetter+j));
			}
			
		}
		Select2Manager.add($component);
		return $component;
	}

	this.getProjectName = function(projectCode){
		return Util.getDisplayNameFromCode(projectCode)
	}

	this.getExperimentName = function(experimentCode, experimentProperties){
		var nameLabel = experimentProperties[this.profile.propertyReplacingCode];
		if(nameLabel) {
			nameLabel = DOMPurify.sanitize(nameLabel);
		} else {
			nameLabel = experimentCode;
		}
		return nameLabel
	}

	this.getSampleName = function(sampleCode, sampleProperties, sampleTypeCode){
		var nameLabel = sampleProperties[this.profile.propertyReplacingCode];
		if(nameLabel) {
			nameLabel = DOMPurify.sanitize(nameLabel);
		} else if(sampleTypeCode === "STORAGE_POSITION") {
			var storagePropertyGroup = this.profile.getStoragePropertyGroup();
			var boxProperty = sampleProperties[storagePropertyGroup.boxProperty];
			if(!boxProperty) {
				boxProperty = "NoBox";
			}
			var positionProperty = sampleProperties[storagePropertyGroup.positionProperty];
			if(!positionProperty) {
				positionProperty = "NoPos";
			}
			nameLabel = boxProperty + " - " + positionProperty;
		} else {
			nameLabel = sampleCode;
		}
		return nameLabel
	}

	this.getDataSetName = function(dataSetCode, dataSetProperties){
		var nameLabel = dataSetProperties[this.profile.propertyReplacingCode];
		if(nameLabel) {
			nameLabel = DOMPurify.sanitize(nameLabel);
		} else {
			nameLabel = dataSetCode;
		}
		return nameLabel
	}

    this.getSampleTypesOnDropdowns = function(spaceCode) {
        var sampleTypes = profile.getAllSampleTypes();
        var visibleObjectTypeCodesForSpace = SettingsManagerUtils.getVisibleObjectTypesForSpace(spaceCode, SettingsManagerUtils.ShowInSpaceSetting.showOnDropdowns);
        var visibleObjectTypesForSpace = [];
        for(var tIdx = 0; tIdx < sampleTypes.length; tIdx++) {
            var sampleType = sampleTypes[tIdx];
            if ($.inArray(sampleType.code, visibleObjectTypeCodesForSpace) !== -1) {
                visibleObjectTypesForSpace.push(sampleType);
            }
        }
        return visibleObjectTypesForSpace;
    }

	this.getSampleTypeDropdown = function(id, isRequired, showEvenIfHidden, showOnly, spaceCode, withEmptyOption) {
	    var visibleObjectTypeCodesForSpace = null;
	    if (spaceCode) {
	        visibleObjectTypeCodesForSpace = SettingsManagerUtils.getVisibleObjectTypesForSpace(spaceCode, SettingsManagerUtils.ShowInSpaceSetting.showOnDropdowns);
	    }
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text("Select an " + ELNDictionary.sample + " type"));
		if (withEmptyOption) {
		    $component.append($("<option>").attr('value', '').text('(empty)'));
		}

		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
			
			if(showOnly && ($.inArray(sampleType.code, showOnly) !== -1)) {
				//Show 
			} else if(showOnly) {
				continue;
			}
			
			if(showEvenIfHidden && ($.inArray(sampleType.code, showEvenIfHidden) !== -1)) {
				// Show even if hidden
			} else if (visibleObjectTypeCodesForSpace && !($.inArray(sampleType.code, visibleObjectTypeCodesForSpace) !== -1)) {
				continue;
			}
			
			var label = Util.getDisplayLabelFromCodeAndDescription(sampleType);
			
			$component.append($("<option>").attr('value',sampleType.code).text(label));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getExperimentTypeDropdown = function(id, isRequired, defaultValue) {
		var experimentTypes = this.profile.allExperimentTypes;
		
		var $component = $("<select>", {"id" : id, class : "form-control"});
		if (isRequired) {
			$component.attr("required", "");
		}

		var $emptyOption = $("<option>").attr("value", "").attr('disabled', '').text("Select an " + ELNDictionary.getExperimentDualName() + " type");
		if (!defaultValue || defaultValue === "") {
			$emptyOption.attr("selected", "");
		}

		$component.append($emptyOption);
		for(var i = 0; i < experimentTypes.length; i++) {
			var experimentType = experimentTypes[i];
			if(profile.isExperimentTypeHidden(experimentType.code)) {
				continue;
			}
			
			var label = Util.getDisplayNameFromCode(experimentType.code);
			var description = Util.getEmptyIfNull(experimentType.description);
			if(description !== "") {
				label += " (" + description + ")";
			}

			var $option = $("<option>").attr("value", experimentType.code).text(label);
			if (experimentType.code === defaultValue) {
				$option.attr("selected", "");
			}
			$component.append($option);
		}
		Select2Manager.add($component);
		return $component;
	};

	this.getInlineExperimentTypeDropdown = function(id, isRequired, defaultValue) {
		var $wrapper = $("<span>", { class : "dropdown" });
		$wrapper.append(this.getExperimentTypeDropdown(id, isRequired, defaultValue));
		return $wrapper;
	};
	
	this.getSpaceDropdown = function(id, isRequired) {
		var spaces = this.profile.allSpaces;
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < spaces.length; i++) {
			$component.append($("<option>").attr('value', spaces[i]).text(Util.getDisplayNameFromCode(spaces[i])));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getDropdown = function(mapVals, placeHolder) {
		$dropdown = this.getPlainDropdown(mapVals, placeHolder);
		Select2Manager.add($dropdown);
		return $dropdown;
	}

	this.setValuesToComponent = function ($component, mapVals) {
		for (var mIdx = 0; mIdx < mapVals.length; mIdx++) {
			var $option = $("<option>").attr('value', mapVals[mIdx].value).text(mapVals[mIdx].label);
			if (mapVals[mIdx].disabled) {
				$option.attr('disabled', '');
			}
			if (mapVals[mIdx].selected) {
				$option.attr('selected', '');
			}
            if (mapVals[mIdx].tooltip) {
                $option.attr("title", mapVals[mIdx].tooltip);
            }
			$component.append($option);
		}
	};

	this.getPlainDropdown = function(mapVals, placeHolder) {
		var $component = $("<select>", {class : 'form-control'});
		if (placeHolder) {
			$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(placeHolder));
		}
		this.setValuesToComponent($component, mapVals);
		return $component;
	};
	
	this.getDataSetsDropDown = function(code, dataSetTypes) {
		var $component = $("<select>", { class : 'form-control ' });
		$component.attr('id', code);
		
		$component.attr('required', '');
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text('Select a dataset type'));
		
		for (var i = 0; i < dataSetTypes.length; i++) {
			var datasetType = dataSetTypes[i];
			var label = Util.getDisplayNameFromCode(datasetType.code);
			var description = Util.getEmptyIfNull(datasetType.description);
			if (description !== "") {
				label += " (" + description + ")";
			}
			
			$component.append($("<option>").attr('value',datasetType.code).text(label));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getOptionsRadioButtons = function(name, isFirstSelected, values, changeAction) {
		var $component = $("<div>");
		for(var vIdx = 0; vIdx < values.length; vIdx++) {
			if(vIdx !== 0) {
				$component.append(" ");
			}
			var $radio = $("<input>", { type: "radio", name: name, value: values[vIdx]});
			
			if(isFirstSelected && (vIdx === 0)) {
				$radio.attr("checked", "");
			}
			$radio.change(changeAction);
			$component.append($radio);
			$component.append(" " + values[vIdx]);
		}
		return $component;
	}
	
	this.getProjectAndExperimentsDropdown = function(withProjects, withExperiments, isRequired, callbackForComponent) {
		mainController.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
			var spaces = dataWithSpacesAndProjects.result;
            var projectsToUse = [];
            for (var i = 0; i < spaces.length; i++) {
            	var space = spaces[i];
            	for (var j = 0; j < space.projects.length; j++) {
                    var project = space.projects[j];
                    delete project["@id"];
                    delete project["@type"];
                    projectsToUse.push(project);
                }
            }
            
            mainController.serverFacade.listExperiments(projectsToUse, function(experiments) {
            	if(experiments.result) {
            		
            		for (var k = 0; k < experiments.result.length; k++) {
                		var experiment = experiments.result[k];
                		
                		for(var pIdx = 0; pIdx < projectsToUse.length; pIdx++) {
                    		var project = projectsToUse[pIdx];
                    		var projectIdentifier = IdentifierUtil.getProjectIdentifier(project.spaceCode, project.code);
                    		if(experiment.identifier.startsWith(projectIdentifier)) {
                    			if(!project.experiments) {
                    				project.experiments = [];
                    			}
                    			project.experiments.push(experiment);
                    		}
                    	}
                	}
                	//
            		//
            		var $component = $("<select>", { class : 'form-control'});
            		if(isRequired) {
            			$component.attr('required', '');
            		}
            		var placeHolder = "";
            		if(withProjects && withExperiments) {
            			placeHolder = "Select a project or " + ELNDictionary.getExperimentDualName();
            		} else if(withProjects) {
            			placeHolder = "Select a project";
            		} else if(withExperiments) {
            			placeHolder = "Select an " + ELNDictionary.getExperimentDualName();
            		}
            		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(placeHolder));
            		for(var pIdx = 0; pIdx < projectsToUse.length; pIdx++) {
            			var project = projectsToUse[pIdx];
            			var projectIdentifier = IdentifierUtil.getProjectIdentifier(project.spaceCode, project.code);
            			if(withProjects) {
            				$component.append($("<option>").attr('value', projectIdentifier).text(projectIdentifier));
            			}
            			if(project.experiments) {
            				for(var eIdx = 0; eIdx < project.experiments.length; eIdx++) {
                    			var experiment = project.experiments[eIdx];
                    			if(withExperiments) {
                    				var name = null;
                    				if(profile.propertyReplacingCode) {
                    					name = experiment.properties[profile.propertyReplacingCode];
                    				}
                    				if(name) {
                    					name = " (" + name + ")";
                    				} else {
                    					name = "";
                    				}
                    				$component.append($("<option>").attr('value',experiment.identifier).text(experiment.identifier + name));
                    			}
                			}
            			}
            		}
            		Select2Manager.add($component);
            		callbackForComponent($component);
            	}
            });
		});
	}
	
	this.getDeleteButton = function(deleteFunction, includeReason, warningText) {
		var $deleteBtn = $("<a>", { 'class' : 'btn btn-default ' });
		$deleteBtn.append($("<span>", { 'class' : 'glyphicon glyphicon-trash', 'style' : 'width:16px; height:16px;'}));
		$deleteBtn.click(function() {
			var modalView = new DeleteEntityController(deleteFunction, includeReason, warningText);
			modalView.init();
		});
		return $deleteBtn;
	}
	
	this.getButtonWithImage = function(src, clickEvent, text, tooltip) {
		var $btn = $("<a>", { 'class' : 'btn btn-default' });
		$btn.append($("<img>", { 'src' : src, 'style' : 'width:16px; height:16px;'}));
		$btn.click(clickEvent);
		if(text) {
			$btn.append("&nbsp;").append(text);
		}
		if(tooltip) {
			$btn.attr("title", tooltip);
			$btn.tooltipster();
		}
		return $btn;
	}
	
	this.getButtonWithText = function(text, clickEvent, btnClass, id) {
		var auxBtnClass = "btn-default";
		if(btnClass) {
			auxBtnClass = btnClass;
		}
		var $pinBtn = $("<a>", { 'class' : 'btn ' + auxBtnClass });
		if(id) {
            $pinBtn.attr("id", id);
        }
		$pinBtn.append(text);
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getFormAwesomeIcon = function(iconClass) {
		return $("<i>", { 'class' : 'fa ' + iconClass });
	}
	
    this.getButtonWithIcon = function(iconClass, clickEvent, text, tooltip, id) {
        var $btn = null;
        if(iconClass) {
            $btn = $("<a>", { 'class' : 'btn btn-default' }).append($("<span>", { 'class' : 'glyphicon ' + iconClass }));
        } else {
            $btn = $("<a>", { 'class' : 'btn btn-default' });
        }
        if(text && iconClass) {
            $btn.append("&nbsp;");
        }
        if(text) {
            $btn.append(text);
        }
        if(tooltip) {
            $btn.attr("title", tooltip);
            $btn.tooltipster();
        }
        if(id) {
            $btn.attr("id", id);
        }
        $btn.click(clickEvent);
        return $btn;
    }

	this.getButtonGroup = function(buttons, size) {
		var styleClass = "btn-group" + (size ? "-" + size : "");
		var $buttonGroup = $("<div>", {
			"class": styleClass,
			"role": "group",
		});
		for (var i=0; i<buttons.length; i++) {
			$buttonGroup.append(buttons[i]);
		}
		$buttonGroup.css({ "margin": "3px" });
		return $buttonGroup;
	}

	/**
	 * @param {string} settingLoadedCallback Can be used to avoid flickering. Only called if dontRestoreState is not true.
	 * @param {string} dontRestoreState Sets the state to collaped and doesn't load it from server.
	 */
	this.getShowHideButton = function($elementToHide, key, dontRestoreState, settingLoadedCallback) {

		var glyphicon = dontRestoreState ? "glyphicon-chevron-right" : 'glyphicon-chevron-down';

		var $showHideButton = FormUtil.getButtonWithIcon(glyphicon, function() {
			$elementToHide.slideToggle();

			var $thisButton = $($(this).children()[0]);
			
			if($thisButton.hasClass("glyphicon-chevron-right")) {
				$thisButton.removeClass("glyphicon-chevron-right");
				$thisButton.addClass("glyphicon-chevron-down");
				mainController.serverFacade.setSetting(key,"true");
			} else {
				$thisButton.removeClass("glyphicon-chevron-down");
				$thisButton.addClass("glyphicon-chevron-right");
				mainController.serverFacade.setSetting(key,"false");
			}
			
		}, null, "Show/Hide section", key);
		
		if (dontRestoreState) {
			$elementToHide.hide();
		} else {
			mainController.serverFacade.getSetting(key, function(value) {
				if(value === "false") {
					var $thisButton = $($showHideButton.children()[0]);
					$thisButton.removeClass("glyphicon-chevron-down");
					$thisButton.addClass("glyphicon-chevron-right");
					$elementToHide.toggle();
				}
				if (settingLoadedCallback) {
					settingLoadedCallback();
				}
			});
		}
		
		$showHideButton.addClass("btn-showhide");
		$showHideButton.css({ "border" : "none", "margin-bottom" : "4px", "margin-left" : "-11px" });

		return $showHideButton;
	}
	
	this.getHierarchyButton = function(permId) {
		var $hierarchyButton = $("<a>", { 'class' : 'btn btn-default'} )
									.append($('<img>', { 'src' : './img/hierarchy-icon.png', 'style' : 'width:16px; height:17px;' }))
									.append(' G');
		$hierarchyButton.click(function() {
			mainController.changeView('showSampleHierarchyPage', permId);
		});
		return $hierarchyButton;
	}
	
	//
	// Get Field with container to obtain a correct layout
	//
	this.getFieldForComponentWithLabel = function($component, label, postComponent, isInline, $info) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		var requiredText = '';
		if($component.attr('required')) {
			requiredText = " (*)"
		}
		
		var labelText = "";
		if(label) {
            labelText = label + requiredText;
        }
        var $controlLabel = this.createLabel(labelText, $info);
		
		var controlColumnClass = ""
		if(!isInline) {
			controlColumnClass = this.controlColumnClass;
		}
		var $controls = $('<div>', { class : 'controls' });
		
		if(label) {
			$controlGroup.append($controlLabel);
		}
		
		if(isInline) {
			$controlGroup.append($component);
		} else {
			$controls.append($component);
			$controlGroup.append($controls);
		}
		
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		if(isInline) {
			return $controlGroup;
		} else {
			return $fieldset;
		}
	}

    this.createLabel = function(label, $info) {
        var $controlLabel = $('<label>', {class : 'control-label' });
        
        if(label) {
            if ($info) {
                var $line = $("<div>");
                $line.append(label);
                $infoIcon = $("<span>", { 'class' : 'glyphicon glyphicon-info-sign', 'style' : 'padding:2px' });
                $infoIcon.tooltipster({
                    content: $info,
                    theme: 'tooltipster-shadow',
                    interactive: true
                });
                $line.append($infoIcon);
                $line.append(":");
                $controlLabel.append($line);
            } else
            {
                $controlLabel.text(label + ":");
            }
        }
        return $controlLabel;
    }

    this.createPropertyField = function(propertyType, propertyValue, $info) {
	    var isLink = propertyType.dataType === "HYPERLINK";
	    var hyperlinkLabel = null;
		if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			propertyValue = this.getVocabularyLabelForTermCode(propertyType, propertyValue);
			if(propertyType.vocabulary.urlTemplate) {
			    hyperlinkLabel = propertyValue;
			    propertyValue = propertyType.vocabulary.urlTemplate.replace('${term}', propertyValue);
			    isLink = true;
			}
		} else if (propertyType.dataType === "INTEGER" || propertyType.dataType === "REAL") {
		    var numberFormat = new Intl.NumberFormat('en-US', { notation : "standard",
		                                                        minimumSignificantDigits :  "1",
		                                                        maximumSignificantDigits : "21",
		                                                        minimumFractionDigits : "0",
		                                                        maximumFractionDigits : "20" });
		    propertyValue = numberFormat.format(propertyValue);
		}
        return this._createField(isLink, propertyType.label, propertyValue, propertyType.code, null, null, hyperlinkLabel, $info);
	}
	
	this.getFieldForLabelWithText = function(label, text, id, postComponent, cssForText) {
		return this._createField(false, label, text, id, postComponent, cssForText);
	}
	
    this._createField = function(hyperlink, label, text, id, postComponent, cssForText, hyperlinkLabel, $info) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		
        var $controlLabel = this.createLabel(label, $info);
		
		var $controls = $('<div>', {class : 'controls' });
		
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent; word-wrap: break-word;'}); //white-space: pre-wrap;
		if(cssForText) {
			$component.css(cssForText);
		}
		
		if(text) {
		    if(typeof(text) != 'string') { // Array case
                text = text.join(", ");
            }
			text = text.replace(/(?:\r\n|\r|\n)/g, '\n'); //Normalise carriage returns
		}

		if(hyperlink) {
		    $component.html(this.asHyperlink(text, hyperlinkLabel));
		} else {
		    if(text && text.includes('\n')) {
		        var lines = text.split('\n');
		        for(var lineIndex = 0; lineIndex < lines.length; lineIndex++) {
		            if(lineIndex != 0) {
		                $component.append($("<br>"));
		            }
		            var textNode = document.createTextNode(lines[lineIndex]);
                    $component.append(textNode);
		        }
		    } else {
		        $component.text(text);
		    }
		}
		
		if(id) {
			$component.attr('id', this.prepareId(id));
		}
		$controls.append($component);
		
		return $fieldset;
	}
	
	this.asHyperlink = function(text, label) {
		return $("<a>", { "href" : text, "target" : "_blank"}).append((label ? label : text));
	}

	//
	// Get Field from property
	//
	this.getVocabularyLabelForTermCode = function(propertyType, data) {
		var vocabulary = propertyType.vocabulary;
		if(vocabulary) {
		    if(Array.isArray(data)) {
		        var codes = [];
		        for(termCode of data) {
                    for(var tIdx = 0; tIdx < vocabulary.terms.length; tIdx++) {
                        if(vocabulary.terms[tIdx].code === termCode &&
                            vocabulary.terms[tIdx].label) {
                            codes.push(vocabulary.terms[tIdx].label);
                        }
                    }
		        }
		        return codes.sort().toString();
		    } else {
		        var termCode = data;
                for(var tIdx = 0; tIdx < vocabulary.terms.length; tIdx++) {
                    if(vocabulary.terms[tIdx].code === termCode &&
                        vocabulary.terms[tIdx].label) {
                        return vocabulary.terms[tIdx].label;
                    }
                }
			}
		}
		return termCode;
	}

	this.getFieldForPropertyType = function(propertyType, timestampValue) {
	    return this.getFieldForPropertyType(propertyType, timestampValue, false);
	}
	
	this.getFieldForPropertyType = function(propertyType, timestampValue, isMultiValue) {
		var $component = null;
		if (propertyType.dataType === "BOOLEAN") {
			$component = this._getBoolean2Field(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			var vocabulary = profile.getVocabularyByCode(propertyType.vocabulary.code);
			$component = this._getDropDownFieldForVocabulary(propertyType.code, vocabulary.terms, propertyType.description, propertyType.mandatory, isMultiValue);
		} else if (propertyType.dataType === "HYPERLINK") {
			$component = this._getInputField("url", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "INTEGER") {
			$component = this._getNumberInputField(propertyType.code, propertyType.description, '1', propertyType.mandatory);
		} else if (propertyType.dataType === "MATERIAL") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (["MULTILINE_VARCHAR", "JSON"].includes(propertyType.dataType)) {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
			if(profile.isForcedMonospaceFont(propertyType)) {
				$component.css("font-family", "Consolas, Monaco, Lucida Console, Liberation Mono, DejaVu Sans Mono, Bitstream Vera Sans Mono, Courier New, monospace");
			}
		} else if (propertyType.dataType === "REAL") {
			$component = this._getNumberInputField(propertyType.code, propertyType.description, 'any', propertyType.mandatory);
		} else if (propertyType.dataType === "TIMESTAMP") {
			$component = this._getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory, false, timestampValue);
		} else if (propertyType.dataType === "DATE") {
			$component = this._getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory, true, timestampValue);
		} else if (propertyType.dataType === "VARCHAR") {
            $component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
        } else if (['ARRAY_STRING', 'ARRAY_INTEGER', 'ARRAY_REAL', 'ARRAY_TIMESTAMP'].includes(propertyType.dataType)) {
            $component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
        } else if (propertyType.dataType === "XML") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "SAMPLE") {
		    var sampleTypeCode = propertyType.sampleTypeCode;
		    var sampleTypePlaceholder = null;
		    if(!sampleTypeCode) {
		        sampleTypePlaceholder = " of any type"
		    } else {
		        sampleTypePlaceholder = " of type " + Util.getDisplayNameFromCode(sampleTypeCode);
		    }
		    $component = new SampleField(propertyType.mandatory, "Select " + ELNDictionary.Sample + sampleTypePlaceholder, sampleTypeCode, undefined, undefined, isMultiValue);
		}
		
		return $component;
	}
	
	//
	// Read/Write Fields
	//
	this.setFieldValue = function(propertyType, $field, value) {
		if(propertyType.dataType === "BOOLEAN") {
		    if(value === 'true') {
		        value = 'true';
		    } else if (value === 'false') {
		        value = 'false';
		    } else {
		        value = '';
		    }
			$field.val(value);
		} else if(propertyType.dataType === "TIMESTAMP" || propertyType.dataType === "DATE") {
			$($($field.children()[0]).children()[0]).val(value);
		} else {
			$field.val(value);
		}
	}

	this.getBooleanValue = function($field) {
	    var value = null;
	    if($field.val() === 'true') {
            value = true;
        } else if ($field.val() === 'false') {
            value = false;
        } else {
            value = null;
        }
        return value;
	}

	this.getFieldValue = function(propertyType, $field) {
		var propertyTypeValue;
		if (propertyType.dataType === "BOOLEAN") {
		    propertyTypeValue = this.getBooleanValue($field);
		} else {
			propertyTypeValue = $field.val();
		}
		return propertyTypeValue;
	}
	
	//
	// Form Fields
	//
	this._getBoolean2Field = function(id, alt, isRequired) {
		var $dropdown = this.getDropDownForTerms(id, [
		    { code : "true", label : "true" },
		    { code : "false", label : "false" }
		], alt, isRequired);
		return $dropdown;
	}

	this._getBooleanField = function(id, alt, checked, isRequired) {
		var attr = {'type' : 'checkbox', 'alt' : alt, 'placeholder' : alt };
		if(checked) {
			attr['checked'] = '';
		}
		if (id) {
            attr['id'] = this.prepareId(id);
        }

		var $container = $('<div>', {'class' : 'checkbox'}).append($('<label>').append($('<input>', attr)));

        $container.append($("<span>", { class: "glyphicon glyphicon-info-sign" })).append(alt ? " " + alt : "");

		if (isRequired) {
            $container.attr('required', '');
        }

        return $container;
	}
	
	this.getDropDownForTerms = function(id, terms, alt, isRequired) {
		return this._getDropDownFieldForVocabulary(id, terms, alt, isRequired);
	}

	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
	    return this._getDropDownFieldForVocabulary(code, terms, alt, isRequired, false);
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired, isMultiValue) {
	    var $component = $("<select>", {'placeholder' : alt, 'class' : 'form-control'});
	    if(isMultiValue) {
	        $component.attr('multiple', 'multiple');
	    }
		$component.attr('id', this.prepareId(code));
		
		if (isRequired) {
			$component.attr('required', '');
		}

		var labelOption = $("<option>").attr('value', '').attr('disabled', '').text(alt);
		if(!isMultiValue) {
		    labelOption = labelOption.attr('selected', '');
		}
		$component.append(labelOption);
		$component.append($("<option>").attr('value', '').text('(empty)'));
        var $options = [];
		for(var i = 0; i < terms.length; i++) {
		    var $option = $("<option>", { value : terms[i].code }).text(terms[i].label);
			$options.push($option);
		}
		$component.append($options);
		Select2Manager.add($component);
		return $component;
	}
	
	this.getTextInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, null, isRequired);
	}
	
	this.getRealInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, 0.01, isRequired);
	}
	
	this.getIntegerInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, 1, isRequired);
	}
	
	this._getNumberInputField = function(id, alt, step, isRequired)
	{
		var $component = this._getInputField("number", id, alt, step, isRequired);
		var validator = function(event) {
			var target = event.target;
			if (!target.checkValidity()) {
				Util.showError("Not a valid number in field '" + target.alt + "'.",
						function() {
							Util.unblockUI();
							target.focus();
						});
			}
		};
		$component.blur(validator);
		return $component;
	}
	
	this._getInputField = function(type, id, alt, step, isRequired) {
		var $component = $('<input>', {'type' : type, 'id' : this.prepareId(id), 'alt' : alt, 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (step) {
			$component.attr('step', step);
		}
		return $component;
	}
	
    this._getTextBox = function(id, alt, isRequired) {
		var $component = $('<textarea>', {'id' : id, 'alt' : alt, 'style' : 'height: 80px; width: 450px;', 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		return $component;
	}

	this._getDiv = function(id, alt, isRequired) {
        var $component = $('<div>', {'id' : id, 'alt' : alt, 'placeholder' : alt});
        if (isRequired) {
            $component.attr('required', '');
        }
        return $component;
    }

	this._getDatePickerField = function(id, alt, isRequired, isDateOnly, value) {
		var $component = $('<div>', {'class' : 'form-group', 'style' : 'margin-left: 0px;', 'placeholder' : alt });
		var $subComponent = $('<div>', {'class' : 'input-group date', 'id' : 'datetimepicker_' + id });
		var $input = $('<input>', {'class' : 'form-control', 'type' : 'text', 'id' : id, 'placeholder' : (isDateOnly ? 'yyyy-MM-dd (YEAR-MONTH-DAY)' : 'yyyy-MM-dd HH:mm:ss (YEAR-MONTH-DAY : HOUR-MINUTE-SECOND)'),
			'data-format' : isDateOnly ? 'yyyy-MM-dd' : 'yyyy-MM-dd HH:mm:ss'});
		if (isRequired) {
			$input.attr('required', '');
			$component.attr('required', '');
		}
		var $spanAddOn = $('<span>', {'class' : 'input-group-addon'})
							.append($('<span>', {'class' : 'glyphicon glyphicon-calendar' }));

		$subComponent.append($input);
		$subComponent.append($spanAddOn);

		var date = null;
		if(value) {
			date = Util.parseDate(value);
		}

		var datetimepicker = $subComponent.datetimepicker({
			format : isDateOnly ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:mm:ss',
			useCurrent : false,
			defaultDate : date
		});

		$component.append($subComponent);

		return $component;
	}

    this.ckEditor4to5ImageStyleMigration = function(value) {
        var buffer = "";
        var offset = 0;

        while(offset < value.length) {
            currentImageStartOffset = -1;
            currentImageEndOffset = -1;
            styleTag = null;

            currentImageStartOffset = value.indexOf("<img", offset);
            if(currentImageStartOffset !== -1) {
                currentImageEndOffset = value.indexOf(">", currentImageStartOffset);
            }

            if(currentImageStartOffset !== -1 && currentImageEndOffset !== -1) {
                currentStyleStartOffset = null;
                currentStyleEndOffset = null;

                currentStyleStartOffset = value.indexOf("style=\"", currentImageStartOffset);
                if(currentStyleStartOffset !== -1) {
                    currentStyleEndOffset = value.indexOf("\"", currentStyleStartOffset + "style=\"".length);
                }

                if(currentStyleStartOffset !== -1 && currentStyleEndOffset !== -1) {
                    styleTag = value.substring(currentStyleStartOffset, currentStyleEndOffset + "\"".length);
                }
            }

            // Move offset
            if(currentImageEndOffset !== -1) {
                buffer = buffer + value.substring(offset, currentImageStartOffset);
                if(styleTag !== null) {
                    buffer = buffer + "<figure class=\"image image_resized\"" + styleTag + ">";
                }
                buffer = buffer + value.substring(currentImageStartOffset, currentImageEndOffset + ">".length);
                if(styleTag !== null) {
                    buffer = buffer + "</figure>";
                }
                offset = currentImageEndOffset + ">".length;
            } else {
                buffer = buffer + value.substring(offset, value.length);
                offset = value.length;
            }
        }

        return buffer;
    }

    this.createCkeditor = function($component, componentOnChange, value, placeholder, isReadOnly, toolbarContainer) {
        // CKEditor 4 to 5 Image style Migration
        if( value &&
            value.indexOf("<img")  !== -1 &&
            value.indexOf("<figure") === -1) {
            value = this.ckEditor4to5ImageStyleMigration(value);
        }
	    var Builder = null;
// CK Editor 34
//	    if(toolbarContainer) {
//            Builder = InlineEditor.DecoupledEditor;
//	    } else {
//	        Builder = InlineEditor.InlineEditor;
//	    }

        // CK Editor 18
	    if(toolbarContainer) {
            Builder = CKEDITOR.DecoupledEditor;
	    } else {
	        Builder = CKEDITOR.InlineEditor;
	    }
        Builder.create($component[0], {
                         placeholder: placeholder,
                         simpleUpload: {
                             uploadUrl: "/openbis/openbis/file-service/eln-lims?type=Files&sessionID=" + mainController.serverFacade.getSession()
                         }
                    })
                    .then( editor => {
                        editor.acceptedData = ""; // Is used to undo paste events containing images coming from a different domain
                        if (value) {
                            value = this.prepareCkeditorData(value);
                            editor.setData(value);
                            editor.acceptedData = editor.getData();
                        }

                        editor.isReadOnly = isReadOnly;
                        editor.model.document.on('change:data', function (event, data) {
                            var newData = editor.getData();
                            if(newData !== editor.acceptedData) {
                                var isDataValid = CKEditorManager.isDataValid(newData);
                                if(isDataValid) {
                                    editor.acceptedData = newData;
                                    componentOnChange(event, newData); // Store changes on original model
                                } else {
                                    editor.setData(editor.acceptedData);
                                    Util.showUserError("It is not possible to copy an image directly from a website.");
                                }
                            }
                        });

                        if(toolbarContainer) {
                            toolbarContainer.append(editor.ui.view.toolbar.element);
                        }

                        CKEditorManager.addEditor($component.attr('id'), editor);

//                        $component.on('paste', function(evt) {
//                            evt.stop(); // we don't let editor to paste data;
//                            alert('paste');
//                        });
                    })
                    .catch(error => {
                        Util.showError(error);
                    });
	}

	this.prepareCkeditorData = function(value) {
	    value = value.replace(/(font-size:\d+\.*\d+)pt/g, "$1" + "px"); // https://ckeditor.com/docs/ckeditor5/latest/features/font.html#using-numerical-values
	    return value;
	}

	this.activateRichTextProperties = function($component, componentOnChange, propertyType, value, isReadOnly, toolbarContainer) {
		// InlineEditor is not working with textarea that is why $component was changed on div
	    var placeholder = propertyType ? propertyType.description : "";
		var $component = this._getDiv($component.attr('id'), $component.attr('alt'), $component.attr('isRequired'));
		FormUtil.createCkeditor($component, componentOnChange, value, placeholder, isReadOnly, toolbarContainer);

		if (propertyType && propertyType.mandatory) {
			$component.attr('required', '');
		}
		return $component;
	}
	
	this.fixStringPropertiesForForm = function(propertyType, entity) {
	    if(entity) {
            var originalValue = entity.properties[propertyType.code];
            if(propertyType.metaData["custom_widget"] && propertyType.metaData["custom_widget"] === "Word Processor" && originalValue) { // Only filter properties rendered as HTML
                entity.properties[propertyType.code] = this.sanitizeRichHTMLText(originalValue);
            }
		}
	}
	
	this.sanitizeRichHTMLText = function(originalValue) {
		if (typeof originalValue === "string") {
			//Take envelope out if pressent
			var bodyStart = originalValue.indexOf("<body>");
			var bodyEnd = originalValue.indexOf("</body>");
			if(bodyStart !== -1 && bodyEnd !== -1) {
				originalValue = originalValue.substring(bodyStart + 6, bodyEnd);
			}
			//Clean the contents
			//originalValue = html.sanitize(originalValue);
			originalValue = DOMPurify.sanitize(originalValue);
		}
		return originalValue;
	}

	this.addCreationDropdown = function(toolbarModel, types, priorityTypeCodes, actionFactory) {
		var priorityTypes = [];
		var otherTypes = [];
		for (var idx = 0; idx < types.length; idx++) {
			var type = types[idx];
			if ($.inArray(type.code, priorityTypeCodes) !== -1) {
				priorityTypes.push(type);
			} else {
				otherTypes.push(type);
			}
		}
		
		var dropdownModel = [];
		this._populateDropdownModel(dropdownModel, priorityTypes, actionFactory);
		if (priorityTypes.length > 0 && otherTypes.length > 0) {
			dropdownModel.push({ separator : true });
		}
		this._populateDropdownModel(dropdownModel, otherTypes, actionFactory);
		
		var newWithIcon = $('<span>')
			.append($('<span>', {'class' : 'glyphicon glyphicon-plus' }))
			.append('&nbsp;New&nbsp;');
		FormUtil.addOptionsToToolbar(toolbarModel, dropdownModel, [], null, newWithIcon);
	}
	
	this._populateDropdownModel = function(dropdownModel, types, actionFactory) {
		types.forEach(function (type) {
			dropdownModel.push({
				title : type.description,
				label : Util.getDisplayNameFromCode(type.code),
				action : actionFactory(type.code)
			});
		});
	}

	this.addOptionsToToolbar = function(toolbarModel, dropdownOptionsModel, hideShowOptionsModel, namespace, title) {
	    var _this = this;
		if(!title) {
			title = "More ... ";
		}
		var id = 'options-menu-btn';
		if (namespace) {
		    id = id + "-" + namespace;
		    id = id.toLowerCase();
		}
		var $dropdownOptionsMenu = $("<span>", { class : 'dropdown' });
		if(toolbarModel) {
		    toolbarModel.push({ component : $dropdownOptionsMenu, tooltip: null });
		}
		var $dropdownOptionsMenuCaret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default', 'id' : id})
				.append(title).append($("<b>", { class : 'caret' }));
		var $dropdownOptionsMenuList = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu' });
		$dropdownOptionsMenu.append($dropdownOptionsMenuCaret);
		$dropdownOptionsMenu.append($dropdownOptionsMenuList);
		for (var idx = 0; idx < dropdownOptionsModel.length; idx++) {
			var option = dropdownOptionsModel[idx];
			if(option.separator) {
				$dropdownOptionsMenuList.append($("<li>", { 'role' : 'presentation' }).append($("<hr>", { style : "margin-top: 5px; margin-bottom: 5px;"})));
			} else {
				var label = option.label;
				var title = option.title ? option.title : label;
				var id = _this.prepareId(title).toLowerCase();
				var $dropdownElement = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : title, 'id' : id}).append(label));
				$dropdownElement.click(option.action);
				$dropdownOptionsMenuList.append($dropdownElement);
			}
		}

		if(hideShowOptionsModel.length > 0 && dropdownOptionsModel.length > 0) {
			$dropdownOptionsMenuList.append($("<li>", { 'role' : 'presentation' }).append($("<hr>", { style : "margin-top: 5px; margin-bottom: 5px;"})));
		}

		var settingsKey = namespace + "-showing-sections";
		mainController.serverFacade.getSetting(settingsKey, function(settingsValue) {
			var sectionsSettings = settingsValue ? JSON.parse(settingsValue) : {};
			for (var idx = 0; idx < hideShowOptionsModel.length; idx++) {
				var option = hideShowOptionsModel[idx];
				var shown = option.forceToShow === true;
				if (shown === false) {
					var sectionSetting = sectionsSettings[option.label];
					if (sectionSetting !== undefined) {
						shown = sectionSetting === 'shown';
					} else if (option.showByDefault) {
						shown = true;
					} else {
						shown = ! profile.hideSectionsByDefault;
					}
				}

				var $section = $(option.section);
				$section.toggle(shown);
				var $label = $("<span>").append((shown ? "Hide " : "Show ") + option.label);
				var id = 'options-menu-btn-' + _this.prepareId(option.label).toLowerCase();
				var $dropdownElement = $("<li>", { 'role' : 'presentation' }).append($("<a>", { 'id' : id }).append($label));
				var action = function(event) {
					var option = event.data.option;
					var $label = event.data.label;
					var $section = event.data.section;
					$section.toggle(300, function() {
						if ($section.css("display") === "none") {
							$label.text("Show " + option.label);
							sectionsSettings[option.label] = "hidden";
						} else {
							if (option.beforeShowingAction) {
								option.beforeShowingAction();
							}
							$label.text("Hide " + option.label);
							sectionsSettings[option.label] = "shown";
						}
						$(window).trigger('resize'); // HACK: Fixes table rendering issues when refreshing the grid on fuelux 3.1.0 for all browsers
						mainController.serverFacade.setSetting(settingsKey, JSON.stringify(sectionsSettings));
					});
				};
				$dropdownElement.click({option : option, label : $label, section : $section}, action);
				$dropdownOptionsMenuList.append($dropdownElement);
			}
		});
	}
	
	this.getToolbar = function(toolbarModel) {
		var $toolbarContainer = $("<span>", { class : 'toolBox' });
		
		for(var tbIdx = 0; tbIdx < toolbarModel.length; tbIdx++) {
			var $toolbarComponent = toolbarModel[tbIdx].component;
			var toolbarComponentTooltip = toolbarModel[tbIdx].tooltip;
			var $toolbarComponentTooltip = toolbarModel[tbIdx].$tooltip;
			if(toolbarComponentTooltip) {
				$toolbarComponent.attr("title", toolbarComponentTooltip);
				$toolbarComponent.tooltipster();
			} else if ($toolbarComponentTooltip) {
				$toolbarComponent.tooltipster({
					content: $toolbarComponentTooltip
				});
			}
			$toolbarContainer.append($toolbarComponent);
			$toolbarContainer.append("&nbsp;");
		}
		
		return $toolbarContainer;
	}
	
	this.getOperationsMenu = function(items) {
		var $dropDownMenu = $("<span>", { class : 'dropdown' });
		var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("More ... ").append($("<b>", { class : 'caret' }));
		var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
		$dropDownMenu.append($caret);
		$dropDownMenu.append($list);
		
		for(var iIdx = 0; iIdx < items.length; iIdx++) {
			var item = items[iIdx];
			var $item = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : item.label}).append(item.label));
			$item.click(item.event);
			$list.append($item);
		}
		return $dropDownMenu;
	}
	
	this.getFormLink = function(displayName, entityKind, permIdOrIdentifier, paginationInfo, id) {
		if(permIdOrIdentifier === null || permIdOrIdentifier === undefined || permIdOrIdentifier.trim().length === 0){
            return;
        }
        var view = null;
		switch(entityKind) {
			case "Space":
				view = "showSpacePage";
				break;
			case "Project":
				view = "showProjectPageFromIdentifier";
				break;
			case "Experiment":
				view = "showExperimentPageFromIdentifier";
				break;
			case "Sample":
				if(permIdOrIdentifier.lastIndexOf("/") !== -1) {
					view = "showViewSamplePageFromIdentifier";
				} else {
					view = "showViewSamplePageFromPermId";
				}
				break;
			case "DataSet":
				view = "showViewDataSetPageFromPermId";
				break;
		}
		
		var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), view, permIdOrIdentifier);
		var click = function() {
			var arg = null;
			if(paginationInfo) {
				arg = {
						permIdOrIdentifier : permIdOrIdentifier,
						paginationInfo : paginationInfo
				}
			} else if (view === "showExperimentPageFromIdentifier") {
				arg = encodeURIComponent('["' + permIdOrIdentifier + '",false]');
			} else {
				arg = permIdOrIdentifier;
			}
			mainController.changeView(view, arg, true);
		}
		displayName = String(displayName).replace(/<(?:.|\n)*?>/gm, ''); //Clean any HTML tags
		var link = $("<a>", { "href" : href, "class" : "browser-compatible-javascript-link", "id" : id }).text(displayName);
		link.click(click);
		return link;
	}
	
	this.getFormPath = function(spaceCode, projectCode, experimentCode, containerSampleCode, containerSampleIdentifierOrPermId, sampleCode, sampleIdentifierOrPermId, datasetCodeAndPermId) {
		var entityPath = $("<span>");
		if(spaceCode) {
			entityPath.append("/").append(this.getFormLink(spaceCode, 'Space', spaceCode));
		}
		if(projectCode) {
		    var projectIdentifier = IdentifierUtil.getProjectIdentifier(spaceCode, projectCode);
		    var id = "PATH" + this.prepareId(projectIdentifier);
			entityPath.append("/").append(this.getFormLink(projectCode, 'Project', projectIdentifier, null, id));
		}
		if(experimentCode) {
			entityPath.append("/").append(this.getFormLink(experimentCode, 'Experiment', IdentifierUtil.getExperimentIdentifier(spaceCode, projectCode, experimentCode)));
		}
		if(sampleCode && sampleIdentifierOrPermId) {
			entityPath.append("/");
			if(containerSampleCode && containerSampleIdentifierOrPermId) {
				entityPath.append(this.getFormLink(containerSampleCode, 'Sample', containerSampleIdentifierOrPermId)).append(":");;
			}
			entityPath.append(this.getFormLink(sampleCode, 'Sample', sampleIdentifierOrPermId));
		}
		if(datasetCodeAndPermId) {
			entityPath.append("/").append(this.getFormLink(datasetCodeAndPermId, 'DataSet', datasetCodeAndPermId));
		}
		return entityPath;
	}
	
	this.getBox = function() {
		var $box = $("<div>", { style : "background-color:#f8f8f8; padding:10px; border-color: #e7e7e7; border-style: solid; border-width: 1px;"});
		return $box;
	}
	
	this.getInfoBox = function(title, lines) {
		var $infoBox = this.getBox();
		
		$infoBox.append($("<span>", { class : 'glyphicon glyphicon-info-sign' })).append(" " + title);
		for(var lIdx = 0; lIdx < lines.length; lIdx++) {
			$infoBox.append($("<br>"));
			$infoBox.append(lines[lIdx]);
		}
		return $infoBox;
	}
	
	this.isInteger = function(str) {
    	var n = ~~Number(str);
    	return String(n) === str;
	}
	
	this.isNumber = function(str) {
    	return !isNaN(str);
	}

	//
	// errors
	//

	// errors: array of strings
	this._getSanitizedErrorString = function(title, errors) {
		var $container = $("<div>");
        $container.append($("<h3>").text(title));
		var $ul = $("<ul>");
		for (var error of errors) {
			$ul.append($("<li>").text(error));
		}
		$container.append($ul);
		return $container.html();
	}

	//
	// Dropbox folder name dialog
	//

	/**
	 * @param {string[]} nameElements - First elements of the folder name.
	 * @param {string} nodeType - "Sample" or "Experiment".
	 */
	this.showDropboxFolderNameDialog = function(nameElements) {

		var $dialog = $("<div>");
		$dialog
			.append($("<div>")
				.append($("<legend>").text("Helper tool for Dataset upload using the eln-lims dropbox:")));

		mainController.serverFacade.listDataSetTypes((function(data) {

			var dataSetTypes = profile.filterDataSetTypesForDropdowns(data.result);

			var $formFieldContainer = $("<div>");
			$dialog.append($formFieldContainer);

			// info text
			$formFieldContainer.append(FormUtil.getInfoText("Example and usage instructions: "))
								.append("<center><img src='./img/eln-lims-dropbox-example.png' width='80%' ></center>")
								.append("<center><b>Screenshot example showing the eln-lims dropbox network folder and how the results will be visualized in the ELN after upload</b></center>")
								.append("The eln-lims dropbox requires a root folder with a specific name. This name contains information on where the data should be uploaded.").append("<br>")
								.append("1. Generate the name of the root folder with this helper tool using the form below.").append("<br>")
								.append("2. The upload will be triggered automatically and the data will become visible in the object/experiment to which it was uploaded.").append("<br>");
								

			// dataset type dropdown
			var $dataSetTypeSelector = FormUtil.getDataSetsDropDown('DATASET_TYPE', dataSetTypes);
			$dataSetTypeSelector.attr("id", "dataSetTypeSelector");
			$formFieldContainer
				.append($("<div>", { class : "form-group" })
					.append($("<label>", { class : "control-label" }).text("Dataset type:")))
					.append($dataSetTypeSelector);

			// name
			var $nameInput = $("<input>", { type : "text", id : "nameInput", class : "form-control", disabled : true });
			$formFieldContainer
				.append($("<div>", { class : "form-group" })
					.append($("<label>", { class : "control-label" }).text("Dataset name:")))
					.append($nameInput);

			var ownerHint = "to upload data to the current ";
			if(nameElements[0] === "O") {
				ownerHint += ELNDictionary.Sample;
			} else if(nameElements[0] === "E") {
				ownerHint += ELNDictionary.ExperimentELN;
			}
			ownerHint += " " + nameElements[nameElements.length-1];
			
			// dropbox folder name ouput
			var $dropboxFolderName = $("<input>", {
				class : "form-control",
				id : "dropboxFolderName",
				readonly : "readonly",
				type : "text",
				value :  this._getDropboxFolderName(nameElements),
				onClick : "this.setSelectionRange(0, this.value.length)"
			});
			// copy to clipboard button
			var $copyToClipboardButton = FormUtil.getButtonWithIcon("glyphicon-copy");
			$copyToClipboardButton.attr("id", "copyToClipboardButton");
			$formFieldContainer
				.append($("<div>", { class : "form-group" })
					.append($("<label>", { class : "control-label" })
						.text("Generated root folder name for the dropbox " + ownerHint + ":"))
					.append($("<div>", { class : "input-group" })
						.append($dropboxFolderName)
						.append($("<span>", { class : "input-group-btn"})
							.append($copyToClipboardButton))));

			// close button
			var $cancelButton = $("<a>", {
				class : "btn btn-default",
				id : "dropboxFolderNameClose"
			}).text("Close").css("margin-top", "15px");
			$dialog.append($cancelButton);
			Util.blockUI($dialog.html(), this.getDialogCss());

			// attach events
			$dataSetTypeSelector = $("#dataSetTypeSelector");
			Select2Manager.add($dataSetTypeSelector);
			$nameInput = $("#nameInput");
			$dropboxFolderName = $("#dropboxFolderName");
			$copyToClipboardButton = $("#copyToClipboardButton");
			$("#dropboxFolderNameClose").on("click", function(event) {
				Util.unblockUI();
			});
			// update folder name on type selector / name change
			$dataSetTypeSelector.change((function() {
				var dataSetTypeCode = $dataSetTypeSelector.val();
				var name = null;
				// dataset type code UNKNOWN has no name
				if (dataSetTypeCode === "UNKNOWN") {
					$nameInput.val("");
					$nameInput.attr("disabled", "true");
				} else {
					var name = $nameInput.val();
					$nameInput.removeAttr("disabled");					
				}
				var folderName = this._getDropboxFolderName(nameElements, dataSetTypeCode, name);
				$dropboxFolderName.val(folderName);
			}).bind(this));
			$nameInput.on("input", (function() {
				var dataSetTypeCode = $dataSetTypeSelector.val();
				var name = $nameInput.val();
				var folderName = this._getDropboxFolderName(nameElements, dataSetTypeCode, name);
				$dropboxFolderName.val(folderName);
			}).bind(this));
			// copy to clipboard
			$copyToClipboardButton.on("click", function() {
				$dropboxFolderName.select();
				document.execCommand("copy");
			});
			$copyToClipboardButton.attr("title", "copy to clipboard");
			$copyToClipboardButton.tooltipster();

		}).bind(this));
	}

	this._getDropboxFolderName = function(nameElements, dataSetTypeCode, name) {
		var folderName = "";

		for(var nIdx = 0; nIdx < nameElements.length; nIdx++) {
		    if(nameElements[nIdx]) {
		        if(nIdx !== 0) {
		            folderName += "+";
		        }
		        folderName += nameElements[nIdx];
		    }
		}

		for (var optionalPart of [dataSetTypeCode, name]) {
			if (optionalPart) {
				folderName += "+" + optionalPart;				
			}
		}
		return folderName;
	}

	this.getDialogCss = function() {
		return {
				'text-align' : 'left',
				'top' : '5%',
				'width' : '90%',
				'left' : '5%',
				'right' : '5%',
				'overflow' : 'auto'
		};
	}

	this.getInfoText = function(infoText) {
		return $("<p>")
			.append($("<div>", { class : "glyphicon glyphicon-info-sign" })
				.css("margin-right", "3px"))
			.append($("<span>").append(FormUtil.sanitizeRichHTMLText(infoText)));
	}

	this.getWarningText = function(infoText) {
    		return $("<p>")
    			.append($("<div>", { class : "glyphicon glyphicon-warning-sign" })
    				.css("margin-right", "3px"))
    			.append($("<span>").text(infoText));
    }

    this.downloadMetadataTemplateDialog = function() {
        var _this = this;
        var dataSetTypesForDropdown = [];
        for (var idx = 0; idx < profile.allDatasetTypeCodes.length; idx++) {
            var code = profile.allDatasetTypeCodes[idx];
            dataSetTypesForDropdown.push({ label: Util.getDisplayNameFromCode(code), value: code})
        }

        var $dropdown = FormUtil.getDropdown(dataSetTypesForDropdown, "Select data set type");
        $dropdown.attr("id", "dataSetTypeDropdown");
        Util.showDropdownAndBlockUI("dataSetTypeDropdown", $dropdown);

        $("#dataSetTypeDropdown").on("change", function(event) {
            var dataSetTypeCode = $("#dataSetTypeDropdown")[0].value;
            require([ "as/dto/entitytype/id/EntityTypePermId", "as/dto/dataset/fetchoptions/DataSetTypeFetchOptions" ],
                    function(EntityTypePermId, DataSetTypeFetchOptions) {
                var ids = [new EntityTypePermId(dataSetTypeCode)];
                var fetchOptions = new DataSetTypeFetchOptions();
                fetchOptions.withPropertyAssignments().withPropertyType();
                var template = '{\n"properties":{';
                mainController.openbisV3.getDataSetTypes(ids, fetchOptions).done(function(dataSetTypes) {
                    var propertyAssignments = dataSetTypes[dataSetTypeCode].getPropertyAssignments();
                    for (var idx = 0; idx < propertyAssignments.length; idx++) {
                        var propertyType = propertyAssignments[idx].getPropertyType();
                        if (idx > 0) {
                            template += ',';
                        }
                        var dataType = propertyType.getDataType();
                        var defaultValue = '""';
                        if (dataType === 'BOOLEAN') {
                            defaultValue = false;
                        } else if (dataType === 'INTEGER') {
                            defaultValue = 0;
                        } else if (dataType === 'REAL') {
                            defaultValue = '0.0';
                        } else if (dataType === 'DATE') {
                            defaultValue = '"' + _this.getCurrentDate() + '"';
                        } else if (dataType === 'TIMESTAMP') {
                            defaultValue = '"' + _this.getCurrentTimestamp() + '"';
                        }
                        template += '\n  "' + propertyType.getCode() + '" : ' + defaultValue;
                    }
                    template += "\n  }\n}\n";
                    Util.unblockUI();
                    var download = document.createElement("a");
                    download.href = "data:application/json," + encodeURI(template);
                    download.target = "_blank";
                    download.download = "metadata.json";
                    download.click();
                });
            });
        });
        $("#dataSetTypeDropdownCancel").on("click", function(event) { 
            Util.unblockUI();
        });
    }

    this.getCurrentTimestamp = function() {
        var time = new Date();
        return this.getCurrentDate() + " " + this.renderWithleadingZeros(time.getHours(), 2) + ":"
                + this.renderWithleadingZeros(time.getMinutes(), 2) + ":"
                + this.renderWithleadingZeros(time.getSeconds(), 2);
    }

    this.getCurrentDate = function() {
        var date = new Date();
        return date.getFullYear() + "-" + this.renderWithleadingZeros(date.getMonth() + 1, 2) + "-" 
                + this.renderWithleadingZeros(date.getDate(), 2);
    }

    this.renderWithleadingZeros = function(number, width) {
        var numberAsString = number.toString();
        while (numberAsString.length < width) {
            numberAsString = '0' + numberAsString;
        }
        return numberAsString;
    }

    //
    // DSS disk space usage dialog
    //

	this.showDiskSpaceDialog = function() {
		var _this = this;

		Util.blockUI(null, null, true);

		mainController.serverFacade.customELNApi({
			"method" : "getDiskSpace",
			"diskMountPoints" : _this.profile.diskMountPoints,
		}, function(error, result){
			if (error) {
				Util.showError("Could not get disk space information.");
			} else {

				var $dialog = $("<div>");
				$dialog
					.append($("<div>")
						.append($("<legend>").text("Available storage space:")));

				var $formFieldContainer = $("<div>");
				$dialog.append($formFieldContainer);

				// close button
				var $closeButton = $("<a>", {
					class : "btn btn-default",
					id : "dropboxFolderNameClose"
				}).text("Close").css("margin-top", "15px");
				$dialog.append($closeButton);

				// add disk space
				var rowHeight = "50px";
				var barHeight = "30px";

				var $table  = $("<table>");
				$table
					.append($("<thead>")
						.append($("<tr>").css("height", rowHeight)
							.append($("<th>").text("Mount point").css("width", "30%"))
							.append($("<th>").text("Size").css("width", "10%"))
							.append($("<th>").text("Used").css("width", "10%"))
							.append($("<th>").text("Available").css("width", "10%"))
							.append($("<th>").text("Percentage").css("width", "40%"))
					));
				$table.css({
					width : "90%",
					"margin-top" : "25px",
					"margin-bottom" : "25px",
					"margin-left" : "auto",
					"margin-right" : "auto",
				});
				$tbody = $("<tbody>");
				$table.append($tbody);
				$formFieldContainer.append($table);

				var diskSpaceValues = result.data;
				for (var i=0; i<diskSpaceValues.length; i++) {
					var filesystem = diskSpaceValues[i]["Mounted_on"]
					var size = diskSpaceValues[i]["Size"]
					var used = diskSpaceValues[i]["Used"]
					var avail = diskSpaceValues[i]["Avail"]
					var usedPercentage = diskSpaceValues[i]["UsedPercentage"]

					var $diskSpaceSection = $("<div>");
					var $total = $("<div>").css({
						height : barHeight,
						width : "100%",
						"background-color" : "lightgray",
						"border-radius" : "7px",
						"text-align" : "center",
						"vertical-align" : "middle",
						"line-height" : barHeight,
					});
					$total.text(usedPercentage);
					var $used = $("<div>").css({
						height: barHeight,
						width : usedPercentage,
						"background-color" : "lightblue",
						"border-radius" : "7px",
						"margin-top" : "-" + barHeight
					});
					$diskSpaceSection.append($total).append($used);

					$tbody
						.append($("<tr>").css("height", rowHeight)
							.append($("<td>").text(filesystem))
							.append($("<td>").text(size))
							.append($("<td>").text(used))
							.append($("<td>").text(avail))
							.append($("<td>").append($diskSpaceSection))
					);
				}

				Util.blockUI($dialog.html(), _this.getDialogCss());

				// events
				$("#dropboxFolderNameClose").on("click", function(event) {
					Util.unblockUI();
				});

			}
		});
	}
	
	this.getPermId = function(entity) {
		var permId = null;
		if(entity["@type"].startsWith("as.dto")) { //v3
			permId = entity.permId.permId;
		} else { // v1
			permId = entity.permId;
		}
		return permId;
	}
	
	this.getType = function(entity) {
		var type = null;
		if(entity["@type"].startsWith("as.dto")) { //v3
			type = entity.type.code;
		} else if(entity.sampleTypeCode) { // v1 sample
			type = entity.sampleTypeCode;
		}
		return type;
	}

	// share project or space with user or group
	// params.title
	// params.components: array of compoments (info, input fields etc.)
	// params.focusedComponent: component which gains focus
	// params.buttons: array of buttons
	// params.css: css as a map
	// params.callback: function to be called on submit
	// params.onBlock: function to be called when dialog is rendered
	this.showDialog = function(params) {

		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		$window.submit(params.callback);

		$window.append($('<legend>').append(params.title));

		for (var i=0; i<params.components.length; i++) {
			$window.append($('<p>').append(params.components[i]));
		}
		var $buttons = $('<p>');
		for (var i=0; i<params.buttons.length; i++) {
			$buttons.append(params.buttons[i]);
			$buttons.append('&nbsp;');
		}
		$window.append($buttons);

		Util.blockUI($window, params.css, false, function() {
			if (params.focuseComponent) {
				params.focuseComponent.focus();
			}
			if (params.onBlock) {
				params.onBlock();
			}
		});
	}

	// params.space: space code (or space code of project)
	// params.project: project code
	// params.acceptCallback: function to be called with (shareWith, groupOrUser)
	this.showAuthorizationDialog = function(params) {

		var _this = this;
		Util.blockUI();

		mainController.serverFacade.searchRoleAssignments({
			space: params.space ? params.space : (params.project ? params.project.spaceCode : null),
			project: params.project ? params.project.code : null,
		}, function(roleAssignments) {

			Util.unblockUI();

			// components
			var $roleAssignmentTable = _this._getRoleAssignmentTable(roleAssignments, _this._revokeRoleAssignment.bind(_this, params));
			var spaceOrProjectLabel = params.space ? params.space : params.project.code;
			var $roleDropdown = FormUtil.getDropdown([
				{ label: 'Observer', value: 'OBSERVER', selected: true },
				{ label: 'User', value: 'USER' },
				{ label: 'Admin', value: 'ADMIN' },
			]);
			var $role = FormUtil.getFieldForComponentWithLabel($roleDropdown, 'Role');
			var $grantToDropdown = FormUtil.getDropdown([
				{ label: 'Group', value: 'Group', selected: true },
				{ label: 'User', value: 'User', selected: true },
			]);
			var $shareWith = FormUtil.getFieldForComponentWithLabel($grantToDropdown, 'grant to');
			var $groupOrUser = FormUtil.getTextInputField('id', 'Group or User');
			// buttons
			var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Grant access' });
			var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Close');
			$btnCancel.click(function() {
			    Util.unblockUI();
			});
			// dialog
			_this.showDialog({
				title: 'Manage access to ' + spaceOrProjectLabel,
				components: [$roleAssignmentTable, $role, $shareWith, $groupOrUser],
				focuseComponent: $groupOrUser,
				buttons: [$btnAccept, $btnCancel],
				css: {'text-align': 'left', 'top': '15%'},
				callback: function() {
					if ($groupOrUser.val() == null || $groupOrUser.val().length == 0) {
						alert("Please enter a user or group name.");
					} else {
						_this._grantRoleAssignment(params, $roleDropdown.val(), $grantToDropdown.val(), $groupOrUser.val());
					}
				},
			});

		});
	}

	this._getRoleAssignmentTable = function(roleAssignments, revokeAction) {
		if (roleAssignments.length == 0) {
			return $('<span>');
		}
		var $table = $('<table>').css({'margin-top': '20px'});
		var $thead = $('<thead>')
			.append($('<tr>')
				.append($('<th>').text('User'))
				.append($('<th>').text('Group'))
				.append($('<th>').text('Role'))
				.append($('<th>')
			));
		var $tbody = $('<tbody>');
		for (var i=0; i<roleAssignments.length; i++) {
			var user = roleAssignments[i].user ? roleAssignments[i].user.userId : '';
			var group = roleAssignments[i].authorizationGroup ? roleAssignments[i].authorizationGroup.code : '';
			var role = roleAssignments[i].role;
			var roleAssignmentTechId = roleAssignments[i].id;
			var $revokeButton = this.getButtonWithIcon('glyphicon-remove', revokeAction.bind(this, roleAssignmentTechId), null, 'revoke');
			$revokeButton.css({'margin-top': '5px'});
			$tbody.append($('<tr>')
				.append($('<td>').text(user))
				.append($('<td>').text(group))
				.append($('<td>').text(role))
				.append($('<td>').append($revokeButton)));
		}
		$table.append($thead).append($tbody);
		$table.css({ width: '100%' });
		return $table;
	}

	this._grantRoleAssignment = function(dialogParams, role, grantTo, groupOrUser) {
		var _this = this;
		mainController.authorizeUserOrGroup({
			user: grantTo == "User" ? groupOrUser : null,
			group: grantTo == "Group" ? groupOrUser.toUpperCase() : null,
			role: role,
			space: dialogParams.space ? dialogParams.space : null,
			project: dialogParams.project ? dialogParams.project.permId : null,
		}, function(success, result) {
			if (success) {
				Util.showSuccess("Access granted.");
				_this.showAuthorizationDialog(dialogParams);
		} else {
				Util.showUserError(result, function() {}, true);
			}
		});
	}

	this._revokeRoleAssignment = function(dialogParams, roleAssignmentTechId) {
		var _this = this;
		mainController.deleteRoleAssignment(roleAssignmentTechId, function(success, result) {
			if (success) {
				Util.showSuccess("Access revoked.");
				_this.showAuthorizationDialog(dialogParams);
			} else {
				Util.showUserError(result, function() {}, true);
			}
		});
	}

    this.getExportAction = function(exportConfig, metadataOnly, includeRoot) {
        return function() {
            Util.blockUI();
            var facade = mainController.serverFacade;
            facade.exportAll(exportConfig, (includeRoot)?true:false, metadataOnly, function(error, result) {
                if(error) {
                    Util.showError(error);
                } else {
               	    Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
                }
            });
        };
    }

	this.getExportButton = function(exportConfig, metadataOnly, includeRoot) {
			return FormUtil.getButtonWithIcon("glyphicon-export", 
					this.getExportAction(exportConfig, metadataOnly, includeRoot),
					metadataOnly ? "Export Metadata only" : "Export Metadata & Data");
	};
	
	this.getFreezeButton = function(entityType, permId, isEntityFrozen) {
		var _this = this;
		var $freezeButton = FormUtil.getButtonWithIcon("glyphicon-lock");
		
		if(isEntityFrozen) {
			$freezeButton.attr("disabled", "disabled");
			$freezeButton.append("Frozen");
		} else {
			$freezeButton.click(function() {
				_this.showFreezeForm(entityType, permId);
			});
		}
		
		return $freezeButton;
	}

    this.createNewSampleOfTypeWithParent = function(sampleTypeCode, experimentIdentifier, sampleIdentifier, parentSample) {
        var argsMap = {
	        "sampleTypeCode" : sampleTypeCode,
	        "experimentIdentifier" : experimentIdentifier
	    }
	    var argsMapStr = JSON.stringify(argsMap);

        mainController.changeView("showCreateSubExperimentPage", argsMapStr);

	    var setParent = function() {
	        mainController.currentView._sampleFormModel.sampleLinksParents.addSample(parentSample);
		    Util.unblockUI();
	    }

	    var repeatUntilSet = function() {
	        if(mainController.currentView.isLoaded()) {
		        setParent();
	        } else {
		        setTimeout(repeatUntilSet, 100);
		    }
	    }

	    repeatUntilSet();
    }

	this.createNewSample = function(experimentIdentifier) {
    		var _this = this;
    		var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true, null, null, IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier));
    		Util.showDropdownAndBlockUI("sampleTypeDropdown", $dropdown);

    		$("#sampleTypeDropdown").on("change", function(event) {
    			var sampleTypeCode = $("#sampleTypeDropdown")[0].value;
                Util.blockUI();
                setTimeout(function() {
                    var argsMap = {
                        "sampleTypeCode" : sampleTypeCode,
                        "experimentIdentifier" : experimentIdentifier
                    };
                    mainController.changeView("showCreateSubExperimentPage", JSON.stringify(argsMap));
                }, 100);
    		});

    		$("#sampleTypeDropdownCancel").on("click", function(event) {
    			Util.unblockUI();
    		});
    }

	this.showFreezeForm = function(entityType, permId) {
		var _this = this;
		
		Util.blockUI();
		
		var parameters = {
				"method" : "freezelist",
				"sessionToken" : mainController.serverFacade.openbisServer.getSession(),
				"entityType" : entityType,
				"permId" : permId
		}
		
		mainController.serverFacade.customASService(parameters, function(result) {
			if(result.status === "OK") {
				Util.unblockUI();
				
				var $window = $('<form>', {
					'action' : 'javascript:void(0);'
				});
				
				$window.append($('<legend>').append("Freeze Entity"));
				
				//
				// List
				//
				$window.append($("<p>")
						.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
						.append(" Choose the entities to freeze (all by default):"));
				
				var $table = $("<table>", { class : "popup-table" } )
								.append($("<tr>")
										.append($("<th>").append("Selected"))
										.append($("<th>").append("Type"))
										.append($("<th>").append("PermId"))
										.append($("<th>").append("Name"))
								);
					
				entityTypeOrder = ["Space", "Project", "Experiment", "Sample", "DataSet"];
				entityMap = result.result;
				
				var getTypeDisplayName = function(type) {
					if(type === "Sample") {
						return ELNDictionary.Sample;
					} else if(type === "Experiment") {
						return ELNDictionary.getExperimentDualName();
					} else {
						return type;
					}
				}
				
				for(var typeOrder = 0 ; typeOrder < entityTypeOrder.length ; typeOrder++) {
					for (key in entityMap) {
						entity = entityMap[key];
						if(entity.type == entityTypeOrder[typeOrder]) {
							$table.append($("<tr>")
									.append($("<td>").append(_this._getBooleanField(_this._createFormFieldId(key), entity.displayName, true)))
									.append($("<td>").append(getTypeDisplayName(entity.type)))
									.append($("<td>").append(entity.permId))
									.append($("<td>").append(entity.displayName))
							);
						}
					}
				}
				
				$window.append($table);
				
				//
				// Warning
				//
				$window.append("<br>");
				$window.append($("<p>")
						.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
						.append(" Enter your password to freeze the entities, after they are frozen no more changes will be possible:"));
				$window.append($("<p>")
						.append($("<span>", { class: "glyphicon glyphicon-warning-sign" }))
						.append($("<span>", { style : "color:red; font-size: large;" }).append(" This operation is irreversible!")));
				
				//
				// Password
				//
				var $passField = FormUtil._getInputField('password', null, 'Password', null, true);
				var $passwordGroup = FormUtil.getFieldForComponentWithLabel($passField, "Password", null);
				$window.append($passwordGroup);
				
				//
				// Buttons
				//
				var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });

				var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
				$btnCancel.click(function() {
					Util.unblockUI();
				});
				
				$window.append($('<br>'));
				
				$window.append($btnAccept).append('&nbsp;').append($btnCancel);
				
				$window.submit(function() {
					if (_this._atLeastOnyEntitySelectedHasBeenSelectedForFreezing(entityMap)) {
						var username = mainController.serverFacade.getUserId();
						var password = $passField.val();					
						new openbis().login(
								username, 
								password, 
								function(data) { 
									if(data.result == null) {
										Util.showUserError('The given password is not correct.');
									} else {
										var sessionToken = data.result;
										
										
										for (key in entityMap) {
											if(!$('#' + _this._createFormFieldId(key))[0].checked) {
												delete entityMap[key];
											}
										}
										
										var parameters = {
												"method" : "freeze",
												"sessionToken" : sessionToken,
												"freezeList" : entityMap
										}
										mainController.serverFacade.customASService(parameters, function(result) {
											if(result.status === "OK") {
												Util.showSuccess("Freezing succeeded.", function() {
													Util.unblockUI();
													switch(entityType) {
													case "SPACE":
														mainController.changeView('showSpacePage', permId);
														break;
													case "PROJECT":
														mainController.changeView('showProjectPageFromPermId', permId);
														break;
													case "EXPERIMENT":
														mainController.changeView('showExperimentPageFromPermId', permId);
														break;
													case "SAMPLE":
														mainController.changeView('showViewSamplePageFromPermId', permId);
														break;
													case "DATASET":
														mainController.changeView('showViewDataSetPageFromPermId', permId);
														break;
													}
												});
											} else {
												Util.showUserError('Freezing failed.', function() {
													Util.unblockUI();
												});
											}
										}, "freeze-api",  _this.showFreezingError);
									}
								});
						Util.blockUI();
					} else {
						Util.showUserError('Nothing selected for freezing.', function() {
							Util.unblockUI();
						});
					}
				});
					
				var css = {
						'text-align' : 'left',
						'top' : '15%',
						'width' : '70%',
						'height' : '400px',
						'left' : '15%',
						'right' : '20%',
						'overflow' : 'auto'
				};
				
				Util.blockUI($window, css);
			} else {
				Util.showUserError('List of entities for freezing failed to .', function() {
					Util.unblockUI();
				});
			}
		}, "freeze-api", _this.showFreezingError);
	}
	
	this._atLeastOnyEntitySelectedHasBeenSelectedForFreezing = function(entityMap) {
		for (key in entityMap) {
			if ($('#' + this._createFormFieldId(key))[0].checked) {
				return true;
			}
		}
		return false;
	}
	
	this._createFormFieldId = function(key) {
		return 'freezing-form-' + key.replace("+", "-").replace(/\./g, "-");
	}
	
	this.showFreezingError = function(error) {
		Util.showError(error.message);
	}

	this.prepareId = function(id) {
		if (id) {
			id = id[0] === '$' ? id.substring(1) : id;
			id = id.split(".").join("");
			id = id.split(" ").join("-").split("/").join("_");
		}
		return id;
	}

    this.renderBooleanGridValue = function(params) {
        if(params.value === null){
            return "(empty)"
        }else{
            return String(params.value)
        }
    }

    this.renderArrayGridValue = function(params) {
        if(!params.value || params.value === null){
            return ""
        }else{
            if(Array.isArray(params.value)) {
                return params.value.toString();
            }
            return String(params.value)
        }
    }

    this.renderMultilineVarcharGridValue = function(row, params, propertyType){
        return this.renderCustomWidgetGridValue(row, params, propertyType)
    }

    this.renderXmlGridValue = function(row, params, propertyType){
        return this.renderCustomWidgetGridValue(row, params, propertyType)
    }

    this.renderCustomWidgetGridValue = function(row, params, propertyType){
        var value = row[propertyType.code]

        if(value === null || value === undefined || value.trim().length === 0){
            return
        }

        var customWidget = this.profile.customWidgetSettings[propertyType.code];
        var forceDisableRTF = this.profile.isForcedDisableRTF(propertyType);

        if(!forceDisableRTF) {
            var $value = null
            var renderTooltip = null

            if(customWidget === 'Word Processor'){
                var valueLowerCase = value.toLowerCase()
                if(valueLowerCase.includes("<img") || valueLowerCase.includes("<table")){
                    $value = $("<img>", { src : "./img/file-richtext.svg", "width": "24px", "height": "24px"})
                    renderTooltip = function(){
                        var valueSanitized = FormUtil.sanitizeRichHTMLText(value)
                        $tooltip = FormUtil.getFieldForPropertyType(propertyType, valueSanitized);
                        $tooltip = FormUtil.activateRichTextProperties($tooltip, undefined, propertyType, valueSanitized, true);
                        return $tooltip
                    }
                }else{
                    return $("<div>").html(FormUtil.sanitizeRichHTMLText(value))
                }
            }else if(customWidget === 'Spreadsheet'){
                $value = $("<img>", { src : "./img/table.svg", "width": "16px", "height": "16px"})
                renderTooltip = function(){
                    $tooltip = $("<div>")
                    JExcelEditorManager.createField($tooltip, FormMode.VIEW, propertyType.code, { properties: row });
                    return $tooltip
                }
            }else{
                $value = $("<div>")
                value.split('\n').forEach(function(line){
                    $("<div>").text(line).appendTo($value)
                })
                return $value
            }

            $value.tooltipster({
                trigger: 'click',
                interactive: true,
                trackTooltip: true,
                trackerInterval: 100,
                theme: 'tooltipster-shadow',
                functionBefore: function(instance, helper){
                    var $content = $("<div>").css({ "max-width" : "50vw", "max-height" : "50vh"}).append(renderTooltip())
                    $(helper.origin).tooltipster('content', $content)
                    return true
                }
            })

            $valueContainer = $("<span>", { style: "cursor: pointer" })
            $valueContainer.append($value)
            return $valueContainer
        }else{
            $value = $("<div>")
            value.split('\n').forEach(function(line){
                $("<div>").text(line).appendTo($value)
            })
            return $value
        }
    }

    this.renderBooleanGridFilter = function(params){
        return React.createElement(window.NgComponents.default.SelectField, {
            label: 'Filter',
            variant: 'standard',
            value: params.value,
            emptyOption: {},
            options: [{value: "(empty)"}, {value: "true"}, {value: "false"}],
            onChange: params.onChange
        })
    }

    this.renderArchivingStatusGridFilter = function(params) {
        return React.createElement(window.NgComponents.default.SelectField, {
            label: 'Filter',
            variant: 'standard',
            value: params.value,
            emptyOption: {},
            options: [
                {value: "AVAILABLE"},
                {value: "LOCKED"},
                {value: "ARCHIVED"},
                {value: "UNARCHIVE_PENDING"},
                {value: "ARCHIVE_PENDING"},
                {value: "BACKUP_PENDING"}
            ],
            onChange: params.onChange
        })
    }

    this.renderVocabularyGridFilter = function(params, vocabulary){
        var options = []

        if(vocabulary && vocabulary.terms){
            vocabulary.terms.forEach(function(term){
                options.push({
                    label: term.label,
                    value: term.code
                })
            })
        }

        return React.createElement(window.NgComponents.default.SelectField, {
            label: 'Filter',
            variant: 'standard',
            value: params.value,
            emptyOption: {},
            options: options,
            onChange: params.onChange
        })
    }

    this.renderDateRangeGridFilter = function(params, dataType){
        return React.createElement(window.NgComponents.default.DateRangeField, {
            variant: "standard",
            value: params.value,
            onChange: params.onChange,
            dateTime: dataType === "TIMESTAMP"
        })
    }

    this.filterDateRangeGridColumn = function(value, filter){
        if(_.isString(filter)){
            if(filter.trim().length === 0){
                return true
            }else{
                if(value === null || value === undefined){
                    return false
                }else{
                    return String(value).trim().includes(filter.trim())
                }
            }
        }else if(_.isObject(filter)){
            var filterFrom = filter.from ? filter.from.dateObject : null
            var filterTo = filter.to ? filter.to.dateObject : null
            if((filterFrom === null || filterFrom === undefined) && (filterTo === null || filterTo === undefined)){
                return true
            }else{
                var matches = true
                if(filterFrom){
                    matches = matches && value >= filter.from.dateString
                }
                if(filterTo){
                    matches = matches && value <= filter.to.dateString
                }
                return matches
            }
        }else{
            return true
        }
    }

    this.sortPropertyColumns = function(propertyColumns, entities){
        var isSortingByOrdinalPossible = entities.every(function(entity){
            var entityKind = _.isString(entity.entityKind) ? entity.entityKind : null

            if(entityKind === null || false === ["SAMPLE", "EXPERIMENT", "DATASET"].includes(entityKind.toUpperCase())){
                return false
            }

            var entityType = _.isString(entity.entityType) ? entity.entityType : null

            if(entityType === null){
                return false
            }

            return true
        })

        if(false === isSortingByOrdinalPossible){
            return propertyColumns.sort(function(p1, p2){
                var label1 = _.isString(p1.label) ? p1.label : ""
                var label2 = _.isString(p2.label) ? p2.label : ""
                return label1.localeCompare(label2)
            })
        }

        var propertyColumnsMap = {}
        var entityTypePropertiesMap = {}
        var sortedPropertyColumnsMap = {}
        var sortedPropertyColumns = []

        propertyColumns.forEach(function(propertyColumn){
            propertyColumnsMap[propertyColumn.property] = propertyColumn
        })

        entities.forEach(function(entity){
            var entityKind = entity.entityKind.toUpperCase()
            var entityType = entity.entityType.toUpperCase()
            
            var entityTypePropertiesKey = entityKind + "-" + entityType
            var entityTypeProperties = entityTypePropertiesMap[entityTypePropertiesKey]

            if(!entityTypeProperties){
                if(entityKind === "SAMPLE"){
                    entityTypeProperties = this.profile.getAllPropertiCodesForTypeCode(entityType)
                }else if(entityKind === "EXPERIMENT"){
                    entityTypeProperties = this.profile.getAllPropertiCodesForExperimentTypeCode(entityType)
                }else if(entityKind === "DATASET"){
                    entityTypeProperties = this.profile.getAllPropertiCodesForDataSetTypeCode(entityType)
                }else{
                    throw new Error("Unsupported entity kind: " + entityKind)
                }
                entityTypePropertiesMap[entityTypePropertiesKey] = entityTypeProperties
            }

            if(entityTypeProperties){
                entityTypeProperties.forEach(function(entityTypeProperty){
                    if(!sortedPropertyColumnsMap[entityTypeProperty]){
                        var propertyColumn = propertyColumnsMap[entityTypeProperty]
                        if(propertyColumn){
                            sortedPropertyColumnsMap[entityTypeProperty] = true
                            sortedPropertyColumns.push(propertyColumn)
                        }
                    }
                })
            }
        })

        propertyColumns.splice(0, propertyColumns.length)
        sortedPropertyColumns.forEach(function(sortedPropertyColumn){
            propertyColumns.push(sortedPropertyColumn)
        })
    }

    this.showDeleteSamples = function(samplePermIds, updateTree, callbackToNextViewOnSuccess) {
        var _this = this;
        var $component = $("<div>");
        var help = "Delete also all descendant " + ELNDictionary.sample + "(s) (i.e. children, grand children etc.) including their data sets, if they exist.";
        var $ddf = FormUtil.getFieldForComponentWithLabel(FormUtil._getBooleanField("delete descendants", help), null, null, true);
        $component.append($ddf);

        var modalView = new DeleteEntityController(function(reason) {
            var deleteDescendants = false;
            var inputs = $component.find("input");
            if (inputs.length > 0) {
                deleteDescendants = inputs[0].checked;
            }
            _this.deleteSamples(samplePermIds, updateTree, callbackToNextViewOnSuccess, reason, deleteDescendants);
        }, true, null, $component);
        modalView.init();
    }

    this.deleteSamples = function(samplePermIds, updateTree, callbackToNextViewOnSuccess, reason, deleteDescendants) {
            var _this = this;
            var doDelete = function(samplesPermIdsToDelete, sampleStoragesCodesToDelete, samplesList, reason) {
                var toDeleteFinal = function(samplesPermIdsToDelete, reason) {
                    mainController.serverFacade.deleteSamples(samplesPermIdsToDelete, reason, function(response) {
                        Util.unblockUI()
                        if(response.error) {
                            Util.showError(response.error.message);
                        } else {
                            Util.showSuccess("" + ELNDictionary.Sample + "(s) moved to Trashcan");
                            if(updateTree) {
                                for(var sIdx = 0; sIdx < samplesPermIdsToDelete.length; sIdx++) {
                                    mainController.sideMenu.deleteNodeByEntityPermId("SAMPLE", samplesPermIdsToDelete[sIdx], true);
                                }
                            }
                            callbackToNextViewOnSuccess();
                        }
                    });
                }

                var $window = $('<form>', { 'action' : 'javascript:void(0);' });
                $window.append($('<legend>').append('These items will be deleted'));

                if(sampleStoragesCodesToDelete.length > 0) {
                    var warningText = "Storages found: " + JSON.stringify(sampleStoragesCodesToDelete) + ". Deleting them will also delete their storage positions.";
                    var $warning = FormUtil.getFieldForLabelWithText(null, warningText);
                    $warning.css('color', FormUtil.warningColor);
                    $window.append($warning);
                }

                var $list = $("<lu>");
                for(var lIdx=0; lIdx < samplesList.length; lIdx++) {
                    if(samplesList[lIdx].getType().getCode() !== "STORAGE_POSITION") {
                        $list.append($("<li>").text(Util.getDisplayNameForEntity(samplesList[lIdx])));
                    }
                }
                var $container = $("<div>", { 'style' : 'padding-left: 10px;' });
                $window.append($container.append($list));

                var css = {
                    'text-align' : 'left',
                    'top' : '15%',
                    'width' : '70%',
                    'left' : '15%',
                    'right' : '20%',
                    'max-height' : '50%',
                    'overflow' : 'auto'
                };

                var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' , 'id' : 'accept-btn'});
                $btnAccept.click(function() {
                        Util.blockUI();
                        if(sampleStoragesCodesToDelete.length > 0) {
                            require([ "as/dto/sample/search/SampleSearchCriteria",
                                        "as/dto/sample/fetchoptions/SampleFetchOptions" ],
                                        function(SampleSearchCriteria, SampleFetchOptions) {
                                var searchCriteria = new SampleSearchCriteria();
                                searchCriteria.withOrOperator();
                                for(var ssIdx=0; ssIdx < sampleStoragesCodesToDelete.length; ssIdx++) {
                                    searchCriteria.withStringProperty("$STORAGE_POSITION.STORAGE_CODE").thatEquals(sampleStoragesCodesToDelete[ssIdx]);
                                }
                                var fetchOptions = new SampleFetchOptions();
                                mainController.openbisV3.searchSamples(searchCriteria, fetchOptions).done(function(results) {
                                    for(var oIdx = 0; oIdx < results.objects.length; oIdx++) {
                                        samplesPermIdsToDelete.push(results.objects[oIdx].getPermId().getPermId());
                                    }
                                    toDeleteFinal(samplesPermIdsToDelete, reason);
                                });
                            });
                        } else {
                            toDeleteFinal(samplesPermIdsToDelete, reason);
                        }
                });
                var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
                $btnCancel.click(function() {
                    Util.unblockUI();
                });
                $window.append($btnAccept).append('&nbsp;').append($btnCancel);
                Util.blockUI($window, css);
            };

            require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
            function(SamplePermId, SampleFetchOptions) {
                var samplePermIdsAsIds = []
                for(var sIdx = 0; sIdx < samplePermIds.length; sIdx++) {
                    samplePermIdsAsIds.push(new SamplePermId(samplePermIds[sIdx]));
                }

                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withType();
                fetchOptions.withProperties();
                if (deleteDescendants) {
                    fetchOptions.withChildrenUsing(fetchOptions);
                } else {
                    fetchOptions.withChildren().withType();
                }
                mainController.openbisV3.getSamples(samplePermIdsAsIds, fetchOptions).done(function(samplesByPermId) {
                    var samplesPermIdsToDelete = [];
                    var samplesList = [];
                    var sampleStoragesCodesToDelete = [];
                    for(permId in samplesByPermId) {
                        var sample = samplesByPermId[permId];
                        if (deleteDescendants) {
                            _this.gatherAllDescendants(samplesPermIdsToDelete, sample, samplesList);
                        } else { // Storage positions always SHOULD be deleted anyway
                            samplesPermIdsToDelete.push(sample.getPermId().getPermId());
                            samplesList.push(sample);
                            for(var idx = 0; idx < sample.children.length; idx++) {
                                var child = sample.children[idx];
                                if (child.getType().getCode() === "STORAGE_POSITION") {
                                    samplesPermIdsToDelete.push(child.getPermId().getPermId());
                                    samplesList.push(child);
                                }
                            }
                        }
                        if(sample.getType().getCode() == "STORAGE") {
                            sampleStoragesCodesToDelete.push(sample.getCode());
                        }
                    }
                    doDelete(samplesPermIdsToDelete, sampleStoragesCodesToDelete, samplesList, reason);
                });
            });
        }

        this.gatherAllDescendants = function(samplePermIds, sample, samplesList) {
            samplePermIds.push(sample.getPermId().getPermId());
            samplesList.push(sample);
            sample.getChildren().forEach(child => this.gatherAllDescendants(samplePermIds, child, samplesList));
        }

}
