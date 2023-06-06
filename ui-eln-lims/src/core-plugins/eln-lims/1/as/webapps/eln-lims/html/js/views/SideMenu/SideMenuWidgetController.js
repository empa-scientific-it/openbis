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
    this._mainController = mainController
    this._sideMenuWidgetModel = new SideMenuWidgetModel()
    this._sideMenuWidgetView = new SideMenuWidgetView(this, this._sideMenuWidgetModel)
    this._browserController = new SideMenuWidgetBrowserController()

    //
    // External API for real time updates
    //

    this.getCurrentNodeId = function () {
        var nodeObject = this._browserController.getSelectedObject()

        if (nodeObject) {
            var nodeObjectStr = JSON.stringify(nodeObject)
            return nodeObjectStr
        } else {
            return null
        }
    }

    this.deleteNodeByEntityPermId = async function (entityType, entityPermId, isMoveToParent) {
        var _this = this

        var nodes = this._browserController.getNodes().filter(function (node) {
            if (node.object) {
                return entityType === node.object.type && entityPermId === node.object.id
            } else {
                return false
            }
        })

        for(var i = 0; i < nodes.length; i++){
            var node = nodes[i]
            await _this._browserController.reloadNode(node.parentId)
        }

        if (isMoveToParent) {
            var parentIds = nodes.map(function (node) {
                return node.parentId
            })

            while (parentIds.length > 0) {
                var parentId = parentIds.shift()
                if (!parentId) {
                    continue
                }

                var parent = _this._browserController.getNode(parentId)
                if (!parent) {
                    continue
                }

                if (parent.view && parent.viewData) {
                    mainController.changeView(parent.view, parent.viewData)
                    break
                } else {
                    parentIds.push(parent.parentId)
                }
            }
        }
    }

    this.refreshCurrentNode = async function () {
        var _this = this
        var selectedObject = this._browserController.getSelectedObject()
        if (selectedObject) {
            var nodes = this._browserController.getNodes()
            for(var i = 0; i < nodes.length; i++){
                var node = nodes[i]
                if (selectedObject.type === node.object.type && selectedObject.id === node.object.id) {
                    await _this._browserController.reloadNode(node.id)
                }
            }
        }
    }

    this.refreshNodeByPermId = async function (entityType, entityPermId) {
        var _this = this
        var nodes = this._browserController.getNodes()
        for(var i = 0; i < nodes.length; i++){
            var node = nodes[i]
            if (node.object && entityType === node.object.type && entityPermId === node.object.id) {
                await _this._browserController.reloadNode(node.id)
            }
        }
    }

    this.refreshNodeParentByPermId = async function (entityType, entityPermId) {
        var _this = this
        var nodes = this._browserController.getNodes()
        for(var i = 0; i < nodes.length; i++){
            var node = nodes[i]
            if (node.object && entityType === node.object.type && entityPermId === node.object.id) {
                await _this._browserController.reloadNode(node.parentId)
            }
        }
    }

    this.moveToNodeId = function (nodeObjectStr) {
        var nodeObject = null

        try {
            nodeObject = JSON.parse(nodeObjectStr)
        } catch (e) {
            // do nothing
        }

        return this._browserController.selectObject(nodeObject, { ignore: true })
    }

    //
    // Init method that builds the menu object hierarchy
    //
    this.init = function ($container, initCallback) {
        var _this = this

        this._mainController.serverFacade.getSetting(this._SORT_FIELD_KEY, function (sortField) {
            _this._sideMenuWidgetModel.sortField = sortField
            _this._sideMenuWidgetModel.$container = $container

            _this._sideMenuWidgetView.repaint($container)

            LayoutManager.addResizeEventHandler(_this.resizeSideMenuBody())

            initCallback()
        })
    }

    this.resizeElement = function ($elementBody, percentageOfUsage) {
        var $elementHead = $("#sideMenuHeader")
        var sideMenuHeaderHeight = $elementHead.outerHeight()
        var $elementSortField = $("#sideMenuSortBar")
        var sideMenuSortFieldHeight = $elementSortField.outerHeight()
        var height = $(window).height()
        var availableHeight = height - sideMenuHeaderHeight - sideMenuSortFieldHeight
        $elementBody.css("height", availableHeight * percentageOfUsage)
    }

    this.resizeSideMenuBody = function () {
        var _this = this
        return function () {
            var $elementBody = $("#sideMenuBody")
            var percentageOfUsage = _this._sideMenuWidgetModel.percentageOfUsage
            _this.resizeElement($elementBody, percentageOfUsage)
        }
    }

    this.addSubSideMenu = function (subSideMenu) {
        // Remove old from DOM if present
        var elementId = subSideMenu.attr("id")
        $("#" + elementId).remove()
        // Add new
        subSideMenu.css("margin-left", "3px")
        this._sideMenuWidgetModel.subSideMenu = subSideMenu
        this._sideMenuWidgetModel.percentageOfUsage = 0.5
        $("#sideMenuTopContainer").append(subSideMenu)
        this.resizeElement($("#sideMenuBody"), 0.5)
        this.resizeElement(subSideMenu, 0.5)
    }

    this.removeSubSideMenu = function () {
        if (this._sideMenuWidgetModel.subSideMenu) {
            this._sideMenuWidgetModel.subSideMenu.remove()
            this._sideMenuWidgetModel.percentageOfUsage = 1
            this.resizeElement($("#sideMenuBody"), 1)
            this._sideMenuWidgetModel.subSideMenu = null
        }
    }
}

function SideMenuWidgetComponent(
    isSelectable,
    isTitle,
    displayName,
    uniqueId,
    parent,
    newMenuIfSelected,
    newViewIfSelected,
    newViewIfSelectedData,
    contextTitle
) {
    this.isSelectable = isSelectable
    this.isTitle = isTitle
    this.displayName = displayName
    this.uniqueId = uniqueId
    this.contextTitle = contextTitle
    this.parent = parent
    this.newMenuIfSelected = newMenuIfSelected
    this.newViewIfSelected = newViewIfSelected
    this.newViewIfSelectedData = newViewIfSelectedData
}
