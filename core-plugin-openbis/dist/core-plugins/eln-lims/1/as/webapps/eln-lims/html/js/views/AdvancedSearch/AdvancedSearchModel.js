/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function AdvancedSearchModel(forceSearch) {
	
	this.forceFreeTextSearch = null;
	this.forceLoadCriteria = false;
	this.criteria = {
			entityKind : null,
			logicalOperator : null,
			rules : { } // { "UUIDv4" : { type : "PROPERTY", name : "GENE", value : "aa" } }
	}
	this.savedSearches = []; // [{ sample: v3Sample, name: "name", criteria: { see this.criteria }}, ...]
	this.selcetedSavedSearchIndex = -1;
	this.searchStoreAvailable = null;

	if(typeof forceSearch === 'object') {
		this.criteria = forceSearch;
		this.forceLoadCriteria = true;
	} else {
		this.forceFreeTextSearch = forceSearch;
	}
	
	this.isSampleTypeForced = false;
	this.isAllRules = function() {
		for(ruleUUID in this.criteria.rules) {
			var rule = this.criteria.rules[ruleUUID];
			if(rule.type !== "All") {
				return false;
			}
		}
		return true;
	}
	
	this.setEntityKind = function(entityKind) {
		this.criteria.entityKind = entityKind;
	}

	// a hidden object type rule is used for the "Search For" dropdown
	this.getHiddenRule = function() {
		for(var ruleKey in this.criteria.rules) {
			if (this.criteria.rules[ruleKey].hidden) {
				return this.criteria.rules[ruleKey];
			}
		}
		return null;
	}

	this.resetModel = function(entityKind) {
		this.criteria.entityKind = entityKind;
		this.criteria.rules = {};
	}	
}
