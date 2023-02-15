/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var UpdateObjectsOperationResult = function(objectIds) {
		this.objectIds = objectIds;
	};
	stjs.extend(UpdateObjectsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.UpdateObjectsOperationResult';
		prototype.objectIds = null;
		prototype.getObjectIds = function() {
			return this.objectIds;
		};
		prototype.getMessage = function() {
			return "UpdateObjectsOperationResult";
		};
	}, {
		objectIds : {
			name : "List",
			arguments : [ "IObjectId" ]
		}
	});
	return UpdateObjectsOperationResult;
})