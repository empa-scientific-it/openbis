define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/dataset/id/IContentCopyId" ], function(stjs, ObjectPermId, IContentCopyId) {
	var ContentCopyPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(ContentCopyPermId, ObjectPermId, [ ObjectPermId, IContentCopyId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.id.ContentCopyPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return ContentCopyPermId;
})