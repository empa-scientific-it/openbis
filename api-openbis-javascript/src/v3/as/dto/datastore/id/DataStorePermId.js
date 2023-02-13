/**
 * Data store perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/datastore/id/IDataStoreId" ], function(stjs, ObjectPermId, IDataStoreId) {

	/**
	 * @param permId
	 *            Data store perm id, e.g. "DSS1".
	 */
	var DataStorePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(DataStorePermId, ObjectPermId, [ ObjectPermId, IDataStoreId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.datastore.id.DataStorePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return DataStorePermId;
})