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

function DataSetFormView(dataSetFormController, dataSetFormModel) {
	var dataSetFormController = dataSetFormController;
	var dataSetFormModel = dataSetFormModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		//Clean and prepare container
		var $wrapper = $('<form>', { class : 'form-horizontal ', 'id' : 'mainDataSetForm', 'role' : 'form'});
		$wrapper.submit(function(event) {
			event.preventDefault();
			dataSetFormController.submit(); 
		});
		
		// Title
		$wrapper.append($("<h1>").text('Create DataSet'));
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<div>');
		$dataSetTypeFieldSet.append($('<legend>').text('Identification Info'));
		$wrapper.append($dataSetTypeFieldSet);
		
		var $dataSetTypeSelector = FormUtil.getDataSetsDropDown('DATASET_TYPE', dataSetFormModel.dataSetTypes);
		$dataSetTypeSelector.change(function() { 
			repaintMetadata(
					dataSetFormModel.getDataSetType($('#DATASET_TYPE').val())
			);
		});
		
		var $dataSetTypeDropDown = $('<div>', { class : 'form-group'});
		$dataSetTypeDropDown.append($('<label>', {class: "control-label " + FormUtil.labelColumnClass}).html('Data Set Type&nbsp;(*):'));
		$dataSetTypeDropDown.append(
			$('<div>', {class: FormUtil.controlColumnClass}).append($dataSetTypeSelector)
		);
		$dataSetTypeFieldSet.append($dataSetTypeDropDown);
		
		var owner = dataSetFormModel.sampleOrExperiment.identifier;
		var ownerLabel = dataSetFormModel.sampleOrExperiment.isSample ? entityTypes.sample : entityTypes.experiment;
		$dataSetTypeFieldSet.append(FormUtil.getFieldForTextWithLabel(owner, ownerLabel));
		
		//
		// Registration and modification info
		//
		
		//Metadata Container
		$wrapper.append($('<div>', { 'id' : 'metadataContainer'}));
		
		//Attach File
		$wrapper.append($('<div>', { 'id' : 'APIUploader' } ));
		
		$wrapper.append($('<div>', { 'id' : 'fileOptionsContainer' } ));
		
		//Show Files
		var filesViewer = $('<div>', { 'id' : 'filesViewer' } );
		$wrapper.append(filesViewer);
		
		//Submit Button
		var $submitButton = $('<fieldset>')
		.append($('<div>', { class : "form-group"}))
		.append($('<div>', {class: FormUtil.controlColumnClass}).append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : "Create DataSet"})));
		$wrapper.append($submitButton);
		
		//Attach to main form
		$container.append($('<div>', { class : 'row'}).append($('<div>', { class : FormUtil.formColumClass}).append($wrapper)));
		
		//Initialize file chooser
		var onComplete = function(data) {
			dataSetFormModel.files.push(data.name);
			updateFileOptions();
		}
		
		var onDelete = function(data) {
			for(var i=0; dataSetFormModel.files.length; i++) {
				if(dataSetFormModel.files[i] === data.name) {
					dataSetFormModel.files.splice(i, 1);
					break;
				}
			}
			updateFileOptions();
		}
		
		openBIS.createSessionWorkspaceUploader($("#APIUploader"), onComplete, {
			main_title : $('<legend>').text('Files Uploader'),
			uploads_title : $('<legend>').text('File list'),
			ondelete:onDelete
		});
	}
	
	var updateFileOptions = function() {
		var _this = this;
		$wrapper = $("#fileOptionsContainer"); //Clean existing
		$wrapper.empty();
		
		if( dataSetFormModel.files.length > 1
			||
			dataSetFormModel.files.length === 1 && dataSetFormModel.files[0].indexOf('zip', dataSetFormModel.files[0].length - 3) !== -1) {
			var $legend = $('<div>').append($('<legend>').text('Files Options'));
			$wrapper.append($legend);
		}
		
		if(dataSetFormModel.files.length > 1) {
			var $textField = FormUtil._getInputField('text', 'folderName', 'Folder Name', null, true);
			var $folderName = $('<div>')
			.append($('<div>', { class : "form-group"})
					.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).html('Folder Name&nbsp;(*):'))
					.append($('<div>', {class: FormUtil.controlColumnClass})
						.append($textField))
			);
			$wrapper.append($folderName);
		}
		
		if(dataSetFormModel.files.length === 1 && 
			dataSetFormModel.files[0].indexOf('zip', dataSetFormModel.files[0].length - 3) !== -1) {
			var $fileFieldSetIsDirectory = $('<div>')
			.append($('<div>', { class : "form-group"})
						.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).text('Uncompress before import:'))
						.append($('<div>', {class: FormUtil.controlColumnClass})
							.append(FormUtil.getFieldForPropertyType({
								dataType : "BOOLEAN",
								code : 'isZipDirectoryUpload',
								description : 'Uncompress before import:'
							})))
			);
			
			$wrapper.append($fileFieldSetIsDirectory);
			
			$("#isZipDirectoryUpload").change(function() {
				if($("#isZipDirectoryUpload"+":checked").val() === "on") {
					var $textField = FormUtil._getInputField('text', 'folderName', 'Folder Name', null, true);
					var $folderName = $('<div>', { "id" : "folderNameContainer"})
					.append($('<div>', { class : "form-group"})
							.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).html('Folder Name&nbsp;(*):'))
							.append($('<div>', {class: FormUtil.controlColumnClass})
								.append($textField))
					);
					$("#fileOptionsContainer").append($folderName);
					$("#folderName").val(dataSetFormModel.files[0].substring(0, dataSetFormModel.files[0].indexOf(".")));
				} else {
					$( "#folderNameContainer" ).remove();
				}
			})
		}
	}
	
	var repaintMetadata = function(dataSetType) {
		var _this = this;
		$("#metadataContainer").empty();
		var $wrapper = $("<div>");
		
		for(var i = 0; i < dataSetType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = dataSetType.propertyTypeGroups[i];
			
			var $fieldset = $('<div>');
			var $legend = $('<legend>'); 
			$fieldset.append($legend);
			
			if(propertyTypeGroup.name) {
				$legend.text(propertyTypeGroup.name);
			} else if(dataSetType.propertyTypeGroups.length === 1) { //Only when there is only one group without name to render it with a default title.
				$legend.text("Metadata Fields");
			} else {
				$legend.remove();
			}
			
			var propertyGroupPropertiesOnForm = 0;
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				//Update model
				var changeEvent = function(propertyType) {
					return function() {
						var propertyTypeCode = null;
						if(propertyType.code.charAt(0) === '$') { //isSystemProperty
							propertyTypeCode = propertyType.code.substr(1);
						} else {
							propertyTypeCode = propertyType.code;
						}
						var field = $(this);
						var value = null;
						if(propertyType.dataType === "BOOLEAN") {
							value = field.children()[0].checked;
						} else if (propertyType.dataType === "TIMESTAMP") {
							value = $($(field.children()[0]).children()[0]).val();
						} else {
							value = Util.getEmptyIfNull(field.val());
						}
						dataSetFormModel.dataSet.properties[propertyTypeCode] = value;
					}
				}
				
				var $component = FormUtil.getFieldForPropertyType(propertyType);
				
				//Avoid modifications in properties managed by scripts
				if(propertyType.managed || propertyType.dinamic) {
					$component.prop('disabled', true);
				}
				
				$component.change(changeEvent(propertyType));
				$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
				$fieldset.append(FormUtil.getFieldForComponentWithLabel($component, propertyType.label));
				propertyGroupPropertiesOnForm++;
			}
			
			if(propertyGroupPropertiesOnForm === 0) {
				$legend.remove();
			}
			
			$wrapper.append($fieldset);
		}
		
		$("#metadataContainer").append($wrapper);
	}
}