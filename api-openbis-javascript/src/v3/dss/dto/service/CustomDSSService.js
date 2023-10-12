define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var CustomDSSService = function() {
	};
	stjs.extend(CustomDSSService, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.service.CustomDSSService';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.toString = function() {
			return "CustomDSSService " + this.code;
		};
	}, {
		fetchOptions : "CustomDSSServiceFetchOptions",
		code: "CustomDssServiceCode"
	});
	return CustomDSSService;
})