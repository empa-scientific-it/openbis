define([ "stjs"], function(stjs) {
	var AbstractExecutionOptionsWithParameters = function() {
		this.parameters = {};
	};
	stjs.extend(AbstractExecutionOptionsWithParameters, null, [ ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.service.execute.AbstractExecutionOptionsWithParameters';
		constructor.serialVersionUID = 1;
		prototype.parameters = null;
		prototype.withParameter = function(parameterName, value) {
			this.parameters[parameterName] = value;
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
	return AbstractExecutionOptionsWithParameters;
})