define([ "stjs" ], function(stjs) {
	var QueryExecutionOptions = function() {
		this.parameters = {};
	};
	stjs.extend(QueryExecutionOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.execute.QueryExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.parameters = null;
		prototype.withParameter = function(parameterName, value) {
			this.parameters[parameterName] = value;
			return this;
		}
		prototype.withParameters = function(parameters) {
			this.parameters = parameters;
			return this;
		}
		prototype.getParameters = function() {
			return this.parameters;
		}
	}, {
		parameters : {
			name : "Map",
			arguments : [ null, null ]
		}
	});
	return QueryExecutionOptions;
})