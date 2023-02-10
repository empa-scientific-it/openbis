/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function JupyterNotebookView(jupyterNotebookController, jupyterNotebookModel) {
	this._jupyterNotebookController = jupyterNotebookController;
	this._jupyterNotebookModel = jupyterNotebookModel;
	
	this.repaint = function() {
		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		$window.append($('<legend>').append("Create Jupyter Notebook"));
		var $btns = $('<div>', {'id' : 'jnb_buttons'});
		$window.append($btns);
		var $btnOpen = $('<div>', { 'class' : 'btn btn-default', 'text' : 'Create & connect', 'id' : 'open_jnb' });
		$btnOpen.click(["open", $window, this], this._handle);
		var $btnSave = $('<div>', { 'class' : 'btn btn-default', 'text' : 'Create & download', 'id' : 'save_jnb' });
		$btnSave.click(["save", $window, this], this._handle);
		var $cancelButton = $('<div>', {'class' : 'btn btn-default', 'text' : 'Cancel', 'id' : 'cancel'});
		$cancelButton.click(function() {
			Util.unblockUI();
		});
		$btns.append($btnOpen).append('&nbsp;').append($btnSave).append('&nbsp;').append($cancelButton);
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '50%',
				'left' : '25%',
				'right' : '25%',
				'max-height' : '70%',
				'overflow' : 'auto'
		};
		Util.blockUI($window, css);
	};
	
	this._handle = function(event) {
		var actionType = event.data[0];
		var $window = event.data[1];
		var _this = event.data[2];
		var entity = _this._jupyterNotebookModel.entity;
		$("#jnb_buttons").hide();
		var $datasetsContainer = $("<div>", { style : "width: 100%;" });
		$window.append(FormUtil.getInfoText("Please enter the names/codes of the datasets you want to download, or the names/codes of the experiments/objects which contain those datasets. "));
		$window.append(FormUtil.getFieldForComponentWithLabel($datasetsContainer, "Datasets"));
		var datasetsSearchDropdown = new AdvancedEntitySearchDropdown(true, false, "Select as many datasets as you need",
				false, false, true, false, false);
		datasetsSearchDropdown.init($datasetsContainer);
		
		if(entity) {
			switch(entity["@type"]) {
				case "DataSet":
					datasetsSearchDropdown.addSelectedDataSets([entity.code]);
					break;
				case "Sample":
					if(mainController.currentView._sampleFormModel.datasets) {
						var datasetCodes = [];
						for(var eIdx = 0; eIdx < mainController.currentView._sampleFormModel.datasets.length; eIdx++) {
							datasetCodes.push(mainController.currentView._sampleFormModel.datasets[eIdx].code);
						}
						datasetsSearchDropdown.addSelectedDataSets(datasetCodes);
					}
					break;
				case "Experiment":
					if(mainController.currentView._experimentFormModel.dataSetViewer && 
							mainController.currentView._experimentFormModel.dataSetViewer._datasetViewerModel.entityDataSets) {
						var datasetCodes = [];
						for(datasetCode in mainController.currentView._experimentFormModel.dataSetViewer._datasetViewerModel.entityDataSets) {
							datasetCodes.push(datasetCode);
						}
						datasetsSearchDropdown.addSelectedDataSets(datasetCodes);
					}
					break;
			}
		}
		
		
		var $ownerContainer = $("<div>", { style : "width: 100%;" });
		$window.append("</br>");
		$window.append(FormUtil.getInfoText("Please enter the name/code of the experiment/object where you want to save the Jupiter notebook."));
		$window.append(FormUtil.getFieldForComponentWithLabel($ownerContainer, "Owner (*)"));
		var ownerSearchDropdown = new AdvancedEntitySearchDropdown(false, true, "Select one owner " + ELNDictionary.sample,
				true, true, false, false, false);
		ownerSearchDropdown.init($ownerContainer);
		
		if(entity) {
			switch(entity["@type"]) {
				case "DataSet":
					if(entity.sampleIdentifierOrNull) {
						ownerSearchDropdown.addSelectedSample(entity.sampleIdentifierOrNull);
					} else if(entity.experimentIdentifier) {
						ownerSearchDropdown.addSelectedExperiment(entity.experimentIdentifier);
					}
					break;
				case "Sample":
					ownerSearchDropdown.addSelectedSample(entity.identifier);
					break;
				case "Experiment":
					ownerSearchDropdown.addSelectedExperiment(entity.identifier);
					break;
			}
		}
		
		var buttonLabel = "Accept & Download";
		if (actionType === 'open') {
			var $workspace = FormUtil._getInputField('text', null, 'directory Name', null, true);
			$window.append(FormUtil.getFieldForComponentWithLabel($workspace, "Directory Name"));
			buttonLabel = "Accept & Open";
		}
		var $notebookName = FormUtil._getInputField('text', null, 'notebook Name', null, true);
		$window.append(FormUtil.getFieldForComponentWithLabel($notebookName, "Notebook Name"));
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : buttonLabel });
		$window.submit(function() {
			var selectedDatasets = datasetsSearchDropdown.getSelected();
			var selectedOwner = ownerSearchDropdown.getSelected();
			var notebookOwner = selectedOwner[0];
			if (actionType === 'open') {
				_this._jupyterNotebookController.create($workspace.val(), $notebookName.val(), selectedDatasets, notebookOwner);
			} else {
				_this._jupyterNotebookController.createAndSave($notebookName.val() + ".ipynb", selectedDatasets, notebookOwner);
			}
		});
		var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
		$btnCancel.click(function() {
			Util.unblockUI();
		});
		$window.append($btnAccept).append('&nbsp;').append($btnCancel);
	}
}