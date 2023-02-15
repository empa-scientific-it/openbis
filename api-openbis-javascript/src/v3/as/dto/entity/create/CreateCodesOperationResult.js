/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateCodesOperationResult = function(codes) {
		this.codes = codes;
	};
	stjs.extend(CreateCodesOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entity.create.CreateCodesOperationResult';
		prototype.codes = null;

		prototype.getCodes = function() {
			return this.codes;
		};
		prototype.getMessage = function() {
			return "CreateCodesOperationResult";
		};
	}, {
		codes : {
			name : "List",
			arguments : [ "String" ]
		}
	});
	return CreateCodesOperationResult;
})