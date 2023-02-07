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


var PrintUtil = new function() {

	this.renderNumberOfBytes = function(bytes) {
		if (typeof bytes == "string") {
			bytes = parseInt(bytes)
		}
		var size = bytes;
		var unit = "bytes"
		var kbytes = bytes / 1024.0;
		var mbytes = kbytes / 1024.0;
		var gbytes = mbytes / 1024.0;
		if (gbytes > 1.0) {
			size = gbytes;
			unit = "Gb";
		} else if (mbytes > 1.0) {
			size = mbytes;
			unit = "Mb";
		} else if (kbytes > 1.0) {
			size = kbytes;
			unit = "kb";
		}
		return size.toFixed(1) + " " + unit;
	}

	this.printEntity = function(entity) {
		var newWindow = window.open(undefined,"print " + entity.permId);
		var pageToPrint = $("<html>")
							.append($("<head>"))
							.append($("<body>").append(this.getTable(entity)));
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this._getCodesFromSamples = function(samplesList) {
		var allSamplesByType = {};
		
		if(samplesList) {
			for(var i = 0; i < samplesList.length; i++) {
				var sample = samplesList[i];
				var samplesByType = allSamplesByType[sample.sampleTypeCode];
				if(samplesByType === null || samplesByType === undefined) {
					samplesByType = new Array();
				}
				samplesByType.push(sample);
				
				allSamplesByType[sample.sampleTypeCode] = samplesByType;
			}
		}
		
		var samplesListOfCodes = "";
		
		for(var sampleTypeCode in allSamplesByType) {
			samplesListOfCodes += Util.getDisplayNameFromCode(sampleTypeCode) + ": ";
			var samples = allSamplesByType[sampleTypeCode];
			for(var i = 0; i < samples.length; i++) {
				var sample = samples[i];
				if(i !== 0) {
					samplesListOfCodes += ", ";
				}
				if(sampleTypeCode === "STORAGE_POSITION") {
					samplesListOfCodes += Util.getStoragePositionDisplayName(sample);
				} else {
					samplesListOfCodes += Util.getDisplayNameForEntity(sample); 
				}
			}
			samplesListOfCodes += "</br>";
		}
		
		return samplesListOfCodes;
	}
	
	this.getTable = function(entity, isNotTransparent, optionalTitle, customClass, extraCustomId, extraContent, extraProperties) {
		var $newInspector = $("<div>");
		if(isNotTransparent) {
			$newInspector.css("background-color", "#FBFBFB");
		} else {
			$newInspector.css("background-color", "transparent");
		} 
		
		
		$newInspector.addClass("inspector");
		if(customClass) {
			$newInspector.addClass(customClass);
		}
		
		if(optionalTitle) {
			$newInspector.append(optionalTitle);
		} else {
			var nameLabel = entity.properties[profile.propertyReplacingCode];
			if(!nameLabel) {
				nameLabel = entity.code;
			}
			
			if(entity.sampleTypeCode) {
				var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), "showViewSamplePageFromPermId", entity.permId);
				var codeLink = $("<a>", { "href" : href, "class" : "browser-compatible-javascript-link" }).text(nameLabel);
				codeLink.click(function() {
					mainController.changeView("showViewSamplePageFromPermId", entity.permId);
				});
				$newInspector.append($("<strong>").append(codeLink));
			} else {
				$newInspector.append($("<strong>").text(nameLabel));
			}
		}
		
		var $newInspectorTable = $("<table>", { "class" : "properties table table-condensed" });
		$newInspector.append($newInspectorTable);
		this._addLabelAndValue($newInspectorTable, "Code", entity.code);
		
		if(extraProperties) {
			for(code in extraProperties) {
				var extraProp = extraProperties[code];
				var propLabel = extraProp.label;
				extraProp.value = FormUtil.sanitizeRichHTMLText(extraProp.value);
				if(propLabel.length > 25) {
					propLabel = propLabel.substring(0, 23) + "..."; 
				}
				this._addLabelAndValue($newInspectorTable, propLabel, extraProp.value);
			}
		}
		
		//Show Properties following the order given on openBIS
		if(entity.sampleTypeCode) {
			var sampleTypePropertiesCode = profile.getAllPropertiCodesForTypeCode(entity.sampleTypeCode);
			var sampleTypePropertiesDisplayName = profile.getPropertiesDisplayNamesForTypeCode(entity.sampleTypeCode, sampleTypePropertiesCode);
				
			for(var i = 0; i < sampleTypePropertiesCode.length; i++) {
				var propertyCode = sampleTypePropertiesCode[i];
				var propertyLabel = sampleTypePropertiesDisplayName[i];
				var propertyContent = null;
				var propertyType = profile.getPropertyType(propertyCode);
				if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
					propertyContent = FormUtil.getVocabularyLabelForTermCode(propertyType, entity.properties[propertyCode]);
				} else if(propertyType.dataType === "MATERIAL") {
					var materialValue = entity.properties[propertyCode];
					if(materialValue) {
						var materialType = this._getMaterialTypeFromPropertyValue(materialValue);
						if(materialType === "GENE" && entity.cachedMaterials) { //Specially supported materials from openBIS
							var gene = this._getMaterialFromCode(entity.cachedMaterials, this._getMaterialCodeFromPropertyValue(materialValue));
							propertyContent = $("<span>").append(gene.properties["GENE_SYMBOLS"]);
						} else {
							propertyContent = $("<span>").append(materialValue);
						}
					}
				} else {
					propertyContent = entity.properties[propertyCode];
					propertyContent = Util.getEmptyIfNull(propertyContent);
					propertyContent = FormUtil.sanitizeRichHTMLText(propertyContent);
					propertyContent = Util.replaceURLWithHTMLLinks(propertyContent);

					if(propertyType.dataType === "MULTILINE_VARCHAR") {
                        var imageWithoutDomain = "src=\"/openbis/openbis/";
                        var imageWithDomain = "src=\"" + window.location.origin + "/openbis/openbis/openbis/";
					    propertyContent = propertyContent.split(imageWithoutDomain).join(imageWithDomain);
					}
				}
				
				var isSingleColumn = false;
				if(((propertyContent instanceof String) || (typeof propertyContent === "string"))) {
					var transformerResult = profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
					isSingleColumn = transformerResult["isSingleColumn"];
					propertyContent = transformerResult["content"];
					if(!profile.isRTF(propertyType)) {
						propertyContent = propertyContent.replace(/\n/g, "<br />");
					}
				}
				
				if(propertyContent && !profile.isSystemProperty(propertyType)) { // Only show non empty properties
					if (propertyLabel === "Spreadsheet" && propertyContent.toUpperCase().startsWith("<DATA>") && propertyContent.toUpperCase().endsWith("</DATA>")) {
						propertyContent = propertyContent.slice(6, -7);
						propertyContent = window.atob(propertyContent);
						propertyContent = this._convertJsonToHtml(JSON.parse(propertyContent));
					}

					if(isSingleColumn) {
						$newInspectorTable
						.append($("<tr>")
									.append($("<td>", { "class" : "property", "colspan" : "2" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(propertyLabel + ":").append("<br>").append(propertyContent)))
								);
					} else {
						this._addLabelAndValue($newInspectorTable, propertyLabel, propertyContent);
					}
				}
			}
		}
		
		if(entity["@type"] === "Sample") {
			//Show Parent Codes
			var allParentCodesAsText = this._getCodesFromSamples(entity.parents);
			if(allParentCodesAsText.length > 0) {
				this._addLabelAndValue($newInspectorTable, "Parents", allParentCodesAsText);
			}
				
			//Show Children Codes
			var allChildrenCodesAsText = this._getCodesFromSamples(entity.children);
			if(allChildrenCodesAsText.length > 0) {
				this._addLabelAndValue($newInspectorTable, "Children", allChildrenCodesAsText);
			}
		}
		
		//Show Modification Date
		if(entity.registrationDetails) {
			this._addLabelAndValue($newInspectorTable, "Modification Date", new Date(entity.registrationDetails["modificationDate"]));
			
			//Show Creation Date
			this._addLabelAndValue($newInspectorTable, "Registration Date", new Date(entity.registrationDetails["registrationDate"]));
		}
		
		if(extraCustomId && extraContent) {
			$newInspector.append($("<div>", { "class" : "inspectorExtra", "id" : extraCustomId}).append(extraContent));
		}
		
		return $newInspector;
	};
	
	this._addLabelAndValue = function($newInspectorTable, label, value) {
		$newInspectorTable
		.append($("<tr>")
					.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLabel"}).append(label + ":")))
					.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLineBreak" }).append(value)))
				);

	}

	this._convertJsonToHtml = function(json) {
		data = json["data"];
		styles = json["style"];

		var commonStyle = "border: 1px solid black;";
		var tableStyle = commonStyle + " border-collapse: collapse;";

		tableBody = ["<table style='", tableStyle, "'>\n"];
		for (var i = 0; i < data.length; i++) {
			tableBody.push("<tr>\n");
			var dataRow = data[i];
			for (var j = 0; j < dataRow.length; j++) {
				var cell = dataRow[j];
				stylesKey = this._convertNumericToAlphanumeric(i, j);
				style = styles[stylesKey];
				tableBody.push("  <td style='", commonStyle, " ", style, "'> ", cell, " </td>\n");
			}
			tableBody.push("</tr>\n");
		}
		tableBody.push("</table>");

		return tableBody.join("");
	};

	this._convertNumericToAlphanumeric = function(row, col) {
		var aCharCode = "A".charCodeAt(0);
		var ord0 = col % 26;
		var ord1 = col / 26;
		var char0 = String.fromCharCode(aCharCode + ord0);
		var char1 = (ord1 > 0) ? String.fromCharCode(aCharCode + ord1 - 1) : "";
		return char1 + char0 + (row + 1).toString(10);
	};
	
	this._getMaterialFromCode = function(materials, code) {
		for(var mIdx = 0; mIdx < materials.length; mIdx++) {
			if(materials[mIdx].materialCode === code) {
				return materials[mIdx];
			}
		}
		return null;
	}
	
	this._getMaterialTypeFromPropertyValue = function(propertyValue) {
		var materialIdentifierParts = propertyValue.split(" ");
		var materialType = materialIdentifierParts[1].substring(1, materialIdentifierParts[1].length-1);
		return materialType;
	}
	
	this._getMaterialCodeFromPropertyValue = function(propertyValue) {
		return propertyValue.split(" ")[0];
	}
	
	this._getMaterialIdentifierFromPropertyValue = function(propertyValue) {
		var materialIdentifierParts = propertyValue.split(" ");
		var materialType = materialIdentifierParts[1].substring(1, materialIdentifierParts[1].length-1);
		var materialIdentifier = IdentifierUtil.getMaterialIdentifier(materialType, materialIdentifierParts[0]);
		return materialIdentifier;
	}
	
}