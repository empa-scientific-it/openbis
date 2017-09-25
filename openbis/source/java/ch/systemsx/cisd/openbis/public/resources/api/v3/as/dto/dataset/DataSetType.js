/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs" ], function(stjs) {
	var DataSetType = function() {
	};
	stjs.extend(DataSetType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.DataSetType';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.description = null;
		prototype.mainDataSetPattern = null;
		prototype.mainDataSetPath = null;
		prototype.disallowDeletion = null;
		prototype.modificationDate = null;
		prototype.propertyAssignments = null;
		prototype.getPropertyAssignments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertyAssignments()) {
				return this.propertyAssignments;
			} else {
				throw new exceptions.NotFetchedException("Property assignments have not been fetched.");
			}
		};
		prototype.setPropertyAssignments = function(propertyAssignments) {
			this.propertyAssignments = propertyAssignments;
		};
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getMainDataSetPattern = function() {
			return this.mainDataSetPattern;
		};
		prototype.setMainDataSetPattern = function(mainDataSetPattern) {
			this.mainDataSetPattern = mainDataSetPattern;
		};
		prototype.getMainDataSetPath = function() {
			return this.mainDataSetPath;
		};
		prototype.setMainDataSetPath = function(mainDataSetPath) {
			this.mainDataSetPath = mainDataSetPath;
		};
		prototype.isDisallowDeletion = function() {
			return this.disallowDeletion;
		};
		prototype.setDisallowDeletion = function(disallowDeletion) {
			this.disallowDeletion = disallowDeletion;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.toString = function() {
			return this.getCode();
		};
	}, {
		fetchOptions : "DataSetTypeFetchOptions",
		permId : "EntityTypePermId",
		modificationDate : "Date"
	});
	return DataSetType;
})