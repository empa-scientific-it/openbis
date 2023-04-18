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
	    Util.blockUI();
		mainController.serverFacade.revertDeletions(deletionIds, function() {
            Util.showSuccess("Deletions Reverted.", function() {
                Util.unblockUI();
                mainController.changeView('showTrashcanPage', null);
            });
		});
	}
	
    this.deletePermanently = function(deletionIds, forceDeletionOfDependentDeletions) {
        Util.blockUI();
        mainController.serverFacade.deletePermanently(deletionIds, forceDeletionOfDependentDeletions, function(data) {
            if(data.error) {
                Util.showError(data.error.message, null, true, true, false, true);
            } else {
                Util.showSuccess("Permanently Deleted.", function() {
                    Util.unblockUI();
                    mainController.changeView('showTrashcanPage', null);
                });
            }
        });
    }
	
	this.emptyTrash = function() {
	    Util.blockUI();
		var deleteIds = [];
		
		for(var delIdx = 0; delIdx < this._trashManagerModel.deletions.length; delIdx++) {
			var deletion = this._trashManagerModel.deletions[delIdx];
			deleteIds.push(deletion.id);
		}
		
        mainController.serverFacade.deletePermanently(deleteIds, true, function(data) {
            if(data.error) {
                Util.showError(data.error.message, null, true, true, false, true);
            } else {
                Util.showSuccess("Trashcan cleaned.", function() {
                    Util.unblockUI();
                    mainController.changeView('showTrashcanPage', null);
                });
            }
		});
	}
	
	this.init = function(views) {
		var _this = this;
		mainController.serverFacade.listDeletions(function(data) {
            _this._trashManagerModel.deletions = data;
			_this._trashManagerView.repaint(views);
		});
	}
}