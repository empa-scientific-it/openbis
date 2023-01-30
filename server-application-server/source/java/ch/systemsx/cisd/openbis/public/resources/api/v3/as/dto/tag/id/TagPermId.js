/**
 * Tag perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/tag/id/ITagId" ], function(stjs, ObjectPermId, ITagId) {
	/**
	 * @param permId
	 *            Tag perm id, e.g. "/MY_USER/MY_TAG".
	 */
	var TagPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(TagPermId, ObjectPermId, [ ObjectPermId, ITagId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.id.TagPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return TagPermId;
})