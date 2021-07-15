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
            var postFixes = profile.getSpaceEndingsForInventory();
            var postFix = this.getMatchIngPostfix(this._spaceFormModel.space, postFixes);
            if (this._spaceFormModel.isInventory && postFix === null) {
                Util.showError("Invalid inventory space code: The code has to end with one of the following post fixes: "
                        + postFixes.join(", "));
                return;
            }
            if (!this._spaceFormModel.isInventory && postFix !== null) {
                Util.showError("Invalid space code: The code shouldn't end with " + postFix);
                return;
            }
            
            require(["as/dto/space/create/SpaceCreation"], function(SpaceCreation) {
                var spaceCreation = new SpaceCreation();    
                spaceCreation.setCode(_this._spaceFormModel.space);
                if (_this._spaceFormModel.description) {
                    spaceCreation.setDescription(_this._spaceFormModel.description);
                }
                _this._mainController.openbisV3.createSpaces([spaceCreation])
                    .done(function(permIds) {
                        Util.showSuccess("Space created", function() {
                            _this._mainController.changeView("showSpacePage", permIds[0].getPermId());
                            Util.unblockUI();
                        });
                        _this._mainController.sideMenu.refreshCurrentNode();
                    })
                    .fail(function(error) {
                        Util.showFailedServerCallError(error);
                        Util.unblockUI();
                    });
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
        var _this = this;
        require(["as/dto/space/delete/SpaceDeletionOptions" ], function(SpaceDeletionOptions) {
            var options = new SpaceDeletionOptions();
            options.setReason(reason);
            var spaceId = _this._spaceFormModel.v3_space.getPermId()
            _this._mainController.openbisV3.deleteSpaces([spaceId], options)
                .done(function(deletionId) {
                    Util.showSuccess("Space Deleted");
                    mainController.sideMenu.deleteNodeByEntityPermId(spaceId.getPermId(), true);
                })
                .fail(function(error) {
                    Util.showFailedServerCallError(error);
                    Util.unblockUI();
                });
        });
    }

    this.getMatchIngPostfix = function(code, postfixes) {
        for(var iIdx = 0; iIdx < postfixes.length; iIdx++) {
            if(code.endsWith(postfixes[iIdx])) {
                return postfixes[iIdx];
            }
        }
        return null;
    }

}