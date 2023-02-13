/**
 * Storage format perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/dataset/id/IStorageFormatId" ], function(stjs, ObjectPermId, IStorageFormatId) {
	/**
	 * @param permId
	 *            Storage format perm id, e.g. "PROPRIETARY".
	 */
	var StorageFormatPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(StorageFormatPermId, ObjectPermId, [ ObjectPermId, IStorageFormatId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.id.StorageFormatPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return StorageFormatPermId;
})