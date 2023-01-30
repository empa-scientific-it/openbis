/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/dataset/search/ShareIdSearchCriteria", "as/dto/dataset/search/LocationSearchCriteria",
		"as/dto/dataset/search/SizeSearchCriteria", "as/dto/dataset/search/StorageFormatSearchCriteria", "as/dto/dataset/search/FileFormatTypeSearchCriteria",
		"as/dto/dataset/search/LocatorTypeSearchCriteria", "as/dto/dataset/search/CompleteSearchCriteria", "as/dto/dataset/search/StatusSearchCriteria",
		"as/dto/dataset/search/ArchivingRequestedSearchCriteria", "as/dto/dataset/search/PresentInArchiveSearchCriteria", "as/dto/dataset/search/StorageConfirmationSearchCriteria", "as/dto/dataset/search/SpeedHintSearchCriteria" ], function(require, stjs,
		AbstractCompositeSearchCriteria) {
	var PhysicalDataSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(PhysicalDataSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.PhysicalDataSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withShareId = function() {
			var ShareIdSearchCriteria = require("as/dto/dataset/search/ShareIdSearchCriteria");
			return this.addCriteria(new ShareIdSearchCriteria());
		};
		prototype.withLocation = function() {
			var LocationSearchCriteria = require("as/dto/dataset/search/LocationSearchCriteria");
			return this.addCriteria(new LocationSearchCriteria());
		};
		prototype.withSize = function() {
			var SizeSearchCriteria = require("as/dto/dataset/search/SizeSearchCriteria");
			return this.addCriteria(new SizeSearchCriteria());
		};
		prototype.withStorageFormat = function() {
			var StorageFormatSearchCriteria = require("as/dto/dataset/search/StorageFormatSearchCriteria");
			return this.addCriteria(new StorageFormatSearchCriteria());
		};
		/*
		 * @Deprecated
		 */
		prototype.withFileFormatType = function() {
			var FileFormatTypeSearchCriteria = require("as/dto/dataset/search/FileFormatTypeSearchCriteria");
			return this.addCriteria(new FileFormatTypeSearchCriteria());
		};
		prototype.withLocatorType = function() {
			var LocatorTypeSearchCriteria = require("as/dto/dataset/search/LocatorTypeSearchCriteria");
			return this.addCriteria(new LocatorTypeSearchCriteria());
		};
		prototype.withComplete = function() {
			var CompleteSearchCriteria = require("as/dto/dataset/search/CompleteSearchCriteria");
			return this.addCriteria(new CompleteSearchCriteria());
		};
		prototype.withStatus = function() {
			var StatusSearchCriteria = require("as/dto/dataset/search/StatusSearchCriteria");
			return this.addCriteria(new StatusSearchCriteria());
		};
		prototype.withArchivingRequested = function() {
			var ArchivingRequestedSearchCriteria = require("as/dto/dataset/search/ArchivingRequestedSearchCriteria");
			return this.addCriteria(new ArchivingRequestedSearchCriteria());
		};
		prototype.withPresentInArchive = function() {
			var PresentInArchiveSearchCriteria = require("as/dto/dataset/search/PresentInArchiveSearchCriteria");
			return this.addCriteria(new PresentInArchiveSearchCriteria());
		};
		prototype.withStorageConfirmation = function() {
			var StorageConfirmationSearchCriteria = require("as/dto/dataset/search/StorageConfirmationSearchCriteria");
			return this.addCriteria(new StorageConfirmationSearchCriteria());
		};
		prototype.withSpeedHint = function() {
			var SpeedHintSearchCriteria = require("as/dto/dataset/search/SpeedHintSearchCriteria");
			return this.addCriteria(new SpeedHintSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return PhysicalDataSearchCriteria;
})