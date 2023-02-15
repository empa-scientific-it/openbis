/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/ListUpdateValue" ], function(stjs, ListUpdateValue) {
	var AttachmentListUpdateValue = function() {
		ListUpdateValue.call(this);
	};
	stjs.extend(AttachmentListUpdateValue, ListUpdateValue, [ ListUpdateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.attachment.update.AttachmentListUpdateValue';
		constructor.serialVersionUID = 1;
	}, {
		actions : {
			name : "List",
			arguments : [ {
				name : "ListUpdateValue.ListUpdateAction",
				arguments : [ "ACTION" ]
			} ]
		}
	});
	return AttachmentListUpdateValue;
})