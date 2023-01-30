/**
 * Caches openBIS JSON responses into the local browser storage.
 * 
 * Once the local storage is populated with data from real 
 * server interactions, the UI development can be done offline
 * based on the cached server responses.
 * 
 * USAGE : include this script in your app *after* the openbis facade.
 * Be careful not to distribute it to your customers.
 *  
 * <script type="text/javascript" src="openbis-request-cache.js"></script>
 * 
 * To clear the cache, you can call "localStorage.clear()" in a browser console. 
 */

var original_ajax_request_func = _openbisInternal.prototype.ajaxRequest;

_openbisInternal.prototype.ajaxRequest = function(settings) {
	
	function getCacheId(settings) {
		var methodName = settings.data['method']
		if (methodName.toLowerCase().indexOf('authenticate') != -1) {
			// do not store sensitive parameters information
			// for login methods (e.g. username/password)
			return methodName
		} else {
			var params = settings.data['params']
			return methodName + '-' + JSON.stringify(params)
		}
	}
	
	var cacheId = getCacheId(settings)
	var cachedResponse = localStorage.getItem(cacheId)
	
	if (cachedResponse == null) {
		var originalCallback = settings.success
		settings.success = function(response) {
			localStorage.setItem(cacheId, JSON.stringify(response))
			originalCallback(response)
		}
		original_ajax_request_func(settings);
	} else {
		// async execution after a delay of 100ms 
		setTimeout(function() {
			settings.success(JSON.parse(cachedResponse))	
		}, 100)
		
	}
}

alert("This app includes development code. For production, do not forget to remove the reference to openbis-request-cache.js")