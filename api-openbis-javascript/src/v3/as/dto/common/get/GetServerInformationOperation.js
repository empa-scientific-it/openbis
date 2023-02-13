define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var GetServerInformationOperation = function() {
	};
	stjs.extend(GetServerInformationOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetServerInformationOperation';
		prototype.getMessage = function() {
			return "GetServerInformationOperation";
		};
	}, {
	});
	return GetServerInformationOperation;
})
