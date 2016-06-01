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

function SpaceFormView(spaceFormController, spaceFormModel) {
	this._spaceFormController = spaceFormController;
	this._spaceFormModel = spaceFormModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "form-horizontal row"});
		var $formColumn = $("<div>", { "class" : FormUtil.formColumClass });
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append("Space " + this._spaceFormModel.space.code);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var $createProj = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
			_this._spaceFormController.createProject();
		});
		
		var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
			Util.blockUI();
			var facade = mainController.serverFacade;
			facade.exportAll({ type: "SPACE", permId : _this._spaceFormModel.space.code }, facade.getUserId(), function(error, result) {
				Util.showSuccess("Export is being processed, you will receibe an email when is ready.", function() { Util.unblockUI(); });
			});
		});
		
		toolbarModel.push({ component : $createProj, tooltip: "Create Project" });
		toolbarModel.push({ component : $export, tooltip: "Export" });
		
		$formColumn.append($formTitle);
		$formColumn.append(FormUtil.getToolbar(toolbarModel));
		$formColumn.append("<br>");
		
		$container.append($form);
	}
}