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

function DataGridController(title, columns, data, rowClickEventHandler, showAllColumns) {
	this._grid = new Grid(columns, data, showAllColumns);
	this._grid.addRowClickListener(rowClickEventHandler);
	this._dataGridModel = new DataGridModel(title, columns, data, rowClickEventHandler, this._grid.render());
	this._dataGridView = new DataGridView(this, this._dataGridModel);
	
	
	this.init = function($container) {
		this._dataGridView.repaint($container);
	}
	
	this.refresh = function() {
		this._dataGridModel.datagrid.repeater('render');
	}
}