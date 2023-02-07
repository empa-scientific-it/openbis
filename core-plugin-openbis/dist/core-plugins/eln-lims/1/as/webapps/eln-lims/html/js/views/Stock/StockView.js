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

function StockView(stockController, stockView) {
	var stockController = stockController;
	var stockView = stockView;
	
	this.repaint = function(views) {
		
		var $form = $("<div>");
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append(profile.MainMenuNodeNames.Stock);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		
		var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
			Util.blockUI();
			var facade = mainController.serverFacade;
			facade.listSpaces(function(spaces) {
	            var stockSpaces = [];
				for (var i = 0; i < spaces.length; i++) {
	                var space = spaces[i];
	                if(space === "STOCK_CATALOG" || space === "STOCK_ORDERS") {
	                	stockSpaces.push({ type: "SPACE", permId : space, expand : true });
	                }
	            }
	            
				facade.exportAll(stockSpaces, true, false, function(error, result) {
					if(error) {
						Util.showError(error);
					} else {
						Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
					}
				});
				
			});
		}, "Export");
		toolbarModel.push({ component : $export });
		
		views.header.append($formTitle);
		views.header.append(FormUtil.getToolbar(toolbarModel));
		views.content.append($formColumn);
	}
}