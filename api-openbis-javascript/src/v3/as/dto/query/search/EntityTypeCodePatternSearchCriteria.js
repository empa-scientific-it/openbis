/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var EntityTypeCodePatternSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "entityTypeCodePattern", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EntityTypeCodePatternSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.search.EntityTypeCodePatternSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EntityTypeCodePatternSearchCriteria;
})