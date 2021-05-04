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

function AdvancedSearchController(mainController, forceSearch) {
	this._mainController = mainController;
	this._advancedSearchModel = new AdvancedSearchModel(forceSearch);
	this._advancedSearchView = new AdvancedSearchView(this, this._advancedSearchModel);
	this.additionalRules = [];
	this.fetchWithSample = false;
	this.enrichResultsFunction = function(results, callback) {
		callback(results);
	}

	this.init = function(views) {
		var _this = this;
		_this._searchStoreAvailable(function(searchStoreAvailable) {
            _this._mainController.serverFacade.getSetting("GLOBAL_SEARCH_DEFAULT", function(globalSearchDefault) {
                if (globalSearchDefault) {
                    _this._advancedSearchModel.globalSearchDefault = globalSearchDefault;
                }
                _this._advancedSearchModel.searchStoreAvailable = searchStoreAvailable;
                if (searchStoreAvailable) {
                    _this._loadSavedSearches(function() {
                        _this._advancedSearchView.repaint(views);
                    });
                } else {
                    _this._advancedSearchView.repaint(views);
                }
            });
		});
	}

	this.search = function() {
		var criteria = this._advancedSearchModel.criteria;
		if (criteria.cached) {
			criteria.cached = false;
		}
		var numberOfGeneralRules = 0;
		var numberOfRules = 0;
		var numberOfWords = 0;

		for(ruleKey in criteria.rules) {
			var rule = criteria.rules[ruleKey];
			numberOfRules++;
			if(rule.value === null || rule.value === undefined || rule.value.toString().trim() === "" || rule.value.toString().trim() === "*") {
				numberOfGeneralRules++;
			} else {
				numberOfWords += rule.value.toString().trim().split(/\s+/).length;
			}
		}

		var _this = this;
		var trueSearch = function() {
			_this._advancedSearchView.renderResults(criteria);
		}

		if (numberOfRules === numberOfGeneralRules && criteria.entityKind !== 'ALL') {
			var warning1 = "This search query is too broad. This might take a long time and might lead to a very large number of search results. \n Do you want to submit the query anyway?";
			Util.showWarning(warning1, trueSearch);
		} else if (criteria.entityKind === 'ALL_PARTIAL') {
			if (numberOfWords > 3) {
				var warning2 = "This search query contains too many words/rules. This might take a long time and might lead to long waiting time. \n Do you want to submit the query anyway?";
				Util.showWarning(warning2, trueSearch);
			} else {
				trueSearch();
			}
		} else {
			trueSearch();
		}
	}

	this.searchWithPagination = function(criteria, isGlobalSearch) {
		var _this = this;
		return function(callback, options) {
			var callbackForSearch = function(results) {
				var dataList = [];
				for(var rIdx = 0; rIdx < results.objects.length; rIdx++) {
					var entity = results.objects[rIdx];

					var rowData = {};
					if(isGlobalSearch) {
						rowData.matched = entity.match;
						rowData.score = entity.score;

						switch(entity.objectKind) {
							case "SAMPLE":
								entity = entity.sample;
							break;
							case "EXPERIMENT":
								entity = entity.experiment;
							break;
							case "DATA_SET":
								entity = entity.dataSet;
							break;
							case "MATERIAL":
								continue; //Skip materials, they are not supported on the ELN
							break;
						}
					}

					if(!entity) {
						continue;
					}

					//properties
					if(entity["@type"]) {
						rowData.entityKind = entity["@type"].substring(entity["@type"].lastIndexOf(".") + 1, entity["@type"].length);
					}

					if(entity.experiment) {
						rowData.experiment = entity.experiment.code;
					}
					if (entity.sample) {
						rowData.sample = entity.sample.code;
					}

					rowData.entityType = (entity.type)?entity.type.code:"";
					rowData.code =  entity.code;
					rowData.permId = (entity.permId)?entity.permId.permId:"";
					rowData.registrator = (entity.registrator)?entity.registrator.userId:null;
					rowData.registrationDate = (entity.registrationDate)?Util.getFormatedDate(new Date(entity.registrationDate)):null;
					rowData.modifier = (entity.modifier)?entity.modifier.userId:null;
					rowData.modificationDate = (entity.modificationDate)?Util.getFormatedDate(new Date(entity.modificationDate)):null;
					rowData.entityObject = entity;

					if(entity.identifier) {
						rowData.identifier = entity.identifier.identifier;
					}

					for(var propertyCode in entity.properties) {
						rowData[propertyCode] = entity.properties[propertyCode];
					}

					//Add the row data
					dataList.push(rowData);
				}

				_this.enrichResultsFunction(dataList, function(enrichedDataList) {
					callback({
						objects : enrichedDataList,
						totalCount : results.totalCount
					});
				});
				$("#search").removeClass("search-query-searching");
			}

			var fetchOptions = {};

			var optionsSearch = null;
			if(options) {
				fetchOptions.count = options.pageSize;
				fetchOptions.from = options.pageIndex * options.pageSize;
				fetchOptions.minTableInfo = true;
				fetchOptions.withExperiment = true;
				fetchOptions.withParents = false;
				fetchOptions.withChildren = false;
				fetchOptions.withSample = _this.fetchWithSample;
				optionsSearch = options.search;
				// TODO : Unused on the UI, should be added for DataSets
				// fetchOptions.withSample = true;
			}

			if(!criteria.cached || (criteria.cachedSearch !== optionsSearch)) {
				fetchOptions.cache = "RELOAD_AND_CACHE";
				criteria.cachedSearch = optionsSearch;
				criteria.cached = true;
			} else {
				fetchOptions.cache = "CACHE";
			}

			var criteriaToSend = $.extend(true, {}, criteria);

			if(options && options.search) {
				var filter = options.search.toLowerCase().split(/[ ,]+/); //Split by regular space or comma
				for(var fIdx = 0; fIdx < filter.length; fIdx++) {
					var fKeyword = filter[fIdx];
					criteriaToSend.rules[Util.guid()] = { type : "All", name : "", value : fKeyword };
				}
			}

			if(options && options.sortProperty && options.sortDirection) {
				fetchOptions.sort = {
						type : null,
						name : null,
						direction : options.sortDirection
				}
				switch(options.sortProperty) {
					case "code":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "code";
						break;
					case "identifier":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "identifier";
						break;
					case "entityType":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "type";
						break;
					case "registrationDate":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "registrationDate"

						break;
					case "modificationDate":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "modificationDate";
						break;
					case "entityKind":
					case "identifier":
					case "experiment":
					case "matched":
					case "score":
						fetchOptions.sort = null;
						break;
					default: //Properties
						fetchOptions.sort.type = "Property";
						fetchOptions.sort.name = options.sortProperty;
						break;
				}
			}

			_this.additionalRules.forEach(rule => criteriaToSend.rules[Util.guid()] = rule);

			$(".repeater-search").remove();

			switch(criteriaToSend.entityKind) {
				case "ALL":
				case "ALL_PARTIAL":
				case "ALL_PREFIX":
					var freeText = "";
					for(var ruleId in criteriaToSend.rules) {
						if(criteriaToSend.rules[ruleId].value) {
							freeText += " " +  criteriaToSend.rules[ruleId].value;
						}
					}
					mainController.serverFacade.searchGlobally(freeText, criteriaToSend.entityKind, fetchOptions,
							callbackForSearch);
					break;
				case "SAMPLE":
					mainController.serverFacade.searchForSamplesAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
					break;
				case "EXPERIMENT":
					mainController.serverFacade.searchForExperimentsAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
					break;
				case "DATASET":
					mainController.serverFacade.searchForDataSetsAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
					break;
			}
		}
	}

	this._getSearchCriteriaV3 = function(callback) {
		var criteriaToSend = $.extend(true, {}, this._advancedSearchModel.criteria);
		this.additionalRules.forEach(rule => criteriaToSend.rules[Util.guid()] = rule);

		switch(criteriaToSend.entityKind) {
			case "ALL":
				var freeText = "";
				for(var ruleId in criteriaToSend.rules) {
					if(criteriaToSend.rules[ruleId].value) {
						freeText += " " +  criteriaToSend.rules[ruleId].value;
					}
				}
				mainController.serverFacade.getSearchCriteriaAndFetchOptionsForGlobalSearch(freeText, false, {},
					callback);
				break;
			case "ALL_PARTIAL":
				var freeText = "";
				for(var ruleId in criteriaToSend.rules) {
					if(criteriaToSend.rules[ruleId].value) {
						freeText += " " +  criteriaToSend.rules[ruleId].value;
					}
				}
				mainController.serverFacade.getSearchCriteriaAndFetchOptionsForGlobalSearch(freeText, true, {},
					callback);
				break;
			case "SAMPLE":
				mainController.serverFacade.getSearchCriteriaAndFetchOptionsForSamplesSearch(criteriaToSend, {}, callback);
				break;
			case "EXPERIMENT":
				mainController.serverFacade.getSearchCriteriaAndFetchOptionsForExperimentSearch(criteriaToSend, {}, callback);
				break;
			case "DATASET":
				mainController.serverFacade.getSearchCriteriaAndFetchOptionsForDataSetSearch(criteriaToSend, {}, callback);
				break;
		}
	}

	//
	// query loading / saving
	//

	this._searchStoreAvailable = function(callback) {
		this._mainController.serverFacade.searchCustomASServices('search-store', function(result) {
			if (result != null && result.objects.length > 0) {
				callback(true);
			} else {
				callback(false);
			}
		})
	}

	this.selectSavedSearch = function(selcetedSavedSearchIndex) {
		var savedSearch = this._advancedSearchModel.savedSearches[selcetedSavedSearchIndex];
		this._advancedSearchModel.selcetedSavedSearchIndex = selcetedSavedSearchIndex;
		this._advancedSearchModel.criteria = this._clone(savedSearch.criteria);
		this._advancedSearchModel.forceLoadCriteria = true;
		this._advancedSearchView.repaintContent();
	}

	// params.name
	// params.experiment
	this.saveNewSample = function(params, callback) {
		var _this = this;

		this._ensureProjectAndExperiment(params.experiment, function(experiment) {
			_this._doIfAdmin(experiment, function() {
				_this._getSearchCriteriaV3(function(criteriaV3, fetchOptionsV3) {
					var criteriaEln = _this._advancedSearchModel.criteria;
					var space = experiment.project.space;
					_this._mainController.serverFacade.saveSearch(space, experiment, params.name, criteriaV3, fetchOptionsV3, criteriaEln, function(sample) {
						Util.showSuccess('Search saved.');
						var savedSearch = _this._sampleToSavedSearch(sample);
						_this._advancedSearchModel.savedSearches.unshift(savedSearch);
						_this.selectSavedSearch(0);
						callback();
					});
				});
			});
		});
	}

	this.updateSelectedSample = function(callback) {
		var _this = this;
		var savedSearch = _this._advancedSearchModel.savedSearches[_this._advancedSearchModel.selcetedSavedSearchIndex];
		_this._doIfAdmin(savedSearch.sample.experiment, function() {

			_this._getSearchCriteriaV3(function(criteriaV3, fetchOptionsV3) {
				var criteriaEln = _this._advancedSearchModel.criteria;
				var selcetedSavedSearchIndex = _this._advancedSearchModel.selcetedSavedSearchIndex;
				var permId = _this._advancedSearchModel.savedSearches[selcetedSavedSearchIndex].sample.permId.permId;
				_this._mainController.serverFacade.updateSearch(permId, criteriaV3, fetchOptionsV3, criteriaEln, function(result){
					if (result) {
						Util.showSuccess('Search updated.');
						savedSearch.criteria = _this._clone(_this._advancedSearchModel.criteria);
					}
					callback();
				});
			});
		});
	}

	this.delete = function(selcetedSavedSearchIndex, callback) {
		var _this = this;
		var selcetedSavedSearchIndex = _this._advancedSearchModel.selcetedSavedSearchIndex;
		var permId = _this._advancedSearchModel.savedSearches[selcetedSavedSearchIndex].sample.permId.permId;
		var reason = 'Search query deletion by user';
		this._mainController.serverFacade.deleteSearch(permId, reason, function(deletionId) {
			if (deletionId) {
				_this._advancedSearchModel.selcetedSavedSearchIndex = -1;
				Util.showSuccess('Search deleted.');
				_this._loadSavedSearches(function() {
					_this._advancedSearchView.repaintContent();
				});
			}
			callback();
		});
	}

	this.clearSelection = function() {
		this._advancedSearchModel.selcetedSavedSearchIndex = -1;
		this._advancedSearchModel.forceLoadCriteria = true;
		this._advancedSearchView.repaintContent();
	}

	this._loadSavedSearches = function(callback) {
		var _this = this;
		this._mainController.serverFacade.searchSamplesV3('SEARCH_QUERY', function(result) {
			_this._advancedSearchModel.savedSearches = [];
			if(result != null && result.objects != null) {
				var samples = _this._sortSearchSamples(result.objects);
				for (var i=0; i<samples.length; i++) {
					_this._advancedSearchModel.savedSearches.push(_this._sampleToSavedSearch(samples[i]));
				}
			}
			callback();
		});
	}

	// puts own samples on top
	// samples are assumed to already be sorted by date
	this._sortSearchSamples = function(samples) {
		var ownSamples = [];
		var otherSamples = [];
		for (var i=0; i<samples.length; i++) {
			if (samples[i].registrator.userId == this._mainController.serverFacade.getUserId()) {
				ownSamples.push(samples[i]);
			} else {
				otherSamples.push(samples[i]);
			}
		}
		return ownSamples.concat(otherSamples);
	}

	// puts own samples on top
	// samples are assumed to already be sorted by date
	this._sortSearches = function(searches) {
		var ownSearches = [];
		var otherSearches = [];
		for (var i=0; i<searches.length; i++) {
			if (searches[i].sample.registrator.userId == this._mainController.serverFacade.getUserId()) {
				ownSearches.push(searches[i]);
			} else {
				otherSearches.push(searches[i]);
			}
		}
		return ownSearches.concat(otherSearches);
	}

	this._ensureProjectAndExperiment = function(experiment, callback) {
		var _this = this;
		if (experiment.defaultDummyExperiment) {
			var dummyExperiment = experiment;
			_this._mainController.serverFacade.getProject(dummyExperiment.projectIdentifier, function(result) {
				if ($.isEmptyObject(result)) {
					var description = ELNDictionary.generatedObjects.searchQueriesProject.description;
					_this._mainController.serverFacade.createProject(
						dummyExperiment.space, dummyExperiment.projectCode, description, function(result) {
							_this._ensureExperiment(dummyExperiment, callback);
						}
					);
				} else {
					_this._ensureExperiment(experiment, callback);
				}
			});
		} else {
			_this._loadExperimentWithProjectAndSpace(experiment, callback);
		}
	}

	this._loadExperimentWithProjectAndSpace = function(experiment, callback) {
		this._mainController.serverFacade.getExperiments([experiment.permId.permId], function(result) {
			callback(result[experiment.permId.permId]);
		})
	}

	this._ensureExperiment = function(dummyExperiment, callback) {
		var _this = this;
		_this._mainController.serverFacade.getExperimentOrNull(
			dummyExperiment.identifier.identifier, function(experiment) {
				if (experiment != null) {
					callback(experiment);
				} else {
					var experimentTypePermId = 'COLLECTION';
					_this._mainController.serverFacade.createExperiment(
						experimentTypePermId, dummyExperiment.projectIdentifier, dummyExperiment.code, function(result) {
							if (result && result.length > 0) {
								var experimentPermId = result[0].permId;
								_this._mainController.serverFacade.getExperiments([experimentPermId], function(result) {
									callback(result[experimentPermId]);
								});
							}
					});
				}
		});
	}

	this._sampleToSavedSearch = function(sample) {
		return {
			sample: sample,
			name: sample.properties['$NAME'],
			criteria: JSON.parse(sample.properties['$SEARCH_QUERY.CUSTOM_DATA'].replace('<xml><![CDATA[', '').replace(']]></xml>', ''))['eln-lims-criteria'],
		};
	}

	this._doIfAdmin = function(experiment, action) {
		var _this = this;
		_this._mainController.getUserRole({
			space: experiment.project.space.code,
			project: experiment.project.code,
		}, function(roles) {
			var hasAdmin = roles.filter(function(role) { return role.indexOf('ADMIN') > -1 }).length > 0;
			if (hasAdmin) {
				action();
			} else {
				Util.showUserError('You need to be admin in the related space or project to save queries.');
			}
		});
	}

	this._clone = function(object) {
		return JSON.parse(JSON.stringify(object));
	}

}
