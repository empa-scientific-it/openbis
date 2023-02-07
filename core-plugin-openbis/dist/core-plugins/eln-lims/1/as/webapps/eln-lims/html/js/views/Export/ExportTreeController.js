/*
 * Copyright 2016 ETH Zuerich, Scientific IT Services
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

function ExportTreeController(parentController) {
	var parentController = parentController;
	var exportTreeModel = new ExportTreeModel();
	var exportTreeView = new ExportTreeView(this, exportTreeModel);
	
	this.init = function(views) {
		exportTreeView.repaint(views);
	};
	
	this.exportSelected = function() {
		var selectedNodes = $(exportTreeModel.tree).fancytree('getTree').getSelectedNodes();
		
		var toExport = [];
		for(var eIdx = 0; eIdx < selectedNodes.length; eIdx++) {
			var node = selectedNodes[eIdx];
			toExport.push({ type: node.data.entityType, permId : node.key, expand : !node.expanded });
		}
		
		if(toExport.length === 0) {
			Util.showInfo("First select something to export.");
		} else {
			Util.blockUI();
			mainController.serverFacade.exportAll(toExport, true, false, function(error, result) {
				if(error) {
					Util.showError(error);
				} else {
					Util.showSuccess("Export is being processed, you will receive an email when it is finished. If you logout the process will stop.", function() { Util.unblockUI(); });
					mainController.refreshView();
				}
			});
		}
		
	}
}