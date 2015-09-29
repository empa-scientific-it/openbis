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

function MoveSampleView(moveSampleController, moveSampleModel) {
	this._moveSampleController = moveSampleController;
	this._moveSampleModel = moveSampleModel;
	var $experimentSection = $("<div>");
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', { 'class' : 'form-horizontal', 'action' : 'javascript:void(0);' });
		$window.submit(function() {
			Util.unblockUI();
			_this._moveSampleController.move();
		});
		
		$window.append($('<legend>').append("Move Sample"));
		$window.append(FormUtil.getFieldForLabelWithText("Type", this._moveSampleModel.sample.sampleTypeCode));
		$window.append(FormUtil.getFieldForLabelWithText("Identifier", this._moveSampleModel.sample.identifier));
		$window.append(FormUtil.getFieldForLabelWithText("Current Experiment", this._moveSampleModel.sample.experimentIdentifierOrNull));
		$window.append(FormUtil.getFieldForComponentWithLabel(FormUtil.getOptionsRadioButtons("oldOrNewExp",true, ["Existing Experiment", "New Experiment"], function(event) {
			var value = $(event.target).val();
			if(value === "Existing Experiment") {
				_this._moveSampleModel.isNewExperiment = false;
				_this.repaintExistingExperiment();
			} else {
				_this._moveSampleModel.isNewExperiment = true;
				_this.repaintNewExperiment();
			}
		}), ""));
		
		$window.append($experimentSection);
		this.repaintExistingExperiment();
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });		
		var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
		$btnCancel.click(function() {
			Util.unblockUI();
		});
		
		$window.append($btnAccept).append('&nbsp;').append($btnCancel);
		
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '70%',
				'left' : '15%',
				'right' : '20%',
				'overflow' : 'hidden'
		};
		
		Util.blockUI($window, css);
	}
	
	this.repaintNewExperiment = function() {
		var _this = this;
		$experimentSection.empty();
		FormUtil.getProjectAndExperimentsDropdown(true, false, true, function($dropdown) {
			//Fields
			var $expTypeField = FormUtil.getExperimentTypeDropdown(null, true);
			var $expNameField = FormUtil._getInputField('text', null, 'Future Experiment Name', null, true);
			
			//Events
			var newExpEvent = function(event){
				var valueProject = $dropdown.val();
				var valueExperiment = $expNameField.val();
				_this._moveSampleModel.experimentIdentifier = valueProject + "/" + valueExperiment;
			};
			var newTypeEVent = function(event) {
				var value = $(event.target).val();
				_this._moveSampleModel.experimentType = value;
			};
			
			//Attach Events
			$dropdown.change(newExpEvent);
			$expNameField.keyup(newExpEvent);
			$expTypeField.change(newTypeEVent);
			
			//Attach Fields
			$experimentSection.append(FormUtil.getFieldForComponentWithLabel($dropdown, "Future Project"))
							.append(FormUtil.getFieldForComponentWithLabel($expTypeField, "Future Experiment Type"))
							.append(FormUtil.getFieldForComponentWithLabel($expNameField, "Future Experiment Name"));
		});
		
		
		
	}
	
	this.repaintExistingExperiment = function() {
		var _this = this;
		$experimentSection.empty();
		FormUtil.getProjectAndExperimentsDropdown(false, true, true, function($dropdown) {
			//Events
			$dropdown.change(function(event){
				var value = $(event.target).val();
				_this._moveSampleModel.experimentIdentifier = value;
			});
			//Attach Fields
			$experimentSection.append(FormUtil.getFieldForComponentWithLabel($dropdown, "Future Experiment"));
		});
	}
	
}