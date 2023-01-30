define([ "stjs", "as/dto/service/execute/AbstractExecutionOptionsWithParameters"], function(stjs, AbstractExecutionOptionsWithParameters) {
	var AggregationServiceExecutionOptions = function() {
		AbstractExecutionOptionsWithParameters.call(this);
	};
	stjs.extend(AggregationServiceExecutionOptions, AbstractExecutionOptionsWithParameters, [AbstractExecutionOptionsWithParameters ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.AggregationServiceExecutionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return AggregationServiceExecutionOptions;
})
