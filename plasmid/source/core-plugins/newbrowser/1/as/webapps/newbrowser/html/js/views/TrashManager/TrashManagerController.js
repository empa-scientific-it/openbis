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

function TrashManagerController(mainController) {
	this._mainController = mainController;
	this._trashManagerModel = new TrashManagerModel();
	this._trashManagerView = new TrashManagerView(this, this._trashManagerModel);
	
	this.init = function($container) {
		var _this = this;
		mainController.serverFacade.listDeletions(function(data) {
			if(data.result && data.result.length > 0) {
				//Fill Model
			}
			_this._trashManagerView.repaint($container);
		});
	}
}