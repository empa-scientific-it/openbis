/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/deletion/fetchoptions/DeletedObjectFetchOptions", "as/dto/deletion/fetchoptions/DeletionSortOptions" ], function(require, stjs,
		FetchOptions) {
	var DeletionFetchOptions = function() {
	};
	stjs.extend(DeletionFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.fetchoptions.DeletionFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.deletedObjects = null;
		prototype.sort = null;
		prototype.withDeletedObjects = function() {
			if (this.deletedObjects == null) {
				var DeletedObjectFetchOptions = require("as/dto/deletion/fetchoptions/DeletedObjectFetchOptions");
				this.deletedObjects = new DeletedObjectFetchOptions();
			}
			return this.deletedObjects;
		};
		prototype.withDeletedObjectsUsing = function(deletedObjects) {
			this.deletedObjects = deletedObjects;
		};
		prototype.hasDeletedObjects = function() {
			return this.deletedObjects != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var DeletionSortOptions = require("as/dto/deletion/fetchoptions/DeletionSortOptions");
				this.sort = new DeletionSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		deletedObjects : "DeletedObjectFetchOptions",
		sort : "DeletionSortOptions"
	});
	return DeletionFetchOptions;
})