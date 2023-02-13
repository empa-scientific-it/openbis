define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var GetServerPublicInformationOperationResult = function(serverInformation) {
		this.serverInformation = serverInformation;
	};
	stjs.extend(GetServerPublicInformationOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetServerPublicInformationOperationResult';
		prototype.serverInformation = null;

		prototype.getServerInformation = function() {
			return this.serverInformation;
		};
		prototype.getMessage = function() {
			return "GetServerPublicInformationOperationResult";
		};
	}, {
		serverInformation : {
			name : "Map",
			arguments : [ "String", "String" ]
		}
	});
	return GetServerPublicInformationOperationResult;
})