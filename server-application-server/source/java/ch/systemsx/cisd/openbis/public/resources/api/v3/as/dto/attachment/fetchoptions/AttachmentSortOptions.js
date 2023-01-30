define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var AttachmentSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(AttachmentSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.attachment.fetchoptions.AttachmentSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return AttachmentSortOptions;
})