/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var OperationExecutionDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(OperationExecutionDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.delete.OperationExecutionDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return OperationExecutionDeletionOptions;
})