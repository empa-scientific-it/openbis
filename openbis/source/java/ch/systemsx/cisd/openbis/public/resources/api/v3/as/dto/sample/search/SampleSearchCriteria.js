/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/sample/search/SampleSearchRelation",
		"as/dto/space/search/SpaceSearchCriteria", "as/dto/project/search/ProjectSearchCriteria", "as/dto/project/search/NoProjectSearchCriteria", "as/dto/experiment/search/ExperimentSearchCriteria",
		"as/dto/experiment/search/NoExperimentSearchCriteria", "as/dto/sample/search/NoSampleContainerSearchCriteria", "as/dto/sample/search/SampleTypeSearchCriteria",
		"as/dto/common/search/IdentifierSearchCriteria", "as/dto/common/search/TextAttributeSearchCriteria" ],
	function(require, stjs, AbstractEntitySearchCriteria, SearchOperator, SampleSearchRelation) {

	var AbstractSampleSearchCriteria = function(relation) {
		AbstractEntitySearchCriteria.call(this);
		this.relation = relation ? relation : SampleSearchRelation.SAMPLE;
	};
	stjs.extend(AbstractSampleSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.AbstractSampleSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withIdentifier = function() {
			var IdentifierSearchCriteria = require("as/dto/common/search/IdentifierSearchCriteria");
			return this.addCriteria(new IdentifierSearchCriteria());
		};
		prototype.withSpace = function() {
			var SpaceSearchCriteria = require("as/dto/space/search/SpaceSearchCriteria");
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.withoutSpace = function() {
			var NoSpaceSearchCriteria = require("as/dto/space/search/NoSpaceSearchCriteria");
			return this.addCriteria(new NoSpaceSearchCriteria());
		};
		prototype.withProject = function() {
			var ProjectSearchCriteria = require("as/dto/project/search/ProjectSearchCriteria");
			return this.addCriteria(new ProjectSearchCriteria());
		};
		prototype.withoutProject = function() {
			var NoProjectSearchCriteria = require("as/dto/project/search/NoProjectSearchCriteria");
			return this.addCriteria(new NoProjectSearchCriteria());
		};
		prototype.withExperiment = function() {
			var ExperimentSearchCriteria = require("as/dto/experiment/search/ExperimentSearchCriteria");
			return this.addCriteria(new ExperimentSearchCriteria());
		};
		prototype.withoutExperiment = function() {
			var NoExperimentSearchCriteria = require("as/dto/experiment/search/NoExperimentSearchCriteria");
			this.addCriteria(new NoExperimentSearchCriteria());
			return this;
		};
		prototype.withoutContainer = function() {
			var NoSampleContainerSearchCriteria = require("as/dto/sample/search/NoSampleContainerSearchCriteria");
			this.addCriteria(new NoSampleContainerSearchCriteria());
			return this;
		};
		prototype.withType = function() {
			var SampleTypeSearchCriteria = require("as/dto/sample/search/SampleTypeSearchCriteria");
			return this.addCriteria(new SampleTypeSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.getRelation = function() {
			return this.relation;
		};
	}, {
		relation : {
			name : "Enum",
			arguments : [ "SampleSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});

	var SampleSearchCriteria = function() {
		AbstractSampleSearchCriteria.call(this, SampleSearchRelation.SAMPLE);
	};
	stjs.extend(SampleSearchCriteria, AbstractSampleSearchCriteria, [ AbstractSampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SampleSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withParents = function() {
			return this.addCriteria(new SampleParentsSearchCriteria());
		};
		prototype.withChildren = function() {
			return this.addCriteria(new SampleChildrenSearchCriteria());
		};
		prototype.withContainer = function() {
			return this.addCriteria(new SampleContainerSearchCriteria());
		};
		prototype.withSubcriteria = function() {
			return this.addCriteria(new SampleSearchCriteria());
		};
		prototype.withTextAttribute = function() {
			var TextAttributeSearchCriteria = require("as/dto/common/search/TextAttributeSearchCriteria");
			return this.addCriteria(new TextAttributeSearchCriteria());
		};
		prototype.negate = function() {
			return AbstractSampleSearchCriteria.negate();
		};
	}, {
		relation : {
			name : "Enum",
			arguments : [ "SampleSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});

	var SampleParentsSearchCriteria = function() {
		AbstractSampleSearchCriteria.call(this, SampleSearchRelation.PARENTS);
	};
	stjs.extend(SampleParentsSearchCriteria, AbstractSampleSearchCriteria, [ AbstractSampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SampleParentsSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "SampleSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});

	var SampleChildrenSearchCriteria = function() {
		AbstractSampleSearchCriteria.call(this, SampleSearchRelation.CHILDREN);
	};
	stjs.extend(SampleChildrenSearchCriteria, AbstractSampleSearchCriteria, [ AbstractSampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SampleChildrenSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "SampleSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});

	var SampleContainerSearchCriteria = function() {
		AbstractSampleSearchCriteria.call(this, SampleSearchRelation.CONTAINER);
	};
	stjs.extend(SampleContainerSearchCriteria, AbstractSampleSearchCriteria, [ AbstractSampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SampleContainerSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "SampleSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});

	return SampleSearchCriteria;
})
