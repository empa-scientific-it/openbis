/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Attachment = function() {
	};
	stjs.extend(Attachment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.attachment.Attachment';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.fileName = null;
		prototype.title = null;
		prototype.description = null;
		prototype.permlink = null;
		prototype.latestVersionPermlink = null;
		prototype.version = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.previousVersion = null;
		prototype.content = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getFileName = function() {
			return this.fileName;
		};
		prototype.setFileName = function(fileName) {
			this.fileName = fileName;
		};
		prototype.getTitle = function() {
			return this.title;
		};
		prototype.setTitle = function(title) {
			this.title = title;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getPermlink = function() {
			return this.permlink;
		};
		prototype.setPermlink = function(permlink) {
			this.permlink = permlink;
		};
		prototype.getLatestVersionPermlink = function() {
			return this.latestVersionPermlink;
		};
		prototype.setLatestVersionPermlink = function(latestVersionPermlink) {
			this.latestVersionPermlink = latestVersionPermlink;
		};
		prototype.getVersion = function() {
			return this.version;
		};
		prototype.setVersion = function(version) {
			this.version = version;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getPreviousVersion = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPreviousVersion()) {
				return this.previousVersion;
			} else {
				throw new exceptions.NotFetchedException("Previous version of attachment  has not been fetched.");
			}
		};
		prototype.setPreviousVersion = function(previousVersion) {
			this.previousVersion = previousVersion;
		};
		prototype.getContent = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasContent()) {
				return this.content;
			} else {
				throw new exceptions.NotFetchedException("Content has not been fetched.");
			}
		};
		prototype.setContent = function(content) {
			this.content = content;
		};
		prototype.toString = function() {
			return "Attachment " + this.fileName + ":" + this.version;
		};
	}, {
		fetchOptions : "AttachmentFetchOptions",
		registrationDate : "Date",
		registrator : "Person",
		previousVersion : "Attachment",
		content : "byte[]"
	});
	return Attachment;
})