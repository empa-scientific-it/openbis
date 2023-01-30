/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/BooleanFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, BooleanFieldSearchCriteria, SearchFieldType) {
	var StorageConfirmationSearchCriteria = function() {
		BooleanFieldSearchCriteria.call(this, "storageConfirmation", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(StorageConfirmationSearchCriteria, BooleanFieldSearchCriteria, [ BooleanFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.StorageConfirmationSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StorageConfirmationSearchCriteria;
})