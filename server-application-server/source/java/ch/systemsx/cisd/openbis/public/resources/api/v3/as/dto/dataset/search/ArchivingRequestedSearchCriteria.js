/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/BooleanFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, BooleanFieldSearchCriteria, SearchFieldType) {
	var ArchivingRequestedSearchCriteria = function() {
		BooleanFieldSearchCriteria.call(this, "archivingRequested", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(ArchivingRequestedSearchCriteria, BooleanFieldSearchCriteria, [ BooleanFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.ArchivingRequestedSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return ArchivingRequestedSearchCriteria;
})