define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var GetServerInformationOperationResult = function(serverInformation) {
		this.serverInformation = serverInformation;
	};
	stjs.extend(GetServerInformationOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetServerInformationOperationResult';
		prototype.serverInformation = null;

		prototype.getServerInformation = function() {
			return this.serverInformation;
		};
		prototype.getMessage = function() {
			return "GetServerInformationOperationResult";
		};
	}, {
		serverInformation : {
			name : "Map",
			arguments : [ "String", "String" ]
		}
	});
	return GetServerInformationOperationResult;
})