define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var EntitySortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
		CODE : "CODE",
		PERM_ID : "PERM_ID",
		REGISTRATION_DATE : "REGISTRATION_DATE",
		MODIFICATION_DATE : "MODIFICATION_DATE"
	};

	stjs.extend(EntitySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.EntitySortOptions';
		constructor.serialVersionUID = 1;
		prototype.code = function() {
			return this.getOrCreateSorting(fields.CODE);
		};
		prototype.getCode = function() {
			return this.getSorting(fields.CODE);
		};
		prototype.permId = function() {
			return this.getOrCreateSorting(fields.PERM_ID);
		};
		prototype.getPermId = function() {
			return this.getSorting(fields.PERM_ID);
		};
		prototype.registrationDate = function() {
			return this.getOrCreateSorting(fields.REGISTRATION_DATE);
		};
		prototype.getRegistrationDate = function() {
			return this.getSorting(fields.REGISTRATION_DATE);
		};
		prototype.modificationDate = function() {
			return this.getOrCreateSorting(fields.MODIFICATION_DATE);
		};
		prototype.getModificationDate = function() {
			return this.getSorting(fields.MODIFICATION_DATE);
		};
	}, {});
	return EntitySortOptions;
})