/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var SearchResult = function() {
		this.objects = [];
		this.totalCount = 0;
	};
	stjs.extend(SearchResult, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.SearchResult';
		constructor.serialVersionUID = 1;
		prototype.getObjects = function() {
			return this.objects;
		};
		prototype.setObjects = function(objects) {
			this.objects = objects;
		};
		prototype.getTotalCount = function() {
			return this.totalCount;
		};
		prototype.setTotalCount = function(totalCount) {
			this.totalCount = totalCount;
		};
	}, {
		objects : {
			name : "Collection",
			arguments : [ "Object" ]
		}
	});
	return SearchResult;
})
