/**
 * =============================================
 * OpenBIS facade internal code (DO NOT USE!!!)
 * =============================================
 */

if(typeof $ == 'undefined'){
	alert('Loading of openbis.js failed - jquery.js is missing');
}

function _openbisInternal(openbisUrlOrNull){
	this.init(openbisUrlOrNull);
}

_openbisInternal.prototype.init = function(openbisUrlOrNull){
	this.openbisUrl = this.normalizeOpenbisUrl(openbisUrlOrNull);
	this.generalInfoServiceUrl = this.openbisUrl + "/rmi-general-information-v1.json";
	this.generalInfoChangingServiceUrl = this.openbisUrl + "/rmi-general-information-changing-v1.json";
	this.queryServiceUrl = this.openbisUrl + "/rmi-query-v1.json";
	this.webInfoServiceUrl = this.openbisUrl + "/rmi-web-information-v1.json"
}

_openbisInternal.prototype.log = function(msg){
	if(console){
		console.log(msg);
	}
}

_openbisInternal.prototype.normalizeOpenbisUrl = function(openbisUrlOrNull){
	var parts = this.parseUri(window.location);
	
	if(openbisUrlOrNull){
		var openbisParts = this.parseUri(openbisUrlOrNull);
		
		for(openbisPartName in openbisParts){
			var openbisPartValue = openbisParts[openbisPartName];
			
			if(openbisPartValue){
				parts[openbisPartName] = openbisPartValue;
			}
		}
	}
	
	return parts.protocol + "://" + parts.authority + "/openbis/openbis";
}

_openbisInternal.prototype.jsonRequestData = function(params) {
	params["id"] = "1";
	params["jsonrpc"] = "2.0";
	return JSON.stringify(params)
}
 
_openbisInternal.prototype.ajaxRequest = function(settings) {
	settings.type = "POST";
	settings.processData = false;
	settings.dataType = "json";
	settings.jsonp = false;
	settings.data = this.jsonRequestData(settings.data);
	settings.success = this.ajaxRequestSuccess(settings.success);
	// we call the same settings.success function for backward compatibility
	settings.error = this.ajaxRequestError(settings.success);
	$.ajax(settings)
}

_openbisInternal.prototype.responseInterceptor = function(response, action) {
	action(response);
}

_openbisInternal.prototype.ajaxRequestSuccess = function(action){
	var openbisObj = this;
	return function(response){
		if(response.error){
			openbisObj.log("Request failed: " + JSON.stringify(response.error));
		}
		
		openbisObj.responseInterceptor(response, function() {
			if(action){
				action(response);
			}
		});
	};
}

_openbisInternal.prototype.ajaxRequestError = function(action){
	var openbisObj = this;
	return function(xhr, status, error){
		openbisObj.log("Request failed: " + error);
		
		var response = { "error" : "Request failed: " + error };
		openbisObj.responseInterceptor(response, function() {
			if(action){
				action(response);
			}
		});
	};
}

// Functions for working with cookies (see http://www.quirksmode.org/js/cookies.html)

_openbisInternal.prototype.createCookie = function(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

_openbisInternal.prototype.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

_openbisInternal.prototype.eraseCookie = function(name) {
	this.createCookie(name,"",-1);
}

// parseUri 1.2.2 (c) Steven Levithan <stevenlevithan.com> MIT License (see http://blog.stevenlevithan.com/archives/parseuri)

_openbisInternal.prototype.parseUri = function(str) {
	var options = {
		strictMode: false,
		key: ["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],
		q:   {
			name:   "queryKey",
			parser: /(?:^|&)([^&=]*)=?([^&]*)/g
		},
		parser: {
			strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
			loose:  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
		}
	};
	
	var	o   = options,
		m   = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
		uri = {},
		i   = 14;

	while (i--) uri[o.key[i]] = m[i] || "";

	uri[o.q.name] = {};
	uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
		if ($1) uri[o.q.name][$1] = $2;
	});

	return uri;
}

_openbisInternal.prototype.listDataStores = function(action){
	this.ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listDataStores",
				"params" : [ this.sessionToken ] 
		},
		success: action
	});
}

_openbisInternal.prototype.initDataStores = function(action){
	var openbisInternal = this;
	
	if(typeof this.dataStores === "undefined" || this.dataStores.length == 0){
		this.listDataStores(function(response){
			if(response.result){
				openbisInternal.dataStores = response.result;
			}else{
				openbisInternal.dataStores = [];
			}
			action();
		});
	}else{
		action();
	}
}

_openbisInternal.prototype.getDataStoreUrlForDataStoreCode = function(dataStoreCodeOrNull, action) {
	var openbisInternal = this;
	
	this.initDataStores(function(){
		if(openbisInternal.dataStores.length == 0){
			throw "Couldn't get a data store url as there are no data stores configured.";
		}else{
			if(dataStoreCodeOrNull){
				var dataStoreUrl = null;
				$.each(openbisInternal.dataStores, function(index, dataStore){
					if(dataStore.code == dataStoreCodeOrNull){
						dataStoreUrl = dataStore.downloadUrl;
					}
				});
				if(dataStoreUrl){
					action(dataStoreUrl);
				}else{
					throw "Couldn't get a data store url because data store with " + dataStoreCodeOrNull + " code does not exist.";
				}
			}else{
				if(openbisInternal.dataStores.length == 1){
					action(openbisInternal.dataStores[0].downloadUrl);
				}else{
					throw "There is more than one data store configured. Please specify a data store code to get a data store url.";
				}
			}
		}
	});
}

_openbisInternal.prototype.getDataStoreUrlForDataSetCode = function(dataSetCode, action) {
	var openbisInternal = this;
	
	this.initDataStores(function(){
		if(openbisInternal.dataStores.length == 0){
			throw "Couldn't get a data store url as there are no data stores configured.";
		}else if(openbisInternal.dataStores.length == 1){
			action(openbisInternal.dataStores[0].downloadUrl);
		}else{
			openbisInternal.ajaxRequest({
				url: openbisInternal.generalInfoServiceUrl,
				data: { "method" : "tryGetDataStoreBaseURL",
						"params" : [ openbisInternal.sessionToken, dataSetCode ] 
						},
				success: function(response){
					var hostUrl = response.result;
					
					if(hostUrl){
						action(hostUrl + "/datastore_server");
					}else{
						throw "Couldn't get a data store url for a data set with " + dataSetCode + " code because the data set does not exist.";
					}
				}
			 });
		}
	});
}

_openbisInternal.prototype.getDataStoreApiUrlForDataStoreCode = function(dataStoreCodeOrNull, action) {
	this.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreUrl){
		action(dataStoreUrl + "/rmi-dss-api-v1.json");
	});
}

_openbisInternal.prototype.getDataStoreApiUrlForDataSetCode = function(dataSetCode, action) {
	this.getDataStoreUrlForDataSetCode(dataSetCode, function(dataStoreUrl){
		action(dataStoreUrl + "/rmi-dss-api-v1.json");
	});
}

_openbisInternal.prototype.getDataStoreHostForDataStoreCode = function(dataStoreCodeOrNull, action) {
	var openbisObj = this;
	
	this.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreUrl){
		var parts = openbisObj.parseUri(dataStoreUrl);
		action(parts.protocol + "://" + parts.authority);
	});
}

/**
 * ===============
 * OpenBIS facade
 * ===============
 * 
 * The facade provides access to the following services:
 * 
 * - ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService
 * - ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService
 * - ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer
 * - ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService
 * - ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric
 * 
 * @class
 * 
 */

function openbis(openbisUrlOrNull) {
	this._internal = new _openbisInternal(openbisUrlOrNull);
}

/**
 * Intercepts responses so clients can handle generic errors like session timeouts with a
 * single handler.
 * 
 * @method
 */
openbis.prototype.setResponseInterceptor = function(responseInterceptor) {
	this._internal.responseInterceptor = responseInterceptor;
}

/**
 * ==================================================================================
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService methods
 * ==================================================================================
 */

/**
 * Log into openBIS.
 * 
 * @see IGeneralInformationService.tryToAuthenticateForAllServices(String, String)
 * @method
 */
openbis.prototype.login = function(userId, userPassword, action) {
	var openbisObj = this
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "tryToAuthenticateForAllServices",
				"params" : [ userId, userPassword ] 
				},
		success: 
			function(loginResponse) {
				if(loginResponse.error){
					alert("Login failed: " + loginResponse.error.message);
				}else{
					openbisObj._internal.sessionToken = loginResponse.result;
					openbisObj.rememberSession();
                    openbisObj._internal.initDataStores(function(){
                        action(loginResponse);
                    });
				}
			}
	 });
}

/**
 * Stores the current session in a cookie. 
 *
 * @method
 */
openbis.prototype.rememberSession = function() {
	this._internal.createCookie('openbis', this.getSession(), 1);
}

/**
 * Removes the current session from a cookie. 
 *
 * @method
 */
openbis.prototype.forgetSession = function() {
	this._internal.eraseCookie('openbis');
}

/**
 * Restores the current session from a cookie.
 *
 * @method
 */
openbis.prototype.restoreSession = function() {
	this._internal.sessionToken = this._internal.readCookie('openbis');
}

/**
 * Sets the current session.
 *
 * @method
 */
openbis.prototype.useSession = function(sessionToken){
	this._internal.sessionToken = sessionToken;
}

/**
 * Returns the current session.
 * 
 * @method
 */
openbis.prototype.getSession = function(){
	return this._internal.sessionToken;
}

/**
 * Checks whether the current session is still active.
 *
 * @see IGeneralInformationService.isSessionActive(String)
 * @method
 */
openbis.prototype.isSessionActive = function(action) {
	if(this.getSession()){
		this._internal.ajaxRequest({
			url: this._internal.generalInfoServiceUrl,
			data: { "method" : "isSessionActive",
					"params" : [ this.getSession() ] 
					},
			success: action
		});
	}else{
		action({ result : false })
	}
}

/**
 * Restores the current session from a cookie and executes 
 * the specified action if the session is still active.
 * 
 * @see restoreSession()
 * @see isSessionActive()
 * @method
 */
openbis.prototype.ifRestoredSessionActive = function(action) {
	this.restoreSession();
	this.isSessionActive(function(data) { if (data.result) action(data) });
}

/**
 * Log out of openBIS.
 * 
 * @see IGeneralInformationService.logout(String)
 * @method
 */
openbis.prototype.logout = function(action) {
	this.forgetSession();
	
	if(this.getSession()){
		this._internal.ajaxRequest({
			url: this._internal.generalInfoServiceUrl,
			data: { "method" : "logout",
					"params" : [ this.getSession() ] 
				  },
			success: action
		});
	}else if(action){
		action({ result : null });
	}
}

/**
 * @see IGeneralInformationService.listNamedRoleSets(String)
 * @method
 */
openbis.prototype.listNamedRoleSets = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listNamedRoleSets",
				"params" : [ this.getSession() ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listSpacesWithProjectsAndRoleAssignments(String, String)
 * @method
 */
openbis.prototype.listSpacesWithProjectsAndRoleAssignments = function(databaseInstanceCodeOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSpacesWithProjectsAndRoleAssignments",
				"params" : [ this.getSession(),  databaseInstanceCodeOrNull ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForSamples(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForSamples = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForSamples",
				"params" : [ this.getSession(),
							 searchCriteria ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForSamples(String, SearchCriteria, EnumSet<SampleFetchOption>)
 * @method
 */
openbis.prototype.searchForSamplesWithFetchOptions = function(searchCriteria, fetchOptions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { 
				"method" : "searchForSamples",
				"params" : [ 
					this.getSession(),
					searchCriteria,
					fetchOptions ] 
		},
		success: action
	 });
}

/**
 * @see IGeneralInformationService.searchForSamplesOnBehalfOfUser(String, SearchCriteria, EnumSet<SampleFetchOption>, String)
 * @method
 */
openbis.prototype.searchForSamplesOnBehalfOfUser = function(searchCriteria, fetchOptions, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { 
				"method" : "searchForSamplesOnBehalfOfUser",
				"params" : [ 
					this.getSession(),
					searchCriteria,
					fetchOptions,
					userId ] 
		},
		success: action
	 });
}

/**
 * @see IGeneralInformationService.filterSamplesVisibleToUser(String, List<Sample>, String)
 * @method
 */
openbis.prototype.filterSamplesVisibleToUser = function(allSamples, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { 
				"method" : "filterSamplesVisibleToUser",
				"params" : [ 
					this.getSession(),
					allSamples,
					userId ] 
		},
		success: action
	 });
}

/**
 * @see IGeneralInformationService.listSamplesForExperiment(String, String)
 * @method
 */
openbis.prototype.listSamplesForExperiment = function(experimentIdentifier, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSamplesForExperiment",
				"params" : [ this.getSession(), experimentIdentifier ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listSamplesForExperimentOnBehalfOfUser(String, String, String)
 * @method
 */
openbis.prototype.listSamplesForExperimentOnBehalfOfUser = function(experimentIdentifier, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSamplesForExperimentOnBehalfOfUser",
				"params" : [ this.getSession(), experimentIdentifier, userId ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSets(String, List<Sample>)
 * @method
 */
openbis.prototype.listDataSetsForSamples = function(samples, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSets",
				"params" : [ this.getSession(), samples ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperiments(String, List<Project>, String)
 * @method
 */
openbis.prototype.listExperiments = function(projects, experimentType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.getSession(), projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperimentsHavingSamples(String, List<Project>, String)
 * @method
 */
openbis.prototype.listExperimentsHavingSamples = function(projects, experimentType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperimentsHavingSamples",
				"params" : [ this.getSession(), projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperimentsHavingDataSets(String, List<Project>, String)
 * @method
 */
openbis.prototype.listExperimentsHavingDataSets = function(projects, experimentType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperimentsHavingDataSets",
				"params" : [ this.getSession(), projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.filterExperimentsVisibleToUser(String, List<Experiment>, String)
 * @method
 */
openbis.prototype.filterExperimentsVisibleToUser = function(allExperiments, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "filterExperimentsVisibleToUser",
				"params" : [ this.getSession(), allExperiments, userId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsForSample(String, Sample, boolean)
 * @method
 */
openbis.prototype.listDataSetsForSample = function(sample, restrictToDirectlyConnected, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForSample",
				"params" : [ this.getSession(), sample, restrictToDirectlyConnected ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataStores(String)
 * @method
 */
openbis.prototype.listDataStores = function(action) {
	this._internal.listDataStores(action);
}

/**
 * @see IGeneralInformationService.getDefaultPutDataStoreBaseURL(String)
 * @method
 */
openbis.prototype.getDefaultPutDataStoreBaseURL = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDefaultPutDataStoreBaseURL",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.tryGetDataStoreBaseURL(String)
 * @method
 */
openbis.prototype.tryGetDataStoreBaseURL = function(dataSetCode, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "tryGetDataStoreBaseURL",
				"params" : [ this.getSession(), dataSetCode ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDataStoreBaseURLs(String, List<String>)
 * @method
 */
openbis.prototype.getDataStoreBaseURLs = function(dataSetCodes, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDataStoreBaseURLs",
				"params" : [ this.getSession(), dataSetCodes ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetTypes(String)
 * @method
 */
openbis.prototype.listDataSetTypes = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetTypes",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listSampleTypes(String)
 * @method
 */
openbis.prototype.listSampleTypes = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSampleTypes",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperimentTypes(String)
 * @method
 */
openbis.prototype.listExperimentTypes = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperimentTypes",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listVocabularies(String)
 * @method
 */
openbis.prototype.listVocabularies = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listVocabularies",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listPropertyTypes(String, boolean withRelations)
 * @method
 */
openbis.prototype.listPropertyTypes = function(withRelations, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listPropertyTypes",
				"params" : [ this.getSession() , withRelations] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.generateCode(String, String prefix, EntityKind entityKind);
 * @method
 */
openbis.prototype.generateCode = function(prefix, entityKind, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "generateCode",
				"params" : [ this.getSession() , prefix, entityKind] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSets(String, List<Sample>, EnumSet<Connections>)
 * @method
 */
openbis.prototype.listDataSetsForSamplesWithConnections = function(samples, connectionsToGet, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSets",
				"params" : [ this.getSession(), samples, connectionsToGet ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsOnBehalfOfUser(String, List<Sample>, EnumSet<Connections>, String)
 * @method
 */
openbis.prototype.listDataSetsForSamplesOnBehalfOfUser = function(samples, connectionsToGet, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsOnBehalfOfUser",
				"params" : [ this.getSession(), samples, connectionsToGet, userId ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsForExperiments(String, List<Experiment>, EnumSet<Connections>)
 * @method
 */
openbis.prototype.listDataSetsForExperiments = function(experiments, connectionsToGet, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForExperiments",
				"params" : [ this.getSession(), experiments, connectionsToGet ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsForExperimentsOnBehalfOfUser(String, List<Experiment>, EnumSet<Connections>, String)
 * @method
 */
openbis.prototype.listDataSetsForExperimentsOnBehalfOfUser = function(experiments, connectionsToGet, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForExperimentsOnBehalfOfUser",
				"params" : [ this.getSession(), experiments, connectionsToGet, userId ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDataSetMetaData(String, List<String>)
 * @method
 */
openbis.prototype.getDataSetMetaData = function(dataSetCodes, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDataSetMetaData",
				"params" : [ this.getSession(), dataSetCodes ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDataSetMetaData(String, List<String>, EnumSet<DataSetFetchOption>)
 * @method
 */
openbis.prototype.getDataSetMetaDataWithFetchOptions = function(dataSetCodes, fetchOptions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDataSetMetaData",
				"params" : [ this.getSession(), dataSetCodes, fetchOptions ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForDataSets(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForDataSets = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForDataSets",
				"params" : [ this.getSession(),
							 searchCriteria ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForDataSetsOnBehalfOfUser(String, SearchCriteria, String)
 * @method
 */
openbis.prototype.searchForDataSetsOnBehalfOfUser = function(searchCriteria, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForDataSetsOnBehalfOfUser",
				"params" : [ this.getSession(),
							 searchCriteria,
							 userId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchOnSearchDomain(String, String, String, Map<String, String>)
 * @method
 */
openbis.prototype.searchOnSearchDomain = function(preferredSearchDomainOrNull, searchString, optionalParametersOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchOnSearchDomain",
				"params" : [ this.getSession(),
							 preferredSearchDomainOrNull,
							 searchString, optionalParametersOrNull ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAvailableSearchDomains(String)
 * @method
 */
openbis.prototype.listAvailableSearchDomains = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAvailableSearchDomains",
				"params" : [ this.getSession()] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.filterDataSetsVisibleToUser(String, List<DataSet>, String)
 * @method
 */
openbis.prototype.filterDataSetsVisibleToUser = function(allDataSets, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "filterDataSetsVisibleToUser",
				"params" : [ this.getSession(),
				             allDataSets,
							 userId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperiments(String, List<String>)
 * @method
 */
openbis.prototype.listExperimentsForIdentifiers = function(experimentIdentifiers, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.getSession(),
				             experimentIdentifiers ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForExperiments(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForExperiments = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForExperiments",
				"params" : [ this.getSession(),
				             searchCriteria ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listProjects(String)
 * @method
 */
openbis.prototype.listProjects = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listProjects",
				"params" : [ this.getSession() ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listProjectsOnBehalfOfUser(String, String)
 * @method
 */
openbis.prototype.listProjectsOnBehalfOfUser = function(userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listProjectsOnBehalfOfUser",
				"params" : [ this.getSession(), userId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.getMaterialByCodes(String, List<MaterialIdentifier>)
 * @method
 */
openbis.prototype.getMaterialByCodes = function(materialIdentifiers, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getMaterialByCodes",
				"params" : [ this.getSession(), materialIdentifiers ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForMaterials(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForMaterials = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForMaterials",
				"params" : [ this.getSession(), searchCriteria ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listMetaprojects(String)
 * @method
 */
openbis.prototype.listMetaprojects = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listMetaprojects",
				"params" : [ this.getSession() ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listMetaprojectsOnBehalfOfUser(String, String)
 * @method
 */
openbis.prototype.listMetaprojectsOnBehalfOfUser = function(userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listMetaprojectsOnBehalfOfUser",
				"params" : [ this.getSession(), userId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.getMetaproject(String, IMetaprojectId)
 * @method
 */
openbis.prototype.getMetaproject = function(metaprojectId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getMetaproject",
				"params" : [ this.getSession(), metaprojectId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.getMetaprojectOnBehalfOfUser(String, IMetaprojectId, String)
 * @method
 */
openbis.prototype.getMetaprojectOnBehalfOfUser = function(metaprojectId, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getMetaprojectOnBehalfOfUser",
				"params" : [ this.getSession(), metaprojectId, userId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAttachmentsForProject(String, IProjectId, boolean)
 * @method
 */
openbis.prototype.listAttachmentsForProject = function(projectId, allVersions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAttachmentsForProject",
				"params" : [ this.getSession(), projectId, allVersions ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAttachmentsForExperiment(String, IExperimentId, boolean)
 * @method
 */
openbis.prototype.listAttachmentsForExperiment = function(experimentId, allVersions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAttachmentsForExperiment",
				"params" : [ this.getSession(), experimentId, allVersions ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAttachmentsForSample(String, ISampleId, boolean)
 * @method
 */
openbis.prototype.listAttachmentsForSample = function(sampleId, allVersions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAttachmentsForSample",
				"params" : [ this.getSession(), sampleId, allVersions ] 
			  },
		success: action
	});
}


/**
 * @see GeneralInformationService.getUserDisplaySettings(String)
 * @method
 */
openbis.prototype.getUserDisplaySettings = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getUserDisplaySettings",
				"params" : [ this.getSession()] },
		success: action
	});
}

/**
 * @see GeneralInformationService.listDeletions(String, EnumSet<DeletionFetchOption>)
 * @method
 */
openbis.prototype.listDeletions = function(fetchOptions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDeletions",
				"params" : [ this.getSession(),
				             fetchOptions ] },
		success: action
	});
}

/**
 * @see GeneralInformationService.listPersons(String)
 * @method
 */
openbis.prototype.listPersons = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listPersons",
				"params" : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see GeneralInformationService.countNumberOfSamplesForType(String, String)
 * @method
 */
openbis.prototype.countNumberOfSamplesForType = function(sampleTypeCode, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "countNumberOfSamplesForType",
				"params" : [ this.getSession(),
				             sampleTypeCode ] },
		success: action
	});
}

/**
 * ==========================================================================================
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService methods
 * ==========================================================================================
 */

/**
 * @see IGeneralInformationChangingService.updateSampleProperties(String, long, Map<String,String>)
 * @method
 */
openbis.prototype.updateSampleProperties = function(sampleId, properties, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "updateSampleProperties",
				"params" : [ this.getSession(), sampleId, properties ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.addUnofficialVocabularyTerm(String, Long, NewVocabularyTerm)
 * @method
 */
openbis.prototype.addUnofficialVocabularyTerm = function(vocabularyId, term, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "addUnofficialVocabularyTerm",
				"params" : [ this.getSession(), vocabularyId, term ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.getWebAppSettings(String, String)
 * @method
 */
openbis.prototype.getWebAppSettings = function(webappId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "getWebAppSettings",
		params : [ this.getSession(), webappId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.setWebAppSettings(String, WebAppSettings)
 * @method
 */
openbis.prototype.setWebAppSettings = function(webappSettings, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "setWebAppSettings",
		params : [ this.getSession(), webappSettings ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.createMetaproject(String, String, String)
 * @method
 */
openbis.prototype.createMetaproject = function(name, descriptionOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "createMetaproject",
		params : [ this.getSession(), name, descriptionOrNull ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.updateMetaproject(String, IMetaprojectId, String, String)
 * @method
 */
openbis.prototype.updateMetaproject = function(metaprojectId, name, descriptionOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "updateMetaproject",
		params : [ this.getSession(), metaprojectId, name, descriptionOrNull ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteMetaproject(String, IMetaprojectId)
 * @method
 */
openbis.prototype.deleteMetaproject = function(metaprojectId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteMetaproject",
		params : [ this.getSession(), metaprojectId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.addToMetaproject(String, IMetaprojectId, MetaprojectAssignmentsIds)
 * @method
 */
openbis.prototype.addToMetaproject = function(metaprojectId, assignmentsToAdd, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "addToMetaproject",
		params : [ this.getSession(), metaprojectId, assignmentsToAdd ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.removeFromMetaproject(String, IMetaprojectId, MetaprojectAssignmentsIds)
 * @method
 */
openbis.prototype.removeFromMetaproject = function(metaprojectId, assignmentsToRemove, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "removeFromMetaproject",
		params : [ this.getSession(), metaprojectId, assignmentsToRemove ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.registerSamplesWithSilentOverrides(String, String, String, String, String, String)
 * @method
 */
openbis.prototype.registerSamplesWithSilentOverrides = function(sampleTypeCode, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull, sessionKey, defaultGroupIdentifier, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "registerSamplesWithSilentOverrides",
				"params" : [ this.getSession(),
							 sampleTypeCode,
							 spaceIdentifierSilentOverrideOrNull,
							 experimentIdentifierSilentOverrideOrNull,
							 sessionKey,
							 defaultGroupIdentifier] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.registerSamples(String, String, String, String)
 * @method
 */
openbis.prototype.registerSamples = function(sampleTypeCode, sessionKey, defaultGroupIdentifier, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "registerSamples",
				"params" : [ this.getSession(),
							 sampleTypeCode,
							 sessionKey,
							 defaultGroupIdentifier] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.updateSamplesWithSilentOverrides(String, String, String, String, String, String)
 * @method
 */
openbis.prototype.updateSamplesWithSilentOverrides = function(sampleTypeCode, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull, sessionKey, defaultGroupIdentifier, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "updateSamplesWithSilentOverrides",
				"params" : [ this.getSession(),
							 sampleTypeCode,
							 spaceIdentifierSilentOverrideOrNull,
							 experimentIdentifierSilentOverrideOrNull,
							 sessionKey,
							 defaultGroupIdentifier] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.updateSamples(String, String, String, String)
 * @method
 */
openbis.prototype.updateSamples = function(sampleTypeCode, sessionKey, defaultGroupIdentifier, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "updateSamples",
				"params" : [ this.getSession(),
							 sampleTypeCode,
							 sessionKey,
							 defaultGroupIdentifier] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.uploadedSamplesInfo(String, String, String)
 * @method
 */
openbis.prototype.uploadedSamplesInfo = function(sampleTypeCode, sessionKey, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "uploadedSamplesInfo",
				"params" : [ this.getSession(),
							 sampleTypeCode,
							 sessionKey] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteProjects(String, List<Long>, String)
 * @method
 */
openbis.prototype.deleteProjects = function(projectIds, reason, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteProjects",
				"params" : [ this.getSession(),
				             projectIds,
				             reason] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteExperiments(String, List<Long>, String, DeletionType)
 * @method
 */
openbis.prototype.deleteExperiments = function(experimentIds, reason, deletionType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteExperiments",
				"params" : [ this.getSession(),
				             experimentIds,
				             reason,
				             deletionType ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteSamples(String, List<Long>, String, DeletionType)
 * @method
 */
openbis.prototype.deleteSamples = function(sampleIds, reason, deletionType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteSamples",
				"params" : [ this.getSession(),
				             sampleIds,
				             reason,
				             deletionType ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteDataSets(String, List<String>, String, DeletionType)
 * @method
 */
openbis.prototype.deleteDataSets = function(dataSetCodes, reason, deletionType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteDataSets",
				"params" : [ this.getSession(),
				             dataSetCodes,
				             reason,
				             deletionType ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteDataSetsForced(String, List<String>, String, DeletionType)
 * @method
 */
openbis.prototype.deleteDataSetsForced = function(dataSetCodes, reason, deletionType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteDataSetsForced",
				"params" : [ this.getSession(),
				             dataSetCodes,
				             reason,
				             deletionType ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.revertDeletions(String, List<Long>)
 * @method
 */
openbis.prototype.revertDeletions = function(deletionIds, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "revertDeletions",
				"params" : [ this.getSession(),
				             deletionIds ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deletePermanently(String, List<Long>)
 * @method
 */
openbis.prototype.deletePermanently = function(deletionIds, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deletePermanently",
				"params" : [ this.getSession(),
				             deletionIds ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deletePermanentlyForced(String, List<Long>)
 * @method
 */
openbis.prototype.deletePermanentlyForced = function(deletionIds, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deletePermanentlyForced",
				"params" : [ this.getSession(),
				             deletionIds ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.registerPerson(String, String)
 * @method
 */
openbis.prototype.registerPerson = function(userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "registerPerson",
				"params" : [ this.getSession(),
				             userId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.registerSpace(String, String, String)
 * @method
 */
openbis.prototype.registerSpace = function(spaceCode, spaceDescription, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "registerSpace",
				"params" : [ this.getSession(),
				             spaceCode,
				             spaceDescription ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.registerPersonSpaceRole(String, String, String, String)
 * @method
 */
openbis.prototype.registerPersonSpaceRole = function(spaceCode, userID, roleCode, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "registerPersonSpaceRole",
				"params" : [ this.getSession(),
				             spaceCode,
				             userID,
				             roleCode ] },
		success: action
	});
}

/**
 * ============================================================================
 * ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer methods
 * ============================================================================
 */

/**
 * @see IQueryApiServer.listQueries(String)
 * @method
 */
openbis.prototype.listQueries = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "listQueries",
				"params" : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.executeQuery(String, long, Map<String, String>)
 * @method
 */
openbis.prototype.executeQuery = function(queryId, parameterBindings, action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "executeQuery",
				"params" : [ this.getSession(), queryId, parameterBindings ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.listTableReportDescriptions(String)
 * @method
 */
openbis.prototype.listTableReportDescriptions = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "listTableReportDescriptions",
				"params" : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.createReportFromDataSets(String, String, String, List<String>)
 * @method
 */
openbis.prototype.createReportFromDataSets = function(dataStoreCode, serviceKey, dataSetCodes, action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "createReportFromDataSets",
		params : [ this.getSession(), dataStoreCode, serviceKey, dataSetCodes ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.listAggregationServices(String)
 * @method
 */
openbis.prototype.listAggregationServices = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "listAggregationServices",
		params : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.createReportFromAggregationService(String, String, String, Map<String, Object>)
 * @method
 */
openbis.prototype.createReportFromAggregationService = function(dataStoreCode, serviceKey, parameters, action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "createReportFromAggregationService",
		params : [ this.getSession(), dataStoreCode, serviceKey, parameters ] },
		success: action
	});
}


/**
 * ==============================================================================
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService methods
 * ==============================================================================
 */

/**
 * Returns the current server side session.
 * 
 * @see IWebInformationService.getSessionToken()
 * @method
 */
openbis.prototype.getSessionTokenFromServer = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.webInfoServiceUrl,
		data: { "method" : "getSessionToken" },
		success: action
	 });
}

/**
 * =================================================================================
 * ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric methods
 * =================================================================================
 */

/**
 * @see IDssServiceRpcGeneric.listFilesForDataSet(String, DataSetFileDTO)
 * @method
 */
openbis.prototype.listFilesForDataSetFile = function(fileOrFolder, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(fileOrFolder.dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "listFilesForDataSet",
					"params" : [ openbisObj.getSession(), fileOrFolder ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSet(String, DataSetFileDTO)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetFile = function(fileOrFolder, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(fileOrFolder.dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSet",
					"params" : [ openbisObj.getSession(), fileOrFolder ] },
			success: action
		});
	});
}

/**
 * Returns a download url that is valid as long as the user session is valid.
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetFileInSession = function(fileOrFolder, action) {
	this.getDownloadUrlForFileForDataSetInSession(fileOrFolder.dataSetCode, fileOrFolder.path, action);
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSetWithTimeout(String, DataSetFileDTO, long)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetFileWithTimeout = function(fileOrFolder, validityDurationInSeconds, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(fileOrFolder.dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSetWithTimeout",
					"params" : [ openbisObj.getSession(), fileOrFolder, validityDurationInSeconds ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.listFilesForDataSet(String, String, String, boolean)
 * @method
 */
openbis.prototype.listFilesForDataSet = function(dataSetCode, path, recursive, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "listFilesForDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, path, recursive ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSet(String, String, String)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSet = function(dataSetCode, path, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, path ] },
			success: action
		});
	});
}

/**
 * Returns a download url that is valid as long as the user session is valid.
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetInSession = function(dataSetCode, path, action) {
	var openbisObj = this;
	this._internal.getDataStoreUrlForDataSetCode(dataSetCode, function(dataStoreUrl){
		var pathWithoutSlash = path.charAt(0) == "/" ? path.substr(1) : path;
		var url = dataStoreUrl + "/" + dataSetCode + "/" + pathWithoutSlash + "?sessionID=" + openbisObj.getSession();
		action(url);
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSetWithTimeout(String, String, String, long)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetWithTimeout = function(dataSetCode, path, validityDurationInSeconds, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSetWithTimeout",
					"params" : [ openbisObj.getSession(), dataSetCode, path, validityDurationInSeconds ] },
			success: action
		});
	});
}

/**
 * Creates a session workspace file uploader inside the specified uploaderContainer element and for the default data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceUploader = function(uploaderContainer, oncomplete, uploaderSettings){
	this.createSessionWorkspaceUploaderForDataStore(uploaderContainer, null, oncomplete, uploaderSettings);
}

/**
 * Creates a session workspace file uploader inside the specified uploaderContainer element and for the specified data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceUploaderForDataStore = function(uploaderContainer, dataStoreCodeOrNull, oncomplete, uploaderSettings){
	var uploaderSupported = window.File && window.FileReader && window.XMLHttpRequest;

	if(!uploaderSupported){
		alert("Uploader is not supported by your browser.");
		return;
	}
	
	var $this = this;
	this._internal.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreUrl){
		// figure out what is the location of the openbis.js script and assume that uploader resources are served by the same server
		var openbisScriptLocation = $('script[src*=openbis\\.js]').attr('src');
		var uploaderDirectoryLocation = jsFileLocation = openbisScriptLocation.replace(/js\/openbis\.js/g, 'uploader');
		
		$('head').append('<link rel="stylesheet" media="screen" type="text/css" href="' + uploaderDirectoryLocation + '/css/src/upload.css" />');
		$('head').append('<script charset="utf-8" type="text/javascript" src="' + uploaderDirectoryLocation + '/js/src/upload.js" />');
		
		$(uploaderContainer).load(uploaderDirectoryLocation + "/index.html", function(){
			var finalSettings = {
				       smart_mode: true,
				       chunk_size: 1000*1024,
				       file_upload_url: dataStoreUrl + "/session_workspace_file_upload",
				       form_upload_url: dataStoreUrl + "/session_workspace_form_upload",
				       file_download_url: dataStoreUrl + "/session_workspace_file_download",
				       oncomplete: oncomplete,
				       sessionID: $this.getSession()
			};
			
			if(uploaderSettings) {
				for(var key in uploaderSettings) {
					finalSettings[key] = uploaderSettings[key];
				}
			}
			
			Uploader.init(finalSettings);
		});
	});
}

/**
 * Creates a session workspace download url for a file with the specified filePath and for the default data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadUrl = function(filePath, action){
	return this.createSessionWorkspaceDownloadUrlForDataStore(filePath, null, action);
}

/**
 * Creates a session workspace download url for a file with the specified filePath and for the specified data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadUrlForDataStore = function(filePath, dataStoreCodeOrNull, action){
	var openbisObj = this;
	
	this._internal.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreUrl){
		var downloadUrl = dataStoreUrl + "/session_workspace_file_download?sessionID=" + openbisObj.getSession() + "&filePath=" + filePath;
		action(downloadUrl);
	});
}

/**
 * Create a session workspace download link for a file with the specified filePath at the default data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadLink = function(filePath, linkText, action){
	return this.createSessionWorkspaceDownloadLinkForDataStore(filePath, linkText, null, action);
}

/**
 * Create a session workspace download link for a file with the specified filePath at the specified data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadLinkForDataStore = function(filePath, linkText, dataStoreCodeOrNull, action){
	this.createSessionWorkspaceDownloadUrlForDataStore(filePath, dataStoreCodeOrNull, function(downloadUrl){
		var link = $("<a href='" + downloadUrl + "'>" + (linkText ? linkText : filePath) + "</a>");
		action(link);
	});
}

/**
 * Downloads a session workspace file with the specified filePath from the default data store.
 * @method
 */
openbis.prototype.downloadSessionWorkspaceFile = function(filePath, action) {
	this.downloadSessionWorkspaceFileForDataStore(filePath, null, action);
}

/**
 * Downloads a session workspace file with the specified filePath from the specified data store.
 * @method
 */
openbis.prototype.downloadSessionWorkspaceFileForDataStore = function(filePath, dataStoreCodeOrNull, action) {
	var openbisObj = this;
	
	this.createSessionWorkspaceDownloadUrlForDataStore(filePath, dataStoreCodeOrNull, function(downloadUrl){
		$.ajax({
			type: "GET",
			dataType: "text",
			url: downloadUrl,
			success: openbisObj._internal.ajaxRequestSuccess(action),
			error: openbisObj._internal.ajaxRequestError(action)
		});
	});
}

/**
 * Deletes a session workspace file with the specified filePath from the default data store.
 * @method
 */
openbis.prototype.deleteSessionWorkspaceFile = function(filePath, action) {
	this.deleteSessionWorkspaceFileForDataStore(filePath, null, action)
}

/**
 * Deletes a session workspace file with the specified filePath from the specified data store.
 * @method
 */
openbis.prototype.deleteSessionWorkspaceFileForDataStore = function(filePath, dataStoreCodeOrNull, action) {
	var openbisObj = this;
	
	this._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: {
				"method" : "deleteSessionWorkspaceFile",
				"params" : [ openbisObj.getSession(), filePath ]
			},
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getPathToDataSet(String, String, String)
 * @method
 */
openbis.prototype.getPathToDataSet = function(dataSetCode, overrideStoreRootPathOrNull, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getPathToDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, overrideStoreRootPathOrNull ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.tryGetPathToDataSet(String, String, String)
 * @method
 */
openbis.prototype.tryGetPathToDataSet = function(dataSetCode, overrideStoreRootPathOrNull, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "tryGetPathToDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, overrideStoreRootPathOrNull ] },
			success: action
		});
	});
}

/**
 * List shares from the default data store.
 * 
 * @see IDssServiceRpcGeneric.listAllShares(String)
 * @method
 */
openbis.prototype.listAllShares = function(action) {
	this.listAllSharesForDataStore(null, action);
}

/**
 * List shares from the specified data store.
 * 
 * @see IDssServiceRpcGeneric.listAllShares(String)
 * @method
 */
openbis.prototype.listAllSharesForDataStore = function(dataStoreCodeOrNull, action) {
	var openbisObj = this;
	
	this._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: {
				"method" : "listAllShares",
				"params" : [ openbisObj.getSession() ]
			},
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.shuffleDataSet(String, String, String)
 * @method
 */
openbis.prototype.shuffleDataSet = function(dataSetCode, shareId, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "shuffleDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, shareId ] },
			success: action
		});
	});
}

/**
 * Get a validation script from the default data store.
 * 
 * @see IDssServiceRpcGeneric.getValidationScript(String, String)
 * @method
 */
openbis.prototype.getValidationScript = function(dataSetTypeOrNull, action) {
	this.getValidationScriptForDataStore(dataSetTypeOrNull, null, action);
}

/**
 * Get a validation script from the specified data store.
 * 
 * @see IDssServiceRpcGeneric.getValidationScript(String, String)
 * @method
 */
openbis.prototype.getValidationScriptForDataStore = function(dataSetTypeOrNull, dataStoreCodeOrNull, action) {
	var openbisObj = this;
	
	this._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: {
				"method" : "getValidationScript",
				"params" : [ openbisObj.getSession(), dataSetTypeOrNull ]
			},
			success: action
		});
	});
}

/**
 * Get a url that will produce a graph with the given configuration at the default data store.
 *
 * @method
 */
openbis.prototype.getGraphUrl = function(graphConfig, action) {
	this.getGraphUrlForDataStore(graphConfig, null, action);
}

/**
 * Get a url that will produce a graph with the given configuration at the specified data store.
 *
 * @method
 */
openbis.prototype.getGraphUrlForDataStore = function(graphConfig, dataStoreCodeOrNull, action) {
	var openbisObj = this;
	
	this._internal.getDataStoreHostForDataStoreCode(dataStoreCodeOrNull, function(dataStoreHost) {
		var graphUrl = dataStoreHost + "/graphservice/?sessionID=" + openbisObj.getSession();
		for (prop in graphConfig) {
			graphUrl += "&" + prop + "=" + encodeURIComponent(graphConfig[prop]);
		}
		action(graphUrl);
	});
}

/**
 * =====================
 * OpenBIS graph config
 * =====================
 * 
 * Defines the configuration for a graph generated by the server's graphservice.
 * 
 * To use, set properties on the object with keys that match the name of the servlet
 * parameters of the graphservice and then call the method getGraphUrl on the openbis
 * object. Do *not* set the sessionID paramter -- this will be filled in by the openbis 
 * object.
 *
 * The graphservice is documented here: https://wiki-bsse.ethz.ch/display/openBISDoc/Configuring+Graphs+and+Plots
 * 
 * @class
 * 
 */
function openbisGraphConfig(filename, graphtype, title) {
	this.file = filename;
	this["graph-type"] =  graphtype;
	this.title = title;
}


/**
 * =====================================================
 * OpenBIS webapp context internal code (DO NOT USE!!!)
 * =====================================================
 */

function _openbisWebAppContextInternal(){
	this.webappCode = this.getParameter("webapp-code");
	this.sessionId = this.getParameter("session-id");
	this.entityKind = this.getParameter("entity-kind");
	this.entityType = this.getParameter("entity-type");
	this.entityIdentifier = this.getParameter("entity-identifier");
	this.entityPermId = this.getParameter("entity-perm-id");
}

_openbisWebAppContextInternal.prototype.getParameter = function(parameterName){
	var match = location.search.match(RegExp("[?|&]"+parameterName+'=(.+?)(&|$)'));
	if(match && match[1]){
		return decodeURIComponent(match[1].replace(/\+/g,' '));
	}else{
		return null;
	}
}

/**
 * =======================
 * OpenBIS webapp context 
 * =======================
 * 
 * Provides a context information for webapps that are embedded inside the OpenBIS UI.
 * 
 * @class
 * 
 */
function openbisWebAppContext(){
	this._internal = new _openbisWebAppContextInternal();
}

openbisWebAppContext.prototype.getWebappCode = function(){
	return this._internal.webappCode;
}

openbisWebAppContext.prototype.getSessionId = function(){
	return this._internal.sessionId;
}

openbisWebAppContext.prototype.getEntityKind = function(){
	return this._internal.entityKind;
}

openbisWebAppContext.prototype.getEntityType = function(){
	return this._internal.entityType;
}

openbisWebAppContext.prototype.getEntityIdentifier = function(){
	return this._internal.entityIdentifier;
}

openbisWebAppContext.prototype.getEntityPermId = function(){
	return this._internal.entityPermId;
}

openbisWebAppContext.prototype.getParameter = function(parameterName){
	return this._internal.getParameter(parameterName);
}

/**
 * =======================
 * OpenBIS Search Criteria
 * =======================
 *
 * Methods and classes for constructing search criteria objects for use in searches.
 */

/**
 * It is easier to construct instances of match clauses using one of the factory methods:
 *
 * 		createPropertyMatch
 * 		createAttributeMatch
 * 		createTimeAttributeMatch
 * 		createAnyPropertyMatch
 * 		createAnyFieldMatch
 *
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause
 * @class
 */
function SearchCriteriaMatchClause(type, fieldType, fieldCode, desiredValue) {
	this["@type"] = type;
	this["fieldType"] = fieldType;
	this["fieldCode"] = fieldCode;
	this["desiredValue"] = desiredValue;
	// compareMode should be one of "LESS_THAN_OR_EQUAL", "EQUALS", "GREATER_THAN_OR_EQUAL"
	this["compareMode"] = "EQUALS";
}

/**
 * Factory method to create a match for a property.
 * 
 * @param propertyCode The code of the property to compare against
 * @param desiredValue The value used in the comparison
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause.createPropertyMatch(String, String)
 * @method
 */
SearchCriteriaMatchClause.createPropertyMatch = function(propertyCode, desiredValue) {
	var matchClause = new SearchCriteriaMatchClause("PropertyMatchClause", "PROPERTY", propertyCode, desiredValue);
	matchClause["propertyCode"] = propertyCode;
	return matchClause;
}

/**
 * Factory method to create a match for an attribute.
 *
 * @param attribute Should be a valid ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute.
 *   It should come from this list:
 *      // common
 *      "CODE", "TYPE", "PERM_ID",
 *      // for sample or experiment
 *      "SPACE",
 *      // for experiment
 *      "PROJECT",
 *      // for all types of entities
 *      "METAPROJECT"
 * @param desiredValue The value used in the comparison 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause.createAttributeMatch(MatchClauseAttribute, String)
 * @method
 */
SearchCriteriaMatchClause.createAttributeMatch = function(attribute, desiredValue) {
	var matchClause = new SearchCriteriaMatchClause("AttributeMatchClause", "ATTRIBUTE", attribute, desiredValue);
	matchClause["attribute"] = attribute;
	return matchClause;
}

/**
 * Factory method to create a MatchClause matching against registration or modification
 * date.
 * 
 * @param attribute Should be a valid ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute
 * 	It should come from this list: REGISTRATION_DATE, MODIFICATION_DATE
 *
 * @param mode One of "LESS_THAN_OR_EQUAL", "EQUALS", "GREATER_THAN_OR_EQUAL"
 * @param date The date to compare against, format YYYY-MM-DD
 * @timezone The time zone of the date ("+1", "-5", "0", etc.)
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause.createAttributeMatch(MatchClauseTimeAttribute, CompareMode, String, String)
 * @method
 */
SearchCriteriaMatchClause.createTimeAttributeMatch = function(attribute, mode, date, timezone)
{
    var matchClause = new SearchCriteriaMatchClause("TimeAttributeMatchClause", "ATTRIBUTE", attribute, date);
	matchClause["attribute"] = attribute;
	matchClause["compareMode"] = mode;
	matchClause["timeZone"] = timezone;
	return matchClause;
}

/**
 * Factory method to create a match for against any property.
 * 
 * @param desiredValue The value used in the comparison
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause.createAnyPropertyMatch(String)
 * @method
 */
SearchCriteriaMatchClause.createAnyPropertyMatch = function(desiredValue) {
	var matchClause = new SearchCriteriaMatchClause("AnyPropertyMatchClause", "ANY_PROPERTY", null, desiredValue);
	return matchClause;
}

/**
 * Factory method to create a match for against any field (property or attribute).
 * 
 * @param desiredValue The value used in the comparison
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause.createAnyFieldMatch(String)
 * @method
 */
SearchCriteriaMatchClause.createAnyFieldMatch = function(desiredValue) {
	var matchClause = new SearchCriteriaMatchClause("AnyFieldMatchClause", "ANY_FIELD", null, desiredValue);
	return matchClause;
}

/**
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
 * @class
 */
function SearchCriteria() {
	this["@type"] =  "SearchCriteria";
	// operator should be either of "MATCH_ALL_CLAUSES" or "MATCH_ANY_CLAUSES"
	this["operator"] = "MATCH_ALL_CLAUSES";
	this["matchClauses"] = [];
	this["subCriterias"] = [];
}

/**
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.addMatchClause(MatchClause)
 * @method
 */
SearchCriteria.prototype.addMatchClause = function(matchClause) {
	this["matchClauses"].push(matchClause);
};

/**
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.addSubCriteria(SearchSubCriteria)
 * @method
 */
SearchCriteria.prototype.addSubCriteria = function(subCriteria) {
	this["subCriterias"].push(subCriteria);
};


/**
 * It is easier to construct instances of sub criteria using one of the factory methods:
 *
 * 		createSampleParentCriteria
 * 		createSampleChildCriteria
 * 		createSampleContainerCriteria
 * 		createSampleCriteria
 * 		createExperimentCriteria
 * 		createDataSetContainerCriteria
 * 		createDataSetParentCriteria
 * 		createDataSetChildCriteria
 *
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria
 * @class
 */
function SearchSubCriteria(targetEntityKind, searchCriteria) {
	this["@type"] = "SearchSubCriteria";
	this["targetEntityKind"] = targetEntityKind;	
	this["criteria"] = searchCriteria;
}

/**
 * Factory method to create a match for a sample parent.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createSampleParentCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createSampleParentCriteria = function(searchCriteria) {
	return new SearchSubCriteria("SAMPLE_PARENT", searchCriteria)
}

/**
 * Factory method to create a match for a sample child.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createSampleChildCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createSampleChildCriteria = function(searchCriteria) {
	return new SearchSubCriteria("SAMPLE_CHILD", searchCriteria)
}

/**
 * Factory method to create a match for a sample container.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createSampleContainerCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createSampleContainerCriteria = function(searchCriteria) {
	return new SearchSubCriteria("SAMPLE_CONTAINER", searchCriteria)
}

/**
 * Factory method to create a match for a sample.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createSampleCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createSampleCriteria = function(searchCriteria) {
	return new SearchSubCriteria("SAMPLE", searchCriteria)
}

/**
 * Factory method to create a match for an experiment.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createExperimentCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createExperimentCriteria = function(searchCriteria) {
	return new SearchSubCriteria("EXPERIMENT", searchCriteria)
}

/**
 * Factory method to create a match for a data set container.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createDataSetContainerCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createDataSetContainerCriteria = function(searchCriteria) {
	return new SearchSubCriteria("DATA_SET_CONTAINER", searchCriteria)
}

/**
 * Factory method to create a match for a data set parent.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createDataSetParentCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createDataSetParentCriteria = function(searchCriteria) {
	return new SearchSubCriteria("DATA_SET_PARENT", searchCriteria)
}

/**
 * Factory method to create a match for a data set child.
 * 
 * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria.createDataSetChildCriteria(SearchCriteria)
 * @method
 */
SearchSubCriteria.createDataSetChildCriteria = function(searchCriteria) {
	return new SearchSubCriteria("DATA_SET_CHILD", searchCriteria)
}
