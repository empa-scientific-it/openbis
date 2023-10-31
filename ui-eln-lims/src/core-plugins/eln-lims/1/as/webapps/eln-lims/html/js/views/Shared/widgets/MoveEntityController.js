function MoveEntityController(entityType, entityPermId) {
	var moveEntityModel = new MoveEntityModel();
	var moveEntityView = new MoveEntityView(this, moveEntityModel);
	
	var searchAndCallback = function(callback) {
		var criteria = { entityKind : entityType, logicalOperator : "AND", rules : { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : entityPermId } } };
		
		switch(entityType) {
			case "EXPERIMENT":
				mainController.serverFacade.searchForExperimentsAdvanced(criteria, null, callback);
				break;
			case "SAMPLE":
				mainController.serverFacade.searchForSamplesAdvanced(criteria, null, callback);
				break;
			case "DATASET":
				mainController.serverFacade.searchForDataSetsAdvanced(criteria, null, callback);
				break;
			case "PROJECT":
				mainController.serverFacade.searchForProjectsAdvanced(criteria, null, callback);
				break;
		}
	};
	
	this.init = function() {
		searchAndCallback(function(result) {
			moveEntityModel.entity = result.objects[0];
			moveEntityView.repaint();
		});
	};
	
	var waitForIndexUpdate = function() {
		searchAndCallback(function(result) {
			var entity = result.objects[0];
			var found = false;
			switch(entityType) {
				case "EXPERIMENT":
					found = entity.getProject().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier;
					break;
				case "SAMPLE":
					found = entity.getExperiment().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier;
					break;
				case "DATASET":
					found = (entity.getSample() && entity.getSample().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier)
							||
							(entity.getExperiment() && entity.getExperiment().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier);
					break;
				case "PROJECT":
					found = entity.getSpace().getPermId().identifier === moveEntityModel.selected.getPermId().identifier;
					break;
			}
			
			if(!found) {
				setTimeout(function(){ waitForIndexUpdate(); }, 300);
			} else {
                Util.showSuccess("Moved successfully", async function() {
                    Util.unblockUI();

                    await mainController.sideMenu.refreshNodeParentByPermId(entityType, entity.getPermId().permId); // Refresh old node parent

                    var selectedType = moveEntityModel.selected["@type"]
                    var selectedEntityType = null

                    if(selectedType === "as.dto.space.Space"){
                        selectedEntityType = "SPACE"
                    }else if(selectedType === "as.dto.project.Project"){
                        selectedEntityType = "PROJECT"
                    }else if(selectedType === "as.dto.experiment.Experiment"){
                        selectedEntityType = "EXPERIMENT"
                    }else if(selectedType === "as.dto.sample.Sample"){
                        selectedEntityType = "SAMPLE"
                    }else if(selectedType === "as.dto.dataset.DataSet"){
                        selectedEntityType = "DATASET"
                    }

                    await mainController.sideMenu.refreshNodeByPermId(selectedEntityType, moveEntityModel.selected.getPermId().permId); // New node parent

                    switch(entityType) {
                        case "EXPERIMENT":
                            mainController.changeView("showExperimentPageFromIdentifier",
                                    encodeURIComponent('["' + entity.getIdentifier().identifier + '",false]'));
                            break;
                        case "SAMPLE":
                            mainController.changeView("showViewSamplePageFromPermId", entity.getPermId().permId);
                            break;
                        case "DATASET":
                            mainController.changeView("showViewDataSetPageFromPermId", entity.getPermId().permId);
                            break;
                        case "PROJECT":
                            mainController.changeView("showProjectPageFromIdentifier", entity.getPermId().permId);
                            break;
                    }
				});
			}
		});
	}
	
	this.move = function(descendants) {
	    var _this = this;
		Util.blockUI();
		
		var done = function() {
			waitForIndexUpdate();
		};
		var fail = function(error) {
			var msg = JSON.stringify(error);
			if (error && error.data && error.data.message) {
				msg = error.data.message;
			}
			Util.showError("Move failed: " + msg);
		};
		
		switch(entityType) {
			case "EXPERIMENT":
				require([ "as/dto/experiment/update/ExperimentUpdate"], 
			        function(ExperimentUpdate) {
			            var experimentUpdate = new ExperimentUpdate();
			            experimentUpdate.setExperimentId(moveEntityModel.entity.getIdentifier());
			 			experimentUpdate.setProjectId(moveEntityModel.selected.getIdentifier());
			            mainController.openbisV3.updateExperiments([ experimentUpdate ]).done(done).fail(fail);
        			});
				break;
			case "SAMPLE":
				require([ "as/dto/sample/fetchoptions/SampleFetchOptions", 
                          "as/dto/sample/update/SampleUpdate", "as/dto/space/id/SpacePermId"],
                    function(SampleFetchOptions, SampleUpdate, SpacePermId) {
                        var permId = moveEntityModel.entity.getPermId();
                        var experimentId = moveEntityModel.selected.getPermId();
                        var spaceCode = moveEntityModel.selected.getIdentifier().getIdentifier().split("/")[1];
                        var spaceId = new SpacePermId(spaceCode);
                        if (descendants) {
                            var currentExperiment = moveEntityModel.entity.getExperiment().getPermId().getPermId();
                            var fetchOptions = new SampleFetchOptions();
                            fetchOptions.withExperiment();
                            fetchOptions.withChildrenUsing(fetchOptions);
                            mainController.openbisV3.getSamples([permId], fetchOptions).done(function(map) {
                                var samplesToUpdate = [];
                                _this.gatherAllDescendants(samplesToUpdate, map[permId]);
                                var updates = []
                                samplesToUpdate.forEach(function(sample) {
                                    if (sample.getExperiment() != null && currentExperiment == sample.getExperiment().getPermId().getPermId()) {
                                        var sampleUpdate = new SampleUpdate();
                                        sampleUpdate.setSampleId(sample.getPermId());
                                        sampleUpdate.setExperimentId(experimentId);
                                        sampleUpdate.setSpaceId(spaceId);
                                        updates.push(sampleUpdate);
                                    }
                                });
                                mainController.openbisV3.updateSamples(updates).done(done).fail(fail);
                            });
                        } else {
                            var sampleUpdate = new SampleUpdate();
                            sampleUpdate.setSampleId(permId);
                            sampleUpdate.setExperimentId(experimentId);
                            sampleUpdate.setSpaceId(spaceId);
                            mainController.openbisV3.updateSamples([ sampleUpdate ]).done(done).fail(fail);
                        }
                    });
				break;
			case "DATASET":
				require([ "as/dto/dataset/update/DataSetUpdate"], 
			        function(DataSetUpdate) {
			            var datasetUpdate = new DataSetUpdate();
			            datasetUpdate.setDataSetId(moveEntityModel.entity.getPermId());
			            
			            switch(moveEntityModel.selected["@type"]) {
							case "as.dto.experiment.Experiment":
								datasetUpdate.setExperimentId(moveEntityModel.selected.getIdentifier());
							break;
							case "as.dto.sample.Sample":
								datasetUpdate.setSampleId(moveEntityModel.selected.getIdentifier());
							break;
						}
						
			            mainController.openbisV3.updateDataSets([ datasetUpdate ]).done(done).fail(fail);
        			});
				break;
			case "PROJECT":
				require(["as/dto/project/update/ProjectUpdate"], function (ProjectUpdate) {
					var projectUpdate = new ProjectUpdate();
					projectUpdate.setProjectId(moveEntityModel.entity.getIdentifier());
					projectUpdate.setSpaceId(moveEntityModel.selected.getPermId());
					mainController.openbisV3.updateProjects([projectUpdate]).done(done).fail(fail);
				});
				break;
		}
		
	}
	
    this.gatherAllDescendants = function(entities, entity) {
        entities.push(entity);
        entity.getChildren().forEach(child => this.gatherAllDescendants(entities, child));
    }
}