/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var AttachmentCreation = function() {
	};
	stjs.extend(AttachmentCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.attachment.create.AttachmentCreation';
		constructor.serialVersionUID = 1;
		prototype.fileName = null;
		prototype.title = null;
		prototype.description = null;
		prototype.content = null;
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
		prototype.getContent = function() {
			return this.content;
		};
		prototype.setContent = function(content) {
			this.content = content;
		};
	}, {
		content : "byte[]"
	});
	return AttachmentCreation;
})