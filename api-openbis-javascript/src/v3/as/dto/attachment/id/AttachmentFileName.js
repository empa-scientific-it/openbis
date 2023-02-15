/**
 * Attachment file name.
 * 
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/attachment/id/IAttachmentId" ], function(stjs, exceptions, IAttachmentId) {
	/**
	 * @param fileName
	 *            Attachment file name, e.g. "my_file.txt".
	 */
	var AttachmentFileName = function(fileName) {
		this.setFileName(fileName);
	};
	stjs.extend(AttachmentFileName, null, [ IAttachmentId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.attachment.id.AttachmentFileName';
		constructor.serialVersionUID = 1;
		prototype.fileName = null;
		prototype.getFileName = function() {
			return this.fileName;
		};
		prototype.setFileName = function(fileName) {
			if (fileName == null) {
				throw new exceptions.IllegalArgumentException("File name cannot be null");
			}
			this.fileName = fileName;
		};
		prototype.toString = function() {
			return this.getFileName();
		};
		prototype.hashCode = function() {
			return ((this.getFileName() == null) ? 0 : this.getFileName().hashCode());
		};
		prototype.equals = function(obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			var other = obj;
			return this.getFileName() == null ? this.getFileName() == other.getFileName() : this.getFileName().equals(other.getFileName());
		};
	}, {});
	return AttachmentFileName;
})