/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var DeleteObjectsOperation = function(objectIds, options) {
		this.objectIds = objectIds;
		this.options = options;
	};
	stjs.extend(DeleteObjectsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.delete.DeleteObjectsOperation';
		prototype.objectIds = null;
		prototype.options = null;
		prototype.getObjectIds = function() {
			return this.objectIds;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "DeleteObjectsOperation";
		};
	}, {
		objectIds : {
			name : "List",
			arguments : [ "IObjectId" ]
		},
		options : "AbstractObjectDeletionOptions"
	});
	return DeleteObjectsOperation;
})