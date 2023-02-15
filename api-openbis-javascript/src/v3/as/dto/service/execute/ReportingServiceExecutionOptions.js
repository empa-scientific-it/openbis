define([ "stjs"], function(stjs) {
	var ReportingServiceExecutionOptions = function() {
		this.dataSetCodes = [];
	};
	stjs.extend(ReportingServiceExecutionOptions, null, [ ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ReportingServiceExecutionOptions';
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
	return ReportingServiceExecutionOptions;
})
