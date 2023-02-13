define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var CreatePermIdsOperationResult = function(permIds) {
		this.permIds = permIds;
	};
	stjs.extend(CreatePermIdsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entity.create.CreatePermIdsOperationResult';
		prototype.permIds = null;

		prototype.getPermIds = function() {
			return this.permIds;
		};
		prototype.getMessage = function() {
			return "CreatePermIdsOperationResult";
		};
	}, {
		permIds : {
			name : "List",
			arguments : [ "String" ]
		}
	});
	return CreatePermIdsOperationResult;
})
