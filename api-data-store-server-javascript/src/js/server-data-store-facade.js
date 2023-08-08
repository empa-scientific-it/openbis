/**
 * ======================================================
 * OpenBIS Data Store Server facade internal code (DO NOT USE!!!)
 * ======================================================
 */

function _DataStoreServerInternal(datastoreUrlOrNull, httpServerUri){
	this.init(datastoreUrlOrNull, httpServerUri);
}

_DataStoreServerInternal.prototype.init = function(datastoreUrlOrNull, httpServerUri){
	this.datastoreUrl = this.normalizeUrl(datastoreUrlOrNull, httpServerUri);
	this.httpServerUri = httpServerUri;
}

_DataStoreServerInternal.prototype.log = function(msg){
	if(console){
		console.log(msg);
	}
}

_DataStoreServerInternal.prototype.normalizeUrl = function(openbisUrlOrNull, httpServerUri){
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
	
	return parts.protocol + "://" + parts.authority + httpServerUri;
}

_DataStoreServerInternal.prototype.getUrlForMethod = function(method) {
    return this.datastoreUrl + "?method=" + method;
}

_DataStoreServerInternal.prototype.jsonRequestData = function(params) {
	return JSON.stringify(params);
}

_DataStoreServerInternal.prototype.sendHttpRequest = function(httpMethod, contentType, url, data, callback) {
	const xhr = new XMLHttpRequest();
	xhr.open(httpMethod, url);
	xhr.setRequestHeader("content-type", contentType);
	xhr.onreadystatechange = function() {
		if (xhr.readyState === XMLHttpRequest.DONE) { 
			const status = xhr.status;	
			if (status >= 200 && status < 300) {
				callback(xhr.responseText);
			} else if(status >= 400 && status < 500) {
				let response = JSON.parse(xhr.responseText);
				alert(response.error[1].message);
			} else if(status >= 500 && status < 600) {
				let response = JSON.parse(xhr.responseText);
				alert(response.error[1].message);
			} else {
				alert("ERROR: " + xhr.responseText);
			}
			
			
		  }
	};
	xhr.send(data);
  }



  _DataStoreServerInternal.prototype.buildGetUrl = function(queryParams) {
	const queryString = Object.keys(queryParams)
	  .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(queryParams[key])}`)
	  .join('&');
	return `${this.datastoreUrl}?${queryString}`;
  }



// Functions for working with cookies (see http://www.quirksmode.org/js/cookies.html)

_DataStoreServerInternal.prototype.createCookie = function(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

_DataStoreServerInternal.prototype.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

_DataStoreServerInternal.prototype.eraseCookie = function(name) {
	this.createCookie(name,"",-1);
}

// parseUri 1.2.2 (c) Steven Levithan <stevenlevithan.com> MIT License (see http://blog.stevenlevithan.com/archives/parseuri)

_DataStoreServerInternal.prototype.parseUri = function(str) {
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


/** Helper method for checking response from DSS server */
function parseJsonResponse(rawResponse, action) {
	let response = JSON.parse(rawResponse);
	if(response.error){
		alert(response.error[1].message);
	}else{
		action(response);
	}
}



/**
 * ===============
 * DSS facade
 * ===============
 * 
 * The facade provides access to the DSS methods
 * 
 */
function DataStoreServer(datastoreUrlOrNull, httpServerUri) {
	this._internal = new _DataStoreServerInternal(datastoreUrlOrNull, httpServerUri);
}


/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.AuthenticationAPI methods
 * ==================================================================================
 */

/**
 * Stores the current session in a cookie. 
 *
 * @method
 */
DataStoreServer.prototype.rememberSession = function() {
	this._internal.createCookie('dataStoreServer', this.getSession(), 1);
}

/**
 * Removes the current session from a cookie. 
 *
 * @method
 */
DataStoreServer.prototype.forgetSession = function() {
	this._internal.eraseCookie('dataStoreServer');
}

/**
 * Restores the current session from a cookie.
 *
 * @method
 */
DataStoreServer.prototype.restoreSession = function() {
	this._internal.sessionToken = this._internal.readCookie('dataStoreServer');
}

/**
 * Sets the current session.
 *
 * @method
 */
DataStoreServer.prototype.useSession = function(sessionToken){
	this._internal.sessionToken = sessionToken;
}

/**
 * Returns the current session.
 * 
 * @method
 */
DataStoreServer.prototype.getSession = function(){
	return this._internal.sessionToken;
}

/**
 * Sets interactiveSessionKey.
 * 
 * @method
 */
DataStoreServer.prototype.setInteractiveSessionKey = function(interactiveSessionKey){
	this._internal.interactiveSessionKey = interactiveSessionKey;
}

/**
 * Returns the current session.
 * 
 * @method
 */
DataStoreServer.prototype.getInteractiveSessionKey = function(){
	return this._internal.interactiveSessionKey;
}

/**
 * Sets transactionManagerKey.
 * 
 * @method
 */
DataStoreServer.prototype.setTransactionManagerKey = function(transactionManagerKey){
	this._internal.transactionManagerKey = transactionManagerKey;
}

/**
 * Returns the current session.
 * 
 * @method
 */
DataStoreServer.prototype.getTransactionManagerKey = function(){
	return this._internal.transactionManagerKey;
}

DataStoreServer.prototype.fillCommonParameters = function(params) {
	if(this.getSession()) {
		params["sessionToken"] = this.getSession();
	}
	if(this.getInteractiveSessionKey()) {
		params["interactiveSessionKey"] = this.getInteractiveSessionKey();
	}
	if(this.getTransactionManagerKey()) {
		params["transactionManagerKey"] = this.getTransactionManagerKey();
	}
	return params;
}

const encodeParams = p =>  Object.entries(p).map(kv => kv.map(encodeURIComponent).join("=")).join("&");

/**
 * Log into DSS.
 * 
 * @method
 */
DataStoreServer.prototype.login = function(userId, userPassword, action) {
	var datastoreObj = this
	const data =  this.fillCommonParameters({
		"method": "login",
		"userId": userId,
		"password": userPassword
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain", 
		this._internal.datastoreUrl,
		encodeParams(data),
		function(loginResponse) {
			datastoreObj._internal.sessionToken = loginResponse;
			datastoreObj.rememberSession();
			action(loginResponse);
		}
	);

}


/**
 * Checks whether the current session is still active.
 *
 */
DataStoreServer.prototype.isSessionValid = function(action) {
	if(this.getSession()){
		const data =  this.fillCommonParameters({"method":"isSessionValid"});
		this._internal.sendHttpRequest(
			"GET",
			"text/plain",
			this._internal.datastoreUrl,
			encodeParams(data),
			(response) => parseJsonResponse(response, action)
		);
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
DataStoreServer.prototype.ifRestoredSessionActive = function(action) {
	this.restoreSession();
	this.isSessionValid(function(data) { if (data.result) action(data) });
}

/**
 * Log out of DSS.
 * 
 * @method
 */
DataStoreServer.prototype.logout = function(action) {
	this.forgetSession();
	
	if(this.getSession()){
		const data =  this.fillCommonParameters({"method":"logout"});
		this._internal.sendHttpRequest(
			"POST",
			"text/plain",
			this._internal.datastoreUrl,
			encodeParams(data),
			(response) => parseJsonResponse(response, action)
		);
	}else if(action){
		action({ result : null });
	}
}


/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.OperationsAPI methods
 * ==================================================================================
 */

/**
 * List files in the DSS for given owner and source
 */
DataStoreServer.prototype.list = function(owner, source, recursively, action){
	const data =  this.fillCommonParameters({
		"method": "list",
		"owner" :  owner,
		"source":  source,
		"recursively":  recursively
	});
	this._internal.sendHttpRequest(
		"GET",
		"text/plain",
		this._internal.buildGetUrl(data),
		{},
		(response) => parseJsonResponse(response, action)
	);
}

/**
 * Read the contents of selected file
 * @param {str} owner owner of the file 
 * @param {str} source path to file
 * @param {int} offset offset from whoch to start reading
 * @param {int} limit how many characters to read
 * @param {*} action post-processing action
 */
DataStoreServer.prototype.read = function(owner, source, offset, limit, action){
	const data =  this.fillCommonParameters({
		"method": "read",
		"owner" :  owner,
		"source":  source,
		"offset":  offset,
		"limit":  limit
	});
	this._internal.sendHttpRequest(
		"GET",
		"text",
		this._internal.buildGetUrl(data),
		{},
		(response) => action(response)
	);
}

/** Helper function to convert string md5Hash into an array. */
function hex2a(hexx) {
    var hex = hexx.toString(); //force conversion
    var str = '';
    for (var i = 0; i < hex.length; i += 2)
        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    return str;
}

/**
 * Write data to file (or create it)
 * @param {str} owner owner of the file
 * @param {str} source path to file
 * @param {int} offset offset from which to start writing
 * @param {str} data data to write
 * @param {*} action post-processing action
 */
DataStoreServer.prototype.write = function(owner, source, offset, data, action){
	const params =  this.fillCommonParameters({
		"method": "write",
		"owner" : owner,
		"source": source,
		"offset": offset,
		"data":  btoa(data),
		"md5Hash":  btoa(hex2a(md5(data))),
	});

	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(params),
		(response) => action(response)
	);
}

/**
 * Delete file from the DSS
 * @param {str} owner owner of the file
 * @param {str} source path to file
 * @param {*} action post-processing action 
 */
DataStoreServer.prototype.delete = function(owner, source, action){
	const data =  this.fillCommonParameters({
		"method": "delete",
		"owner" : owner,
		"source": source
	});
	this._internal.sendHttpRequest(
		"DELETE",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
}

/**
 * Copy file within DSS
 */
DataStoreServer.prototype.copy = function(sourceOwner, source, targetOwner, target, action){
	const data =  this.fillCommonParameters({
		"method": "copy",
		"sourceOwner" : sourceOwner,
		"source": source,
		"targetOwner": targetOwner,
		"target" : target
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
}

/** 
 * Move file within DSS
 */
DataStoreServer.prototype.move = function(sourceOwner, source, targetOwner, target, action){
	const data =  this.fillCommonParameters({
		"method": "move",
		"sourceOwner" : sourceOwner,
		"source": source,
		"targetOwner": targetOwner,
		"target" : target
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
	
}

/**
 * Create a file/directory within DSS
 */
DataStoreServer.prototype.create = function(owner, source, directory, action){
	const data =  this.fillCommonParameters({
		"method": "create",
		"owner" : owner,
		"source": source,
		"directory": directory
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
}


/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.TwoPhaseTransactionAPI methods
 * ==================================================================================
 */

DataStoreServer.prototype.begin = function(transactionId, action){
	const data =  this.fillCommonParameters({
		"method": "begin",
		"transactionId" : transactionId
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
	
}

DataStoreServer.prototype.prepare = function(action){
	const data =  this.fillCommonParameters({
		"method": "prepare"
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
	
}

DataStoreServer.prototype.commit = function(action){
	const data =  this.fillCommonParameters({
		"method": "commit"
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
	
}


DataStoreServer.prototype.rollback = function(action){
	const data =  this.fillCommonParameters({
		"method": "rollback"
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
}

DataStoreServer.prototype.recover = function(action){
	const data =  this.fillCommonParameters({
		"method": "recover"
	});
	this._internal.sendHttpRequest(
		"POST",
		"text/plain",
		this._internal.datastoreUrl,
		encodeParams(data),
		(response) => action(response)
	);
	
}

