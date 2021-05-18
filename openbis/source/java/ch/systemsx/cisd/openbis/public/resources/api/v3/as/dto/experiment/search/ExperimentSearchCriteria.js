/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/project/search/ProjectSearchCriteria",
		"as/dto/experiment/search/ExperimentTypeSearchCriteria", "as/dto/common/search/IdentifierSearchCriteria",
		"as/dto/common/search/TextAttributeSearchCriteria"],
	function(require, stjs, AbstractEntitySearchCriteria, SearchOperator) {
	var ExperimentSearchCriteria = function() {
		AbstractEntitySearchCriteria.call(this);
	};
	stjs.extend(ExperimentSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.ExperimentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withIdentifier = function() {
			var IdentifierSearchCriteria = require("as/dto/common/search/IdentifierSearchCriteria");
			return this.addCriteria(new IdentifierSearchCriteria());
		};
		prototype.withType = function() {
			var ExperimentTypeSearchCriteria = require("as/dto/experiment/search/ExperimentTypeSearchCriteria");
			return this.addCriteria(new ExperimentTypeSearchCriteria());
		};
		prototype.withProject = function() {
			var ProjectSearchCriteria = require("as/dto/project/search/ProjectSearchCriteria");
			return this.addCriteria(new ProjectSearchCriteria());
		};
		prototype.withSubcriteria = function() {
			return this.addCriteria(new ExperimentSearchCriteria());
		};
		prototype.withTextAttribute = function() {
			var TextAttributeSearchCriteria = require("as/dto/common/search/TextAttributeSearchCriteria");
			return this.addCriteria(new TextAttributeSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.negate = function() {
			return AbstractEntitySearchCriteria.prototype.negate.call();
		};
	}, {
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ExperimentSearchCriteria;
})
