define([ "stjs", "as/dto/service/execute/AbstractExecutionOptionsWithParameters"], function(stjs, AbstractExecutionOptionsWithParameters) {
	var CustomASServiceExecutionOptions = function() {
		AbstractExecutionOptionsWithParameters.call(this);
	};
	stjs.extend(CustomASServiceExecutionOptions, AbstractExecutionOptionsWithParameters, [AbstractExecutionOptionsWithParameters ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.CustomASServiceExecutionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return CustomASServiceExecutionOptions;
})
