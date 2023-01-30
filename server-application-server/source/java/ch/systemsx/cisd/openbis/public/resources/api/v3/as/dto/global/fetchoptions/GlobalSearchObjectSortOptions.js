define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var GlobalSearchObjectSortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
		SCORE : "SCORE",
		OBJECT_KIND : "OBJECT_KIND",
		OBJECT_PERM_ID : "OBJECT_PERM_ID",
		OBJECT_IDENTIFIER : "OBJECT_IDENTIFIER"
	};

	stjs.extend(GlobalSearchObjectSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.fetchoptions.GlobalSearchObjectSortOptions';
		constructor.serialVersionUID = 1;
		prototype.score = function() {
			return this.getOrCreateSorting(fields.SCORE);
		};
		prototype.getScore = function() {
			return this.getSorting(fields.SCORE);
		};
		prototype.objectKind = function() {
			return this.getOrCreateSorting(fields.OBJECT_KIND);
		};
		prototype.getObjectKind = function() {
			return this.getSorting(fields.OBJECT_KIND);
		};
		prototype.objectPermId = function() {
			return this.getOrCreateSorting(fields.OBJECT_PERM_ID);
		};
		prototype.getObjectPermId = function() {
			return this.getSorting(fields.OBJECT_PERM_ID);
		};
		prototype.objectIdentifier = function() {
			return this.getOrCreateSorting(fields.OBJECT_IDENTIFIER);
		};
		prototype.getObjectIdentifier = function() {
			return this.getSorting(fields.OBJECT_IDENTIFIER);
		};
	}, {});
	return GlobalSearchObjectSortOptions;
})