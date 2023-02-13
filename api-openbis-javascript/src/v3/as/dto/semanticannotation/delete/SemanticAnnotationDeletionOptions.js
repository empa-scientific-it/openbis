/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var SemanticAnnotationDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(SemanticAnnotationDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SemanticAnnotationDeletionOptions;
})