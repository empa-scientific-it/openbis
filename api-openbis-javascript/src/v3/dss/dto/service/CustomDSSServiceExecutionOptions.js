define([ "stjs", "dss/dto/service/execute/AbstractExecutionOptionsWithParameters"], function(stjs, AbstractExecutionOptionsWithParameters) {
	var CustomDSSServiceExecutionOptions = function() {
		AbstractExecutionOptionsWithParameters.call(this);
	};
	stjs.extend(CustomDSSServiceExecutionOptions, AbstractExecutionOptionsWithParameters, [AbstractExecutionOptionsWithParameters ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.service.CustomDSSServiceExecutionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return CustomDSSServiceExecutionOptions;
})
