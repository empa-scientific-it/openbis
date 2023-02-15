define([ "stjs" ], function(stjs) {
	var AggregationService = function() {
	};
	stjs.extend(AggregationService, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.AggregationService';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.name = null;
		prototype.label = null;

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
		prototype.toString = function() {
			return "AggregationService: " + this.permId;
		};
	}, {
		fetchOptions : "AggregationServiceFetchOptions"
	});
	return AggregationService;
})