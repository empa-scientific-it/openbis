/* Copyright 2014 ETH Zuerich, Scientific IT Services
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

function ExperimentTableView(experimentTableController, experimentTableModel) {
	this._experimentTableController = experimentTableController;
	this._experimentTableModel = experimentTableModel;
	this._tableContainer = $("<div>");
	this.typeSelector = null;
	this._$container = null;
	
	this.repaint = function($container) {
		var _this = this;
		this._$container = $container;
		$container.empty();
		this._tableContainer.empty();
		if(this._experimentTableModel.title) {
			var $title = $("<h1>").append(this._experimentTableModel.title);
			$container.append($title);
		}
		
		$container.append(this._tableContainer);
	}
	
	this.getTableContainer = function() {
		return this._tableContainer;
	}
	
	//
	// Components
	//
	this.getTypeSelector = function() {
		return this.typeSelector;
	}
}