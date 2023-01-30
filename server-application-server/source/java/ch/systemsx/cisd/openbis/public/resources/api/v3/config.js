var require = (function() {

	var getBaseUrl = function() {
		// To find where the V3 API has been loaded from we scan the script tags until finding this file
		// This way is possible to load the V3 API from a separate domain
		var scripts = document.getElementsByTagName("script");
		var baseUrl = null;
		for(var sIdx = 0; sIdx < scripts.length; sIdx++) {
			var src = scripts[sIdx].src;
			var substringEnd = -1;
			if((substringEnd = src.indexOf("/api/v3/config.js")) > -1) {
				baseUrl = src.substring(0, substringEnd+"/api/v3".length);
			}
		}
		return baseUrl;
	}

	return {
		baseUrl : getBaseUrl(),
		paths : {
			"jquery" : "lib/jquery/js/jquery",
			"stjs" : "lib/stjs/js/stjs",
			"underscore" : "lib/underscore/js/underscore",
			"moment" : "lib/moment/js/moment",
			// Backwards compatibility with V1 Components
			"components" : "../../components",
			// "openbis" : "../../js/openbis",
			"openbis-screening" : "../../js/openbis-screening",
			"bootstrap" : "../../lib/bootstrap/js/bootstrap.min",
			"bootstrap-slider" : "../../lib/bootstrap-slider/js/bootstrap-slider.min"
		},
		shim : {
			"stjs" : {
				exports : "stjs",
				deps : [ "underscore" ]
			},
			"underscore" : {
				exports : "_"
			},
			// Backwards compatibility with V1 Components
			// "openbis" : {
			// 	deps : [ "jquery" ],
			// 	exports : "openbis"
			// },
			"openbis-screening" : {
				deps : [ "openbis" ],
				exports : "openbis"
			}
		}
	}

})();