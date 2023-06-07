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

function MoveSampleController(samplePermIdOrIds, successAction) {
	this._moveSampleModel = new MoveSampleModel(samplePermIdOrIds, successAction);
	this._moveSampleView = new MoveSampleView(this, this._moveSampleModel);
	
	this.init = function() {
		var _this = this;
		mainController.serverFacade.searchWithUniqueId(this._moveSampleModel.samplePermIds, function(data) {
			_this._moveSampleModel.samples = data;
			_this._moveSampleView.repaint();
		});
	}
	
	this.move = function() {
		var _this = this;
		if(!this._moveSampleModel.isNewExperiment && !this._moveSampleModel.experimentIdentifier) {
			Util.showUserError("Please choose an " + ELNDictionary.getExperimentDualName() + ".", function() {});
			return;
		}
		
		if(this._moveSampleModel.isNewExperiment && !this._moveSampleModel.experimentIdentifier) {
			Util.showUserError("Please choose the project and " + ELNDictionary.getExperimentDualName() + " name.", function() {});
			return;
		}
		
		if(this._moveSampleModel.isNewExperiment && !this._moveSampleModel.experimentType) {
			Util.showUserError("Please choose the " + ELNDictionary.getExperimentDualName() + " type.", function() {});
			return;
		}

        var experimentIdentifier = this._moveSampleModel.experimentIdentifier;
        var moveSampleFunction = function() {
            mainController.serverFacade.moveSample(
                _this._moveSampleModel.samples.map(s => s.permId), experimentIdentifier,
                function() {
                    mainController.serverFacade.getExperimentOrNull(experimentIdentifier, function(experiment){
                        var msg = ELNDictionary.Sample + " " + _this._moveSampleModel.samples[0].identifier;
                        if (_this._moveSampleModel.samples.length > 1) {
                            msg = _this._moveSampleModel.samples.length + " " + ELNDictionary.Samples; 
                        }
                        Util.showSuccess(msg + " moved to " + _this._moveSampleModel.experimentIdentifier, async function() {
                            Util.unblockUI()

                            //Refresh old experiment
                            await mainController.sideMenu.refreshNodeParentByPermId("SAMPLE", _this._moveSampleModel.samples[0].permId);

                            // Refresh new experiment
                            if(_this._moveSampleModel.isNewExperiment){
                                await mainController.sideMenu.refreshNodeByPermId("PROJECT", experiment.getProject().getPermId().getPermId());
                            } else {
                                await mainController.sideMenu.refreshNodeByPermId("EXPERIMENT", experiment.getPermId().getPermId());
                            }

                            if(_this._moveSampleModel.successAction){
                                _this._moveSampleModel.successAction();
                            }
                        });
                    })
                });
        }
        if (this._moveSampleModel.isNewExperiment) {
            var experimentType = this._moveSampleModel.experimentType;
            var projectIdentifier = IdentifierUtil.getProjectIdentifierFromExperimentIdentifier(experimentIdentifier);
            var code = IdentifierUtil.getCodeFromIdentifier(experimentIdentifier)
            mainController.serverFacade.createExperiment(experimentType, projectIdentifier, code, moveSampleFunction) 
        } else {
            moveSampleFunction();
        }
    }
}

