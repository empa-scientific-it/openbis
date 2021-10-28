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
	
	this.revertDeletions = function(deletionIds) {
		mainController.serverFacade.revertDeletions(deletionIds, function(data) {
		    if(data.error) {
                Util.showError(data.error.message, null, true, true, false, true);
            } else {
                Util.showSuccess("Deletions Reverted.", function() {});
                mainController.changeView('showTrashcanPage', null);
            }
		});
	}
	
    this.deletePermanently = function(deletionIds, forceDeletionOfDependentDeletions) {
        mainController.serverFacade.deletePermanently(deletionIds, forceDeletionOfDependentDeletions, function(data) {
            if(data.error) {
                Util.showError(data.error.message, null, true, true, false, true);
            } else {
                Util.showSuccess("Permanently Deleted.");
                mainController.changeView('showTrashcanPage', null);
            }
        });
    }
	
	this.emptyTrash = function() {
		var deleteIds = [];
		
		for(var delIdx = 0; delIdx < this._trashManagerModel.deletions.length; delIdx++) {
			var deletion = this._trashManagerModel.deletions[delIdx];
			deleteIds.push(deletion.id);
		}
		
        mainController.serverFacade.deletePermanently(deleteIds, true, function(data) {
            if(data.error) {
                Util.showError(data.error.message, null, true, true, false, true);
            } else {
                Util.showSuccess("Trashcan cleaned.");
                mainController.changeView('showTrashcanPage', null);
            }
		});
	}
	
	this.init = function(views) {
		var _this = this;
		mainController.serverFacade.listDeletions(function(data) {
			if(data.result && data.result.length > 0) {
				_this._trashManagerModel.deletions = data.result;
			}
			_this._trashManagerView.repaint(views);
		});
	}
}