define([ "require", "stjs", "as/dto/common/fetchoptions/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var ProjectSortOptions = function() {
		EntitySortOptions.call(this);
	};

	var fields = {
		IDENTIFIER : "IDENTIFIER"
	};

	stjs.extend(ProjectSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.fetchoptions.ProjectSortOptions';
		constructor.serialVersionUID = 1;

		prototype.identifier = function() {
			return this.getOrCreateSorting(fields.IDENTIFIER);
		};
		prototype.getIdentifier = function() {
			return this.getSorting(fields.IDENTIFIER);
		};
	}, {});
	return ProjectSortOptions;
})