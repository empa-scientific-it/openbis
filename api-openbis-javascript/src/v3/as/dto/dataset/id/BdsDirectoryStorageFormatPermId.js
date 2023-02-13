/**
 * Bds directory storage format perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/dataset/id/StorageFormatPermId" ], function(stjs, StorageFormatPermId) {
	var BdsDirectoryStorageFormatPermId = function() {
		StorageFormatPermId.call(this, "BDS_DIRECTORY");
	};
	stjs.extend(BdsDirectoryStorageFormatPermId, StorageFormatPermId, [ StorageFormatPermId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.id.BdsDirectoryStorageFormatPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return BdsDirectoryStorageFormatPermId;
})