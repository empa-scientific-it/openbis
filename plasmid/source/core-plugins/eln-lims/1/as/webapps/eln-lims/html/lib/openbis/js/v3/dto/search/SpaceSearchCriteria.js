/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractObjectSearchCriteria", "dto/search/CodeSearchCriteria", "dto/search/PermIdSearchCriteria", "dto/search/AbstractCompositeSearchCriteria" ], function(
		stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, PermIdSearchCriteria, AbstractCompositeSearchCriteria) {
	var SpaceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SpaceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SpaceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.createBuilder = function() {
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
			builder.setName("SPACE");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return SpaceSearchCriteria;
})