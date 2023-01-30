/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var CreatePermIdsOperation = function(count) {
		this.count = count;
	};
	stjs.extend(CreatePermIdsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entity.create.CreatePermIdsOperation';
		prototype.count = null;

		prototype.getCount = function() {
			return this.count;
		};
		prototype.getMessage = function() {
			return "CreatePermIdsOperation";
		};
	}, {
	});
	return CreatePermIdsOperation;
})
