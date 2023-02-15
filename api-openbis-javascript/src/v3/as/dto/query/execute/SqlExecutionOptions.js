define([ "stjs" ], function(stjs) {
	var SqlExecutionOptions = function() {
		this.parameters = {};
	};
	stjs.extend(SqlExecutionOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.execute.SqlExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.databaseId = null;
		prototype.parameters = null;
		prototype.withDatabaseId = function(databaseId) {
			this.databaseId = databaseId;
			return this;
		}
		prototype.getDatabaseId = function() {
			return this.databaseId;
		}
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
		databaseId : "IQueryDatabaseId",
		parameters : {
			name : "Map",
			arguments : [ null, null ]
		}
	});
	return SqlExecutionOptions;
})