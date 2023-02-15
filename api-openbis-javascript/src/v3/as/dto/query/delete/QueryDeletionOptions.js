/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var QueryDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(QueryDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.delete.QueryDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return QueryDeletionOptions;
})