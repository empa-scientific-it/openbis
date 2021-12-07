define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractSearchCriteria" ], function(require, stjs, exceptions, AbstractSearchCriteria) {
	var GlobalSearchObjectKindCriteria = function() {
		this.objectKinds = [];
	};

	stjs.extend(GlobalSearchObjectKindCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.search.GlobalSearchObjectKindCriteria';
		constructor.serialVersionUID = 1;
		prototype.objectKinds = null;

		prototype.thatIn = function(objectKinds) {
			this.objectKinds = objectKinds;
		};
		prototype.getObjectKinds = function() {
			return this.objectKinds;
		};
		prototype.toString = function() {
			return "with object kinds " + this.objectKinds;
		}
	}, {
		objectKinds: {
			name: "List",
			arguments: ["GlobalSearchObjectKind"]
		}
	});

	return GlobalSearchObjectKindCriteria;
})
