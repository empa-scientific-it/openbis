/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var GetSessionInformationOperation = function() {
	};
	stjs.extend(GetSessionInformationOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.get.GetSessionInformationOperation';
		prototype.getMessage = function() {
			return "GetSessionInformationOperation";
		};
	}, {});
	return GetSessionInformationOperation;
})