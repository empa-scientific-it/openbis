/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var GitCommitHashSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "gitCommitHash", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(GitCommitHashSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.GitCommitHashSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return GitCommitHashSearchCriteria;
})