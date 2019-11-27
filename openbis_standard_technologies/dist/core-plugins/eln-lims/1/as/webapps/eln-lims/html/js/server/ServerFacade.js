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
 * Class ServerFacade.
 *
 * Contains all methods used to access the server, is used as control point to modify the API methods used
 * without impacting other classes.
 *
 * @constructor
 * @this {ServerFacade}
 * @param {openbis} openbisServer API facade to access the server.
 */
function ServerFacade(openbisServer) {
	this.openbisServer = openbisServer;

	//
	// V3 API creation
	//
    this.getOpenbisV3 = function(callbackFunction) {
        require(['openbis'], function(openbis) {
            //Boilerplate
            var testProtocol = window.location.protocol;
            var testHost = window.location.hostname;
            var testPort = window.location.port;

            var testUrl = testProtocol + "//" + testHost + ":" + testPort;
            var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

            var openbisV3 = new openbis(testApiUrl);
            callbackFunction(openbisV3);
        });
    }

    //
	// Intercepting general errors
	//
	var responseInterceptor = function(response, action){
		var isError = false;
		if(response && response.error) {
			if(response.error.message === "Session no longer available. Please login again."
					|| response.error.message.endsWith("is invalid: user is not logged in.")) {
				isError = true;
				Util.showError(response.error.message, function() {
					location.reload(true);
				}, true);
			} else if(response.error === "Request failed: ") {
				Util.showError(response.error + "openBIS or DSS cannot be reached. Please try again or contact your admin.", null, true, false, true);
			}
		}
		
		if(action && !isError){
			action(response);
		}
	}
	
	this.openbisServer.setResponseInterceptor(responseInterceptor);

	//
	// Custom Widget Settings
	//
	this.getCustomWidgetSettings = function(doneCallback) {
        require([ "openbis", "as/dto/property/search/PropertyTypeSearchCriteria", "as/dto/property/fetchoptions/PropertyTypeFetchOptions" ],
        function(openbis, PropertyTypeSearchCriteria, PropertyTypeFetchOptions) {
            var ptsc = new PropertyTypeSearchCriteria();
            var ptfo = new PropertyTypeFetchOptions();
            mainController.openbisV3.searchPropertyTypes(ptsc, ptfo).done(function(searchResult) {
                var customWidgetProperties = [];
                for(var oIdx = 0; oIdx < searchResult.totalCount; oIdx++) {
                    var propertyType = searchResult.objects[oIdx];
                    if(propertyType.metaData["custom_widget"]) {
                        customWidgetProperties.push({
                            "Property Type" : propertyType.code,
                            "Widget" : propertyType.metaData["custom_widget"]
                        });
                    }
                }
                doneCallback(customWidgetProperties);
            });
        });
    }

    this.setCustomWidgetSettings = function(widgetSettings, doneCallback) {
        require([ "openbis", "as/dto/property/update/PropertyTypeUpdate", "as/dto/property/id/PropertyTypePermId", "as/dto/common/update/ListUpdateActionAdd" ],
        function(openbis, PropertyTypeUpdate, PropertyTypePermId) {
            var ptus = [];
            for(var uIdx = 0; uIdx < widgetSettings.length; uIdx++) {
                var ptu = new PropertyTypeUpdate();
                ptu.setTypeId(new PropertyTypePermId(widgetSettings[uIdx]["Property Type"]));
                var luaa = new ListUpdateActionAdd();
                luaa.setItems([{"custom_widget" : widgetSettings[uIdx]["Widget"] }])
                ptu.setMetaDataActions([luaa]);
                ptus.push(ptu);
            }
            mainController.openbisV3.updatePropertyTypes(ptus).done(function() {
                doneCallback();
            });
        });
    }

	//
	// Display Settings
	//
	this.getSetting = function(keyOrNull, callback) {
		mainController.serverFacade.openbisServer.getWebAppSettings("ELN-LIMS", function(response) {
			var settings = response.result.settings;
			if(!settings) {
				settings = {};
			}
			if(keyOrNull) {
				callback(settings[keyOrNull]);
			} else {
				callback(settings);
			}
		});
	}
	
	this.setSetting = function(key, value) {
		var _this = this;
		var webAppId = "ELN-LIMS";
		this.openbisServer.getWebAppSettings(webAppId, function(response) {
			var settings = response.result.settings;
			if(!settings) {
				settings = {};
			}
			settings[key] = value;
			
			var webAppSettings = {
					"@type" : "WebAppSettings",
					"webAppId" : webAppId,
					"settings" : settings
			}
			
			_this.openbisServer.setWebAppSettings(webAppSettings, function(result) {});
		});
	}
	/* New Settings API - To use with new release
	this.getSetting = function(key, callback) {
		require([ "jquery", "openbis", "as/dto/person/update/PersonUpdate", "as/dto/person/id/Me", "as/dto/webapp/create/WebAppSettingCreation", "as/dto/person/fetchoptions/PersonFetchOptions" ],
        function($, openbis, PersonUpdate, Me, WebAppSettingCreation, PersonFetchOptions) {
            $(document).ready(function() {
				var mefo = new PersonFetchOptions();
				var	mefowsfo = mefo.withWebAppSettings("ELN-LIMS");
					mefowsfo.withSetting(key);
				mainController.openbisV3.getPersons([ new Me() ], mefo).done(function(persons) {
					var person = persons[new Me()];
					var settings = person.getWebAppSettings("ELN-LIMS");
					
					var keySettings = settings.getSetting(key);
					var value = null;
					if(keySettings) {
						value = keySettings.getValue();
					}
					console.log("getSetting key: " + key + " value: " + value);
					callback(value);
				});
            });
        });
	}
	
	this.setSetting = function(key, value) {
		// console.log("Write key: " + key + " value: " + value);
		require([ "jquery", "openbis", "as/dto/person/update/PersonUpdate", "as/dto/person/id/Me", "as/dto/webapp/create/WebAppSettingCreation", "as/dto/person/fetchoptions/PersonFetchOptions" ],
        function($, openbis, PersonUpdate, Me, WebAppSettingCreation, PersonFetchOptions) {
            $(document).ready(function() {
            		var update = new PersonUpdate();
                update.setUserId(new Me());
                var elnlims = update.getWebAppSettings("ELN-LIMS");
                elnlims.add(new WebAppSettingCreation(key, value));
                mainController.openbisV3.updatePersons([ update ]).done(function() { });
            });
        });
	}
	*/

    this.scheduleKeepAlive = function() {
        var _this = this;
        var TIMEOUT = 60000; //60 Seconds

        setTimeout(function(){
            _this.keepAlive();
        }, TIMEOUT);
    }

	this.keepAlive = function() {
	    var _this = this;
	    mainController.openbisV3.isSessionActive().done(function(isSessionActive) {
	        var timeStamp = Math.floor(Date.now() / 1000);
            _this.scheduleKeepAlive();
	    }).fail(function(error) {
            var timeStamp = Math.floor(Date.now() / 1000);
            _this.scheduleKeepAlive();
        });
	}

	this.getPersons = function(personIds, callbackFunction) {
		if(!mainController.openbisV3.getPersons) {
			return null; // In case the method doesn't exist, do nothing
		}
		require([ "jquery", "openbis", "as/dto/person/id/PersonPermId", "as/dto/person/fetchoptions/PersonFetchOptions" ],
        function($, openbis, PersonPermId, PersonFetchOptions) {
            $(document).ready(function() {
            		var personFetchOptions = new PersonFetchOptions();
            		personFetchOptions.withSpace();
                	var personPermIds = [];
                	for(var pIds=0; pIds < personIds.length; pIds++) {
                		personPermIds.push(new PersonPermId(personIds[pIds]));
                	}
                	mainController.openbisV3.getPersons(personPermIds, personFetchOptions).done(function(personsMap) {
                		var persons = [];
                		for(personId in personsMap) {
                			persons.push(personsMap[personId])
                		}
                		callbackFunction(persons);
                	});
            });
        });
	}
	
	//
	// Login Related Functions
	//
	this.getUserId = function() {
		var sessionId = this.openbisServer.getSession();
		var userId = sessionId.substring(0, sessionId.lastIndexOf("-"));
		return userId;
	}
	
	this.login = function(username, pass, callbackFunction) {
		this.openbisServer.login(username, pass, callbackFunction);
	}

	this.ifRestoredSessionActive = function(callbackFunction) {
		this.openbisServer.ifRestoredSessionActive(callbackFunction);
	}

	this.logout = function() {
		$("#mainContainer").hide();
		this.openbisServer.logout(function() {
			location.reload();
		});
	}
	
	//
	// User Related Functions
	//
	this.isFileAuthUser = function(callbackFunction) {
		this.customELNApi({
			"method" : "isFileAuthUser",
			"userId" : this.getUserId(),
		}, callbackFunction, "eln-lims-api");
	}
	
	this.listPersons = function(callbackFunction) {
		this.openbisServer.listPersons(callbackFunction);
	};
	
	this.updateUserInformation = function(userId, userInformation, callbackFunction) {
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(),
			{
				"method" : "updateUserInformation",
				"userId" : userId,
				"firstName" : userInformation.firstName,
				"lastName" : userInformation.lastName,
				"email" : userInformation.email,
			},
			this._handleAggregationServiceData.bind(this, callbackFunction));
	}

	this.registerUserPassword = function(userId, userPass, callbackFunction) {
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(),
			{
				"method" : "registerUserPassword",
				"userId" : userId,
				"password" : userPass
			},
			this._handleAggregationServiceData.bind(this, callbackFunction));
	}

	this._handleAggregationServiceData = function(callbackFunction, data) {
		if(data.result.rows[0][0].value == "OK") {
			callbackFunction(true);
		} else {
			Util.showError("Call failed to server: <pre>" + JSON.stringify(data, null, 2) + "</pre>");
			callbackFunction(false);
		}
	}

	this.createELNUser = function(userId, callback) {
 		var _this = this;
 		var inventorySpacesToRegister = [];
 		var inventorySpaceToRegisterFunc = function(spaceCode, userRole, callback) {
			return function() {
				_this.openbisServer.registerPersonSpaceRole(spaceCode, userId, userRole, function(data) {
					if(data.error) {
						callback(false, data.error.message);
					} else {
						var spaceToRegister = inventorySpacesToRegister.pop();
						if(spaceToRegister) {
							spaceToRegister();
						} else {
							callback(true, "User " + userId + " created successfully.");
						}
					}
				});
			}
		};
 		
		_this.openbisServer.registerPerson(userId, function(data) {
			if(data.error) {
				callback(false, data.error.message);
			} else {
				_this.openbisServer.registerSpace(userId, "Space for user " + userId, function(data) {
					if(data.error) {
						callback(false, data.error.message);
					} else {
						_this.openbisServer.registerPersonSpaceRole(userId, userId, "ADMIN", function(data) {
							if(data.error) {
								callback(false, data.error.message);
							} else {
								for(var i = 0; i < profile.inventorySpaces.length; i++) {
									var spaceCode = profile.inventorySpaces[i];
									inventorySpacesToRegister.push(inventorySpaceToRegisterFunc(spaceCode, "USER", callback));
								}
								
								for(var i = 0; i < profile.inventorySpacesReadOnly.length; i++) {
									var spaceCode = profile.inventorySpacesReadOnly[i];
									inventorySpacesToRegister.push(inventorySpaceToRegisterFunc(spaceCode, "OBSERVER", callback));
								}
								
								var spaceToRegister = inventorySpacesToRegister.pop();
								if(spaceToRegister) {
									spaceToRegister();
								} else {
									callback(true, "User " + userId + " created successfully.");
								}
							}
						});
					}			
				});
			}			
		});
	}
	
	//
	//
	//
	this.exportAll = function(entities, includeRoot, metadataOnly, callbackFunction) {
		this.customELNApi({
			"method" : "exportAll",
			"includeRoot" : includeRoot,
			"entities" : entities,
			"metadataOnly" : metadataOnly,
		}, callbackFunction, "exports-api");
	};

	//
	// Research collection export
	//
	this.exportRc = function(entities, includeRoot, metadataOnly, submissionUrl, submissionType, retentionPeriod, userInformation, callbackFunction) {
		this.asyncExportRc({
			"method": "exportAll",
			"includeRoot": includeRoot,
			"entities": entities,
			"metadataOnly": metadataOnly,
			"submissionUrl": submissionUrl,
			"submissionType": submissionType,
			"retentionPeriod": retentionPeriod,
            "userInformation": userInformation,
			"originUrl": window.location.origin,
			"sessionToken": this.openbisServer.getSession(),
		}, callbackFunction, "rc-exports-api");
	};

	this.asyncExportRc = function(parameters, callbackFunction, serviceId) {
		require(["as/dto/service/execute/ExecuteAggregationServiceOperation",
				"as/dto/operation/AsynchronousOperationExecutionOptions", "as/dto/service/id/DssServicePermId",
				"as/dto/datastore/id/DataStorePermId", "as/dto/service/execute/AggregationServiceExecutionOptions"],
			function(ExecuteAggregationServiceOperation, AsynchronousOperationExecutionOptions, DssServicePermId, DataStorePermId,
					 AggregationServiceExecutionOptions) {
				var dataStoreCode = profile.getDefaultDataStoreCode();
				var dataStoreId = new DataStorePermId(dataStoreCode);
				var dssServicePermId = new DssServicePermId(serviceId, dataStoreId);
				var options = new AggregationServiceExecutionOptions();

				options.withParameter("sessionToken", parameters["sessionToken"]);

				options.withParameter("entities", parameters["entities"]);
				options.withParameter("includeRoot", parameters["includeRoot"]);
				options.withParameter("metadataOnly", parameters["metadataOnly"]);
				options.withParameter("method", parameters["method"]);
				options.withParameter("originUrl", parameters["originUrl"]);
				options.withParameter("submissionType", parameters["submissionType"]);
				options.withParameter("retentionPeriod", parameters["retentionPeriod"]);
				options.withParameter("submissionUrl", parameters["submissionUrl"]);
				options.withParameter("entities", parameters["entities"]);
				options.withParameter("userId", parameters["userInformation"]["id"]);
				options.withParameter("userEmail", parameters["userInformation"]["email"]);
				options.withParameter("userFirstName", parameters["userInformation"]["firstName"]);
				options.withParameter("userLastName", parameters["userInformation"]["lastName"]);

				var operation = new ExecuteAggregationServiceOperation(dssServicePermId, options);
				mainController.openbisV3.executeOperations([operation], new AsynchronousOperationExecutionOptions()).done(function(results) {
					callbackFunction(results.executionId.permId);
				});
			});
	};

    this.exportZenodo = function(entities, includeRoot, metadataOnly, userInformation, title, accessToken, callbackFunction) {
        this.asyncExportZenodo({
            "method": "exportAll",
            "includeRoot": includeRoot,
            "entities": entities,
            "metadataOnly": metadataOnly,
            "userInformation": userInformation,
            "originUrl": window.location.origin,
            "sessionToken": this.openbisServer.getSession(),
			"submissionTitle": title,
			"accessToken": accessToken
        }, callbackFunction, "zenodo-exports-api");
    };

	this.asyncExportZenodo = function(parameters, callbackFunction, serviceId) {
		require(["as/dto/service/execute/ExecuteAggregationServiceOperation",
				"as/dto/operation/AsynchronousOperationExecutionOptions", "as/dto/service/id/DssServicePermId",
				"as/dto/datastore/id/DataStorePermId", "as/dto/service/execute/AggregationServiceExecutionOptions"],
			function(ExecuteAggregationServiceOperation, AsynchronousOperationExecutionOptions, DssServicePermId, DataStorePermId,
					 AggregationServiceExecutionOptions) {
				var dataStoreCode = profile.getDefaultDataStoreCode();
				var dataStoreId = new DataStorePermId(dataStoreCode);
				var dssServicePermId = new DssServicePermId(serviceId, dataStoreId);
				var options = new AggregationServiceExecutionOptions();

				options.withParameter("sessionToken", parameters["sessionToken"]);
				options.withParameter("entities", parameters["entities"]);
				options.withParameter("includeRoot", parameters["includeRoot"]);
				options.withParameter("metadataOnly", parameters["metadataOnly"]);
				options.withParameter("method", parameters["method"]);
				options.withParameter("originUrl", parameters["originUrl"]);
				options.withParameter("submissionType", parameters["submissionType"]);
				options.withParameter("submissionUrl", parameters["submissionUrl"]);
				options.withParameter("entities", parameters["entities"]);
				options.withParameter("submissionTitle", parameters["submissionTitle"]);
				options.withParameter("accessToken", parameters["accessToken"]);
				options.withParameter("userId", parameters["userInformation"]["id"]);
				options.withParameter("userEmail", parameters["userInformation"]["email"]);
				options.withParameter("userFirstName", parameters["userInformation"]["firstName"]);
				options.withParameter("userLastName", parameters["userInformation"]["lastName"]);

				var operation = new ExecuteAggregationServiceOperation(dssServicePermId, options);
				mainController.openbisV3.executeOperations([operation], new AsynchronousOperationExecutionOptions()).done(function(results) {
					callbackFunction(results.executionId.permId);
				});
			});
	};

	//
	// Gets submission types
	//
	this.listSubmissionTypes = function(callbackFunction) {
		this.customELNApi({
			"method": "getSubmissionTypes",
		}, callbackFunction, "rc-exports-api");
	};

	//
	// Gets archiving info for specified data set codes
	//
	this.getArchivingInfo = function(dataSets, callbackFunction) {
		this.customELNApi({
			"method" : "getArchivingInfo",
			"args" : dataSets.join(","),
		}, function(error, result) {
			if (error) {
				Util.showError(error);
			} else {
				callbackFunction(result.data);
			}
		}, "archiving-api");
	};

	//
	// Metadata Related Functions
	//
	this.listSampleTypes = function(callbackFunction) {
		this.openbisServer.listSampleTypes(callbackFunction);
	}
	
	this.listExperimentTypes = function(callbackFunction) {
		this.openbisServer.listExperimentTypes(callbackFunction);
	}
	
	this.listVocabularies = function(callbackFunction) {
		this.openbisServer.listVocabularies(callbackFunction);
	}
	
	this.listDataSetTypes = function(callbackFunction) {
		this.openbisServer.listDataSetTypes(callbackFunction);
	}
	
	this.listSpaces = function(callbackFunction) {
		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(spacesSearchResult) {
			var spaces = [];
			for(var sIdx = 0; sIdx < spacesSearchResult.objects.length; sIdx++) {
				var space = spacesSearchResult.objects[sIdx];
				spaces.push(space.code);
			}
			callbackFunction(spaces);
		});
	}
	
	this.listSpacesWithProjectsAndRoleAssignments = function(somethingOrNull, callbackFunction) {
		this.openbisServer.listSpacesWithProjectsAndRoleAssignments(somethingOrNull, callbackFunction);
	}
	
	this.getSpaceFromCode = function(spaceCode, callbackFunction) {
		this.listSpaces(function(spaces) {
			spaces.forEach(function(space){
				if(space === spaceCode) {
					callbackFunction(space);
				}
			});
		});
	}
	
	this.listExperiments = function(projects, callbackFunction) {
		if(projects && projects.length > 0) {
			this.openbisServer.listExperiments(projects, null, callbackFunction);
		} else {
			callbackFunction({});
		}
	}
	
	this.getProjectFromIdentifier = function(identifier, callbackFunction) {
		this.openbisServer.listProjects(function(data) {
			data.result.forEach(function(project){
				var projIden = IdentifierUtil.getProjectIdentifier(project.spaceCode, project.code);
				if(projIden === identifier) {
					callbackFunction(project);
					return;
				}
			});
		});
	}
	
	this.getProjectFromPermId = function(permId, callbackFunction) {
		this.openbisServer.listProjects(function(data) {
			data.result.forEach(function(project){
				if(project.permId === permId) {
					callbackFunction(project);
					return;
				}
			});
		});
	}
	
	this.listExperimentsForIdentifiers = function(experimentsIdentifiers, callbackFunction) {
		this.openbisServer.listExperimentsForIdentifiers(experimentsIdentifiers, callbackFunction);
	}
	
	this.listSamplesForExperiments = function(experiments, callbackFunction) {
		var experimentsMatchClauses = []
		
		experiments.forEach(function(experiment){
			experimentsMatchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "PERM_ID",
				desiredValue : experiment.permId
			});
		});
		
		var experimentCriteria = {
				matchClauses : experimentsMatchClauses,
				operator : "MATCH_ANY_CLAUSES"
		}
		
		var experimentSubCriteria = {
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "EXPERIMENT",	
				"criteria" : experimentCriteria
		}

		var sampleCriteria = 
		{
			subCriterias : [ experimentSubCriteria ],
			operator : "MATCH_ALL_CLAUSES"
		};
		
		if(experiments.length === 0) {
			callbackFunction({});
		} else {
			this.openbisServer.searchForSamples(sampleCriteria, callbackFunction);
		}
	}
	
	this.listPropertyTypes = function(callbackFunction) {
		if(this.openbisServer.listPropertyTypes) { //If not present will not break, but annotations should not be used.
			this.openbisServer.listPropertyTypes(false, callbackFunction);
		}
	}
	

	this.generateCode = function(sampleType, action) {
		var parameters = {
			"method" : "getNextSequenceForType",
			"sampleTypeCode" : sampleType.code
		}
		this.customELNASAPI(parameters, function(nextInSequence) {
			action(sampleType.codePrefix + nextInSequence);
		});
	}
		
	this.deleteDataSets = function(datasetIds, reason, callback) {
		this.openbisServer.deleteDataSets(datasetIds, reason, "TRASH", callback);
	}
	
	
	this.deleteSamples = function(samplePermIds, reason, callback, confirmDeletions) {
		require(["as/dto/sample/id/SamplePermId", "as/dto/sample/delete/SampleDeletionOptions" ], 
			function(SamplePermId, SampleDeletionOptions) {
				var samplePermIdsObj = samplePermIds.map(function(permId) { return new SamplePermId(permId)});
				var deletionOptions = new SampleDeletionOptions();
				deletionOptions.setReason(reason);

				// logical deletion (move objects to the trash can)
				mainController.openbisV3.deleteSamples(samplePermIdsObj, deletionOptions).done(function(deletionId) {
					if(confirmDeletions) {
						// Confirm deletion of samples
						mainController.openbisV3.confirmDeletions([deletionId]).then(function() {
							callback({});
						});
					} else {
						callback(deletionId);
					}
				}).fail(function(error) {
					Util.showFailedServerCallError(error);
					Util.unblockUI();
				});
		});
	}
	
	this.deleteExperiments = function(experimentIds, reason, callback) {
		this.openbisServer.deleteExperiments(experimentIds, reason, "TRASH", callback);
	}
	
	this.deleteProjects = function(projectIds, reason, callback) {
		this.openbisServer.deleteProjects(projectIds, reason, callback);
	}
	
	this.listDeletions = function(callback) {
		this.openbisServer.listDeletions(["ALL_ENTITIES"], callback);
	}
	
	this.deletePermanently = function(deletionIds, callback) {
		this.openbisServer.deletePermanently(deletionIds, callback);
	}
	
	this.revertDeletions = function(deletionIds, callback) {
		this.openbisServer.revertDeletions(deletionIds, callback);
	}
	
	//
	// Data Set Related Functions
	//
	this.listDataSetsForExperiment = function(experimentToSend, callbackFunction) {
		//Should be a V1 Experiment
		this.openbisServer.listDataSetsForExperiments([experimentToSend], [ 'PARENTS' ], callbackFunction);
	}
	
	this.listDataSetsForSample = function(sampleToSend, trueOrFalse, callbackFunction) {
		var _this = this;
		var listDataSetsForV1Sample = function(v1Sample) {
			var cleanSample = $.extend({}, v1Sample);
			delete cleanSample.parents;
			delete cleanSample.children;
			_this.openbisServer.listDataSetsForSample(cleanSample, trueOrFalse, callbackFunction);
		}
		
		if(sampleToSend.id !== -1) { //Is V1 Sample
			listDataSetsForV1Sample(sampleToSend);
		} else { //Ask for a V1 Sample
			this.searchSamplesV1({
				"samplePermId" : sampleToSend.permId,
				"withProperties" : true,
				"withParents" : true,
				"withChildren" : true
			}, function(sampleList) {
				listDataSetsForV1Sample(sampleList[0]);
			});
		}
	}

	this.listFilesForDataSet = function(datasetCode, pathInDataset, trueOrFalse, callbackFunction) {
		this.openbisServer.listFilesForDataSet(datasetCode, pathInDataset, trueOrFalse, callbackFunction);
	}

	//
	// Samples Import Related Functions
	//
	this.uploadedSamplesInfo = function(sampleTypeCode, fileKeyAtHTTPSession, callbackFunction) {
		this.openbisServer.uploadedSamplesInfo(sampleTypeCode, fileKeyAtHTTPSession, callbackFunction);
	}

	this.registerSamplesWithSilentOverrides = function(sampleTypeCode, spaceIdentifier, experimentIdentifier, fileKeyAtHTTPSession, somethingOrNull, callbackFunction) {
		this.openbisServer.registerSamplesWithSilentOverrides(sampleTypeCode, spaceIdentifier, experimentIdentifier, fileKeyAtHTTPSession, somethingOrNull, callbackFunction);
	}

	this.updateSamplesWithSilentOverrides = function(sampleTypeCode, spaceIdentifier, experimentIdentifier, fileKeyAtHTTPSession, somethingOrNull, callbackFunction) {
		this.openbisServer.updateSamplesWithSilentOverrides(sampleTypeCode, spaceIdentifier, experimentIdentifier, fileKeyAtHTTPSession, somethingOrNull, callbackFunction);
	}
	
	this.fileUpload = function(file, callbackFunction) {
		//Building Form Data Object for Multipart File Upload
		var formData = new FormData();
		formData.append("sessionKeysNumber", 1);
		formData.append("sessionKey_0", "sample-file-upload");
		formData.append("sample-file-upload", file);
		formData.append("sessionID", this.openbisServer.getSession());
		
		$.ajax({
			type: "POST",
			url: "/openbis/openbis/upload",
			contentType: false,
			processData: false,
			data: formData,
			success: function(result) {
				callbackFunction(result);
			}
		});
	}
	
	this.getTemplateLink = function(entityType, operationKind) {
		var GET = '/openbis/openbis/template-download?entityKind=SAMPLE';
			GET += '&entityType=' + entityType;
			GET += '&autoGenerate=false';
			GET += '&with_experiments=true';
			GET += '&with_space=true';
			GET += '&batch_operation_kind=' + operationKind;
			GET += '&timestamp=' + new Date().getTime();
			GET += '&sessionID=' + this.openbisServer.getSession();
		return GET;
	}

	this.getDirectLinkURL = function(callbackFunction) {
		this.customELNApi({ "method" : "getDirectLinkURL"}, callbackFunction);
	}
	
	//
	// Sample Others functions
	//
	this.moveSample = function(sampleIdentifier, experimentIdentifier, experimentType, callbackFunction) {
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(),
				{
					"method" : "moveSample",
					"sampleIdentifier" : sampleIdentifier,
					"experimentIdentifier" : experimentIdentifier,
					"experimentType" : experimentType
				},
				function(data){
					if(data.result.rows[0][0].value == "OK") {
						callbackFunction(true);
					} else {
						callbackFunction(false, data.result.rows[0][1].value);
					}
				});
	}
	//
	// Data Set Import Related Functions
	//
	
	this.fileUploadToWorkspace = function(dataStoreURL, fileFieldId, fileSessionKey, callbackHandler) {
		//File
		var file = document.getElementById(fileFieldId).files[0];
		var sessionID = this.openbisServer.getSession();
		var id = 0;
		var startByte = 0;
		var endByte = file.size;
		
		$.ajax({
			type: "POST",
			url: dataStoreURL + "/session_workspace_file_upload?sessionID=" + sessionID + "&filename=" + fileSessionKey + "&id=" + id + "&startByte=" + startByte + "&endByte=" + endByte,
			contentType: "multipart/form-data",
			processData: false,
			data: file,
			success: function(result) {
				callbackHandler(result);
			},
			error: function(result) {
				Util.showError("The upload failed. Configure your environment properly.", function() {Util.unblockUI();});
			}
		});
	}
	
	//
	// ELN Custom API
 	//

    this.sendResetPasswordEmail = function(userId, callbackFunction) {
        var parameters = {
                method : "sendResetPasswordEmail",
                userId : userId,
                baseUrl : location.protocol + '//' + location.host + location.pathname
        };
        this._callPasswordResetService(parameters, callbackFunction);
    }

    this.resetPassword = function(userId, token, callbackFunction) {
        var parameters = {
                method : "resetPassword",
                userId : userId,
                token : token
            };
        this._callPasswordResetService(parameters, callbackFunction);        
    }

    this.doIfFileAuthenticationService = function(callbackFunction) {
        var _this = this;
        this.getOpenbisV3(function(openbisV3) {
            openbisV3.loginAsAnonymousUser().done(function(sessionToken) {
                openbisV3.getServerInformation().done(function(serverInformation) {
                    var authSystem = serverInformation["authentication-service"];
                    if (authSystem && authSystem.indexOf("file") !== -1) {
                        callbackFunction();
                    }
                });
            }).fail(function(result) {
                console.log("Call failed to server: " + JSON.stringify(result));
            });
        });
    }

    this._callPasswordResetService = function(parameters, callbackFunction) {
        var _this = this;
        this.getOpenbisV3(function(openbisV3) {
            openbisV3.loginAsAnonymousUser().done(function(sessionToken) {
                _this.openbisServer._internal.sessionToken = sessionToken;

                _this.listDataStores(function(dataStores) {
                    profile.allDataStores = dataStores.result;
                    _this.customELNApi(parameters, function(error, result) {
                        if (error) {
                            Util.showError(error);
                        } else {
                            callbackFunction(result);                            
                        }
                    }, "password-reset-api");
                });

            }).fail(function(result) {
                console.log("Call failed to server: " + JSON.stringify(result));
            });

        });
    }

 	this.customELNApi = function(parameters, callbackFunction, service) {
		var _this = this;
 		if(!service) {
 			service = "eln-lims-api";
 		}
 		
 		if(!parameters) {
 			parameters = {};
 		}
 		parameters["sessionToken"] = this.openbisServer.getSession();
 		
 		var dataStoreCode = profile.getDefaultDataStoreCode();
 		this.openbisServer.createReportFromAggregationService(dataStoreCode, service, parameters, function(data) {
			_this.customELNApiCallbackHandler(data, callbackFunction);
 		});
	};

	this.customELNApiCallbackHandler = function(data, callbackFunction) {
		var error = null;
		var result = {};
		if (data && data.error) { //Error Case 1
			error = data.error.message;
		} else if (data && data.result.columns[1].title === "Error") { //Error Case 2
			error = data.result.rows[0][1].value;
		} else if (data && data.result.columns[0].title === "STATUS" && data.result.rows[0][0].value === "OK") { //Success Case
			result.message = data.result.rows[0][1].value;
			result.data = data.result.rows[0][2].value;
			if(result.data) {
				result.data = JSON.parse(result.data);
			}
		} else {
			error = "Unknown Error.";
		}
		callbackFunction(error, result);
	};

	this.customELNASAPI = function(parameters, callbackFunction) {
		this.customASService(parameters, callbackFunction, "as-eln-lims-api");
	}

	this.createReportFromAggregationService = function(dataStoreCode, parameters, callbackFunction, service) {
 		if(!service) {
 			service = "eln-lims-api";
 		}
 		if(!parameters) {
 			parameters = {};
 		}
 		parameters["sessionToken"] = this.openbisServer.getSession();
 		
		this.openbisServer.createReportFromAggregationService(dataStoreCode, service, parameters, callbackFunction);
	}
	
	//
	// Configuration Related Functions
	//
	this.getSession = function() {
		return this.openbisServer.getSession();
	}

	this.listDataStores = function(callbackFunction) {
		this.openbisServer.listDataStores(callbackFunction);
	}

	this.getUserDisplaySettings = function(callbackFunction) {
		if(this.openbisServer.getUserDisplaySettings) { //If the call exists
			this.openbisServer.getUserDisplaySettings(callbackFunction);
		}
	}

	//
	// Search Related Functions
	//
	
	this._createMaterialIdentifier = function(identifierString) {
		var parts = identifierString.split("/");

		return {
			"@type" : "MaterialIdentifierGeneric",
			"materialTypeIdentifier" : {
				"@type" : "MaterialTypeIdentifierGeneric",
				"materialTypeCode" : parts[1]
			},
			"materialCode" : parts[2]
		};
	}
	
	this.getMaterialsForIdentifiers = function(materialIdentifiers, callback) {
		var materialIdentifierObjects = [];
		for(var i = 0; i < materialIdentifiers.length; i++) {
			materialIdentifierObjects.push(this._createMaterialIdentifier(materialIdentifiers[i]));
		}
		this.openbisServer.getMaterialByCodes(materialIdentifierObjects, callback);
	}
	
	//
	// Search DataSet
	//
	
	this.searchDataSetWithUniqueId = function(dataSetPermId, callbackFunction) {
		var dataSetMatchClauses = [{
    			"@type":"AttributeMatchClause",
    			fieldType : "ATTRIBUTE",			
    			attribute : "PERM_ID",
    			desiredValue : dataSetPermId
		}]
		
		var dataSetCriteria = 
		{
			matchClauses : dataSetMatchClauses,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		this.openbisServer.searchForDataSets(dataSetCriteria, callbackFunction)
	}
	
	this.searchDataSetsWithTypeForSamples = function(dataSetTypeCode, samplesPermIds, callbackFunction)
	{
		var sampleMatchClauses = []
		
		samplesPermIds.forEach(function(samplesPermId){
			sampleMatchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "PERM_ID",
				desiredValue : samplesPermId
			});
		});
		
		var sampleCriteria = {
				matchClauses : sampleMatchClauses,
				operator : "MATCH_ANY_CLAUSES"
		}
		
		var sampleSubCriteria = {
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "SAMPLE",	
				"criteria" : sampleCriteria
		}
		
		var dataSetMatchClauses = [{
    			"@type":"AttributeMatchClause",
    			fieldType : "ATTRIBUTE",			
    			attribute : "TYPE",
    			desiredValue : dataSetTypeCode
		}]

		var dataSetCriteria = 
		{
			matchClauses : dataSetMatchClauses,
			subCriterias : [ sampleSubCriteria ],
			operator : "MATCH_ALL_CLAUSES"
		};
		
		this.openbisServer.searchForDataSets(dataSetCriteria, callbackFunction)
	}
	
	// Used for blast search datasets
	this.getSamplesForDataSets = function(dataSetCodes, callback) {
		this.openbisServer.getDataSetMetaDataWithFetchOptions(dataSetCodes, [ 'SAMPLE' ], callback);
	}
	
	//
	// New Advanced Search
	//

	this.getSearchCriteriaAndFetchOptionsForDataSetSearch = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/dataset/search/DataSetSearchCriteria';
		var fetchOptionsClass = 'as/dto/dataset/fetchoptions/DataSetFetchOptions';
		this.getSearchCriteriaAndFetchOptionsForEntitySearch(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass);
	}

	this.searchForDataSetsAdvanced = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/dataset/search/DataSetSearchCriteria';
		var fetchOptionsClass = 'as/dto/dataset/fetchoptions/DataSetFetchOptions';
		var searchMethodName = 'searchDataSets';
		this.searchForEntityAdvanced(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}

	this.getSearchCriteriaAndFetchOptionsForExperimentSearch = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/experiment/search/ExperimentSearchCriteria';
		var fetchOptionsClass = 'as/dto/experiment/fetchoptions/ExperimentFetchOptions';
		this.getSearchCriteriaAndFetchOptionsForEntitySearch(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass);
	}

	this.searchForExperimentsAdvanced = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/experiment/search/ExperimentSearchCriteria';
		var fetchOptionsClass = 'as/dto/experiment/fetchoptions/ExperimentFetchOptions';
		var searchMethodName = 'searchExperiments';
		this.searchForEntityAdvanced(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}

	this.getSearchCriteriaAndFetchOptionsForSamplesSearch = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/sample/search/SampleSearchCriteria';
		var fetchOptionsClass = 'as/dto/sample/fetchoptions/SampleFetchOptions';
		this.getSearchCriteriaAndFetchOptionsForEntitySearch(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass);
	}

	this.searchForSamplesAdvanced = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/sample/search/SampleSearchCriteria';
		var fetchOptionsClass = 'as/dto/sample/fetchoptions/SampleFetchOptions';
		var searchMethodName = 'searchSamples';
		this.searchForEntityAdvanced(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForSpacesAdvanced = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/space/search/SpaceSearchCriteria';
		var fetchOptionsClass = 'as/dto/space/fetchoptions/SpaceFetchOptions';
		var searchMethodName = 'searchSpaces';
		this.searchForEntityAdvanced(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForProjectsAdvanced = function(advancedSearchCriteria, advancedFetchOptions, callback) {
		var criteriaClass = 'as/dto/project/search/ProjectSearchCriteria';
		var fetchOptionsClass = 'as/dto/project/fetchoptions/ProjectFetchOptions';
		var searchMethodName = 'searchProjects';
		this.searchForEntityAdvanced(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}

	this.getSearchCriteriaAndFetchOptionsForEntitySearch = function(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass) {
		require([criteriaClass,
		         fetchOptionsClass,
		         'as/dto/common/search/DateObjectEqualToValue',
		         'as/dto/experiment/search/ExperimentSearchCriteria',
		         'as/dto/experiment/fetchoptions/ExperimentFetchOptions',
		         'as/dto/space/search/SpaceSearchCriteria',
		         'as/dto/sample/fetchoptions/SampleFetchOptions',
		         'as/dto/space/search/SpaceSearchCriteria',
		         'as/dto/space/fetchoptions/SpaceFetchOptions',
		         'as/dto/project/search/ProjectSearchCriteria',
		         'as/dto/project/fetchoptions/ProjectFetchOptions'], function(EntitySearchCriteria, EntityFetchOptions, DateObjectEqualToValue) {
			try {
				//Setting the searchCriteria given the advancedSearchCriteria model
				var searchCriteria = new EntitySearchCriteria();

				//Setting the fetchOptions given standard settings
				var fetchOptions = new EntityFetchOptions();
				
				
				//Optional fetchOptions
				if(!advancedFetchOptions ||
				   (advancedFetchOptions && !(advancedFetchOptions.minTableInfo || advancedFetchOptions.only))
				   ) {
					if(fetchOptions.withType) {
						fetchOptions.withType();
					}
					if(fetchOptions.withSpace) {
						fetchOptions.withSpace();
					}
					if(fetchOptions.withRegistrator) {
						fetchOptions.withRegistrator();
					}
					if(fetchOptions.withModifier) {
						fetchOptions.withModifier();
					}
					var forceDisableWithProperties = advancedFetchOptions && advancedFetchOptions.withProperties === false;
					if(fetchOptions.withProperties && !forceDisableWithProperties) {
						fetchOptions.withProperties();
					}
					
					if(fetchOptions.withProject) {
						fetchOptions.withProject();
					}
					if(fetchOptions.withSample) {
						fetchOptions.withSample();
						if(advancedFetchOptions && advancedFetchOptions.withSampleProperties) {
							fetchOptions.withSample().withProperties();
						}
					}
					if(fetchOptions.withExperiment) {
						fetchOptions.withExperiment();
						if(advancedFetchOptions && advancedFetchOptions.withExperimentProperties) {
							fetchOptions.withExperiment().withProperties();
						}
					}
					if(fetchOptions.withTags) {
						fetchOptions.withTags();
					}
					if(fetchOptions.withLinkedData) {
						fetchOptions.withLinkedData();
					}
					if(fetchOptions.withPhysicalData) {
						fetchOptions.withPhysicalData();
					}
					var parentfetchOptions = jQuery.extend(true, {}, fetchOptions);
					var childrenfetchOptions = jQuery.extend(true, {}, fetchOptions);
					var forceDisableWithParents = advancedFetchOptions && advancedFetchOptions.withParents === false;
					if(fetchOptions.withParents && !forceDisableWithParents) {
						var forceDisableWithAncestors = advancedFetchOptions && advancedFetchOptions.withAncestors === false;
						if(!forceDisableWithAncestors) {
							parentfetchOptions.withParentsUsing(parentfetchOptions);
						}
						fetchOptions.withParentsUsing(parentfetchOptions);
					}
					var forceDisableWithChildren = advancedFetchOptions && advancedFetchOptions.withChildren === false;
					if(fetchOptions.withChildren && !forceDisableWithChildren) {
						var forceDisableWithDescendants = advancedFetchOptions && advancedFetchOptions.withDescendants === false;
						if(!forceDisableWithDescendants) {
							childrenfetchOptions.withChildrenUsing(childrenfetchOptions);
						}
						fetchOptions.withChildrenUsing(childrenfetchOptions);
					}
				} else if(advancedFetchOptions.minTableInfo) {
					if(fetchOptions.withType) {
						fetchOptions.withType();
					}
					if(fetchOptions.withSpace) {
						fetchOptions.withSpace();
					}
					if(fetchOptions.withRegistrator) {
						fetchOptions.withRegistrator();
					}
					if(fetchOptions.withModifier) {
						fetchOptions.withModifier();
					}
					if(fetchOptions.withProperties) {
						fetchOptions.withProperties();
					}
					
					if(advancedFetchOptions.withExperiment && fetchOptions.withExperiment) {
						fetchOptions.withExperiment();
					}
					if(advancedFetchOptions.withSample && fetchOptions.withSample) {
						fetchOptions.withSample();
					}
					if(fetchOptions.withParents && !(advancedFetchOptions.withParents === false)) {
						fetchOptions.withParents();
					}
					if(fetchOptions.withChildren && !(advancedFetchOptions.withChildren === false)) {
						var childrenFetchOptions = fetchOptions.withChildren();
						if(advancedFetchOptions.withChildrenInfo) {
							childrenFetchOptions.withType();
							childrenFetchOptions.withProperties();
						}
					}
				} else if(advancedFetchOptions.only) {
					if(advancedFetchOptions.withSample) {
						fetchOptions.withSample();
						if(advancedFetchOptions.withSampleProperties) {
							fetchOptions.withSample().withProperties();
						}
					}
					if(advancedFetchOptions.withExperiment) {
						fetchOptions.withExperiment();
						if(advancedFetchOptions.withExperimentProperties) {
							fetchOptions.withExperiment().withProperties();
						}
					}
					
					if(advancedFetchOptions.withProperties) {
						fetchOptions.withProperties();
					}
					if(advancedFetchOptions.withType) {
						fetchOptions.withType();
					}
					if(advancedFetchOptions.withExperiment) {
						fetchOptions.withExperiment();
					}
					if(advancedFetchOptions.withParents) {
						var parentFetchOptions = fetchOptions.withParents();
						if(advancedFetchOptions.withParentsType) {
							parentFetchOptions.withType();
						}
					}
					if(advancedFetchOptions.withChildren) {
						var childrenFetchOptions = fetchOptions.withChildren();
						if(advancedFetchOptions.withChildrenType) {
							childrenFetchOptions.withType();
						}
						if(advancedFetchOptions.withChildrenProperties) {
                            childrenFetchOptions.withProperties();
                        }
					}
					if(advancedFetchOptions.withAncestors) {
						var ancestorsFetchOptions = fetchOptions.withParents();
						if(advancedFetchOptions.withAncestorsProperties) {
							ancestorsFetchOptions.withProperties();
						}
						ancestorsFetchOptions.withParentsUsing(ancestorsFetchOptions);
					}
				}
				
				if(advancedFetchOptions && advancedFetchOptions.cache) {
					fetchOptions.cacheMode(advancedFetchOptions.cache);
				}
				
				if(advancedFetchOptions && 
						advancedFetchOptions.count != null &&
						advancedFetchOptions.count != undefined && 
						advancedFetchOptions.from != null &&
						advancedFetchOptions.from != undefined) {
					fetchOptions.from(advancedFetchOptions.from);
					fetchOptions.count(advancedFetchOptions.count);
				}
				
				if(advancedFetchOptions && advancedFetchOptions.sort) {
					switch(advancedFetchOptions.sort.type) {
						case "Attribute":
							if(fetchOptions.sortBy()[advancedFetchOptions.sort.name]) {
								fetchOptions.sortBy()[advancedFetchOptions.sort.name]()[advancedFetchOptions.sort.direction]();
							}
							break;
						case "Property":
							fetchOptions.sortBy().property(advancedFetchOptions.sort.name)[advancedFetchOptions.sort.direction]();
							break;
					}
				}
				
				var setOperator = function(criteria, operator) {
					//Operator
					if (!operator) {
						operator = "AND";
					}
					criteria.withOperator(operator);
					return criteria;
				}
				
				searchCriteria = setOperator(searchCriteria, advancedSearchCriteria.logicalOperator);
			
				//Rules
				var ruleKeys = Object.keys(advancedSearchCriteria.rules);
				for (var idx = 0; idx < ruleKeys.length; idx++)
				{
					var fieldType = advancedSearchCriteria.rules[ruleKeys[idx]].type;
					var fieldName = advancedSearchCriteria.rules[ruleKeys[idx]].name;
					var fieldNameType = null;
					var fieldValue = advancedSearchCriteria.rules[ruleKeys[idx]].value;
					var fieldOperator = advancedSearchCriteria.rules[ruleKeys[idx]].operator;
					
					if(fieldName) {
						var firstDotIndex = fieldName.indexOf(".");
						fieldNameType = fieldName.substring(0, firstDotIndex);
						fieldName = fieldName.substring(firstDotIndex + 1, fieldName.length);
					}
				
					if(!fieldValue) {
						fieldValue = "*";
					}
				
					var setPropertyCriteria = function(criteria, propertyName, propertyValue, comparisonOperator) {
						if(comparisonOperator) {
							try {
								switch(comparisonOperator) {
									case "thatEqualsString":
										criteria.withProperty(propertyName).thatEquals(propertyValue);
										break;
									case "thatEqualsNumber":
										criteria.withNumberProperty(propertyName).thatEquals(parseFloat(propertyValue));
										break;
									case "thatEqualsDate":
										criteria.withDateProperty(propertyName).thatEquals(propertyValue);
										break;
									case "thatContainsString":
										criteria.withProperty(propertyName).thatContains(propertyValue);
										break;
									case "thatStartsWithString":
										criteria.withProperty(propertyName).thatStartsWith(propertyValue);
										break;
									case "thatEndsWithString":
										criteria.withProperty(propertyName).thatEndsWith(propertyValue);
										break;
									case "thatIsLessThanNumber":
										criteria.withNumberProperty(propertyName).thatIsLessThan(parseFloat(propertyValue));
										break;
									case "thatIsLessThanOrEqualToNumber":
										criteria.withNumberProperty(propertyName).thatIsLessThanOrEqualTo(parseFloat(propertyValue));
										break;
									case "thatIsGreaterThanNumber":
										criteria.withNumberProperty(propertyName).thatIsGreaterThan(parseFloat(propertyValue));
										break;
									case "thatIsGreaterThanOrEqualToNumber":
										criteria.withNumberProperty(propertyName).thatIsGreaterThanOrEqualTo(parseFloat(propertyValue));
										break;
									case "thatIsLaterThanOrEqualToDate":
										criteria.withDateProperty(propertyName).thatIsLaterThanOrEqualTo(propertyValue);
										break;
									case "thatIsEarlierThanOrEqualToDate":
										criteria.withDateProperty(propertyName).thatIsEarlierThanOrEqualTo(propertyValue);
										break;
								}
							} catch(error) {
								Util.showError("Error parsing criteria: " + error.message);
								return;
							}
						} else {
							criteria.withProperty(propertyName).thatContains(propertyValue);
						}
					}
				
					var setAttributeCriteria = function(criteria, attributeName, attributeValue, comparisonOperator) {
						switch(attributeName) {
							//Used by all entities
							case "CODE":
								if(!comparisonOperator) {
									comparisonOperator = "thatEquals";
								}
								switch(comparisonOperator) {
									case "thatEquals":
											criteria.withCode().thatEquals(attributeValue);
											break;
									case "thatContains":
											criteria.withCode().thatContains(attributeValue);
											break;
								}
								break;
							case "PERM_ID":
								criteria.withPermId().thatEquals(attributeValue);
								break;
							case "METAPROJECT":
								criteria.withTag().withCode().thatEquals(attributeValue); //TO-DO To Test, currently not supported by ELN UI
								break;
							case "REGISTRATOR":
								if(comparisonOperator) {
									switch(comparisonOperator) {
										case "thatEqualsUserId":
											criteria.withRegistrator().withUserId().thatEquals(attributeValue);
											break;
										case "thatContainsFirstName":
											criteria.withRegistrator().withFirstName().thatContains(attributeValue);
											break;
										case "thatContainsLastName":
											criteria.withRegistrator().withLastName().thatContains(attributeValue);
											break;
									}
								}
								break;
							case "REGISTRATION_DATE": //Must be a string object with format 2009-08-18
								if(comparisonOperator) {
									switch(comparisonOperator) {
										case "thatEqualsDate":
											criteria.withRegistrationDate().thatEquals(attributeValue);
											break;
										case "thatIsLaterThanOrEqualToDate":
											criteria.withRegistrationDate().thatIsLaterThanOrEqualTo(attributeValue);
											break;
										case "thatIsEarlierThanOrEqualToDate":
											criteria.withRegistrationDate().thatIsEarlierThanOrEqualTo(attributeValue);
											break;
									}
								} else {
									criteria.withRegistrationDate().thatEquals(attributeValue);
								}
								break;
							case "MODIFIER":
								if(comparisonOperator) {
									switch(comparisonOperator) {
										case "thatEqualsUserId":
											criteria.withModifier().withUserId().thatEquals(attributeValue);
											break;
										case "thatContainsFirstName":
											criteria.withModifier().withFirstName().thatContains(attributeValue);
											break;
										case "thatContainsLastName":
											criteria.withModifier().withLastName().thatContains(attributeValue);
											break;
									}
								}
								break;
							case "MODIFICATION_DATE": //Must be a string object with format 2009-08-18
								if(comparisonOperator) {
									switch(comparisonOperator) {
										case "thatEqualsDate":
											criteria.withModificationDate().thatEquals(attributeValue);
											break;
										case "thatIsLaterThanOrEqualToDate":
											criteria.withModificationDate().thatIsLaterThanOrEqualTo(attributeValue);
											break;
										case "thatIsEarlierThanOrEqualToDate":
											criteria.withModificationDate().thatIsEarlierThanOrEqualTo(attributeValue);
											break;
									}
								} else {
									criteria.withModificationDate().thatEquals(attributeValue);
								}
								break;
							case "SAMPLE_TYPE":
							case "EXPERIMENT_TYPE":
							case "DATA_SET_TYPE":
								criteria.withType().withCode().thatEquals(attributeValue);
								break;
							//Only Sample
							case "SPACE":
								criteria.withSpace().withCode().thatEquals(attributeValue);
								break;
							//Only Experiment
							case "PROJECT":
								criteria.withProject().withCode().thatEquals(attributeValue);
								break;
							case "PROJECT_PERM_ID":
								criteria.withProject().withPermId().thatEquals(attributeValue);
								break;
							case "PROJECT_SPACE":
								criteria.withProject().withSpace().withCode().thatEquals(attributeValue);
								break;
						}
					}
				
					switch(fieldType) {
						case "All":
							if(fieldValue !== "*") {
								searchCriteria.withAnyField().thatContains(fieldValue);
							}
							break;
						case "Property":
							setPropertyCriteria(setOperator(searchCriteria, advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
							break;
						case "Attribute":
							setAttributeCriteria(setOperator(searchCriteria, advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
							break;
						case "Property/Attribute":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(setOperator(searchCriteria, advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "ATTR":
									setAttributeCriteria(setOperator(searchCriteria, advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
							}
							break;
						case "Sample":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(setOperator(searchCriteria.withSample(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "ATTR":
									setAttributeCriteria(setOperator(searchCriteria.withSample(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "NULL":
									searchCriteria.withoutSample();
									break;
							}
							break;
						case "Experiment":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(setOperator(searchCriteria.withExperiment(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "ATTR":
									setAttributeCriteria(setOperator(searchCriteria.withExperiment(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "NULL":
									searchCriteria.withoutExperiment();
									break;
							}
							break;
						case "Parent":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(setOperator(searchCriteria.withParents(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "ATTR":
									setAttributeCriteria(setOperator(searchCriteria.withParents(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
							}
							break;
						case "Children":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(setOperator(searchCriteria.withChildren(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
								case "ATTR":
									setAttributeCriteria(setOperator(searchCriteria.withChildren(),advancedSearchCriteria.logicalOperator), fieldName, fieldValue, fieldOperator);
									break;
							}
							break;
					}
				}
			
				//
				// Fix For broken equals PART 1
				// Currently the back-end matches whole words instead doing a standard EQUALS
				// This fixes some most used cases for the storage system, but other use cases that use subcriterias can fail
				//
				var hackFixForBrokenEquals = [];
				if(searchCriteria.criteria) {
					for(var cIdx = 0; cIdx < searchCriteria.criteria.length; cIdx++) {
						if(searchCriteria.criteria[cIdx].fieldType === "PROPERTY" && 
								searchCriteria.criteria[cIdx].fieldValue.__proto__["@type"] === "as.dto.common.search.StringEqualToValue") {
							hackFixForBrokenEquals.push({
								propertyCode : searchCriteria.criteria[cIdx].fieldName,
								value : searchCriteria.criteria[cIdx].fieldValue.value
							});
						}
					}
				}
				//
				// Fix For broken equals PART 1 - END
				//
				callback(searchCriteria, fetchOptions, hackFixForBrokenEquals);

			} catch(exception) {
				Util.showError(exception.name + ": " + exception.message);
			}
		});
	}

    this.getResultsWithBrokenEqualsFix = function(hackFixForBrokenEquals, results, operator) {
        if (!operator) {
            operator = "AND";
        }

        if(hackFixForBrokenEquals.length > 0 && results) {
            var filteredResults = [];
            var resultsValid = new Array(results.length);
            for (var vIdx = 0; vIdx < resultsValid.length; vIdx++) {
                switch(operator) {
                    case "AND":
                        resultsValid[vIdx] = true;
                        break;
                    case "OR":
                        resultsValid[vIdx] = false;
                        break;
                }
            }

            for(var rIdx = 0; rIdx < results.length; rIdx++) {
        	    var result = results[rIdx];
        	    for(var fIdx = 0; fIdx < hackFixForBrokenEquals.length; fIdx++) {
        		    if(	result &&
        		        result.properties &&
        			    result.properties[hackFixForBrokenEquals[fIdx].propertyCode] === hackFixForBrokenEquals[fIdx].value) {
        			    switch(operator) {
                            case "AND":
                                resultsValid[rIdx] = resultsValid[rIdx] && true;
                                break;
                            case "OR":
                                resultsValid[rIdx] = resultsValid[rIdx] || true;
                                break;
                        }
        		    } else {
                        switch(operator) {
                            case "AND":
                                resultsValid[rIdx] = resultsValid[rIdx] && false;
                                break;
                            case "OR":
                                resultsValid[rIdx] = resultsValid[rIdx] || false;
                                break;
                        }
        	        }
                }
            }

            for(var rIdx = 0; rIdx < results.length; rIdx++) {
        	    if(resultsValid[rIdx]) {
        	        filteredResults.push(results[rIdx]);
        	    }
            }

            results = filteredResults;
        }

        return results;
    }

	this.searchForEntityAdvanced = function(advancedSearchCriteria, advancedFetchOptions, callback, criteriaClass, fetchOptionsClass, searchMethodName) {
		var _this = this;
		var searchFunction = function(searchCriteria, fetchOptions, hackFixForBrokenEquals) {
			mainController.openbisV3[searchMethodName](searchCriteria, fetchOptions)
			.done(function(apiResults) {
				apiResults.objects = _this.getResultsWithBrokenEqualsFix(hackFixForBrokenEquals, apiResults.objects, advancedSearchCriteria.logicalOperator);
				callback(apiResults);
			})
			.fail(function(result) {
				Util.showFailedServerCallError(result);
			});
		}

		this.getSearchCriteriaAndFetchOptionsForEntitySearch(advancedSearchCriteria, advancedFetchOptions, searchFunction, criteriaClass, fetchOptionsClass);
	}

	//
	// Search Samples
	//
	
	this.getV3SamplesAsV1 = function(v3Samples, alreadyConverted) {
		if(!alreadyConverted) {
			alreadyConverted = {};
		}
		var v1Samples = [];
		for(var sIdx = 0; sIdx < v3Samples.length; sIdx++) {
			var permId = (v3Samples[sIdx].permId)?v3Samples[sIdx].permId.permId:null;
			if(alreadyConverted[permId]) {
				v1Samples.push(alreadyConverted[permId]);
			} else {
				v1Samples.push(this.getV3SampleAsV1(v3Samples[sIdx], alreadyConverted));
			}
		}
		return v1Samples;
	}
	
	this.getV3SampleAsV1 = function(v3Sample, alreadyConverted) {
		if(!alreadyConverted) {
			alreadyConverted = {};
		}
		
		var CONST_UNSUPPORTED_NUMBER = -1;
		var CONST_UNSUPPORTED_OBJ = null;
		var CONST_UNSUPPORTED_BOOL = false;
		
		var v1Sample = {};
		v1Sample["@type"] = "Sample";
		v1Sample["@id"] = CONST_UNSUPPORTED_NUMBER;
		v1Sample["spaceCode"] = (v3Sample.space)?v3Sample.space.code:null;
		v1Sample["permId"] = (v3Sample.permId)?v3Sample.permId.permId:null;
		v1Sample["code"] = v3Sample.code;
		v1Sample["identifier"] = (v3Sample.identifier)?v3Sample.identifier.identifier:null;
		v1Sample["projectCode"] = (v3Sample.project) ? v3Sample.project.code : null;
		v1Sample["experimentIdentifierOrNull"] = (v3Sample.experiment)?v3Sample.experiment.identifier.identifier:null;
		v1Sample["sampleTypeCode"] = (v3Sample.type)?v3Sample.type.code:null;
		v1Sample["properties"] = v3Sample.properties;
		
		v1Sample["registrationDetails"] = {};
		v1Sample["registrationDetails"]["@type"] = "EntityRegistrationDetails";
		v1Sample["registrationDetails"]["@id"] = CONST_UNSUPPORTED_NUMBER;
		v1Sample["registrationDetails"]["userFirstName"] = (v3Sample.registrator)?v3Sample.registrator.firstName:null;
		v1Sample["registrationDetails"]["userLastName"] = (v3Sample.registrator)?v3Sample.registrator.lastName:null;
		v1Sample["registrationDetails"]["userEmail"] = (v3Sample.registrator)?v3Sample.registrator.email:null;
		v1Sample["registrationDetails"]["userId"] = (v3Sample.registrator)?v3Sample.registrator.userId:null;
		v1Sample["registrationDetails"]["modifierFirstName"]  = (v3Sample.modifier)?v3Sample.modifier.firstName:null;
		v1Sample["registrationDetails"]["modifierLastName"] = (v3Sample.modifier)?v3Sample.modifier.lastName:null;
		v1Sample["registrationDetails"]["modifierEmail"] = (v3Sample.modifier)?v3Sample.modifier.email:null;
		v1Sample["registrationDetails"]["modifierUserId"] = (v3Sample.modifier)?v3Sample.modifier.userId:null;
		v1Sample["registrationDetails"]["registrationDate"] = v3Sample.registrationDate;
		v1Sample["registrationDetails"]["modificationDate"] = v3Sample.modificationDate;
		v1Sample["registrationDetails"]["accessTimestamp"] = CONST_UNSUPPORTED_OBJ;
		
		alreadyConverted[v1Sample["permId"]] = v1Sample;
		
		v1Sample["parents"] = null;
		if(v3Sample.parents) {
			v1Sample["parents"] = this.getV3SamplesAsV1(v3Sample.parents, alreadyConverted);
		}
		v1Sample["children"] = null;
		if(v3Sample.children) {
			v1Sample["children"] = this.getV3SamplesAsV1(v3Sample.children, alreadyConverted);
		} 
		
		v1Sample["stub"] = CONST_UNSUPPORTED_BOOL;
		v1Sample["metaprojects"] = CONST_UNSUPPORTED_OBJ;
		v1Sample["sampleTypeId"] = CONST_UNSUPPORTED_NUMBER;
		v1Sample["id"] = CONST_UNSUPPORTED_NUMBER;
		
		return v1Sample;
	}
	
	this.searchSamplesV3DSS = function(fechOptions, callbackFunction)
	{
		var localReference = this;
		fechOptions["method"] = "searchSamples";
		fechOptions["custom"] = profile.searchSamplesUsingV3OnDropboxRunCustom;
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(), fechOptions, function(result) {
			if(result && result.result && result.result.rows[0][0].value === "OK") {
				var json = result.result.rows[0][2].value;
				var jsonParsed = JSON.parse(json);
				require(["util/Json"], function(Json){
					Json.fromJson("SearchResult", jsonParsed).done(function(data) {
						var v3Samples = data.objects;
						var samplesAsV1 = localReference.getV3SamplesAsV1(v3Samples);
						callbackFunction(samplesAsV1);
					}).fail(function() {
						alert("V3 dropbox search failed to be parsed.");
					});
				});
			} else {
				alert("V3 dropbox search failed to execute.");
			}
		});
	}
	
	this.searchSamplesV1 = function(fechOptions, callbackFunction)
	{	
		//Text Search
		var anyFieldContains = fechOptions["anyFieldContains"];
		
		//Attributes
		var samplePermId = fechOptions["samplePermId"];
		var withExperimentWithProjectPermId = fechOptions["withExperimentWithProjectPermId"];
		var sampleIdentifier = fechOptions["sampleIdentifier"];
		var sampleCode = fechOptions["sampleCode"];
		var sampleTypeCode = fechOptions["sampleTypeCode"];
		var registrationDate = fechOptions["registrationDate"];
		var modificationDate = fechOptions["modificationDate"];
		
		//Properties
		var properyKeyValueList = fechOptions["properyKeyValueList"];
		
		//Sub Queries
		var sampleExperimentIdentifier = fechOptions["sampleExperimentIdentifier"];
		var sampleContainerPermId = fechOptions["sampleContainerPermId"];
		
		//Hierarchy Options
		var withProperties = fechOptions["withProperties"];
		var withParents = fechOptions["withParents"];
		var withChildren = fechOptions["withChildren"];
		var withAncestors = fechOptions["withAncestors"];
		var withDescendants = fechOptions["withDescendants"];
		
		var matchClauses = [];
		
		// Free Text
		if(anyFieldContains) {
			var words = anyFieldContains.split(" ");
			for(var sIdx = 0; sIdx < words.length; sIdx++) {
				var word = words[sIdx];
				if(word) {
					matchClauses.push({
						"@type": "AnyFieldMatchClause",
						fieldType: "ANY_FIELD",
						desiredValue: "*" + word.trim() + "*"
					});
				}
			}
		}
		
		// Attributes
		if(sampleIdentifier) {
			throw "Unexpected operation exception : v1 search by sampleIdentifier and samplePermId removed";
		}
		
		if(samplePermId) {
			matchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "PERM_ID",
				desiredValue : samplePermId 
			});
		}
		
		if(sampleCode) {
			matchClauses.push({
			  	"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "CODE",
				desiredValue : sampleCode 
			});
		}
		
		if(sampleTypeCode) {
			matchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "TYPE",
				desiredValue : sampleTypeCode
			});
		}
		
		if(registrationDate) {
			matchClauses.push({
				"@type":"TimeAttributeMatchClause",
				fieldType : "ATTRIBUTE",
				fieldCode : "REGISTRATION_DATE",
				desiredValue : registrationDate,
				compareMode : "EQUALS",
				timeZone : "+1",
				attribute : "REGISTRATION_DATE"
			});
		}
		
		if(modificationDate) {
			matchClauses.push({
				"@type":"TimeAttributeMatchClause",
				fieldType : "ATTRIBUTE",
				fieldCode : "MODIFICATION_DATE",
				desiredValue : modificationDate,
				compareMode : "EQUALS",
				timeZone : "+1",
				attribute : "MODIFICATION_DATE"
			});
		}
		
		//Properties
		if(properyKeyValueList) {
			for(var kvIdx = 0; kvIdx < properyKeyValueList.length; kvIdx++) {
				var properyKeyValue = properyKeyValueList[kvIdx];
				for(properyTypeCode in properyKeyValue) {
					matchClauses.push(
							{	
								"@type":"PropertyMatchClause",
								fieldType : "PROPERTY",
								//fieldCode : properyTypeCode,
								propertyCode : properyTypeCode,
								desiredValue : "\"" + properyKeyValue[properyTypeCode] + "\"",
								compareMode : "EQUALS"
							}
						);
				}
			}
		}
		
		//Sub Queries
		var subCriterias = [];
		
		if(withExperimentWithProjectPermId) {
			subCriterias.push({
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "EXPERIMENT",	
				"criteria" : {
					matchClauses : [{
							"@type":"AttributeMatchClause",
							fieldType : "ATTRIBUTE",			
							attribute : "PROJECT_PERM_ID",
							desiredValue : withExperimentWithProjectPermId
					}],
					operator : "MATCH_ALL_CLAUSES"
				}
			});
		}
		
		if(sampleExperimentIdentifier) {
			subCriterias.push({
					"@type" : "SearchSubCriteria",
					"targetEntityKind" : "EXPERIMENT",	
					"criteria" : {
						matchClauses : [{
								"@type":"AttributeMatchClause",
								fieldType : "ATTRIBUTE",			
								attribute : "SPACE",
								desiredValue : IdentifierUtil.getSpaceCodeFromIdentifier(sampleExperimentIdentifier)
							},{
								"@type":"AttributeMatchClause",
								fieldType : "ATTRIBUTE",			
								attribute : "PROJECT",
								desiredValue : IdentifierUtil.getProjectCodeFromExperimentIdentifier(sampleExperimentIdentifier)
							}, {
								"@type":"AttributeMatchClause",
								fieldType : "ATTRIBUTE",			
								attribute : "CODE",
								desiredValue : IdentifierUtil.getCodeFromIdentifier(sampleExperimentIdentifier)
							}],
						operator : "MATCH_ALL_CLAUSES"
				}
			});
		}
		
		if(sampleContainerPermId) {
			subCriterias.push({
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "SAMPLE_CONTAINER",
				"criteria" : {
					matchClauses : [{
							"@type":"AttributeMatchClause",
							fieldType : "ATTRIBUTE",			
							attribute : "PERM_ID",
							desiredValue : sampleContainerPermId
						}],
					operator : "MATCH_ALL_CLAUSES"
				}
			});
		}
		
		var sampleCriteria = {
			matchClauses : matchClauses,
			subCriterias : subCriterias,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		//Hierarchy Options
		var options = [];
		
		if(withProperties) {
			options.push("PROPERTIES");
		}
		
		if(withAncestors) {
			options.push("ANCESTORS");
		}
		
		if(withDescendants) {
			options.push("DESCENDANTS");
		}
		
		if(withParents) {
			options.push("PARENTS");
		}
		
		if(withChildren) {
			options.push("CHILDREN");
		}
		
		var localReference = this;
		
		//
		// Collection Rules for broken equals Fix
		// Currently the back-end matches whole words instead doing a standard EQUALS
		// This fixes some most used cases for the storage system, but other use cases that use subcriterias can fail
		//
		var hackFixForBrokenEquals = [];
		if(sampleCriteria.matchClauses) {
			for(var cIdx = 0; cIdx < sampleCriteria.matchClauses.length; cIdx++) {
				if(sampleCriteria.matchClauses[cIdx]["@type"] === "PropertyMatchClause" &&
						sampleCriteria.matchClauses[cIdx]["compareMode"] === "EQUALS") {
					hackFixForBrokenEquals.push({
						propertyCode : sampleCriteria.matchClauses[cIdx].propertyCode,
						value : sampleCriteria.matchClauses[cIdx].desiredValue.substring(1,sampleCriteria.matchClauses[cIdx].desiredValue.length-1)
					});
				}
			}
		}
		//
		// Fix For broken equals PART 1 - END
		//
		var _this = this;
		this.openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, options, function(data) {
			var results = localReference.getInitializedSamples(data.result);
			results = _this.getResultsWithBrokenEqualsFix(hackFixForBrokenEquals, results, "AND");
			callbackFunction(results);
		});
	}
	
	this.searchSamples = function(fechOptions, callbackFunction)
	{
		if(profile.searchSamplesUsingV3OnDropbox) {
			this.searchSamplesV3DSS(fechOptions, callbackFunction);
		} else if(fechOptions["sampleIdentifier"] || fechOptions["samplePermId"]) {
			this.searchSamplesV1replacement(fechOptions, callbackFunction);
		} else {
			this.searchSamplesV1(fechOptions, callbackFunction);
		}
	}
	
	this.searchSamplesV1replacement = function(fechOptions, callbackFunction)
	{
		var _this = this;
		require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/id/SampleIdentifier", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
        function(SamplePermId, SampleIdentifier, SampleFetchOptions) {
            //
            // Main entity Block
            //
            var fetchOptions = new SampleFetchOptions();
            fetchOptions.withSpace();
            fetchOptions.withType();
            fetchOptions.withRegistrator();
            fetchOptions.withModifier();
            fetchOptions.withProject();
            fetchOptions.withExperiment();
            if(fechOptions["withProperties"]) {
            		fetchOptions.withProperties();
            }
            
            
            //
            // Parents/Children Block
            //
            var parentfetchOptions = jQuery.extend(true, {}, fetchOptions);
			var childrenfetchOptions = jQuery.extend(true, {}, fetchOptions);
			
            if(fechOptions["withAncestors"]) {
             	fechOptions["withParents"] = true;
            }
            if(fechOptions["withDescendants"]) {
            		fechOptions["withChildren"] = true;
            }
            if(fechOptions["withParents"]) {
				fetchOptions.withParentsUsing(parentfetchOptions);
            }
            if(fechOptions["withChildren"]) {
            		fetchOptions.withChildrenUsing(childrenfetchOptions);
            }
            if(fechOptions["withAncestors"]) {
             	parentfetchOptions.withParentsUsing(parentfetchOptions);
            }
            if(fechOptions["withDescendants"]) {
            		childrenfetchOptions.withChildrenUsing(childrenfetchOptions);
            }
            
            var id = null;
            if(fechOptions["samplePermId"]) {
            		id = new SamplePermId(fechOptions["samplePermId"]);
            }
            if(fechOptions["sampleIdentifier"]) {
            		id = new SampleIdentifier(fechOptions["sampleIdentifier"]);
            }
            
            mainController.openbisV3.getSamples([id], fetchOptions).done(function(map) {
                var samples = Util.mapValuesToList(map);
                callbackFunction(_this.getV3SamplesAsV1(samples));
            }).fail(function(result) {
				Util.showFailedServerCallError(result);
				callbackFunction(false);
			});
        });
	}
	
	this.searchWithUniqueId = function(samplePermId, callbackFunction)
	{	
		this.searchSamples({
			"samplePermId" : samplePermId,
			"withProperties" : true,
			"withParents" : true,
			"withChildren" : true
		}, callbackFunction);
	}
	
	this.searchWithUniqueIdCompleteTree = function(samplePermId, callbackFunction)
	{	
		this.searchSamples({
			"samplePermId" : samplePermId,
			"withProperties" : true,
			"withAncestors" : true,
			"withDescendants" : true
		}, callbackFunction);
	}
	
	this.searchByType = function(sampleType, callbackFunction)
	{
		this.searchSamples({
			"sampleTypeCode" : sampleType,
			"withProperties" : true
		}, callbackFunction);
	}
	
	this.searchByTypeWithParents = function(sampleType, callbackFunction)
	{
		this.searchSamples({
			"sampleTypeCode" : sampleType,
			"withProperties" : true,
			"withParents" : true,
		}, callbackFunction);
	}
	
	this.searchWithType = function(sampleType, sampleCode, includeAncestorsAndDescendants, callbackFunction)
	{
		this.searchSamples({
			"sampleTypeCode" : sampleType,
			"sampleCode" : sampleCode,
			"withProperties" : true,
			"withAncestors" : includeAncestorsAndDescendants,
			"withDescendants" : includeAncestorsAndDescendants
		}, callbackFunction);
	}
	
	this.searchWithExperiment = function(experimentIdentifier, projectPermId, properyKeyValueList, callbackFunction)
	{	
		this.searchSamples({
			"sampleExperimentIdentifier" : experimentIdentifier,
			"withExperimentWithProjectPermId" : projectPermId,
			"withProperties" : true,
			"withParents" : true,
			"properyKeyValueList" : properyKeyValueList
		}, callbackFunction);
	}

	this.searchWithProperties = function(propertyTypeCodes, propertyValues, callbackFunction, isComplete, withParents)
	{
		var advancedSearchCriteria = { rules : {} };
		for(var i = 0; i < propertyTypeCodes.length ;i++) {
			var propertyTypeCode = propertyTypeCodes[i];			
			var propertyTypeValue = propertyValues[i];
			advancedSearchCriteria.rules[Util.guid()] = { type : "Property", name : "PROP." + propertyTypeCode, value : propertyTypeValue, operator : "thatEqualsString" }
		}
		var advancedFetchOptions = {
			"withProperties" : true,
			"withAncestors" : isComplete,
			"withDescendants" : isComplete,
			"withParents" : withParents,
			"withChildren" : false
		}
		
		var _this = this;
		this.searchForSamplesAdvanced(advancedSearchCriteria, advancedFetchOptions, function(results) {
			callbackFunction(_this.getV3SamplesAsV1(results.objects));
		});
	}
	
	this.searchWithIdentifiers = function(sampleIdentifiers, callbackFunction)
	{
		var _this = this;
		var searchResults = [];
		var searchForIdentifiers = jQuery.extend(true, [], sampleIdentifiers);
		
		var searchNext = function() {
			if(searchForIdentifiers.length === 0) {
				callbackFunction(searchResults);
			} else {
				var next = searchForIdentifiers.pop();
				searchFunction(next);
			}
		}
		
		var searchFunction = function(sampleIdentifier) {
			_this.searchSamples({
				"withProperties" : true,
				"withParents" : true,
				"withChildren" : true,
				"sampleIdentifier" : sampleIdentifier
			}, function(samples) {
				samples.forEach(function(sample) {
					searchResults.push(sample);
				});
				searchNext();
			});
		}
		
		searchNext();
	}
	
	this.searchContained = function(permId, callbackFunction) {
		this.searchSamples({
			"sampleContainerPermId" : permId,
			"withProperties" : true,
			"withParents" : true,
			"withChildren" : true
		}, callbackFunction);
	}
	
	this.getInitializedSamples = function(result) {
		
		//
		// Fill Map that uses as key the sample @id and value the sample object 
		//
		var samplesById = {};
		
		function storeSamplesById(originalSample)
		{
			var stack = [originalSample];
			
			var referredSample = null;
			while (referredSample = stack.pop()) {
				if (isNaN(referredSample)) {
					samplesById[referredSample["@id"]] = referredSample;
					if (referredSample.parents) {
						for(var i = 0, len = referredSample.parents.length; i < len; ++i) {
							stack.push(referredSample.parents[i]);
						}
					}
					if (referredSample.children) {
						for(var i = 0, len = referredSample.children.length; i < len; ++i) {
							stack.push(referredSample.children[i]);
						}
					}
				}					
			}
		}
		
		for(var i = 0; i < result.length; i++) {
			var sampleOrId = result[i];
			storeSamplesById(sampleOrId);
		}
		
		//
		// Fix Result List
		//
		var visitedSamples = {};
		function fixSamples(result)
		{
			for(var i = 0; i < result.length; i++)
			{
				var sampleOrId = result[i];
				
				if (isNaN(sampleOrId))
				{
					sampleOrId = samplesById[sampleOrId["@id"]];
				} else
				{
					sampleOrId = samplesById[sampleOrId]; 
				}
				result[i] = sampleOrId;
				if(visitedSamples[sampleOrId.permId]) {
					continue;
				} else {
					visitedSamples[sampleOrId.permId] = true;
				}
				
				//Fill Parents
				if(sampleOrId.parents) {
					for(var j = 0; j < sampleOrId.parents.length; j++) {
						var parentOrId = sampleOrId.parents[j];
						if(!isNaN(parentOrId)) { //If is an Id get the reference
							sampleOrId.parents[j] = samplesById[parentOrId];
						}
					}
					fixSamples(sampleOrId.parents);
				}
				
				//Fill Children
				if(sampleOrId.children) {
					for(var j = 0; j < sampleOrId.children.length; j++) {
						var childOrId = sampleOrId.children[j];
						if(!isNaN(childOrId)) { //If is an Id get the reference
							sampleOrId.children[j] = samplesById[childOrId];
						}
					}
					fixSamples(sampleOrId.children);
				}
			}
		}
		
		fixSamples(result);
		
		return result;
	}
	
	//
	// Global Search
	//

	this.getSearchCriteriaAndFetchOptionsForGlobalSearch = function(freeText, advancedFetchOptions, callbackFunction) {
		require(['as/dto/global/search/GlobalSearchCriteria', 
		         'as/dto/global/fetchoptions/GlobalSearchObjectFetchOptions'], 
		         function(GlobalSearchCriteria, GlobalSearchObjectFetchOptions){
			var searchCriteria = new GlobalSearchCriteria();
			searchCriteria.withText().thatContains(freeText.toLowerCase().trim());
			searchCriteria.withOperator("AND");

			var fetchOptions = new GlobalSearchObjectFetchOptions();
			var sampleFetchOptions = fetchOptions.withSample();
			sampleFetchOptions.withSpace();
			sampleFetchOptions.withType();
			sampleFetchOptions.withRegistrator();
			sampleFetchOptions.withModifier();
			sampleFetchOptions.withExperiment();
			sampleFetchOptions.withProperties();
			
			var experimentFetchOptions = fetchOptions.withExperiment();
			experimentFetchOptions.withType();
			experimentFetchOptions.withRegistrator();
			experimentFetchOptions.withModifier();
			experimentFetchOptions.withProperties();
			
			var dataSetFetchOptions = fetchOptions.withDataSet();
			dataSetFetchOptions.withType();
			dataSetFetchOptions.withRegistrator();
			dataSetFetchOptions.withModifier();
			dataSetFetchOptions.withProperties();
			
			if(advancedFetchOptions && advancedFetchOptions.cache) {
				fetchOptions.cacheMode(advancedFetchOptions.cache);
			}
			
			if(advancedFetchOptions && 
					advancedFetchOptions.count != null &&
					advancedFetchOptions.count != undefined && 
					advancedFetchOptions.from != null &&
					advancedFetchOptions.from != undefined) {
				fetchOptions.from(advancedFetchOptions.from);
				fetchOptions.count(advancedFetchOptions.count);
			}

			callbackFunction(searchCriteria, fetchOptions);
		});
	}

	this.searchGlobally = function(freeText, advancedFetchOptions, callbackFunction)
	{
		this.getSearchCriteriaAndFetchOptionsForGlobalSearch(freeText, advancedFetchOptions, function(searchCriteria, fetchOptions)
		{
			mainController.openbisV3.searchGlobally(searchCriteria, fetchOptions).done(function(results) {
				callbackFunction(results);
			}).fail(function(error) {
				Util.showError("Call failed to server: " + JSON.stringify(error));
				Util.unblockUI();
			});
		});
	}
	
	//
	// Legacy Global Search
	//
	this.searchWithText = function(freeText, callbackFunction)
	{
		var _this = this;
		var regEx = /\d{4}-\d{2}-\d{2}/g;
		var match = freeText.match(regEx);
		
		if(match && match.length === 1) { //Search With Date Mode, we merge results with dates found on registration and modification fields what is slow for large number of entities
			this.searchSamples(this._getCriteriaWithDate(freeText, true, false), function(samples1) {
				_this.searchSamples(_this._getCriteriaWithDate(freeText, false, true), function(samples2) {
					_this.searchSamples(_this._getCriteriaWithDate(freeText, false, false), function(samples3) {
						var results = samples1.concat(samples2).concat(samples3).uniqueOBISEntity();
						callbackFunction(results);
					});
				});
			});
		} else if(match && match.length > 1) {
			Util.showError("Search only supports one date at a time!");
			callbackFunction([]);
		} else { //Normal Search
			this.searchSamples(this._getCriteriaWithDate(freeText, false, false), function(samples) {
				callbackFunction(samples);
			});
			callbackFunction([]);
		}
	}
	
	this._getCriteriaWithDate = function(freeText, isRegistrationDate, isModificationDate) {
		//Find dates on string and delete them to use them differently on the search
		var regEx = /\d{4}-\d{2}-\d{2}/g;
		var match = freeText.match(regEx);
		freeText = freeText.replace(regEx, "");
		if(!isRegistrationDate && !isModificationDate && match && match.length > 0) {
			for(var mIdx = 0; mIdx < match.length; mIdx++) {
				freeText += " " + match[mIdx].replace(/-/g, "");
			}
		}
		
		//Build Search
		var sampleCriteria = {
			"withProperties" : true
		};
		
		if(freeText) {
			sampleCriteria["anyFieldContains"] = freeText;
		}
		
		if(match && match.length > 0) {
			for(var mIdx = 0; mIdx < match.length; mIdx++) {
				if(isRegistrationDate) {
					sampleCriteria["registrationDate"] = match[mIdx];
				}
				
				if(isModificationDate) {
					sampleCriteria["modificationDate"] = match[mIdx];
				}
			}
		}
		
		return sampleCriteria;
	}
	
	//
	// Search Domains
	//
	this.listSearchDomains = function(callbackFunction) {
		if(this.openbisServer.listAvailableSearchDomains) {
			this.openbisServer.listAvailableSearchDomains(callbackFunction);
		} else {
			callbackFunction();
		}
	}
	
	this.searchOnSearchDomain = function(preferredSearchDomainOrNull, searchText, callbackFunction) {
		
		//TO-DO: For testing please put codes that exist in your database and you can access, the rest leave it as it is, when done just pass null to the function.
		var optionalParametersOrNull = {
				"SEQ-1" : JSON.stringify({
					"searchDomain" : "Echo database",
					"dataSetCode" : "20141023091944740-99",
					"pathInDataSet" : "PATH-1",
					"sequenceIdentifier" : "ID-1",
					"positionInSequence" : "1"
				}),
				"SEQ-2" : JSON.stringify({
					"searchDomain" : "Echo database",
					"dataSetCode" : "20141023091930970-98",
					"pathInDataSet" : "PATH-2",
					"sequenceIdentifier" : "ID-2",
					"positionInSequence" : "2"
				})
		}
		
		this.openbisServer.searchOnSearchDomain(preferredSearchDomainOrNull, searchText, null, callbackFunction);
	}

	//
	// V3 Functions
	//

    this.createPermIdStrings = function(count, callbackFunction) {
        mainController.openbisV3.createPermIdStrings(count)
        .done(function(result) {
            callbackFunction(result);
        }).fail(function(result) {
            Util.showFailedServerCallError(result);
        });
    }

	this.getSpace = function(spaceIdentifier, callbackFunction) {
		require(["as/dto/space/id/SpacePermId", "as/dto/space/fetchoptions/SpaceFetchOptions"], 
			function(SpacePermId, SpaceFetchOptions) {
				mainController.openbisV3.getSpaces([new SpacePermId(spaceIdentifier)], new SpaceFetchOptions()).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.getProject = function(projectIdentifier, callbackFunction) {
		require(["as/dto/project/id/ProjectIdentifier", "as/dto/project/fetchoptions/ProjectFetchOptions"],
		  function(ProjectIdentifier, ProjectFetchOptions) {
				var projectId = new ProjectIdentifier(projectIdentifier);
				var fetchOptions = new ProjectFetchOptions();

				mainController.openbisV3.getProjects([projectId], fetchOptions).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.searchSamplesV3 = function(sampleType, callbackFunction) {
		require(["as/dto/sample/search/SampleSearchCriteria", "as/dto/sample/fetchoptions/SampleFetchOptions"],
				function(SampleSearchCriteria, SampleFetchOptions) {
			var searchCriteria = new SampleSearchCriteria();
			searchCriteria.withType().withCode().thatEquals(sampleType);
			var fetchOptions = new SampleFetchOptions();
			fetchOptions.withProperties();
			fetchOptions.withExperiment().withProject().withSpace();
			fetchOptions.withRegistrator();
			fetchOptions.sortBy().modificationDate().desc();
			mainController.openbisV3.searchSamples(searchCriteria, fetchOptions).done(function(result) {
				callbackFunction(result);
			}).fail(function(result) {
				Util.showFailedServerCallError(result);
				callbackFunction(false);
			});
		});
	}

	this.getSamples = function(permIds, callbackFunction) {
		require(["as/dto/sample/id/SamplePermId", "as/dto/sample/fetchoptions/SampleFetchOptions"],
			function(SamplePermId, SampleFetchOptions) {
				var samplePermIds = permIds.map(function(permId) { return new SamplePermId(permId) });
				var fetchOptions = new SampleFetchOptions();
				fetchOptions.withProperties();
				fetchOptions.withExperiment().withProject().withSpace();
				fetchOptions.withRegistrator();
				fetchOptions.sortBy().modificationDate().desc();
				mainController.openbisV3.getSamples(samplePermIds, fetchOptions).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.createSample = function(sampleType, experiment, properties, callbackFunction) {
		require(["as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", 
			"as/dto/experiment/id/ExperimentPermId", "as/dto/space/id/SpacePermId"], 
				function(SampleCreation, EntityTypePermId, ExperimentPermId, SpacePermId) {
			var experimentPermId = experiment.permId.permId;
			var space = experiment.project.space.code;
			var sampleCreation = new SampleCreation();
			sampleCreation.setTypeId(new EntityTypePermId(sampleType));
			sampleCreation.setExperimentId(new ExperimentPermId(experimentPermId));
			sampleCreation.setSpaceId(new SpacePermId(space));
			if (properties) {
				for (var property in properties) {
					if (properties.hasOwnProperty(property)) {
						sampleCreation.setProperty(property, properties[property]);
					}
				}
			}

			mainController.openbisV3.createSamples([sampleCreation]).done(function(result) {
				callbackFunction(result);
			}).fail(function(result) {
				Util.showFailedServerCallError(result);
				callbackFunction(false);
			});
		});
		
	}

	this.createProject = function(space, code, description, callbackFunction) {
		require(["as/dto/project/create/ProjectCreation", "as/dto/space/id/SpacePermId"], 
			function(ProjectCreation, SpacePermId) {
				var projectCreation = new ProjectCreation();
				projectCreation.setSpaceId(new SpacePermId(space));
				projectCreation.setCode(code);
				projectCreation.setDescription(description);

				mainController.openbisV3.createProjects([projectCreation]).done(function(result) {
					callbackFunction(true);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
		});
	}

	this.searchExperimentTypes = function(experimentTypeCode, callbackFunction) {
		require(["as/dto/experiment/search/ExperimentTypeSearchCriteria", "as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions"],
			function(ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions) {
				var searchCriteria = new ExperimentTypeSearchCriteria();
				var fetchOptions = new ExperimentTypeFetchOptions();
				searchCriteria.withCode().thatEquals(experimentTypeCode);

				mainController.openbisV3.searchExperimentTypes(searchCriteria, fetchOptions).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.getExperiments = function(experimentPermIds, callbackFunction) {
		require(["as/dto/experiment/id/ExperimentPermId", "as/dto/experiment/fetchoptions/ExperimentFetchOptions"],
			function(ExperimentPermId, ExperimentFetchOptions) {
				var expPermIds = experimentPermIds.map(function(permId) { return new ExperimentPermId(permId)});
				var fetchOptions = new ExperimentFetchOptions();
				fetchOptions.withProject().withSpace();

				mainController.openbisV3.getExperiments(expPermIds, fetchOptions).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.searchExperiments = function(projectCode, experimentCode, callbackFunction) {
		require(["as/dto/experiment/search/ExperimentSearchCriteria", "as/dto/experiment/fetchoptions/ExperimentFetchOptions"],
			function(ExperimentSearchCriteria, ExperimentFetchOptions) {
				var searchCriteria = new ExperimentSearchCriteria();
				var fetchOptions = new ExperimentFetchOptions();
				searchCriteria.withProject().withCode().thatEquals(projectCode);
				searchCriteria.withCode().thatEquals(experimentCode);
				fetchOptions.withProject().withSpace();

				mainController.openbisV3.searchExperiments(searchCriteria, fetchOptions).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.createExperiment = function(experimentTypePermId, projectIdentifier, code, callbackFunction) {
		require(["as/dto/experiment/create/ExperimentCreation", "as/dto/project/id/ProjectIdentifier", "as/dto/entitytype/id/EntityTypePermId"],
			function(ExperimentCreation, ProjectIdentifier, EntityTypePermId) {
				var experimentCreation = new ExperimentCreation();
				experimentCreation.setTypeId(new EntityTypePermId(experimentTypePermId));
				experimentCreation.setProjectId(new ProjectIdentifier(projectIdentifier));
				experimentCreation.setCode(code);

				mainController.openbisV3.createExperiments([experimentCreation]).done(function(result) {
					callbackFunction(result);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.updateSample = function(sampleV1, callbackFunction) {
		require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SamplePermId"], 
			function(SampleUpdate, SamplePermId) {
				var sampleUpdate = new SampleUpdate();
				sampleUpdate.setSampleId(new SamplePermId(sampleV1.permId));
				for(var propertyCode in sampleV1.properties) {
					sampleUpdate.setProperty(propertyCode, sampleV1.properties[propertyCode]);
				}

				mainController.openbisV3.updateSamples([ sampleUpdate ]).done(function() {
					callbackFunction(true);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	this.updateDataSet = function(dataSetPermId, newPhysicalData, callbackFunction) {
		require([ "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/update/DataSetUpdate", 
			"as/dto/dataset/update/PhysicalDataUpdate", "as/dto/common/update/FieldUpdateValue"],
				function(DataSetPermId, DataSetUpdate, PhysicalDataUpdate, FieldUpdateValue) {

			var update = new DataSetUpdate();
			update.setDataSetId(new DataSetPermId(dataSetPermId));

			if (newPhysicalData) {
				var physicalDataUpdate = new PhysicalDataUpdate();
				for (var property in newPhysicalData) {
					if (newPhysicalData.hasOwnProperty(property)) {
						var setterName = "set" + property[0].toUpperCase() + property.substr(1);
						if (typeof physicalDataUpdate[setterName] === 'function') {
							physicalDataUpdate[setterName](newPhysicalData[property]);
						}
					}
				}
				update.setPhysicalData(physicalDataUpdate);	
			}

			mainController.openbisV3.updateDataSets([update]).done(function(result) {
				callbackFunction(true);
			}).fail(function(result) {
				Util.showFailedServerCallError(result);
				callbackFunction(false);
			});
		});
	}

	this.unarchiveDataSets = function(dataSetPermIds, callbackFunction) {
		require(["as/dto/dataset/id/DataSetPermId", "as/dto/dataset/unarchive/DataSetUnarchiveOptions"], 
			function(DataSetPermId, DataSetUnarchiveOptions) {
				var ids = dataSetPermIds.map(function(dataSetPermId) {
					return new DataSetPermId(dataSetPermId);
				});
				var options = new DataSetUnarchiveOptions();
				mainController.openbisV3.unarchiveDataSets(ids, options).done(function(result) {
					callbackFunction(true);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			});
	}
	
	this.lockDataSet = function(dataSetPermId, lock, callbackFunction) {
		require(["as/dto/dataset/id/DataSetPermId", "as/dto/dataset/lock/DataSetLockOptions", "as/dto/dataset/unlock/DataSetUnlockOptions"], 
				function(DataSetPermId, DataSetLockOptions, DataSetUnlockOptions) {
			var ids = [new DataSetPermId(dataSetPermId)];
			if (lock) {
				mainController.openbisV3.lockDataSets(ids, new DataSetLockOptions()).done(function(result) {
					callbackFunction(true);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			} else {
				mainController.openbisV3.unlockDataSets(ids, new DataSetUnlockOptions()).done(function(result) {
					callbackFunction(true);
				}).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		});
	}

	// errorHandler: optional. if present, it is called instead of showing the error and the callbackFunction is not called
	this.searchRoleAssignments = function(criteriaParams, callbackFunction, errorHandler) {
		require(["as/dto/roleassignment/search/RoleAssignmentSearchCriteria", "as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions"], 
			function(RoleAssignmentSearchCriteria, RoleAssignmentFetchOptions) {
				var criteria = new RoleAssignmentSearchCriteria();

				if (criteriaParams.project) {
					criteria.withProject().withCode().thatEquals(criteriaParams.project);
					if (criteriaParams.space) {
						criteria.withProject().withSpace().withCode().thatEquals(criteriaParams.space);
					}
				} else if (criteriaParams.space) {
					criteria.withSpace().withCode().thatEquals(criteriaParams.space);
				}
				if (criteriaParams.user) {
					criteria.withUser().withUserId().thatEquals(criteriaParams.user);
				}
				var fetchOptions = new RoleAssignmentFetchOptions();
				fetchOptions.withSpace();
				fetchOptions.withProject();
				fetchOptions.withUser();
				fetchOptions.withAuthorizationGroup();

				mainController.openbisV3.searchRoleAssignments(criteria, fetchOptions).done(function(result) {
					callbackFunction(result.objects);
				}).fail(function(result) {
					if (errorHandler) {
						errorHandler(result);
					} else {
						Util.showFailedServerCallError(result);
						callbackFunction(false);
					}
				});
			});
	}

	this.deleteRoleAssignment = function(roleAssignmentTechId, callbackFunction) {
		var userId = this.getUserId()
		require(["as/dto/roleassignment/delete/RoleAssignmentDeletionOptions"], 
			function(RoleAssignmentDeletionOptions) {

				var deleteOptions = new RoleAssignmentDeletionOptions();
				deleteOptions.setReason('deleted by ELN user ' + userId);

				mainController.openbisV3.deleteRoleAssignments([roleAssignmentTechId], deleteOptions).done(function(result) {
					callbackFunction(true, result);
				}).fail(function(result) {
					if (result.message) {
						callbackFunction(false, result.message);
					} else {
						callbackFunction(false, "Call failed to server: " + JSON.stringify(result));
					}
				});
			});
	}

	this.createRoleAssignment = function(creationParams, callbackFunction) {
		require(["as/dto/roleassignment/create/RoleAssignmentCreation", "as/dto/roleassignment/Role", 
				"as/dto/space/id/SpacePermId", "as/dto/project/id/ProjectPermId", "as/dto/person/id/PersonPermId",
				"as/dto/authorizationgroup/id/AuthorizationGroupPermId"],
			function(RoleAssignmentCreation, Role, SpacePermId, ProjectPermId, PersonPermId, AuthorizationGroupPermId) {
				var creation = new RoleAssignmentCreation();
				// user or group
				if (creationParams.user) {
					creation.setUserId(new PersonPermId(creationParams.user));
				} else if (creationParams.group) {
					creation.setAuthorizationGroupId(new AuthorizationGroupPermId(creationParams.group));
				}
				// space or project
				if (creationParams.space) {
					creation.setSpaceId(new SpacePermId(creationParams.space));
				} else if (creationParams.project) {
					creation.setProjectId(new ProjectPermId(creationParams.project));
				}
				// role
				if (creationParams.role == "OBSERVER"){
					creation.setRole(Role.OBSERVER);
				} else if (creationParams.role == "USER") {
					creation.setRole(Role.USER);
				} else if (creationParams.role == "ADMIN") {
					creation.setRole(Role.ADMIN);
				}

				mainController.openbisV3.createRoleAssignments([creation]).done(function(response) {
					if (response.length == 1) {
						callbackFunction(true, response[0]);
					} else {
						callbackFunction(false, "No role assignments created.");
					}
				}).fail(function(result) {
					if (result.message) {
						callbackFunction(false, result.message);
					} else {
						callbackFunction(false, "Call failed to server: " + JSON.stringify(result));
					}
				});
			});
	}

	this.getSessionInformation = function(callbackFunction) {
		mainController.openbisV3.getSessionInformation().done(function(sessionInfo) {
			callbackFunction(sessionInfo);
        }).fail(function(result) {
			Util.showFailedServerCallError(result);
			callbackFunction(false);
		});
	}

	this.searchCustomASServices = function(code, callbackFunction) {
		require(['as/dto/service/search/CustomASServiceSearchCriteria', 'as/dto/service/fetchoptions/CustomASServiceFetchOptions'],
			function(CustomASServiceSearchCriteria, CustomASServiceFetchOptions) {
				var searchCriteria = new CustomASServiceSearchCriteria();
				var fetchOptions = new CustomASServiceFetchOptions();
				searchCriteria.withCode().thatEquals(code);
				mainController.openbisV3.searchCustomASServices(searchCriteria, fetchOptions).done(function(result) {
					callbackFunction(result);
		        }).fail(function(result) {
					Util.showFailedServerCallError(result);
					callbackFunction(false);
				});
			}
		);
	}

	// errorHandler: optional. if present, it is called instead of showing the error and the callbackFunction is not called
	this.customASService = function(parameters, callbackFunction, serviceCode, errorHandler) {
		require([ "as/dto/service/id/CustomASServiceCode", "as/dto/service/CustomASServiceExecutionOptions" ],
			   function(CustomASServiceCode, CustomASServiceExecutionOptions) {
				   var id = new CustomASServiceCode(serviceCode);
				   var options = new CustomASServiceExecutionOptions();
				   
				   if(parameters) {
					   for(key in parameters) {
						   options.withParameter(key, parameters[key]);
					   }
				   }
				   
				   mainController.openbisV3.executeCustomASService(id, options).done(function(result) {
					   callbackFunction(result);
				   }).fail(function(result) {
					    if (errorHandler) {
							errorHandler(result);
					    } else {
							alert("Call failed to server: " + JSON.stringify(result));
						}
				   });
		});
	}

	//
	// search-store Functions
	//

	this.callSearchStoreService = function(parameters, callbackFunction) {
		this.customASService(parameters, callbackFunction, 'search-store', function(errorResult) {
			Util.showError("Call failed to server: " + JSON.stringify(errorResult));
			callbackFunction(false);
		});
	}

	this.saveSearch = function(space, experiment, name, criteriaV3, fetchOptionsV3, criteriaEln, callbackFunction) {
		var parameters = {
			method: 'SAVE',
			name: name,
			searchCriteria: criteriaV3,
			fetchOptions: fetchOptionsV3,
			customData: { 'eln-lims-criteria': criteriaEln },
			spacePermId: space.permId.permId,
			experimentPermId: experiment.permId.permId,
		}
		this.callSearchStoreService(parameters, callbackFunction);
	}

	this.updateSearch = function(permId, criteriaV3, fetchOptionsV3, criteriaEln, callbackFunction) {
		var parameters = {
			method: 'UPDATE',
			permId: permId,
			searchCriteria: criteriaV3,
			fetchOptions: fetchOptionsV3,
			customData: { 'eln-lims-criteria': criteriaEln },
		}
		this.callSearchStoreService(parameters, callbackFunction);
	}

	this.loadSearches = function(callbackFunction) {
		var parameters = {
			method: 'LOAD',
		}
		this.callSearchStoreService(parameters, callbackFunction);
	}

	this.deleteSearch = function(permId, reason, callbackFunction) {
		parameters = {
			method: 'DELETE',
			permId: permId,
			reason: reason,
		}
		this.callSearchStoreService(parameters, callbackFunction);
	}

}
