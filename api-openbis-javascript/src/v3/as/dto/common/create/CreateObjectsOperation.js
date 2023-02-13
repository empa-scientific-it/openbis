/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var CreateObjectsOperation = function(creations) {
		this.creations = creations;
	};
	stjs.extend(CreateObjectsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.create.CreateObjectsOperation';
		prototype.creations = null;
		prototype.getCreations = function() {
			return this.creations;
		};
		prototype.getMessage = function() {
			return "CreateObjectsOperation";
		};
	}, {
		creations : {
			name : "List",
			arguments : [ "IObjectCreation" ]
		}
	});
	return CreateObjectsOperation;
})