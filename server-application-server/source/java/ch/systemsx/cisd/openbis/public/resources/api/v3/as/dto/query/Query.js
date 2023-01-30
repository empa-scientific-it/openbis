/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Query = function() {
	};
	stjs.extend(Query, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.Query';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.name = null;
		prototype.description = null;
		prototype.databaseId = null;
		prototype.databaseLabel = null;
		prototype.queryType = null;
		prototype.entityTypeCodePattern = null;
		prototype.sql = null;
		prototype.publicFlag = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.registrator = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getDatabaseId = function() {
			return this.databaseId;
		};
		prototype.setDatabaseId = function(databaseId) {
			this.databaseId = databaseId;
		};
		prototype.getDatabaseLabel = function() {
			return this.databaseLabel;
		};
		prototype.setDatabaseLabel = function(databaseLabel) {
			this.databaseLabel = databaseLabel;
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
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
	}, {
		fetchOptions : "QueryFetchOptions",
		permId : "IQueryId",
		queryType : "QueryType",
		databaseId : "IQueryDatabaseId",
		registrationDate : "Date",
		registrator : "Person",
		modificationDate : "Date"
	});
	return Query;
})