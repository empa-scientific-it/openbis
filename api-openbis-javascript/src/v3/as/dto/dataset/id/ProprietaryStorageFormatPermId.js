/**
 * Proprietary storage format perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/dataset/id/StorageFormatPermId" ], function(stjs, StorageFormatPermId) {
	var ProprietaryStorageFormatPermId = function() {
		StorageFormatPermId.call(this, "PROPRIETARY");
	};
	stjs.extend(ProprietaryStorageFormatPermId, StorageFormatPermId, [ StorageFormatPermId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.id.ProprietaryStorageFormatPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return ProprietaryStorageFormatPermId;
})