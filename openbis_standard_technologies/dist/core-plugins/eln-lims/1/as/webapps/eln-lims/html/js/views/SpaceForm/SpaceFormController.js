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

function SpaceFormController(mainController, mode, isInventory, space) {
	this._mainController = mainController;
	this._spaceFormModel = new SpaceFormModel(mode, isInventory, space);
	this._spaceFormView = new SpaceFormView(this, this._spaceFormModel);

	this.init = function(views) {
		var _this = this;
        if (space) {
            require([ "as/dto/space/id/SpacePermId", "as/dto/space/fetchoptions/SpaceFetchOptions", 
                "as/dto/project/id/ProjectIdentifier", "as/dto/rights/fetchoptions/RightsFetchOptions" ],
            function(SpacePermId, SpaceFetchOptions, ProjectIdentifier, RightsFetchOptions) {
                var id = new SpacePermId(space);
                var fetchOptions= new SpaceFetchOptions();
                fetchOptions.withRegistrator();
                mainController.openbisV3.getSpaces([ id ], fetchOptions).done(function(map) {
                    _this._spaceFormModel.v3_space = map[id];
                    var space = _this._spaceFormModel.space;
                    _this._mainController.getUserRole({
                        space: space
                    }, function(roles){
                        _this._spaceFormModel.roles = roles;
                        var dummyId = new ProjectIdentifier("/" + space + "/__DUMMY_FOR_RIGHTS_CALCULATION__");
                        mainController.openbisV3.getRights([id, dummyId], new RightsFetchOptions()).done(function(rightsByIds) {
                            _this._spaceFormModel.projectRights = rightsByIds[dummyId];
                            _this._spaceFormView.repaint(views);
                        });
                    });
                });
            });
        } else {
            _this._spaceFormView.repaint(views);
        }
	}

    this.createProject = function() {
        this._mainController.changeView('showCreateProjectPage', this._spaceFormModel.space);
    }

    this.enableEditing = function() {
        this._mainController.changeView('showEditSpacePage', this._spaceFormModel.space);
    }

    this.updateSpace = function() {
        Util.blockUI();
        var _this = this;
        if (this._spaceFormModel.mode === FormMode.CREATE) {
            if (!this._spaceFormModel.space) {
                Util.showError("Code Missing.");
                return;
            }
            var groupPrefix = this._spaceFormModel.prefix;
            var isInventorySpace = this._spaceFormModel.isInventory;
            var isReadOnly = this._spaceFormModel.isReadOnly;
            this._mainController.serverFacade.registerSpace(groupPrefix, this._spaceFormModel.postfix,
                    isInventorySpace, isReadOnly, this._spaceFormModel.description, 
                function(result) {
                    Util.showSuccess("Space created", function() {
                        _this._mainController.changeView("showSpacePage", result.spaceIds[0].getPermId());
                        if (result.reloadNeeded) {
                            Util.reloadApplication("Application will be reloaded because the settings have changed.");
                         }
                        Util.unblockUI();
                    });
                    _this._mainController.sideMenu.refreshCurrentNode();
                });
        } else { // update
            require(["as/dto/space/update/SpaceUpdate"], function(SpaceUpdate) {
                var spaceUpdate = new SpaceUpdate();
                spaceUpdate.setSpaceId(_this._spaceFormModel.v3_space.getPermId());
                if (_this._spaceFormModel.description) {
                    spaceUpdate.setDescription(_this._spaceFormModel.description);
                }
                _this._mainController.openbisV3.updateSpaces([spaceUpdate])
                    .done(function() {
                        Util.showSuccess("Space updated", function() {
                            _this._mainController.changeView("showSpacePage", _this._spaceFormModel.v3_space.getCode());
                            Util.unblockUI();
                        });
                        _this._mainController.sideMenu.refreshCurrentNode();
                    })
                    .fail(function(error) {
                        Util.showFailedServerCallError(error);
                        Util.unblockUI();
                    });
            });
        }
    }
    
    this.deleteSpace = function(reason) {
        var spaceCode = this._spaceFormModel.space;
        this._mainController.serverFacade.deleteSpace(spaceCode, reason, function(updated) {
            Util.showSuccess("Space Deleted");
            mainController.sideMenu.deleteNodeByEntityPermId(spaceCode, true);
            if (updated) {
                Util.reloadApplication("Application will be reloaded because the settings have changed.");
            }
        });
    }

    this.getAllGroupPrefixes = function() {
        var config = this._mainController.profile.userManagementMaintenanceTaskConfig;
        if (config) {
            return JSON.parse(config).groups.map(def => def.key.toUpperCase());
        }
        return [];
    }

    this.setPrefix = function(prefix) {
        this._spaceFormModel.prefix = prefix;
        this._createCode();
    }

    this.setPostfix = function(postfix) {
        this._spaceFormModel.postfix = postfix;
        this._createCode();
    }
    
    this._createCode = function() {
        var prefix = this._spaceFormModel.prefix;
        if (prefix.length > 0) {
            prefix += "_";
        }
        var postfix = this._spaceFormModel.postfix;
        if (postfix) {
            this._spaceFormModel.space = prefix + postfix;
            this._spaceFormModel.isFormDirty = true;
        }
    }
}