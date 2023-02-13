define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var GetServerPublicInformationOperation = function() {
	};
	stjs.extend(GetServerPublicInformationOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetServerPublicInformationOperation';
		prototype.getMessage = function() {
			return "GetServerPublicInformationOperation";
		};
	}, {
	});
	return GetServerPublicInformationOperation;
})
