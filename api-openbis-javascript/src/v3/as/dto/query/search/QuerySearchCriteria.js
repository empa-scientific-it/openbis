/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/NameSearchCriteria", "as/dto/common/search/DescriptionSearchCriteria",
		"as/dto/query/search/DatabaseIdSearchCriteria", "as/dto/query/search/QueryTypeSearchCriteria", "as/dto/query/search/SqlSearchCriteria",
		"as/dto/query/search/EntityTypeCodePatternSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var QuerySearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(QuerySearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.search.QuerySearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withName = function() {
			var NameSearchCriteria = require("as/dto/common/search/NameSearchCriteria");
			return this.addCriteria(new NameSearchCriteria());
		};
		prototype.withQueryType = function() {
			var QueryTypeSearchCriteria = require("as/dto/query/search/QueryTypeSearchCriteria");
			return this.addCriteria(new QueryTypeSearchCriteria());
		};
		prototype.withDatabaseId = function() {
			var DatabaseIdSearchCriteria = require("as/dto/query/search/DatabaseIdSearchCriteria");
			return this.addCriteria(new DatabaseIdSearchCriteria());
		};
		prototype.withDescription = function() {
			var DescriptionSearchCriteria = require("as/dto/common/search/DescriptionSearchCriteria");
			return this.addCriteria(new DescriptionSearchCriteria());
		};
		prototype.withEntityTypeCodePattern = function() {
			var EntityTypeCodePatternSearchCriteria = require("as/dto/query/search/EntityTypeCodePatternSearchCriteria");
			return this.addCriteria(new EntityTypeCodePatternSearchCriteria());
		};
		prototype.withSql = function() {
			var SqlSearchCriteria = require("as/dto/query/search/SqlSearchCriteria");
			return this.addCriteria(new SqlSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return QuerySearchCriteria;
})