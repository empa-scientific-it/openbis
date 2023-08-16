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

function ExperimentTableController(parentController, title, project, showInProjectOverview, extraOptions) {
	this._parentController = parentController;
	this._experimentTableModel = new ExperimentTableModel(title, project, showInProjectOverview);
	this._experimentTableView = new ExperimentTableView(this, this._experimentTableModel);
	
	this.init = function($container) {
		var _this = this;
		Util.blockUI();
		this._experimentTableModel.reset();
		var callback = function(data) {
            for(var i = 0; i < data.experiments.length; i++) {
				var item = data.experiments[i];
				var showOnlyOverview = _this._experimentTableModel.showInProjectOverview && item.properties["$SHOW_IN_PROJECT_OVERVIEW"] === "true";
				var showAll = !_this._experimentTableModel.showInProjectOverview;
				if(showOnlyOverview || showAll) {
					_this._experimentTableModel.allExperiments.push(item);
				}
			}
			
			_this._experimentTableView.repaint($container);
			Util.unblockUI();
            _this._reloadTable();
		};
		
		if(this._experimentTableModel.project) {
			this._loadProjectData(callback);
		}
	}
	
	this._loadProjectData = function(callback) {
		var project = this._experimentTableModel.project;
		delete project["@id"];
		delete project["@type"];
		mainController.serverFacade.getProjectFromPermIdWithExperiments(project.permId, function(result) {
            callback(result[project.permId]);
        });
	}
	
	this._reloadTable = function() {
        var experiments = [];
        for (var idx = 0; idx < this._experimentTableModel.allExperiments.length; idx++) {
            var exptoCheckType = this._experimentTableModel.allExperiments[idx];
            var showOnlyOverview = this._experimentTableModel.showInProjectOverview && exptoCheckType.properties["$SHOW_IN_PROJECT_OVERVIEW"] === "true";
            var showAll = !this._experimentTableModel.showInProjectOverview;
            if(showOnlyOverview || showAll) {
                experiments.push(exptoCheckType);
            }
        }
        
        //Click event
        var rowClick = null;
        
        //Create and display table
        var multiselectable = extraOptions != null;
        this._dataGridController = ExperimentDataGridUtil.getExperimentDataGrid(experiments, rowClick, multiselectable, 50);
        this._dataGridController.init(this._experimentTableView.getTableContainer(), extraOptions);
	}
	
	this.refresh = function()
	{
		if (this._dataGridController) {
			this._dataGridController.refresh();
		}
	}
}