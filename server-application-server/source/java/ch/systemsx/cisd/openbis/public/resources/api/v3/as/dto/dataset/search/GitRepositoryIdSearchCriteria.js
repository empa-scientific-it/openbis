define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var GitRepositoryIdSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "gitRepositoryId", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(GitRepositoryIdSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.GitRepositoryIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return GitRepositoryIdSearchCriteria;
})