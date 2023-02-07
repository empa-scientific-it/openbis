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

function ExportTreeView(exportTreeController, exportTreeModel) {
	var exportTreeModel = exportTreeModel;
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		
		var $form = $("<div>");
		
		var $formColumn = $("<form>", {
			'name': 'exportTreeForm',
			'role' : "form",
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.exportSelected();'
		});
			
		$form.append($formColumn);
		
        var $formTitle = $("<h2>").append("Export Builder");
        $header.append($formTitle);

        var $exportButton = $("<input>", { "type": "button", "class": "btn btn-primary", 'value': 'Export Selected',
                'onClick': '$("form[name=\'exportTreeForm\']").submit()' });
        $header.append($exportButton);

		var $infoBox = FormUtil.getInfoBox("You can select any parts of the accessible openBIS structure to export:", [
		                                   "If you select a tree node and do not expand it, everything below this node will be exported by default.",
		                                   "To export selectively only parts of a tree, open the nodes and select what to export."
		]);
		$infoBox.css("border", "none");
		$container.append($infoBox);
		
		var $tree = $("<div>", { "id" : "exportsTree" });
		$formColumn.append($("<br>"));
		$formColumn.append(FormUtil.getBox().append($tree));
		
		$container.append($form);
		
    	exportTreeModel.tree = TreeUtil.getCompleteTree($tree);
	}
}