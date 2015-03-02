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

/**
 * Creates an instance of Inspector.
 *
 * @constructor
 * @this {Inspector}
 * @param {ServerFacade} serverFacade The facade used to access server side search functionality.
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 */
function Inspector(serverFacade, containerId, profile) {
	this.serverFacade = serverFacade;
	this.containerId = containerId;
	this.profile = profile;
	this.inspectedSamples = [];
	
	this.repaint = function() {
		$("#"+containerId).empty();
		var allInspectors = ""
		allInspectors += "<div class='row'>";
		allInspectors += "<div class='col-md-12' style='margin-top: 10px;'>";
		allInspectors += "<a class='btn btn-default' href='javascript:mainController.inspector.printInspectors()'><span class='glyphicon glyphicon-print'></span></a>";
		allInspectors += "<div id='inspectorsContainer' class='inspectorsContainer'>";
		allInspectors += this.getAllInspectors(false, true, true, true);
		allInspectors += "</div>";
		allInspectors += "</div>";
		allInspectors += "</div>";	
		$("#"+containerId).append(allInspectors);
	}
	
	this.containsByPermId = function(permId) {
		//Bugfix Hack to avoid blank screens
		if(!this.inspectedSamples) {
			this.inspectedSamples = [];
		}

		for(var i = 0; i < this.inspectedSamples.length; i++) {
			if(this.inspectedSamples[i].permId === permId) {
				return i;
			}
		}
		return -1;
	}
	
	this.containsSample = function(sample) {
		//Bugfix Hack to avoid blank screens
		if(!this.inspectedSamples) {
			this.inspectedSamples = [];
		}

		for(var i = 0; i < this.inspectedSamples.length; i++) {
			if(this.inspectedSamples[i].permId === sample.permId) {
				return i;
			}
		}
		return -1;
	}
	
	this.addInspectSampleIfNotFound = function(sampleToInspect) {
		var samplePosition = this.containsSample(sampleToInspect);
		
		if(samplePosition === -1) {
			this.toggleInspectSample(sampleToInspect);
			return true;
		}
		
		return false;
	}
	
	this.toggleInspectPermId = function(permId) {
		var _this = this;
		//Already inspected check
		var samplePosition = this.containsByPermId(permId);
		var isInspected = null;
		if(samplePosition !== -1) {
			this.inspectedSamples.splice(samplePosition, 1);
			isInspected = false;
			$("#num-pins").empty();
			$("#num-pins").append(_this.inspectedSamples.length);
		} else {
			mainController.serverFacade.searchWithUniqueId(permId, function(data) {
				_this.inspectedSamples.push(data[0]);
				$("#num-pins").empty();
				$("#num-pins").append(_this.inspectedSamples.length);
			});
			
			isInspected = true;
		}
		return isInspected;
	}
	
	//Public method that should be used to add a page, is used by the MainController.js
	this.toggleInspectSample = function(sampleToInspect) {
		var isInspected = null;
		//Null Check
		if(sampleToInspect === null || sampleToInspect === undefined) {
			return;
		}
		
		//Already inspected check
		var samplePosition = this.containsSample(sampleToInspect);
		if(samplePosition !== -1) {
			this.inspectedSamples.splice(samplePosition, 1);
			isInspected = false;
		} else {
			this.inspectedSamples.push(sampleToInspect);
			isInspected = true;
		}
		
		$("#num-pins").empty();
		$("#num-pins").append(this.inspectedSamples.length);
		
		return isInspected;
	}
	
	this.getAllInspectors = function(withSeparator, withClose, withColors, withLinks) {
		var inspectorsContent = "";
		for(var i=0;i<this.inspectedSamples.length;i++) {
			inspectorsContent += this.getInspectorTable(this.inspectedSamples[i], withClose, withColors, withLinks);
			if(withSeparator) {
				inspectorsContent += "<hr>";
			}
		}
		return inspectorsContent;
	}
	
	this.closeNewInspector = function(sampleIdToDelete) {
		for(var i = 0; i < this.inspectedSamples.length; i++) {
			if(this.inspectedSamples[i].id === sampleIdToDelete) {
				this.inspectedSamples.splice(i, 1);
				break;
			}
		
		}
		
		$("#num-pins").empty();
		$("#num-pins").append(this.inspectedSamples.length);
		this.repaint();
	}
	
	this.printInspector = function(entityPermId) {
		for(var i = 0; i < this.inspectedSamples.length; i++) {
			if(this.inspectedSamples[i].permId === entityPermId) {
				this.printSample(this.inspectedSamples[i])
			}
		}
	}
	
	this.printSample = function(sample) {
		var newWindow = window.open(undefined,"print " + sample.permId);
		
		var pageToPrint = "";
			pageToPrint += "<html>";
			pageToPrint += "<head>";
			pageToPrint += "</head>";
			pageToPrint += "<body stlye='font-family: '\"'Helvetica Neue\",Helvetica,Arial,sans-serif;'>";
			pageToPrint += this.getInspectorTable(sample, false, false, false);
			pageToPrint += "</body>";
			pageToPrint += "</html>";
		
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this.toogleInspector = function(permId) {
		if($("#" + permId).is(":visible")) {
			$("#" + permId).hide();
			$("#" + permId+"_ICON").removeClass("glyphicon glyphicon-chevron-up");
			$("#" + permId+"_ICON").addClass("glyphicon glyphicon-chevron-down");
		} else {
			$("#" + permId).show();
			$("#" + permId+"_ICON").removeClass("glyphicon glyphicon-chevron-down");
			$("#" + permId+"_ICON").addClass("glyphicon glyphicon-chevron-up");
		}
	}
	
	this.printInspectors = function() {
		var newWindow = window.open(undefined,"print");
		
		var pageToPrint = "";
			pageToPrint += "<html>";
			pageToPrint += "<head>";
			pageToPrint += "</head>";
			pageToPrint += "<body stlye='font-family: '\"'Helvetica Neue\",Helvetica,Arial,sans-serif;'>";
			pageToPrint += this.getAllInspectors(true, false, false, false);
			pageToPrint += "</body>";
			pageToPrint += "</html>";
		
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this.showSampleOnInspector = function(samplePermId) {
		var localReference = this;
		
		this.serverFacade.searchWithUniqueId(samplePermId, function(data) {
			//Clean glow effect in case was used already with that div
			var divID = data[0].sampleTypeCode + "_" + data[0].code + "_INSPECTOR";
			$("#"+divID).removeClass("glow");
			
			var isAdded = localReference.addInspectSampleIfNotFound(data[0]);
			if(isAdded) {
				var inspectorTable = localReference.getInspectorTable(data[0], true, true, true);
				$("#inspectorsContainer").append(inspectorTable);
			}
			
			//Move Scrollbar	
			var objDiv = document.getElementById(divID);
			var moveTo = moveTo = objDiv.offsetTop-50;
			$('html,body').animate({scrollTop:  moveTo}, 200, "swing");
			
			
			var glow = function() {
				//Make it Glow
				$("#"+divID).addClass("glow");
			}
			
			setTimeout(glow, 300);
		});
	}
	
	this.getParentsChildrenText = function(parentsChildrenList, withLinks) {
		var allParentCodesByType = {};
		
		if(parentsChildrenList) {
			for(var i = 0; i < parentsChildrenList.length; i++) {
				var parent = parentsChildrenList[i];
				var parentsWithType = allParentCodesByType[parent.sampleTypeCode];
				if(parentsWithType === null || parentsWithType === undefined) {
					parentsWithType = new Array();
				}
				parentsWithType.push(parent);
				
				allParentCodesByType[parent.sampleTypeCode] = parentsWithType;
			}
		}
		
		var allParentCodesAsText = "";
		
		for(var sampleType in allParentCodesByType) {
			var displayName = profile.getSampleTypeForSampleTypeCode(sampleType).description;
			if(displayName === null) {
				displayName = sampleType;
			}
			allParentCodesAsText += displayName + ": ";
			var parents = allParentCodesByType[sampleType];
			for(var i = 0; i < parents.length; i++) {
				var parent = parents[i];
				if(withLinks) {
					allParentCodesAsText += "<a href=\"javascript:mainController.inspector.showSampleOnInspector('" + parent.permId + "');\">" + parent.code + "</a> ";
				} else {
					allParentCodesAsText += parent.code + " ";
				}
			}
			allParentCodesAsText += "</br>";
		}
		
		return allParentCodesAsText;
	}
	
	this.getInspectorTable = function(entity, showClose, withColors, withLinks, optionalTitle, isCondensed) {
		var defaultColor = null;
		
		if(!withColors) {
			defaultColor = "#ffffff"
		} else {
			defaultColor = this.profile.getColorForInspectors(entity.sampleTypeCode);
		} 

		var inspector = "";
			var divID = entity.sampleTypeCode + "_" + entity.code + "_INSPECTOR";
			
			var inspectorClass = null;
			if(isCondensed) {
				inspectorClass = 'inspectorCondensed';
			} else {
				inspectorClass = 'inspector';
			}
			
			inspector += "<div id='"+divID+"' class='" + inspectorClass + "' style='background-color:" + defaultColor + ";' >";
			
			if(showClose) {
				var removeButton = "<span class='btn inspectorToolbar btn-default' style='float:left; margin: 2px' onclick='mainController.inspector.closeNewInspector(\""+entity.id+"\")'><i class='glyphicon glyphicon-remove'></i></span>";
				inspector += removeButton;
			}
			
			if(withLinks) {
				var toogleButton = "<span class='btn inspectorToolbar btn-default' style='float:left; margin: 2px' onclick='mainController.inspector.toogleInspector(\""+entity.permId+"_TOOGLE\")'><i id='"+entity.permId+"_TOOGLE_ICON' class='glyphicon glyphicon-chevron-up'></i></span>";
				inspector += toogleButton;
			}
			
			if(optionalTitle) {
				inspector += optionalTitle;
			} else {
				inspector += "<strong>" + entity.code + "</strong>";
			}
			
			
			if(withLinks) {
				var printButton = "<span class='btn btn-default inspectorToolbar' style='float:right; margin: 2px;' onclick='javascript:mainController.inspector.printInspector(\""+entity.permId+"\")'><i class='glyphicon glyphicon-print'></i></span>";
				inspector += printButton;
				var viewButton = "<span class='btn btn-default inspectorToolbar' style='float:right; margin: 2px' onclick='javascript:mainController.changeView(\"showViewSamplePageFromPermId\",\""+entity.permId+"\")'><i class='glyphicon glyphicon-edit'></i></span>";
				inspector += viewButton;
				var hierarchyButton = "<span class='btn btn-default inspectorToolbar' style='float:right; margin: 2px' onclick=\"javascript:mainController.changeView('showSampleHierarchyPage','"+entity.permId+"');\"><img src='./img/hierarchy-icon.png' style='width:16px; height:17px;' /></span>";
				inspector += hierarchyButton;
			}
			
			inspector += "<table id='" + entity.permId +"_TOOGLE' class='properties table table-condensed'>"
			
			//Show Properties following the order given on openBIS
			var sampleTypePropertiesCode =  this.profile.getAllPropertiCodesForTypeCode(entity.sampleTypeCode);
			var sampleTypePropertiesDisplayName = this.profile.getPropertiesDisplayNamesForTypeCode(entity.sampleTypeCode, sampleTypePropertiesCode);
			
			for(var i = 0; i < sampleTypePropertiesCode.length; i++) {
				
				var propertyCode = sampleTypePropertiesCode[i];
				var propertyLabel = sampleTypePropertiesDisplayName[i];
				var propertyContent = entity.properties[propertyCode];
				
				//
				// Fix to show vocabulary labels instead of codes
				//
				var sampleType = this.profile.getSampleTypeForSampleTypeCode(entity.sampleTypeCode);
				var propertyType = this.profile.getPropertyTypeFrom(sampleType, propertyCode);
				if(propertyType && propertyType.dataType === "CONTROLLEDVOCABULARY") {
					var vocabulary = null;
					if(isNaN(propertyType.vocabulary)) {
						vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
					} else {
						vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
					}
					
					if(vocabulary) {
						for(var j = 0; j < vocabulary.terms.length; j++) {
							if(vocabulary.terms[j].code === propertyContent) {
								propertyContent = vocabulary.terms[j].label;
								break;
							}
						}
					}
				}
				// End Fix
				
				propertyContent = Util.getEmptyIfNull(propertyContent);
				
				var isSingleColumn = false;
				if((propertyContent instanceof String) || (typeof propertyContent === "string")) {
					var transformerResult = this.profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
					isSingleColumn = transformerResult["isSingleColumn"];
					propertyContent = transformerResult["content"];
					propertyContent = propertyContent.replace(/\n/g, "<br />");
				}
				
				if(propertyContent !== "") {
					propertyContent = Util.replaceURLWithHTMLLinks(propertyContent);
					inspector += "<tr>";
						
					if(isSingleColumn) {
						inspector += "<td class='property' colspan='2'>"+propertyLabel+"<br />"+propertyContent+"</td>";
					} else {
						inspector += "<td class='property'>"+propertyLabel+"</td>";
						inspector += "<td class='property'><p class='inspectorLineBreak'>"+propertyContent+"</p></td>";
					}
					
					inspector += "</tr>";
				}
			}
			
			//Show Properties not found on openBIS (TO-DO Clean duplicated code)
			for(propertyCode in entity.properties) {
				if($.inArray(propertyCode, sampleTypePropertiesCode) === -1) {
					var propertyLabel = propertyCode;
					var propertyContent = entity.properties[propertyCode];
					propertyContent = Util.getEmptyIfNull(propertyContent);
					
					var isSingleColumn = false;
					if((propertyContent instanceof String) || (typeof propertyContent === "string")) {
						var transformerResult = this.profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
						isSingleColumn = transformerResult["isSingleColumn"];
						propertyContent = transformerResult["content"];
						propertyContent = propertyContent.replace(/\n/g, "<br />");
					}
					
					if(propertyContent !== "") {
						inspector += "<tr>";
							
						if(isSingleColumn) {
							inspector += "<td class='property' colspan='2'>"+propertyLabel+"<br />"+propertyContent+"</td>";
						} else {
							inspector += "<td class='property'>"+propertyLabel+"</td>";
							inspector += "<td class='property'><p class='inspectorLineBreak'>"+propertyContent+"</p></td>";
						}
						
						inspector += "</tr>";
					}
				}
			}
			
			//Show Parent Codes
			var allParentCodesAsText = this.getParentsChildrenText(entity.parents, withLinks);
			if(allParentCodesAsText.length > 0) {
				inspector += "<tr>";
				inspector += "<td class='property'>Parents</td>";
				inspector += "<td class='property'>"+allParentCodesAsText+"</td>";
				inspector += "</tr>";
			}
			
			//Show Children Codes
			var allChildrenCodesAsText = this.getParentsChildrenText(entity.children, withLinks);
			if(allChildrenCodesAsText.length > 0) {
				inspector += "<tr>";
				inspector += "<td class='property'>Children</td>";
				inspector += "<td class='property'>"+allChildrenCodesAsText+"</td>";
				inspector += "</tr>";
			}
			
			//Show Modification Date
			inspector += "<tr>";
			inspector += "<td class='property'>Modification Date</td>";
			inspector += "<td class='property'>"+new Date(entity.registrationDetails["modificationDate"])+"</td>";
			inspector += "</tr>";
		
			//Show Creation Date
			inspector += "<tr>";
			inspector += "<td class='property'>Registration Date</td>";
			inspector += "<td class='property'>"+new Date(entity.registrationDetails["registrationDate"])+"</td>";
			inspector += "</tr>";
			
			inspector += "</table>"
			
			var extraContainerId = entity.sampleTypeCode + "_" + entity.code+"_INSPECTOR_EXTRA";
			inspector += "<div class='inspectorExtra' id='"+ extraContainerId + "'></div>";
			this.profile.inspectorContentExtra(extraContainerId, entity);
			
			inspector += "</div>"
			
			
		return inspector;
	}
}