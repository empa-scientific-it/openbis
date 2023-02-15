define([ "stjs", "as/dto/service/execute/AbstractExecutionOptionsWithParameters"], function(stjs, AbstractExecutionOptionsWithParameters) {
	var ProcessingServiceExecutionOptions = function() {
		AbstractExecutionOptionsWithParameters.call(this);
		this.dataSetCodes = [];
	};
	stjs.extend(ProcessingServiceExecutionOptions, AbstractExecutionOptionsWithParameters, [AbstractExecutionOptionsWithParameters], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ProcessingServiceExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.dataSetCodes = null;
		prototype.withDataSets = function(dataSetCodes) {
			if (Object.prototype.toString.call( dataSetCodes ) === '[object Array]' ) {
				this.dataSetCodes = this.dataSetCodes.concat(dataSetCodes);
			} else {
				this.dataSetCodes.push(dataSetCodes);
			}
		};
		prototype.getDataSetCodes = function() {
			return this.dataSetCodes;
		};
	}, {});
	return ProcessingServiceExecutionOptions;
})
