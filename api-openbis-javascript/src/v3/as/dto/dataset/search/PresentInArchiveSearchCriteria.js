/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/BooleanFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, BooleanFieldSearchCriteria, SearchFieldType) {
	var PresentInArchiveSearchCriteria = function() {
		BooleanFieldSearchCriteria.call(this, "presentInArchive", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PresentInArchiveSearchCriteria, BooleanFieldSearchCriteria, [ BooleanFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.PresentInArchiveSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PresentInArchiveSearchCriteria;
})