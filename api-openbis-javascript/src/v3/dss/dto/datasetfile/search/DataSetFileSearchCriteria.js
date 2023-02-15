/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/dataset/search/DataSetSearchCriteria" ], function(require, stjs, AbstractCompositeSearchCriteria,
		DataSetSearchCriteria) {
	var DataSetFileSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(DataSetFileSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.search.DataSetFileSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withDataSet = function() {
			var DataSetSearchCriteria = require("as/dto/dataset/search/DataSetSearchCriteria");
			return this.addCriteria(new DataSetSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return DataSetFileSearchCriteria;
})