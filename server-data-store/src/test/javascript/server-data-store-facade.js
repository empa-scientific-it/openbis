/**
 * ======================================================
 * OpenBIS Datastore facade internal code (DO NOT USE!!!)
 * ======================================================
 */

if(typeof $ == 'undefined'){
	alert('Loading of openbis.js failed - jquery.js is missing');
}

function _datastoreInternal(datastoreUrlOrNull, httpServerUri){
	this.init(datastoreUrlOrNull, httpServerUri);
}

_datastoreInternal.prototype.init = function(datastoreUrlOrNull, httpServerUri){
	this.datastoreUrl = this.normalizeUrl(datastoreUrlOrNull, httpServerUri);
	this.httpServerUri = httpServerUri;
}

_datastoreInternal.prototype.log = function(msg){
	if(console){
		console.log(msg);
	}
}

_datastoreInternal.prototype.normalizeUrl = function(openbisUrlOrNull, httpServerUri){
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

_datastoreInternal.prototype.getUrlForMethod = function(method) {
    return this.datastoreUrl + "?method=" + method;
}

_datastoreInternal.prototype.jsonRequestData = function(params) {
	return JSON.stringify(params);
}
 
_datastoreInternal.prototype.ajaxRequest = function(settings) {
	settings.processData = false;
	settings.jsonp = false;
	settings.data = this.jsonRequestData(settings.data);
	settings.success = this.ajaxRequestSuccess(settings.success);
	// we call the same settings.success function for backward compatibility
	settings.error = this.ajaxRequestError(settings.success);
	$.ajax(settings)
}

_datastoreInternal.prototype.ajaxRequestWithQueryParams = function(settings) {
	settings.processData = false;
	settings.jsonp = false;
	settings.data = jQuery.param( settings.data );
	settings.success = this.ajaxRequestSuccess(settings.success);
	// we call the same settings.success function for backward compatibility
	settings.error = this.ajaxRequestError(settings.success);
	$.ajax(settings)
}

_datastoreInternal.prototype.responseInterceptor = function(response, action) {
	action(response);
}

_datastoreInternal.prototype.ajaxRequestSuccess = function(action){
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

_datastoreInternal.prototype.ajaxRequestError = function(action){
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

_datastoreInternal.prototype.createCookie = function(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

_datastoreInternal.prototype.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

_datastoreInternal.prototype.eraseCookie = function(name) {
	this.createCookie(name,"",-1);
}

// parseUri 1.2.2 (c) Steven Levithan <stevenlevithan.com> MIT License (see http://blog.stevenlevithan.com/archives/parseuri)

_datastoreInternal.prototype.parseUri = function(str) {
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



/**
 * ===============
 * DSS facade
 * ===============
 * 
 * The facade provides access to the DSS methods
 * 
 */
function datastore(datastoreUrlOrNull, httpServerUri) {
	this._internal = new _datastoreInternal(datastoreUrlOrNull, httpServerUri);
}

/**
 * Intercepts responses so clients can handle generic errors like session timeouts with a
 * single handler.
 * 
 * @method
 */
datastore.prototype.setResponseInterceptor = function(responseInterceptor) {
	this._internal.responseInterceptor = responseInterceptor;
}



/**
 * ==================================================================================
 * ch.ethz.sis.afsapi.api.AuthenticationAPI methods
 * ==================================================================================
 */

/**
 * Log into DSS.
 * 
 * @method
 */
datastore.prototype.login = function(userId, userPassword, action) {
	var datastoreObj = this
	this._internal.ajaxRequest({
        type: "POST",
		dataType: "json",
		url: this._internal.getUrlForMethod("login"),
        data: {
            "userId": userId,
            "password": userPassword
        },
		success: 
			function(loginResponse) {
				if(loginResponse.error){
					alert("Login failed: " + loginResponse.error.message);
				}else{
					datastoreObj._internal.sessionToken = loginResponse.result;
					datastoreObj.rememberSession();
                    action(loginResponse);
				}
			}
	 });
}

/**
 * Stores the current session in a cookie. 
 *
 * @method
 */
datastore.prototype.rememberSession = function() {
	this._internal.createCookie('datastore', this.getSession(), 1);
}

/**
 * Removes the current session from a cookie. 
 *
 * @method
 */
datastore.prototype.forgetSession = function() {
	this._internal.eraseCookie('datastore');
}

/**
 * Restores the current session from a cookie.
 *
 * @method
 */
datastore.prototype.restoreSession = function() {
	this._internal.sessionToken = this._internal.readCookie('datastore');
}

/**
 * Sets the current session.
 *
 * @method
 */
datastore.prototype.useSession = function(sessionToken){
	this._internal.sessionToken = sessionToken;
}

/**
 * Returns the current session.
 * 
 * @method
 */
datastore.prototype.getSession = function(){
	return this._internal.sessionToken;
}

/**
 * Checks whether the current session is still active.
 *
 */
datastore.prototype.isSessionValid = function(action) {
	if(this.getSession()){
		this._internal.ajaxRequest({
			type: "GET",
			dataType: "json",
			url: this._internal.getUrlForMethod("isSessionValid"),
			data: { "sessionToken" : this.getSession()
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
datastore.prototype.ifRestoredSessionActive = function(action) {
	this.restoreSession();
	this.isSessionValid(function(data) { if (data.result) action(data) });
}

/**
 * Log out of DSS.
 * 
 * @method
 */
datastore.prototype.logout = function(action) {
	this.forgetSession();
	
	if(this.getSession()){
		this._internal.ajaxRequest({
			type: "POST",
			dataType: "json",
			url: this._internal.getUrlForMethod("logout"),
			data: { "sessionToken" : this.getSession()
				  },
			success: action
		});
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
datastore.prototype.list = function(owner, source, recursively, action){
	this._internal.ajaxRequestWithQueryParams({
		type: "GET",
		dataType: "json",
		url: this._internal.getUrlForMethod("list"),
		data: {
				"owner" :  owner,
				"source":  source,
				"recursively":  recursively,
				"sessionToken" :  this.getSession()
		},
		success: action
	});
}

/**
 * Read the contents of selected file
 * @param {str} owner owner of the file 
 * @param {str} source path to file
 * @param {int} offset offset from whoch to start reading
 * @param {int} limit how many characters to read
 * @param {*} action post-processing action
 */
datastore.prototype.read = function(owner, source, offset, limit, action){
	this._internal.ajaxRequestWithQueryParams({
		type: "GET",
		dataType: "text",
		url: this._internal.getUrlForMethod("read"),
		data: {
				"owner" : owner,
				"source": source,
				"offset":  offset,
				"limit":  limit,
				"sessionToken" : this.getSession()
		},
		success: action
	});
}

/** Helper function to encode string into byte array */
String.prototype.encodeHex = function () {
    return this.split('').map(e => e.charCodeAt())
};

/** Helper function to convert string md5Hash into byte array that Java MessageDigest uses. */
function convertToBin(md5hash) {
	var res = []
	for(var i=0;i<md5hash.length;i+=2){
		var val = parseInt(md5hash.substring(i, i+2), 16);
		res.push(val >= 128 ? -256+val : val);
	}
	return res;
}

/**
 * Write data to file (or create it)
 * @param {str} owner owner of the file
 * @param {str} source path to file
 * @param {int} offset offset from which to start writing
 * @param {str} data data to write
 * @param {*} action post-processing action
 */
datastore.prototype.write = function(owner, source, offset, data, action){
	let md5Hash = md5(data).toUpperCase();
	this._internal.ajaxRequest({
		type: "POST",
		dataType: "json",
		url: this._internal.getUrlForMethod("write"),
		data: {
				"owner" : ["java.lang.String", owner],
				"source": ["java.lang.String", source],
				"offset": [ "java.lang.Long", offset ],
				"data":  [ "[B", data.encodeHex() ],
				"md5Hash":  [ "[B", convertToBin(md5Hash)],
				"sessionToken" : ["java.lang.String", this.getSession()]
		},
		success: action
	});
}

/**
 * Delete file from the DSS
 * @param {str} owner owner of the file
 * @param {str} source path to file
 * @param {*} action post-processing action 
 */
datastore.prototype.delete = function(owner, source, action){
	this._internal.ajaxRequest({
		type: "DELETE",
		dataType: "json",
		url: this._internal.getUrlForMethod("delete"),
		data: {
				"owner" : ["java.lang.String", owner],
				"source": ["java.lang.String", source],
				"sessionToken" : ["java.lang.String", this.getSession()]
		},
		success: action
	});
}

/**
 * Copy file within DSS
 */
datastore.prototype.copy = function(sourceOwner, source, targetOwner, target, action){
	this._internal.ajaxRequest({
		type: "POST",
		dataType: "json",
		url: this._internal.getUrlForMethod("copy"),
		data: {
				"sourceOwner" : ["java.lang.String", sourceOwner],
				"source": ["java.lang.String", source],
				"targetOwner": ["java.lang.String", targetOwner],
				"target": ["java.lang.String", target],
				"sessionToken" : ["java.lang.String", this.getSession()]
		},
		success: action
	});
}

/** 
 * Move file within DSS
 */
datastore.prototype.move = function(sourceOwner, source, targetOwner, target, action){
	this._internal.ajaxRequest({
		type: "POST",
		dataType: "json",
		url: this._internal.getUrlForMethod("move"),
		data: {
				"sourceOwner" : ["java.lang.String", sourceOwner],
				"source": ["java.lang.String", source],
				"targetOwner": ["java.lang.String", targetOwner],
				"target": ["java.lang.String", target],
				"sessionToken" : ["java.lang.String", this.getSession()]
		},
		success: action
	});
}









