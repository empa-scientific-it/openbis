define([ "stjs" ], function(stjs) {
	var SearchDomainServiceExecutionResult = function() {
	};
	stjs.extend(SearchDomainServiceExecutionResult, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.SearchDomainServiceExecutionResult';
		constructor.serialVersionUID = 1;
		prototype.servicePermId = null;
		prototype.searchDomainName = null;
		prototype.searchDomainLabel = null;
		prototype.entityKind = null;
		prototype.entityType = null;
		prototype.entityIdentifier = null;
		prototype.entityPermId = null;
		prototype.resultDetails = null;
		prototype.getServicePermId = function() {
			return this.servicePermId;
		};
		prototype.setServicePermId = function(servicePermId) {
			this.servicePermId = servicePermId;
		}
		prototype.getSearchDomainName = function() {
			return this.searchDomainName;
		};
		prototype.setSearchDomainName = function(searchDomainName) {
			this.searchDomainName = searchDomainName;
		}
		prototype.getSearchDomainLabel = function() {
			return this.searchDomainLabel;
		};
		prototype.setSearchDomainLabel = function(searchDomainLabel) {
			this.searchDomainLabel = searchDomainLabel;
		}
		prototype.getEntityKind = function() {
			return this.entityKind;
		};
		prototype.setEntityKind = function(entityKind) {
			this.entityKind = entityKind;
		}
		prototype.getEntityType = function() {
			return this.entityType;
		};
		prototype.setEntityType = function(entityType) {
			this.entityType = entityType;
		}
		prototype.getEntityIdentifier = function() {
			return this.entityIdentifier;
		};
		prototype.setEntityIdentifier = function(entityIdentifier) {
			this.entityIdentifier = entityIdentifier;
		}
		prototype.getEntityPermId = function() {
			return this.entityPermId;
		};
		prototype.setEntityPermId = function(entityPermId) {
			this.entityPermId = entityPermId;
		}
		prototype.getResultDetails = function() {
			return this.resultDetails;
		};
		prototype.setResultDetails = function(resultDetails) {
			this.entityPresultDetailsermId = resultDetails;
		}
	}, {
		servicePermId: "DssServicePermId",
		entityKind: "EntityKind",
		resultDetails: {
			name: "Map",
			arguments: ["String", "String"]
		},
	}
	);
	return SearchDomainServiceExecutionResult;
})
