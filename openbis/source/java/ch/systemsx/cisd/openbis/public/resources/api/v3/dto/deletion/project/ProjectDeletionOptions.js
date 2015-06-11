/**
 * @author pkupczyk
 */
define([ "stjs", "dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var ProjectDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(ProjectDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.project.ProjectDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ProjectDeletionOptions;
})