/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/dataset/search/DataSetSearchRelation",
		"as/dto/experiment/search/ExperimentSearchCriteria", "as/dto/experiment/search/NoExperimentSearchCriteria", "as/dto/sample/search/SampleSearchCriteria",
		"as/dto/sample/search/NoSampleSearchCriteria", "as/dto/dataset/search/DataSetTypeSearchCriteria", "as/dto/dataset/search/PhysicalDataSearchCriteria",
		"as/dto/dataset/search/LinkedDataSearchCriteria", "as/dto/common/search/TextAttributeSearchCriteria" ],
	function(require, stjs, AbstractEntitySearchCriteria, SearchOperator, DataSetSearchRelation) {
	var AbstractDataSetSearchCriteria = function(relation) {
		AbstractEntitySearchCriteria.call(this);
		this.relation = relation ? relation : DataSetSearchRelation.DATASET;
	};
	stjs.extend(AbstractDataSetSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.AbstractDataSetSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withType = function() {
			var DataSetTypeSearchCriteria = require("as/dto/dataset/search/DataSetTypeSearchCriteria");
			return this.addCriteria(new DataSetTypeSearchCriteria());
		};
		prototype.withPhysicalData = function() {
			var PhysicalDataSearchCriteria = require("as/dto/dataset/search/PhysicalDataSearchCriteria");
			return this.addCriteria(new PhysicalDataSearchCriteria());
		};
		prototype.withLinkedData = function() {
			var LinkedDataSearchCriteria = require("as/dto/dataset/search/LinkedDataSearchCriteria");
			return this.addCriteria(new LinkedDataSearchCriteria());
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
		prototype.withSample = function() {
			var SampleSearchCriteria = require("as/dto/sample/search/SampleSearchCriteria");
			return this.addCriteria(new SampleSearchCriteria());
		};
		prototype.withoutSample = function() {
			var NoSampleSearchCriteria = require("as/dto/sample/search/NoSampleSearchCriteria");
			this.addCriteria(new NoSampleSearchCriteria());
			return this;
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
			arguments : [ "DataSetSearchRelation" ]
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

	var DataSetSearchCriteria = function() {
		AbstractDataSetSearchCriteria.call(this, DataSetSearchRelation.PARENTS);
	};
	// var TextAttributeSearchCriteria = function(relation) {
	// 	AbstractSearchCriteria.call(this);
	// };
	stjs.extend(DataSetSearchCriteria, AbstractDataSetSearchCriteria, [ AbstractDataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.DataSetSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withParents = function() {
			return this.addCriteria(new DataSetParentsSearchCriteria());
		};
		prototype.withChildren = function() {
			return this.addCriteria(new DataSetChildrenSearchCriteria());
		};
		prototype.withContainer = function() {
			return this.addCriteria(new DataSetContainerSearchCriteria());
		};
		prototype.withSubcriteria = function() {
			return this.addCriteria(new DataSetSearchCriteria());
		};
		prototype.withTextAttribute = function() {
			var TextAttributeSearchCriteria = require("as/dto/common/search/TextAttributeSearchCriteria");
			return this.addCriteria(new TextAttributeSearchCriteria());
		};
		prototype.negate = function() {
			return AbstractDataSetSearchCriteria.prototype.negate.call();
		};
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
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

	var DataSetParentsSearchCriteria = function() {
		AbstractDataSetSearchCriteria.call(this, DataSetSearchRelation.PARENTS);
	};
	stjs.extend(DataSetParentsSearchCriteria, AbstractDataSetSearchCriteria, [ AbstractDataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.DataSetParentsSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
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

	var DataSetChildrenSearchCriteria = function() {
		AbstractDataSetSearchCriteria.call(this, DataSetSearchRelation.CHILDREN);
	};
	stjs.extend(DataSetChildrenSearchCriteria, AbstractDataSetSearchCriteria, [ AbstractDataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.DataSetChildrenSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
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

	var DataSetContainerSearchCriteria = function() {
		AbstractDataSetSearchCriteria.call(this, DataSetSearchRelation.CONTAINER);
	};
	stjs.extend(DataSetContainerSearchCriteria, AbstractDataSetSearchCriteria, [ AbstractDataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.DataSetContainerSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
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

	return DataSetSearchCriteria;
})
