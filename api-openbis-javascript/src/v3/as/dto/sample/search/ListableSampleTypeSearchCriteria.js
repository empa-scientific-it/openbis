define([ "stjs", "as/dto/common/search/AbstractSearchCriteria" ], function(stjs, AbstractSearchCriteria) {
	var ListableSampleTypeSearchCriteria = function() {
		AbstractSearchCriteria.call(this);
	};
	stjs.extend(ListableSampleTypeSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.ListableSampleTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		
		prototype.listable;
	    
		prototype.thatEquals = function(value) {
	        this.setListable(value);
	    }

		prototype.setListable = function(value) {
	        this.listable = value;
	    }

		prototype.isListable = function()
	    {
	        return this.listable;
	    }

	}, {});
	return ListableSampleTypeSearchCriteria;
})
