/**
 * @author pkupczyk
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ArchivingStatus = function() {
		Enum.call(this, [ "AVAILABLE", "LOCKED", "ARCHIVED", "UNARCHIVE_PENDING", "ARCHIVE_PENDING", "BACKUP_PENDING" ]);
	};
	stjs.extend(ArchivingStatus, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ArchivingStatus();
})