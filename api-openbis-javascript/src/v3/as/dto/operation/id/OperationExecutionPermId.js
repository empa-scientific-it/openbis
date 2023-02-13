/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/operation/id/IOperationExecutionId" ], function(stjs, ObjectPermId, IOperationExecutionId) {
	var OperationExecutionPermId = function(permId) {
		if (!permId) {
			// http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
			permId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
				var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
				return v.toString(16);
			});
		}
		ObjectPermId.call(this, permId);
	};
	stjs.extend(OperationExecutionPermId, ObjectPermId, [ ObjectPermId, IOperationExecutionId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.id.OperationExecutionPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return OperationExecutionPermId;
})