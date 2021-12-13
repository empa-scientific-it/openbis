/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var QueryCreation = function() {
	};
	stjs.extend(QueryCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.create.QueryCreation';
		constructor.serialVersionUID = 1;
		prototype.name = null;
		prototype.databaseId = null;
		prototype.queryType = null;
		prototype.entityTypeCodePattern = null;
		prototype.description = null;
		prototype.sql = null;
		prototype.publicFlag = false;

		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getDatabaseId = function() {
			return this.databaseId;
		};
		prototype.setDatabaseId = function(databaseId) {
			this.databaseId = databaseId;
		};
		prototype.getQueryType = function() {
			return this.queryType;
		};
		prototype.setQueryType = function(queryType) {
			this.queryType = queryType;
		};
		prototype.getEntityTypeCodePattern = function() {
			return this.entityTypeCodePattern;
		};
		prototype.setEntityTypeCodePattern = function(entityTypeCodePattern) {
			this.entityTypeCodePattern = entityTypeCodePattern;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getSql = function() {
			return this.sql;
		};
		prototype.setSql = function(sql) {
			this.sql = sql;
		};
		prototype.isPublic = function() {
			return this.publicFlag;
		};
		prototype.setPublic = function(publicFlag) {
			this.publicFlag = publicFlag;
		};
		prototype.getPublicFlag = function() {
			return this.publicFlag;
		};
		prototype.setPublicFlag = function(publicFlag) {
			this.publicFlag = publicFlag;
		};
	}, {
		databaseId: "IQueryDatabaseId",
		queryType: "QueryType"
	});
	return QueryCreation;
})