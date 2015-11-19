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
function PlateView(plateController, plateModel) {
	this._plateController = plateController;
	this._plateModel = plateModel;
	
	this.getPlaceHolder = function() {
		var container = $("<div>", { "id" : this._plateModel.getPlaceHolderId() });
		var gridTable = this.getGridTable(false);
		container.append(gridTable);
		return container[0].outerHTML
	}
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		//Paint Toolbar
		var $toolbar = $("<div>");
		if( this._plateModel.sample.featureVectorsCache &&
			this._plateModel.sample.featureVectorsCache.featureVectorDatasets) {
			
			//1. Selected Feature Vector Dataset Features
			var $featureVectorDatasetFeaturesDropdown = FormUtil.getDropDownForTerms(
					"featureVectorDatasetFeaturesDropdown-" + this._plateModel.sample.permId,
					[],
					"Choose a Feature Vector Dataset first",
					false
					);
			
			//2. Feature Vector Dataset Dropdow
			var featureVectorDatasets = this._plateModel.sample.featureVectorsCache.featureVectorDatasets;
			var featureVectorDatasetsDropdowTerms = [];
			for(var fvdIdx = 0; fvdIdx < featureVectorDatasets.length; fvdIdx++) {
				featureVectorDatasetsDropdowTerms.push({
					code : featureVectorDatasets[fvdIdx],
					label : featureVectorDatasets[fvdIdx]
				})
			}
			
			var $featureVectorDatasetsDropdown = FormUtil.getDropDownForTerms(
												"featureVectorDatasetsDropdow-" + this._plateModel.sample.permId,
												featureVectorDatasetsDropdowTerms,
												"Choose a Feature Vector Dataset please",
												false
												);
			
			$featureVectorDatasetsDropdown.change(function(event) {
				var featureVectorDatasetCode = $(this).val();
				if(!featureVectorDatasetCode) {
					$featureVectorDatasetFeaturesDropdown.empty();
					$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', '').text("Choose a Feature Vector Dataset first"));
				} else {
					var featureVectorDatasetFeatures = _this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode];
					
					var loadFeatureVectorDatasetFeatures = function() {
						var featureVectorDatasetFeatures = _this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode];
						$featureVectorDatasetFeaturesDropdown.empty();
						$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', '').attr('selected', '').text(''));
						for(featureVectorDatasetFeatureCode in featureVectorDatasetFeatures) {
							$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', featureVectorDatasetFeatureCode).text(featureVectorDatasetFeatures[featureVectorDatasetFeatureCode]));
						}
					}
					
					if(!featureVectorDatasetFeatures) {
						mainController.serverFacade.customELNApi({
							"method" : "listAvailableFeatures",
							"samplePlatePermId" : _this._plateModel.sample.permId,
							"featureVectorDatasetPermId" : featureVectorDatasetCode
						}, function(error, result){
							if(error) {
								Util.showError(error);
							} else {
								_this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode] = result.data;
								loadFeatureVectorDatasetFeatures();
							}
						});
					} else {
						loadFeatureVectorDatasetFeatures();
					}
				}
			});
			
			//Build Toolbar
			$toolbar.append(FormUtil.getFieldForComponentWithLabel($featureVectorDatasetsDropdown, "Feature Vector Datasets"))
					.append(FormUtil.getFieldForComponentWithLabel($featureVectorDatasetFeaturesDropdown, "Feature Vector Dataset Features"));
		}
		
		
		
		//Paint grid
		var $gridTable = this.getGridTable(true);
		$container.append($toolbar).append($gridTable);
	}
	
	this.getGridTable = function(withWells) {
		var gridTable = $("<table>", { "class" : "table table-bordered gridTable", "style" : "table-layout: fixed;" });
		
		for(var i = 0; i <= this._plateModel.numRows; i++) {
			var $row = $("<tr>");
			for(var j = 0; j <= this._plateModel.numColumns; j++) {
				var $cell = null;
				if(i === 0 && j === 0) { //Empty cell at the top left
					$cell = $("<th>");
				} else if (i === 0 && j !== 0){ //header with column numbers
					$cell = $("<th>").append(j);
				} else if (j === 0){ //header with row letter
					$cell = $("<th>").append(this._plateModel.getAlphabetLabel(i-1));
				} else {
					$cell = $("<td>").append("&nbsp;");
					$cell.addClass('well');
					if(withWells) {
						var well = this._plateModel.getWell(i-1,j);
						if(well) {
							//Color Annotations only enabled for wells with a type having COLOR_ENCODED_ANNOTATION property
							var isAnWell = jQuery.inArray("COLOR_ENCODED_ANNOTATION", profile.getAllPropertiCodesForTypeCode(well.sampleTypeCode) ) !== -1;
							var colorEncodedWellAnnotationsSelector = "";
							
							if(isAnWell) {
								$cell.attr("id", "well-" + well.permId);
								var selectedColorEncodedAnnotation = well.properties["COLOR_ENCODED_ANNOTATION"];
								if(selectedColorEncodedAnnotation && selectedColorEncodedAnnotation !== "DEFAULT") {
									$cell.css( { "background-color" : this._getColorForCode(selectedColorEncodedAnnotation) } );
								}
								colorEncodedWellAnnotationsSelector = this.getWellGroups(well, selectedColorEncodedAnnotation);
							}
							
							var tooltip = PrintUtil.getTable(well, false, null, 'inspectorWhiteFont',
															'colorEncodedWellAnnotations-holder-' + well.permId,
															colorEncodedWellAnnotationsSelector);
							
							$cell.tooltipster({
								content: $(tooltip),
								interactive: true
							});
						}
					}
				}
				
				$cell.css('width', Math.floor(80/this._plateModel.numColumns+1) +'%');
				$row.append($cell);
			}
			gridTable.append($row);
		}	
		return gridTable;
	}
	
	this._getColorForCode = function(code) {
		var valueInfo = profile.getVocabularyTermByCodes("COLOR_ENCODED_ANNOTATIONS", code);
		if(valueInfo) {
			return valueInfo.description.split(":")[0].trim();
		}
		return null;
	}
	
	this.getWellGroups = function(well, selectedAnnotation) {
		var $component = $("<select>", { "id" : 'colorEncodedWellAnnotations-' + well.permId, class : 'form-control', 'permId' : well.permId, "identifier" : well.identifier });
		if(this._plateModel.isDisabled) {
			$component.attr('disabled', '');
		}
		
		var $defaultOption = $("<option>").attr('value', '').attr('disabled', '').text('Select an annotation');
		if(!selectedAnnotation) {
			$defaultOption.attr('selected', '');
		}
		$component.append($defaultOption);
			
		var colorEncodedAnnotations = profile.getVocabularyByCode("COLOR_ENCODED_ANNOTATIONS");
		for(var cIdx = 0; cIdx < colorEncodedAnnotations.terms.length; cIdx++) {
			var $option = $("<option>").attr('value', colorEncodedAnnotations.terms[cIdx].code).text(colorEncodedAnnotations.terms[cIdx].label);
			if(selectedAnnotation === colorEncodedAnnotations.terms[cIdx].code) {
				$option.attr('selected', '');
			}
			$component.append($option);
		}
		
		var _this = this;
		$component.change(function(event) {
			var $componentChange = $(this);
			var value = $componentChange.val();
			var permId = $componentChange.attr("permId");
			var identifier = $componentChange.attr("identifier");
			if(value === "DEFAULT") {
				$("#well-"+permId).css( { "background-color" : "" } );
			} else {
				$("#well-"+permId).css( { "background-color" : _this._getColorForCode(value) } );
			}
			_this._plateModel.changesToDo.push({ "permId" : permId, "identifier" : identifier, "properties" : {"COLOR_ENCODED_ANNOTATION" : value } });
			if(mainController.currentView.setDirty) {
				mainController.currentView.setDirty();
			}
		});
		var $componentWithLabel = $("<span>").append("Color Encoded Annotation: ").append($component);
		return $componentWithLabel;
	}
}