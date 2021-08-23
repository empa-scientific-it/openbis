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

/**
 * Creates an instance of SideMenuWidget.
 *
 * @constructor
 * @this {SideMenuWidgetController}
 * @param {MainController} mainController Used to control view changes.
 */
function SideMenuWidgetController(mainController) {
    this._mainController = mainController;
    this._sideMenuWidgetModel = new SideMenuWidgetModel();
    this._sideMenuWidgetView = new SideMenuWidgetView(this, this._sideMenuWidgetModel);
    
    this._SORT_FIELD_KEY = "side-menu-sort-field";


    //
    // External API for real time updates
    //
    
    this.getCurrentNodeId = function() {
    	return (this._sideMenuWidgetModel.selectedNodeData)?this._sideMenuWidgetModel.selectedNodeData.key:null;
    };
    
    this.deleteNodeByEntityPermId = function(key, isMoveToParent) {
    	var node = $(this._sideMenuWidgetModel.tree).fancytree('getTree').getNodeByKey(key);
        if (node) {
            if(isMoveToParent) {
                var parent = node.getParent();
                this._showNodeView(parent);
            }
            node.remove();
        }
    };
    
    this.refreshCurrentNode = function() {
    	this._refreshNode(this.getCurrentNodeId());
    }
    
    this.refreshNode = function(key) {
    		this._refreshNode(key);
    }
    
    this.refreshNodeParent = function(key) {
    	var node = $(this._sideMenuWidgetModel.tree).fancytree('getTree').getNodeByKey(key);
    	if(node) {
    		var parent = node.getParent();
    		if(parent) {
    			this._refreshNode(parent.key);
    		}
    	}
    }
    
    this.moveToNodeId = function(uniqueId) {
    	// Can't be implemented initially, it will be required to keep the whole menu path to know all parents that need to be loaded 
    }
    
    //
    // Init method that builds the menu object hierarchy
    //
    this.init = function($container, initCallback) {
        var _this = this;

        this._mainController.serverFacade.getSetting(this._SORT_FIELD_KEY, function(sortField) {
            _this._sideMenuWidgetModel.sortField = sortField;
            _this._sideMenuWidgetModel.$container = $container;
            
            _this._sideMenuWidgetView.repaint($container);
            
            LayoutManager.addResizeEventHandler(_this.resizeSideMenuBody());
            
            initCallback();    
        });
    }

    this.resizeElement = function($elementBody, percentageOfUsage) {
        var $elementHead = $("#sideMenuHeader");
        var sideMenuHeaderHeight = $elementHead.outerHeight();
        var $elementSortField = $("#sideMenuSortBar");
        var sideMenuSortFieldHeight = $elementSortField.outerHeight();
        var height = $( window ).height();
        var availableHeight = height - sideMenuHeaderHeight - sideMenuSortFieldHeight;
        $elementBody.css('height', (availableHeight * percentageOfUsage));
    }

    this.resizeSideMenuBody = function() {
        var _this = this;
        return function() {
            var $elementBody = $("#sideMenuBody");
            var percentageOfUsage = _this._sideMenuWidgetModel.percentageOfUsage;
            _this.resizeElement($elementBody, percentageOfUsage);
        }
    }

    this.addSubSideMenu = function(subSideMenu) {
        subSideMenu.css("margin-left", "3px");
        this._sideMenuWidgetModel.subSideMenu = subSideMenu;
        this._sideMenuWidgetModel.percentageOfUsage = 0.5;
        $("#sideMenuTopContainer").append(subSideMenu);
        this.resizeElement($("#sideMenuBody"), 0.5);
        this.resizeElement(subSideMenu, 0.5);
    }

    this.removeSubSideMenu = function() {
        if(this._sideMenuWidgetModel.subSideMenu) {
            this._sideMenuWidgetModel.subSideMenu.remove();
            this._sideMenuWidgetModel.percentageOfUsage = 1;
            this.resizeElement($("#sideMenuBody"), 1);
            this._sideMenuWidgetModel.subSideMenu = null;
        }
    }

    this._showNodeView = function(node) {
		if(node.data.view) {
			var viewData =  node.data.viewData;
			if(!viewData) {
				viewData = null;
			}
			mainController.changeView(node.data.view, viewData);
			this._sideMenuWidgetModel.selectedNodeData = {
				key : node.key,
				view : node.data.view,
				viewData : node.data.viewData
			};
		}
    }
    
    this._refreshNode = function(key) {
    	var node = $(this._sideMenuWidgetModel.tree).fancytree('getTree').getNodeByKey(key);
    	if(node) {
    		node.removeChildren();
        	node.resetLazy();
        	node.setExpanded(true);
    	}
    }

    //
    // service calls
    //

    this.setSortField = function(sortField) {
        this._sideMenuWidgetModel.sortField = sortField;
        this._mainController.serverFacade.setSetting(this._SORT_FIELD_KEY, sortField);
    }

}

function SideMenuWidgetComponent(isSelectable, isTitle, displayName, uniqueId, parent, newMenuIfSelected, newViewIfSelected, newViewIfSelectedData, contextTitle) {
    this.isSelectable = isSelectable;
    this.isTitle = isTitle;
    this.displayName = displayName;
    this.uniqueId = uniqueId;
    this.contextTitle = contextTitle;
    this.parent = parent;
    this.newMenuIfSelected = newMenuIfSelected;
    this.newViewIfSelected = newViewIfSelected;
    this.newViewIfSelectedData = newViewIfSelectedData;
}

var naturalSortSideMenuWidgetComponent = function(componentA, componentB){
  	return naturalSort(componentA.displayName, componentB.displayName);
}