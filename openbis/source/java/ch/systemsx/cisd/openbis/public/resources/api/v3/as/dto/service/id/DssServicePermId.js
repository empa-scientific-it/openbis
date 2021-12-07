define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/service/id/IDssServiceId" ], function(stjs, ObjectPermId, IDssServiceId) {
	var DssServicePermId = function(permId, dataStoreId) {
		ObjectPermId.call(this, permId);
		this.dataStoreId = dataStoreId;
	};
	stjs.extend(DssServicePermId, ObjectPermId, [ObjectPermId, IDssServiceId], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.id.DssServicePermId';
		constructor.serialVersionUID = 1;
		prototype.dataStoreId = null;
		prototype.getDataStoreId = function() {
			return this.dataStoreId;
		};
		prototype.toString = function() {
			return this.getDataStoreId() + ":" + this.getPermId();
		};
	}, {
		dataStoreId: "IDataStoreId"
	});
	return DssServicePermId;
})
