/**
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Deletion = function() {
		this.deletedObjects = [];
	};
	stjs.extend(Deletion, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.Deletion';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.id = null;
		prototype.reason = null;
		prototype.deletedObjects = null;
		prototype.deletionDate = null;
        prototype.totalExperimentsCount = null;
        prototype.totalSamplesCount = null;
        prototype.totalDataSetsCount = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
		prototype.getReason = function() {
			return this.reason;
		};
		prototype.setReason = function(reason) {
			this.reason = reason;
		};
		prototype.getDeletedObjects = function() {
            return this._getIfItHasDeletedObjects(this.deletedObjects);
		};
		prototype.setDeletedObjects = function(deletedObjects) {
			this.deletedObjects = deletedObjects;
		};
		prototype.getDeletionDate = function() {
			return this.deletionDate;
		};
		prototype.setDeletionDate = function(deletionDate) {
			this.deletionDate = deletionDate;
		};
        prototype.getTotalExperimentsCount = function() {
            return this._getIfItHasDeletedObjects(this.totalExperimentsCount);
        };
        prototype.setTotalExperimentsCount = function(totalExperimentsCount) {
            this.totalExperimentsCount = totalExperimentsCount;
        };
        prototype.getTotalSamplesCount = function() {
            return this._getIfItHasDeletedObjects(this.totalSamplesCount);
        };
        prototype.setTotalSamplesCount = function(totalSamplesCount) {
            this.totalSamplesCount = totalSamplesCount;
        };
        prototype.getTotalDataSetsCount = function() {
            return this._getIfItHasDeletedObjects(this.totalSamplesCount);
        };
        prototype.setTotalDataSetsCount = function(totalDataSetsCount) {
            this.totalDataSetsCount = totalDataSetsCount;
        };
        prototype._getIfItHasDeletedObjects = function(value) {
            if (this.getFetchOptions() && this.getFetchOptions().hasDeletedObjects()) {
                return value;
            } else {
                throw new exceptions.NotFetchedException("Deleted objects have not been fetched.");
            }
        };
	}, {
		fetchOptions : "DeletionFetchOptions",
		id : "IDeletionId",
		deletedObjects : {
			name : "List",
			arguments : [ "DeletedObject" ]
		},
		deletionDate : "Date"
	});
	return Deletion;
})