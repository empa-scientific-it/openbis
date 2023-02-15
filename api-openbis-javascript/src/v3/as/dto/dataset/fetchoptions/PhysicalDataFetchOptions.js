/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/dataset/fetchoptions/StorageFormatFetchOptions", "as/dto/dataset/fetchoptions/FileFormatTypeFetchOptions",
		"as/dto/dataset/fetchoptions/LocatorTypeFetchOptions", "as/dto/dataset/fetchoptions/PhysicalDataSortOptions" ], function(require, stjs, FetchOptions) {
	var PhysicalDataFetchOptions = function() {
	};
	stjs.extend(PhysicalDataFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.PhysicalDataFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.storageFormat = null;
		/*
		 * @Deprecated
		 */
		prototype.fileFormatType = null;
		prototype.locatorType = null;
		prototype.sort = null;
		prototype.withStorageFormat = function() {
			if (this.storageFormat == null) {
				var StorageFormatFetchOptions = require("as/dto/dataset/fetchoptions/StorageFormatFetchOptions");
				this.storageFormat = new StorageFormatFetchOptions();
			}
			return this.storageFormat;
		};
		prototype.withStorageFormatUsing = function(fetchOptions) {
			return this.storageFormat = fetchOptions;
		};
		prototype.hasStorageFormat = function() {
			return this.storageFormat != null;
		};
		/*
		 * @Deprecated
		 */
		prototype.withFileFormatType = function() {
			if (this.fileFormatType == null) {
				var FileFormatTypeFetchOptions = require("as/dto/dataset/fetchoptions/FileFormatTypeFetchOptions");
				this.fileFormatType = new FileFormatTypeFetchOptions();
			}
			return this.fileFormatType;
		};
		/*
		 * @Deprecated
		 */
		prototype.withFileFormatTypeUsing = function(fetchOptions) {
			return this.fileFormatType = fetchOptions;
		};
		/*
		 * @Deprecated
		 */
		prototype.hasFileFormatType = function() {
			return this.fileFormatType != null;
		};
		prototype.withLocatorType = function() {
			if (this.locatorType == null) {
				var LocatorTypeFetchOptions = require("as/dto/dataset/fetchoptions/LocatorTypeFetchOptions");
				this.locatorType = new LocatorTypeFetchOptions();
			}
			return this.locatorType;
		};
		prototype.withLocatorTypeUsing = function(fetchOptions) {
			return this.locatorType = fetchOptions;
		};
		prototype.hasLocatorType = function() {
			return this.locatorType != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PhysicalDataSortOptions = require("as/dto/dataset/fetchoptions/PhysicalDataSortOptions");
				this.sort = new PhysicalDataSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		storageFormat : "StorageFormatFetchOptions",
		fileFormatType : "FileFormatTypeFetchOptions",
		locatorType : "LocatorTypeFetchOptions",
		sort : "PhysicalDataSortOptions"
	});
	return PhysicalDataFetchOptions;
})