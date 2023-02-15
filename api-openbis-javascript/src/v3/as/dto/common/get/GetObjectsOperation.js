/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var GetObjectsOperation = function(objectIds, fetchOptions) {
		this.objectIds = objectIds;
		this.fetchOptions = fetchOptions;
	};
	stjs.extend(GetObjectsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetObjectsOperation';
		prototype.objectIds = null;
		prototype.fetchOptions = null;
		prototype.getObjectIds = function() {
			return this.objectIds;
		};
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.getMessage = function() {
			return "GetObjectsOperation";
		};
	}, {
		objectIds : {
			name : "List",
			arguments : [ "IObjectId" ]
		},
		fetchOptions : "FetchOptions"
	});
	return GetObjectsOperation;
})