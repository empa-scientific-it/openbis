define([ "stjs" ], function(stjs) {
	var ProcessingService = function() {
	};
	stjs.extend(ProcessingService, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.ProcessingService';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.name = null;
		prototype.label = null;
		prototype.dataSetTypeCodes = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDataSetTypeCodes = function() {
			return this.dataSetTypeCodes;
		};
		prototype.setDataSetTypeCodes = function(dataSetTypeCodes) {
			this.dataSetTypeCodes = dataSetTypeCodes;
		};
		prototype.toString = function() {
			return "ProcessingService: " + this.permId;
		};
	}, {
		fetchOptions : "ProcessingServiceFetchOptions"
	});
	return ProcessingService;
})