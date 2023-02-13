define([ "require", "stjs", "as/dto/common/fetchoptions/SortOrder", "as/dto/common/fetchoptions/Sorting" ], function(require, stjs, SortOrder, Sorting) {
	var SortOptions = function() {
		this.sortings = [];
	};

	stjs.extend(SortOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.SortOptions';
		constructor.serialVersionUID = 1;
		
		prototype.getOrCreateSorting = function(field) {
			return this.getOrCreateSortingWithParameters(field, null);
		};
		
		prototype.getOrCreateSortingWithParameters = function(field, parameters) {
			var order = this.getSorting(field);
			if (order == null) {
				order = new SortOrder();
				this.sortings.push(new Sorting(field, order, parameters));
			}
			return order;
		};
		
		prototype.getSorting = function(field) {
			var order = null;
			this.sortings.forEach(function(sorting) {
				if (field == sorting.getField()) {
					order = sorting.getOrder();
				}
			});
			return order;
		};
		prototype.getSortings = function() {
			return this.sortings;
		};
	}, {});
	return SortOptions;
})