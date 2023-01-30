/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/common/fetchoptions/EmptyFetchOptions",
		"as/dto/attachment/fetchoptions/AttachmentSortOptions" ], function(require, stjs, FetchOptions) {
	var AttachmentFetchOptions = function() {
	};
	stjs.extend(AttachmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.attachment.fetchoptions.AttachmentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.previousVersion = null;
		prototype.content = null;
		prototype.sort = null;
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withPreviousVersion = function() {
			if (this.previousVersion == null) {
				this.previousVersion = new AttachmentFetchOptions();
			}
			return this.previousVersion;
		};
		prototype.withPreviousVersionUsing = function(fetchOptions) {
			return this.previousVersion = fetchOptions;
		};
		prototype.hasPreviousVersion = function() {
			return this.previousVersion != null;
		};
		prototype.withContent = function() {
			if (this.content == null) {
				var EmptyFetchOptions = require("as/dto/common/fetchoptions/EmptyFetchOptions");
				this.content = new EmptyFetchOptions();
			}
			return this.content;
		};
		prototype.withContentUsing = function(fetchOptions) {
			return this.content = fetchOptions;
		};
		prototype.hasContent = function() {
			return this.content != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var AttachmentSortOptions = require("as/dto/attachment/fetchoptions/AttachmentSortOptions");
				this.sort = new AttachmentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		previousVersion : "AttachmentFetchOptions",
		content : "EmptyFetchOptions",
		sort : "AttachmentSortOptions"
	});
	return AttachmentFetchOptions;
})