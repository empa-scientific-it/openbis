/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var QueryUpdate = function() {
		this.name = new FieldUpdateValue();
		this.description = new FieldUpdateValue();
		this.databaseId = new FieldUpdateValue();
		this.queryType = new FieldUpdateValue();
		this.entityTypeCodePattern = new FieldUpdateValue();
		this.sql = new FieldUpdateValue();
		this.publicFlag = new FieldUpdateValue();
	};
	stjs.extend(QueryUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.update.QueryUpdate';
		constructor.serialVersionUID = 1;
		prototype.queryId = null;
		prototype.name = null;
		prototype.description = null;
		prototype.databaseId = null;
		prototype.queryType = null;
		prototype.entityTypeCodePattern = null;
		prototype.sql = null;
		prototype.publicFlag = null;

		prototype.getObjectId = function() {
			return this.getQueryId();
		};
		prototype.getQueryId = function() {
			return this.queryId;
		};
		prototype.setQueryId = function(queryId) {
			this.queryId = queryId;
		};
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name.setValue(name);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getDatabaseId = function() {
			return this.databaseId;
		};
		prototype.setDatabaseId = function(databaseId) {
			this.databaseId.setValue(databaseId);
		};
		prototype.getQueryType = function() {
			return this.queryType;
		};
		prototype.setQueryType = function(queryType) {
			this.queryType.setValue(queryType);
		};
		prototype.getEntityTypeCodePattern = function() {
			return this.entityTypeCodePattern;
		};
		prototype.setEntityTypeCodePattern = function(entityTypeCodePattern) {
			this.entityTypeCodePattern.setValue(entityTypeCodePattern);
		};
		prototype.getSql = function() {
			return this.sql;
		};
		prototype.setSql = function(sql) {
			this.sql.setValue(sql);
		};
		prototype.isPublic = function() {
			return this.publicFlag;
		};
		prototype.setPublic = function(publicFlag) {
			this.publicFlag.setValue(publicFlag);
		};
	}, {
		queryId : "IQueryId",
		name: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		description: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		databaseId: {
			name: "FieldUpdateValue",
			arguments: ["IQueryDatabaseId"]
		},
		queryType: {
			name: "FieldUpdateValue",
			arguments: ["QueryType"]
		},
		entityTypeCodePattern: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		sql: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		publicFlag: {
			name: "FieldUpdateValue",
			arguments: ["Boolean"]
		}
	});
	return QueryUpdate;
})