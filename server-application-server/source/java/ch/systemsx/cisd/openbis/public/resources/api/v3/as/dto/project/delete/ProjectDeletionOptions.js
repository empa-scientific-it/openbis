/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var ProjectDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(ProjectDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.delete.ProjectDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ProjectDeletionOptions;
})