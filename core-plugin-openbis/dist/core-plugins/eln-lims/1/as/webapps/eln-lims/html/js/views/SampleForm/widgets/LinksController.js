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

var EmptyLinksController = {
	    init : function($container) {
	    },
	    getAnnotations : function() {
	        return {};
	    },
        isValid : function() {
            return true;
        },
        addVirtualSample : function(sample) {
        },
        addSample : function(sample, isInit) {
        },
        addSamplesOnInit : function(samples) {
        },
        getSamples : function() {
            return [];
        },
        getSampleByIdentifier : function(identifier) {
            return [];
        },
        getSamplesIdentifiers : function() {
            return [];
        },
        getSamplesAddedIdentifiers : function() {
            return [];
        },
        getSamplesRemovedIdentifiers : function() {
            return [];
        }
};

function LinksController(title, sampleTypeHints, isDisabled, samplesToEdit, showAnnotableTypes, disableAddAnyType, sampleTypeCode, stateObj, spaceCode, setDirty) {
	var linksModel = new LinksModel(title, sampleTypeHints, isDisabled, showAnnotableTypes, disableAddAnyType, sampleTypeCode, stateObj, spaceCode);
	var linksView = new LinksView(this, linksModel);
	var dirtyCallback = setDirty;
	
	this.init = function($container) {
		linksView.repaint($container);
		
		if(sampleTypeHints && showAnnotableTypes) {
			for(var sIdx = 0; sIdx < sampleTypeHints.length; sIdx++) {
				linksView.initContainerForType(sampleTypeHints[sIdx].TYPE, undefined, sampleTypeHints[sIdx].LABEL);
			}
		}
		
		if(samplesToEdit) {
			this.addSamplesOnInit(samplesToEdit);
		}
	}
	
	this.refresh = function() {
		linksView.refresh();
	}
	
	//
	// API - Used by Sample Form
	//

	// 2. Used by other Controller/Form get the Annotations
	this.getAnnotations = function() {
	    return linksModel.stateObj;
	}

    // 1. isValid validates the stateObj
	this.isValid = function() {
		if(sampleTypeHints) {

            var allOwnerAnnotations = linksView.stateObj;

			for(var typeIdx = 0; typeIdx < sampleTypeHints.length; typeIdx++) {
				var sampleTypeHint = sampleTypeHints[typeIdx];
				var sampleTypeCode = sampleTypeHint["TYPE"];
				var sampleTypeMinCount = sampleTypeHint["MIN_COUNT"];
				var sampleTypeMaxCount = sampleTypeHint["MAX_COUNT"];
				var sampleTypeAnnotations = sampleTypeHint["ANNOTATION_PROPERTIES"];
				var sampleTypeCount = (linksModel.samplesByType[sampleTypeCode])?linksModel.samplesByType[sampleTypeCode].length:0;
				if(sampleTypeCount < sampleTypeMinCount) {
					Util.showUserError("Currently only have " + sampleTypeCount + " of the " + sampleTypeMinCount + " required " + sampleTypeCode + ".");
					return false;
				}
				if(sampleTypeMaxCount && sampleTypeCount > sampleTypeMaxCount) {
					Util.showUserError("Currently have " + sampleTypeCount + " of the maximum " + sampleTypeMaxCount + " for " + sampleTypeCode + ".");
					return false;
				}
				
				if(sampleTypeCount > 0) {
					for(var sampleIdx = 0; sampleIdx < linksModel.samplesByType[sampleTypeCode].length; sampleIdx++) {
						var sampleFromIdx = linksModel.samplesByType[sampleTypeCode][sampleIdx];
						var sampleFromIdxAnnotations = null;
						
						if(allOwnerAnnotations && allOwnerAnnotations[sampleFromIdx.permId]) {
							sampleFromIdxAnnotations = allOwnerAnnotations[sampleFromIdx.permId];
						}
						
						if(sampleFromIdxAnnotations) {
							for(var annotIdx = 0; annotIdx < sampleTypeAnnotations.length; annotIdx++) {
								var sampleTypeAnnotation = sampleTypeAnnotations[annotIdx];
								var sampleTypeAnnotationType = sampleTypeAnnotation["TYPE"];
								
								var sampleTypeAnnotationIsMandatory = sampleTypeAnnotation["MANDATORY"];
								if(sampleTypeAnnotationIsMandatory && !sampleFromIdxAnnotations[sampleTypeAnnotationType]) {
									Util.showUserError("Missing an annotation " + sampleTypeAnnotationType + " on " + sampleFromIdx.code +".");
									return false;
								} else {
									var propertyType = profile.getPropertyType(sampleTypeAnnotationType);
									var propertyValue = sampleFromIdxAnnotations[sampleTypeAnnotationType];
									
									var isValid = true;
									switch(propertyType.dataType) {
										case "BOOLEAN":
											break;
										case "CONTROLLEDVOCABULARY":
											break;
										case "HYPERLINK":
											break;
										case "INTEGER":
											isValid = FormUtil.isInteger(propertyValue);
											break;
										case "MATERIAL":
											break;
										case "MULTILINE_VARCHAR":
											break;
										case "REAL":
											isValid = FormUtil.isNumber(propertyValue);
											break;
										case "TIMESTAMP":
											break;
										case "DATE":
											break;
										case "VARCHAR":
											break;
										case "XML":
											break;
									}
									if(!isValid) {
										Util.showUserError("Annotation " + sampleTypeAnnotationType + " is not an " + propertyType.dataType +".");
										return false;
									}
								}
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	this.addVirtualSample = function(sample) {
		linksView.updateSample(sample, true, false);
	}
	
	this.addSample = function(sample, isInit) {
		Util.blockUI();
		mainController.serverFacade.searchWithIdentifiers([sample.identifier], function(results) {
			if(results.length > 0) {
				linksView.updateSample(results[0], true, isInit);
				dirtyCallback();
				Util.unblockUI();
			}
		});
	}
	
	
	this.addSamplesOnInit = function(samples) {
		Util.blockUI();
		
		if(!linksModel.isDisabled) {
			var samplesByType = {};
			if(samples && samples.length > 0) {
				for(var sIdx = 0; sIdx < samples.length; sIdx++) {
					var sampleTypeCode = samples[sIdx].sampleTypeCode;
					var samplesOfType = samplesByType[sampleTypeCode];
					if(!samplesOfType) {
						samplesOfType = [];
						samplesByType[sampleTypeCode] = samplesOfType;
					}
					samplesOfType.push(samples[sIdx]);
				}
			}
			
			for(var type in samplesByType) {
				linksView.updateSample(samplesByType[type], true, true);
			}
		} else {
			// Only add once on view mode, there can be a race condition happening on this case
			// Each time we add, the fuelux repeater html needs to load if is not cached by the browser, typically done the first time you use it during the day, but can vary due to caching policies.
			// If the last load, don't finish last, all samples will not be displayed, this can only happen on view mode since all sample types share the same container and this corner case don't requires to load by type anyway.
			linksView.updateSample(samples, true, true);  
		}
		
		Util.unblockUI();
	}
	
	this.getSamples = function() {
		var allSamples = [];
		for(var sampleTypeCode in linksModel.samplesByType) {
			allSamples = allSamples.concat(linksModel.samplesByType[sampleTypeCode]);
		}
		return allSamples;
	}
	
	this.getSampleByIdentifier = function(identifier) {
		for(var sampleTypeCode in linksModel.samplesByType) {
			var samplesByType = linksModel.samplesByType[sampleTypeCode];
			for(var sIdx = 0; sIdx < samplesByType.length; sIdx++) {
				if(samplesByType[sIdx].identifier === identifier) {
					return samplesByType[sIdx];
				}
			}
		}
		return null;
	}
	
	this.getSamplesIdentifiers = function() {
		var allSamples = this.getSamples();
		var allSamplesIdentifiers = [];
		for(var sIdx = 0; sIdx < allSamples.length; sIdx++) {
			var sample = allSamples[sIdx];
			allSamplesIdentifiers.push(sample.identifier);
		}
		return allSamplesIdentifiers;
	}
	
	this.getSamplesAddedIdentifiers = function() {
		return linksModel.samplesAdded;
	}
	
	this.getSamplesRemovedIdentifiers = function() {
		return linksModel.samplesRemoved;
	}
}