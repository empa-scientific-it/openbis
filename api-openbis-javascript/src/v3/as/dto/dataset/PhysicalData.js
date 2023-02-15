/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PhysicalData = function() {
	};
	stjs.extend(PhysicalData, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.PhysicalData';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.shareId = null;
		prototype.location = null;
		prototype.size = null;
		prototype.storageFormat = null;
		/*
		 * @Deprecated
		 */
		prototype.fileFormatType = null;
		prototype.locatorType = null;
		prototype.complete = null;
		prototype.status = null;
		prototype.presentInArchive = null;
		prototype.storageConfirmation = null;
		prototype.speedHint = null;
		prototype.archivingRequested = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getShareId = function() {
			return this.shareId;
		};
		prototype.setShareId = function(shareId) {
			this.shareId = shareId;
		};
		prototype.getLocation = function() {
			return this.location;
		};
		prototype.setLocation = function(location) {
			this.location = location;
		};
		prototype.getSize = function() {
			return this.size;
		};
		prototype.setSize = function(size) {
			this.size = size;
		};
		prototype.getStorageFormat = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasStorageFormat()) {
				return this.storageFormat;
			} else {
				throw new exceptions.NotFetchedException("Storage format has not been fetched.");
			}
		};
		prototype.setStorageFormat = function(storageFormat) {
			this.storageFormat = storageFormat;
		};
		/*
		 * @Deprecated
		 */
		prototype.getFileFormatType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasFileFormatType()) {
				return this.fileFormatType;
			} else {
				throw new exceptions.NotFetchedException("File format type has not been fetched.");
			}
		};
		/*
		 * @Deprecated
		 */
		prototype.setFileFormatType = function(fileFormatType) {
			this.fileFormatType = fileFormatType;
		};
		prototype.getLocatorType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasLocatorType()) {
				return this.locatorType;
			} else {
				throw new exceptions.NotFetchedException("Locator type has not been fetched.");
			}
		};
		prototype.setLocatorType = function(locatorType) {
			this.locatorType = locatorType;
		};
		prototype.getComplete = function() {
			return this.complete;
		};
		prototype.setComplete = function(complete) {
			this.complete = complete;
		};
		prototype.getStatus = function() {
			return this.status;
		};
		prototype.setStatus = function(status) {
			this.status = status;
		};
		prototype.isPresentInArchive = function() {
			return this.presentInArchive;
		};
		prototype.setPresentInArchive = function(presentInArchive) {
			this.presentInArchive = presentInArchive;
		};
		prototype.isStorageConfirmation = function() {
			return this.storageConfirmation;
		};
		prototype.setStorageConfirmation = function(storageConfirmation) {
			this.storageConfirmation = storageConfirmation;
		};
		prototype.getSpeedHint = function() {
			return this.speedHint;
		};
		prototype.setSpeedHint = function(speedHint) {
			this.speedHint = speedHint;
		};
		prototype.isArchivingRequested = function() {
			return this.archivingRequested;
		};
		prototype.setArchivingRequested = function(archivingRequested) {
			this.archivingRequested = archivingRequested;
		};
	}, {
		fetchOptions : "PhysicalDataFetchOptions",
		storageFormat : "StorageFormat",
		fileFormatType : "FileFormatType",
		locatorType : "LocatorType",
		complete : "Complete",
		status : "ArchivingStatus"
	});
	return PhysicalData;
})