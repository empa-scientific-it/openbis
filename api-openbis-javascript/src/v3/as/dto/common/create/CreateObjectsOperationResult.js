/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateObjectsOperationResult = function(objectIds) {
		this.objectIds = objectIds;
	};
	stjs.extend(CreateObjectsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.create.CreateObjectsOperationResult';
		prototype.objectIds = null;
		prototype.getObjectIds = function() {
			return this.objectIds;
		};
		prototype.getMessage = function() {
			return "CreateObjectsOperationResult";
		};
	}, {
		creations : {
			name : "List",
			arguments : [ "IObjectId" ]
		}
	});
	return CreateObjectsOperationResult;
})