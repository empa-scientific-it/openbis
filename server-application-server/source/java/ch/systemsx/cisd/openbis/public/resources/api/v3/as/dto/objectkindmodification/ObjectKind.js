
/**
 * Enumeration of object kinds. An object kind is an attribute an object (like a sample, a data set type or a vocabulary term)
 * which can be created, deleted or updated.
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ObjectKind = function() {
		Enum.call(this, [ "AUTHORIZATION_GROUP", "SAMPLE", "EXPERIMENT", "MATERIAL", "DATA_SET", "SAMPLE_TYPE", 
		                  "EXPERIMENT_TYPE", "MATERIAL_TYPE", "DATASET_TYPE", "FILE_FORMAT_TYPE", "PROJECT", 
		                  "SPACE", "PROPERTY_TYPE", "PROPERTY_TYPE_ASSIGNMENT", "VOCABULARY", "VOCABULARY_TERM", 
		                  "ROLE_ASSIGNMENT", "PERSON", "GRID_CUSTOM_FILTER", "GRID_CUSTOM_COLUMN", "SCRIPT", 
		                  "DELETION", "POSTREGISTRATION_QUEUE", "QUERY", "METAPROJECT", "PERSONAL_ACCESS_TOKEN" ]);
	};
	stjs.extend(ObjectKind, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ObjectKind();
})
